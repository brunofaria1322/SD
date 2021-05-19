
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.rmi.RemoteException;
import java.util.Map;
import WebInterface.model.WebServer;

public class LoginAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	private String username = null, password = null;

	@Override
	public String execute() throws RemoteException {
		try {
			if (this.username != null && !username.equals("") && this.password != null && !password.equals("")) {
				int login = this.getWebServer().login(username, password);
				if (login == 1) {
					session.put("username", username);
					session.put("loggedin", true); // this marks the user as logged in
					return "voter";
				}
				else if(login == 2){
					session.put("username", username);
					session.put("loggedin", true); // this marks the user as logged in
					session.put("admin",true);
					return "admin";
				}
			}
			//DEBUG
			return "admin";

			//return LOGIN;
		}
		catch (RemoteException e){
			if(!getWebServer().connect()){
				System.out.println(e);
				return ERROR;
			}
			return this.execute();
		}
	}
	
	public void setUsername(String username) {
		this.username = username; // will you sanitize this input? maybe use a prepared statement?
	}

	public void setPassword(String password) {
		this.password = password; // what about this input? 
	}
	
	public WebServer getWebServer() {
		if(!session.containsKey("WebServer"))
			this.setWebServer(new WebServer());
		return (WebServer) session.get("WebServer");
	}

	public void setWebServer(WebServer WebServer) {
		WebServer.connect();
		this.session.put("WebServer", WebServer);
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
