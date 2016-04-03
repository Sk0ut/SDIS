package subprotocol;

import communication.Message;
import communication.message.RemovedMessage;
import general.ChunkIdentifier;
import general.ChunksMetadataManager;
import general.MulticastChannel;

import java.io.IOException;
import java.util.List;

/**
 * Created by Afonso on 02/04/2016.
 */
public class ReclaimInitiator {
    private final String localId;
    private final long space;
    private MulticastChannel mc;

    public ReclaimInitiator(long space, String localId, MulticastChannel mc) {
        this.space = space;
        this.localId = localId;
        this.mc = mc;
    }

    private List<ChunkIdentifier> deleteChunks() throws IOException {
        List<ChunkIdentifier> chunks = ChunksMetadataManager.getInstance().getNRemovableChunks(space);
        for(ChunkIdentifier chunk : chunks){
            ChunksMetadataManager.getInstance().removeFileIfExists(chunk.getFileId(), chunk.getChunkNo());
            Message message = new RemovedMessage(localId, chunk.getFileId(), chunk.getChunkNo());
            mc.send(message.getBytes());
        }
        return null;
    }
}
