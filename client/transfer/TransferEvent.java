package transfer;

import watch.FileEvent;

public class TransferEvent {
	private FileEvent event;
	private String diff;
	
	public FileEvent getEvent() {
		return event;
	}
	public void setEvent(FileEvent event) {
		this.event = event;
	}
	public String getDiff() {
		return diff;
	}
	public void setDiff(String diff) {
		this.diff = diff;
	}
}
