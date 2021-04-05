package Admin;

import java.awt.Container;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Date;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import Commun.database;
import Commun.database.Pair;


/**
 * Main Class for the Admin Console
 *
 * @author Bruno Faria
 * @author Diogo Flórido
 * @version 1.0
 */
public class AdminConsole {

    /**
     * connector to the RMI server
     */
    static database db;

    /**
     * Frame for the realtime updates
     */
    static textAreaTest tat;

    /**
     * Class for the update Thread
     *
     * @author Diogo Flórido
     * @since 1.0
     */
    static class updateThread extends Thread{

        /**
         * TODO
         */
        textAreaTest aa;

        /**
         * TODO
         */
        Thread princ;


        /**
         * TODO
         *
         * @param abc
         * @param princ
         */
        public updateThread(textAreaTest abc,Thread princ)
        {
            aa = abc;
            this.princ = princ;
        }

        /**
         * TODO
         */
        @Override
        public void run(){
            while(true){
                try{
                    HashMap<String,Pair<Integer,Integer>> stations = db.getActiveStationStatus();
                    HashMap<String,HashMap<String,Integer>> results = db.getNumberVotesPerStation();
                    StringBuilder display = new StringBuilder();
                    if(stations != null){
                        for(String mesa : stations.keySet()){
                            display.append(mesa).append(": (").append(stations.get(mesa).right).append("/").append(stations.get(mesa).left).append(") active voting terminals\n");
                        }
                    }
                    display.append("\n\n");
                    if(results != null){

                        for(String el : results.keySet()){
                            display.append("---").append(el).append("---\n");
                            for (String mesa : results.get(el).keySet()){
                                display.append(mesa).append(":\t").append(results.get(el).get(mesa)).append(" votes\n");
                            }
                        }

                        sleep(100);
                    }
                    aa.setText(display.toString());
                }
                catch (InterruptedException e){
                    //e.printStackTrace();
                }
                catch (RemoteException e) {
                    princ.suspend();
                    reconnect();
                    princ.resume();
                }

            }
        }

    }


    /**
     * TODO
     *
     * @author Diogo Flórido
     * @since 1.0
     */
    static class textAreaTest extends javax.swing.JFrame
    {
        /**
         * TODO
         */
        JTextArea area = new JTextArea();

        /**
         * TODO
         */
        updateThread thread;

        /**
         * TODO
         *
         * @param princ
         */
        public textAreaTest(Thread princ)
        {
            thread = new updateThread(this, princ);
            JPanel panel = new JPanel();
            panel.add(area);
            this.setSize(1000, 500);
            Container c = this.getContentPane();
            c.add(area);
            this.setVisible(true);
            thread.start();
        }

        /**
         * TODO
         *
         * @param text
         */
        public void setText(String text)
        {
            area.setText(text);
        }

        /**
         * TODO
         */
        public void stop(){
            thread.stop();
        }
    }

    /**
     * Tries to reconnect to db in the next 30s
     */
    private static synchronized void reconnect(){
        long until = System.currentTimeMillis()+30000;
        boolean ok = false;
        while(System.currentTimeMillis() <= until){
            try {
                db= (database) LocateRegistry.getRegistry(1099).lookup("central");
                if(db.isWorking()){
                    ok = true;
                    break;
                }
            } catch (Exception e) {
                //TODO
            }
        }
        if(!ok){
            System.out.println("Server is closed");
            System.exit(0);
        }
    }

