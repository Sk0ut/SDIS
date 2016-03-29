package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import general.Logger;
import general.MalformedMessageException;
import general.SubProtocolListener;

import java.io.IOException;

public class PutChunkListener extends SubProtocolListener {
    private static final MessageParser parser = new PutChunkMessage.Parser();

    public void processMessage(byte[] args) throws IOException, MalformedMessageException {
        Message msg = parser.parse(args);
        Logger.getInstance().printLog(msg.getHeader());
    }

}
