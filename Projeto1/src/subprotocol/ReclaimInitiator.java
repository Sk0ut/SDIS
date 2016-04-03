package subprotocol;

import communication.Message;
import communication.Peer;
import communication.message.RemovedMessage;
import general.ChunkIdentifier;
import general.ChunksMetadataManager;
import general.MulticastChannel;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReclaimInitiator {
    private final String localId;
    private final long space;
    private MulticastChannel mc;

    public ReclaimInitiator(long space, String localId, MulticastChannel mc) {
        this.space = space;
        this.localId = localId;
        this.mc = mc;
    }

    public void deleteChunks() throws IOException {
        List<ChunkIdentifier> chunks = ChunksMetadataManager.getInstance().getNRemovableChunks(space);

        while (Peer.freeSpace() < 0 && chunks.size() != 0) {
            ChunkIdentifier chunk = chunks.remove(0);
            String fileName = "peer" + Peer.localId + File.separator + chunk.getFileId() + "-" + chunk.getChunkNo() + ".chunk";
            File f = new File(fileName);
            ChunksMetadataManager.getInstance().removeFileIfExists(chunk.getFileId(), chunk.getChunkNo());
            if (f.exists() && !f.isDirectory()) {
                f.delete();
            }
            Message message = new RemovedMessage(localId, chunk.getFileId(), chunk.getChunkNo());
            mc.send(message.getBytes());
        }
    }
}
