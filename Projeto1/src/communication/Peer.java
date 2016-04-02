package communication;

import client.BackupService;
import general.ChunksMetadataManager;
import general.FilesMetadataManager;
import general.MulticastListener;
import subprotocol.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;

/**
 * Created by afonso on 26-03-2016.
 */
public class Peer implements BackupService{
    MulticastListener mcListener;
    MulticastListener mdbListener;
    MulticastListener mdrListener;
    public static int localId;

    public Peer(int id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        ChannelManager.getInstance().init(new InetSocketAddress(mcAddress, mcPort),
                new InetSocketAddress(mdbAddress, mdbPort), new InetSocketAddress(mdrAddress, mdrPort));
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
    }

    public void start() {
        try {
            /*
            BackupService service = (BackupService) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(Integer.toString(localId), service);
            */
            MulticastSocket receiveSocketMc = new MulticastSocket(ChannelManager.getInstance().getMcAddress().getPort());
            MulticastSocket receiveSocketMdb = new MulticastSocket(ChannelManager.getInstance().getMdbAddress().getPort());
            MulticastSocket receiveSocketMdr = new MulticastSocket(ChannelManager.getInstance().getMdrAddress().getPort());
            receiveSocketMc.joinGroup(ChannelManager.getInstance().getMcAddress().getAddress());
            receiveSocketMdb.joinGroup(ChannelManager.getInstance().getMdbAddress().getAddress());
            receiveSocketMdr.joinGroup(ChannelManager.getInstance().getMdrAddress().getAddress());
            receiveSocketMc.setTimeToLive(1);
            receiveSocketMdr.setTimeToLive(1);
            receiveSocketMdb.setTimeToLive(1);

            mcListener = new MulticastListener(receiveSocketMc);
            mdbListener = new MulticastListener(receiveSocketMdb);
            mdrListener = new MulticastListener(receiveSocketMdr);
            mcListener.addSubProtocol(new PutChunkListener("" + localId));
            mcListener.addSubProtocol(new GetChunkListener("" + localId));
            mcListener.addSubProtocol(new RemovedListener("" + localId));
            mcListener.addSubProtocol(new DeleteListener("" + localId));

            new Thread(mcListener).start();
            /*new Thread(mdbListener).start();
            new Thread(mdrListener).start();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backup(String filepath, int replicationDeg) throws RemoteException {
        try {
            BackupInitiator bi = new BackupInitiator(filepath, "" + localId, replicationDeg, mcListener, mdbListener);
            bi.sendChunks();
            bi.storeMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restore(String filename) throws RemoteException {
        try {
            RestoreInitiator ri = new RestoreInitiator(filename, "" + localId, mcListener, mdrListener);
            ri.getChunks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String filename) throws RemoteException {
        try {
            DeleteInitiator di = new DeleteInitiator(filename, "" + localId);
            di.deleteFile();
            di.setMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reclaim(long space) throws RemoteException {

    }
}
