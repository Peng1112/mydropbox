package server;
import java.security.InvalidKeyException;
import javax.crypto.NoSuchPaddingException;
import security.TextEncryptor;
import java.security.NoSuchAlgorithmException;
import util.DropboxFileTransferProtocolDeserializer;
import util.DropboxFileUtil;
import common.*;
import javax.crypto.SecretKey;
import util.DataReader;


public class DropboxClientFileChangeHandler   {
	 
    private DropboxFileUtil fileUtil = null;
    private DropboxFileTransferProtocolDeserializer deserializer = null;
    private TextProcessor textEncryptor;

    
    public DropboxClientFileChangeHandler(DataReader clientDataReader, SecretKey serverSideFileEncryptionKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        deserializer = new DropboxFileTransferProtocolDeserializer(clientDataReader, DropboxConstants.DROPBOX_DIRECTORY, DropboxConstants.DROPBOX_SERVER_METADATA_DIR);
        this.fileUtil = new DropboxFileUtil(DropboxConstants.DROPBOX_DIRECTORY);
        this.textEncryptor = new TextEncryptor(serverSideFileEncryptionKey);
    }

    public DropboxFileTransferProtocolDeserializer getDeserializer() {
        return this.deserializer;
    }
    
    public void handleClientFileChange() throws Exception {
        boolean isHasNextFile = deserializer.getIsHasNextFile();
        while(isHasNextFile) {
            deserializer.processFile(this.textEncryptor);
            isHasNextFile = deserializer.getIsHasNextFile();
        }

       // byte[] clientSig = this.deserializer.getDataReader().readSignature();
        //clientSig = this.deserializer.getDataReader().readSignature();
        /*
        if(clientSig != null){
                    System.out.println("I got the signature of the client:  signature length 3 "+clientSig.length);
                    for(int i = 0; i < clientSig.length; i++)
                        System.out.print(clientSig[i]);
                    System.out.println();
                }*/
    }
}

