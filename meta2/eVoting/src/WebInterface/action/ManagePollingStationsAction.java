
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManagePollingStationsAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private List<String[]> results;
	private List<String> departments;

	@Override
	public String execute() {

		/* TODO: aqui!!!
		if(!session.containsKey("loggedin") || !((Boolean) session.get("loggedin"))) {
			return LOGIN;
		}

		if(!session.containsKey("admin") || !((Boolean) session.get("admin"))) {
			return LOGIN;
		}
		*/

		if(!session.containsKey("electionId")) {
			return LOGIN;
		}

		//election

		Integer electionId = (Integer) session.get("electionId");

		results = new ArrayList<>();

		//TODO: get and treat results!!
		//results = {station_id, DepName }
		results.add( new String[] {"2","Dep EI"});
		results.add( new String[] {"6", "EC FCTUC"});
		results.add( new String[] {"69", "Casa do Caralho"});

		//departments
		departments = new ArrayList<String>();
		//TODO: get lista de departamentos que podem ter mas não têm mesas de voto!
		departments.add("DEEC");
		departments.add("DEC");
		departments.add("Dep das belas artes");

		return SUCCESS;
	}

	public String addPollingStation() {

		//TODO: addPollingStation;
		return SUCCESS;
	}

	public String removePollingStation() {

		//TODO: removePollingStation;
		return SUCCESS;
	}

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	public void setDepartments(List<String> departments) { this.departments = departments; }
	public List<String> getDepartments() { return departments; }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
