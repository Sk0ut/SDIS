package subprotocol;

import communication.Message;
import communication.message.DeleteMessage;
import general.Logger;
import general.MalformedMessageException;
import general.ChunksMetadataManager;
import general.SubProtocolListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
        String filePrefix = "peer" + getLocalId() + "/" + msg.getFileId();
        List<String> chunks = ChunksMetadataManager.getInstance().getChunksFromFile(msg.getFileId());
        for(String chunk : chunks) {
            String fileName = filePrefix + "-" + chunk + ".chunk";
            File f = new File(fileName);
            ChunksMetadataManager.getInstance().removeFileIfExists(msg.getFileId(), chunk);
            if (f.exists() && !f.isDirectory()) {
                f.delete();
            }
        }
        Logger.getInstance().printLog(msg.getHeader());

    }
}
