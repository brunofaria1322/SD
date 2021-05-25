
package WebInterface.action;

import Commun.database;
import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private HashMap<String,String> election;
	private Integer electionId;
	private int estado;

	private List<String[]> results;
	private List<String[]> candidateList;
	private String listId;


	@Override
	public String execute() throws RemoteException {
		if(session.containsKey("loggedin") && (boolean)session.get("loggedin")) {
			if(session.containsKey("electionId") && session.get("electionId")!=null){
				System.out.println((String) session.get("username"));
				System.out.println((Integer) session.get("electionId"));
				System.out.println(Integer.parseInt(listId));
				getWebServer().vote((String) session.get("username"), (Integer) session.get("electionId"), Integer.parseInt(listId), 0);
				return SUCCESS;
			}
			return LOGIN;
		}
		else{
			return LOGIN;
		}
	}

	public String display() throws RemoteException {

		HashMap<Integer, HashMap<String, String>> elecs;
		elecs = getWebServer().getElections(null,null);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
		HashMap<Integer, HashMap<String,String>> electionsList = new HashMap<>();
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


		election = electionsList.get(electionId);

		results = new ArrayList<>();
		candidateList = new ArrayList<>();
		session.put("electionId", electionId);

		try {

			//Election hasn't started yet
			if(LocalDateTime.parse(election.get("inicio"), formatter).isAfter(LocalDateTime.now())){
				estado = 1;
			}
			//Election is currently active
			else if(LocalDateTime.parse(election.get("fim"), formatter).isAfter(LocalDateTime.now())){
				estado = 2;
				HashMap<Integer, database.Pair<String, ArrayList<database.Pair<String, String>>>> lists = getWebServer().getLists(electionId);
				for(Integer key : lists.keySet()){
					candidateList.add (new String[] {key.toString(), lists.get(key).left});
				}
			}
			//Election has finished
			else{
				estado = 3;

				//TODO: get and treat results!!
				//results = {election_id: (election_name, {list_id: (list_name, number_of_votes)})}
				HashMap<Integer, database.Pair<String, HashMap<Integer, database.Pair<String, Integer>>>> lists;
				lists = getWebServer().getResults(electionId);
				for(Integer key : lists.get(electionId).right.keySet()){
					results.add(new String[]{lists.get(electionId).right.get(key).left,lists.get(electionId).right.get(key).right.toString()});
				}

			}

		}catch (Exception e){
			estado = -1;
		}

		return NONE;
	}


	public Integer getElectionId() { return electionId;	}
	public void setElectionId(Integer electionId) { this.electionId = electionId; }

	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}

	public HashMap<String, String> getElection() { return election; }
	public void setElection(HashMap<String, String> election) { this.election = election; }

	public int getEstado() { return estado; }
	public void setEstado(int estado) {	this.estado = estado; }

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	public List<String[]> getCandidateList() { return candidateList; }
	public void setCandidateList(List<String[]> candidateList) { this.candidateList = candidateList; }

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
