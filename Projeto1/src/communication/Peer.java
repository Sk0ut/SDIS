package communication;

import client.BackupService;
import general.ChunksMetadataManager;
import general.FilesMetadataManager;
import general.MulticastChannelManager;
import subprotocol.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by afonso on 26-03-2016.
 */
public class Peer implements BackupService{
    private MulticastChannelManager mcChannel;
    private MulticastChannelManager mdbChannel;
    private MulticastChannelManager mdrChannel;
    public static int localId;

    public Peer(int id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        Peer.localId = id;
        new File("peer"+ id).mkdir();
        File chunkMetadata = new File("peer"+id+File.separator + "chunks.metadata");
        if(!chunkMetadata.exists())
            chunkMetadata.createNewFile();
        File fileMetadata = new File("peer"+id+File.separator + "files.metadata");
        if(!fileMetadata.exists())
            fileMetadata.createNewFile();
        ChunksMetadataManager.getInstance().init("" + id);
        FilesMetadataManager.getInstance().init("" + id);
        MulticastSocket receiveSocketMc = new MulticastSocket(mcPort);
        MulticastSocket receiveSocketMdb = new MulticastSocket(mdbPort);
        MulticastSocket receiveSocketMdr = new MulticastSocket(mdrPort);
        receiveSocketMc.joinGroup(InetAddress.getByName(mcAddress));
        receiveSocketMdb.joinGroup(InetAddress.getByName(mdbAddress));
        receiveSocketMdr.joinGroup(InetAddress.getByName(mdrAddress));
        receiveSocketMc.setTimeToLive(1);
        receiveSocketMdr.setTimeToLive(1);
        receiveSocketMdb.setTimeToLive(1);
        mcChannel = new MulticastChannelManager(receiveSocketMc);
        mdbChannel = new MulticastChannelManager(receiveSocketMdb);
        mdrChannel = new MulticastChannelManager(receiveSocketMdr);
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


        mcChannel.addSubProtocol(new PutChunkListener("" + localId, mdbChannel));
        mcChannel.addSubProtocol(new GetChunkListener("" + localId, mcChannel));
        mcChannel.addSubProtocol(new RemovedListener("" + localId, mcChannel));
        mcChannel.addSubProtocol(new DeleteListener("" + localId, mcChannel));

        new Thread(mcChannel).start();
        new Thread(mdbChannel).start();
        new Thread(mdrChannel).start();

        while (true) ;

        /*try {
            backup("C:\\Users\\Afonso\\Desktop\\Faculdade\\2º Semestre\\SDIS\\Projeto1", 3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
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
    public void restore(String filename) throws RemoteException {
        try {
            RestoreInitiator ri = new RestoreInitiator(filename, "" + localId, mcChannel, mdrChannel);
            ri.getChunks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String filename) throws RemoteException {
        try {
            DeleteInitiator di = new DeleteInitiator(filename, "" + localId, mcChannel);
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
