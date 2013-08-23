package transfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import common.MyDropboxContants;
import common.MyEventQueue;

public class TransferThread extends Thread{

	public void blockTransfer(){
		
	}
	
	public void rsyncTransfer(){
		
	}
	
	public void transferTest() throws IOException{
		Socket s = new  Socket(MyDropboxContants.SERVERHOST,MyDropboxContants.SERVERPORT);
		 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));                    
         PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())),true);                   
         //in means read data from socket
         //out means write data into socket
		try{
			while(MyEventQueue.transferList.size() != 0)
			{
				System.out.println("send file event message:"+MyEventQueue.transferList.peek().getEvent().getCatogory());
				out.println("Sync file data from client:"+MyEventQueue.transferList.poll());
			}
             out.println("END");
		}finally{
			System.out.println("waiting file data ...");
			s.close();
		}

	}
	
	public void run(){
		System.out.println("begin to send data...");
		while(true)
		{
			try 
			{
				transferTest();
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
