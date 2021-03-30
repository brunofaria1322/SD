import java.net.MulticastSocket;
import java.util.HashMap;
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
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            VotingInterface it = new VotingInterface(group, PORT);          //TODO: apenas criar quando desbloqueado

            socket = new MulticastSocket(PORT);  // create socket and bind it
            socket.joinGroup(group);

            String message = "type | identification";
            it.sendMessage(message);

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            HashMap<String,String> hash_map;
            
            while (this.id == -1) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                hash_map = it.packetToHashMap(packet);
                if(hash_map.get("type").equals("identification") && hash_map.get("id") != null){
                    this.id = Integer.parseInt(hash_map.get("id"));
                }
            }

            System.out.println("I Have Id = "+ this.id);

            while (true) {
                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}

class VotingInterface extends Thread{
    private MulticastSocket socket;
    private InetAddress group;
    private int PORT;

    public VotingInterface(InetAddress group, int PORT){
        this.group = group;
        this.PORT = PORT;
        this.start();
    }

    public void run() {
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)

            while(true){
                try { sleep((long) 10000); } catch (InterruptedException e) { }
            }

            //TODO:

                
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public void unlock(){
        //TODO: This
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
}