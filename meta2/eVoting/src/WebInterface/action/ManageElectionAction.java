
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
	public String execute() throws RemoteException {
		if(!session.containsKey("admin") || !(boolean) session.get("admin")){
			return LOGIN;
		}

		if (electionId == null){
			if(!session.containsKey("electionId")){
				return LOGIN;
			}
			electionId = (Integer) session.get("electionId");
		}

		HashMap<Integer, HashMap<String,String>> electionsList;
		electionsList = new HashMap<>();
		HashMap<Integer, HashMap<String, String>> elecs = new HashMap<>();
		elecs = getWebServer().getElections(null,null);
		for(Integer key : elecs.keySet()){
			HashMap<String, String> temp = new HashMap<>();
			temp.put("titulo", elecs.get(key).get("titulo"));
			temp.put("descricao", elecs.get(key).get("descricao"));
			temp.put("inicio", elecs.get(key).get("inicio"));
			temp.put("fim",  elecs.get(key).get("fim"));
			temp.put("departamentos", elecs.get(key).get("departamentos"));
			temp.put("mesas", elecs.get(key).get("mesas"));
			electionsList.put(key, temp);
		}

		//election
		election = electionsList.get(electionId);

		results = new ArrayList<>();
		session.put("electionId", electionId);

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

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
				HashMap<Integer, database.Pair<String, HashMap<Integer, database.Pair<String, Integer>>>> lists;
				lists = getWebServer().getResults(electionId);
				for(Integer key : lists.get(electionId).right.keySet()){
					results.add(new String[]{lists.get(electionId).right.get(key).left,lists.get(electionId).right.get(key).right.toString()});
				}

			}
		}catch (Exception e){
			System.out.println(e);
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
