
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageElectionAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private HashMap<String,String> election;
	private Integer electionId;
	private int estado;
	//private HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> results;
	private List<String[]> results;

	@Override
	public String execute() {

		//election
		//TODO: get eleição a partir do id;
		election = new HashMap<>();
		election.put("title", "titulo bué fixe");
		election.put("description", "dasc desc disc dosc dusc");
		election.put("start_date", "2020-05-21 17:15:00");
		election.put("end_date", "2020-05-26 21:30:00");
		election.put("ids_of_the_departments", ";4;1;");
		election.put("ids_of_the_voting_stations", ";4;");

		results = new ArrayList<>();

		this.session.put("electionId", electionId);

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			//Election hasn't started yet
			if(LocalDateTime.parse(election.get("start_date"), formatter).isAfter(LocalDateTime.now())){
				estado = 1;
			}
			//Election is currently active
			else if(LocalDateTime.parse(election.get("end_date"), formatter).isAfter(LocalDateTime.now())){
				estado = 2;
			}
			//Election has finished
			else{
				estado = 3;

				//TODO: get and treat results!!
				//results = {election_id: (election_name, {list_id: (list_name, number_of_votes)})}
				results.add (new String[] {"Lista A", "12"});
				results.add( new String[] {"Lista B", "6"});
				results.add( new String[] {"Lista CONA", "69"});
				results.add( new String[] {"Brancos", "420"});
				results.add( new String[] {"Pretos", "666"});

			}

		}catch (Exception e){
			estado = -1;
		}

		return SUCCESS;
	}


	public Integer getElectionId() { return electionId;	}
	public void setElectionId(Integer electionId) { this.electionId = electionId; }

	public HashMap<String, String> getElection() { return election; }
	public void setElection(HashMap<String, String> election) { this.election = election; }

	public int getEstado() { return estado; }
	public void setEstado(int estado) {	this.estado = estado; }

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
