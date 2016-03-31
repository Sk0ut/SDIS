package communication;

import general.MetadataFile;
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
    MulticastListener mcListener;
    MulticastListener mdbListener;
    MulticastListener mdrListener;
    public static int senderId;

    public Peer(int id, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        ChannelManager.getInstance().init(new InetSocketAddress(mcAddress, mcPort),
                new InetSocketAddress(mdbAddress, mdbPort), new InetSocketAddress(mdrAddress, mdrPort));
        MetadataFile.getInstance().init("" + id);
        Peer.senderId = id;
        new File("peer"+ id).mkdir();
        File metadata = new File("peer"+id+"/.metadata");
        if(!metadata.exists())
            metadata.createNewFile();
    }

    public void start() {
        try {
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
}
