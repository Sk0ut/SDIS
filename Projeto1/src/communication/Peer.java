package communication;

import general.MulticastListener;
import subprotocol.DeleteListener;
import subprotocol.GetChunkListener;
import subprotocol.PutChunkListener;
import subprotocol.RemovedListener;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

/**
 * Created by afonso on 26-03-2016.
 */
public class Peer {

    private InetSocketAddress mcAddress;
    private InetSocketAddress mdbAddress;
    private InetSocketAddress mdrAddress;

    MulticastListener mcListener;
    MulticastListener mdbListener;
    MulticastListener mdrListener;
    public static int senderId;

    public Peer(int id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        this.mcAddress = new InetSocketAddress(mcAddress, mcPort);
        this.mdbAddress = new InetSocketAddress(mdbAddress, mdbPort);
        this.mdrAddress = new InetSocketAddress(mdrAddress, mdrPort);
        Peer.senderId = id;
        new File("peer"+ id).mkdir();
    }

    public void start() {
        try {
            MulticastSocket receiveSocketMc = new MulticastSocket(mcAddress.getPort());
            MulticastSocket receiveSocketMdb = new MulticastSocket(mdbAddress.getPort());
            MulticastSocket receiveSocketMdr = new MulticastSocket(mdrAddress.getPort());
            receiveSocketMc.joinGroup(mcAddress.getAddress());
            receiveSocketMdb.joinGroup(mdbAddress.getAddress());
            receiveSocketMdr.joinGroup(mdrAddress.getAddress());
            receiveSocketMc.setTimeToLive(1);
            receiveSocketMdr.setTimeToLive(1);
            receiveSocketMdb.setTimeToLive(1);

            mcListener = new MulticastListener(receiveSocketMc);
            mdbListener = new MulticastListener(receiveSocketMdb);
            mdrListener = new MulticastListener(receiveSocketMdr);
            mcListener.addSubProtocol(new PutChunkListener("" + senderId));
            mcListener.addSubProtocol(new GetChunkListener("" + senderId));
            mcListener.addSubProtocol(new RemovedListener("" + senderId));
            mcListener.addSubProtocol(new DeleteListener("" + senderId));

            new Thread(mcListener).start();
            /*new Thread(mdbListener).start();
            new Thread(mdrListener).start();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InetSocketAddress getMcAddress() {
        return mcAddress;
    }

    public InetSocketAddress getMdbAddress() {
        return mdbAddress;
    }

    public InetSocketAddress getMdrAddress() {
        return mdrAddress;
    }
}
