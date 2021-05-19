/**
 * Raul Barbosa 2014-11-07
 */
package WebInterface.action;

import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.Map;

public class RegisterPersonAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	private String personType = null, username = null, password = null, name = null, address = null, phone = null, ncc = null;
	private int ndep = -1;
	private Date val_cc = null;
	@Override
	public String execute() throws RemoteException {
		int res = getWebServer().createUser(personType,ndep,name,address,phone,ncc,val_cc,username,password);
		if(res==1) {
			return LOGIN;
		}
		else if (res==-1){
			session.put("error","There is already an user with the given username in the database.");
			return ERROR;
		}
		else if (res==-2){
			session.put("error","There is already an user with the given ID number in the database.");
			return ERROR;
		}
		else{
			session.put("error","Something went wrong. Sorry...");
			return ERROR;
		}
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

	public void setPassword(String password) {
		this.password = password;
	}
	public void setUsername(String username){
		this.username = username;
	}
	public void setPersonType(String personType){
		this.personType = personType;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setAddress(String address){
		this.address = address;
	}
	public void setPhone(String phone){
		this.phone = phone;
	}
	public void setNcc(String ncc){
		this.ncc = ncc;
	}
	public void setNdep(int ndep){
		this.ndep = ndep;
	}
	public void setVal_cc(Date val_cc){
		this.val_cc = val_cc;
	}
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}