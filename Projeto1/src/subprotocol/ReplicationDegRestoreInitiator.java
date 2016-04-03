package subprotocol;

import communication.Message;
import communication.message.PutChunkMessage;
import general.ChunksMetadataManager;
import general.MalformedMessageException;
import general.MulticastChannel;
import general.Subprotocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Flávio on 03/04/2016.
 */
public class ReplicationDegRestoreInitiator extends Subprotocol implements Observer, Runnable {
    private MulticastChannel mc;
    private MulticastChannel mdb;
    private String fileId;
    private String chunkNo;
    private static PutChunkMessage.Parser parser = new PutChunkMessage.Parser();
    private AtomicBoolean backupStarted;


    public ReplicationDegRestoreInitiator(String localId, String fileId, String chunkNo, MulticastChannel mc, MulticastChannel mdb) {
        super(localId);
        this.mc = mc;
        this.mdb = mdb;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.backupStarted = new AtomicBoolean();
    }

    @Override
    public void run() {
        this.backupStarted.set(false);
        mc.addObserver(this);

        try {
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException ignored) {}


        mc.deleteObserver(this);

        if (!backupStarted.get()) {
            ChunksMetadataManager.Entry entry = ChunksMetadataManager.getInstance().findChunk(fileId, chunkNo);

            try {
                FileInputStream in = new FileInputStream(new File(fileId + '-' + chunkNo + ".chunk"));
                byte [] chunk = new byte[64 * 1000];
                int size = in.read(chunk);
                new BackupChunkInitiator(getLocalId(), fileId, chunkNo, Integer.parseInt(entry.repDegree), Arrays.copyOf(chunk, size), mc, mdb).run();
            } catch (IOException ignored) {
            }
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            Message message = parser.parse((byte []) arg);
            if (message.getSenderId().equals(getLocalId()))
                return;

            if (message.getFileId().equals(fileId) && message.getChunkNo().equals(chunkNo))
                this.backupStarted.set(true);
        } catch (IOException e) {

        } catch (MalformedMessageException ignored) {}
    }
}
