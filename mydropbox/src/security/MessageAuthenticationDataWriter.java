/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package security;

import java.security.Signature;
import util.BytesConverter;
import util.DataWriter;
/**
 *
 * @author yepeng
 */
public class MessageAuthenticationDataWriter extends DataWriter{

   private Signature dsa;

   public MessageAuthenticationDataWriter(Signature dsa) {
       super(null);
       this.dsa = dsa;
   }   
   protected void writeBoolean(boolean value) throws Exception {
        byte[] bytes = BytesConverter.boolean2bytes(value);
        super.writeBoolean(value);
        dsa.update(bytes, 0, bytes.length);
    }

    protected void writeInt(int value) throws Exception {
        byte[] bytes = BytesConverter.int2bytes(value);
        super.writeInt(value);
        dsa.update(bytes, 0, bytes.length);
    }

    protected void writeByte(byte value) throws Exception {
        super.writeByte(value);
        dsa.update(value);
    }

    protected void writeBytes(byte[] value) throws Exception {
        super.writeBytes(value);
        dsa.update(value, 0, value.length);
    }
    
    protected void writeLong(long value) throws Exception {
        byte[] bytes = BytesConverter.long2bytes(value);
        super.writeLong(value);
        dsa.update(bytes, 0, bytes.length);
    }

   // writeSignature must be called when the serialization of data ends
    
    public void writeSignature() throws Exception{
        byte[] realSig = dsa.sign();
        int sigSize = realSig.length;
      /*
        System.out.println("Client generates the signature: ");
        System.out.println("size: " + sigSize);
        for(int i = 0; i < sigSize; i++)
                System.out.print(realSig[i]);
        System.out.println();
        */
        super.writeInt(sigSize);
        super.writeBytes(realSig);
    }
}
