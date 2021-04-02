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

import com.mysql.cj.conf.ConnectionUrlParser.Pair;
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

class Heartbeat implements Runnable {
	private byte[] buffer;
	private DatagramPacket request;
	private DatagramSocket socket;
	CountDownLatch latch;
	private int connections;

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

public class RMIServer extends UnicastRemoteObject implements database{
	/**
	 *
	 */
	private static final long serialVersionUID = -5368081680660901104L;
	public static Connection conn;
	private static String enckey = "cenabuesegura";
	public static ArrayList<Pair<String,String>> logged;
	private static Thread current;
	public RMIServer() throws RemoteException {
		super();
	}

	private static void connectToBD(){
		try {
			//Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://ba5bfd4cfc576d:f93b7db6@eu-cdbr-west-03.cleardb.net/heroku_5e154400fde3501?reconnect=true");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void primary() {
		// create our mysql database connection
		connectToBD();
		//createUser("testador",0,"teste","testelandia","123456789","testecc",new Date(1970,1,1),"teste","passparatestes");
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
				logged.add(new Pair<String,String>(rs.getString("numcc"),rs.getString("nome")));
				return true; //sucesso
			}
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return login(username, num_cc, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
				query = "SELECT * FROM eleicoes WHERE (cargos = '"+cargo+"' OR cargos = 'all') AND departamentos LIKE '%"+departamento+";%' AND mesas LIKE '%"+dep_mesa+";%' AND estado = 'open';";
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
			String [] out = {rs.getString("numcc"),rs.getString("nome")};
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
	public boolean logOut(String username){
		try{
			String query = "SELECT * FROM users WHERE username = '"+username+"';";
			System.out.println(query);
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next()){
				logged.indexOf(new Pair<String,String>(rs.getString("numcc"),rs.getString("nome")));
				return true;
			}
			else{
				return false;
			}
		} catch (CommunicationsException | SQLNonTransientConnectionException e) {
			connectToBD();
			return logOut(username);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
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
			query = "SELECT COUNT(nlista) as count FROM listas;";
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			rs.next();
			int count = rs.getInt("count");
			if(users != null){
				for (Pair<String,String> pair : users) {
					query = "SELECT * FROM users WHERE numcc = '"+pair.left+"';";
					st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					rs = st.executeQuery();
					if(rs.next()){
						if((cargos.equals("all") && !rs.getString("cargo").equals(cargos)) || (departamentos != null && Arrays.asList(departamentos.split(";")).contains(Integer.valueOf(rs.getInt("ndep")).toString()))){
							return -2; //O user não corresponde aos parâmetros pedidos
						}
						if(!remove){
							rs.updateInt("nlista",count+1);
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
							rs.updateInt("nlista",count+1);
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
			query = "SELECT * FROM listas WHERE nome = '"+nome+"' AND neleicao = '"+neleicao+"';";
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			if(rs.next()){
				if(users==null){
					return -3; //Já existe uma lista com esse nome para essa eleição
				}
			}
			else{
				rs.moveToInsertRow();
				rs.updateInt("neleicao",neleicao);
				rs.updateString("nome",nome);
				rs.insertRow();
			}
			return count+1;
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
			int lista = rs.getInt("nlista");
			HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> out = new HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>>();
			while(rs.next()){
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
			query = "SELECT * FROM votos WHERE username = '"+username+"' AND neleicao = "+neleicao+";";
			System.out.println(query);
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			if(rs.next()){
				return -1; // Já votou nesta eleição
			}
			else{
				rs.moveToInsertRow();
				rs.updateString("username", username); rs.updateInt("ndep", mesa); rs.updateInt("neleicao", neleicao);
				rs.insertRow();
				query = "SELECT * FROM eleicoes WHERE neleicao = "+neleicao+";";
				st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = st.executeQuery();
				rs.next();
				if(criado.before(rs.getTimestamp("inicio"))){
					query = "SELECT * FROM listas WHERE nlista = "+nlista+";";
					st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					rs = st.executeQuery();
					rs.next();
					rs.updateInt("votos",rs.getInt("votos")+1);
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
			query = "SELECT listas.nlista, listas.neleicao, listas.nome, eleicoes.titulo FROM listas, eleicoes WHERE listas.neleicao = "+neleicao+" AND eleicoes.neleicao = "+neleicao+" AND estado = 'open';";
		}
		else{
			query = "SELECT listas.nlista, listas.neleicao, listas.nome, eleicoes.titulo FROM listas, eleicoes WHERE listas.neleicao = eleicoes.neleicao AND estado = 'open';";
		}
		try{
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = st.executeQuery();
			while(rs.next()){
				if(out.containsKey(rs.getInt("neleicao"))){
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
			return null;
		}
	}
	public static Timestamp updateElections(){
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
					if(rs2.next()){
						rs.updateString("estado", "open");
						rs.updateRow();	
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

}