package client;

import communication.MessageParser;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Flávio on 30/03/2016.
 */
public class TestApp {

    private static final int MIN_ARGS = 3;
    private static final int MAX_ARGS = 4;

    private String peerAP;
    private BackupService service = null;

    public TestApp(String peerAP) {
        this.peerAP = peerAP;
    }

    public void connect() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        service = (BackupService) registry.lookup("rmi://localhost/" + peerAP);
    }

    public void execute(String subProtocol, String opnd1, String opnd2) throws RemoteException {
        switch (subProtocol) {
            case "BACKUP":
                if (opnd2 == null)
                    throw new IllegalArgumentException("BACKUP subprotocol has 2 operands");
                backup(opnd1, opnd2);
                break;
            case "RESTORE":
                if (opnd2 != null)
                    throw new IllegalArgumentException("RESTORE subprotocol only has 1 operand");
                restore(opnd1);
                break;
            case "DELETE":
                if (opnd2 != null)
                    throw new IllegalArgumentException("DELETE subprotocol only has 1 operand");
                delete(opnd1);
                break;
            case "RECLAIM":
                if (opnd2 != null)
                    throw new IllegalArgumentException("RECLAIM subprotocol only has 1 operand");
                reclaim(opnd1);
                break;
            default:
                throw new IllegalArgumentException("Unknown subprotocol: " + subProtocol);
        }
    }


    private void backup(String filePath, String replicationDegString) throws RemoteException {
        if (!MessageParser.validReplicationDeg(replicationDegString))
            throw new IllegalArgumentException("BACKUP replication degree must be a single digit integer");

        final int replicationDeg = Integer.parseInt(replicationDegString);

        service.backup(filePath, replicationDeg);
    }

    private void restore(String filename) throws RemoteException {
        service.restore(filename);
    }


    private void delete(String filename) throws RemoteException {
        service.delete(filename);
    }

    private void reclaim(String spaceString) throws RemoteException {
        if (!spaceString.matches("$\\d+$"))
            throw  new IllegalArgumentException("RECLAIM space must be an integer");

        final long space = Long.parseLong(spaceString);

        service.reclaim(space);
    }



    public static void main(String [] args) {
        if (! ((MIN_ARGS <= args.length) && (args.length <= MAX_ARGS))) {
            System.err.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }

        final String peerAp = args[0];
        final String subProtocol = args[1];
        final String opnd1 = args[2];
        final String opnd2 = args.length == MAX_ARGS ? args[3] : null;

        TestApp app = new TestApp(peerAp);
        try {
            app.connect();
            app.execute(subProtocol, opnd1, opnd2);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            return;
        }
    }
}
