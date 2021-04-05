package Multicast;

import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import Commun.database;
import Commun.database.Pair;


/**
 * Main Class for the Polling Station
 *
 * @author Bruno Faria
 * @version 1.0
 */
public class MulticastServer extends Thread {

    /**
     * The Multicast address
     */
    private String MULTICAST_ADDRESS;

    /**
     * The Multicast port
     */
    private int PORT;

    /**
     * Number of department of this Polling Station
     */
    private String NDEP;

    /**
     * Main Function
     *
     * @param args      Arguments
     */
    public static void main(String[] args) {

        MulticastServer server = new MulticastServer();
        server.start();
    }

    /**
     * Constructor for the Polling Station
     * <p>
     * Will try to read the configuration file
     */
    public MulticastServer() {
        try {
            //reads config file
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
     * Run Function for the Thread start
     * <p>
     * Will start by trying to connect to the RMI server and then to the Multicast, then it will start the
     * interface thread, passing as arguments the group, port, RMI connector and number of department. After that it will try to
     * keep up with the terminal ids that ate already in the group (this oly happens when Polling Station had a problem
     * and had to be rebooted) and if it receives a message from a Polling Station it will exit. after going through
     * all the mentioned the main thread will start to listen to the group and responding to the messages that are for him.
     */
    public void run() {
        MulticastSocket socket = null;
        int ids = 0;

        database db;
        try {
            //connects to rmi server
			db = (database) LocateRegistry.getRegistry(1099).lookup("central");
		} catch (Exception e) {
			System.out.println("Couldn't connect to RMI server\nExiting...");
            return;
		}
        
        System.out.println("Polling station of dep no. "+this.NDEP+" is running...");

        try {
            //connects to the multicast
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            //creation of interface thread
            PollingStationInterface it = new PollingStationInterface(group, this.PORT, db, this.NDEP);

            socket = new MulticastSocket(this.PORT);    // create socket and bind it
            socket.joinGroup(group);                    // joins the group

            /* Sends message to Multicast Group to keep up with ids and
                to check if there is no other Polling Station on this group */
            String message = "type | whosthere";
            it.sendMessage(message);

            byte[] buffer;
            DatagramPacket packet;

            HashMap<String,String> hash_map;
            
            /* In case Polling Station needed restart it will continue the ids
                based on the biggest id among the terminals connected
               If there is already a Polling Station on this group this one will leave
            */
            try {
                socket.setSoTimeout(1000);
                while (true) {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    hash_map = it.packetToHashMap(packet);

                    if(hash_map.get("type").equals("imhere")){
                        if (hash_map.get("id") != null){
                            //Response from a Voting Terminal
                            int temp_id = Integer.parseInt(hash_map.get("id"));
                            if (temp_id >= ids) {
                                ids = temp_id+1;
                            }
                        }
                        else if(hash_map.get("ndep") != null){
                            //Response from a Polling Station
                            System.out.println("There is already a Polling Station in this group\nLeaving...");
                            it.interrupt();
                            System.exit(0);
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                //No more requests to catch
                socket.setSoTimeout(0);
            }

            while (true) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = it.packetToHashMap(packet);
                
                switch (hash_map.get("type")) {
                    // when Polling Station asks who's connected to the multicast group
                    case "whosthere":
                        it.sendMessage("type | imhere; ndep | " + this.NDEP);
                        break;

                    // when Voting Terminal asks for an ID
                    case "identification":
                        if(hash_map.get("id") == null){
                            it.sendMessage("type | identification; id | " + ids++);
                        }
                        break;

                    

                    // when Voting Terminal asks for an ID
                    case "updateStatus":
                        it.updateStatus(socket);
                        break;

                    // Sends response to login request
                    case "login":
                        if(hash_map.get("id") != null){
                            boolean res;

                            try {
                                res = db.login(hash_map.get("username"), hash_map.get("cc"), hash_map.get("password"));

                            } catch (RemoteException e) {
                                //tries to reconnect to RMI Server
                                db = it.reconnect(db);
                                res = db.login(hash_map.get("username"), hash_map.get("cc"), hash_map.get("password"));
                            }

                            if(res){
                                it.sendMessage("type | login; status | success; to | " + hash_map.get("id"));
                            } else{
                                it.sendMessage("type | login; status | unsuccess; to | " + hash_map.get("id"));
                            }
                        }
                        break;

                    /* Response to the request for the elections
                        sends the list of elections that the user is able to vote in the current department
                    */
                    case "getElections":
                        if(hash_map.get("id") != null && hash_map.get("username")!= null){

                            HashMap<Integer,HashMap<String,String>> elections;
                            try{
                                elections = db.getElections(hash_map.get("username"), this.NDEP);
                            }
                            catch(NullPointerException e){
                                elections = null;
                            } catch (RemoteException e) {
                                //tries to reconnect to RMI Server
                                db = it.reconnect(db);
                                elections = db.getElections(hash_map.get("username"), this.NDEP);
                            }

                            StringBuilder out = new StringBuilder("type | electionsList; to | " + hash_map.get("id"));
                            
                            if(elections != null){
                                for (Integer elec_id: elections.keySet()){
                                    out.append("; ").append(elec_id).append(" | ").append(elections.get(elec_id).get("titulo"));
                                }
                            }

                            it.sendMessage(out.toString());
                        }
                        break;

                    // Response to the request for the list of candidates
                    case "getLists":
                        if(hash_map.get("id") != null && hash_map.get("nelec")!= null){

                            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists;
                            try{
                                lists = db.getLists(Integer.parseInt(hash_map.get("nelec")));

                            } catch(NullPointerException e){
                                lists = null;

                            } catch (RemoteException e) {
                                //tries to reconnect to RMI Server
                                db = it.reconnect(db);
                                lists = db.getLists(Integer.parseInt(hash_map.get("nelec")));
                            }
                            
                            StringBuilder out = new StringBuilder("type | candidatsList; to | " + hash_map.get("id") + "; nelec | " + hash_map.get("nelec"));
                            
                            if(lists != null){
                                for (Integer list_id: lists.keySet()){
                                    out.append("; ").append(list_id).append(" | ").append(lists.get(list_id).left);
                                }
                            }

                            it.sendMessage(out.toString());
                        }
                        break;

                    // sends the response to the vote request
                    case "vote":
                        if(hash_map.get("id") != null){
                            int temp;

                            try {
                                temp = db.vote(hash_map.get("username"), Integer.parseInt(hash_map.get("neleicao")), Integer.parseInt(hash_map.get("nlista")), Integer.parseInt(this.NDEP));

                            } catch (RemoteException e) {
                                //tries to reconnect to RMI Server
                                db = it.reconnect(db);
                                temp = db.vote(hash_map.get("username"), Integer.parseInt(hash_map.get("neleicao")), Integer.parseInt(hash_map.get("nlista")), Integer.parseInt(this.NDEP));
                            }


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

                    // Messages that are not for Polling Station
                    default:

                        /*
                        DEBUG
                        System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                        message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println(message);
                        */

                        break;
                }
            }
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Reads the terminal.config file
     *
     * @throws FileNotFoundException    When file was not found
     * @throws IOException              When file content is not as expected
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

/**
 * The Polling Station Interface
 *
 * @author Bruno Faria
 * @version 1.0
 */
class PollingStationInterface extends Thread{

    /**
     * The Multicast socket
     */
    private MulticastSocket socket;

    /**
     * The Multicast socket for reading
     */
    private MulticastSocket read_socket;

    /**
     * The Multicast group
     */
    private final InetAddress group;

    /**
     * The Multicast Port
     */
    private final int PORT;

    /**
     * The RMI server connector
     */
    public database db;

    /**
     * The number of department of the Polling Station
     */
    private final String NDEP;

    /**
     * Constructor for the Polling Station Interface
     *
     * @param group     the Multicast group
     * @param PORT      the Multicast Port
     * @param db        the RMI Server connector
     * @param NDEP      the number of department of the Polling Station
     */
    public PollingStationInterface(InetAddress group, int PORT, database db, String NDEP){
        this.group = group;
        this.PORT = PORT;
        this.db = db;
        this.NDEP = NDEP;
        this.start();
    }

    /**
     * Run Function for the Thread start
     */
    public void run() {
        Scanner in = null;
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)
            this.read_socket = new MulticastSocket(this.PORT);   // create socket and bind it (only for reading)

            int option;
            in = new Scanner(System.in);
            do{
                System.out.println("\n1 - Find Person\n0 - exit");

                try{
                    option = in.nextInt();
                } catch (InputMismatchException e){
                    option = -1;
                }

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
        } catch (IOException e) {
            System.out.println("Couldn't connect to multicast!");
        } finally {
            if (in != null) {
                in.close();
            }
            System.out.println("Bye!");
            this.socket.close();
            this.read_socket.close();
            System.exit(0);
        }
    }

    /**
     * Sends message to Multicast group
     *
     * @param message   Message to be sent
     */
    public void sendMessage( String message ){
        try {
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.group, this.PORT);
            this.socket.send(packet);
                
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }
    }

    /**
     * Turns packet received from multicast group to a HashMap
     *
     * @param packet    packet received
     * @return          returns the HashMap
     */
    public HashMap<String, String> packetToHashMap(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        HashMap<String, String> hash_map = new HashMap<>();

        for(String aux : message.split(";")){
            String[] key_value = aux.split("\\|");
            
            hash_map.put(key_value[0].trim(), key_value[1].trim());
        }

        //System.out.println("String:\n\t" + message + "\nto HashMap:\n\t" + hash_map);
        return hash_map;
    }

    /**
     * Interface used to search for a person by CC number or username
     *
     * @param in    the input Scanner
     */
    private void findPersonByUsernameOrCC(Scanner in) throws RemoteException {

        System.out.print("\nCC number or username: ");
        String aux = in.nextLine();
        String[] user = null;
        try {
            user = db.getUser(aux);

        } catch (RemoteException e) {
            //tries to reconnect to RMI Server
            db = this.reconnect(db);
            user = db.getUser(aux);
        }

        if (user == null){
            System.out.println("Couldn't find user with CC number or username: " + aux);
        }
        else{

            HashMap<Integer,HashMap<String,String>> elections = null;
            try{
                elections = db.getElections(user[2], this.NDEP);

            } catch(NullPointerException ignored){
                //ignored because elections is already initialized as null

            } catch (RemoteException e) {
                //tries to reconnect to RMI Server
                db = this.reconnect(db);
                elections = db.getElections(user[2], this.NDEP);
            }

            //if there are no elections for this user to vote in the current Polling Station
            if(elections == null || elections.isEmpty()){
                System.out.println("There are no elections for "+ user[1] +" to vote here");
                return;
            }

            //asks for a free Voting Terminal
            System.out.println("Unlocking a terminal for " + user[1]);
            this.sendMessage("type | whosfree");

            try {
                //timeout for the request for a free Voting Terminal
                this.read_socket.setSoTimeout(1000);
                this.read_socket.joinGroup(group);

                byte[] buffer;
                DatagramPacket packet;

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
                this.sendMessage("type | updateStatus");
                System.out.println("Terminal id = "+id+" was ulocked!");

            } catch (SocketTimeoutException e) {
                //when there was no response to the request
                System.out.println("Timeout: Couldn't find any free terminals");
                try {
                    this.read_socket.setSoTimeout(0);
                    this.read_socket.leaveGroup(group);
                } catch (IOException e1) {
                    //TODO
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                //TODO
                e.printStackTrace();
            }
            
        }

    }


    /**
     * Tries to reconnect to db in the next 30s
     * If 30s has passed it will exit, turning off the Polling Station
     */
    public database reconnect(database db){
        long until = System.currentTimeMillis()+30000;
        
        while(System.currentTimeMillis() <= until){
            try {
                db= (database) LocateRegistry.getRegistry(1099).lookup("central");
                if(db.isWorking()){
                    return db;
                }
            } catch (Exception e) {
                //Couldn't connect
            }
        }
        System.out.println("Server is closed");
        System.exit(0);
        return null;
    }

    /**
     * Function for updating Voting Terminal Status
     * <p>
     * This function is called whenever a Voting Terminal is Locked or Unlocked and his purpose is to
     * update the Terminals status for the real time updated status in the admin console
     * This function starts by requesting the status for everyone in the Multicast group. Then, while receiving
     * responses, will increment to the locked or unlocked counters. After 1 second without any response it
     * will update the values on the RMI server side
     *
     * @param socket                    the Mulicast Socket for reading
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    public void updateStatus(MulticastSocket socket) throws RemoteException{
        /* Sends message to Multicast Group toget the states from all the 
        voting terminals on this group */
        String message = "type | whosthere";
        this.sendMessage(message);

        byte[] buffer;
        DatagramPacket packet;

        HashMap<String,String> hash_map;
        
        // Captures the messages from the Voting terminals receiving his id and status
        
        int availableTerminals = 0, lockedTerminals = 0;
        try {
            socket.setSoTimeout(1000);
            while (true) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = this.packetToHashMap(packet);

                if (hash_map.get("type").equals("imhere")) {
                    if (hash_map.get("id") != null) {
                        if (hash_map.get("status").equals("locked")) {
                            lockedTerminals++;
                        } else {
                            availableTerminals++;
                        }

                    }
                }
            }
        } catch (SocketTimeoutException ignored) {
            //No more requests to catch
            //leaves
        } catch (IOException e){
            //TODO
        }

        try {
            db.changeActiveStationStatus(Integer.parseInt(this.NDEP), availableTerminals, lockedTerminals);

        } catch (RemoteException e) {
            //tries to reconnect to RMI Server
            this.reconnect(db);
            db.changeActiveStationStatus(Integer.parseInt(this.NDEP), availableTerminals, lockedTerminals);
        }

    }
}
