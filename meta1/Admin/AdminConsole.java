import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class AdminConsole {
    public static void main(String[] args) {
        
        /*
        try {
			Bla bla = (Bla) LocateRegistry.getRegistry(7000).lookup("benfica");
		} catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
		}
        */
        int option = 0;
        Scanner in = new Scanner(System.in);
        do{
            System.out.println("Welcome to Admin Console\n1 - Regist person\n2 - Create election\n3 - Manage election\n0 - Quit");

            option = in.nextInt();
            in.nextLine();
            
            switch (option){
                case 0:
                    System.out.println("Option 0!");
                    break;
                case 1:
                    System.out.println("Option 1!");
                    break;
                case 2:
                    System.out.println("Option 2!");
                    break;
                case 3:
                    System.out.println("Option 3!");
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
        }while (option != 0);
        in.close();
    }
}
