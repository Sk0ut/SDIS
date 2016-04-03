package communication;

import client.BackupService;
import subprotocol.*;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Created by Flávio on 03/04/2016.
 */
public class EnhancedPeer extends Peer {
    public EnhancedPeer(int id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        super(id, mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
    }

    public void start() {

        try {
            registerRMI();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        startMulticastChannelListeners();

        new PutChunkListener("" + localId, mcChannel, mdbChannel).start();
        new StoredListener(""+localId, mcChannel).start();
        new DeleteListener(""+localId, mcChannel).start();
        new DeleteEnhListener("" + localId, mcChannel).start();
        new GetChunkListener(""+localId, mcChannel, mdrChannel).start();
    }


    @Override
    public String backupEnh(String filepath, int replicationDeg) throws RemoteException
    {
        return "Not implemented";
    }

    public String restoreEnh(String filepath) throws RemoteException
    {
        return "Not implemented";
    }

    public String deleteEnh(String filepath) throws RemoteException
    {
        return "DELETEENH subprotocol will search for messages about deleted files and send a DELETE message again to the system.\n"
                + delete(filepath);
    }

    public String reclaimEnh(long space) throws RemoteException {
        return "Reclaim enhanced is not testable by invoking RECLAIMENH. Executing RECLAIM instead.\n" + reclaim(space);
    }
}
