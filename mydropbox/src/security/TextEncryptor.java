/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package security;
/**
 *
 * @author yepeng
 */
import common.TextProcessor;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
public class TextEncryptor extends TextProcessor {
    private SecretKey myDesKey;
    private Cipher desCipher;
    public TextEncryptor(SecretKey myDesKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        this.myDesKey = myDesKey;
        this.desCipher = Cipher.getInstance("RC4");
        this.desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
    }

    public SecretKey getKey() {
        return this.myDesKey;
    }

    /* return the encrypted text return null if exception is thrown
     * 
     */
    public byte[] processText(byte[] text)  {
        byte[] textEncrtypted = null;
        try {

            textEncrtypted = desCipher.doFinal(text);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally{
            return textEncrtypted;
        }
    }

}
