/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package security;

import java.security.Signature;
import util.BytesConverter;
import util.DataReader;

/**
 *
 * @author yepeng
 */
public class MessageAuthenticationDataReader extends DataReader {
    private Signature dsa;

   public MessageAuthenticationDataReader(Signature dsa) {
       super(null);
       this.dsa = dsa;
   }

   protected boolean readBoolean() throws Exception {
        boolean ans = super.readBoolean();
        byte[] bytes = BytesConverter.boolean2bytes(ans);
        dsa.update(bytes, 0, bytes.length);
        return ans;
    }

    protected int readInt()  throws Exception {
        int ans = super.readInt();
        byte[] bytes = BytesConverter.int2bytes(ans);
        dsa.update(bytes, 0, bytes.length);
        return ans;
    }

    protected byte readByte() throws Exception {
        byte ans = super.readByte();
        dsa.update(ans);
        return ans;
    }

    protected byte[] readBytes(int len) throws Exception {
        byte[] ans = super.readBytes(len);
        dsa.update(ans, 0, ans.length);
        return ans;
    }

    protected long readLong() throws Exception {
        long ans = super.readLong();
        byte[] bytes = BytesConverter.long2bytes(ans);
        dsa.update(bytes, 0, bytes.length);
        return ans;
    }

   // readSignature Must be called when the serialization of data ends
   public byte[] readSignature() throws Exception {
        int sigSize = super.readInt();
        byte[] bytes = super.readBytes(sigSize);
        return bytes;
   }

   public Signature getSignature(){
       return this.dsa;
   }
     
}
