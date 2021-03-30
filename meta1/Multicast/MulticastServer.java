import java.net.MulticastSocket;
import java.util.HashMap;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

public class MulticastServer extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private String DEP = "NE";

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
        System.out.println(DEP + " Polling station running...");
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            PollingStationInterface it = new PollingStationInterface(group, PORT);

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

                //DEBUG
                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);

                hash_map = it.packetToHashMap(packet);
                
                switch (hash_map.get("type")) {
                    case "identification":
                        if(hash_map.get("id") == null){
                            it.sendMessage("type | identification; id | " + ids++);
                        }
                        break;
                
                    default:
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

    public PollingStationInterface(InetAddress group, int PORT){
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

            //TODO: AQUI
                
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
}
