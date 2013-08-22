package transfer;

import common.MyEventQueue;

public class TransferThread extends Thread{

	public void blockTransfer(){
		
	}
	
	public void rsyncTransfer(){
		
	}
	
	public void transferTest(){
		while(MyEventQueue.transferList.size() != 0)
		{
			System.out.println("send message:"+MyEventQueue.transferList.poll().getEvent().getCatogory());
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
