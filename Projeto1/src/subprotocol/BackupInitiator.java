package subprotocol;


import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.FilesMetadataManager;
import general.Logger;
import general.MalformedMessageException;
import general.MulticastChannel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by Afonso on 31/03/2016.
 */
public class BackupInitiator implements Observer {
    private static final int MAXCHUNKSIZE = 64 * 1000;
    private final MulticastChannel mcChannel;
    private final MulticastChannel mdbChannel;

    private String filePath;
    private int totalChunks;
    private String localId;
    private int replicationDeg;
    private String fileId;
    private Map<Integer, HashSet<String>> chunksReplication;

    public BackupInitiator(String filePath, String localId, int replicationDeg, MulticastChannel mcChannel, MulticastChannel mdbChannel) throws IOException {
        this.filePath = filePath;
        this.localId = localId;
        this.replicationDeg = replicationDeg;
        this.mcChannel = mcChannel;
        this.mdbChannel = mdbChannel;
        long fileSize = new File(filePath).length();
        totalChunks = (int)(fileSize / MAXCHUNKSIZE) + 1;
        chunksReplication = new HashMap<>();
        for (int i : IntStream.range(0, totalChunks).toArray()){
            chunksReplication.put(i, new HashSet<>());
        }
    }

    public void sendChunks() throws IOException{
        RandomAccessFile in = new RandomAccessFile(filePath, "r");
        byte[] buffer = new byte[MAXCHUNKSIZE];
        fileId = generateFileId();
        Message message;
        List<Integer> chunksBelowReplicationDeg;
        for(int attempt = 0; attempt < 5; ++attempt) {
            chunksBelowReplicationDeg = checkReplicationDeg();
            if (chunksBelowReplicationDeg.size() == 0)
                break;
            mcChannel.addObserver(this);
            for (int i : chunksBelowReplicationDeg) {
                in.seek(i * MAXCHUNKSIZE);
                int size = in.read(buffer);
                message = new PutChunkMessage(localId, fileId, "" + i, "" + replicationDeg, Arrays.copyOf(buffer, size));
                mdbChannel.send(message.getBytes());
                //Logger.getInstance().printLog(message.getHeader());
            }
            try {
                Thread.sleep((long) (1000 * Math.pow(2,attempt)));
            } catch (InterruptedException ignored) {}
            mcChannel.deleteObserver(this);
        }
    }

    private List<Integer> checkReplicationDeg(){
        List<Integer> ret = new ArrayList<>();
        for(int i : IntStream.range(0, totalChunks).toArray())
            if(chunksReplication.get(i).size() < replicationDeg)
                ret.add(i);
        return ret;
    }

    public void storeMetadata() throws IOException {
        FilesMetadataManager.getInstance().addIfNotExists(filePath, "" + new File(filePath).lastModified(), fileId, replicationDeg);
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

    @Override
    public void update(Observable o, Object arg) {
        MessageParser parser = new StoredMessage.Parser();
        Message message;
        try {
            message = parser.parse((byte[])arg);
        } catch (IOException | MalformedMessageException e) {
            return;
        }
        int chunkNo = Integer.parseInt(message.getChunkNo());
        HashSet<String> peers = chunksReplication.get(chunkNo);
        peers.add(message.getSenderId()); //According to the java docs, if it already exists it just isn't added.
        Logger.getInstance().printLog(new String((byte[])arg));
    }
}
