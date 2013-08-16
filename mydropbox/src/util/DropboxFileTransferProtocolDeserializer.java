package util;

import common.DropboxConstants;
import common.TextProcessor;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Deserilzizer for the file transfer protocol
 * Deserilize the protocol and save the change
 * @author yepeng
 */

public class DropboxFileTransferProtocolDeserializer {
    private InputStream is = null;
    private String rootDirAddr = null;
    private String tempDirPath = null;
    private DataReader dataReader = null;

    public DropboxFileTransferProtocolDeserializer(DataReader dataReader) {
        this.dataReader = dataReader;
        this.is = dataReader.getDataInputstream();
    }
    
    public DropboxFileTransferProtocolDeserializer(DataReader dataReader,
                                                    String rootDirAddr,
                                                    String tempDirPath) {
        this(dataReader);
        this.rootDirAddr = rootDirAddr;
        this.tempDirPath = tempDirPath;        
    }

    public void setDataReader(DataReader dataReader) {
        this.dataReader = dataReader;
    }
    
    public DataReader getDataReader() {
        return this.dataReader;
    }
    
    public boolean getIsHasNextFile() throws Exception {
        return dataReader.readBoolean();
    }

   public byte getFileOperation() throws Exception {
    	return dataReader.readByte();
    }

    public String getFileName() throws Exception {
        int nameLen = dataReader.readInt();
        byte[] result = dataReader.readBytes(nameLen);
        return new String(result);
    }

    public long getFileLastModifiedTime() throws Exception {
        return dataReader.readLong();
    }

    public boolean getIsDirFlag() throws Exception {
        return dataReader.readBoolean();
    }

    public long getFileSize() throws Exception {
        return dataReader.readLong();
    }

    /*
     * Save fileData(from fileDataStream), lastModifiedTime the newFile
     */
    
    public void saveStreamDataToDiskFile(long lastModifiedTime, long fileSize, File newFile, TextProcessor textProcessor) throws Exception {
        DropboxFileTransferProtocolDeserializer.synchronizedSaveStreamDataToDiskFile(fileSize, newFile, textProcessor, this.dataReader);
    }
    
    private synchronized static void synchronizedSaveStreamDataToDiskFile(long fileSize,
                                                                          File newFile,
                                                                          TextProcessor textProcessor,
                                                                          DataReader dataReader) throws Exception {

        if(!newFile.isDirectory()) {        
            FileOutputStream os = new FileOutputStream(newFile);
            int bufferSize = 1;
            byte[] buffer = new byte[bufferSize];
            int count = 0;
            while (count < fileSize) {
                //int n = is.read(buffer);
                buffer = dataReader.readBytes(bufferSize);
                if(textProcessor != null) {
                    byte[] encryptedText = textProcessor.processText(buffer);
                    os.write(encryptedText, 0, encryptedText.length);
                }
                else {
                    os.write(buffer, 0, bufferSize);
                }
                count += bufferSize;
            }
        }
        else
            newFile.mkdirs();

        // "chengren"newFile.setLastModified(lastModifiedTime);
    }

    public boolean processFile(TextProcessor textProcessor) throws Exception {
        if(this.rootDirAddr == null) {
            System.out.println("Please Specify the root directory first");
            return false;
        }

    	byte operation = this.getFileOperation();
        if(operation == DropboxConstants.ADD || operation == DropboxConstants.EDIT) {
         	String fileName = this.getFileName();
	        long fileLastModifiedTime = this.getFileLastModifiedTime();
	        boolean isDir = this.getIsDirFlag();
	        long fileSize = this.getFileSize();
	        this.atomicSaveFile(is, rootDirAddr+fileName, fileSize, fileLastModifiedTime, isDir, textProcessor);
        }
        else  {
            String fullPath = rootDirAddr+this.getFileName();
            File fileObj = new File(fullPath);
            if(fileObj.exists()) {
                if(fileObj.isDirectory()) {
                    DropboxFileUtil.deleteDirAndSubsidiaries(fileObj);
                    System.out.println("delete dir and its subsidiaries:"+ fullPath);
                }
                else {                    
                    fileObj.delete();
                    System.out.println("delete file:"+ fullPath);
                }
            }
        }
        return true;
    }

    /* Method Name: atomicSaveFile
     * Description: this should be atomic saving,
     * saves stream data to a temp file, then rename
     */
    
