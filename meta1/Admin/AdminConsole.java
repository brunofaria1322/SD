import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;

public class AdminConsole {
    public static void main(String[] args) {

        /*
        try {
			Bla bla = (Bla) LocateRegistry.getRegistry(1099).lookup("central");
		} catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
		}
        */
        System.out.println("Welcome to Admin Console");

        int option;
        Scanner in = new Scanner(System.in);
        do{
            System.out.print("\n1 - Regist person\n2 - Create election\n3 - Manage election\n0 - Quit\noption: ");

            option = in.nextInt();
            in.nextLine();
            
            switch (option){
                case 0:
                    System.out.println("Bye!");
                    break;
                case 1:
                    if (registerPerson(in)){
                        System.out.println("Person successfully registered!");
                    } else {
                        System.out.println("Registry was aborted!");
                    }
                    break;
                case 2:
                    if (createElection(in)){
                        System.out.println("Election successfully created!");
                    } else {
                        System.out.println("Election creation was aborted!");
                    }
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

    private static boolean registerPerson(Scanner in){
        int type, ndep;
        boolean isValid = false;
        do{
            System.out.print("\n1 - Regist student\n2 - Regist teacher\n3 - Regist employee\n0 - Abort\noption: ");

            type = in.nextInt();
            in.nextLine();
            
            switch (type){
                case 0:
                    return false;
                case 1:
                case 2:
                case 3:
                    isValid = true;
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
        }while (!isValid);

        //TODO: escolher departamento da BD

        isValid = false;
        System.out.println("\nbla bla bla... selecionar o departamento a que pertence");
        do{
            System.out.print("0 - Abort\noption: ");

            ndep = in.nextInt();
            in.nextLine();
            
            if (ndep == 0){
                return false;
            }

            else if (ndep< 0 || ndep > 19){     //TODO: dep > num de departamentos
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else{
                isValid=true;
            }
        }while (!isValid);

        System.out.print("\nName: ");
        String name = in.nextLine();

        System.out.print("\nAddress: ");
        String address = in.nextLine();

        System.out.print("\nPhone number: ");
        String phone_number = in.nextLine();

        System.out.print("\nCC number: ");
        String cc_number = in.nextLine();

        isValid=false;
        Date cc_expiration_date = new Date();
        do{
            System.out.print("\nCC expiration date (dd/mm/yyyy): ");
            String aux = in.nextLine();
            try{
                cc_expiration_date = new SimpleDateFormat("dd/MM/yyyy").parse(aux);
                isValid = true;
            }catch(Exception e){
                System.out.print("Invalid date! Date should be in format (dd/mm/yyyy). Try Again...");
            }

        }while(!isValid);

        System.out.print("\nUsername: ");
        String username = in.nextLine();

        isValid = false;
        String password;
        do{ 
            System.out.print("\nPassword: ");
            password = in.nextLine();
            if(password.length()>=4){
                System.out.print("Confirm password: ");
                String pv = in.nextLine();
                if (password.equals(pv)){
                    isValid=true;
                }else{
                    System.out.print("\nPasswords dont match! Try Again...");
                }
            }else{
                System.out.print("\nPassword requires a minimum of 4 characters! Try Again...");
            }
        }while(!isValid);


        //TODO:contactar servidor

        //DEBUG
        System.out.println(
            "\ntype: " + type +
            "\nndep: " + ndep +
            "\nname: " + name +
            "\naddress: " + address +
            "\nPhone no: " + phone_number +
            "\nCC: " + cc_number +
            "\nCC val: " + cc_expiration_date.toString() +
            "\nusername: " + username +
            "\npassword: " + password
        );
        return true;
        
    }

    private static boolean createElection(Scanner in){
        int voters, ndep;
        boolean isValid = false;
        do{
            System.out.print("\nWho are the voters?\n1 - Students\n2 - Teachers\n3 - Employees\n0 - Abort\noption: ");

            voters = in.nextInt();
            in.nextLine();
            
            switch (voters){
                case 0:
                    return false;
                case 1:
                case 2:
                case 3:
                    isValid = true;
                    break;
                default:
                    System.out.println("Wrong option! Try Again...");
                    break;
            }
        }while (!isValid);

        //TODO: escolher departamento da BD

        isValid = false;
        System.out.println("\nbla bla bla... selecionar o departamento a que pertence");
        do{
            System.out.print("0 - Abort\noption: ");

            ndep = in.nextInt();
            in.nextLine();
            
            if (ndep == 0){
                return false;
            }

            else if (ndep< 0 || ndep > 19){     //TODO: dep > num de departamentos
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else{
                isValid=true;
            }
        }while (!isValid);

        System.out.print("\nElection title: ");
        String title = in.nextLine();

        System.out.print("\nElection description: ");
        String description = in.nextLine();

        isValid=false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime start_time = LocalDateTime.now();
        do{
            System.out.print("\nElection start (dd/MM/yyyy HH:mm): ");
            String aux = in.nextLine();
            try{
                start_time = LocalDateTime.parse(aux, formatter);
                if(start_time.isAfter(LocalDateTime.now())){
                    isValid = true;
                }else{
                    System.out.print("Start date and time already passed. Try Again...");
                }
            }catch(Exception e){
                System.out.print("Invalid date and time! Try Again...");
            }

        }while(!isValid);

        isValid=false;
        LocalDateTime end_time = LocalDateTime.now();
        do{
            System.out.print("\nElection end (dd/MM/yyyy HH:mm): ");
            String aux = in.nextLine();
            try{
                end_time = LocalDateTime.parse(aux, formatter);

                if(end_time.isAfter(start_time)){
                    isValid = true;
                }else{
                    System.out.print("End needs to be after start time! Try Again...");
                }

            }catch(Exception e){
                System.out.print("Invalid date and time! Try Again...");
            }

        }while(!isValid);


        //TODO:contactar servidor

        //DEBUG
        System.out.println(
            "\ntype: " + voters +
            "\nndep: " + ndep +
            "\ntitle: " + title +
            "\ndescription: " + description +
            "\nstart: " + start_time.toString() +
            "\nend: " + end_time.toString()
        );
        return true;
        
    }
}
