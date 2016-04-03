package subprotocol;


import general.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BackupInitiator {
    private static final int MAXCHUNKSIZE = 64 * 1000;
    private final MulticastChannel mcChannel;
    private final MulticastChannel mdbChannel;

    private String filePath;
    private int totalChunks;
    private String localId;
    private int replicationDeg;
    private String fileId;

    public BackupInitiator(String filePath, String localId, int replicationDeg, MulticastChannel mcChannel, MulticastChannel mdbChannel) throws IOException {
        this.filePath = filePath;
        this.localId = localId;
        this.replicationDeg = replicationDeg;
        this.mcChannel = mcChannel;
        this.mdbChannel = mdbChannel;
        long fileSize = new File(filePath).length();
        this.totalChunks = (int)(fileSize / MAXCHUNKSIZE) + 1;
        this.fileId = generateFileId();
    }

    public void sendChunks() throws IOException {
        RandomAccessFile in = new RandomAccessFile(filePath, "r");
        byte[] buffer = new byte[MAXCHUNKSIZE];

        for (int chunkNo = 0; chunkNo < totalChunks; ++chunkNo) {
            in.seek(chunkNo * MAXCHUNKSIZE);
            int size = in.read(buffer);

            BackupChunkInitiator initiator = new BackupChunkInitiator(localId, fileId, Integer.toString(chunkNo), replicationDeg, Arrays.copyOf(buffer, size), mcChannel, mdbChannel);

            initiator.run();

            if (!initiator.reachedReplicationDeg()) {
                Logger.getInstance().printLog("Chunk " + chunkNo + " from fileId " + fileId + " failed to reach desired replication degree");
            }
        }
    }

    public void storeMetadata() throws IOException {
        FilesMetadataManager.getInstance().addBackingUp(filePath, "" + new File(filePath).lastModified(), fileId, replicationDeg);
    }

    private String generateFileId() {
        try {
            long lastModified = new File(filePath).lastModified();
            String hashable = filePath + lastModified + localId;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(hashable.getBytes(StandardCharsets.US_ASCII));
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for(byte b : digest){
                sb.append(String.format("%02x",b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMetadata() throws IOException {
        FilesMetadataManager.getInstance().changeFileStatus(filePath, FilesMetadataManager.FileStatus.BACKEDUP);
    }
}
