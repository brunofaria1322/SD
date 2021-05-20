package Commun;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Web extends Remote {
    public void change() throws RemoteException;
}