
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

public class ManageCandidateListsAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	private String candidateName;
	private String candidateId;
	private String listName;
	private String listId;
	private List<String[]> results;

	private Map<String, List<String[]>> candidates;

	@Override
	public String execute() throws RemoteException {

		if(!session.containsKey("admin") || !(boolean) session.get("admin")){
			return LOGIN;
		}

		if(!session.containsKey("electionId")) {
			return LOGIN;
		}

		//election

		Integer electionId = (Integer) session.get("electionId");

		results = new ArrayList<>();
		HashMap<Integer, database.Pair<String, ArrayList<database.Pair<String, String>>>> lists;
		lists = getWebServer().getLists(electionId);

		//results = {list_id, ListName, }
		candidates = new HashMap<>();
		for(Integer key : lists.keySet()){
			results.add(new String[]{key.toString(),lists.get(key).left});
			List<String[]> temp = new ArrayList<>();
			for(database.Pair<String, String> candidato: lists.get(key).right){
				temp.add(new String[] {candidato.left,candidato.right});
			}
			candidates.put(key.toString(),temp);
		}

		return SUCCESS;
	}

	public String addCandidateList() throws RemoteException {
		int res = getWebServer().createOrEditList((Integer) session.get("electionId"),listName,null,false);
		if(res >0){
			return SUCCESS;
		}
		else if(res == -3){
			session.put("error","There's already a list with that name.");
			return "none";
		}
		else {
			session.put("error","List could not be added.");
			return "none";
		}
	}

	public String removeCandidateList() throws RemoteException {
		Integer[] list = {new Integer(listId)};
		int res = getWebServer().editElection((Integer)session.get("electionId"), true, null, null, null, null, null, null,list );
		if(res >0){
			return SUCCESS;
		}
		else {
			session.put("error","List could not be removed.");
			return "none";
		}
	}

	public String addCandidate() throws RemoteException {
		ArrayList<database.Pair<String, String>> users = new ArrayList<>();
		users.add(new database.Pair<String,String>(candidateId,candidateName));
		int res = getWebServer().createOrEditList((Integer) session.get("electionId"),listName,users,false);
		if(res >0){
			return SUCCESS;
		}
		else if(res == -2){
			session.put("error","That user cannot be a member of that list.");
			return "none";
		}
		else {
			session.put("error","User could not be added.");
			return "none";
		}
	}

	public String removeCandidate() throws RemoteException {


		ArrayList<database.Pair<String, String>> users = new ArrayList<>();
		users.add(new database.Pair<String,String>(candidateId,candidateName));
		int res = getWebServer().createOrEditList((Integer) session.get("electionId"),listName,users,true);
		if(res >0){
			return SUCCESS;
		}
		else {
			System.out.println(res);
			session.put("error","User could not be removed.");
			return "none";
		}
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

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	public Map<String, List<String[]>> getCandidates() { return candidates; }
	public void setCandidates(Map<String, List<String[]>> candidates) { this.candidates = candidates; }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public String getCandidateName() {
		return candidateName;
	}

	public void setCandidateName(String candidateName) {
		this.candidateName = candidateName;
	}

	public String getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(String candidateId) {
		this.candidateId = candidateId;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}
}
