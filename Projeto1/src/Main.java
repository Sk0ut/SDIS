import communication.Peer;
import communication.Receiver;
import communication.Sender;

import java.io.IOException;

/**
 * Created by afonso on 28-03-2016.
 */
public class Main {
    /* No need to use args for now, just put the args here and we change this later */
    public final static String INET_ADDR = "224.0.0.3";
    public final static int PORT = 8888;
    public final static int SENDERID = 2;

    public static void main(String[] args) throws IOException {
        Peer peer = new Peer(INET_ADDR, PORT, SENDERID);
        new Thread(new Sender(peer)).start();
        new Thread(new Receiver(peer)).start();
    }
}
