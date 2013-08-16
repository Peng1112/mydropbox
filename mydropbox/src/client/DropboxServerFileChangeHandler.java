package client;

import util.DropboxFileTransferProtocolDeserializer;
import util.DropboxFileUtil;
import common.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import util.DataReader;
import util.DataWriter;
import util.DropboxFileTransferProtocolSerializer;

/*
 * Description:  Deserialize the protocol received in client side
 */

public class DropboxServerFileChangeHandler {

    private String rootDirAddress = null;
    private String metadataDirAddress = null;
    private DropboxFileUtil fileUtil = null;
    private SSLSocket serverSocket = null;
    private HashSet<String> updatedFileSet = new HashSet<String>();
    private HashSet<String> deletedFileSet = new HashSet<String>();
    private HashSet<String> serverFileSet = new HashSet<String>();
    private HashMap<String, FileStructure> prevFileList = new HashMap<String, FileStructure>();
    private DataReader clientDataReader;
    private DataWriter clientDataWriter;
    private Cipher cipher;
    private SecretKey messageCryptionKey;
    public DropboxServerFileChangeHandler(SSLSocket serverSocket, String dropboxClientDir,
                                          String metadataDirAddress, DataReader clientDataReader,
                                          DataWriter clientDataWriter, Cipher cipher,
                                          SecretKey messageCryptionKey) {
        this.rootDirAddress = dropboxClientDir;
        this.metadataDirAddress = metadataDirAddress;
        this.fileUtil = new DropboxFileUtil(rootDirAddress);
        this.serverSocket = serverSocket;
        this.clientDataReader = clientDataReader;
        this.clientDataWriter = clientDataWriter;
        this.cipher = cipher;
        this.messageCryptionKey = messageCryptionKey;
    }

    public void resetClientDataWriter(DataWriter clientDataWriter) {
        this.clientDataWriter = clientDataWriter;
    }
    
    private void clear() {
        updatedFileSet.clear();
        deletedFileSet.clear();
        serverFileSet.clear();
    }

    public void setPrevFileList(HashMap<String, FileStructure> prevFileList) {
        this.prevFileList = prevFileList;
    }

    public HashSet<String> getNewlyUpdatedFileSet() {       
        return updatedFileSet;
    }

   public HashSet<String> getNewlyDeletedFileSet() {
        return deletedFileSet;
   }

    private void deleteFile() {
        HashMap<String, FileStructure> currFileList = fileUtil.getAllFiles(rootDirAddress, true);
        Iterator iter = currFileList.keySet().iterator();
        while(iter.hasNext()) {
            String currFileName = (String)iter.next();
            // here we have a bug, cannot delete an empty dir!
            if(!serverFileSet.contains(currFileName)
               && prevFileList.containsKey(currFileName)
               // conflict file will not be deleted!
               // if a conflict file is deleted in server side, it would not be deleted in the client side
               && !currFileName.endsWith(DropboxConstants.CONFLICT_MARK)) {               
                String fullPath = rootDirAddress+fileUtil.getDeletedParentRelativePath(currFileName);
                File deletedFile = new File(fullPath);
                if(deletedFile.isDirectory()) {
                    DropboxFileUtil.deleteDirAndSubsidiaries(deletedFile);
                    System.out.println("delete dir and its subsidiaries:"+ fullPath);
                    // get the address of the copy file and delete it
                    /*
                    rootDirAddress
                    File deletedFileCopy = new File(rootDirAddress+DropboxConstants.COPYFILE_DIR_MARK+fileUtil.getDeletedParentRelativePath(currFileName));
                    */
                }
                else {
                    deletedFile.delete();
                    System.out.println("delete file:"+ fullPath);
                }                
                deletedFileSet.add(fileUtil.getDeletedParentRelativePath(currFileName));                
            }
        }

        // So far, the client sides's files hierarchy is almost the same as the server side
        // except some empty dirs remain undeleted. So next we need to delete these empty dirs

    }

    public void handleServerFileChange() {
 
        parseFileHeader();
        requestUpdatedFileData();
        saveUpdatedFileData();
        deleteFile();
    }

