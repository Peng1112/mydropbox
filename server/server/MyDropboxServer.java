package server;


import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import transfer.ReceiveThread;

public class MyDropboxServer {


	public static void receiveData(){
		ReceiveThread rt = new ReceiveThread();
		rt.start();
	}
	
    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException  {
    	
    	System.out.println("Server started..");
    	
    	receiveData();
    }
}

