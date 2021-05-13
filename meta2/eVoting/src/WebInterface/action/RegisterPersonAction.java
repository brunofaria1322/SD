/**
 * Raul Barbosa 2014-11-07
 */
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;

import java.io.Serial;
import java.rmi.RemoteException;

public class RegisterPersonAction extends ActionSupport {
	@Serial
	private static final long serialVersionUID = 4L;

	@Override
	public String execute() throws RemoteException {

		return LOGIN;
	}
}
