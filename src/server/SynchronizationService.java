package server;

import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.DropboxFileTransferProtocolSerializer;
import util.DropboxFileUtil;
import common.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import security.MessageAuthenticationDataReader;
import security.TextDecryptor;
import util.DataWriter;
import util.DropboxFileTransferProtocolDeserializer;

public class SynchronizationService implements Runnable {
        private SSLSocket s;
        private DropboxClientFileChangeHandler clientFileChangeHandler;
        private DropboxFileUtil dropboxUtil;
        private DataWriter dataWriter;
        private MessageAuthenticationDataReader dataReader;
        private PublicKey publicKey;
        private SecretKey messageCryptionKey;
        static SecretKey serversideFileCryptionKey;
        private Cipher cipher;
        
        public SynchronizationService(SSLSocket s, PublicKey publicKey, SecretKey messageCryptionKey) throws Exception {
            this.s = s;
            this.dropboxUtil = new DropboxFileUtil(DropboxConstants.DROPBOX_DIRECTORY);
            this.dataWriter = new DataWriter();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            this.publicKey = publicKey;
            this.messageCryptionKey = messageCryptionKey;
            this.initSignature();
            this.cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        }

       public void initSignature() throws Exception {
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(publicKey);
            this.dataReader = new MessageAuthenticationDataReader(sig);
       }
       
       public void resetSignature() throws Exception {
           this.initSignature();
       }
       
       private void syncServerFileChangeToClient() throws Exception {
           this.syncFileHeaderToClient();
           this.resetSignature();
           this.ackClientRequestForUpdatedFile();
       }

       /*
        * File Header Format:
        * hasNextFileFlag --- fileName --- lastModifiedTime --- isDir -- ... -- false
        */
       private void syncFileHeaderToClient() throws Exception {
            OutputStream os = null;
            HashMap<String, FileStructure> currFileList = new HashMap<String, FileStructure>();
            currFileList = dropboxUtil.getAllFiles(DropboxConstants.DROPBOX_DIRECTORY, true);
            os = s.getOutputStream();
            this.dataWriter.setDataOutputstream(os);

            //this.dataWriter.setDataOutputstream(os);
            DropboxFileTransferProtocolSerializer serializer = new DropboxFileTransferProtocolSerializer(this.dataWriter);

            for (Map.Entry<String, FileStructure> e : currFileList.entrySet()) {
                String fileName = e.getKey();
                File file = e.getValue().file;
                if(file.exists()) {
                    serializer.syncFileHeader(fileName, new FileOperation(DropboxConstants.ADD, file));
                }
            }
            //cout.write(0);
            
            serializer.writeHasNextFileFlag(false);
            
       }

       /* Method Description: send updated file content
        * File Content Protocol format:
        * HasNext+FileNameLength+FileName+IsDir+FileContent
        */
       
       private void ackClientRequestForUpdatedFile() throws Exception{
        try {
            InputStream is = null;
            OutputStream os = null;
            ArrayList<String> updatedFileName = new ArrayList<String>();
            is = s.getInputStream();
            this.dataReader.setDataInputstream(is);
            DropboxFileTransferProtocolDeserializer deserializer = new DropboxFileTransferProtocolDeserializer(this.dataReader);
            boolean isHasNextFile = deserializer.getIsHasNextFile();
            while (isHasNextFile) {
                updatedFileName.add(deserializer.getFileName());
                isHasNextFile = deserializer.getIsHasNextFile();
            }
            
            byte[] clientSig = this.dataReader.readSignature();
            Signature mySig = this.dataReader.getSignature();
            if(!mySig.verify(clientSig)){
                System.out.println("Message not authentic and integral 1!");
            }

            os =s.getOutputStream();
            
            this.dataWriter.setDataOutputstream(os);
            //this.dataWriter.setDataOutputstream(os);
            
            DropboxFileTransferProtocolSerializer serializer = new DropboxFileTransferProtocolSerializer(this.dataWriter);
            for (int i = 0; i < updatedFileName.size(); i++) {
                String fullPath = DropboxConstants.DROPBOX_DIRECTORY + updatedFileName.get(i);
                File file = new File(fullPath);
                if (file.exists()) {
                    System.out.println("Will be sending file:" + updatedFileName.get(i));
                    serializer.syncSingleFile(updatedFileName.get(i), new FileOperation(DropboxConstants.ADD, file), new TextDecryptor(SynchronizationService.serversideFileCryptionKey));
                }
            }
            // end of the stream
            serializer.writeHasNextFileFlag(false);
        } catch (SignatureException ex) {
            Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE, null, ex);
        }
       }

        public void run() {
            try {
                this.syncServerFileChangeToClient();
                this.syncClientChangeToServer();
            }
            catch (Exception ex) {
                Logger.getLogger(SynchronizationService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public void syncClientChangeToServer() throws Exception {
            
                InputStream is = s.getInputStream();                
                this.dataReader.setDataInputstream(is);
                this.clientFileChangeHandler = new DropboxClientFileChangeHandler(dataReader, serversideFileCryptionKey);
                clientFileChangeHandler.handleClientFileChange();
                
                byte[] clientSig = this.dataReader.readSignature();
                Signature mySig = this.dataReader.getSignature();
                if(!mySig.verify(clientSig)){
                    System.out.println("Message not authentic and integral 2!");
                }
                this.resetSignature();
                this.clientFileChangeHandler.getDeserializer().setDataReader(dataReader);
        }
}
