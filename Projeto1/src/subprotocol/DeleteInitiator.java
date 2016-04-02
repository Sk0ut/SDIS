package subprotocol;

import communication.message.DeleteMessage;
import general.FilesMetadataManager;
import general.MulticastChannelManager;

import java.io.IOException;

/**
 * Created by Afonso on 02/04/2016.
 */
public class DeleteInitiator {
    private static final int ATTEMPTS = 5;

    private String fileId;
    private String localId;
    private String filePath;
    private MulticastChannelManager mcInstance;

    public DeleteInitiator(String filename, String localId, MulticastChannelManager mcChannel){
        fileId = FilesMetadataManager.getInstance().getFileId(filename);
        this.localId = localId;
        this.filePath = filename;
        this.mcInstance = mcChannel;
    }

    public void deleteFile() throws IOException {
        for(int attempt = 0; attempt < ATTEMPTS; ++attempt) {
            byte[] message = new DeleteMessage(localId, fileId).getBytes();
            mcInstance.send(message);
        }
    }

    public void setMetadata() throws IOException {
        FilesMetadataManager.getInstance().removeIfExists(filePath);
    }
}
