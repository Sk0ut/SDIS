import communication.Peer;

import java.io.IOException;

/**
 * Created by afonso on 28-03-2016.
 */
public class Main {
    /* No need to use args for now, just put the args here and we change this later */
    public final static String INET_ADDR_MC = "224.0.0.3";
    public final static int PORT_MC = 8891;
    public final static String INET_ADDR_MDR = "224.0.0.4";
    public final static int PORT_MDR = 8889;
    public final static String INET_ADDR_MDB = "224.0.0.5";
    public final static int PORT_MDB = 8890;
    public final static int SENDERID = 1;

    public static void main(String[] args) throws IOException {
        Peer peer = new Peer(SENDERID, INET_ADDR_MC, PORT_MC, INET_ADDR_MDB, PORT_MDB, INET_ADDR_MDR, PORT_MDR);
        peer.start();
    }
}
