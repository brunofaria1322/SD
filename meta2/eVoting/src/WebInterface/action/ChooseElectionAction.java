
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class ChooseElectionAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private HashMap<Integer, HashMap<String,String>> electionsList;

	public ChooseElectionAction(){

		//elections
		electionsList = new HashMap<>();
		//TODO: get lista de eleições
		HashMap<String, String> temp = new HashMap<>();
		temp.put("title", "titulo fixe");
		temp.put("description", "dasc");
		temp.put("start_date", "start");
		temp.put("end_date", "end");
		temp.put("ids_of_the_departments", "all");
		temp.put("ids_of_the_voting_stations", "all");

		electionsList.put(1, temp);
		electionsList.put(3, temp);
		electionsList.put(2, temp);
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