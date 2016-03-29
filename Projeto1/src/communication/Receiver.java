package communication;

import general.MalformedMessageException;

import java.io.IOException;

/**
 * Created by afonso on 28-03-2016.
 */
public class Receiver implements Runnable{
    Peer peer;

    public Receiver(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        while(true) {
            try {
                peer.receiveAndDispatch();
            } catch (IOException | MalformedMessageException e) {
                e.printStackTrace();
            }
        }
    }
}
