package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Flávio on 30/03/2016.
 */
public interface BackupService extends Remote {
    void backup(String filename) throws RemoteException;
    void restore(String filename) throws RemoteException;
    void delete(String filename) throws RemoteException;
    void reclaim(long space) throws RemoteException;
}
