package sync;

import common.MyEventQueue;

import transfer.TransferEvent;
import watch.FileEvent;

public class SyncProcessThread extends Thread{
	
	public void processRenameEvent(FileEvent file_event){
		TransferEvent transferevent = new TransferEvent();
		transferevent.setEvent(file_event);
		MyEventQueue.transferList.add(transferevent);
	}
	
	public void processModifyEvent(FileEvent file_event){
		TransferEvent transferevent = new TransferEvent();
		transferevent.setEvent(file_event);
		transferevent.setDiff("test diff data");
		MyEventQueue.transferList.add(transferevent);
	}
	
	public void processDeleteEvent(FileEvent file_event){
		TransferEvent transferevent = new TransferEvent();
		transferevent.setEvent(file_event);
		MyEventQueue.transferList.add(transferevent);
	}
	
	public void processCreateEvent(FileEvent file_event){
		TransferEvent transferevent = new TransferEvent();
		transferevent.setEvent(file_event);
		MyEventQueue.transferList.add(transferevent);
	}
	
	public void processMoveEvent(FileEvent file_event){
		TransferEvent transferevent = new TransferEvent();
		transferevent.setEvent(file_event);
		MyEventQueue.transferList.add(transferevent);
	}
	
	public void run(){
		System.out.println("begin to synchronize file data...");
		while(true)
		{
			try 
			{
				while(MyEventQueue.fileEventList.size() != 0)
				{
					FileEvent fv = MyEventQueue.fileEventList.poll();
					switch(fv.getCatogory())
					{
					case 1: processRenameEvent(fv);
					break;
					case 2: processModifyEvent(fv);
					break;
					case 3: processDeleteEvent(fv);
					break;
					case 4: processCreateEvent(fv);
					break;
					default: ;
					}
				}
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
