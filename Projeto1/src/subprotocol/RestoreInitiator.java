package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.ChunkMessage;
import communication.message.GetChunkMessage;
import general.FilesMetadataManager;
import general.MalformedMessageException;
import general.MulticastChannelManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Afonso on 02/04/2016.
 */
public class RestoreInitiator implements Observer {
    private static final int MAXCHUNKSIZE = 64 * 1024;
    private final MulticastChannelManager mcChannel;
    private final MulticastChannelManager mdrChannel;
    private final String filePath;
    private final String fileId;
    private final String lastModified;
    private final String localId;
    private final List<Integer> chunksToReceive;
    private RandomAccessFile file;

    public RestoreInitiator(String filePath, String localId, MulticastChannelManager mcChannel, MulticastChannelManager mdrChannel) throws IOException {
        this.filePath = filePath;
        this.localId = localId;
        this.mcChannel = mcChannel;
        this.mdrChannel = mdrChannel;
        long fileSize = new File(filePath).length();
        int totalChunks = (int) (fileSize / MAXCHUNKSIZE) + 1;
        this.lastModified = FilesMetadataManager.getInstance().getFileId(filePath);
        this.chunksToReceive = IntStream.range(0, totalChunks).boxed().collect(Collectors.toList());
        this.file = new RandomAccessFile(filePath, "w");
        this.fileId = generateFileId();
    }

    public void getChunks() throws IOException {
        while (true) {
            mdrChannel.addObserver(this);
            for (int i : chunksToReceive) {
                byte[] message = new GetChunkMessage(localId, generateFileId(), "" + i).getBytes();
                mcChannel.send(message);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mdrChannel.deleteObserver(this);
            if (chunksToReceive.size() == 0)
                break;
        }
    }

    private String generateFileId() {
        try {
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
        MessageParser parser = new ChunkMessage.Parser();
        Message message;
        try {
            message = parser.parse((byte[])arg);
        } catch (IOException | MalformedMessageException e) {
            return;
        }
        if(!message.getFileId().equals(fileId))
            return;
        int chunkNo = Integer.parseInt(message.getChunkNo());
        if (chunksToReceive.contains(chunkNo) ){
            try {
                file.write(message.getBody(), chunkNo * MAXCHUNKSIZE, message.getBody().length);
                chunksToReceive.remove(chunkNo);
            } catch (IOException ignored) {}
        }
    }
}
