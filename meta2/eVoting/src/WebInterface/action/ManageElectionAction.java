
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

		//elections
		HashMap<Integer, HashMap<String,String>> electionsList = new HashMap<>();
		//TODO: get lista de eleições
		HashMap<String, String> temp = new HashMap<>();
		temp.put("titulo", "Por começar");
		temp.put("descricao", "esta eleiçai ainda nem começou....");
		temp.put("inicio", "2021-06-21 17:15:00");
		temp.put("fim", "2021-06-26 21:30:00");
		temp.put("departamentos", ";4;1;");
		temp.put("mesas", ";4;");

		electionsList.put(1, temp);

		temp = new HashMap<>();
		temp.put("titulo", "Ativa");
		temp.put("descricao", "esta eleição está ativa e a decorrer...");
		temp.put("inicio", "2021-05-20 17:15:00");
		temp.put("fim", "2021-06-23 21:30:00");
		temp.put("departamentos", ";4;1;");
		temp.put("mesas", ";4;");
		electionsList.put(3, temp);

		temp = new HashMap<>();
		temp.put("titulo", "Terminada");
		temp.put("descricao", "esta eleição está terminada...");
		temp.put("inicio", "2021-04-20 17:15:00");
		temp.put("fim", "2021-04-23 21:30:00");
		temp.put("departamentos", ";4;1;");
		temp.put("mesas", ";4;");
		electionsList.put(2, temp);

		//election
		//TODO: get eleição a partir do id;
		election = electionsList.get(electionId);

		results = new ArrayList<>();
		session.put("electionId", electionId);

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			//Election hasn't started yet
			if(LocalDateTime.parse(election.get("inicio"), formatter).isAfter(LocalDateTime.now())){
				estado = 1;
			}
			//Election is currently active
			else if(LocalDateTime.parse(election.get("fim"), formatter).isAfter(LocalDateTime.now())){
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
