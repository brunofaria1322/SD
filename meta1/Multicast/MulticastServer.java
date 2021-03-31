import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Scanner;
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

        HashMap<Integer,Boolean> terminals = new HashMap<Integer,Boolean>();    // terminal_id : isFree
        
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

    public PollingStationInterface(InetAddress group, int PORT){
        this.group = group;
        this.PORT = PORT;
        this.start();
    }

    public void run() {
        try {
            this.socket = new MulticastSocket();  // create socket without binding it (only for sending)

            int option;
            Scanner in = new Scanner(System.in);
            do{
                System.out.print("\n1 - Find Person by CC number\n2 - Find Person by name\noption: ");

                option = in.nextInt();
                in.nextLine();
                
                switch (option){
                    case 0:
                        break;
                    case 1:
                        findPersonByCC(in);
                        break;
                    case 2:
                        findPersonByName(in);
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

    private void findPersonByCC(Scanner in){

        System.out.print("\nCC number: ");
        String cc_number = in.nextLine();

        //TODO: Buscar por CC
    }

    private void findPersonByName(Scanner in){

        System.out.print("\nName: ");
        String name = in.nextLine();

        //TODO: Buscar por Name
    }

    private void unlockVotingTerminal(){
        //TODO: argument = personId

    }
}
