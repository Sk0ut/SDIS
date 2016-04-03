package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BackupService extends Remote {
    String backup(String filepath, int replicationDeg) throws RemoteException;
    String restore(String filepath) throws RemoteException;
    String delete(String filepath) throws RemoteException;
    String reclaim(long space) throws RemoteException;


    String backupEnh(String filepath, int replicationDeg) throws RemoteException;
    String restoreEnh(String filepath) throws RemoteException;
    String deleteEnh(String filepath) throws RemoteException;
    String reclaimEnh(long space) throws RemoteException;
}
