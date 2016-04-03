package communication;

import client.BackupService;
import general.ChunksMetadataManager;
import general.FilesMetadataManager;
import general.MulticastChannel;
import general.SpaceMetadataManager;
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
public class Peer implements BackupService {
    protected MulticastChannel mcChannel;
    protected MulticastChannel mdbChannel;
    protected MulticastChannel mdrChannel;
    protected int localId;

    public Peer(int id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        localId = id;
        new File("peer"+ id).mkdir();
        ChunksMetadataManager.getInstance().init("" + id);
        FilesMetadataManager.getInstance().init("" + id);
        SpaceMetadataManager.getInstance().init("" + id);
        mcChannel = new MulticastChannel(mcAddress, mcPort);
        mdbChannel = new MulticastChannel(mdbAddress, mdbPort);
        mdrChannel = new MulticastChannel(mdrAddress, mdrPort);
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
        new GetChunkListener(""+localId, mcChannel, mdrChannel).start();

        /*
        try {
            //backup((new File("Pikachu.png")).getCanonicalPath(), 1);
            //delete((new File("Pikachu.png")).getCanonicalPath());
            //restore((new File("Pikachu.png").getCanonicalPath()));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    protected void startMulticastChannelListeners() {
        new Thread(mcChannel).start();
        new Thread(mdbChannel).start();
        new Thread(mdrChannel).start();
    }

    protected void registerRMI() throws RemoteException {
        BackupService service = (BackupService) UnicastRemoteObject.exportObject(this, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(Integer.toString(localId), service);
    }

    @Override
    public String backup(String filepath, int replicationDeg) throws RemoteException {
        String path;
        try {
             path = new File(filepath).getCanonicalPath();
        } catch (IOException e) {
            return "Failed to locate file " + filepath;
        }

        try {
            BackupInitiator bi = new BackupInitiator(path, "" + localId, replicationDeg, mcChannel, mdbChannel);
            bi.sendChunks();
            bi.storeMetadata();
        } catch (IOException e) {
            return "Failed to backup file " + filepath;
        }

        return "File " + filepath + " backed up";
    }

    @Override
    public String restore(String filepath) throws RemoteException {
        String path;
        try {
            path = new File(filepath).getCanonicalPath();
        } catch (IOException e) {
            return "Failed to locate file " + filepath;
        }

        try {
            if (FilesMetadataManager.getInstance().getFileId(path) == null)
                return "File not registered in backup system";
            RestoreInitiator ri = new RestoreInitiator(path, "" + localId, mcChannel, mdrChannel);
            if (ri.getChunks()) {
                return filepath + " restored sucessfully";
            } else {
                return "Failed to restore file " + filepath;
            }
        } catch (IOException e) {
            return "Failed to restore file " + filepath;
        }
    }

    @Override
    public String delete(String filepath) throws RemoteException {
        String path;
        try {
            path = new File(filepath).getCanonicalPath();
        } catch (IOException e) {
            return "Failed to locate file " + filepath;
        }

        FilesMetadataManager.getInstance().getFileId(filepath);

        try {
            DeleteInitiator di = new DeleteInitiator(path, "" + localId, mcChannel);
            di.deleteFile();
            di.setMetadata();
        } catch (IOException e) {
            return "Failed to remove file " + filepath + " from backup system";
        }

        return "File " + filepath + " removed from backup system";
    }

    @Override
    public String reclaim(long space) throws RemoteException {
        SpaceMetadataManager spaceManager = SpaceMetadataManager.getInstance();
        if (space <= spaceManager.getReclaimedSpace()) {
            try {
                spaceManager.setReclaimedSpace(space);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Reclaimed " + space + " bytes. Reserved backup space: " + spaceManager.getAvailableSpace() + " bytes.";
        }

        // Check for unused space

        ReclaimInitiator ri = new ReclaimInitiator(space, "" + localId, mcChannel);

        return "Reclaimed " + space + " bytes. Reserved backup space: " + spaceManager.getAvailableSpace() + " bytes";
    }

    @Override
    public String backupEnh(String filepath, int replicationDeg) throws RemoteException {
        return "Subprotocol not supported";
    }

    @Override
    public String restoreEnh(String filepath) throws RemoteException {
        return "Subprotocol not supported";
    }

    @Override
    public String deleteEnh(String filepath) throws RemoteException {
        return "Subprotocol not supported";
    }

    @Override
    public String reclaimEnh(long space) throws RemoteException {
        return "Subprotocol not supported";
    }
}
