/**
 * Raul Barbosa 2014-11-07
 */
package WebInterface.model;

import java.util.ArrayList;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class HeyBean {
	private String username;	// username supplied by the user
	private String password;	// password supplied by the user

	public HeyBean() {
	}

	public ArrayList<String> getAllUsers() throws RemoteException {
		return null;
	}

	public boolean getUserMatchesPassword() throws RemoteException {
		return false;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
