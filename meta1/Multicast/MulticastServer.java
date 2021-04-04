package Multicast;

import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import Commun.database;
import Commun.database.Pair;

public class MulticastServer extends Thread {
    /**
     *
     */
    private String MULTICAST_ADDRESS;
    private int PORT;
    private String NDEP;
    private database db;

    /**
     * @param args
     */
    public static void main(String[] args) {

        MulticastServer server = new MulticastServer();
        server.start();
    }

    /**
     *
     */
    public MulticastServer() {
        try {
            readConfig();
        } catch (FileNotFoundException f) {
            System.out.println("Couldn't find config file");
            System.exit(-1);
        } catch (IOException f) {
            System.out.println("Couldn't read config file");
            System.exit(-1);
        }
    }

    /**
     *
     */
    public void run() {
        MulticastSocket socket = null;
        int ids = 0;
        
        try {
			this.db= (database) LocateRegistry.getRegistry(1099).lookup("central");
		} catch (Exception e) {
			System.out.println("Could't connect to RMI server\nExiting");
            return;
		}
        
        System.out.println("Polling station of dep no. "+this.NDEP+" is running...");

        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            PollingStationInterface it = new PollingStationInterface(group, this.PORT, this.db, this.NDEP);

            socket = new MulticastSocket(this.PORT);  // create socket and bind it
            socket.joinGroup(group);

            String message = "type | whosthere";
            it.sendMessage(message);

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            HashMap<String,String> hash_map;
            
            /*  
                In case pooling station needed restart it will continue the ids
                based on the biggest id among the terminals connected
            */
            try {
                socket.setSoTimeout(1000);
                while (true) {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    hash_map = it.packetToHashMap(packet);

                    if(hash_map.get("type").equals("imhere")){
                        int temp_id = Integer.parseInt(hash_map.get("id"));
                        if (temp_id >= ids) {
                            ids = temp_id+1;
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                socket.setSoTimeout(0);
            }

            while (true) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = it.packetToHashMap(packet);
                
                switch (hash_map.get("type")) {
                    case "identification":
                        if(hash_map.get("id") == null){
                            it.sendMessage("type | identification; id | " + ids++);
                        }
                        break;

                    case "login":
                        if(hash_map.get("id") != null){
                            if(db.login(hash_map.get("username"), hash_map.get("cc"), hash_map.get("password"))){
                                it.sendMessage("type | login; status | success; to | " + hash_map.get("id"));
                            } else{
                                it.sendMessage("type | login; status | unsuccess; to | " + hash_map.get("id"));
                            }
                        }
                        break;

                    case "getElections":
                        if(hash_map.get("id") != null && hash_map.get("username")!= null){

                            HashMap<Integer,HashMap<String,String>> elections;
                            try{
                                elections = db.getElections(hash_map.get("username"), this.NDEP);
                            }
                            catch(NullPointerException e){
                                elections = null;
                            }
                            String out = "type | electionsList; to | " + hash_map.get("id");
                            
                            if(elections != null){
                                for (Integer elec_id: elections.keySet()){
                                    out = out + "; "+elec_id+ " | " + elections.get(elec_id).get("titulo");
                                }
                            }

                            it.sendMessage(out);
                        }
                        break;

                    case "getLists":
                        if(hash_map.get("id") != null && hash_map.get("nelec")!= null){

                            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists;
                            try{
                                lists = db.getLists(Integer.parseInt(hash_map.get("nelec")));
                            }
                            catch(NullPointerException e){
                                lists = null;
                            }
                            
                            String out = "type | candidatsList; to | " + hash_map.get("id") + "; nelec | " + hash_map.get("nelec");
                            
                            if(lists != null){
                                for (Integer list_id: lists.keySet()){
                                    out = out + "; "+list_id+ " | " + lists.get(list_id).left;
                                }
                            }

                            it.sendMessage(out);
                        }
                        break;
                    
                    case "vote":
                        if(hash_map.get("id") != null){
                            int temp = db.vote(hash_map.get("username"), Integer.parseInt(hash_map.get("neleicao")), Integer.parseInt(hash_map.get("nlista")), Integer.parseInt(this.NDEP));
                            if(temp == 1){
                                it.sendMessage("type | vote; status | success; to | " + hash_map.get("id"));
                            } else{
                                String msg;
                                if (temp == -1){
                                    msg = "You have already voted in this election";
                                }
                                else if (temp == -2){
                                    msg = "Your account was registered after election start";
                                }
                                else if (temp == -3){
                                    msg = "Error connecting to database.";
                                }
                                else{
                                    msg = "Unknown";
                                }
                                it.sendMessage("type | vote; status | unsuccess; to | " + hash_map.get("id") + "; msg | "+msg);
                            }
                        }
                        break;

                    default:
                        //NOT FOR ME
                        /*
                        //DEBUG
                        System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                        message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println(message);
                        */
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    /**
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void readConfig() throws FileNotFoundException, IOException{
        Properties prop = new Properties();

        InputStream is = new FileInputStream("meta1/Multicast/station.config");
        prop.load(is);
       
        this.NDEP= prop.getProperty("station.NDEP");
        this.MULTICAST_ADDRESS = prop.getProperty("station.MULTICAST_ADDRESS");
        this.PORT = Integer.parseInt(prop.getProperty("station.PORT"));
    }
}

class PollingStationInterface extends Thread{
    /**
     *
     */
    private MulticastSocket socket;
    private MulticastSocket read_socket;
    private InetAddress group;
    private int PORT;
    public database db;
    private String NDEP;

    /**
     * @param group
     * @param PORT
     * @param db
     * @param NDEP
     */
    public PollingStationInterface(InetAddress group, int PORT, database db, String NDEP){
        this.group = group;
        this.PORT = PORT;
        this.db = db;
        this.NDEP = NDEP;
        this.start();
    }

    /**
     *
     */
    public void run() {
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)
            this.read_socket = new MulticastSocket(this.PORT);   // create socket and bind it

            int option;
            Scanner in = new Scanner(System.in);
            do{
                System.out.println("\n1 - Find Person\n0 - exit");

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

    /**
     * @param message
     */
    public void sendMessage( String message ){
        try {
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.group, this.PORT);
            this.socket.send(packet);
                
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param packet
     * @return
     */
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

    /**
     * @param in
     */
    private void findPersonByUsernameOrCC(Scanner in){

        System.out.print("\nCC number or username: ");
        String aux = in.nextLine();
        String[] user = null;
        try {
            user = db.getUser(aux);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (user == null){
            System.out.println("Couldn't find user with CC number or username: " + aux);
        }
        else{

            HashMap<Integer,HashMap<String,String>> elections = null;
            try{
                elections = db.getElections(user[2], this.NDEP);
            }
            catch(NullPointerException e){
                elections = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if(elections == null || elections.isEmpty()){
                System.out.println("There are no elections for "+ user[1] +" to vote here");
            }

            System.out.println("Unlocking a terminal for " + user[1]);
            this.sendMessage("type | whosfree");

            try {
                this.read_socket.setSoTimeout(1000);
                this.read_socket.joinGroup(group);

                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                HashMap<String,String> hash_map;

                int id = -1;

                while (id == -1) {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    this.read_socket.receive(packet);

                    hash_map = this.packetToHashMap(packet);
                    
                    if(hash_map.get("type").equals("imfree")){
                        id = Integer.parseInt(hash_map.get("id"));
                    }
                }
                this.read_socket.setSoTimeout(0);
                this.read_socket.leaveGroup(group);


                this.sendMessage("type | work; cc | "+ user[0] +"; name | "+ user[1] +"; to | "+ id);
                System.out.println("Terminal id = "+id+" was ulocked!");

            } catch (SocketTimeoutException e) {
                System.out.println("Timeout: Couldn't find any free terminals");
                try {
                    this.read_socket.setSoTimeout(0);
                    this.read_socket.leaveGroup(group);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }
}
