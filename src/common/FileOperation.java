package common;

import java.io.File;

/**
 *
 * @author yepeng
 */
public class FileOperation {
    public byte operation;
    public File file;
    
    public FileOperation(byte operation, File file) {
        this.operation = operation;
        this.file = file;
    }
}