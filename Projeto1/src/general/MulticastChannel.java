package general;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

public class MulticastChannel extends Observable implements Runnable {

    private final int port;
    private final InetAddress ip;
    private MulticastSocket socket;

    public MulticastChannel(String address, int port) throws IOException {
        this.port = port;
        socket = new MulticastSocket(port);
        this.ip = InetAddress.getByName(address);
        socket.setTimeToLive(1);
        socket.joinGroup(ip);
    }

    @Override
    public void run() {
        byte[] buf = new byte[65000];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        while(true){
            try {
                socket.receive(receivePacket);
                byte[] message = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                setChanged();
                notifyObservers(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void send(byte[] sendbuf) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(sendbuf, sendbuf.length, ip, port);
        socket.send(sendPacket);
    }
}
