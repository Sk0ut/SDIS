package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.GetChunkMessage;
import general.Logger;
import general.MalformedMessageException;
import general.SubProtocolListener;

import java.io.*;

/**
 * Created by afonso on 26-03-2016.
 */
public class GetChunkListener extends SubProtocolListener {
    private static final MessageParser parser = new GetChunkMessage.Parser();

    public GetChunkListener(String localId) {
        super(localId);
    }

    public void processMessage(byte[] args) throws IOException, MalformedMessageException {
        Message msg = parser.parse(args);

        String fileName = "peer" + getLocalId() + "/" + msg.getFileId() + "-" + msg.getChunkNo() + ".chunk";

        File f = new File(fileName);
        if(f.exists() && !f.isDirectory()){
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            char[] buffer = new char[64*1024];
            br.read(buffer, 0, buffer.length);
            byte[] file = new String(buffer).getBytes("UTF-8");
            Logger.getInstance().printLog(msg.getHeader());
        }


    }
}
