package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.Peer;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

public class EnhancedPutChunkListener extends Subprotocol implements Observer {
    private static final MessageParser parser = new PutChunkMessage.Parser();
    private final MulticastChannel mc;
    private final MulticastChannel mdb;

    private class PutChunkWaiter implements Runnable {
        private final String localId;
        private final String fileId;
        private final String chunkNo;
        private final byte[] chunk;
        private final MulticastChannel mc;
        private String replicationDeg;

        public PutChunkWaiter(String localId, String fileId, String chunkNo, String replicationDeg, byte[] chunk, MulticastChannel mc) {
            this.localId = localId;
            this.fileId = fileId;
            this.chunkNo = chunkNo;
            this.chunk = chunk;
            this.mc = mc;
            this.replicationDeg = replicationDeg;
        }

        @Override
        public void run() {
            try {

                ChunksMetadataManager chunksMetadataManager = ChunksMetadataManager.getInstance();
                chunksMetadataManager.addFileIfNotExists(fileId, chunkNo, replicationDeg, Long.toString(chunk.length), new HashSet<>());

                try {
                    Thread.sleep((long) (Math.random() * 400));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ChunksMetadataManager.Entry entry = chunksMetadataManager.findChunk(fileId, chunkNo);


                if (Integer.parseInt(entry.repDegree) <= entry.peers.size()) {
                    chunksMetadataManager.removeFileIfExists(fileId, chunkNo);
                    return;
                }

                String fileName = "peer" + localId + "/" + fileId + "-" + chunkNo + ".chunk";
                FileOutputStream pw = new FileOutputStream(fileName);
                pw.write(chunk, 0, chunk.length);
                pw.flush();
                pw.close();

                byte[] message = new StoredMessage(localId, fileId, chunkNo).getBytes();
                mc.send(message);
            }
            catch (IOException e) {}
        }
    }


    public EnhancedPutChunkListener(String localId, MulticastChannel mc, MulticastChannel mdb) {
        super(localId);
        this.mc = mc;
        this.mdb = mdb;
    }

    public void start() {
        this.mdb.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            Message msg = parser.parse((byte[]) arg);

            ChunksMetadataManager chunksMetadataManager = ChunksMetadataManager.getInstance();

            if (FilesMetadataManager.getInstance().ownedFile(msg.getFileId()))
                return;

            if (chunksMetadataManager.findChunk(msg.getFileId(), msg.getChunkNo()) == null) {
                if (msg.getBody().length > Peer.freeSpace())
                    return;
                new Thread(new PutChunkWaiter(getLocalId(), msg.getFileId(), msg.getChunkNo(), msg.getReplicationDeg(),
                        Arrays.copyOf(msg.getBody(), msg.getBody().length), mc)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedMessageException ignored) {}
    }
}