    private void parseFileHeader()  {
        InputStream is = null;
        try {
            this.clear();
        
            is = serverSocket.getInputStream();
            this.clientDataReader.setDataInputstream(is);
       
            DropboxFileTransferProtocolDeserializer deserializer = new DropboxFileTransferProtocolDeserializer(this.clientDataReader);
            boolean isHasNextFile = deserializer.getIsHasNextFile();

            while (isHasNextFile) {
                String fileName = deserializer.getFileName();
                long serverSideFileLastModifiedTime = deserializer.getFileLastModifiedTime();
                // for each file header from server, check whether it should be updated in client side,
                // if yes, then asks server for data of the file
                File testFile = new File(rootDirAddress + fileName + DropboxConstants.CONFLICT_MARK);
                boolean isConclictFile = testFile.exists();
                if (!isConclictFile) {
                    // conflict file would not be updated and added, conflict file cannot be downloaded
                    processFileHeader(rootDirAddress, fileName, serverSideFileLastModifiedTime);
                }
                isHasNextFile = deserializer.getIsHasNextFile();
                // record all fileName, will later be used for deletion
                this.serverFileSet.add(fileName);
            }
            this.clientDataReader.readSignature();
        } catch (Exception ex) {
            Logger.getLogger(DropboxServerFileChangeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
   
    /*
     * Method Description: ask server for the data of the files need to be updated
     */
    
    private void requestUpdatedFileData() {      
        OutputStream os = null;
        try {
            os = this.serverSocket.getOutputStream();
            this.clientDataWriter.setDataOutputstream(os);
            DropboxFileTransferProtocolSerializer serializer = new DropboxFileTransferProtocolSerializer(this.clientDataWriter);
            for (String updatedFileName : this.updatedFileSet) {
                serializer.writeHasNextFileFlag(true);
                serializer.writeFileName(updatedFileName);
            }
            // the end of the stream
            serializer.writeHasNextFileFlag(false);
            this.clientDataWriter.writeSignature();

            //os.close();
            //cout.flush();
            //cout.close();
        } catch (Exception ex) {
            Logger.getLogger(DropboxServerFileChangeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }

    private void saveUpdatedFileData() {
       InputStream is = null;
       try {
            is = serverSocket.getInputStream();
            this.clientDataReader.setDataInputstream(is);
            DropboxFileTransferProtocolDeserializer deserializer = new DropboxFileTransferProtocolDeserializer(clientDataReader, this.rootDirAddress, this.metadataDirAddress);
            boolean isHasNextFile = deserializer.getIsHasNextFile();
            
            while(isHasNextFile) {
                deserializer.processFile(null);
                isHasNextFile = deserializer.getIsHasNextFile();
            }
            
            this.clientDataReader.readSignature();
        }
        catch (Exception ex) {
            Logger.getLogger(DropboxServerFileChangeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* Method Name: processFileHeader
     * Method Description: put the file which should be updated into updatedFileSet
     */

    private void processFileHeader(String rootDirAddress, String fileName, long serverSideFileLastModifiedTime) {

	    String  fullPath = rootDirAddress+fileName;
	    File clientSideFile = new File(fullPath);
	    String clientSideFileRelativePath = fileUtil.getFileRelativePath(clientSideFile);
	    boolean isEmptyDir = clientSideFile.isDirectory() && clientSideFile.list().length == 0;
	    boolean isExistInClient = clientSideFile.exists() && (clientSideFile.isFile() || isEmptyDir);
	    boolean isExistInClientLastSyn = prevFileList.containsKey(clientSideFileRelativePath);

        if(!isExistInClientLastSyn && isExistInClient) {
            if(clientSideFile.lastModified() < serverSideFileLastModifiedTime) {
                updatedFileSet.add(clientSideFileRelativePath);
            }
	    }
	    else if(!isExistInClientLastSyn && !isExistInClient) {
            updatedFileSet.add(clientSideFileRelativePath);
            /*
            if(isDir) {
                 // clientSideFile.mkdirs();
                System.out.println("add dir: " + fullPath);
                updatedFileSet.add(clientSideFileRelativePath);
            }
            else {
                String filePath = fullPath.substring(0, fullPath.lastIndexOf(System.getProperty("file.separator")));
                File fileDirInClientSide = new File(filePath);
                // fileDirInClientSide.mkdirs();
                // clientSideFile.createNewFile();
                System.out.println("add file: " + fullPath);
                //saveOneFileToDisk(clientSideFile, is, fileSize, serverSideFileLastModifiedTime);
                updatedFileSet.add(clientSideFileRelativePath);
            }*/
	    }
	    else if (isExistInClient && isExistInClientLastSyn) {
            long lastModifiedTimeInClientSide = clientSideFile.lastModified();
            long prevLastModifiedTimeInClientSide = prevFileList.get(clientSideFileRelativePath).lastModifiedTime;
            long lastModifiedTimeInServerSide = serverSideFileLastModifiedTime;
            boolean isUpdatedInClientSide = prevLastModifiedTimeInClientSide < lastModifiedTimeInClientSide;
            boolean isUpdatedInServerSide = prevLastModifiedTimeInClientSide < lastModifiedTimeInServerSide;

            if(isUpdatedInClientSide && isUpdatedInServerSide) {
                DropboxFileUtil.renameFile(fullPath, fullPath+DropboxConstants.CONFLICT_MARK);
                System.out.println("mark as conflict: "+fullPath);
                //is.skip(fileSize);
            }
            else if(isUpdatedInServerSide && !isUpdatedInClientSide) {
                // saveOneFileToDisk(clientSideFile, is, fileSize, serverSideFileLastModifiedTime);
                // next syn skip this file
                this.updatedFileSet.add(clientSideFileRelativePath);
            }
	    }
	}
}
