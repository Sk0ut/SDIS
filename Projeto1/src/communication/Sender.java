package communication;

import communication.message.DeleteMessage;

import java.io.IOException;

/**
 * Created by afonso on 28-03-2016.
 */
public class Sender implements Runnable {
    Peer peer;

    public Sender(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        Message message = new DeleteMessage(Integer.toString(Peer.senderId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5");

        while(true) {
            try {
                Thread.sleep(1000);
                peer.send(message.getBytes());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
