package server;



import java.util.concurrent.ExecutorService;
import java.io.IOException;
import common.DropboxConstants;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.*;
import java.util.LinkedList;
import java.util.Queue;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import util.BytesConverter;
/**
 * Class: DropboxServer
 * Description: Receives file stream, parses the stream and synchronizes it to the disk
 */

public class DropboxServer {

    class Keys {
        KeyPair keyPair;
        SecretKey secretKey;
        Keys(KeyPair keyPair, SecretKey secretKey) {
            this.keyPair = keyPair;
            this.secretKey = secretKey;
        }
    }
    
    private ExecutorService threadService = java.util.concurrent.Executors.newFixedThreadPool(DropboxConstants.MAX_CLIENT_NUM);
    private Queue<Keys> clientKeyQueue = new LinkedList<Keys>();
    private HashMap<String, Keys> clientkeys = new HashMap<String, Keys>();
    SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

    private KeyPair generateKeyPairForClient() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA","SUN");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG","SUN");
        keyGen.initialize(1024, random);
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }

    private SecretKey generateSecretKeyForClient() throws NoSuchAlgorithmException {
        KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
		SecretKey encryptionKey = keygenerator.generateKey();
        return encryptionKey;
    }

    private SecretKey generateSecretKeyForServer() throws NoSuchAlgorithmException {
        KeyGenerator keygenerator = KeyGenerator.getInstance("RC4");
		SecretKey encryptionKey = keygenerator.generateKey();
        return encryptionKey;
    }
    
    public DropboxServer() throws NoSuchProviderException, NoSuchAlgorithmException {
        SynchronizationService.serversideFileCryptionKey = this.generateSecretKeyForServer();
        for(int i = 0; i < DropboxConstants.MAX_CLIENT_NUM; i++) {
            KeyPair keyPair = this.generateKeyPairForClient();
            SecretKey cryptionKey = this.generateSecretKeyForClient();
            this.clientKeyQueue.add(new Keys(keyPair, cryptionKey));
            
            byte[] bytes = keyPair.getPrivate().getEncoded();
            String byteString = BytesConverter.byteArray2HexString(bytes);
            System.out.println("Server generates authenication key for client " + i +" : "+ byteString);
            bytes = cryptionKey.getEncoded();
            byteString = BytesConverter.byteArray2HexString(bytes);
            System.out.println("Server generates encryption key for client " + i +" : "+ byteString);
        }
    }
    
    public void listen() {
    	SSLSocket s = null;
        SSLServerSocket serverSocket = null;
    	try {
            System.out.println(DropboxConstants.SERVER_PORT);
        	//serverSocket = new ServerSocket(DropboxConstants.SERVER_PORT);
            serverSocket = (SSLServerSocket)this.sslserversocketfactory.createServerSocket(DropboxConstants.SERVER_PORT);
        	while(true) {
                try {
                    s = (SSLSocket) serverSocket.accept();
                    
                    String ip = s.getInetAddress().getHostAddress();
                    if(!this.clientkeys.containsKey(ip)) {
                        System.out.println("A new client comes in, pick up keys for him");
                        Keys keys = this.clientKeyQueue.poll();
                        this.clientkeys.put(ip, keys);
                    }
                    PublicKey pubKey = this.clientkeys.get(ip).keyPair.getPublic();
                    SecretKey encryptionKey = this.clientkeys.get(ip).secretKey;
                    SynchronizationService service = new SynchronizationService(s, pubKey, encryptionKey);
                    threadService.submit(service);
                }
                catch(Exception ex) {
                    System.out.println("Socket Error or File Processing Error!");
                    Logger.getLogger(DropboxServer.class.getName()).log(Level.SEVERE, null, ex);
                }
        	}
       }
       catch(IOException ex) {
           System.out.println("DropboxConstants.SERVER_PORT has already in use");
           Logger.getLogger(DropboxServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                if(serverSocket != null)
                    serverSocket.close();
                if(s != null)
                    s.close();
           } catch (IOException ex) {
                Logger.getLogger(DropboxServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException  {
    	DropboxServer server = new DropboxServer();
    	System.out.println("Server started..");
    	server.listen();
    }
}