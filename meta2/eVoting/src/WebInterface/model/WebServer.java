/**
 * Raul Barbosa 2014-11-07
 */
package WebInterface.model;

import Commun.Web;
import Commun.database;
import com.google.gson.Gson;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/ws")
public class WebServer extends UnicastRemoteObject implements Web {
	private static String RMI_PORT;
	private static String RMI_ADDRESS;
	static database db;
	private HashMap<String, database.Pair<Integer,Integer>> stationsStatus;
	private HashMap<String,HashMap<String,Integer>> votesPerStation;
	private static final Set<WebServer> users = new CopyOnWriteArraySet<>();
	private Session wsSession;

	public WebServer() throws RemoteException{
		super();
	}

	public void readConfig() throws FileNotFoundException, IOException {
		Properties prop = new Properties();

		InputStream is = new FileInputStream("config/Admin.config");
		prop.load(is);

		RMI_ADDRESS = prop.getProperty("Admin.RMI_ADDRESS");
		RMI_PORT = prop.getProperty("Admin.RMI_PORT");
	}

	public synchronized boolean connect(){
		long until = System.currentTimeMillis()+30000;
		boolean ok = false;
		while(System.currentTimeMillis() <= until){
			try {
				db = (database) Naming.lookup(RMI_ADDRESS);
				if(db.isWorking()){
					ok = true;
					break;
				}
			} catch (Exception e) {
				//TODO
			}
		}
		return ok;
	}

	public void callback() throws RemoteException {
		WebServer wb = new WebServer();
		db.setWeb(wb);
	}

	public int login(String username, String facebook_id, String password) throws RemoteException {
		return db.login(username,facebook_id,null,password);
	}
	public int createUser(String cargo, int ndep, String nome, String morada, String telefone, String numcc, Date valcc, String username, String password) throws java.rmi.RemoteException{
		return db.createUser(cargo, ndep, nome, morada, telefone, numcc, valcc, username, password);
	}
	public HashMap<Integer,String> getDepartments() throws java.rmi.RemoteException{
		return db.getDepartments();
	}
	public int createElection(String cargos, String departamentos, String mesas, String titulo, String desc, LocalDateTime inicio, LocalDateTime fim) throws java.rmi.RemoteException{
		return db.createElection(cargos, departamentos, mesas, titulo, desc, inicio, fim);
	}
	public int editElection(int neleicao, boolean remove, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String departamentos, String mesas, Integer[] listas) throws java.rmi.RemoteException{
		return db.editElection(neleicao, remove, titulo, descricao, inicio, fim, departamentos, mesas, listas);
	}
	public HashMap<Integer,HashMap<String,String>> getElections(String username, String dep_mesa) throws java.rmi.RemoteException{
		return db.getElections(username, dep_mesa);
	}
	public int createOrEditList(int neleicao, String nome, ArrayList<database.Pair<String,String>> users, boolean remove) throws java.rmi.RemoteException{
		return db.createOrEditList(neleicao, nome, users, remove);
	}
	public HashMap<Integer, database.Pair<String,ArrayList<database.Pair<String,String>>>> getLists(int neleicao) throws java.rmi.RemoteException{
		return db.getLists(neleicao);
	}
	public int vote(String username, int neleicao, int nlista, int mesa) throws java.rmi.RemoteException{
		return db.vote(username, neleicao, nlista, mesa);
	}
	public HashMap<Integer, database.Pair<String,HashMap<Integer, database.Pair<String,Integer>>>> getResults(int neleicao) throws java.rmi.RemoteException{
		return db.getResults(neleicao);
	}
	public HashMap<Integer, database.Pair<Integer,String>> getUserVotes(String username) throws java.rmi.RemoteException{
		return db.getUserVotes(username);
	}
	public HashMap<String,HashMap<String,Integer>> getNumberVotesPerStation() throws java.rmi.RemoteException{
		return db.getNumberVotesPerStation();
	}
	public boolean isWorking() throws java.rmi.RemoteException{
		return db.isWorking();
	}
	public HashMap<String, database.Pair<Integer,Integer>> getActiveStationStatus() throws java.rmi.RemoteException{
		return db.getActiveStationStatus();
	}
	public void changeActiveStationStatus(int ndep, int availableTerms, int beingUsedTerms)throws java.rmi.RemoteException{
		db.changeActiveStationStatus(ndep, availableTerms, beingUsedTerms);
	}
	public void addFacebook(String username, String facebook_id) throws RemoteException {
		db.addFacebook(username, facebook_id);
	}
	public void change() throws RemoteException{
		try{
			this.stationsStatus = db.getActiveStationStatus();
			this.votesPerStation = db.getNumberVotesPerStation();
			HashMap<String,Object> hm = new HashMap<String,Object>();
			hm.put("stations",stationsStatus);
			hm.put("votes",votesPerStation);
			for(WebServer web: users){
				try {
					web.wsSession.getBasicRemote().sendText(new Gson().toJson(hm));
				} catch (IOException e) {
					// clean up once the WebSocket connection is closed
					try {
						this.wsSession.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		catch (RemoteException e) {
			connect();
		}

	}

	@OnOpen
	public void start(Session session) {
		this.wsSession = session;
		users.add(this);
	}

	@OnClose
	public void end() {
		// clean up once the WebSocket connection is closed
		users.remove(this);
	}

	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace();
	}

}
