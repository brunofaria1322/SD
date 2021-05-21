
package WebInterface.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class ChooseElectionAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;

	private HashMap<Integer, HashMap<String,String>> electionsList;

	public ChooseElectionAction(){

		//elections
		electionsList = new HashMap<>();
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
	}



	public String display() {
		return NONE;
	}

	public HashMap<Integer, HashMap<String,String>> getElectionsList(){ return electionsList;}
	public void setElectionsList(HashMap<Integer, HashMap<String,String>> electionsList){ this.electionsList = electionsList;}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
