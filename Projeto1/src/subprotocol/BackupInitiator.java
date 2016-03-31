package subprotocol;


import communication.ChannelManager;
import communication.message.PutChunkMessage;
import general.FilesMetadataManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Afonso on 31/03/2016.
 */
public class BackupInitiator {
    private String filePath;
    String localId;
    int replicationDeg;
    String fileId;

    public BackupInitiator(String filePath, String localId, int replicationDeg) {
        this.filePath = filePath;
        this.localId = localId;
        this.replicationDeg = replicationDeg;
    }

    private int sendChunks() throws IOException{
        FileChannel inChannel = new RandomAccessFile(filePath, "r").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(64*1024);
        int i = 0;
        fileId = generateFileId(filePath);
        while(inChannel.read(buffer) > 0) {
            byte[] body = new byte[buffer.remaining()];
            buffer.get(body, 0, buffer.remaining());
            byte[] message = new PutChunkMessage(localId, fileId , "" + i, "" + replicationDeg, body).getBytes();
            ChannelManager.getInstance().send(ChannelManager.ChannelType.DATABACKUPCHANNEL, message);
            buffer.clear();
        }
        /*@TODO Subscribe Stored messages for 1 second*/
        int replication = 0;
        return replication;
    }

    private void storeMetadata() throws IOException {
        int replication = 0;
        do {
            replication += sendChunks();
        } while(replication < replicationDeg);
        FilesMetadataManager.getInstance().addIfNotExists(filePath, "" + new File(filePath).lastModified(), fileId, replicationDeg);
    }

    private String generateFileId(String filePath) {
        try {
            long lastModified = new File(filePath).lastModified();
            String hashable = filePath + lastModified;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(hashable.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return new String(digest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
