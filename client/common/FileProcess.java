package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileProcess {

	public static void createDir(String path){
		File filedir =new File(path);
		if(!filedir.exists()){
			filedir.mkdirs();
		}
		
	}
	
	public static void createFile(String path) throws IOException{
		File file = new File(path);
		if(!file.exists())
			file.createNewFile();
	}
	
	public static void deleteDir(String path){
		File f =new File(path);
		if(f.exists() && f.isDirectory())
		{
			if(f.listFiles().length == 0)
			{
				f.delete();
			}
			else
			{
				File delFile[] = f.listFiles();
				int i = f.listFiles().length;
				for(int j=0;j<i;j++)
				{
					if(delFile[j].isDirectory()){
						deleteDir(delFile[j].getAbsolutePath());
					}
					delFile[j].delete();
				}
			}
			deleteDir(path);
		}

	}
	
	public static void deleteFile(String path){
		File file =new File(path);
		if(file.exists() && file.isFile())
			file.delete();
	}
	
	/*do not support file dir copy*/
	public static void copyFile(String src, String dest)throws IOException{
		FileInputStream in = new FileInputStream(src);
		File file = new File(dest);
		if(!file.exists())
			file.createNewFile();
		FileOutputStream out =new FileOutputStream(dest);
		int c;
		byte buffer[] = new byte[1024];
		while((c=in.read(buffer))!=-1){
			for(int i=0;i<c;i++)
				out.write(buffer[i]);
		}
		in.close();
		out.close();
	}
	
	public static void moveFile(String src, String dest)throws IOException{
		copyFile(src, dest);
		deleteFile(src);	
	}
	
	public static void renameFile(String oldname, String newname){
		if(!oldname.equals(newname)){
			File oldfile = new File(oldname);
			File newfile = new File(newname);
			if(!newfile.exists())
				oldfile.renameTo(newfile);
		}
		
	}
}
