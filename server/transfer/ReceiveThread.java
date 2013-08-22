package transfer;

import java.io.IOException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import server.DropboxServer;
import server.SynchronizationService;

import common.DropboxConstants;

public class ReceiveThread extends Thread{
	SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	public void receiveData(){
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
                    
                    System.out.println(ip);
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
}
