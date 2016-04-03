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
    public static int localId;

    public static void main(String [] args){
        final int MIN_ARGS = 7;
        final int MAX_ARGS = 8;

        if (! (MIN_ARGS <= args.length && args.length <= MAX_ARGS) ) {
            System.out.println("Usage: Peer <id> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port> [ENH]");
            System.out.println("No arguments for default values");
            return;
        }

        int id = Integer.parseInt(args[0]);
        String mcAddress = args[1];
        int mcPort = Integer.parseInt(args[2]);
        String mdbAddress = args[3];
        int mdbPort = Integer.parseInt(args[4]);
        String mdrAddress = args[5];
        int mdrPort = Integer.parseInt(args[6]);
        boolean enhanced = false;

        if (args.length == MAX_ARGS) {
            if (args[7].equalsIgnoreCase("ENH"))
                enhanced = true;
            else {
                System.out.println("Usage: Peer <id> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port> [ENH]");
                return;
            }
        }

        try {
            if (enhanced)
                new EnhancedPeer(id, mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort).start();
            else
                new Peer(id, mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort).start();
        } catch (IOException e) {
            System.out.println("Failed to start Peer");
        }
    }

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
        new RemovedListener(""+localId, mcChannel, mdbChannel).start();
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

    public static long freeSpace() {
        return SpaceMetadataManager.getInstance().getAvailableSpace() - ChunksMetadataManager.getInstance().getOccupiedSpace();
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
            bi.storeMetadata();
            bi.sendChunks();
            bi.updateMetadata();
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

        String fileId = FilesMetadataManager.getInstance().getFileId(path);

        try {
            DeleteInitiator di = new DeleteInitiator(fileId, "" + localId, mcChannel);
            di.deleteFile();
            di.setMetadata();
        } catch (IOException e) {
            return "Failed to remove file " + filepath + " from backup system";
        }

        return "File " + filepath + " removed from backup system";
    }

    @Override
    public String reclaim(long spaceUnbound) throws RemoteException {
        SpaceMetadataManager spaceManager = SpaceMetadataManager.getInstance();
        long space = Math.min(spaceUnbound, spaceManager.MAX_SPACE);
        if (space <= freeSpace() + spaceManager.getReclaimedSpace()) {
            try {
                spaceManager.setReclaimedSpace(space);
            } catch (IOException e) {
                return "Failed to reclaim space";
            }
            return "Reclaimed " + space + " bytes without deleting chunks. Reserved backup space: " + spaceManager.getAvailableSpace() + " bytes.";
        }

        try {
            spaceManager.setReclaimedSpace(space);
            ReclaimInitiator ri = new ReclaimInitiator(space, "" + localId, mcChannel);
            ri.deleteChunks();
            if (freeSpace() < 0) {
                spaceManager.setReclaimedSpace(space + freeSpace());
                return "Unable to reclaim " + spaceUnbound + " bytes, reclaimed " + spaceManager.getReclaimedSpace() + " bytes instead. " +
                        "Reserved backup space: " + spaceManager.getAvailableSpace() + " bytes";
            }

        } catch (IOException e) {
            return "Failed to reclaim space";
        }
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
