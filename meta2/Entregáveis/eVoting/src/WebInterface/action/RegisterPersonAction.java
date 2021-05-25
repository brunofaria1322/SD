
package WebInterface.action;

import WebInterface.model.WebServer;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.IOException;
import java.io.Serial;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegisterPersonAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	private String personType = null, username = null, password = null, name = null, address = null, phone = null, ncc = null;
	private String department = null;
	private Date val_cc = null;

	private List<String> personTypes;
	private List<String> departments;

	public RegisterPersonAction() throws IOException {

		//types
		personTypes = new ArrayList<>();
		personTypes.add("Student");
		personTypes.add("Professor");
		personTypes.add("Employee");

		//departments
		WebServer wb = new WebServer();
		wb.readConfig();
		wb.connect();
		departments = new ArrayList<>(wb.getDepartments().values());

	}


	public String display() {
		return NONE;
	}

	@Override
	public String execute() throws ParseException {
		if(!session.containsKey("admin") || !(boolean) session.get("admin")){
			return LOGIN;
		}
		int ndep = 0;
		for(String dep : departments){
			ndep++;
			if(department.replaceAll("[^a-zA-Z0-9]", "?").equals(dep.replaceAll("[^a-zA-Z0-9]", "?"))){
				break;
			}
		}

		try {
			int res = getWebServer().createUser(personType, ndep, name, address, phone, ncc,val_cc, username, password);
			if (res == 1) {
				return SUCCESS;
			} else if (res == -1) {
				session.put("error", "There is already an user with the given username in the database.");
				return "none";
			} else if (res == -2) {
				session.put("error", "There is already an user with the given ID number in the database.");
				return "none";
			} else {
				session.put("error", "Something went wrong. Sorry...");
				return "none";
			}
		}
		catch(Exception e){
			session.put("error","The server is down. Sorry...");
			return "none";
		}
	}

	public WebServer getWebServer() throws IOException {
		if(!session.containsKey("WebServer"))
			this.setWebServer(new WebServer());
		return (WebServer) session.get("WebServer");
	}

	public void setWebServer(WebServer WebServer) throws IOException {
		getWebServer().readConfig();
		getWebServer().connect();
		this.session.put("WebServer", WebServer);
	}

	public void setPersonType(String personType){
		if(personType.equals("Student")){
			this.personType = "aluno";
		}
		else if(personType.equals("Professor")){
			this.personType = "docente";
		}
		else{
			this.personType = "funcionario";
		}

	}
	public String getPersonType(){ return personType; }

	public void setPersonTypes(List<String> personTypes) { this.personTypes = personTypes; }
	public List<String> getPersonTypes() { return personTypes; }

	public void setDepartments(List<String> departments) { this.departments = departments; }
	public List<String> getDepartments() { return departments; }

	public void setPassword(String password) {
		this.password = password;
	}
	public void setUsername(String username){
		this.username = username;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setAddress(String address){
		this.address = address;
	}
	public void setPhone(String phone){
		this.phone = phone;
	}
	public void setNcc(String ncc){
		this.ncc = ncc;
	}
	public void setVal_cc(Date val_cc){
		this.val_cc = val_cc;
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
}
