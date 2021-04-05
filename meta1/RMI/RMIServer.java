//Bruno Ricardo Leitão Faria
//Diogo Alves Almeida Flórido <-- autor do ficheiro


package RMI;

import java.rmi.*;
import java.rmi.server.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Commun.database;
import Commun.database.Pair;
/**
 * Thread that changes messages with a possible secondary RMI server via a Datagram Socket.
 *
 *
 * @author Diogo Flórido
 * @version 1.0
 */
class Heartbeat implements Runnable {
	/**
	 * Buffer for holding the datagrams sent to the datagram socket.
	 */
	private byte[] buffer;
	/**
	 * The last datagram packet received.
	 */
	private DatagramPacket request;
	/**
	 * The datagram socket.
	 */
	private DatagramSocket socket;
	/**
	 * A Countdown Latch used to block the Primary RMI Server's main thread until a password is received by
	 * a Secondary RMI Server.
	 */
	CountDownLatch latch;
	/**
	 * Set to 1 when the Primary RMI Server starts changing messages with the secondary one. Set back to 0 when
	 * the secondary server stops sending messages.
	 */
	private int connections;
	/**
	 * Constructor of the thread.
	 * @param wait_for_sec if true, the Primary RMI Server will wait for a secondary server before connecting
	 * to the database.
	 */
	public Heartbeat(boolean wait_for_sec) {
		buffer = new byte[20];
		request = new DatagramPacket(buffer, buffer.length);
		latch = new CountDownLatch(1);
		connections = 0;
		try {
			socket = new DatagramSocket(6789);
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		}
		new Thread(this, "Primario").start();
		try {
			if (wait_for_sec) {
				latch.await();
			}
		} catch (InterruptedException e) {
			System.out.println("interruptedException caught");
		}
	}
	/**
	 * Method called when the thread is created. 
	 * <p>
	 * The thread starts listening for messages sent via the datagram {@link  #socket}. If a specific password is received,
	 * the primary server acknowledges it was sent by a secondary server and will reply back if it is not already
	 * changing messages with another one. If no messages are received, the primary server will assume the secondary
	 * one has stopped working and will be listening for the password again but not stopping the main thread {@link RMIServer}
	 */
	public synchronized void run() {
		while (true) {
			try {
				try{
					socket.receive(request);
				}
				catch(SocketTimeoutException e){
					System.out.println("CUIDADO: estou a correr sem servidor secundario!");
					socket.setSoTimeout(0);
					connections--;
				}
				if(new String(request.getData(), 0, request.getLength()).equals("Sou eu, o secundario") && connections == 0){
					latch.countDown();
					socket.send(new DatagramPacket("gosto de ti".getBytes(), "gosto de ti".length(), request.getAddress(),request.getPort()));
					socket.receive(request);
					connections++;
					socket.setSoTimeout(10000);
					System.out.println("Estou conectado ao servidor secundario");
				}
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(),
						request.getPort());
				socket.send(reply);
			} catch (SocketException e) {
				System.out.println("Socket: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			}
		}
	}
}
/**
 * The main class for the RMI Servers.~
 * @author Diogo Flórido
 * @version 1.0
 */
public class RMIServer extends UnicastRemoteObject implements database{

