/**
 * Raul Barbosa 2014-11-07
 */
package WebInterface.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;

import Commun.database;

public class WebServer {
	private static String RMI_PORT;
	private static String RMI_ADDRESS;
	static database db;

	public WebServer() {
		try {
			readConfig();
		}
		catch (Exception e){
			System.out.println(e);
			System.out.println(System.getProperty("user.dir"));
		}
	}

	private void readConfig() throws FileNotFoundException, IOException {
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


	public int login(String username, String password) throws RemoteException {
		return db.login(username, password);
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
}
