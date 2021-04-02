package Voting;

import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

public class VotingTerminal extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private int id = -1;

    public static void main(String[] args) {
        VotingTerminal client = new VotingTerminal();
        client.start();
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
                            System.out.println("Im free");
                            it.sendMessage("type | imfree; id | " + this.id);
                        }
                        break;
                    case "work":
                        if (Integer.parseInt(hash_map.get("to")) == this.id) {
                            it.unlock(hash_map.get("cc"), hash_map.get("name"));
                        }
                        break;
                    case "login":
                        if (Integer.parseInt(hash_map.get("to")) == this.id) {
                            if(hash_map.get("to").equals("success")){
                                //TODO: eleiçoes
                            }
                            else{
                                System.out.println("Wrong credentials on login! Try again");
                                it.sem.doSignal();
                            }
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
        }catch (SocketTimeoutException e) {
            System.out.println("Timeout: Haven't any recieved response from pooling station on id atribution");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
            System.out.println("Exiting...");
            it.leave();
            return;
        }
    }
}

class VotingInterface extends Thread{
    private MulticastSocket socket;
    private InetAddress group;
    private int PORT;
    private boolean running = true;

    public boolean locked = true;
    public int id;
    public Semaphore sem = new Semaphore();

    private String u_name;
    private String u_cc;

    public VotingInterface(InetAddress group, int PORT){
        this.group = group;
        this.PORT = PORT;
        this.start();
    }

    public void run() {
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)
 
            Scanner in = new Scanner(System.in);

            while(this.running){
                if (this.locked){
                    System.out.println("LOCKED!");
                } else {
                    System.out.println("UNLOCKED!");
                    this.login(in);
                }
                sem.doWait();
            }

                
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public void unlock(String cc, String name){
        this.u_name = name;
        this.u_cc = cc;
        
        this.locked = false;
        sem.doSignal();
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

    public void login(Scanner in){
        System.out.println("Hi " + u_name + "\nPlease Log in");
        
        System.out.print("username: ");
        String username = in.nextLine();

        System.out.print("password: ");
        String password = new String( System.console().readPassword() );

        this.sendMessage("type | login; username | " + username + "; password | " + password + "; cc | " + this.u_cc + "; id | " + this.id);
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