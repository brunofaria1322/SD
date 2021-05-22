
package WebInterface.action;

import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.IOException;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class VoterIndexAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private HashMap<Integer, HashMap<String,String>> electionsList;

	public VoterIndexAction() throws IOException {

		//elections
		electionsList = new HashMap<>();
		WebServer wb = new WebServer();
		wb.readConfig();
		wb.connect();
		HashMap<Integer, HashMap<String, String>> elecs;
		elecs = wb.getElections(null,null);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
		for(Integer key : elecs.keySet()){

				HashMap<String, String> temp = new HashMap<>();
				temp.put("titulo", elecs.get(key).get("titulo"));
				temp.put("descricao", elecs.get(key).get("descricao"));
				temp.put("inicio", elecs.get(key).get("inicio"));
				temp.put("fim", elecs.get(key).get("fim"));
				temp.put("departamentos", elecs.get(key).get("departamentos"));
				temp.put("mesas", elecs.get(key).get("mesas"));
				electionsList.put(key, temp);

		}
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
