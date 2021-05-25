package Commun;
import java.rmi.*;

public interface Admin extends Remote{
	public void change() throws RemoteException;
}