	private static final long serialVersionUID = -5368081680660901104L;
	/**
	 * The session with the database.
	 */
	public static Connection conn;
	/**
	 * A key used for password encryption.
	 */
	private static String enckey = "cenabuesegura";
	/**
	 * A reference to the server's main thread.
	 */
	private static Thread current;
	/**
	 * A structure with the votes per Voting Station for each active election being updated in real time.
	 *
	 */
	private static HashMap<String,HashMap<String,Integer>> rtstations;
	/**
	 * A structure with the names of all the departments with a voting station.
	 */
	private static HashMap<Integer,String> rtdepartamentos;
	/**
	 * A structure with the number of available and active voting terminals being updated in real time.
	 */
	private static HashMap<Integer,Pair<Integer,Integer>> rtterminais = new HashMap<>();
	/**
	 * The constructor of the class.
	 * @throws RemoteException if failed to export object.
	 */
	public RMIServer() throws RemoteException {
		super();
	}
	/**
	 * A boolean which tells if the Server is connected to the database or not.
	 */
	private static boolean isworking = false;
	/**
	 * Starts a session with the database.
	 * <ul>
	 * <li>All data is stored in an external database hosted in a platform called <a href= https://www.heroku.com/>Heroku</a>. See the database's 
	 * diagram in database.png 
	 * <li>This method is called in the beginning of the execution and everytime an operation in the database
	 * throws a {@link CommunicationsException}. It initializes all the real time structures by reading data from the database.
	 * </ul>
	 */
	private static void connectToBD(){
		try {
			conn = DriverManager.getConnection("jdbc:mysql://ba5bfd4cfc576d:f93b7db6@eu-cdbr-west-03.cleardb.net/heroku_5e154400fde3501?reconnect=true");
			rtstations = NumberVotesPerStation();
			rtdepartamentos = privateGetDepartments();
			isworking = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Starts the job as the Primary RMI Server.
	 * <ul>
	 * <li>The server connects to the database with {@link #connectToBD}.
	 * <li>It calls {@link #updateElections} to update the status ("waiting","open","closed") of the elections, if needed, and makes the main thread
	 * {@link Thread#sleep} until the next exact {@link Timestamp} when an election needs to be updated.
	 * <li>If there are no elections to watch, the server will wait 1 minute and check again.
	 * </ul>
	 */
	private static void primary() {
		connectToBD();
		Timestamp ue;
		current = Thread.currentThread();
		while(true){
			try {
				ue = updateElections();
				if(System.currentTimeMillis() - ue.getTime() > 0){
					Thread.sleep(System.currentTimeMillis() - ue.getTime());
				}
				else{
					Thread.sleep(60000);
				}
			} catch (InterruptedException e) {
				; //nova eleicao ou alteração de datas
			}
		}
	}

	public static void setRtterminais(HashMap<Integer, Pair<Integer, Integer>> rtterminais) {
		RMIServer.rtterminais = rtterminais;
	}

	public int createUser(String cargo, int ndep, String nome, String morada, String telefone, String numcc, Date valcc, String username, String password){
		try {
			String query = "SELECT * FROM users WHERE username = '"+username+"' OR numcc = '"+numcc+"';";
			System.out.println(query);
			PreparedStatement st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next() == false){
				rs.moveToInsertRow();
				rs.updateString("cargo", cargo); rs.updateInt("ndep", ndep); rs.updateString("nome", nome); rs.updateString("morada", morada); rs.updateString("telefone", telefone);rs.updateString("numcc", numcc);rs.updateDate("valcc", valcc);rs.updateString("username", username);
				rs.updateString("password",AES.encrypt(password, enckey));
				rs.updateTimestamp("data_created", new Timestamp(System.currentTimeMillis()));
				rs.insertRow();
				return 1; // Sucesso
			}
			else if(rs.getString("username").equals(username)){
				return -1; //Username já existe
			}
			else if(rs.getString("numcc").equals(numcc)){
				if(rs.getString("username") == null){
					rs.updateString("cargo", cargo); rs.updateInt("ndep", ndep); rs.updateString("nome", nome); rs.updateString("morada", morada); rs.updateString("telefone", telefone);rs.updateString("numcc", numcc);rs.updateDate("valcc", valcc);rs.updateString("username", username);
					rs.updateString("password",AES.encrypt(password, enckey));
					rs.updateTimestamp("data_created", new Timestamp(System.currentTimeMillis()));
					rs.updateRow();
					return 1;
				}
				return -2; //CC já existe
			}
			else{
				return -3; //Erro que não devia acontecer
			}
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return createUser(cargo, ndep, nome, morada, telefone, numcc, valcc, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
			return -3;
		}
	}
	public HashMap<Integer,String> getDepartments(){ // {faculdade: [(ndep,dep)]}
		try {
			HashMap<Integer,String>  deps = new HashMap<Integer,String>();
			String query = "SELECT * FROM departamentos;";
			System.out.println(query);
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				deps.put(rs.getInt("ndep"), rs.getString("nome"));
			}
			return deps;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return getDepartments();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * A static version of {@link #getDepartments}
	 * @return an {@link HashMap}<{@link Integer},{@link String}> 
	 * <ul>
	 * <li> {id_of_the_department : name_of_the_department}
	 * </ul>
	 */
	private static HashMap<Integer,String> privateGetDepartments(){ // {faculdade: [(ndep,dep)]}
		try {
			HashMap<Integer,String>  deps = new HashMap<Integer,String>();
			String query = "SELECT * FROM departamentos;";
			System.out.println(query);
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				deps.put(rs.getInt("ndep"), rs.getString("nome"));
			}
			return deps;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return privateGetDepartments();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public boolean login(String username, String num_cc, String password){
		try {
			String query = "SELECT * FROM users WHERE username = '"+username+"';";
			System.out.println(query);
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next()==false || !rs.getString("numcc").equals(num_cc)){
				return false; //dados invalidos
			}
			if(AES.decrypt(rs.getString("password"),enckey).equals(password)){
				return true; //sucesso
			}
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return login(username, num_cc, password);
		} catch (SQLException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}
	// =======================================================
	public int createElection(String cargos, String departamentos, String mesas, String titulo, String desc, LocalDateTime inicio, LocalDateTime fim){
		//cargos = {'aluno','docente','funcionario','all'}
		//departamentos, mesas = ndeps separados por ; (; no fim também)
		try {
			if(departamentos == null){
				departamentos = ";";
			}
			if(mesas == null){
				mesas = ";";
			}
			int ret = 0;
			String query = "SELECT * FROM eleicoes WHERE titulo = '"+titulo+"';";
			System.out.println(query);
			PreparedStatement st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next()){
				ret = -1; // WARNING: Já existe uma eleição com esse título. Tem a certeza?
			}
			rs.moveToInsertRow();
			rs.updateString("cargos", cargos); rs.updateString("departamentos", departamentos); rs.updateString("mesas", mesas); rs.updateString("titulo", titulo); rs.updateString("descricao", desc);rs.updateTimestamp("inicio", Timestamp.valueOf(inicio));rs.updateTimestamp("fim", Timestamp.valueOf(fim));
			rs.updateString("estado", "waiting");
			rs.insertRow();
			current.interrupt();
			ResultSet rs2 = st.executeQuery("SELECT LAST_INSERT_ID()");
			rs2.next();
			createOrEditList(rs2.getInt(1), "votos em branco", null, false);
			createOrEditList(rs2.getInt(1), "votos nulos", null, false);
			if(ret == -1){
				return 0-rs2.getInt(1);
			}
			else{
				return rs2.getInt(1);
			}
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return createElection(cargos, departamentos, mesas, titulo, desc, inicio, fim);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	public int editElection(int neleicao, boolean remove, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String departamentos, String mesas, Integer[] listas){
		try {
			String query = "SELECT * FROM eleicoes WHERE neleicao = "+neleicao+";";
			System.out.println(query);
			PreparedStatement st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next() == false){
				return -1; //A eleição não existe
			}
			if(!rs.getString("estado").equals("waiting") && (departamentos != null || listas != null || titulo != null || descricao != null)){
				return -2; //A eleição já começou
			}
			if(titulo != null){
				rs.updateString("titulo", titulo);
			}
			if(descricao != null){
				rs.updateString("descricao", descricao);
			}
			if(inicio != null){
				rs.updateTimestamp("inicio", Timestamp.valueOf(inicio));
			}
			if(fim != null){
				rs.updateTimestamp("fim", Timestamp.valueOf(fim));
			}
			if(remove){
				if(departamentos!=null){
					String deps = rs.getString("departamentos");
					for (String d : departamentos.split(";")) {
						deps = deps.replace(d+";", "");
					}
					rs.updateString("departamentos",deps);
				}
				if(mesas!=null){
					String me = rs.getString("mesas");
					for (String d : mesas.split(";")) {
						me = me.replace(d+";", "");
					}
					rs.updateString("mesas",me);
				}
			}
			else{
				if(departamentos!=null){
					rs.updateString("departamentos",rs.getString("departamentos")+departamentos);
				}
				if(mesas!=null){
					rs.updateString("mesas",rs.getString("mesas")+mesas);
				}
			}
			rs.updateRow();
			if(remove && listas != null){
				for (int i : listas) {
					query = "SELECT * FROM listas WHERE nlista = "+i+";";
					st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					System.out.println(query);
					rs = st.executeQuery();
					if(rs.next()){
						rs.updateInt("neleicao",0);
						rs.updateRow();
					}
				}
			}
			current.interrupt();
			return 1;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return editElection(neleicao, remove, titulo, descricao, inicio, fim, departamentos, mesas, listas);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -3;
		}
	}
	public HashMap<Integer,HashMap<String,String>> getElections(String username, String dep_mesa){
		// {neleicao:{titulo:String, descricao: String, inicio: String, fim: String, departamentos: String, mesas: String}}
		try {
			String query;
			PreparedStatement st;
			ResultSet rs;
			if(username!=null){
				query = "SELECT * FROM users WHERE username = '"+username+"';";
				System.out.println(query);
				st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = st.executeQuery();
				rs.next();
				int departamento = rs.getInt("ndep");
				String cargo = rs.getString("cargo");
				if(dep_mesa!=null){
					query = "SELECT * FROM eleicoes WHERE (cargos = '"+cargo+"' OR cargos = 'all') AND departamentos LIKE '%;"+departamento+";%' AND mesas LIKE '%;"+dep_mesa+";%' AND estado = 'open';";
				}
				else{
					query = "SELECT * FROM eleicoes WHERE (cargos = '"+cargo+"' OR cargos = 'all') AND departamentos LIKE '%;"+departamento+";%' AND estado != 'open';";
				}
			}
			else{
				query = "SELECT * FROM eleicoes;";
			}
			System.out.println(query);
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			final ResultSet rs2 = st.executeQuery();
			HashMap<Integer,HashMap<String,String>> out = new HashMap<Integer,HashMap<String,String>>();
			while(rs2.next()){
				HashMap<String,String> hm = new HashMap<String,String>();
				hm.put("titulo",rs2.getString("titulo"));
				hm.put("descricao", rs2.getString("descricao"));
				hm.put("inicio",rs2.getTimestamp("inicio").toString());
				hm.put("fim", rs2.getTimestamp("fim").toString());
				hm.put("departamentos", rs2.getString("departamentos"));
				hm.put("mesas", rs2.getString("mesas"));
				out.put(rs2.getInt("neleicao"), hm);
			}
			return out;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return getElections(username, dep_mesa);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	public String[] getUser(String usernameOrCC){
		// (cc,nome)
		try {
			String query = "SELECT * FROM users WHERE username = '"+usernameOrCC+"' OR numcc = '"+usernameOrCC+"';";
			System.out.println(query);
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next()==false){
				return null; //não há user com esse username
			}
			String [] out = {rs.getString("numcc"),rs.getString("nome"),rs.getString("username")};
			return out;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return getUser(usernameOrCC);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public int createOrEditList(int neleicao, String nome, ArrayList<Pair<String,String>> users, boolean remove){
		//Pair(numcc,nome)
		try{
			String query;
			PreparedStatement st;
			ResultSet rs;
			String departamentos;
			String cargos;
			query = "SELECT * FROM eleicoes WHERE neleicao = "+neleicao+";";
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			if(!rs.next()){
				return -1; //a eleicao não existe
			}
			else{
				departamentos = rs.getString("departamentos");
				cargos = rs.getString("cargos");
			}
			query = "SELECT * FROM listas WHERE nome = '"+nome+"' AND neleicao = '"+neleicao+"';";
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			int count;
			if(rs.next()){
				count = rs.getInt("nlista");
				if(users==null){
					return -3; //Já existe uma lista com esse nome para essa eleição
				}
			}
			else{
				rs.moveToInsertRow();
				rs.updateInt("neleicao",neleicao);
				rs.updateString("nome",nome);
				rs.insertRow();
				rs = st.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				count = rs.getInt(1);
			}

			if(users != null){
				for (Pair<String,String> pair : users) {
					query = "SELECT * FROM users WHERE numcc = '"+pair.left+"';";
					st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					rs = st.executeQuery();
					if(rs.next()){
						if((rs.getString("username")!= null && !cargos.equals("all") && !rs.getString("cargo").equals(cargos)) || (departamentos != null && Arrays.asList(departamentos.split(";")).contains(Integer.valueOf(rs.getInt("ndep")).toString()))){
							return -2; //O user não corresponde aos parâmetros pedidos
						}
						if(!remove){
							rs.updateInt("nlista",count);
						}
						else{
							rs.updateInt("nlista",0);
						}
						rs.updateRow();
					}
					else{ //cria uma pessoa não registada
						rs.moveToInsertRow();
						rs.updateString("cargo", cargos); rs.updateString("nome", pair.right); rs.updateString("numcc", pair.left);
						if(!remove){
							rs.updateInt("nlista",count);
						}
						else{
							rs.updateInt("nlista",0);
						}
						rs.updateTimestamp("data_created", new Timestamp(System.currentTimeMillis()));
						rs.insertRow();
					}
				}
				current.interrupt();
			}

			return count;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return createOrEditList(neleicao, nome, users, remove);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -4;
		}
	}
	public HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> getLists(int neleicao){
		// {nlista:(nome,[(cc,nome)])}
		try {
			String query;
			PreparedStatement st;
			ResultSet rs;

			if(neleicao!=0){
				query = "SELECT * FROM listas WHERE neleicao = "+neleicao+";";
			}
			else{
				query = "SELECT * FROM listas;";
			}
			System.out.println(query);
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> out = new HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>>();
			while(rs.next()){
				int lista = rs.getInt("nlista");
				ArrayList<Pair<String,String>> al = new ArrayList<Pair<String,String>>();
				String query2;
				PreparedStatement st2;
				ResultSet rs2;
				query2 = "SELECT * FROM users WHERE nlista = "+lista+";";
				st2 = conn.prepareStatement(query2,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs2 = st2.executeQuery();
				while(rs2.next()){
					al.add(new Pair<String,String>(rs2.getString("numcc"),rs2.getString("nome")));
				}
				out.put(lista, new Pair<String,ArrayList<Pair<String,String>>>(rs.getString("nome"),al));
			}
			return out;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return getLists(neleicao);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	public int vote(String username, int neleicao, int nlista, int mesa){
		try {
			String query = "SELECT * FROM users WHERE username = '"+username+"';";
			PreparedStatement st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			rs.next();
			Timestamp criado = rs.getTimestamp("data_created");
			query = "SELECT * FROM departamentos WHERE ndep = "+mesa+";";
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			rs.next();
			String dep = rs.getString("nome");
			query = "SELECT * FROM votos WHERE username = '"+username+"' AND neleicao = "+neleicao+";";
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			if(rs.next()){
				return -1; // Já votou nesta eleição
			}
			else{
				rs.moveToInsertRow();
				rs.updateString("username", username); rs.updateInt("ndep", mesa); rs.updateInt("neleicao", neleicao);
				rs.updateTimestamp("data", new Timestamp(System.currentTimeMillis()));
				rs.insertRow();
				query = "SELECT * FROM eleicoes WHERE neleicao = "+neleicao+";";
				st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = st.executeQuery();
				rs.next();
				String elec = rs.getString("titulo");
				if(criado.before(rs.getTimestamp("inicio"))){
					query = "SELECT * FROM listas WHERE nlista = "+nlista+";";
					st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					rs = st.executeQuery();
					rs.next();
					rs.updateInt("votos",rs.getInt("votos")+1);
					rs.updateRow();
					if(rtstations.containsKey(elec)){
						if(rtstations.get(elec).containsKey(dep)){
							rtstations.get(elec).put(dep, rtstations.get(elec).get(dep)+1);
						}
						else{
							rtstations.get(elec).put(dep, 1);
						}

					}
					else{
						HashMap<String,Integer> hm = new HashMap<String,Integer>();
						hm.put(dep, 1);
						rtstations.put(elec, hm);
					}

					return 1;
				}
				else{
					return -2; //Conta criada depois da eleição
				}
			}

		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return vote(username, neleicao, nlista, mesa);
		} catch (SQLException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			return -3;
		}
	}
	public HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> getResults(int neleicao){
		// {neleicao: (nome_eleicao, {nlista: (nome_lista, votos)})}
		HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> out = new HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>>();
		String query;
		PreparedStatement st;
		ResultSet rs;
		if(neleicao > 0){
			query = "SELECT listas.nlista, listas.neleicao, listas.nome, listas.votos, eleicoes.titulo FROM listas, eleicoes WHERE listas.neleicao = "+neleicao+" AND eleicoes.neleicao = "+neleicao+";";
		}
		else{
			query = "SELECT listas.nlista, listas.neleicao, listas.nome, listas.votos, eleicoes.titulo FROM listas, eleicoes WHERE listas.neleicao = eleicoes.neleicao AND estado = 'open';";
		}
		try{
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			while(rs.next()){
				if(!out.containsKey(rs.getInt("neleicao"))){
					HashMap<Integer,Pair<String,Integer>> hm = new HashMap<Integer,Pair<String,Integer>>();
					hm.put(rs.getInt("nlista"),new Pair<String,Integer>(rs.getString("nome"),rs.getInt("votos")));
					out.put(rs.getInt("neleicao"), new Pair<String,HashMap<Integer,Pair<String,Integer>>>(rs.getString("titulo"),hm));
				}
				else{
					out.get(rs.getInt("neleicao")).right.put(rs.getInt("nlista"),new Pair<String,Integer>(rs.getString("nome"),rs.getInt("votos")));
				}
			}
			return out;
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return getResults(neleicao);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("erro: " + e);
			return null;
		}
	}
	/**
	 * Updates the status of all the elections in need.
	 * <ul>
	 * <li>If an election with no lists starts, it gets deleted
	 * </ul>
	 * @return the {@link Timestamp} of the next update.
	 */
	public static synchronized Timestamp updateElections(){
		try{
			String query = "SELECT * FROM eleicoes WHERE estado = 'waiting' ORDER BY inicio ASC;";
			PreparedStatement st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			Timestamp nextWaiting = new Timestamp(System.currentTimeMillis());
			nextWaiting.setTime(nextWaiting.getTime()+TimeUnit.MINUTES.toMillis(1));
			Timestamp nextOpen = new Timestamp(System.currentTimeMillis());;
			nextOpen.setTime(nextOpen.getTime()+TimeUnit.MINUTES.toMillis(1));
			while(rs.next()){

				nextWaiting = rs.getTimestamp("inicio");
				if(rs.getTimestamp("inicio").before(new Timestamp(System.currentTimeMillis()))){
					String query2 = "SELECT * FROM listas WHERE neleicao = "+rs.getInt("neleicao")+";";
					PreparedStatement st2 = conn.prepareStatement(query2,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					ResultSet rs2 = st2.executeQuery();
					int size = 0;
					while(rs2.next()){
						size+=1;
					}
					if(size > 2){
						rs.updateString("estado", "open");
						rs.updateRow();
					}
					else{
						rs.deleteRow();
						while(rs2.previous()){
							rs2.deleteRow();
						}
					}
				}
				else{
					break;
				}
			}
			query = "SELECT * FROM eleicoes WHERE estado = 'open' ORDER BY fim ASC;";
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			while(rs.next()){
				nextOpen = rs.getTimestamp("inicio");
				if(rs.getTimestamp("fim").before(new Timestamp(System.currentTimeMillis()))){
					rs.updateString("estado", "done");
					rs.updateRow();
				}
				else{
					break;
				}
			}
			if(nextWaiting.before(nextOpen)){
				return nextWaiting;
			}
			else{
				return nextOpen;
			}
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return updateElections();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public HashMap<Integer,Pair<Integer,String>> getUserVotes(String username) throws java.rmi.RemoteException{

		try {
			HashMap<Integer,Pair<Integer,String>> out = new HashMap<Integer,Pair<Integer,String>>();
			String query = "SELECT * FROM votos WHERE username = '"+username+"';";
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				out.put(rs.getInt("neleicao"), new Pair<Integer,String>(rs.getInt("ndep"),rs.getTimestamp("data").toString()));
			}
			return out;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	private static HashMap<String,HashMap<String,Integer>> NumberVotesPerStation(){
		//{eleicao:{dep:votos}}
		try {
			HashMap<String,HashMap<String,Integer>> out = new HashMap<String,HashMap<String,Integer>>();
			String query = "SELECT eleicoes.titulo,departamentos.nome, COUNT(*) as count FROM votos, eleicoes, departamentos WHERE votos.neleicao = eleicoes.neleicao AND eleicoes.estado = 'open' AND votos.ndep = departamentos.ndep GROUP BY eleicoes.neleicao, votos.ndep";
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				HashMap<String,Integer> hm = new HashMap<String,Integer>();
				hm.put(rs.getString("departamentos.nome"), rs.getInt("count"));
				out.put(rs.getString("eleicoes.titulo"), hm);
			}
			return out;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public HashMap<String,HashMap<String,Integer>> getNumberVotesPerStation() throws java.rmi.RemoteException{
		return rtstations;
	}
	public boolean isWorking() throws java.rmi.RemoteException{
		return isworking;
	}
	/**
	 * The main method of the RMIServer class.
	 * <ul>
	 * <li>If a registry has not been created in the port yet, the server becomes the Primary RMI Server and starts a {@link Heartbeat} thread.
	 * <li>If the Primary RMI Server starts changing messages with a secondary one, it will execute the {@link #primary} method.
	 * <li>If a registry has already been created in the port, the server sends a message to the Primary RMI Server indicating that it is willing to become a Secondary RMI Server then waits for a response.
	 * <li>If a server willing to become a Secondary RMI Server gets an approval message it becomes one else it exits.
	 * <li>If a Secondary RMI Server doesn't receive 5 replies in a row, it will assume the Primary RMI Server has crashed and will assume it's role.
	 * </ul>
	 */
	public static void main(String args[]) {

		try {
			RMIServer h = new RMIServer();
			Registry r = LocateRegistry.createRegistry(1099);
			r.rebind("central", h);
			System.out.println("Sou o servidor primario!");
			new Heartbeat(true);
			System.out.println("Comecei a trabalhar!");
			primary();
		} catch (ExportException re){
			int no_replies = 0;
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(1000);
				byte [] m = "P".getBytes();
				byte [] pass = "Sou eu, o secundario".getBytes();
				byte[] buffer = new byte[12];
				InetAddress aHost = InetAddress.getByName("localhost");
				DatagramPacket request = new DatagramPacket(m,m.length,aHost,6789);
				DatagramPacket requestpass = new DatagramPacket(pass,pass.length,aHost,6789);
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				socket.send(requestpass);
				socket.receive(reply);
				if(!new String(reply.getData(), 0, reply.getLength()).equals("gosto de ti")){
					System.out.println("Ja existem servidores primario e secundario logo sou inutil. Adeus!");
					System.exit(0);
				}
				System.out.println("Sou o servidor secundario!");
				while(true){
					socket.send(request);
					try{
						socket.receive(reply);
						no_replies = 0;
					}
					catch(SocketTimeoutException e){
						no_replies++;
						if(no_replies == 1){
							System.out.println("1 ping sem resposta...");
						}
						else{
							System.out.println(no_replies + " pings sem resposta...");
						}
						if(no_replies == 5){
							System.out.println("O servidor principal morreu. Tomarei o seu cargo!");
							RMIServer h = new RMIServer();
							Registry r = LocateRegistry.createRegistry(1099);
							r.rebind("central", h);
							new Heartbeat(false);
							primary();
							break;
						}
					}
					if(no_replies == 0){
						try{
							Thread.sleep(1000);
						}
						catch(InterruptedException e) {
							System.out.println("interruptedException caught");
						}
					}
				}

			}catch (SocketException e){System.out.println("Socket: " + e.getMessage());
			}catch (IOException e){System.out.println("IO: " + e.getMessage());
			}
		}
		catch (RemoteException re) {
			System.out.println("Exception in RMIServer.main: " + re);
		}

	}
	public void changeActiveStationStatus(int ndep, int availableTerms, int beingUsedTerms)throws java.rmi.RemoteException{
		if(availableTerms > 0){
			rtterminais.put(ndep, new Pair<>(availableTerms, beingUsedTerms));
		}
		else{
			if(rtterminais.containsKey(ndep)){
				rtterminais.remove(ndep);
			}
		}
	}
	public HashMap<String,Pair<Integer,Integer>> getActiveStationStatus() throws java.rmi.RemoteException{
		HashMap<String,Pair<Integer,Integer>> out = new HashMap<String,Pair<Integer,Integer>>();
		if(rtterminais!=null){
			for(Integer i : rtterminais.keySet()){
				out.put(rtdepartamentos.get(i), rtterminais.get(i));
			}
		}
		return out;
	}
}