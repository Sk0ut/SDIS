package subprotocol;


import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.FilesMetadataManager;
import general.MalformedMessageException;
import general.MulticastChannelManager;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by Afonso on 31/03/2016.
 */
public class BackupInitiator implements Observer {
    private static final int MAXCHUNKSIZE = 64 * 1024;
    private final MulticastChannelManager mcListener;
    private final MulticastChannelManager mdbListener;

    private String filePath;
    private int totalChunks;
    private String localId;
    private int replicationDeg;
    private String fileId;
    private Map<Integer, HashSet<String>> chunksReplication;

    public BackupInitiator(String filePath, String localId, int replicationDeg, MulticastChannelManager mcChannel, MulticastChannelManager mdbChannel) throws IOException {
        this.filePath = filePath;
        this.localId = localId;
        this.replicationDeg = replicationDeg;
        this.mcListener = mcChannel;
        this.mdbListener = mdbChannel;
        long fileSize = new File(filePath).length();
        totalChunks = (int)(fileSize / MAXCHUNKSIZE) + 1;
        chunksReplication = new HashMap<>();
        for (int i : IntStream.range(0, totalChunks).toArray()){
            chunksReplication.put(i, new HashSet<>());
        }
    }

    public void sendChunks() throws IOException{
        FileInputStream in = new FileInputStream(filePath);
        byte[] buffer = new byte[MAXCHUNKSIZE];
        fileId = generateFileId();
        byte[] message;
        List<Integer> chunksBelowReplicationDeg;
        while(true) {
            chunksBelowReplicationDeg = checkReplicationDeg();
            if (chunksBelowReplicationDeg.size() == 0)
                break;
            mcListener.addObserver(this);
            for (int i : chunksBelowReplicationDeg) {
                in.read(buffer, i * MAXCHUNKSIZE, buffer.length);
                message = new PutChunkMessage(localId, fileId, "" + i, "" + replicationDeg, buffer).getBytes();
                mdbListener.send(message);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mcListener.deleteObserver(this);
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
    }
}
