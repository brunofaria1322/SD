import java.rmi.*;
import java.rmi.server.*;
import java.sql.Time;
import java.util.concurrent.CountDownLatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Heartbeat implements Runnable {
	private byte[] buffer;
	private DatagramPacket request;
	private DatagramSocket socket;
	CountDownLatch latch;
	public Heartbeat() {
		buffer = new byte[2];
		request = new DatagramPacket(buffer, buffer.length);
		latch = new CountDownLatch(1);
		try{
			socket = new DatagramSocket(6789);
		}
		catch (SocketException e){System.out.println("Socket: " + e.getMessage());}
		new Thread(this, "Primario").start();
		try{
			latch.await();
		}
		catch(InterruptedException e) {
			System.out.println("interruptedException caught");
		}
	  }
	
	public synchronized void run() {
		while(true){
			try{
				socket.receive(request);
				latch.countDown();
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
				socket.send(reply);
			}
			catch (SocketException e){System.out.println("Socket: " + e.getMessage());}
			catch (IOException e) {System.out.println("IO: " + e.getMessage());
			}
		}
	}
}

public class RMIServer extends UnicastRemoteObject {

	public RMIServer() throws RemoteException {
		super();
	}

	
	// =======================================================

	public static void main(String args[]) {

		try {
			RMIServer h = new RMIServer();
			Registry r = LocateRegistry.createRegistry(1099);
			r.rebind("central", h);
			System.out.println("Sou o servidor primário!");
			new Heartbeat();
			System.out.println("Conectado ao servidor secundário!");
		} catch (ExportException re){
			System.out.println("Sou o servidor secundário!");
			// argumentos da linha de comando: hostname 
			int no_replies = 0;
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();   
				socket.setSoTimeout(1000); 
				byte [] m = "P".getBytes();
				byte[] buffer = new byte[2];
				InetAddress aHost = InetAddress.getByName("localhost");                                 
				DatagramPacket request = new DatagramPacket(m,m.length,aHost,6789);
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
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