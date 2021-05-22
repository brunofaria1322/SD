
package WebInterface.action;

import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import javax.ejb.Local;
import java.io.IOException;
import java.io.Serial;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateElectionAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	private String votersType = null, title = null, description = null, department = null;
	private int ndep = -1;
	private LocalDateTime starting_datetime = null, ending_datetime = null;

	private List<String> votersTypes;
	private List<String> departments;


	public CreateElectionAction() throws IOException {

		//types
		votersTypes = new ArrayList<>();
		votersTypes.add("Students");
		votersTypes.add("Professors");
		votersTypes.add("Employees");
		votersTypes.add("Everyone");

		//departments
		WebServer wb = new WebServer();
		wb.readConfig();
		wb.connect();
		departments = new ArrayList<String>(wb.getDepartments().values());
		departments.add(0,"All Departments");
	}



	public String display() {
		return NONE;
	}

	@Override
	public String execute() throws RemoteException {
		try {
			if(!session.containsKey("admin") || !(boolean) session.get("admin")){
				return LOGIN;
			}
			if(starting_datetime.isBefore(LocalDateTime.now()) || starting_datetime.isAfter(ending_datetime)){
				session.put("error","Invalid starting time");
				return "none";
			}
			if(ending_datetime.isBefore(LocalDateTime.now())){
				session.put("error","Invalid ending time");
				return "none";
			}
			for (String dep : departments) {
				ndep++;
				if (department.replaceAll("[^a-zA-Z0-9]", "?").equals(dep.replaceAll("[^a-zA-Z0-9]", "?"))) {
					break;
				}
			}
			String dep = ";" + ndep + ";";
			int res = getWebServer().createElection(votersType, dep, dep, title, description, starting_datetime, ending_datetime);
			if (res == -1) {
				session.put("error", "Added duplicated election.");
			}
			return  SUCCESS;
		}
		catch (Exception e){
			System.out.println(e);
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


	public void setVotersType(String votersType){
		if(votersType.equals("Students")){
			this.votersType = "aluno";
		}
		else if(votersType.equals("Professors")){
			this.votersType = "docente";
		}
		else if(votersType.equals("Employees")){
			this.votersType = "funcionario";
		}
		else{
			this.votersType = "all";
		}
	}
	public String getPersonType(){
		return votersType;
	}

	public void setDepartment(String department){
		this.department = department;
	}
	public String getDepartment(){
		return department;
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
