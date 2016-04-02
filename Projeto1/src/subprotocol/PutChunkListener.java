package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.Logger;
import general.MalformedMessageException;
import general.MulticastChannelManager;
import general.SubProtocolListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class PutChunkListener extends SubProtocolListener implements Observer {
    private static final MessageParser parser = new PutChunkMessage.Parser();

    public PutChunkListener(String localId, MulticastChannelManager mcm) {
        super(localId, mcm);    }

    @Override
    public void update(Observable o, Object arg) {
        Message msg = null;
        try {
            msg = parser.parse((byte[]) arg);
            String fileName = "peer" + getLocalId() + "/" + msg.getFileId() + "-" + msg.getChunkNo() + ".chunk";
            FileOutputStream pw = new FileOutputStream(fileName);
            pw.write(msg.getBody(), 0, msg.getBody().length);
            pw.flush();
            Logger.getInstance().printLog(msg.getHeader());
            try {
                Thread.sleep((long) (Math.random() * 400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] message = new StoredMessage(getLocalId(),msg.getFileId(),msg.getChunkNo()).getBytes();
            mcm.send(message);
        } catch (IOException | MalformedMessageException e) {
            e.printStackTrace();
        }
    }
}
