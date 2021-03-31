package RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.Scanner;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;


public class AdminConsole {
    static database db;
    public static void main(String[] args) throws RemoteException {

        
        try {
			db= (database) LocateRegistry.getRegistry(1099).lookup("central");
		} catch (Exception e) {
			System.out.println("Exception in main: " + e);
			e.printStackTrace();
		}
        
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
                    createElection(in);
                    break;
                case 3:
                    chooseElection(in);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
        }while (option != 0);
        in.close();
    }

    private static boolean registerPerson(Scanner in) throws RemoteException{
        int type, ndep;
        String cargo = "all";
        boolean isValid = false;
        do{
            System.out.print("\n1 - Regist student\n2 - Regist professor\n3 - Regist employee\n0 - Abort\noption: ");

            type = in.nextInt();
            in.nextLine();
            switch (type){
                case 0:
                    return false;
                case 1:
                    cargo = "aluno";
                    isValid = true;
                    break;
                case 2:
                    cargo = "docente";
                    isValid = true;
                    break;
                case 3:
                    isValid = true;
                    cargo = "funcionario";
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
        }while (!isValid);

        HashMap<Integer,String> deps = db.getDepartments();

        isValid = false;
        System.out.println("\nSelect the user's department");
        do{
            int i = 1;
            for (Integer key : deps.keySet()){
               System.out.println(i + " - " + deps.get(key));
               i++;
            }
            System.out.print("0 - Abort\noption: ");
            ndep = in.nextInt();
            in.nextLine();
            
            if (ndep == 0){
                return false;
            }

            else if (ndep< 0 || ndep > deps.keySet().size()){     
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else{
                isValid=true;
                ndep = deps.keySet().toArray(new Integer[deps.keySet().size()])[ndep-1];
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
        Date cc_expiration_date = new Date(1970, 1, 1);
        do{
            System.out.print("\nCC expiration date (mm/yyyy): ");
            String aux = in.nextLine();
            try{
                cc_expiration_date = new Date(new SimpleDateFormat("MM/yyyy").parse(aux).getTime());
                isValid = true;
            }catch(Exception e){
                System.out.print(e);
                System.out.print("Invalid date! Date should be in format (mm/yyyy). Try Again...");
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

        db.createUser(cargo, ndep, name, address, phone_number, cc_number, cc_expiration_date, username, password);
        //DEBUG
        System.out.println(
            "\ntype: " + cargo +
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

    private static void createElection(Scanner in) throws RemoteException{
        int voters, ndep;
        boolean isValid = false;
        String cargos = new String();
        do{
            System.out.print("\nWho are the voters?\n1 - Students\n2 - Professors\n3 - Employees\n4 - Everyone\n0 - Abort\noption: ");

            voters = in.nextInt();
            in.nextLine();
            
            switch (voters){
                case 0:
                    return;
                case 1:
                    cargos = "aluno";
                    isValid = true;
                    break;
                case 2:
                    cargos = "docente";
                    isValid = true;
                    break;
                case 3:
                    cargos = "funcionario";
                    isValid = true;
                    break;
                case 4:
                    cargos = "all";
                    isValid = true;
                    break;
                default:
                    System.out.println("Wrong option! Try Again...");
                    break;
            }
        }while (!isValid);


        isValid = false;
        System.out.println("\nWhat are the voters' department?");
        do{
            HashMap<Integer,String> deps = db.getDepartments();
            int i = 1;
            for (Integer key : deps.keySet()){
               System.out.println(i + " - " + deps.get(key));
               i++;
            }
            System.out.println(i + " - all" );
            System.out.print("0 - Abort\noption: ");

            ndep = in.nextInt();
            in.nextLine();
            
            if (ndep == 0){
                return;
            }

            else if (ndep< 0 || ndep > deps.keySet().size()+1){     
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else{
                isValid=true;
                if(ndep < deps.keySet().size()){
                    ndep = deps.keySet().toArray(new Integer[deps.keySet().size()])[ndep-1];
                }
                else{
                    ndep = 0;
                }
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

        //DEBUG
        System.out.println(
            "\ntype: " + voters +
            "\nndep: " + ndep +
            "\ntitle: " + title +
            "\ndescription: " + description +
            "\nstart: " + start_time.toString() +
            "\nend: " + end_time.toString()
        );


        db.createElection(cargos, ndep + ";", ndep + ";", title, description, start_time, end_time);

        //TODO: if registado com sucesso
            //TODO: maanage election em causa.
            manageElection(in);
        
    }

    private static void chooseElection(Scanner in){

        //TODO: getElections
        
        int nelec;
        System.out.println("\nbla bla bla... selecionar eleição");
        do{
            System.out.print("0 - Back\noption: ");

            nelec = in.nextInt();
            in.nextLine();

            if (nelec< 0 || nelec > 19){     //TODO: nelec > num de eleições
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nelec>0) {
                //TODO: manageElection(in, )
                manageElection(in);
            }
        }while (nelec!=0);

    }

    private static void manageElection(Scanner in){

        int option;
        //TODO: (if now < eleição.start)
            do{
                System.out.print("Election hasn't start yet\n1 - Manage candidate list\n2 - Manage polling stations\n3 - Change properties\n0 - Back\noption: ");

                option = in.nextInt();
                in.nextLine();

                switch (option){
                    case 0:
                        break;
                    case 1:
                        //TODO: manageCandidateLists(in, election_id);
                        manageCandidateLists(in);
                        break;
                    case 2:
                        //TODO: managePollingStations(in, election_id);
                        managePollingStations(in);
                        break;
                    case 3:
                        //TODO: changeProperties(in, election_id);
                        changeProperties(in);
                        break;
                    default:
                        System.out.println("Wrong option!");
                        break;
                }                
            }while (option!=0);
        
            
        //TODO: (if now > eleição.end)
            do{
                System.out.print("Election has finished \n1 - Check Results\n0 - Back\noption: ");

                option = in.nextInt();
                in.nextLine();

                switch (option){
                    case 0:
                        break;
                    case 1:
                        //TODO: checkResults(in, election_id);
                        checkResults(in);
                        break;
                    default:
                        System.out.println("Wrong option!");
                        break;
                }                
            }while (option!=0);

        //TODO: else
            do{
                System.out.print("Election is currently active\n1 - Manage polling stations\n0 - Back\noption: ");

                option = in.nextInt();
                in.nextLine();

                switch (option){
                    case 0:
                        break;
                    case 1:
                        //TODO: managePollingStations(in, election_id);
                        managePollingStations(in);
                        break;
                    default:
                        System.out.println("Wrong option!");
                        break;
                }                
            }while (option!=0);
    }
    
    private static void manageCandidateLists(Scanner in){

        int option;
        do{
            System.out.print("1 - Add candidate list\n2 - Remove candidate list\n3 - Manage candidate list\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    //TODO: manageCandidates(in, election_id);
                    addCandidateList(in);
                    break;
                case 2:
                    //TODO: removeCandidateList(in, election_id);
                    removeCandidateList(in);
                    break;
                case 3:
                    //TODO: manageCandidates(in, election_id);
                    manageCandidates(in);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
            
            
        }while (option!=0);
    }

    private static void addCandidateList(Scanner in){

        System.out.print("\nList Name: ");
        String name = in.nextLine();

        //TODO: criar Lista
        
        manageCandidates(in);

    }

    private static void removeCandidateList(Scanner in){

        //TODO: getList
        
        int nlist;
        System.out.println("\nbla bla bla... selecionar lista");
        do{
            System.out.print("0 - Back\noption: ");

            nlist = in.nextInt();
            in.nextLine();

            if (nlist< 0 || nlist > 19){     //TODO: nmember > num de membros
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nlist>0) {
                //TODO: Remover Membro
            }
        }while (nlist!=0);

    }

    private static void manageCandidates(Scanner in){
        int option;
        do{
            System.out.print("1 - Add candidate\n2 - Remove candidate\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    //TODO: addCandidate(in, list_id);
                    addCandidate(in);
                    break;
                case 2:
                    //TODO: removeCandidate(in, list_id);
                    removeCandidate(in);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
            
        }while (option!=0);
    }

    private static void addCandidate(Scanner in){

        System.out.print("\nCC number: ");
        String cc_number = in.nextLine();

        System.out.print("\nName: ");
        String name = in.nextLine();

        //TODO: adicionar Membro
    }

    private static void removeCandidate(Scanner in){

        //TODO: getMembers
        
        int nmember;
        System.out.println("\nbla bla bla... selecionar membro");
        do{
            System.out.print("0 - Back\noption: ");

            nmember = in.nextInt();
            in.nextLine();

            if (nmember< 0 || nmember > 19){     //TODO: nmember > num de membros
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nmember>0) {
                //TODO: Remover Membro
            }
        }while (nmember!=0);

    }

    private static void managePollingStations(Scanner in){
        int option;
        do{
            System.out.print("1 - Add pooling station\n2 - Remove pooling station\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    //TODO: addPollingStation(in, list_id);
                    addPollingStation(in);
                    break;
                case 2:
                    //TODO: removePollingStation(in, list_id);
                    removePollingStation(in);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
            
        }while (option!=0);

    }

    private static void addPollingStation(Scanner in){
        
        //TODO: ISTO

    }

    private static void removePollingStation(Scanner in){
        
        //TODO: ISTO

    }

    private static void changeProperties(Scanner in){
        
        boolean isValid  = false;
        int option;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        //TODO: bla bla ir buscar os dados
        String title = "titulo";
        String description = "decrição";
        LocalDateTime start_time = LocalDateTime.now();
        LocalDateTime end_time = LocalDateTime.now().plusDays(1);

        do{
            System.out.println(
            "\ntitle: " + title +
            "\ndescription: " + description +
            "\nstart: " + start_time.toString() +
            "\nend: " + end_time.toString()
            );

            System.out.print("1 - Change Title\n2 - Change Description\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    System.out.print("\nNew election title: ");
                    title = in.nextLine();
                    //TODO: comunicar com o sv
                    break;
                case 2:
                    System.out.print("\nElection description: ");
                    description = in.nextLine();
                    //TODO: comunicar com o sv
                    break;
                case 3:
                    isValid = false;
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
                    //TODO: comunicar com o sv
                    break;
                case 4:
                    isValid = false;
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
                    //TODO: comunicar com o sv
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }

        }while(option != 0);

    }

    private static void checkResults(Scanner in){
        //TODO: BLA BLA printar os resultados
        
        System.out.println("Press enter to continue...");
        in.nextLine();
    }

}
