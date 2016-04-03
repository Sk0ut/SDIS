package client;

import communication.MessageParser;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
        service = (BackupService) registry.lookup("" + peerAP);
    }

    public String execute(String subProtocol, String opnd1, String opnd2) throws RemoteException {
        switch (subProtocol) {
            case "BACKUP":
                if (opnd2 == null)
                    return "BACKUP subprotocol has 2 operands";
                return backup(opnd1, opnd2);
            case "BACKUPENH":
                if (opnd2 == null)
                    return "BACKUPENH subprotocol has 2 operands";
                return backupEnh(opnd1, opnd2);
            case "RESTORE":
                if (opnd2 != null)
                    return "RESTORE subprotocol only has 1 operand";
                return restore(opnd1);
            case "RESTOREENH":
                if (opnd2 != null)
                    return "RESTOREENH subprotocol only has 1 operand";
                return restoreEnh(opnd1);
            case "DELETE":
                if (opnd2 != null)
                    return "DELETE subprotocol only has 1 operand";
                return delete(opnd1);
            case "DELETEENH":
                if (opnd2 != null)
                    return "DELETEENH subprotocol only has 1 operand";
                return deleteEnh(opnd1);
            case "RECLAIM":
                if (opnd2 != null)
                    return "RECLAIM subprotocol only has 1 operand";
                return reclaim(opnd1);
            case "RECLAIMENH":
                if (opnd2 != null)
                    return "RECLAIMENH subprotocol only has 1 operand";
                return reclaimEnh(opnd1);
        }
        return "Unknown subprotocol: " + subProtocol;
    }


    private String backup(String filePath, String replicationDegString) throws RemoteException {
        if (!MessageParser.validReplicationDeg(replicationDegString))
            return "BACKUP replication degree must be a single digit integer";

        final int replicationDeg = Integer.parseInt(replicationDegString);

        return service.backup(filePath, replicationDeg);
    }
    private String backupEnh(String filePath, String replicationDegString) throws RemoteException {
        if (!MessageParser.validReplicationDeg(replicationDegString))
            return "BACKUPENH replication degree must be a single digit integer";

        final int replicationDeg = Integer.parseInt(replicationDegString);

        return service.backupEnh(filePath, replicationDeg);
    }

    private String restore(String filename) throws RemoteException {
        return service.restore(filename);
    }
    private String restoreEnh(String filename) throws RemoteException {
        return service.restoreEnh(filename);
    }


    private String delete(String filename) throws RemoteException {
        return service.delete(filename);
    }
    private String deleteEnh(String filename) throws RemoteException {
        return service.deleteEnh(filename);
    }

    private String reclaim(String spaceString) throws RemoteException {
        if (!spaceString.matches("^\\d+$"))
            return "RECLAIM space must be an integer";

        final long space = Long.parseLong(spaceString);

        return service.reclaim(space);
    }
    private String reclaimEnh(String spaceString) throws RemoteException {
        if (!spaceString.matches("^\\d+$"))
            return "RECLAIM space must be an integer";

        final long space = Long.parseLong(spaceString);

        return service.reclaimEnh(space);
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
            System.out.println(app.execute(subProtocol, opnd1, opnd2));
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Failed to connect to peer " + peerAp);
        }
    }
}
