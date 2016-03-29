package communication;

import communication.message.PutChunkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * Created by afonso on 28-03-2016.
 */
public class Sender implements Runnable {
    SocketAddress address;

    public Sender(SocketAddress address) {
        this.address = address;
    }

    @Override
    public void run() {
        //Message message = new DeleteMessage(Integer.toString(Peer.senderId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5");
        //Message message2 = new RemovedMessage(Integer.toString(Peer.senderId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5", "0");
        //Message message3 = new GetChunkMessage(Integer.toString(Peer.senderId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5", "0");
        Message message4 = new PutChunkMessage(Integer.toString(Peer.senderId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5", "0", "3", "1234567893453453534534".getBytes());

        //while(true) {
            try {
                Thread.sleep(1000);
                send(message4.getBytes());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        //}
    }

    public void send(byte[] sendbuf) throws IOException {
        DatagramSocket sendSocket = new DatagramSocket();
        DatagramPacket sendPacket = new DatagramPacket(sendbuf, sendbuf.length, address);
        sendSocket.send(sendPacket);
        sendSocket.close();
    }
}
