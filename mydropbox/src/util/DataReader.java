/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import common.DropboxConstants;
import java.io.InputStream;

/**
 *
 * @author yepeng
 */
public class DataReader {
   
    protected InputStream is = null;

    public DataReader() {
    }

    public DataReader(InputStream is) {
        this.is = is;
    }

    // read nothing in the end
    public byte[] readSignature() throws Exception{
        return null;
    }

    public void setDataInputstream(InputStream is) {
        this.is = is;
    }

    public InputStream getDataInputstream() {
        return this.is;
    }

    protected long readLong() throws Exception {
        long ans;
        if(DropboxConstants.isMessageEncrypted) {
            byte[] bytes = new byte[8];
            is.read(bytes);
            ans = BytesConverter.bytes2long(bytes);
        }
        else {
            //ans = is.readLong();
        }
        return ans;
    }

    protected int readInt() throws Exception  {
        int ans;
        if(DropboxConstants.isMessageEncrypted) {
            byte[] bytes = new byte[4];
            is.read(bytes);
            ans = BytesConverter.bytes2int(bytes);
        }
        else {
            //ans = is.readInt();
        }
        return ans;
    }

    protected boolean readBoolean() throws Exception {
        boolean ans;
        if(DropboxConstants.isMessageEncrypted) {
            byte[] bytes = new byte[4];
            is.read(bytes);
            ans = BytesConverter.bytes2boolean(bytes);            
        }
        else {
            //ans = is.readBoolean();
        }

        return ans;
    }

    // the len should be the length of the byte array!
    protected byte[] readBytes(int len) throws Exception {
        byte[] result = new byte[len];
        is.read(result);
        return result;

    }

    protected byte readByte() throws Exception {
        byte result = (byte) is.read();
        return result;
    }    
}
