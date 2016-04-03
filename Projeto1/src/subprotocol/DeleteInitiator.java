package subprotocol;

import communication.message.DeleteMessage;
import general.FilesMetadataManager;
import general.MulticastChannel;

import java.io.IOException;

public class DeleteInitiator {
    private static final int ATTEMPTS = 5;

    private String fileId;
    private String localId;
    private String filePath;
    private MulticastChannel mcChannel;

    public DeleteInitiator(String fileId, String localId, MulticastChannel mcChannel){
        this.fileId = fileId;
        this.localId = localId;
        this.mcChannel = mcChannel;
    }

    public void deleteFile() throws IOException {
        for(int attempt = 0; attempt < ATTEMPTS; ++attempt) {
            byte[] message = new DeleteMessage(localId, fileId).getBytes();
            mcChannel.send(message);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}

        }
    }

    public void setMetadata() throws IOException {
        FilesMetadataManager.getInstance().changeFileStatus(filePath, FilesMetadataManager.FileStatus.DELETED);

    }
}
