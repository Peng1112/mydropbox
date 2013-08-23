package Test;

import java.io.IOException;

import common.FileProcess;
import common.MyDropboxContants;

public class TestFileProcess {
	
	public static void main(String args[]){
		try {
//			FileProcess.deleteFile("C:/pengye/graduate_design/watchDir/11/sa.txt");
			FileProcess.moveFile("C:/pengye/graduate_design/watchDir/11/qq.txt", "C:/pengye/graduate_design/qq.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
}
