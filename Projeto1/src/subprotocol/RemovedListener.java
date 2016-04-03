package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.RemovedMessage;
import general.*;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class RemovedListener extends Subprotocol implements Observer {
    private static final MessageParser parser = new RemovedMessage.Parser();
    private MulticastChannel mc;
    private MulticastChannel mdb;

    public RemovedListener(String localId, MulticastChannel mc, MulticastChannel mdb) {
        super(localId);
        this.mc = mc;
        this.mdb = mdb;
    }

    public void start() {
        mc.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        Message msg = null;
        try {
            msg = parser.parse((byte[]) arg);
            if (msg.getSenderId().equals(getLocalId()))
                return;

            ChunksMetadataManager.getInstance().removePeerIfExists(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
            ChunksMetadataManager.Entry entry = ChunksMetadataManager.getInstance().findChunk(msg.getFileId(), msg.getChunkNo());
            if ((entry != null) && (Integer.parseInt(entry.repDegree) > entry.peers.size())) {
                new Thread(new ReplicationDegRestoreInitiator(getLocalId(), msg.getFileId(), msg.getChunkNo(), mc, mdb)).start();
            }
        } catch (IOException | MalformedMessageException ignored) {}
    }
}
