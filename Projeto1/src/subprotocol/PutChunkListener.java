package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.Logger;
import general.MalformedMessageException;
import general.MulticastChannel;
import general.Subprotocol;

import java.io.FileOutputStream;
import java.io.IOException;
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
            String fileName = "peer" + getLocalId() + "/" + msg.getFileId() + "-" + msg.getChunkNo() + ".chunk";
            FileOutputStream pw = new FileOutputStream(fileName);
            pw.write(msg.getBody(), 0, msg.getBody().length);
            pw.flush();
            //Logger.getInstance().printLog(msg.getHeader());
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
