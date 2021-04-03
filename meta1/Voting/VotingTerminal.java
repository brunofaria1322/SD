package Voting;

import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class VotingTerminal extends Thread {
    private String MULTICAST_ADDRESS;
    private int PORT;
    private int id = -1;

    public static void main(String[] args) {
        VotingTerminal client = new VotingTerminal();
        client.start();
    }

    public VotingTerminal(){
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

    public void run() {
        MulticastSocket socket = null;
        VotingInterface it = null;
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            it = new VotingInterface(group, PORT);

            socket = new MulticastSocket(PORT);  // create socket and bind it
            socket.joinGroup(group);

            String message = "type | identification";
            it.sendMessage(message);

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            HashMap<String,String> hash_map;
            
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
            socket.setSoTimeout(0);

            it.id = this.id;

            System.out.println("I Have Id = "+ this.id);

            while (true) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = it.packetToHashMap(packet);

                switch (hash_map.get("type")) {
                    case "whosfree":
                        if (it.locked) {
                            it.sendMessage("type | imfree; id | " + this.id);
                        }
                        break;
                    case "whosthere":
                        it.sendMessage("type | imhere; id | " + this.id);
                        break;
                    case "work":
                        if (Integer.parseInt(hash_map.get("to")) == this.id) {
                            it.unlock(hash_map.get("cc"), hash_map.get("name"));
                        }
                        break;
                    case "login":
                        if (hash_map.get("to") != null && Integer.parseInt(hash_map.get("to")) == this.id) {
                            if(hash_map.get("status").equals("success")){
                                it.sendMessage("type | getElections; username | " + it.u_username + "; id | " + it.id);
                            }
                            else{
                                System.out.println("Wrong credentials on login! Try again");
                                it.sem.doSignal();
                            }
                        }
                        break;
                    case "electionsList":
                        if (hash_map.get("to") != null && Integer.parseInt(hash_map.get("to")) == this.id) {
                            hash_map.remove("type");
                            hash_map.remove("to");

                            if (hash_map.isEmpty()){
                                System.out.println("There are no elections for you to vote here");
                                it.lock();
                            }
                            else {
                                it.chooseElection(hash_map);
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
        }catch (SocketTimeoutException e) {
            System.out.println("Timeout: Haven't any recieved response from pooling station on id atribution");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
            System.out.println("Exiting...");
            it.leave();
        }
        return;
    }

    private void readConfig() throws FileNotFoundException, IOException{
        Properties prop = new Properties();

        prop.load(new FileInputStream("meta1/Voting/terminal.config"));
       
        this.MULTICAST_ADDRESS = prop.getProperty("terminal.MULTICAST_ADDRESS");
        this.PORT = Integer.parseInt(prop.getProperty("terminal.PORT"));
    }
}

class VotingInterface extends Thread{
    private MulticastSocket socket;
    private InetAddress group;
    private int PORT;
    private boolean running = true;
    private Scanner in;

    public boolean locked = true;
    public int id;
    public Semaphore sem;
    public String u_username;

    private String u_name;
    private String u_cc;

    public VotingInterface(InetAddress group, int PORT){
        this.group = group;
        this.PORT = PORT;
        this.in = new Scanner(System.in);
        this.sem = new Semaphore();
        this.start();
    }

    public void run() {
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)
            
            System.out.println("\nLOCKED!");
            while(this.running){
                if (this.locked){
                } else {
                    this.login();
                }
                sem.doWait();
            }
                
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.socket.close();
            this.in.close();
        }
    }

    public void unlock(String cc, String name){
        this.u_name = name;
        this.u_cc = cc;
        
        this.locked = false;
        System.out.println("\nUNLOCKED!");
        sem.doSignal();
    }

    public void lock(){
        this.locked = true;
        System.out.println("\nLOCKED!");
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

    public void leave(){
        this.running = false;
        
        sem.doSignal();
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

    private void login(){
        System.out.println("\nHi " + u_name + "\nPlease Log in");
        
        System.out.print("username: ");
        this.u_username = this.in.nextLine();

        System.out.print("password: ");
        String password = new String( System.console().readPassword() );

        this.sendMessage("type | login; username | " + this.u_username + "; password | " + password + "; cc | " + this.u_cc + "; id | " + this.id);
    }

    public void chooseElection(HashMap<String, String> elections) {
        System.out.println("\nChoose an election:");

        int i = 1;
        for (String name : elections.values()){
            System.out.println(i + " - " + name);
            i++;
        }

        int nelec;
        
        do{
            System.out.print("0 - Back\noption: ");

            nelec = this.in.nextInt();
            this.in.nextLine();
            
            if (nelec< 0 || nelec >= i){     
                System.out.println("Wrong option! Try Again...");
            }
            else if (nelec != 0){
                this.sendMessage("type | getLists; id | " + this.id + "; nelec | " + elections.keySet().toArray()[nelec-1]);;
                break;
            }
        }while (nelec!=0);
    }
}

class Semaphore {
    boolean waiting = true;

    public synchronized void doWait() throws InterruptedException {
        while (waiting) {
            wait();
        }
        waiting = !waiting;

    }


    public synchronized void doSignal() {
        waiting = !waiting;
        notify();
    }

}