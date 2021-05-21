
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
	private LocalDateTime starting_datetime = null, ending_datetime = null;

	private List<String> votersTypes;
	private List<String> departments;

	public String display() {

		//types
		votersTypes = new ArrayList<>();
		votersTypes.add("Students");
		votersTypes.add("Professors");
		votersTypes.add("Employees");
		votersTypes.add("Everyone");

		//departments
		departments = new ArrayList<String>();
		//TODO: get lista de departamentos
		departments.add("Every Departments");

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
		if(!session.containsKey("electionId")) {
			return LOGIN;
		}
		Integer electionId = (Integer) session.get("electionId");
		HashMap<String,String> election = electionsList.get(electionId);
		title = election.get("titulo");
		description = election.get("descricao");
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			starting_datetime = LocalDateTime.parse(election.get("inicio"), formatter);
			ending_datetime = LocalDateTime.parse(election.get("fim"), formatter);
		} catch (Exception e) {
			return ERROR;
		}
		return NONE;
	}

	@Override
	public String execute() throws RemoteException {

		//TODO: alterar as propiedades;
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
	public LocalDateTime getStarting_datetime() { return starting_datetime; }
	public void setStarting_datetime(LocalDateTime starting_datetime) { this.starting_datetime = starting_datetime; }
	public LocalDateTime getEnding_datetime() { return ending_datetime; }
	public void setEnding_datetime(LocalDateTime ending_datetime) { this.ending_datetime = ending_datetime; }
}
