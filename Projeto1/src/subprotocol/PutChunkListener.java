package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import general.Logger;
import general.MalformedMessageException;
import general.SubProtocolListener;

import java.io.File;
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
        File file = new File(fileName);
        FileOutputStream pw = new FileOutputStream(fileName);
        pw.write(msg.getBody(), 0, msg.getBody().length);
        pw.flush();

        try {
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Logger.getInstance().printLog(msg.getHeader());
    }

}
