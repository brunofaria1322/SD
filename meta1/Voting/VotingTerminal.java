package Voting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;


/**
 * Main Class for the Voting Terminal
 *
 * @author Bruno Faria
 * @version 1.0
 */
public class VotingTerminal extends Thread {

    /**
     * The Multicast address
     */
    private String MULTICAST_ADDRESS;

    /**
     * The Multicast port
     */
    private int PORT;

    /**
     * Terminal id
     */
    private int id = -1;

    /**
     * Main Function
     *
     * @param args      Arguments
     */
    public static void main(String[] args) {
        VotingTerminal client = new VotingTerminal();
        client.start();
    }

    /**
     * Constructor for the Voting Terminal
     */
    public VotingTerminal(){
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
     */
    public void run() {
        MulticastSocket socket = null;
        VotingInterface it = null;
        try {
            //connects to the multicast
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            //creation of interface thread
            it = new VotingInterface(group, PORT);

            socket = new MulticastSocket(PORT);     // create socket and bind it
            socket.joinGroup(group);                // joins the group

            // Sends message to Polling Station requesting an id
            String message = "type | identification";
            it.sendMessage(message);

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            HashMap<String,String> hash_map;

            //Timout of 1 sec while waiting for id
            socket.setSoTimeout(1000);
            while (this.id == -1) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = it.packetToHashMap(packet);
                if(hash_map.get("type").equals("identification") && hash_map.get("id") != null){
                    this.id = Integer.parseInt(hash_map.get("id"));
                }
            }
            //resets timeout
            socket.setSoTimeout(0);

            it.id = this.id;

            System.out.println("I Have Id = "+ this.id);

            // Starts to listen
            while (true) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = it.packetToHashMap(packet);

                switch (hash_map.get("type")) {
                    // when polling stations asks who's free to work
                    case "whosfree":
                        if (it.locked) {
                            it.sendMessage("type | imfree; id | " + this.id);
                        }
                        break;

                    // when Polling Station asks who's connected to the multicast group
                    case "whosthere":
                        it.sendMessage("type | imhere; id | " + this.id);
                        break;

                    // Sends this terminal to work
                    case "work":
                        if (Integer.parseInt(hash_map.get("to")) == this.id) {
                            //unlocks terminal
                            it.unlock(hash_map.get("cc"), hash_map.get("name"));
                        }
                        break;

                    // response to login request
                    case "login":
                        if (hash_map.get("to") != null && Integer.parseInt(hash_map.get("to")) == this.id) {
                            if(hash_map.get("status").equals("success")){
                                // asks for elections
                                it.sendMessage("type | getElections; username | " + it.u_username + "; id | " + it.id);
                            }
                            else{
                                System.out.println("Wrong credentials on login! Try again");
                                it.sem.doSignal();
                            }
                        }
                        break;

                    // response to getElections (list of elections)
                    case "electionsList":
                        if (hash_map.get("to") != null && Integer.parseInt(hash_map.get("to")) == this.id) {
                            hash_map.remove("type");
                            hash_map.remove("to");

                            //after removes hash_map will only have the elections {elec_num : elec_name}
                            if (hash_map.isEmpty()){
                                System.out.println("There are no elections for you to vote here");
                                it.lock();
                            }
                            else {

                                it.chooseElection(hash_map);
                            }
                        }
                        break;

                    // response to getCandidates
                    case "candidatsList":
                        if (hash_map.get("to") != null && Integer.parseInt(hash_map.get("to")) == this.id) {
                            hash_map.remove("type");
                            hash_map.remove("to");
                            String nelec = hash_map.get("nelec");
                            hash_map.remove("nelec");

                            //after removes hash_map will only have the candidates {c_num : c_name}

                            it.vote(hash_map, nelec);
                            
                        }
                        break;

                    // Response to vote request
                    case "vote":
                        if (hash_map.get("to") != null && Integer.parseInt(hash_map.get("to")) == this.id) {
                            if(hash_map.get("status").equals("success")){
                                System.out.println("Vote sent successfully!");
                            }
                            else{
                                System.out.println("Vote was rejected! Cause: " + hash_map.get("msg"));
                            }
                            //asks for elections again (to vote on others)
                            it.sendMessage("type | getElections; username | " + it.u_username + "; id | " + it.id);
                            
                        }
                        break;

                    // Messages that are not for Voting Terminal
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
        }catch (SocketTimeoutException e) {
            //Time Out when asking Polling station for an id
            System.out.println("Timeout: Haven't any received response from pooling station on id attribution");
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        } finally {
            if (socket != null){
                socket.close();
            }
            System.out.println("Exiting...");
            if(it != null){
                it.leave();
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

        prop.load(new FileInputStream("meta1/Voting/terminal.config"));

        //reads the variables
        this.MULTICAST_ADDRESS = prop.getProperty("terminal.MULTICAST_ADDRESS");
        this.PORT = Integer.parseInt(prop.getProperty("terminal.PORT"));
    }
}

/**
 * The Voting Terminal Interface
 *
 * @author Bruno Faria
 * @version 1.0
 */
class VotingInterface extends Thread{

    /**
     * The Multicast socket
     */
    private MulticastSocket socket;

    /**
     * The Multicast group
     */
    private final InetAddress group;

    /**
     * The Multicast Port
     */
    private final int PORT;

    /**
     * Is Terminal running
     */
    private boolean running = true;

    /**
     * Input Scanner
     */
    private final Scanner in;


    /**
     * Is Terminal Locked
     */
    public boolean locked = true;

    /**
     * Terminal ID
     */
    public int id;

    /**
     * Semaphore
     */
    public Semaphore sem;

    /**
     * Username of current user
     */
    public String u_username;


    /**
     * Name of current user
     */
    private String u_name;

    /**
     * CC number of current user
     */
    private String u_cc;

    /**
     * Constructor for the Voting Terminal Interface
     *
     * @param group     the Multicast group
     * @param PORT      the Multicast Port
     */
    public VotingInterface(InetAddress group, int PORT){
        this.group = group;
        this.PORT = PORT;
        this.in = new Scanner(System.in);
        this.sem = new Semaphore();
        this.start();
    }

    /**
     * Run Function for the Thread start
     */
    public void run() {
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)
            
            System.out.println("\nLOCKED!");
            while(this.running){
                if (!this.locked){
                    this.login();
                }
                //waits for signal
                sem.doWait();
            }
                
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        } finally {
            this.socket.close();
            this.in.close();
        }
    }

    /**
     * Unlocks terminal
     *
     * @param cc        CC number
     * @param name      User name
     */
    public void unlock(String cc, String name){
        this.u_name = name;
        this.u_cc = cc;
        
        this.locked = false;
        System.out.println("\nUNLOCKED!");

        sem.doSignal();
    }

    /**
     * Locks terminal
     */
    public void lock(){
        this.locked = true;
        System.out.println("\nLOCKED!");
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
     * Makes Thread leave while loop
     */
    public void leave(){
        this.running = false;
        
        sem.doSignal();
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
     * Reads login credentials from input and sends it to the Multicast group
     */
    private void login(){
        System.out.println("\nHi " + u_name + "\nPlease Log in");
        
        System.out.print("username: ");
        this.u_username = this.in.nextLine();

        System.out.print("password: ");
        String password = new String( System.console().readPassword() );

        this.sendMessage("type | login; username | " + this.u_username + "; password | " + password + "; cc | " + this.u_cc + "; id | " + this.id);
    }

    /**
     * Prints the Elections, asks for user to pick one and sends a request for
     * his candidate lists to the Multicast group
     *
     * @param elections HashMap containing number of election : election name
     */
    public void chooseElection(HashMap<String, String> elections) {
        System.out.println("\nChoose an election:");

        int nelec;
        
        do{
            int i = 1;
            //prints election list
            for (String name : elections.values()){
                System.out.println(i + " - " + name);
                i++;
            }

            System.out.print("0 - Exit\noption: ");

            nelec = this.in.nextInt();
            this.in.nextLine();
            
            if(nelec == 0){
                this.lock();
            }
            else if (nelec< 0 || nelec >= i){     
                System.out.println("Wrong option! Try Again...\n");
            }
            else {
                // Asks for the Candidates Lists in this election
                this.sendMessage("type | getLists; id | " + this.id + "; nelec | " + elections.keySet().toArray()[nelec-1]);
                break;
            }
        }while (nelec!=0);
    }

    /**
     * Prints and asks for vote option and sends it to the Multicast group
     *
     * @param lists HashMap containing number of list : list name
     * @param nelec Number of election
     */
    public void vote(HashMap<String, String> lists, String nelec) {
        System.out.println("\nChoose a list:");

        int nlist;
        
        do{  
            int i = 1;
            // prints voting options
            for (String name : lists.values()){
                if(name.equals("votos nulos")){
                    System.out.println(i + " - Voto Nulo");
                }
                else if(name.equals("votos em branco")){
                    System.out.println(i + " - Voto Branco");
                }
                else{
                    System.out.println(i + " - " + name);
                }
                i++;
            }

            System.out.print("0 - Back\noption: ");

            nlist = this.in.nextInt();
            this.in.nextLine();
            
            if(nlist == 0){
                this.sendMessage("type | getElections; username | " + this.u_username + "; id | " + this.id);
            }
            else if (nlist< 0 || nlist >= i){     
                System.out.println("Wrong option! Try Again...\n");
            }
            else{
                // Sends vote to the Multicast group
                this.sendMessage("type | vote; id | " + this.id + "; nlista | " + lists.keySet().toArray()[nlist-1] + "; username | " + this.u_username + "; neleicao | " + nelec);
                break;
            }
        }while (nlist!=0);
    }
}

/**
 * Semaphore for Voting Terminal Interface
 *
 * @author Bruno Faria
 * @version 1.0
 */
class Semaphore {
    /**
     * boolean for semaphore state
     */
    boolean waiting = true;

    /**
     * Waits for signal to continue
     *
     * @throws InterruptedException if thread gets interrupted while waiting
     */
    public synchronized void doWait() throws InterruptedException {
        while (waiting) {
            wait();
        }
        waiting = true;

    }


    /**
     * Signal to continue
     */
    public synchronized void doSignal() {
        waiting = false;
        notify();
    }
}