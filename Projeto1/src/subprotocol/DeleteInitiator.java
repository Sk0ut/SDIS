package subprotocol;

import communication.ChannelManager;
import communication.Message;
import communication.message.DeleteMessage;
import general.FilesMetadataManager;

import java.io.IOException;

/**
 * Created by Afonso on 02/04/2016.
 */
public class DeleteInitiator {
    private static final int ATTEMPTS = 5;

    private String fileId;
    private String localId;
    private String filePath;

    public DeleteInitiator(String filename, String localId){
        fileId = FilesMetadataManager.getInstance().getFileId(filename);
        this.localId = localId;
        this.filePath = filename;
    }

    public void deleteFile() throws IOException {
        for(int attempt = 0; attempt < ATTEMPTS; ++attempt) {
            byte[] message = new DeleteMessage(localId, fileId).getBytes();
            ChannelManager.getInstance().send(ChannelManager.ChannelType.CONTROLCHANNEL, message);
        }
    }

    public void setMetadata() throws IOException {
        FilesMetadataManager.getInstance().removeIfExists(filePath);
    }
}
