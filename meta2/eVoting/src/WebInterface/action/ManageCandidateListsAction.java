
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageCandidateListsAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private List<String[]> results;

	private Map<String, List<String[]>> candidates;

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
		//results = {list_id, ListName, }
		results.add( new String[] {"2","Lista A"});
		results.add( new String[] {"6", "Lista B"});
		results.add( new String[] {"69", "Lista CONA"});


		candidates = new HashMap<>();
		//TODO: get and treat candidates!!
		List<String[]> temp = new ArrayList<>();
		temp.add(new String[] {"2","Ze droguinha"});
		temp.add(new String[] {"3","Ze Zé"});
		temp.add(new String[] {"4","Ze camelo"});
		candidates.put("2", temp);

		temp = new ArrayList<>();
		temp.add(new String[] {"5","Manél"});
		temp.add(new String[] {"6","Mano L"});
		candidates.put("6", temp);

		temp = new ArrayList<>();
		temp.add(new String[] {"7","FlóLó"});
		candidates.put("69", temp);


		return SUCCESS;
	}

	public String addCandidateList() {

		//TODO: addCandidateList;
		return SUCCESS;
	}

	public String removeCandidateList() {

		//TODO: removeCandidateList;
		return SUCCESS;
	}

	public String addCandidate() {

		//TODO: addCandidate;
		return SUCCESS;
	}

	public String removeCandidate() {

		//TODO: removeCandidate;
		return SUCCESS;
	}

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	public Map<String, List<String[]>> getCandidates() { return candidates; }
	public void setCandidates(Map<String, List<String[]>> candidates) { this.candidates = candidates; }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
