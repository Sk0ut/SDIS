package subprotocol;

import communication.Message;
import communication.message.DeleteMessage;
import general.Logger;
import general.MalformedMessageException;
import general.ChunksMetadataManager;
import general.SubProtocolListener;

import java.io.File;
import java.io.IOException;

/**
 * Created by afonso on 26-03-2016.
 */
public class DeleteListener extends SubProtocolListener {
    private static final DeleteMessage.Parser parser = new DeleteMessage.Parser();

    public DeleteListener(String localId) {
        super(localId);
    }

    public void processMessage(byte[] args) throws IOException, MalformedMessageException {
        Message msg = parser.parse(args);
        String fileName = "peer" + getLocalId() + "/" + msg.getFileId() + "-" + msg.getChunkNo() + ".chunk";

        System.out.println(fileName);
        File f = new File(fileName);
        ChunksMetadataManager.getInstance().removeFileIfExists(msg.getFileId(), msg.getChunkNo());
        if (f.exists() && !f.isDirectory()) {
            f.delete();
        }
        Logger.getInstance().printLog(msg.getHeader());
    }
}
