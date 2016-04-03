package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.ChunkMessage;
import general.ChunksMetadataManager;
import general.MalformedMessageException;
import general.MulticastChannel;
import general.Subprotocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkInitiator extends Subprotocol implements Runnable, Observer {
    private String fileId;
    private String chunkNo;
    private String path;
    private MulticastChannel mdr;
    private AtomicBoolean alreadySent;
    private final static MessageParser parser = new ChunkMessage.Parser();

    public ChunkInitiator(String localId, String fileId, String chunkNo, MulticastChannel mdr) {
        super(localId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.path = "peer" + localId + "/" + fileId + "-" + chunkNo + ".chunk";
        this.mdr = mdr;
        this.alreadySent = new AtomicBoolean();
    }

    @Override
    public void run() {
        this.alreadySent.set(false);
        mdr.addObserver(this);

        FileInputStream in = null;
        try {
            in = new FileInputStream(path);
            byte[] buffer =  new byte[64*1000];
            int size = in.read(buffer, 0, buffer.length);
            byte[] message = new ChunkMessage(getLocalId(), fileId, chunkNo, Arrays.copyOf(buffer, size)).getBytes();

            Thread.sleep((long) (Math.random() * 400));

            if (alreadySent.get())
                return;

            mdr.send(message);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        mdr.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            Message message = parser.parse((byte[]) arg);

            if (message.getFileId().equals(fileId) && message.getChunkNo().equals(fileId))
                alreadySent.set(true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedMessageException e) {
            e.printStackTrace();
        }
    }
}
