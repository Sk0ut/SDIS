package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.ChunkMessage;
import communication.message.GetChunkMessage;
import general.FilesMetadataManager;
import general.MalformedMessageException;
import general.MulticastChannel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Afonso on 02/04/2016.
 */
public class RestoreInitiator implements Observer {
    private static final int MAXCHUNKSIZE = 64 * 1000;
    private final MulticastChannel mcChannel;
    private final MulticastChannel mdrChannel;
    private final String filePath;
    private final String fileId;
    private final String localId;
    private final List<Integer> chunksToReceive;
    private RandomAccessFile file;

    public RestoreInitiator(String filePath, String localId, MulticastChannel mcChannel, MulticastChannel mdrChannel) throws IOException {
        this.filePath = filePath;
        this.localId = localId;
        this.mcChannel = mcChannel;
        this.mdrChannel = mdrChannel;
        long fileSize = new File(filePath).length();
        int totalChunks = (int) (fileSize / MAXCHUNKSIZE) + 1;
        this.chunksToReceive = IntStream.range(0, totalChunks).boxed().collect(Collectors.toList());
        this.file = new RandomAccessFile(filePath, "w");
        this.fileId = FilesMetadataManager.getInstance().getFileId(filePath);
    }

    public void getChunks() throws IOException {
        while (true) {
            mdrChannel.addObserver(this);
            for (int i : chunksToReceive) {
                byte[] message = new GetChunkMessage(localId, fileId, "" + i).getBytes();
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
