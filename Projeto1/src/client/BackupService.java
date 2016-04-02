package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Flávio on 30/03/2016.
 */
public interface BackupService extends Remote {
    void backup(String filepath, int replicationDeg) throws RemoteException;
    void restore(String filepath) throws RemoteException;
    void delete(String filepath) throws RemoteException;
    void reclaim(long space) throws RemoteException;
}
