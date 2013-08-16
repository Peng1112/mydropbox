package util;

import common.*;
import client.DropboxClient;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Serilzizer for the file transfer protocol
 * Serilize the protocol and save the change
 * @author yepeng
 */
public class DropboxFileTransferProtocolSerializer {

    private OutputStream os = null;
    private DataWriter dataWriter = null;
    //private isMessageEncryp

    public DropboxFileTransferProtocolSerializer(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
        this.os = this.dataWriter.getDataOutputStream();
    }
    
    public void writeHasNextFileFlag(boolean nextHasFileFlag) throws Exception {
            if(DropboxConstants.isMessageEncrypted) {
                dataWriter.writeBoolean(nextHasFileFlag);
            }
            else {
                /*
                os.writeBoolean(nextHasFileFlag);
                os.flush();
                 */
            }          
    }
        
    public void writeFileOperation(byte operation) throws Exception {
            // no need to convert to byte
            if(DropboxConstants.isMessageEncrypted) {
                dataWriter.writeByte(operation);
            }
            else {
                /*
                os.writeByte(operation);
                os.flush();
                */
            }
    }

    public void writeFileName(String fileName) throws Exception {
        if(DropboxConstants.isMessageEncrypted) {
            byte[] fileNameBytes = fileName.getBytes();
            dataWriter.writeInt(fileNameBytes.length);
            dataWriter.writeBytes(fileNameBytes);
        }
        else {
            /*
            byte[] fileNameBytes = fileName.getBytes();
            os.writeInt(fileNameBytes.length);
            os.flush();
            os.write(fileNameBytes);
            os.flush();
            */
        }
    }

    public void writeFileLastModifiedTime(long fileLastModifiedTime) throws Exception {
            if(DropboxConstants.isMessageEncrypted) {
                dataWriter.writeLong(fileLastModifiedTime);
            }
            else {
                /*
                os.writeLong(fileLastModifiedTime);
                os.flush();
                */
            }           
    }

    public void writeIsDirFlag(boolean isDir) throws Exception {
            if(DropboxConstants.isMessageEncrypted) {
                //os.write(BytesConverter.boolean2bytes(isDir));
                dataWriter.writeBoolean(isDir);
            }
            else {
                /*
                os.writeBoolean(isDir);
                os.flush();
                */
            }            
    }

    public void writeFileSize(long fileSize) throws Exception {
            if(DropboxConstants.isMessageEncrypted) {
                dataWriter.writeLong(fileSize);
            }
            else {
                /*
                os.writeLong(fileSize);
                os.flush();
                */
            }           
    }

    public long getDecryptedFileSize(FileInputStream is, TextProcessor textProcessor) throws IOException {
            byte[] buffer = new byte[1];
            long size = 0;
            while ((is.read(buffer)) != -1) {
                if(textProcessor != null)
                    size += textProcessor.processText(buffer).length;
            }
            return size;
    }
    
    public void writeFileContent(FileInputStream is, TextProcessor textProcessor) throws Exception {
            byte[] buffer = new byte[1];
            byte[] bytes = null;
            while ((is.read(buffer)) != -1) {
                if(textProcessor != null)
                    bytes = textProcessor.processText(buffer);
                else
                    bytes = buffer;
                dataWriter.writeBytes(bytes);
                //os.write(buffer);
            }
    }

    public void syncSingleFile(String fileName, FileOperation fileOperation, TextProcessor textProcessor) {
        byte operation = fileOperation.operation;
        long fileLastModifiedTime = fileOperation.file.lastModified();
        long fileSize = fileOperation.file.length();
        boolean isDir = fileOperation.file.isDirectory();
        FileInputStream is = null;

        try {
            if(textProcessor != null) {
                fileSize = this.getDecryptedFileSize(new FileInputStream(fileOperation.file), textProcessor);
            }
            writeHasNextFileFlag(true);
            if(operation == DropboxConstants.ADD || operation == DropboxConstants.EDIT) {
                writeFileOperation(operation);
                writeFileName(fileName);
                writeFileLastModifiedTime(fileLastModifiedTime);
                writeIsDirFlag(isDir);

                if(fileOperation.file.isDirectory()) {
                    writeFileSize(0);
                }
                else {
                    writeFileSize(fileSize);
                    is = new FileInputStream(fileOperation.file);
                    writeFileContent(is, textProcessor);
                }
            }
            else { // DELETE
                writeFileOperation(operation);
                writeFileName(fileName);
            }
        }
        catch(Exception ex) {
            Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
        	try {
        		if(is != null)
				    is.close();
				}
        	catch (Exception ex) {
                Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
			}
        }
    }

    public void syncFileHeader(String fileName, FileOperation fileOperation) {
        long fileLastModifiedTime = fileOperation.file.lastModified();
        FileInputStream is = null;
        try {
            writeHasNextFileFlag(true);
            writeFileName(fileName);
            writeFileLastModifiedTime(fileLastModifiedTime);
        }
        catch(Exception ex) {
            Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
        	try {
        		if(is != null)
				    is.close();
				}
        	catch (Exception ex) {
                Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
			}
        }
    }
    
    public void syncFileContent(String fileName, FileOperation fileOperation) {
        byte operation = fileOperation.operation;
        long fileLastModifiedTime = fileOperation.file.lastModified();
        FileInputStream is = null;
        try {
            writeHasNextFileFlag(true);
            if(operation == DropboxConstants.ADD || operation == DropboxConstants.EDIT) {
                writeFileOperation(operation);
                writeFileName(fileName);
                writeFileLastModifiedTime(fileLastModifiedTime);
            }
            else { // DELETE
                writeFileOperation(operation);
                writeFileName(fileName);
            }
        }
        catch(Exception ex) {
            Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
        	try {
        		if(is != null)
				    is.close();
				}
        	catch (Exception ex) {
                Logger.getLogger(DropboxClient.class.getName()).log(Level.SEVERE, null, ex);
			}
        }
    }
    
}
