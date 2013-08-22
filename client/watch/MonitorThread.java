package watch;

import common.MyDropboxContants;

import watch.Listener;
import net.contentobjects.jnotify.JNotify;

public class MonitorThread extends Thread{
	
	public void watch(){
		try{
			int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED
					| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
			boolean watchSubtree = true;
			JNotify.addWatch(MyDropboxContants.WATCH_DIR, mask, watchSubtree, new Listener());	
		} catch (Exception e) {
			System.out.println("error:" + e.getMessage());
		}

	}
	public void run(){
		watch();
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// 休眠被唤醒时，捕获该异常，不做额外处理，继续后续操作
		}
	}
	public static void main(String args[]){
		MonitorThread m = new MonitorThread();
		m.start();
	}
}
