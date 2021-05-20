
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChooseElectionAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private List<String> electionsList;



	public ChooseElectionAction(){

		//elections
		electionsList = new ArrayList<>();
		//TODO: get lista de eleições
		electionsList.add("DEI");
	}



	public String display() {
		return NONE;
	}

	public List<String> getElectionsList(){ return electionsList;}
	public void setElectionsList(List<String> electionsList){ this.electionsList = electionsList;}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
