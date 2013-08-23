package util;

import common.*;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 * DropboxUtil:
 * Contains all of the Util methods for both client and server side
 * @author yepeng
 */
public class DropboxFileUtil {

    private String syncDirectory;
    
    public DropboxFileUtil(String syncDirectory) {
        this.syncDirectory = syncDirectory;
    }
    
    public void setSyncDirectory(String syncDir) {
        this.syncDirectory = syncDir;
    }

    public static void deleteDirAndSubsidiaries(File f) {
    	  if(f.isDirectory()) {
    		  for (File c : f.listFiles())
    			  deleteDirAndSubsidiaries(c);
    	  }
          
    	  f.delete();
    }


    public static void renameFile(String originalFileName, String renamedFileName) {

        File originalFile = new File(originalFileName);
        boolean fileExists = originalFile.exists();

        if (!fileExists) {
            System.out.println("File does not exist: " + originalFileName);
            System.out.println("Rename Operation Aborted.");
            return;
        }
        File renamedFile = new File(renamedFileName);
        originalFile.renameTo(renamedFile);
    }

    public HashMap<String, FileStructure> getAllFiles(String syncDirectory, boolean filterOn) {
        HashMap<String, FileStructure> currFileList = getAllFiles(syncDirectory);

        if(filterOn) {
            Iterator it=currFileList.keySet().iterator();
            while(it.hasNext())
            {
                String str=(String)it.next();
                int beginIndex = str.lastIndexOf(System.getProperty("file.separator"))+1;
                int endIndex = str.length();
                str = str.substring(beginIndex, endIndex);
                if(str.startsWith(".nfs"))
                    it.remove();
            }
        }
        return currFileList;
    }

    // a recursive function to get all of the files and empty dir under the dropbox directory
    public HashMap<String, FileStructure> getAllFiles(String syncDirectory) {
        File root = new File(syncDirectory);
        File[] files = root.listFiles();
        HashMap<String, FileStructure> currFileList = new HashMap<String, FileStructure>();
        
        for(File file:files) {
            if(file.isDirectory()) {
                currFileList.putAll(getAllFiles(file.getAbsolutePath()));
                if(file.listFiles().length == 0) {
                    String fileName = getFileRelativePath(file);
                    currFileList.put(fileName, new FileStructure(file, file.lastModified()));
                }
            }
            else {
                String fileName = getFileRelativePath(file);
                currFileList.put(fileName, new FileStructure(file, file.lastModified()));
            }
        }
        
        return currFileList;           
    }
    
    public String getDeletedParentRelativePath(String s) {
        String parentDir = s;
        File f = new File(syncDirectory+parentDir);
        while(!f.getParentFile().exists()) {
            parentDir = parentDir.substring(0, parentDir.lastIndexOf("/"));
            f = new File(syncDirectory+parentDir);
        }
        return parentDir;
    }
    // get the relative path of file (the root dir. is syncDirectory)
    public String getFileRelativePath(File file) {
        String absolutePath = file.getPath();
        return absolutePath.substring(absolutePath.indexOf(syncDirectory)+syncDirectory.length());
    }  
}