    /**
     * Main Function
     * Runs the Admin Console interface
     *
     * @param args                      Arguments
     * @throws InterruptedException     when thread is interrupted
     */
    public static void main(String[] args) throws InterruptedException {


        try {
            //connects to RMI server
            db= (database) LocateRegistry.getRegistry(1099).lookup("central");
            if(!db.isWorking()){
                System.out.println("Server closed. Sorry...");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Server closed. Sorry...");
            System.exit(0);
        }

        System.out.println("Welcome to Admin Console");
        tat = new textAreaTest(Thread.currentThread());

        int option = -1;
        Scanner in = new Scanner(System.in);
        //Starts printing the initial interface
        do{
            try{
                System.out.print("\n1 - Register person\n2 - Create election\n3 - Manage election\n4 - Check user's voting history\n0 - Quit\noption: ");

                option = in.nextInt();
                in.nextLine();

                switch (option) {
                    //Exits
                    case 0 -> {
                        System.out.println("Bye!");
                        tat.stop();
                        System.exit(0);
                    }

                    // Register Person
                    case 1 -> {
                        int rp = registerPerson(in);
                        if (rp == 1) {
                            System.out.println("Person registered successfully !");
                        } else if (rp == -4) {
                            System.out.println("Registry was aborted!");
                        } else if (rp == -3) {
                            System.out.println("Sorry, something went wrong with our server. Please contact us!");
                        } else if (rp == -2) {
                            System.out.println("There's already an account with that CC number!");
                        } else {
                            System.out.println("There's already an account with that username!");
                        }
                    }

                    //Create election
                    case 2 -> {
                        int ce = createElection(in);
                        if (ce == 0) {
                            System.out.println("Sorry, something went wrong with our server. Please contact us!");
                        } else if (ce == -2) {
                            System.out.println("Election was created successfully!");
                            System.out.println("WARNING: There was already an election with the same name as the new one's");
                            manageElection(in, -ce);
                        } else if (ce > 0) {
                            System.out.println("Election was created successfully!");
                            manageElection(in, ce);
                        } else {
                            System.out.println("Creation was aborted!");
                        }
                    }

                    //Manage election
                    case 3 -> chooseElection(in);

                    //Checks user's voting history
                    case 4 -> getUserVotes(in);

                    //Wrong option
                    default -> System.out.println("Wrong option!");
                }
            }
            catch(InputMismatchException e){
                //when a int is asked and is given a String
                System.out.println("Invalid input!");
                in.nextLine();
            }
            catch(RemoteException e){
                //tries to reconnect to RMI Server
                reconnect();
            }
            catch(java.util.NoSuchElementException e){
                System.exit(0);
            }
        }while (option != 0);
        in.close();
    }


    /**
     * Register Person interface
     *
     * @param in                    input Scanner
     * @return                      int containing the result of the register
     * @throws RemoteException      when is not able to connect to RMI Server
     */
    private static int registerPerson(Scanner in) throws RemoteException{
        int type;
        String cargo = "all";
        boolean isValid = false;


        //Type of person to be registered
        do{
            System.out.print("\n1 - Register student\n2 - Register professor\n3 - Register employee\n0 - Abort\noption: ");

            type = in.nextInt();
            in.nextLine();

            switch (type){
                //Abort register
                case 0:
                    return -4;

                //Register Student
                case 1:
                    cargo = "aluno";
                    isValid = true;
                    break;

                //Register Professor
                case 2:
                    cargo = "docente";
                    isValid = true;
                    break;

                //Register Employee
                case 3:
                    isValid = true;
                    cargo = "funcionario";
                    break;

                //Wrong option
                default:
                    System.out.println("Wrong option!");
                    break;
            }
        }while (!isValid);


        //department of the person
        HashMap<Integer,String> deps = db.getDepartments();
        isValid = false;
        int ndep;
        do{
            System.out.println("\nSelect the user's department");
            int i = 1;
            for (Integer key : deps.keySet()){
                System.out.println(i + " - " + deps.get(key));
                i++;
            }
            System.out.print("0 - Abort\noption: ");
            ndep = in.nextInt();
            in.nextLine();

            //ABORT
            if (ndep == 0){
                return -4;
            }
            //Wrong option
            else if (ndep< 0 || ndep > deps.keySet().size()){
                System.out.println("Wrong option! Try Again...");
                break;
            }
            //Valid option
            else{
                isValid = true;
                ndep = deps.keySet().toArray(new Integer[deps.keySet().size()])[ndep-1];
            }
        }while (!isValid);


        //Person Name
        String name;
        isValid = false;
        do {
            System.out.print("\nName: ");
            name = in.nextLine();
            if (name.equals("0")) {
                return -4;
            }
            if (name.length() > 64) {
                System.out.println("Please make the name shorter than 64 characters. You can replace the middle names for their initials.");
            }
            else{
                isValid=true;
            }
        }while (!isValid);


        //Person Address
        isValid = false;
        String address;
        do {
            System.out.print("\nAddress: ");
            address = in.nextLine();
            if (address.equals("0")) {
                return -4;
            }
            if (address.length() > 64) {
                System.out.println("Please make the address shorter than 64 characters. Sorry...");
            }
            else{
                isValid=true;
            }
        }while (!isValid);


        //Person Phone's number
        isValid = false;
        String phone_number;
        do {
            System.out.print("\nPhone number: ");
            phone_number = in.nextLine();
            if(phone_number.equals("0")){
                return -4;
            }
            try{
                Integer.parseInt(phone_number);
            }
            catch(Exception e){
                System.out.println("A phone number can only have digits!");
                return -4;
            }
            if(phone_number.length() < 7 || phone_number.length() > 16){
                System.out.println("That is not a valid phone number!");
            }
            else{
                isValid=true;
            }
        }while (!isValid);


        //Person CC's number
        isValid = false;
        String cc_number;
        do {

            System.out.print("\nCC number: ");
            cc_number = in.nextLine();
            if(cc_number.equals("0")){
                return -4;
            }
            try{
                Integer.parseInt(cc_number);
            }
            catch(Exception e){
                System.out.println("An ID must only have digits!");
                return -4;
            }
            if(cc_number.length()>16 || cc_number.length() < 8){
                System.out.println("That is not a valid ID!");
            }
            else{
                isValid=true;
            }
        }while (!isValid);

        //CC's Expiration Date
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
               //e.printStackTrace();
               System.out.print("Invalid date! Date should be in format (mm/yyyy). Try Again...");
            }

        }while(!isValid);


