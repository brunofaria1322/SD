
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class ManageElectionAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private HashMap<String,String> election;
	private Integer electionId;

	@Override
	public String execute() {
		//elections
		election = new HashMap<>();
		//TODO: get eleição a partir do id;
		HashMap<String, String> temp = new HashMap<>();
		temp.put("title", "titulo fixe");
		temp.put("description", "dasc");
		temp.put("start_date", "start");
		temp.put("end_date", "end");
		temp.put("ids_of_the_departments", "all");
		temp.put("ids_of_the_voting_stations", "all");


		this.session.put("electionId", electionId);
		return SUCCESS;

	}


	public Integer getElectionId() { return electionId;	}
	public void setElectionId(Integer electionId) { this.electionId = electionId; }

	public HashMap<String, String> getElection() { return election; }
	public void setElection(HashMap<String, String> election) { this.election = election; }


	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
