package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.PutChunkMessage;
import communication.message.StoredMessage;
import general.Logger;
import general.MalformedMessageException;
import general.MulticastChannel;

import java.io.IOException;
import java.util.*;


public class BackupChunkInitiator implements Runnable, Observer {
    private static final int MAX_ATTEMPTS = 5;
    public static final int WAITING_TIME = 1000;
    private String localId;
    private String fileId;
    private String chunkNo;
    private int replicationDeg;
    private byte[] chunk;
    private MulticastChannel mc;
    private MulticastChannel mdb;
    private Set<String> storeds;

    public BackupChunkInitiator(String localId, String fileId, String chunkNo, int replicationDeg, byte[] chunk, MulticastChannel mc, MulticastChannel mdb) {
        this.localId = localId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.chunk = chunk;
        this.mc = mc;
        this.mdb = mdb;

        this.storeds = Collections.synchronizedSet(new HashSet<>());
    }


    @Override
    public void run() {
        try {
            Message message = new PutChunkMessage(localId, fileId, "" + chunkNo, "" + replicationDeg, Arrays.copyOf(chunk, chunk.length));

            mc.addObserver(this);

            int attempt;
            for (attempt = 0; attempt < MAX_ATTEMPTS; ++attempt) {
                mdb.send(message.getBytes());

                try {
                    Thread.sleep((long) (WAITING_TIME * Math.pow(2, attempt)));
                } catch (InterruptedException ignored) {}

                if (reachedReplicationDeg())
                    break;
            }
            mc.deleteObserver(this);

            if (!reachedReplicationDeg()) {
                Logger.getInstance().printLog("Chunk " + chunkNo + "from file " + fileId + " didn't reach desired replication degree.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        MessageParser parser = new StoredMessage.Parser();
        Message message;
        try {
            message = parser.parse((byte[])arg);
        } catch (IOException | MalformedMessageException e) {
            return;
        }

        if (!message.getFileId().equals(fileId) || !message.getChunkNo().equals(chunkNo))
            return;

        storeds.add(message.getSenderId());
    }

    public boolean reachedReplicationDeg() {
        return replicationDeg <= storeds.size();
    }
}
