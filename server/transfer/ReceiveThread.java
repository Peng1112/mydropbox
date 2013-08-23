package transfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import server.DropboxServer;

import common.DropboxConstants;

public class ReceiveThread extends Thread{
	
	SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	
	public void receivePacket(){
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
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));                    
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())),true);                   
                    while(true){
                    	String str = in.readLine();
                    	if(str.equals("END")) break;
                    	System.out.println("Echoing: " + str);
                    	out.println(str);
                    }
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
	
	public void run(){
		receivePacket();
	}
}
