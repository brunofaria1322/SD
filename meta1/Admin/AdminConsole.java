package Admin;

import java.io.Console;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

import Commun.database;


                
          

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
        new textAreaTest(db);

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
                    int rp = registerPerson(in);
                    if (rp == 1){
                        System.out.println("Person registered successfully !");
                    } else if(rp == -4){
                        System.out.println("Registry was aborted!");
                    }
                    else if (rp == -3){
                        System.out.println("Sorry, something went wrong with our server. Please contact us!");
                    }
                    else if(rp == -2){
                        System.out.println("There's already an account with that CC number!");
                    }
                    else{
                        System.out.println("There's already an account with that username!");
                    }
                    break;
                case 2:
                    int ce = createElection(in);
                    if(ce == 0){
                        System.out.println("Sorry, something went wrong with our server. Please contact us!");
                    }
                    else if(ce < 0){
                        System.out.println("Election was created successfully!");
                        System.out.println("WARNING: There was already an election with the same name as the new one's");
                        manageElection(in,-ce);
                    }
                    else if(ce > 0){
                        System.out.println("Election was created successfully!");
                        manageElection(in,ce);
                    }
                    else{
                        System.out.println("Creation was aborted!"); 
                    }
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

    private static int registerPerson(Scanner in) throws RemoteException{
        int type, ndep;
        String cargo = "all";
        boolean isValid = false;
        do{
            System.out.print("\n1 - Regist student\n2 - Regist professor\n3 - Regist employee\n0 - Abort\noption: ");

            type = in.nextInt();
            in.nextLine();
            switch (type){
                case 0:
                    return -4;
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
                return -4;
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
        if(name.equals("0")){
            return -4;
        }

        System.out.print("\nAddress: ");
        String address = in.nextLine();
        if(address.equals("0")){
            return -4;
        }
        System.out.print("\nPhone number: ");
        String phone_number = in.nextLine();
        if(phone_number.equals("0")){
            return -4;
        }
        System.out.print("\nCC number: ");
        String cc_number = in.nextLine();
        if(cc_number.equals("0")){
            return -4;
        }
        isValid=false;
        Date cc_expiration_date = new Date(1970, 1, 1);
        do{
            System.out.print("\nCC expiration date (mm/yyyy): ");
            String aux = in.nextLine();
            if(aux.equals("0")){
                return -4;
            }
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
        if(username.equals("0")){
            return -4;
        }

        isValid = false;
        String password;
        do{ 
            System.out.print("\nPassword: ");
            password = new String(System.console().readPassword());
            if(password.equals("0")){
                return -4;
            }
            if(password.length()>=4){
                System.out.print("Confirm password: ");
                String pv = new String(System.console().readPassword());
                if(pv.equals("0")){
                    return -4;
                }
                if (password.equals(pv)){
                    isValid=true;
                }else{
                    System.out.print("\nPasswords dont match! Try Again...");
                }
            }else{
                System.out.print("\nPassword requires a minimum of 4 characters! Try Again...");
            }
        }while(!isValid);
         //DEBUG
        /*System.out.println(
            "\ntype: " + cargo +
            "\nndep: " + ndep +
            "\nname: " + name +
            "\naddress: " + address +
            "\nPhone no: " + phone_number +
            "\nCC: " + cc_number +
            "\nCC val: " + cc_expiration_date.toString() +
            "\nusername: " + username +
            "\npassword: " + password
        );*/
        return db.createUser(cargo, ndep, name, address, phone_number, cc_number, cc_expiration_date, username, password);
       
        
        
    }

    private static int createElection(Scanner in) throws RemoteException{
        int voters, ndep;
        boolean isValid = false;
        String cargos = new String();
        do{
            System.out.print("\nWho are the voters?\n1 - Students\n2 - Professors\n3 - Employees\n4 - Everyone\n0 - Abort\noption: ");

            voters = in.nextInt();
            in.nextLine();
            
            switch (voters){
                case 0:
                    return -2;
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
        System.out.println("\nWhat is the voters' department?");
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
                return -2;
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
        if(title.equals("0")){
            return -2;
        }

        System.out.print("\nElection description: ");
        String description = in.nextLine();
        if(description.equals("0")){
            return -2;
        }
        isValid=false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime start_time = LocalDateTime.now();
        do{
            System.out.print("\nElection start (dd/MM/yyyy HH:mm): ");
            String aux = in.nextLine();
            if(aux.equals("0")){
                return -2;
            }
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
            if(aux.equals("0")){
                return -2;
            }
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
        /*
        System.out.println(
            "\ntype: " + voters +
            "\nndep: " + ndep +
            "\ntitle: " + title +
            "\ndescription: " + description +
            "\nstart: " + start_time.toString() +
            "\nend: " + end_time.toString()
        );
        */

        
        return db.createElection(cargos, ndep + ";", ndep + ";", title, description, start_time, end_time);

            //TODO: maanage election em causa.
            //manageElection(in);
        
    }

    private static void chooseElection(Scanner in) throws RemoteException{
        System.out.println("\nChoose an election:");
        HashMap<Integer,HashMap<String,String>> elections;
        try{
            elections = db.getElections(null, null);
        }
        catch(NullPointerException e){
            elections = null;
        }
        int i= 1;
        if(elections != null){
            for (Integer key : elections.keySet()){
                System.out.println(i + " - " + elections.get(key).get("titulo"));
                i++;
            }
        }
        int nelec;
        
        do{
            System.out.print("0 - Back\noption: ");

            nelec = in.nextInt();
            in.nextLine();
            if(nelec == 0){
                break;
            }
            if (nelec< 0 || (nelec>0 && elections == null) || nelec > elections.keySet().size()){     
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nelec>0) {
                manageElection(in, elections.keySet().toArray(new Integer[elections.keySet().size()])[nelec-1]);
            }
        }while (nelec!=0);

    }

    private static void manageElection(Scanner in, int nelec) throws RemoteException{
        HashMap<Integer,HashMap<String,String>> elections = db.getElections(null, null);
        int option;
        int estado;
            do{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                if(LocalDateTime.parse(elections.get(nelec).get("inicio"), formatter).isAfter(LocalDateTime.now())){
                    estado = 1;
                    System.out.print("Election hasn't started yet\n1 - Manage candidate list\n2 - Manage polling stations\n3- Change properties\n0 - Back\noption: ");
                }
                else if(LocalDateTime.parse(elections.get(nelec).get("fim"), formatter).isAfter(LocalDateTime.now())){
                    estado = 2;
                    System.out.print("Election is currently active\n1 - Manage polling stations\n0 - Back\noption: ");
                }
                else{
                    estado = 3;
                    System.out.print("Election has finished \n1 - Check Results\n0 - Back\noption: ");
                }
                

                option = in.nextInt();
                in.nextLine();

            
                if (option == 1 && estado == 1){
                    manageCandidateLists(in,nelec);
                }
                else if ((option == 2 && estado == 1) || option == 1 && estado == 2){
                     managePollingStations(in, nelec);
                }        
                else if (option == 3 && estado == 1){
                    changeProperties(in, nelec);
                }
                else if (option == 1 && estado == 3){
                    checkResults(in, nelec);
                }        
                else{
                    System.out.println("Wrong option!");
                }              
            }while (option!=0);
    }
    
    private static void manageCandidateLists(Scanner in, int nelec) throws RemoteException{
        int option;
        do{
            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec); 
            System.out.println("Current candidate lists in the election:\n");
            if(lists != null){
                for (Integer key : lists.keySet()){
                    System.out.println("."+lists.get(key).left);
                }
            }
            System.out.println("\n1 - Add candidate list\n2 - Remove candidate list\n3 - Manage candidate list\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    //TODO: manageCandidates(in, election_id);
                    addCandidateList(in,nelec);
                    break;
                case 2:
                    //TODO: removeCandidateList(in, election_id);
                    removeCandidateList(in,nelec);
                    break;
                case 3:
                    //TODO: manageCandidates(in, election_id);  
                    chooseCandidateList(in, nelec);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
            
            
        }while (option!=0);
    }

    private static void addCandidateList(Scanner in, int nelec) throws RemoteException{

        System.out.print("\nList Name: ");
        String name = in.nextLine();
        int cl = db.createOrEditList(nelec, name, null,false);
        if(cl > 0){
            manageCandidates(in, nelec, cl);
        }
        else if (cl == -3){
            System.out.println("\nThere is already a list with that name associated to this election!");
        }
        else{
            System.out.println("Sorry, something went wrong with our server. Please contact us!");
        }
    }

    private static void removeCandidateList(Scanner in, int nelec) throws RemoteException{

        
        
        int nlist;
        System.out.println("\nWhich list do you want to remove?");
        do{
            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec); 
            int i = 1;
            if(lists != null){
                for (Integer key : lists.keySet()){
                    System.out.println(i + " - " +lists.get(key).left);
                    i++;
                }
            }
            System.out.print("0 - Back\noption: ");

            nlist = in.nextInt();
            in.nextLine();
            if(nlist == 0){
                break;
            }
            if (nlist< 0 || (nlist > 0 && lists == null) || nlist > lists.keySet().size()){     
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nlist>0) {
                Integer a[] = {lists.keySet().toArray(new Integer[lists.keySet().size()])[nlist-1]};
                db.editElection(nelec, true, null, null, null, null, null, null, a);
            }
        }while (nlist!=0);

    }
    private static void chooseCandidateList(Scanner in, int nelec) throws RemoteException{

        
        
        int nlist;
        System.out.println("\nWhich list do you want to edit?");
        do{
            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec); 
            int i = 1;
            if(lists != null){
                for (Integer key : lists.keySet()){
                    System.out.println(i + " - " +lists.get(key).left);
                    i++;
                }
            }
            System.out.print("0 - Back\noption: ");

            nlist = in.nextInt();
            in.nextLine();

            if (nlist< 0 || nlist > lists.keySet().size()){     
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nlist>0) {
                manageCandidates(in, nelec, lists.keySet().toArray(new Integer[lists.keySet().size()])[nlist-1]);
            }
        }while (nlist!=0);

    }
    private static void manageCandidates(Scanner in, int nelec, int nlista) throws RemoteException{
        int option;
        do{
            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists;
            ArrayList<Pair<String,String>> candis;
            try{
                lists = db.getLists(nelec);
                candis = lists.get(nlista).right;
            }
            catch(NullPointerException e){
                candis = null;
            }
             
            System.out.println("Candidates in the list:\n");
            if(candis != null){
                for(Pair<String,String> p : candis){
                    System.out.println("." + p.right + " ("+ p.left + ")");
                }
            }
            System.out.println("\n1 - Add candidate\n2 - Remove candidate\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    addCandidate(in, nelec, nlista);
                    break;
                case 2:
                    //TODO: removeCandidate(in, list_id);
                    removeCandidate(in, nelec, nlista);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
            
        }while (option!=0);
    }

    private static void addCandidate(Scanner in, int nelec, int nlista) throws RemoteException{
        HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec);
        System.out.print("\nCC number: ");
        String cc_number = in.nextLine();

        System.out.print("\nName: ");
        String name = in.nextLine();
        ArrayList<Pair<String,String>> al = new ArrayList<Pair<String,String>>();
        al.add(new Pair<String,String>(cc_number,name));
        if(db.createOrEditList(nelec, lists.get(nlista).left, al,false) == -2){
            System.out.println("That user does not have the requirements to be a candidate in that election");
        }
    }

    private static void removeCandidate(Scanner in, int nelec, int nlista) throws RemoteException{
        
        
        int nmember;
        System.out.println("\nWhat candidate do you wish to remove?");
        do{
            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists;
            ArrayList<Pair<String,String>> candis;
            try{
                lists = db.getLists(nelec);
                candis = lists.get(nlista).right;
            }
            catch(NullPointerException e){
                lists = null;
                candis = null;
            }
            int i= 1;
            if(candis != null){
                for(Pair<String,String> p : candis){
                    System.out.println(i+" - " + p.right + " ("+ p.left + ")");
                    i++;
                }
            }
            System.out.print("0 - Back\noption: ");

            nmember = in.nextInt();
            in.nextLine();

            if (nmember< 0 || nmember > candis.size()){     //TODO: nmember > num de membros
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nmember>0) {
                ArrayList<Pair<String,String>> a =  new ArrayList<Pair<String,String>>();
                a.add(candis.get(nmember-1));
                db.createOrEditList(nelec, lists.get(nlista).left,a, true);
            }
        }while (nmember!=0);

    }

    private static void managePollingStations(Scanner in, int nelec) throws RemoteException{
        int option;
       
        do{
            HashMap<Integer,HashMap<String,String>> elecs; 
            HashMap<Integer,String> deps; 
            try{
                elecs = db.getElections(null, null);
                deps = db.getDepartments();
            }
            catch(NullPointerException e){
                elecs = null;
                deps = null;
            }
            String mesas = elecs.get(nelec).get("mesas");
            List<String> amesas = Arrays.asList(mesas.split(";"));
            if(amesas.contains(new String("0"))){
                amesas = new ArrayList<String>();
                for(Integer key: deps.keySet()){
                    amesas.add(key.toString());
                }
            }
            System.out.println("PollingStations associated to the election:\n");
            if(amesas != null){
                for(String mesa : amesas){
                    System.out.println("." + deps.get(Integer.parseInt(mesa)));
                }
            }
            System.out.println("\n1 - Add polling station\n2 - Remove polling station\n0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    //TODO: addPollingStation(in, list_id);
                    addPollingStation(in, nelec, amesas);
                    break;
                case 2:
                    //TODO: removePollingStation(in, list_id);
                    removePollingStation(in, nelec, amesas);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
            
        }while (option!=0);

    }

    private static void addPollingStation(Scanner in, int nelec,List<String> stations) throws RemoteException{
        int option;
        do{
            HashMap<Integer,String> deps = db.getDepartments();
            ArrayList<Integer> amesas = new ArrayList<Integer>();
            System.out.println("Which polling station would you like to associate to the election?");
            int i = 1;
            if(deps != null){
                for(Integer key : deps.keySet()){
                    if(!stations.contains(key.toString())){
                        amesas.add(key);
                        System.out.println(i + " - " + deps.get(key));
                        i++;
                    }
                }
            }
            System.out.print("0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();
            if (option< 0 || option > amesas.size()){     //TODO: nmember > num de membros
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, false, null, null, null, null, null, amesas.get(option-1)+";", null);
            }
        }while (option!=0);

    }

    private static void removePollingStation(Scanner in, int nelec,List<String> stations) throws RemoteException{  
        int option;
        do{
            HashMap<Integer,String> deps = db.getDepartments();
            int i = 1;
            System.out.println("Which polling station would you like to remove from the election?");
            if(stations != null){
                for(String station : stations){
                    System.out.println(i + " - " + deps.get(Integer.parseInt(station)));
                }
                i++;
            }
            System.out.print("0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();
            if (option< 0 || option > stations.size()){     //TODO: nmember > num de membros
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, true, null, null, null, null, null, stations.get(option-1)+";", null);
            }
        }while (option!=0);


    }

    private static void changeProperties(Scanner in, int nelec) throws RemoteException{
        
        boolean isValid  = false;
        int option;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        HashMap<Integer,HashMap<String,String>> elecs = db.getElections(null, null);
        HashMap<String,String> elec = elecs.get(nelec);

        String title = elec.get("titulo");
        String description = elec.get("descricao");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        LocalDateTime start_time = LocalDateTime.parse(elec.get("inicio"),formatter2);
        LocalDateTime end_time = LocalDateTime.parse(elec.get("fim"),formatter2);

        do{
            System.out.println(
            "\ntitle: " + title +
            "\ndescription: " + description +
            "\nstart: " + start_time.toString() +
            "\nend: " + end_time.toString()
            );

            System.out.print("1 - Change Title\n2 - Change Description\n3 - Change Start Date\n4- Change End Date\n5- Change who is able to vote\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    System.out.print("\nNew election title: ");
                    title = in.nextLine();
                    if(title.equals("0")){
                        break;
                    }
                    db.editElection(nelec, false, title, null, null, null, null, null, null);
                    break;
                case 2:
                    System.out.print("\nElection description: ");
                    description = in.nextLine();
                    if(description.equals("0")){
                        break;
                    }
                    db.editElection(nelec, false, null, description, null, null, null, null, null);
                    break;
                case 3:
                    isValid = false;
                    do{
                        System.out.print("\nElection start (dd/MM/yyyy HH:mm): ");
                        String aux = in.nextLine();
                        if(aux.equals("0")){
                            break;
                        }
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
                    db.editElection(nelec, false, null, null, start_time, null, null, null, null);
                    break;
                case 4:
                    isValid = false;
                    do{
                        System.out.print("\nElection end (dd/MM/yyyy HH:mm): ");
                        String aux = in.nextLine();
                        if(aux.equals("0")){
                            break;
                        }
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
                    db.editElection(nelec, false, null, null, null, end_time, null, null, null);
                    break;
                case 5:
                    manageVoters(in, nelec);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }

        }while(option != 0);

    }

    private static void manageVoters(Scanner in, int nelec) throws RemoteException{
        int option;
       
        do{
            HashMap<Integer,HashMap<String,String>> elecs = db.getElections(null, null);
            HashMap<Integer,String> deps = db.getDepartments();
            String depars = elecs.get(nelec).get("departamentos");
            List<String> adeps= Arrays.asList(depars.split(";"));
            if(adeps.contains(new String("0"))){
                adeps = new ArrayList<String>();
                for(Integer key: deps.keySet()){
                    adeps.add(key.toString());
                }
            }
            System.out.println("You can vote if you belong to one of the following departments:\n");
            if(adeps != null){
                for(String mesa : adeps){
                    System.out.println("." + deps.get(Integer.parseInt(mesa)));
                }
            }
            System.out.println("\n1 - Add department\n2 - Remove department\n0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    //TODO: addPollingStation(in, list_id);
                    addDepartment(in, nelec, adeps);
                    break;
                case 2:
                    //TODO: removePollingStation(in, list_id);
                    removeDepartment(in, nelec, adeps);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }
            
        }while (option!=0);

    }

    private static void addDepartment(Scanner in, int nelec,List<String> departments) throws RemoteException{
        int option;
        do{
            HashMap<Integer,String> deps = db.getDepartments();
            ArrayList<Integer> adeps = new ArrayList<Integer>();
            System.out.println("Which department do you wish to add to the election?");
            int i = 1;
            if(deps != null){
                for(Integer key : deps.keySet()){
                    if(!departments.contains(key.toString())){
                        adeps.add(key);
                        System.out.println(i + " - " + deps.get(key));
                        i++;
                    }
                }
            }
            System.out.print("0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();
            if (option< 0 || option > adeps.size()){     
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, false, null, null, null, null, adeps.get(option-1)+";", null , null);
            }
        }while (option!=0);

    }

    private static void removeDepartment(Scanner in, int nelec,List<String> departments) throws RemoteException{  
        int option;
        do{
            HashMap<Integer,String> deps = db.getDepartments();
            int i = 1;
            System.out.println("Which department do you wish to remove from the election?");
            if(departments != null){
                for(String station : departments){
                    System.out.println(i + " - " + deps.get(Integer.parseInt(station)));
                }
                i++;
            }
            System.out.print("0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();
            if (option< 0 || option > departments.size()){     //TODO: nmember > num de membros
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, true, null, null, null, null, departments.get(option-1)+";", null, null);
            }
        }while (option!=0);


    }

    private static void checkResults(Scanner in, int nelec) throws RemoteException{
        HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> results = db.getResults(nelec);
        HashMap<Integer,Pair<String,Integer>> lists = results.get(nelec).right;
        if(lists != null){
            for(Integer list : lists.keySet()){
                System.out.println(lists.get(list).left + ": " + lists.get(list).right + " votos");
            }
        }
        System.out.println("Press enter to continue...");
        in.nextLine();
    }

}
