import java.rmi.*;
import java.rmi.server.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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

public class RMIServer extends UnicastRemoteObject {
	public static Connection conn;
	private static String enckey = "cenabuesegura";
	public RMIServer() throws RemoteException {
		super();
	}

	private static void primary() {
		// create our mysql database connection
		try {
			//Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://ba5bfd4cfc576d:f93b7db6@eu-cdbr-west-03.cleardb.net/heroku_5e154400fde3501?reconnect=true");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//createUser("testador",0,"teste","testelandia","123456789","testecc",new Date(1970,1,1),"teste","passparatestes");
	}
	public static int createUser(String cargo, int ndep, String nome, String morada, String telefone, String numcc, Date valcc, String username, String password){
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
				return -2; //CC já existe
			}
			else{
				return -3; //Erro que não devia acontecer
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -3;
		}
	}
	public static HashMap<String,ArrayList<Pair<Integer,String>>> getDepartments(){ // {faculdade: [(ndep,dep)]}
		try {
			HashMap<String,ArrayList<Pair<Integer,String>>> deps = new HashMap<String,ArrayList<Pair<Integer,String>>>();
			String query = "SELECT * FROM departamentos;";
			System.out.println(query);
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				if(!deps.containsKey(rs.getString("faculdade"))){
					ArrayList<Pair<Integer,String>> arr = new ArrayList<Pair<Integer,String>>();
					arr.add(new Pair<Integer,String>(rs.getInt("ndep"),rs.getString("nome")));
					deps.put(rs.getString("faculdade"), arr);
				}
				else{
					deps.get(rs.getString("faculdade")).add(new Pair<Integer,String>(rs.getInt("ndep"),rs.getString("nome")));
				}
			}
			return deps;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static boolean login(String username, String password){
		try {
			String query = "SELECT * FROM users WHERE username = '"+username+"';";
			System.out.println(query);
			PreparedStatement st;
			st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next()==false){
				return false; //dados invalidos
			}
			if(AES.decrypt(rs.getString("password"),enckey).equals(password)){
				return true; //sucesso
			}
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return false;
	}
	// =======================================================
	public static int createVoting(String cargos, String departamentos, String mesas, String titulo, String desc, LocalDateTime inicio, LocalDateTime fim){
		//cargos = {'aluno','docente','funcionario','all'}
		//departamentos, mesas = ndeps separados por ; (; no fim também)
		try {
			int ret = 1; // sucesso
			String query = "SELECT * FROM eleicoes WHERE titulo = '"+titulo+"';";
			System.out.println(query);
			PreparedStatement st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next() == false){
				ret = 2; // WARNING: Já existe uma eleição com esse título. Tem a certeza?
			}
				rs.moveToInsertRow();
				rs.updateString("cargos", cargos); rs.updateString("departamentos", departamentos); rs.updateString("mesas", mesas); rs.updateString("titulo", titulo); rs.updateString("descricao", desc);rs.updateTimestamp("inicio", Timestamp.valueOf(inicio));rs.updateTimestamp("fim", Timestamp.valueOf(fim));
				rs.updateString("estado", "waiting");
				rs.insertRow();
				return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1; // Erro qualquer que não devia acontecer
		}
	}
	public static int editVoting(int neleicao, boolean remove, String departamentos, String mesas){
		try {
			String query = "SELECT * FROM eleicoes WHERE neleicao = '"+neleicao+"';";
			System.out.println(query);
			PreparedStatement st = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = st.executeQuery();
			if(rs.next() == false){
				return -1; //A eleição não existe
			}
			if(!rs.getString("estado").equals("waiting")){
				return -2; //A eleição já começou
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
			return 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -3; // Erro qualquer que não devia acontecer
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
			// argumentos da linha de comando: hostname 
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