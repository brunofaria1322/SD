
package WebInterface.action;

import Commun.database;
import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckUsersVotingHistory extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private String username = null;
	private List<String[]> results = null;

	@Override
	public String execute() throws RemoteException {
		if(!session.containsKey("admin") || !(boolean) session.get("admin")){
			return LOGIN;
		}
		HashMap<Integer,database.Pair<Integer,String>> votes = getWebServer().getUserVotes(username);
		HashMap<Integer,String> deps = getWebServer().getDepartments();
		HashMap<Integer,HashMap<String,String>> elecs = getWebServer().getElections(username, null);
		results = new ArrayList<>();
		try{
			for(Integer key: votes.keySet()){
				if(key!=0) {
					results.add(new String[]{elecs.get(key).get("titulo"), deps.get(votes.get(key).left), votes.get(key).right});
				}
				else {
					results.add(new String[]{elecs.get(key).get("titulo"), "eVote", votes.get(key).right});
				}
			}
		}
		catch( java.lang.NullPointerException e){
			return "none";
		}

		return SUCCESS;
	}


	public String getUsername() { return username;	}
	public void setUsername(String username) { this.username = username; }

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public WebServer getWebServer() throws RemoteException {
		if(!session.containsKey("WebServer")) {
			this.setWebServer(new WebServer());
		}
		return (WebServer) session.get("WebServer");
	}

	public void setWebServer(WebServer WebServer) {
		WebServer.connect();
		this.session.put("WebServer", WebServer);
	}
}
