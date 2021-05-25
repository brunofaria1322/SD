
package WebInterface.action;

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

public class ChangeElectionPropertiesAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	private String votersType = null, title = null, description = null;
	private int ndep = -1;
	private String starting_datetime = null, ending_datetime = null;

	private List<String> votersTypes;
	private List<String> departments;

	public String display() throws RemoteException {

		//types
		votersTypes = new ArrayList<>();
		votersTypes.add("Students");
		votersTypes.add("Professors");
		votersTypes.add("Employees");
		votersTypes.add("Everyone");

		//departments
		HashMap<Integer, String> deps = getWebServer().getDepartments();
		departments = new ArrayList<String>();
		for(Integer dep : deps.keySet()){
			departments.add(deps.get(dep));
		}

		departments.add("Every Departments");

		//elections
		HashMap<Integer, HashMap<String,String>> electionsList = new HashMap<>();
		HashMap<Integer, HashMap<String, String>> elecs = getWebServer().getElections(null,null);


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


		if(!session.containsKey("electionId")) {
			return LOGIN;
		}
		Integer electionId = (Integer) session.get("electionId");
		HashMap<String,String> election = electionsList.get(electionId);
		title = election.get("titulo");
		description = election.get("descricao");
		starting_datetime = election.get("inicio").replace(' ', 'T');
		int i = starting_datetime.lastIndexOf(':');
		starting_datetime =  starting_datetime.substring(0, i);

		ending_datetime = election.get("fim").replace(' ', 'T');
		i = ending_datetime.lastIndexOf(':');
		ending_datetime =  ending_datetime.substring(0, i);

		return NONE;
	}

	@Override
	public String execute() throws RemoteException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

		LocalDateTime start = LocalDateTime.parse(starting_datetime, formatter);
		LocalDateTime end = LocalDateTime.parse(ending_datetime, formatter);

		if(start.isBefore(LocalDateTime.now()) || start.isAfter(end)){
			session.put("error","Invalid starting time");
			return NONE;
		}
		if(end.isBefore(LocalDateTime.now())){
			session.put("error","Invalid ending time");
			return NONE;
		}
		if(getWebServer().editElection((Integer)session.get("electionId"),false,title,description,start,end,null,null,null)>0) {
			return SUCCESS;
		}

		session.put("error","Unsuccessful");
		return NONE;
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


	public void setVotersType(String votersType){
		this.votersType = votersType;
	}
	public String getPersonType(){
		return votersType;
	}

	public void setVotersTypes(List<String> votersTypes) { this.votersTypes = votersTypes; }
	public List<String> getVotersTypes() { return votersTypes; }


	public void setNdep(int ndep){
		this.ndep = ndep;
	}
	public int getNdep(){
		return ndep;
	}

	public void setDepartments(List<String> departments) { this.departments = departments; }
	public List<String> getDepartments() { return departments; }


	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getStarting_datetime() { return starting_datetime; }
	public void setStarting_datetime(String starting_datetime) { this.starting_datetime = starting_datetime.replace('T', ' '); }
	public String getEnding_datetime() { return ending_datetime; }
	public void setEnding_datetime(String ending_datetime) { this.ending_datetime = ending_datetime.replace('T', ' '); }
}
