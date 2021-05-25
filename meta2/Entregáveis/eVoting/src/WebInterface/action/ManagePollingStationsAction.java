
package WebInterface.action;

import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.rmi.RemoteException;
import java.util.*;

public class ManagePollingStationsAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private List<String[]> results;
	private List<String> departments;
	private String department;
	private String stationId;

	@Override
	public String execute() throws RemoteException {

		if(!session.containsKey("admin") || !((Boolean) session.get("admin"))) {
			return LOGIN;
		}


		if(!session.containsKey("electionId")) {
			return LOGIN;
		}

		//election

		Integer electionId = (Integer) session.get("electionId");

		results = new ArrayList<>();

		//results = {station_id, DepName }

		HashMap<Integer, HashMap<String, String>> elecs;
		HashMap<Integer, String> deps;
		elecs = getWebServer().getElections(null,null);
		deps = getWebServer().getDepartments();
		String mesas = elecs.get(electionId).get("mesas");
		List<String> amesas;
		amesas = new ArrayList<>(Arrays.asList(mesas.split(";")));
		if(amesas.contains("0")){
			for(Integer key: deps.keySet()){
				results.add( new String[] {key.toString(),deps.get(key)});
			}
		}
		else{
			for (String mesa : amesas){
				results.add( new String[] {mesa,deps.get(mesa)});
			}
		}

		//departments
		departments = new ArrayList<String>();
		if(!amesas.contains("0")) {
			for (Integer dep : deps.keySet()) {
				System.out.println(dep + ", " + amesas);
				if (!amesas.contains(dep.toString())) {
					departments.add(deps.get(dep));
				}
			}
		}

		return SUCCESS;
	}

	public String addPollingStation() throws RemoteException {
		HashMap<Integer, String> deps = getWebServer().getDepartments();
		Integer depid = 0;
		for(Integer dep : deps.keySet()){
			if(deps.get(dep).replaceAll("[^a-zA-Z0-9]", "?").equals(department.replaceAll("[^a-zA-Z0-9]", "?"))){
				depid = dep;
			}
		}
		getWebServer().editElection((Integer) session.get("electionId"), false, null, null, null, null, null, depid+";", null);
		return SUCCESS;
	}

	public String removePollingStation() throws RemoteException {

		getWebServer().editElection((Integer) session.get("electionId"), true, null, null, null, null, null, stationId+";", null);
		//TODO: removePollingStation;
		return SUCCESS;
	}

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	public void setDepartments(List<String> departments) { this.departments = departments; }
	public List<String> getDepartments() { return departments; }

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

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
}
