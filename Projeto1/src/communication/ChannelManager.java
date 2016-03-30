package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by afonso on 30-03-2016.
 */
public class ChannelManager {

    public enum ChannelType { CONTROLCHANNEL, DATABACKUPCHANNEL, DATARESTORECHANNEL }

    private InetSocketAddress mcAddress;
    private InetSocketAddress mdrAddress;
    private InetSocketAddress mdbAddress;
    private static ChannelManager instance = null;

    private ChannelManager() {}
    public static ChannelManager getInstance(){
        if (instance == null)
            ChannelManager.instance = new ChannelManager();
        return instance;
    }

    public void init(InetSocketAddress mcAddress, InetSocketAddress mdbAddress, InetSocketAddress mdrAddress){
        getInstance().mcAddress = mcAddress;
        getInstance().mdrAddress = mdrAddress;
        getInstance().mdbAddress = mdbAddress;
    }

    public InetSocketAddress getMcAddress() {
        return mcAddress;
    }

    public InetSocketAddress getMdrAddress() {
        return mdrAddress;
    }

    public InetSocketAddress getMdbAddress() {
        return mdbAddress;
    }

    public void send(ChannelType channelType, byte[] sendbuf) throws IOException {
        DatagramSocket sendSocket = new DatagramSocket();
        DatagramPacket sendPacket;
        switch (channelType) {
            case DATABACKUPCHANNEL:
                sendPacket = new DatagramPacket(sendbuf, sendbuf.length, mdbAddress);
                break;
            case DATARESTORECHANNEL:
                sendPacket = new DatagramPacket(sendbuf, sendbuf.length, mdrAddress);
                break;
            default:
                sendPacket = new DatagramPacket(sendbuf, sendbuf.length, mcAddress);
                break;
        }
        sendSocket.send(sendPacket);
        sendSocket.close();
    }
}
