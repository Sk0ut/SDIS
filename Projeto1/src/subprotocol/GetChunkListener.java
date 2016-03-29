package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.GetChunkMessage;
import general.Logger;
import general.MalformedMessageException;
import general.SubProtocolListener;

import java.io.IOException;

/**
 * Created by afonso on 26-03-2016.
 */
public class GetChunkListener extends SubProtocolListener {
    private static final MessageParser parser = new GetChunkMessage.Parser();

    public void processMessage(byte[] args) throws IOException, MalformedMessageException {
        Message msg = parser.parse(args);
        Logger.getInstance().printLog(msg.getHeader());
    }
}
