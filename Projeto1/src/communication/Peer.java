package communication;

import general.MalformedMessageException;
import general.SubProtocolManager;
import subprotocols.Delete;
import subprotocols.Getchunk;
import subprotocols.Putchunk;
import subprotocols.Remove;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by afonso on 26-03-2016.
 */
public class Peer {

    InetAddress address;
    SubProtocolManager spm;
    int port;
    public static int senderId;
    MulticastSocket receiveSocket;

    public Peer(String inetaddress, int port, int senderId) throws IOException {
        address = InetAddress.getByName(inetaddress);
        this.port = port;
        Peer.senderId = senderId;
        spm = new SubProtocolManager();
        SubProtocolManager.addSubProtocol(new Putchunk());
        SubProtocolManager.addSubProtocol(new Getchunk());
        SubProtocolManager.addSubProtocol(new Remove());
        SubProtocolManager.addSubProtocol(new Delete());
        receiveSocket = new MulticastSocket(port);
        receiveSocket.setTimeToLive(1);
        receiveSocket.joinGroup(address);
    }

    public void receiveAndDispatch() throws IOException, MalformedMessageException {
        byte[] buf = new byte[255];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        receiveSocket.receive(receivePacket);
        String receivedMessage = new String(receivePacket.getData());
        receivedMessage = receivedMessage.replaceAll("\u0000", "");
        spm.dispatchMessage(receivedMessage);
    }

    public void send(byte [] sendbuf) throws IOException {
        DatagramSocket sendSocket = new DatagramSocket();
        DatagramPacket sendPacket = new DatagramPacket(sendbuf, sendbuf.length, address, port);
        sendSocket.send(sendPacket);
        sendSocket.close();
    }
}
