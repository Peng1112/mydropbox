package server;
import common.DropboxConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.DropboxFileUtil;

/*
 * An tool for keep track of all the changes in server
 * Not Finished Yet
 * @author yepeng
 */
public class ServerLog {
    private String logfile;
    private BigInteger transactionID;
    private BufferedWriter out ;
    private boolean consoleOutputEnabled;

    public ServerLog(String logfile) {
        this.logfile = logfile;
        this.transactionID = BigInteger.valueOf(0);
        try {
            this.out = new BufferedWriter(new FileWriter(logfile));
        } catch (IOException ex) {
            Logger.getLogger(ServerLog.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.consoleOutputEnabled = true;
    }

    public void setConsoleOutputEnable(boolean enableConsoleOutput) {
        this.consoleOutputEnabled = enableConsoleOutput;
    }

    private void writeAddOperationToLog(File addedFile) throws IOException {
        DropboxFileUtil fileUtil = new DropboxFileUtil(DropboxConstants.DROPBOX_DIRECTORY);
        Date fileLastModifiedTime = new Date(addedFile.lastModified());
        DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String fileLastModifiedTimeStr = sdf.format(fileLastModifiedTime);
        String logRecord = this.transactionID.toString() + " Add File "
                           + fileUtil.getFileRelativePath(addedFile) + " "+fileLastModifiedTimeStr;
        this.out.write(logRecord);
        if(this.consoleOutputEnabled)
            System.out.println(logRecord);
    }

    private void writeEditOperationToLog(File oldFile, File newFile) throws IOException {
        DropboxFileUtil fileUtil = new DropboxFileUtil(DropboxConstants.DROPBOX_DIRECTORY);
        Date fileLastModifiedTime = new Date(newFile.lastModified());
        DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String fileLastModifiedTimeStr = sdf.format(fileLastModifiedTime);
        String logRecord = this.transactionID.toString() + " Edit File "
                           + fileUtil.getFileRelativePath(newFile) + " "+fileLastModifiedTimeStr;
        this.out.write(logRecord);
        
        if(this.consoleOutputEnabled)
            System.out.println(logRecord);
    }
    
    private void writeDeleteOperationToLog(File deletedFile, long deletedTime) throws IOException {
        DropboxFileUtil fileUtil = new DropboxFileUtil(DropboxConstants.DROPBOX_DIRECTORY);
        Date fileDeletedTime = new Date(deletedTime);
        DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String fileDeletedTimeStr = sdf.format(fileDeletedTime);
        String logRecord = this.transactionID.toString() + " Edit File "
                           + fileUtil.getFileRelativePath(deletedFile) + " "+fileDeletedTimeStr;
        this.out.write(logRecord);
        
        if(this.consoleOutputEnabled)
            System.out.println(logRecord);
    }
    /*
    public void writeLog(byte operation, File oldFile, File newFile) throws IOException {
        switch(operation) {
            case DropboxConstants.ADD: writeAddOperationToLog(newFile);  break;
            case DropboxConstants.EDIT: writeEditOperationToLog(oldFile, newFile);  break;
            default: writeDeleteOperationToLog(oldFile); break;
        }
    }*/
}
