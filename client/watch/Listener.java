package watch;

import common.MyEventQueue;

import net.contentobjects.jnotify.JNotifyListener;

public class Listener extends Thread implements JNotifyListener {
	public Listener() {
		System.out.println("begin to listen file event...");
		
	}

	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
		FileEvent event =new FileEvent(1, oldName, newName, rootPath, rootPath);
		MyEventQueue.fileEventList.add(event);
		print("renamed " + rootPath + " : " + oldName + " -> " + newName);
	}

	public void fileModified(int wd, String rootPath, String name) {
		FileEvent event =new FileEvent(2, name, name, rootPath, rootPath);
		MyEventQueue.fileEventList.add(event);
		print("modified " + rootPath + " : " + name);
	}

	public void fileDeleted(int wd, String rootPath, String name) {
		FileEvent event =new FileEvent(3, name, name, rootPath, rootPath);
		MyEventQueue.fileEventList.add(event);
		print("deleted " + rootPath + " : " + name);
	}

	public void fileCreated(int wd, String rootPath, String name) {
		FileEvent event =new FileEvent(4, name, name, rootPath, rootPath);
		MyEventQueue.fileEventList.add(event);
		print("created " + rootPath + " : " + name);
	}

	void print(String msg) {
		System.err.println(msg);
	}
}
