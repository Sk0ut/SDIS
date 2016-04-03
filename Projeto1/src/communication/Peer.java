package communication;

import client.BackupService;
import general.ChunksMetadataManager;
import general.FilesMetadataManager;
import general.MulticastChannel;
import subprotocol.*;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by afonso on 26-03-2016.
 */
public class Peer implements BackupService{
    private MulticastChannel mcChannel;
    private MulticastChannel mdbChannel;
    private MulticastChannel mdrChannel;
    public static int localId;

    public Peer(int id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        Peer.localId = id;
        new File("peer"+ id).mkdir();
        ChunksMetadataManager.getInstance().init("" + id);
        FilesMetadataManager.getInstance().init("" + id);
        mcChannel = new MulticastChannel(mcAddress, mcPort);
        mdbChannel = new MulticastChannel(mdbAddress, mdbPort);
        mdrChannel = new MulticastChannel(mdrAddress, mdrPort);
    }

    public void start() {

        BackupService service = null;
        Registry registry = null;

        try {
            service = (BackupService) UnicastRemoteObject.exportObject(this, 0);
            registry = LocateRegistry.getRegistry();
            registry.rebind(Integer.toString(localId), service);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        new PutChunkListener("" + localId, mcChannel, mdbChannel).start();
        new StoredListener(""+localId, mcChannel).start();
        new DeleteListener(""+localId, mcChannel).start();
        new GetChunkListener(""+localId, mcChannel, mdrChannel).start();

        new Thread(mcChannel).start();
        new Thread(mdbChannel).start();
        new Thread(mdrChannel).start();

        try {
            //backup((new File("Pikachu.png")).getCanonicalPath(), 1);
            //delete((new File("Pikachu.png")).getCanonicalPath());
            restore((new File("Pikachu.png").getCanonicalPath()));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backup(String filepath, int replicationDeg) throws RemoteException {
        try {
            BackupInitiator bi = new BackupInitiator(filepath, "" + localId, replicationDeg, mcChannel, mdbChannel);
            bi.sendChunks();
            bi.storeMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restore(String filepath) throws RemoteException {
        try {
            if (FilesMetadataManager.getInstance().getFileId(filepath) == null)
                return;
            RestoreInitiator ri = new RestoreInitiator(filepath, "" + localId, mcChannel, mdrChannel);
            ri.getChunks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String filepath) throws RemoteException {
        try {
            DeleteInitiator di = new DeleteInitiator(filepath, "" + localId, mcChannel);
            di.deleteFile();
            di.setMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reclaim(long space) throws RemoteException {
        ReclaimInitiator ri = new ReclaimInitiator(space, "" + localId);
    }
}
