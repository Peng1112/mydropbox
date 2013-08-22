package common;

import java.util.LinkedList;
import java.util.Queue;

import transfer.TransferEvent;
import watch.FileEvent;

public interface MyEventQueue {
	public static Queue<FileEvent> fileEventList = new LinkedList<FileEvent>();
	public static Queue<TransferEvent> transferList = new LinkedList<TransferEvent>();
}
