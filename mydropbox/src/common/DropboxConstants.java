package common;

/*
 * Interface: DropboxConstants
 * Description: Contains constants for the whole Dropbox project
 */
public interface DropboxConstants {
    int SERVER_PORT = 6919;
    int MAX_FILE_NAME_LENGTH = 4;
    int SYNC_SLEEP_MILLIS = 10 * 1000;
    int MAX_CLIENT_NUM = 3;
    
	final static byte DELETE = 0;
	final static byte EDIT = 1;
	final static byte ADD = 2;
    
    /* -- Message Type -- */
    final static byte FILE_OPERATION_FAIL = 0;
    final static byte NO_FILE_OPERATION = 1;
    final static byte DELETE_DIR = 2;
    final static byte DELETE_FILE = 3;
    final static byte UPDATE_FILE = 4;
    final static byte ADD_DIR = 5;
    final static byte ADD_FILE = 6;

    String CONFLICT_MARK = "(conflict_copy)";
    String LOGIN = "yepeng";
    String TMP_DIRECTORY = "/tmp";
    String DROPBOX_DIRECTORY = TMP_DIRECTORY + System.getProperty("file.separator") + LOGIN;
    //String DROPBOX_CLIENT_DIRECTORY = "/home/yepeng/Dropbox/deleteme";
    
    /* -- Metadata dir -- */
    //String DROPBOX_CLIENT_METADATA_DIR = "/home/yepeng/Dropbox/.dropbox_temp_file";
    String DROPBOX_SERVER_METADATA_DIR = TMP_DIRECTORY + System.getProperty("file.separator") + ".dropbox_temp_file";
    
    String COPYFILE_DIR_MARK = "/copy_files";
    String PREV_FILE_LIST_DIR_MARK = "/prev_files";

   /*indicates whether the message should be encrypted*/
    boolean isMessageEncrypted = true;
    
    //SecretKey keys
}
