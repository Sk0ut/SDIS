package general;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

public class MulticastChannelManager extends Observable implements Runnable {

    private MulticastSocket ms;

    public MulticastChannelManager(MulticastSocket ms) {
        this.ms = ms;
    }

    private List<SubProtocolListener> subProtocolListeners = new ArrayList<>();

    public void addSubProtocol(SubProtocolListener subProtocolListener){
        subProtocolListeners.add(subProtocolListener);
    }

    @Override
    public void run() {
        byte[] buf = new byte[255+64*1024];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        while(true){
            try {
                ms.receive(receivePacket);
                byte[] message = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                setChanged();
                notifyObservers(message);
                //dispatchMessage(message);
            } catch (IOException ignored) {}
        }
    }

    public void send(byte[] sendbuf) throws IOException {
        DatagramSocket sendSocket = new DatagramSocket();
        DatagramPacket sendPacket;
        sendPacket = new DatagramPacket(sendbuf, sendbuf.length, ms.getInetAddress(), ms.getPort());
        sendSocket.send(sendPacket);
        sendSocket.close();
    }
}
