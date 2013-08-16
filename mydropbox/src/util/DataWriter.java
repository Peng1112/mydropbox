/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 *
 * @author yepeng
 */
public class DataWriter {
    protected OutputStream os = null;
 
    public DataWriter() {
    }
    
    public DataWriter(DataOutputStream os) {
        this.os = os;
    }

    public void writeSignature() throws Exception{
        System.out.println("I never appear!!!");
    }
    
    public void setDataOutputstream(OutputStream os) {
        this.os = os;
    }
   
    public OutputStream getDataOutputStream() {
        return this.os;
    }
    
    protected void writeBoolean(boolean value) throws Exception {
        os.write(BytesConverter.boolean2bytes(value));
        os.flush();
    }

    protected void writeInt(int value) throws Exception {
        os.write(BytesConverter.int2bytes(value));
        os.flush();
    }

    protected void writeByte(byte value) throws Exception {
        os.write(value);
        os.flush();
    }

    protected void writeBytes(byte[] value) throws Exception {
        /*
        for(int i = 0; i < value.length; i++){
            System.out.print(i+" ");
            os.write(value[i]);
        }*/
        
        os.write(value, 0, value.length);
        os.flush();
    }
    
    protected void writeLong(long value) throws Exception {
        os.write(BytesConverter.long2bytes(value));
        os.flush();
    }
}
