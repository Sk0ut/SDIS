package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

public class PutChunkListener extends Subprotocol implements Observer {
    private static final MessageParser parser = new PutChunkMessage.Parser();
    private final MulticastChannel mc;
    private final MulticastChannel mdb;

    public PutChunkListener(String localId, MulticastChannel mc, MulticastChannel mdb) {
        super(localId);
        this.mc = mc;
        this.mdb = mdb;
    }

    public void start() {
        this.mdb.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            Message msg = parser.parse((byte[]) arg);

            ChunksMetadataManager chunksMetadataManager = ChunksMetadataManager.getInstance();

            if (msg.getSenderId().equals(getLocalId()))
                return;

            if (chunksMetadataManager.findChunk(msg.getFileId(), msg.getChunkNo()) == null) {
                String fileName = "peer" + getLocalId() + "/" + msg.getFileId() + "-" + msg.getChunkNo() + ".chunk";
                FileOutputStream pw = new FileOutputStream(fileName);
                pw.write(msg.getBody(), 0, msg.getBody().length);
                pw.flush();
                pw.close();
                chunksMetadataManager.addFileIfNotExists(msg.getFileId(), msg.getChunkNo(), msg.getReplicationDeg(), new HashSet<>());
            }


            Logger.getInstance().printLog(msg.getHeader());

            try {
                Thread.sleep((long) (Math.random() * 400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            byte[] message = new StoredMessage(getLocalId(),msg.getFileId(),msg.getChunkNo()).getBytes();
            mc.send(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedMessageException ignored) {}
    }
}
