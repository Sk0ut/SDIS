package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.*;
import general.*;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Afonso on 03/04/2016.
 */
public class DeleteEnhListener extends Subprotocol implements Observer{
    private MulticastChannel mc;

    public DeleteEnhListener(String localId, MulticastChannel mc) {
        super(localId);
        this.mc = mc;
    }

    public void start() {
        mc.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        MessageParser[] parsers = {new ChunkMessage.Parser(), new GetChunkMessage.Parser(), new PutChunkMessage.Parser(),
        new RemovedMessage.Parser(), new StoredMessage.Parser()};
        for (MessageParser parser : parsers) {
            try {
                Message msg = parser.parse((byte[]) arg);
                if (FilesMetadataManager.getInstance().findDeletedFileById(msg.getFileId()) != null) {
                    Message send = new DeleteMessage(getLocalId(), msg.getFileId());
                    mc.send(send.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MalformedMessageException ignored) {}
        }

    }
}