        //Person's username
        isValid = false;
        String username;
        do {

            System.out.print("\nUsername: ");
            username = in.nextLine();
            if(username.equals("0")){
                return -4;
            }
            if(username.length() > 16 || username.length() < 3){
                System.out.println("username must have between 3 and 16 characters.");
            }
            else{
                isValid=true;
            }
        }while (!isValid);


        //Person's Password
        isValid = false;
        String password;
        do{
            System.out.print("\nPassword: ");
            password = new String(System.console().readPassword());
            if(password.equals("0")){
                return -4;
            }
            if(password.length()>=4 && password.length() < 16){
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
                System.out.print("\nPassword requires a minimum of 4 characters and a maximum of 16 characters! Try Again...");
            }
        }while(!isValid);

        /*
        DEBUG
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
        */

        return db.createUser(cargo, ndep, name, address, phone_number, cc_number, cc_expiration_date, username, password);

    }

    /**
     * Create Election interface
     *
     * @param in                    input Scanner
     * @return                      int containing the result of the register
     * @throws RemoteException      when is not able to connect to RMI Server
     */
    private static int createElection(Scanner in) throws RemoteException{
        int voters;
        boolean isValid = false;
        String cargos = "all";


        //Type of voters of the election
        do{
            System.out.print("\nWho are the voters?\n1 - Students\n2 - Professors\n3 - Employees\n4 - Everyone\n0 - Abort\noption: ");

            voters = in.nextInt();
            in.nextLine();

            switch (voters){
                case 0:
                    return -1;
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


        //departments of the election
        HashMap<Integer,String> deps = db.getDepartments();
        int ndep;
        isValid = false;
        do{
            System.out.println("\nWhat is the voters' department?");
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


        //Election's Title
        String title;
        isValid = false;
        do {
            System.out.print("\nElection title: ");
            title = in.nextLine();
            if(title.equals("0") || title.equals("votos nulos") || title.equals("votos em branco")){
                return -1;
            }
            if(title.length()>64){
                System.out.println("Please make the title shorter than 64 characters. You can write more details of the election in the description!");
            }
            else{
                isValid=true;
            }
        }while (!isValid);


        //Election's Description
        System.out.print("\nElection description: ");
        String description = in.nextLine();
        if(description.equals("0")){
            return -1;
        }


        //Election's Starting Date and Time
        isValid=false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime start_time = LocalDateTime.now();
        do{
            System.out.print("\nElection start (dd/MM/yyyy HH:mm): ");
            String aux = in.nextLine();
            if(aux.equals("0")){
                return -1;
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


        //Election's Ending Date and Time
        isValid=false;
        LocalDateTime end_time = LocalDateTime.now();
        do{
            System.out.print("\nElection end (dd/MM/yyyy HH:mm): ");
            String aux = in.nextLine();
            if(aux.equals("0")){
                return -1;
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

        /*
        DEBUG
        System.out.println(
            "\ntype: " + voters +
            "\nndep: " + ndep +
            "\ntitle: " + title +
            "\ndescription: " + description +
            "\nstart: " + start_time.toString() +
            "\nend: " + end_time.toString()
        );
        */

        return db.createElection(cargos, ";" + ndep + ";", ";"+ ndep + ";", title, description, start_time, end_time);

    }

    /**
     * interface to pick an election to manage
     *
     * @param in                        input Scanner
     * @throws RemoteException          when is not able to connect to RMI Server
     * @throws InterruptedException     when interrupted
     */
    private static void chooseElection(Scanner in) throws RemoteException, InterruptedException{
        System.out.println("\nChoose an election:");
        HashMap<Integer,HashMap<String,String>> elections;

        //tries to get all elections from RMI Server
        try{
            elections = db.getElections(null, null);
        }
        catch(NullPointerException e){
            elections = null;
        }

        int i, nelec;
        do{
            i= 1;
            if(elections != null){
                for (Integer key : elections.keySet()){
                    System.out.println(i + " - " + elections.get(key).get("titulo"));
                    i++;
                }
            }
            System.out.print("0 - Back\noption: ");

            nelec = in.nextInt();
            in.nextLine();

            if (nelec< 0 || (nelec>0 && elections == null) || nelec > Objects.requireNonNull(elections).keySet().size()){
                System.out.println("Wrong option! Try Again...");
            }
            else if(nelec>0) {
                //manages the selected election
                manageElection(in, elections.keySet().toArray(new Integer[elections.keySet().size()])[nelec-1]);
            }
        }while (nelec!=0);

    }

    /**
     * interface for managing an election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @throws RemoteException          when is not able to connect to RMI Server
     * @throws InterruptedException     when interrupted
     */
    private static void manageElection(Scanner in, int nelec) throws RemoteException, InterruptedException{
        HashMap<Integer,HashMap<String,String>> elections = db.getElections(null, null);
        int option, estado;
        do{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

            //if Election hasn't started yet
            if(LocalDateTime.parse(elections.get(nelec).get("inicio"), formatter).isAfter(LocalDateTime.now())){
                estado = 1;
                System.out.print("Election hasn't started yet\n1 - Manage candidate list\n2 - Manage polling stations\n3 - Change properties\n0 - Back\noption: ");
            }
            //if Election is currently active
            else if(LocalDateTime.parse(elections.get(nelec).get("fim"), formatter).isAfter(LocalDateTime.now())){
                estado = 2;
                System.out.print("Election is currently active\n1 - Manage polling stations\n0 - Back\noption: ");
            }
            //if election has finished
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
            else if (option == 1){  // and estado 3
                checkResults(in, nelec);
            }
            else{
                System.out.println("Wrong option!");
            }
        }while (option!=0);
    }

    /**
     * interface to manage candidate lists to a certain election
     *
     * @param in                    input Scanner
     * @param nelec                 number of election
     * @throws RemoteException      when is not able to connect to RMI Server
     */
    private static void manageCandidateLists(Scanner in, int nelec) throws RemoteException{
        int option;
        do{
            System.out.println("Current candidate lists in the election:\n");

            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec);

            ArrayList<Integer> toremove = new ArrayList<>();
            // white votes and null votes will be removed because they are not lists

            if(lists != null){
                for (Integer key : lists.keySet()){
                    if(!lists.get(key).left.equals("votos em branco") && !lists.get(key).left.equals("votos nulos")){
                        System.out.println("."+lists.get(key).left);
                    }
                    else{
                        toremove.add(key);
                    }
                }
                for(Integer i : toremove){
                    //removes the lists "votos em branco" and " votos nulos"
                    lists.remove(i);
                }
            }
            System.out.println("\n1 - Add candidate list\n2 - Remove candidate list\n3 - Manage candidate list\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    addCandidateList(in,nelec);
                    break;
                case 2:
                    removeCandidateList(in,nelec);
                    break;
                case 3:
                    chooseCandidateList(in, nelec);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }


        }while (option!=0);
    }

    /**
     * interface to add a candidate list to a certain election
     *
     * @param in                    input Scanner
     * @param nelec                 number of election
     * @throws RemoteException      when is not able to connect to RMI Server
     */
    private static void addCandidateList(Scanner in, int nelec) throws RemoteException{

        System.out.print("\nList Name: ");
        String name = in.nextLine();

        int cl;
        if(name.length()<64){
            cl = db.createOrEditList(nelec, name, null,false);
        }
        else{
            cl = -1;
        }
        if(cl > 0){
            manageCandidates(in, nelec, cl);
        }
        else if (cl == -3){
            System.out.println("\nThere is already a list with that name associated to this election!");
        }
        else if ( cl == -1){
            System.out.println("\nPlease make the list's name shorter than 64 characters.");
        }
        else{
            System.out.println("Sorry, something went wrong with our server. Please contact us!");
        }
    }

    /**
     * interface to remove a candidate list to a certain election
     *
     * @param in                    input Scanner
     * @param nelec                 number of election
     * @throws RemoteException      when is not able to connect to RMI Server
     */
    private static void removeCandidateList(Scanner in, int nelec) throws RemoteException{

        int nlist;
        System.out.println("\nWhich list do you want to remove?");
        do{
            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec);
            int i = 1;
            if(lists != null){
                ArrayList<Integer> toremove = new ArrayList<>();
                // white votes and null votes will be removed because they are not lists

                for (Integer key : lists.keySet()){
                    if(!lists.get(key).left.equals("votos em branco") && !lists.get(key).left.equals("votos nulos")){
                        System.out.println(i + " - " +lists.get(key).left);
                        i++;
                    }
                    else{
                        toremove.add(key);
                    }
                }
                for(Integer j : toremove){
                    //removes the lists "votos em branco" and " votos nulos"
                    lists.remove(j);
                }

            }
            System.out.print("0 - Back\noption: ");

            nlist = in.nextInt();
            in.nextLine();
            if (nlist< 0 || (nlist > 0 && lists == null) || nlist > Objects.requireNonNull(lists).keySet().size()){
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(nlist>0) {
                Integer[] a = {lists.keySet().toArray(new Integer[lists.keySet().size()])[nlist-1]};
                db.editElection(nelec, true, null, null, null, null, null, null, a);
            }
        }while (nlist!=0);

    }

    /**
     * interface to pick a candidate list to manage
     *
     * @param in                    input Scanner
     * @param nelec                 number of election
     * @throws RemoteException      when is not able to connect to RMI Server
     */
    private static void chooseCandidateList(Scanner in, int nelec) throws RemoteException{

        int nlist;
        System.out.println("\nWhich list do you want to edit?");
        do{
            HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec);
            int i = 1;
            //prints candidate lists
            if(lists != null){
                for (Integer key : lists.keySet()){
                    System.out.println(i + " - " +lists.get(key).left);
                    i++;
                }
            }
            System.out.print("0 - Back\noption: ");

            nlist = in.nextInt();
            in.nextLine();

            if (nlist< 0 || nlist > Objects.requireNonNull(lists).keySet().size()){
                System.out.println("Wrong option! Try Again...\n");
                break;
            }
            else if(nlist>0) {
                manageCandidates(in, nelec, lists.keySet().toArray(new Integer[lists.keySet().size()])[nlist-1]);
            }
        }while (nlist!=0);

    }

    /**
     * interface for managing candidates in a list
     *
     * @param in                    input Scanner
     * @param nelec                 number of election in cause
     * @param nlista                number of candidate list in cause
     * @throws RemoteException      when is not able to connect to RMI Server
     */
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

            //prints candidates in list
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
                    removeCandidate(in, nelec, nlista);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }

        }while (option!=0);
    }

    /**
     * interface to add a candidate to a certain list
     *
     * @param in                    input Scanner
     * @param nelec                 number of election in cause
     * @param nlista                number of candidate list in cause
     * @throws RemoteException      when is not able to connect to RMI Server
     */
    private static void addCandidate(Scanner in, int nelec, int nlista) throws RemoteException{
        HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> lists = db.getLists(nelec);
        System.out.print("\nCC number: ");
        String cc_number = in.nextLine();

        System.out.print("\nName: ");
        String name = in.nextLine();

        ArrayList<Pair<String,String>> al = new ArrayList<>();
        al.add(new Pair<>(cc_number, name));

        if(db.createOrEditList(nelec, lists.get(nlista).left, al,false) == -2){
            System.out.println("That user does not have the requirements to be a candidate in that election");
        }
    }

    /**
     * interface to remove a candidate to a certain list
     *
     * @param in                    input Scanner
     * @param nelec                 number of election in cause
     * @param nlista                number of candidate list in cause
     * @throws RemoteException      when is not able to connect to RMI Server
     */
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
                //prints candidates
                for(Pair<String,String> p : candis){
                    System.out.println(i+" - " + p.right + " ("+ p.left + ")");
                    i++;
                }
            }
            System.out.print("0 - Back\noption: ");

            nmember = in.nextInt();
            in.nextLine();

            if (nmember< 0 || nmember > Objects.requireNonNull(candis).size()){
                System.out.println("Wrong option! Try Again...");
            }
            else if(nmember>0) {
                ArrayList<Pair<String,String>> a = new ArrayList<>();
                a.add(candis.get(nmember-1));
                db.createOrEditList(nelec, lists.get(nlista).left,a, true);
            }
        }while (nmember!=0);

    }

    /**
     * interface for managing polling stations associated to a certain election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @throws RemoteException          when is not able to connect to RMI Server
     * @throws InterruptedException     when interrupted
     */
    private static void managePollingStations(Scanner in, int nelec) throws RemoteException, InterruptedException{
        int option;

        do{
            HashMap<Integer,HashMap<String,String>> elecs;
            HashMap<Integer,String> deps;
            try{
                //gets all elections
                elecs = db.getElections(null, null);
                //gets all departments
                deps = db.getDepartments();
            }
            catch(NullPointerException e){
                elecs = null;
                deps = null;
            }
            String mesas;
            List<String> amesas = null;
            if (elecs != null) {
                mesas = elecs.get(nelec).get("mesas");
                amesas = new ArrayList<>(Arrays.asList(mesas.split(";")));

                if(amesas.contains("0")){
                    amesas = new ArrayList<>();
                    for(Integer key: deps.keySet()){
                        amesas.add(key.toString());
                    }
                }
            }
            System.out.println("PollingStations associated to the election:\n");
            if(amesas != null){
                for(String mesa : amesas){
                    try{
                        System.out.println("." + deps.get(Integer.parseInt(mesa)));
                    }
                    catch(NumberFormatException e){
                        // Couldn't parse
                        // TODO
                    }
                }
            }
            System.out.println("\n1 - Add polling station\n2 - Remove polling station\n0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    addPollingStation(in, nelec, amesas);
                    break;
                case 2:
                    removePollingStation(in, nelec, amesas);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }

        }while (option!=0);

    }

    /**
     * interface to add a polling station to an election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @param stations                  polling stations associated to the election
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    private static void addPollingStation(Scanner in, int nelec,List<String> stations) throws RemoteException {
        int option;
        do{

            HashMap<Integer,String> deps = db.getDepartments();
            ArrayList<Integer> amesas = new ArrayList<>();
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
            if (option< 0 || option > amesas.size()){
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, false, null, null, null, null, null, amesas.get(option-1)+";", null);
                stations.add(Integer.toString(amesas.get(option-1)));
            }
        }while (option!=0);

    }


    /**
     * interface to add a polling station to an election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @param stations                  polling stations associated to the election
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    private static void removePollingStation(Scanner in, int nelec,List<String> stations) throws RemoteException {
        int option;
        do{

            HashMap<Integer,String> deps = db.getDepartments();
            int i = 1;
            System.out.println("Which polling station would you like to remove from the election?");
            if(stations != null){
                for(String station : stations){
                    try{
                        System.out.println(i + " - " + deps.get(Integer.parseInt(station)));
                        i++;
                    }
                    catch(NumberFormatException e){
                        //TODO
                    }
                }

            }
            System.out.print("0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();
            if (option< 0 || option > Objects.requireNonNull(stations).size()){
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, true, null, null, null, null, null, stations.get(option)+";", null);
                stations.remove(option);
            }
        }while (option!=0);


    }

    /**
     * interface to change properties of an election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @throws RemoteException          when is not able to connect to RMI Server
     * @throws InterruptedException     when interrupted
     */
    private static void changeProperties(Scanner in, int nelec) throws RemoteException, InterruptedException{

        boolean isValid;
        int option;

        //gets info of election
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

            System.out.print("1 - Change Title\n2 - Change Description\n3 - Change Start Date and Time\n4- Change End Date and Time\n5- Change who is able to vote\n0 - Back\noption: ");

            option = in.nextInt();
            in.nextLine();

            switch (option){

                //Back
                case 0:
                    break;

                //Change Title
                case 1:
                    System.out.print("\nNew election title: ");
                    title = in.nextLine();
                    if(title.equals("0")){
                        break;
                    }
                    db.editElection(nelec, false, title, null, null, null, null, null, null);
                    break;

                //Change Description
                case 2:
                    System.out.print("\nElection description: ");
                    description = in.nextLine();
                    if(description.equals("0")){
                        break;
                    }
                    db.editElection(nelec, false, null, description, null, null, null, null, null);
                    break;

                //Change Start Date and Time
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

                //Change End Date and Time
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

                //Manage Voters
                case 5:
                    manageVoters(in, nelec);
                    break;

                default:
                    System.out.println("Wrong option!");
                    break;
            }

        }while(option != 0);

    }

    /**
     * interface to manage who can vote in a certain election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    private static void manageVoters(Scanner in, int nelec) throws RemoteException {
        int option;

        do{
            //gets election
            HashMap<Integer,HashMap<String,String>> elecs = db.getElections(null, null);
            HashMap<Integer,String> deps = db.getDepartments();
            String depars = elecs.get(nelec).get("departamentos");
            // departments able to vote in election
            List<String> adeps= new ArrayList<>(Arrays.asList(depars.split(";")));
            if(adeps.contains("0")){
                adeps = new ArrayList<>();
                for(Integer key: deps.keySet()){
                    adeps.add(key.toString());
                }
            }
            System.out.println("You can vote if you belong to one of the following departments:\n");
            for(String dep : adeps){
                try{
                    System.out.println("." + deps.get(Integer.parseInt(dep)));
                }
                catch(NumberFormatException e){
                    // TODO
                    // Couldn't parse int
                }
            }
            System.out.println("\n1 - Add department\n2 - Remove department\n0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();

            switch (option){
                case 0:
                    break;
                case 1:
                    addDepartment(in, nelec, adeps);
                    break;
                case 2:
                    removeDepartment(in, nelec, adeps);
                    break;
                default:
                    System.out.println("Wrong option!");
                    break;
            }

        }while (option!=0);

    }

    /**
     * interface to add a department to be able to vote in an election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @param departments               departments able to vote in election
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    private static void addDepartment(Scanner in, int nelec,List<String> departments) throws RemoteException {
        int option;
        do{

            HashMap<Integer,String> deps = db.getDepartments();
            ArrayList<Integer> adeps = new ArrayList<>();
            System.out.println("Which department do you wish to add to the election?");

            //prints departments
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

            //reads inputted option
            option = in.nextInt();
            in.nextLine();

            if (option< 0 || option > adeps.size()){
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, false, null, null, null, null, adeps.get(option-1)+";", null , null);
                departments.add(Integer.toString(adeps.get(option-1)));
            }
        }while (option!=0);

    }

    /**
     * interface to remove a department from the list of departments
     * that are able to vote in an election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @param departments               departments able to vote in election
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    private static void removeDepartment(Scanner in, int nelec,List<String> departments) throws RemoteException {
        int option;
        do{
            //gets departments
            HashMap<Integer,String> deps = db.getDepartments();
            int i = 1;
            System.out.println("Which department do you wish to remove from the election?");

            //Prints departments that are able to vote in this election
            if(departments != null){
                for(String station : departments){
                    try{
                        System.out.println(i + " - " + deps.get(Integer.parseInt(station)));
                        i++;
                    }
                    catch(NumberFormatException e){
                        // TODO
                        // Can't parse to int
                    }

                }

            }
            System.out.print("0 - Back\noption: ");
            option = in.nextInt();
            in.nextLine();
            if (option< 0 || option > Objects.requireNonNull(departments).size()){
                System.out.println("Wrong option! Try Again...");
                break;
            }
            else if(option>0) {
                db.editElection(nelec, true, null, null, null, null, departments.get(option)+";", null, null);
                departments.remove(option);
            }
        }while (option!=0);


    }

    /**
     * Prints the results of an election
     *
     * @param in                        input Scanner
     * @param nelec                     number of election in cause
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    private static void checkResults(Scanner in, int nelec) throws RemoteException{
        HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> results = db.getResults(nelec);

        if(results != null){
            int total = 0;
            HashMap<Integer,Pair<String,Integer>> lists = results.get(nelec).right;
            for(Integer list : lists.keySet()){
                total+=lists.get(list).right;
            }
            if(total == 0){
                total = 1;
            }
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);

            // Prints results
            for(Integer list : lists.keySet()){
                System.out.println(lists.get(list).left + ": " + lists.get(list).right + " votos ("+df.format(lists.get(list).right*100/total)+"%)");
            }
        }
        System.out.println("Press enter to continue...");
        in.nextLine();
    }

    /**
     * Interface to print an user's voting history
     *
     * @param in                        input Scanner
     * @throws RemoteException          when is not able to connect to RMI Server
     */
    private static void getUserVotes(Scanner in) throws RemoteException{
        //asks for username
        System.out.print("\nUsername: ");
        String username = in.nextLine();
        
        HashMap<Integer,Pair<Integer,String>> votes = db.getUserVotes(username);
        HashMap<Integer,String> deps = db.getDepartments();
        HashMap<Integer,HashMap<String,String>> elecs = db.getElections(username, null);
        try{
            for(Integer key: votes.keySet()){
                System.out.println(elecs.get(key).get("titulo")+": "+ deps.get(votes.get(key).left)+" at " + votes.get(key).right);
            }
        }
        catch( java.lang.NullPointerException e){
            System.out.println("This user has no past votes.\n");
        }
        System.out.println("Press enter to continue...");
        in.nextLine();
    }

}
