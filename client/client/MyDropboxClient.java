package client;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import sync.SyncProcessThread;
import transfer.TransferThread;

import watch.Listener;
import watch.MonitorThread;

import java.net.InetAddress;

public class MyDropboxClient{
	private String serverHost;
	private int serverPort;
	private SSLSocket socket;
	SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	
	public MyDropboxClient(String serverHost, int serverPort){
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}
	
	public void startconnection(String serverHost, int serverPort) throws Exception{
		socket = (SSLSocket) sslsocketfactory.createSocket(InetAddress.getByName(serverHost), serverPort);
		
	}
	
	public static void watchFile(){
		MonitorThread m = new MonitorThread();
		m.start();
	}
	
	public static void synchronizeFileProcess(){
		SyncProcessThread s = new SyncProcessThread();
		s.start();
	}
	
	public static void transferData(){
		TransferThread t =new TransferThread();
		t.start();
	}
	
	public static void main(String args[]){
		MyDropboxClient client = new MyDropboxClient("10.254.34.213",6919);
		try 
		{
			client.startconnection(client.serverHost, client.serverPort);
			System.out.println("Successfully connected to server!");
			
			watchFile();
			
			synchronizeFileProcess();
			
			transferData();//test
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
