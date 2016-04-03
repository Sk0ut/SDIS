package subprotocol;

import communication.Message;
import communication.message.StoredMessage;
import general.*;

import java.io.IOException;
import java.util.*;

public class StoredListener extends Subprotocol implements Observer {
    private MulticastChannel mc;
    private static StoredMessage.Parser parser = new StoredMessage.Parser();

    public StoredListener(String localId, MulticastChannel mc) {
        super(localId);
        this.mc = mc;
    }



    public void start() {
        mc.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            Message message = parser.parse((byte []) arg);
            ChunkIdentifier identifier = new ChunkIdentifier(message.getFileId(), message.getChunkNo());
            ChunksMetadataManager.getInstance().addPeerIfNotExists(message.getFileId(), message.getChunkNo(), message.getSenderId());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedMessageException ignored) {}
    }
}
