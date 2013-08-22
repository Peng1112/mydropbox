package client;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;
import util.DropboxFileTransferProtocolSerializer;
import util.DropboxFileUtil;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import common.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import security.MessageAuthenticationDataWriter;
import util.BytesConverter;
import util.DataReader;
import util.DataWriter;

/**
 * Class: DropboxClient
 * Description: synchronizes all of the files & dirs change in the syncDirectory to the remote server whose IP is specified by the argument
 * Example: Java client/DropboxClient 192.161.3.57
 */

public class DropboxClient implements Runnable {
    private String serverHost;
    private int serverPort;
    private SSLSocket socket;
    private DropboxServerFileChangeHandler serverFileChangeHandler = null;
    private HashMap<String, FileStructure> prevFileList = new HashMap<String, FileStructure>();
    private HashMap<String, FileStructure> currFileList = new HashMap<String, FileStructure>();
    private DropboxFileUtil dropboxUtil;
    private String dropboxClientDir;
    private PrivateKey authenticationPrivateKey;
    private SecretKey messageCryptionKey;
    private DataWriter dataWriter;
    private DataReader dataReader;
    private Cipher cipher;
    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    //private String dropboxPrevFileListDataDir;
    
    public DropboxClient(String host, String dropboxClientDir, String hexAuthenticationPrivateKey, String hexMessageCryptionKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException {
        this.serverHost = host;
        this.serverPort = DropboxConstants.SERVER_PORT;
        this.dropboxClientDir = dropboxClientDir;
        dropboxUtil = new DropboxFileUtil(this.dropboxClientDir);
        this.prevFileList = this.serializePrevFileList();
        
        /* convert the hex string representation back to the authentication key object */
        byte[] bytes = BytesConverter.hexString2ByteArray(hexAuthenticationPrivateKey);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
        this.authenticationPrivateKey = keyFactory.generatePrivate(privateKeySpec);

        /* convert the hex string representation back to the message cryption key object */
        bytes = BytesConverter.hexString2ByteArray(hexMessageCryptionKey);
        this.messageCryptionKey = new SecretKeySpec(bytes, 0, bytes.length, "DES");

        /*Init Cipher for message en/de-cryption*/
        this.cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        this.dataReader = new DataReader();        
    }

    public String getDropboxClientDir() {
        return this.dropboxClientDir;
    }

    public String getDropboxClientMetaDataDir() {
          
        // The meta data dir should be in the parent dir of the sync dir
        // Ex. sync dir: "/root/user/dropbox"
        //     meta data dir: "/root/user/.dropbox_temp_file"

        // Should check whether the parent dir of the dropboxClientDir exist
        String dropboxClientMetaDataDir = dropboxClientDir.substring(0, dropboxClientDir.lastIndexOf(System.getProperty("file.separator")));
        dropboxClientMetaDataDir += "/dropbox_temp_file";
        return dropboxClientMetaDataDir;
    }
    
    public void processFileSyncFromServer(SSLSocket socket) throws Exception {
        this.serverFileChangeHandler = new DropboxServerFileChangeHandler(socket, this.dropboxClientDir, this.getDropboxClientMetaDataDir(), 
                                                                          this.dataReader, this.dataWriter, this.cipher, this.messageCryptionKey);
        this.serverFileChangeHandler.setPrevFileList(prevFileList);
        this.serverFileChangeHandler.handleServerFileChange();
        this.resetSignature();
    }
    
    public void run() {       
        while(true) {
            try {
                startConnection(this.serverHost, this.serverPort);
                processFileSyncFromServer(socket);
                currFileList = dropboxUtil.getAllFiles(dropboxClientDir, true);
                syncClientFileChangeToServer(socket.getOutputStream());
                Thread.sleep(4000);
            } catch (Exception ex) {
                Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally {
                endConnection();
            }         
        }
    }

    public void initSignature() throws Exception {
        Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
        dsa.initSign(authenticationPrivateKey);
        // also need to update the dataWriter!
        this.dataWriter = new MessageAuthenticationDataWriter(dsa);
    }
    
    public void resetSignature() throws Exception {
        Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
        dsa.initSign(authenticationPrivateKey);
        // also need to update the dataWriter!
        this.dataWriter = new MessageAuthenticationDataWriter(dsa);
        this.serverFileChangeHandler.resetClientDataWriter(dataWriter);
    }
    
    public void startConnection(String serverHost, int serverPort) throws Exception {
        socket = (SSLSocket) sslsocketfactory.createSocket(InetAddress.getByName(this.serverHost), serverPort);
        this.initSignature();
    }

    public void endConnection() {
        try {
            if(socket != null)
                socket.close();
        } catch (IOException ex) {
            System.out.println("closing connection error");
            Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
     // synchronize all of the file change in the syncDirectory to the output stream
     public void syncClientFileChangeToServer(OutputStream os) throws IOException, SignatureException, Exception {
        HashMap<String, FileOperation> fileOperations = new HashMap();
        Iterator it = prevFileList.entrySet().iterator();

        HashSet<String> newlyDeletedFileSet = this.serverFileChangeHandler.getNewlyDeletedFileSet();

        // find the deleted file/dir
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            String fileName = (String) pairs.getKey();
            
            FileStructure fileInCurrList = currFileList.get(fileName);
            // if current file list contains the prev file, do not delete
            if(fileInCurrList != null)
                continue;
            else {
                Iterator it2 = currFileList.entrySet().iterator();
                while(it2.hasNext()) {
                    Map.Entry pairs2 = (Map.Entry)it2.next();
                    String fileName2 = (String) pairs2.getKey();
                    if(fileName2.startsWith(fileName))
                        break;
                }

                if(it2.hasNext() == false && !newlyDeletedFileSet.contains(fileName)) {
                    File tmpFile = new File(this.dropboxClientDir+fileName+DropboxConstants.CONFLICT_MARK);
                    // deletion of conflict file will not be uploaded to server, server side conflict file will not be deleted!
                    if(fileInCurrList == null && !tmpFile.exists()) {
                        //System.out.println(fileName);
                        String path = dropboxUtil.getDeletedParentRelativePath(fileName);
                        fileOperations.put(path, new FileOperation(DropboxConstants.DELETE, new File(path)));
                    }
                }
            }
        }
        
        HashSet<String> newlyUpdatedFileSet = this.serverFileChangeHandler.getNewlyUpdatedFileSet();
        
        // find the newly created file/dir and the modified file
        for (Map.Entry<String, FileStructure> entry : currFileList.entrySet()) {
             String fileName = entry.getKey();
             File file = entry.getValue().file;
             FileStructure fileInPrevList = prevFileList.get(fileName);

             // Creation or updating conclict File won't be uploaded to server
             if(fileName.endsWith(DropboxConstants.CONFLICT_MARK)) {
                 //fileOperations.put(fileName, new FileOperation(DropboxConstants.RENAME, file));
                 //System.out.println("this is a conflict file, do not upload it!");
                 continue;
             }
             
             // if the file has been newly-updated, then do not have to sync to server, this can save the I/O
             // but this will cause bug, I will fix this later
             if(newlyUpdatedFileSet.contains(fileName)) {
                 continue;
             }
              
             // if one file is newly created, then write it into stream
             if(fileInPrevList == null) {
                fileOperations.put(fileName, new FileOperation(DropboxConstants.ADD, file));
             }
             else if(fileInPrevList.lastModifiedTime != file.lastModified()) { // if one file is modified, then write it into stream
                fileOperations.put(fileName, new FileOperation(DropboxConstants.EDIT, file));
             }
        }

        int currFileOperationNum = fileOperations.size();
        this.dataWriter.setDataOutputstream(os);
        DropboxFileTransferProtocolSerializer serializer = new DropboxFileTransferProtocolSerializer(this.dataWriter);
        for(Entry<String, FileOperation> entry : fileOperations.entrySet()) {
             if(entry.getValue().operation == DropboxConstants.DELETE) {
                 currFileOperationNum--;
                 serializer.syncSingleFile(entry.getKey(), entry.getValue(), null);
             }
        }

        if(currFileOperationNum == 0){
            serializer.writeHasNextFileFlag(false);
        }
        else {
            for(Entry<String, FileOperation> entry : fileOperations.entrySet()) {
                 if(entry.getValue().operation != DropboxConstants.DELETE)
                     serializer.syncSingleFile(entry.getKey(), entry.getValue(), null); // this wastes a lot of time when it's a big file!!
            }
            serializer.writeHasNextFileFlag(false);
        }
         this.dataWriter.writeSignature();
         //cout.close();
         //os.close();
         prevFileList = (HashMap<String, FileStructure>) currFileList.clone();
         // write the prev FileList into disk!
         this.deserializePrevFileList();
         currFileList.clear();
         this.resetSignature();
         
    }

    private void deserializePrevFileList() throws FileNotFoundException, IOException {
        String outputFileAddr = this.getDropboxClientMetaDataDir()+DropboxConstants.PREV_FILE_LIST_DIR_MARK;
        FileOutputStream fos = new FileOutputStream(outputFileAddr);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this.prevFileList);
        oos.close();
    }

    private HashMap<String, FileStructure> serializePrevFileList() {
        String outputFileAddr = this.getDropboxClientMetaDataDir()+DropboxConstants.PREV_FILE_LIST_DIR_MARK;
        HashMap<String, FileStructure> prevFileList = new HashMap<String, FileStructure>();
        FileInputStream fis;
        try {
//            fis = new FileInputStream(outputFileAddr);
            fis = new FileInputStream("C:\\pengye\\graduate_design\\dropbox_temp_file\\prev_files.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            prevFileList = (HashMap<String, FileStructure>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            return prevFileList;
        }
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException {
        // args[0] ip addr of the server
        // args[1] local sync dir
        // args[2] the private key for signature
//        Thread t = new Thread(new DropboxClient(args[0], args[1], args[2], args[3]), "DropboxClient");
    	String serverIp = "10.254.34.213";
    	String syncDir = "C:\\pengye\\graduate_design\\folder1";
    	String priKey = "3082014B0201003082012C06072A8648CE3804013082011F02818100FD7F53811D75122952DF4A9C2EECE4E7F611B7523CEF4400C31E3F80B6512669455D402251FB593D8D58FABFC5F5BA30F6CB9B556CD7813B801D346FF26660B76B9950A5A49F9FE8047B1022C24FBBA9D7FEB7C61BF83B57E7C6A8A6150F04FB83F6D3C51EC3023554135A169132F675F3AE2B61D72AEFF22203199DD14801C70215009760508F15230BCCB292B982A2EB840BF0581CF502818100F7E1A085D69B3DDECBBCAB5C36B857B97994AFBBFA3AEA82F9574C0B3D0782675159578EBAD4594FE67107108180B449167123E84C281613B7CF09328CC8A6E13C167A8B547C8D28E0A3AE1E2BB3A675916EA37F0BFA213562F1FB627A01243BCCA4F1BEA8519089A883DFE15AE59F06928B665E807B552564014C3BFECF492A041602143D13375912522A94C4618A5EA0F74ECEFADC680F";
    	String cryKey = "2C04A454F2297F25";
    	Thread t = new Thread(new DropboxClient(serverIp, syncDir, priKey, cryKey), "DropboxClient");
        t.start();
    }
}
