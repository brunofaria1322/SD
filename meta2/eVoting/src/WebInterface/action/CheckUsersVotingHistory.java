
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckUsersVotingHistory extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private String username = null;
	private List<String[]> results = null;

	@Override
	public String execute() {

		if (username != null){


			//TODO: get and treat results from username!!;
			results = new ArrayList<>();
			//[ [title, dep, dateTime], ... ]
			results.add(new String[] {"elec 1 title", "dep de EEC", "2020-05-21 17:15:00"});
			results.add(new String[] {"elec 3 title", "dep de EI", "2020-04-01 15:52:00"});
			results.add(new String[] {"elec 2 title", "dep de Math", "2020-11-19 09:40:00"});
		}

		return SUCCESS;
	}


	public String getUsername() { return username;	}
	public void setUsername(String username) { this.username = username; }

	public List<String[]> getResults() { return results; }
	public void setResults(List<String[]> results) { this.results = results; }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
