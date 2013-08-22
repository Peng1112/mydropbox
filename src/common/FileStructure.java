package common;

import java.io.File;
import java.io.Serializable;

/** A temp class for DropboxUtil
 * @author yepeng
 */
public class FileStructure implements Serializable {
    public File file;
    public long lastModifiedTime;
    public FileStructure(File file, long lastModifiedTime) {
        this.file = file;
        this.lastModifiedTime = lastModifiedTime;
    }
}