package util;
import common.*;
/**
 *
 * @author yepeng
 */
public class DropboxMessageConsole {
    static void printMessage(byte fileOperation, String filePath){
        switch(fileOperation) {
            case DropboxConstants.ADD_DIR: System.out.println("Add Dir: " + filePath); break;
            case DropboxConstants.ADD_FILE: System.out.println("Add File: " + filePath); break;
            case DropboxConstants.DELETE_DIR: System.out.println("Delete Dir: "+ filePath); break;
            case DropboxConstants.DELETE_FILE: System.out.println("Delete File: "+ filePath); break;
            case DropboxConstants.UPDATE_FILE: System.out.println("Update File: "+filePath); break;
            default: break;
        }
    }
}
