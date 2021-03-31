import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

public class MulticastServer extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private String DEP = "NE";
    private database db;

    public static void main(String[] args) {

        MulticastServer server = new MulticastServer();
        server.start();
    }

    public MulticastServer() {
        //TODO: ler config.txt
    }

    public void run() {
        MulticastSocket socket = null;
        int ids = 0;

        HashMap<Integer,Boolean> terminals = new HashMap<Integer,Boolean>();    // terminal_id : isFree
        
        try {
			db= (database) LocateRegistry.getRegistry(1099).lookup("central");
		} catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
		}
        
        System.out.println(DEP + " Polling station running...");
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            PollingStationInterface it = new PollingStationInterface(group, PORT, db);

            socket = new MulticastSocket(PORT);  // create socket and bind it
            socket.joinGroup(group);

            byte[] buffer;
            DatagramPacket packet;
            String message;
            
            HashMap<String,String> hash_map;

            while (true) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = it.packetToHashMap(packet);
                
                switch (hash_map.get("type")) {
                    case "identification":
                        if(hash_map.get("id") == null){
                            it.sendMessage("type | identification; id | " + ids);
                            terminals.put(ids++, true);
                        }
                        break;
                
                    case "leaving":
                        if(hash_map.get("id") == null){
                            terminals.remove(Integer.parseInt(hash_map.get("id")));

                            //DEBUG
                            message = new String(packet.getData(), 0, packet.getLength());
                            System.out.println(message);
                        }
                        break;
                

                    default:
                        //DEBUG
                        System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                        message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println(message);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}

class PollingStationInterface extends Thread{
    private MulticastSocket socket;
    private InetAddress group;
    private int PORT;
    private database db;

    public PollingStationInterface(InetAddress group, int PORT, database db){
        this.group = group;
        this.PORT = PORT;
        this.db = db;
        this.start();
    }

    public void run() {
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)

            int option;
            Scanner in = new Scanner(System.in);
            do{
                System.out.print("\n1 - Find Person\noption: ");

                option = in.nextInt();
                in.nextLine();
                
                switch (option){
                    case 0:
                        break;
                    case 1:
                        findPersonByUsernameOrCC(in);
                        break;
                    default:
                        System.out.println("Wrong option!");
                        break;
                }
            }while (option != 0);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Bye!");
            socket.close();
        }
    }

    public void sendMessage( String message ){
        try {
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.group, this.PORT);
            this.socket.send(packet);
                
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> packetToHashMap(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        HashMap<String, String> hash_map = new HashMap<String, String>();
            
        for(String aux : message.split(";")){
            String key_value[] = aux.split("\\|");
            
            hash_map.put(key_value[0].trim(), key_value[1].trim());
        }
        
        //System.out.println("String:\n\t" + message + "\nto HashMap:\n\t" + hash_map);
        return hash_map;
    }

    private void findPersonByUsernameOrCC(Scanner in){

        System.out.print("\nCC number or username: ");
        String aux = in.nextLine();
        Pair<String,String> user = null;
        try {
            user = db.getUser(aux);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (user == null){
            System.out.println("Couldn't find user with CC number or username: " + aux);
        }
        else{
            System.out.println(user);
        }
    }

    private void unlockVotingTerminal(){
        //TODO: argument = personId

    }
}