    private void atomicSaveFile(InputStream is, String fullPath, long fileSize, 
                                long lastModifiedTime, boolean isDir, TextProcessor textProcessor) throws Exception {
        DropboxFileUtil util = new DropboxFileUtil(this.rootDirAddr);
        String relativePath = util.getFileRelativePath(new File(fullPath));
        String fileCopyFullPath = this.tempDirPath+DropboxConstants.COPYFILE_DIR_MARK+relativePath;
        byte message = this.saveFile(is, fileCopyFullPath, fileSize, lastModifiedTime, isDir, fullPath, textProcessor);

        File fileCopy = new File(fileCopyFullPath);
        File file = new File(fullPath);

        if(message != DropboxConstants.NO_FILE_OPERATION && message != DropboxConstants.FILE_OPERATION_FAIL) {

            if(!file.exists()) {
                if(isDir)
                    file.mkdirs();
                else {
                    // get it's parent's dir path
                    // ex: file fullpath: /home/chengren/1/2/3/4/5.txt (it's a non-existing file, and dir 1,2,3,4 do not exist either)
                    // file.createNewFile() will report error is some dirs in the file path does not exist,
                    // so here we need its parentDirPath : /home/chengren/1/2/3/4
                    // then use mkdir to create this dir
                   String parentDirPath = fullPath.substring(0, fullPath.lastIndexOf(System.getProperty("file.separator")));
                   File parentDir = new File(parentDirPath);
                   parentDir.mkdirs();
                   file.createNewFile();
                }
            }
            
            // rename to is the only file move operation provided by java.io.file
            boolean isRenameSucceed = fileCopy.renameTo(file);
            // ensure that after the renaming, the modified would not change
            file.setLastModified(lastModifiedTime);
            System.out.println("file src is dir? "+fileCopy.isDirectory());
            if(!isRenameSucceed) {
                System.out.println("File Copy "+ fileCopyFullPath +" Exists ? " + fileCopy.exists());
                System.out.println("rename fails: " + "src file :" + fileCopyFullPath + " dest file : "+ fullPath);
            }
            else
                DropboxMessageConsole.printMessage(message, fullPath);
        }
    }
    
    private byte saveFile(InputStream is, String fullPath, long fileSize,
                          long lastModifiedTime, boolean isDir, String realFileFullPath,
                          TextProcessor textProcessor) throws Exception {
        byte message = DropboxConstants.NO_FILE_OPERATION;
        try {            
            String filePath = fullPath.substring(0, fullPath.lastIndexOf(System.getProperty("file.separator")));
            File copyFileDir = new File(filePath);
            File copyFileObj = new File(fullPath);
            File realFileObj = new File(realFileFullPath);
            
            if (isDir && !realFileObj.exists()) { // if this is an empty file dir and does not exist in this side
                copyFileObj.mkdirs();
                copyFileObj.setLastModified(lastModifiedTime);
                message = DropboxConstants.ADD_DIR;
            }
            else {
                // copyFileDir is it's parent's dir path
                // ex: file fullpath: /home/chengren/1/2/3/4/5.txt (it's a non-existing file, and dir 1,2,3,4 do not exist either)
                // file.createNewFile() will report error is some dirs in the file path does not exist,
                // so here we need it's parentDirPath : /home/chengren/1/2/3/4
                // then use mkdir to create this dir.
                copyFileDir.mkdirs();
                copyFileDir.setLastModified(lastModifiedTime);
            }

            if(!isDir) {
                if (!realFileObj.exists()) {
                    message = DropboxConstants.ADD_FILE;
                    copyFileObj.createNewFile();
                    this.saveStreamDataToDiskFile(lastModifiedTime, fileSize, copyFileObj, textProcessor);
                }
                else if (realFileObj.lastModified() != lastModifiedTime) {
                    message = DropboxConstants.UPDATE_FILE;          
                    this.saveStreamDataToDiskFile(lastModifiedTime, fileSize, copyFileObj, textProcessor);
                }
                else {
                    is.skip(fileSize);
                }
            }
            else {
                is.skip(fileSize);
            }
        }
        catch (IOException ex) {
            message = DropboxConstants.FILE_OPERATION_FAIL;
            Logger.getLogger(DropboxFileTransferProtocolDeserializer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return message;
    }
    
}
