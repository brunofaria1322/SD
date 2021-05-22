
package WebInterface.action;

import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class VoterIndexAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private HashMap<Integer, HashMap<String,String>> electionsList;

	public VoterIndexAction() throws IOException {

		//elections
		WebServer wb = new WebServer();
		wb.readConfig();
		wb.connect();
		electionsList = wb.getElections((String) session.get("username"), null);
		//TODO: falta completar aaaaaquiiiii
	}



	public String display() {
		return NONE;
	}

	public HashMap<Integer, HashMap<String,String>> getElectionsList(){ return electionsList;}
	public void setElectionsList(HashMap<Integer, HashMap<String,String>> electionsList){ this.electionsList = electionsList;}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
