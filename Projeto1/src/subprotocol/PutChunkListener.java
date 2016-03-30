package subprotocol;

import communication.ChannelManager;
import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.Logger;
import general.MalformedMessageException;
import general.SubProtocolListener;

import java.io.FileOutputStream;
import java.io.IOException;

public class PutChunkListener extends SubProtocolListener {
    private static final MessageParser parser = new PutChunkMessage.Parser();

    public PutChunkListener(String localId) {
        super(localId);
    }

    public void processMessage(byte[] args) throws IOException, MalformedMessageException {
        Message msg = parser.parse(args);
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
        ChannelManager.getInstance().send(ChannelManager.ChannelType.CONTROLCHANNEL, message);
    }

}
