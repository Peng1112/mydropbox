package watch;

public class FileEvent {

	private int catogory;
	private String oldFileName;
	private String newFileName;
	private String oldPath;
	private String newPath;
	
	public int getCatogory() {
		return catogory;
	}

	public void setCatogory(int catogory) {
		this.catogory = catogory;
	}

	public String getOldFileName() {
		return oldFileName;
	}

	public void setOldFileName(String oldFileName) {
		this.oldFileName = oldFileName;
	}

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}
	
	public String getOldPath() {
		return oldPath;
	}

	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}

	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	public FileEvent(int catogory, String oldFileName, String newFileName, String oldPath, String newPath){
		this.catogory = catogory;
		this.oldFileName = oldFileName;
		this.newFileName = newFileName;
		this.oldPath = oldPath;
		this.newPath = newPath;
	}
	
}
