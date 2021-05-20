package Commun;

import java.io.Serializable;
import java.rmi.Remote;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Interface with the RMI Server methods to be called remotely
 * @author Diogo Flórido
 * @version 1.0
 */
public interface database extends Remote {
    /**
     * A simple serializable version of the java.sql's Pair class
     */
    public class Pair<T,T2> implements Serializable{
        /**
         *
         */
        private static final long serialVersionUID = -329674930060363561L;
        public T left;
        public T2 right;
        public Pair(T left, T2 right){
            this.left = left;
            this.right = right;
        }

        public String toString(){
            return ("( " + this.left + " , " + this.right + " )");
        }
    }
    /**
     * Inserts a new user into the database.
     * <p>
     * @param cargo the type(job) of the person to be added.
     * @param ndep the department the user belongs to.
     * @param nome the name of the user.
     * @param morada the address of the user.
     * @param telefone the phone number of the user.
     * @param numcc the ID number of the user.
     * @param valcc the expiration date of the ID.
     * @param username the username the user will use to log in.
     * @param password the password the user will use to log in.
     * @return An integer indicating if the operation was successful. 
     * <ul> 
     * <li> 1 if the user was successfully added to the database. 
     * <li> -1 if there is already an user with the given username in the database.
     * <li> -2 if there is already an user with the given ID number in the database.
     * <li> -3 if a RemoteException is thrown.
     * </ul> 
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public int createUser(String cargo, int ndep, String nome, String morada, String telefone, String numcc, Date valcc, String username, String password) throws java.rmi.RemoteException;
    /**
     * Returns a structure with the names of all the departments in the database.
     * @return an {@link HashMap}<{@link Integer},{@link String}> 
     * <ul>
     * <li> {id_of_the_department : name_of_the_department}
     * </ul>
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public HashMap<Integer,String> getDepartments() throws java.rmi.RemoteException;
    /**
     * Checks if there is any user in the database with the given username or ID and password.
     *
     * @param username  the username (it may be null if the num_cc isn't).
     * @param num_cc the user's ID number (it may be null if the username isn't).
     * @param password the user's password.
     * @return Whether a user was found for the given parameters or not.
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public int login(String username, String password) throws java.rmi.RemoteException;
    /**
     * Inserts a new election into the database.
     * <ul>
     * <li> if successful it calls the method {@link RMIServer#updateElections}.
     * </ul>
     * @param cargos The type (job) of the people allowed to vote in the election. <ul>
     * <li>'all' if every type can vote.
     * </ul>
     * @param departamentos The IDs of the departments where the people allowed to vote belong to separated by semi-colons.
     * <ul>
     * <li> ";id1;id2;id3;"
     * </ul>
     * @param mesas The IDs of the departments where people are allowed to vote in separated by semi-colons.
     * <ul>
     * <li> ";id1;id2;id3;"
     * </ul>
     * @param titulo The title of the election.
     * @param desc A description of the election.
     * @param inicio The date and time the election begins.
     * @param fim The date and time the election ends.
     * @return  An integer indicating the id of the new election if the operation was successful. 
     * <ul> 
     * <li> (the new election's id) if it was successfully added to the database. 
     * <li> (0-the new election's id) if it was successfully added to the database but there was already an election with the same name in the database.
     * <li> 0 if a RemoteException is thrown.
     * </ul> 
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public int createElection(String cargos, String departamentos, String mesas, String titulo, String desc, LocalDateTime inicio, LocalDateTime fim) throws java.rmi.RemoteException;
    /**
     * Edits the values of an election in the database.
     * <ul>
     * <li> if successful it calls the method {@link RMIServer#updateElections}.
     * </ul>
     * @param neleicao the id of the election to be edited.
     * @param remove whether the given parameters are meant to be removed from the database or added.
     * @param titulo a new title for the election (it may be null).
     * @param descricao a new description for the election (it may be null).
     * @param inicio a new starting date for the election (it may be null).
     * @param fim a new ending date for the election (it may be null).
     * @param departamentos The IDs of the departments meant to be added or removed where the people allowed to vote belong to separated by semi-colons (it may be null).
     * <ul>
     * <li> ";id1;id2;id3;"
     * </ul>
     * @param mesas The IDs of the departments meant to be added or removed where people are allowed to vote in separated by semi-colons (it may be null).
     * <ul>
     * <li> ";id1;id2;id3;"
     * </ul>
     * @param listas An array with the candidate lists which belong to this election (it may be null).
     * @return An integer indicating if the operation was successful. 
     * <ul> 
     * <li> 1 if the election was successfully edited in the database. 
     * <li> -1 if there is no election with the given id in the database.
     * <li> -2 if the election has already begun.
     * <li> -3 if a RemoteException is thrown.
     * </ul> 
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public int editElection(int neleicao, boolean remove, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String departamentos, String mesas, Integer[] listas) throws java.rmi.RemoteException;
    /**
     * Returns a dictionary with the data of the selected elections in the database.
     * @param username the username of a user who is going to vote (it may be null).
     * @param dep_mesa the id of the department where the user is going to vote (it may be null).
     * @return an {@link HashMap}<{@link Integer},{@link HashMap}<{@link String},{@link String}>> with the data of the selected elections.
     * <ul>
     * <li> {election_id :{title:String, description: String, start_date: String, end_date: String, ids_of_the_departments: String, ids_of_the_voting_stations: String}}
     * <li> returns all the elections in the database if both parameters are null.
     * <li> returns the elections a user is allowed to vote if none of the parameters are null.
     * <li> returns the elections a user has voted in the past if only dep_mesa is null.
     * </ul>
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public HashMap<Integer,HashMap<String,String>> getElections(String username, String dep_mesa) throws java.rmi.RemoteException;
    /**
     * Returns the name and ID number of an user.
     * @param usernameOrCC the username or ID number of the user.
     * @return a {@link String}[2] with the name of the user and the ID number of the user 
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public String[] getUser(String usernameOrCC) throws java.rmi.RemoteException;
    /**
     * Inserts a new candidate list in the database or edits an existent one.
     * @param neleicao the id of the election the list belongs to.
     * @param nome the name of the new list to be inserted or the name of the existent list to be edited.
     * @param users an {@link ArrayList}<{@link Pair}<{@link String},{@link String}>> with the names and ID numbers of the user meant to be added or removed from the list.
     * @param remove whether the users are meant to be removed from the list or added.
     * @return An integer indicating if the operation was successful. 
     * <ul> 
     * <li> 1 if the list was successfully created or edited in the database. 
     * <li> -1 if there is no election with the given id in the database.
     * <li> -2 if one of the users meant to be added cannot belong to the list.
     * <li> -3 if trying to create a new list when there is already a list with the same name for the same election.
     * <li> -4 if a RemoteException is thrown.
     * </ul> 
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public int createOrEditList(int neleicao, String nome, ArrayList<Pair<String,String>> users, boolean remove) throws java.rmi.RemoteException;
    /**
     * Returns the candidate lists belonging to a given election.
     * @param neleicao the id of the election.
     * @return an {@link HashMap}<{@link Integer},{@link Pair}<{@link String},{@link ArrayList}<{@link Pair}<{@link String},{@link String}>>>>
     * with the data of all lists belonging to the given election.
     * <ul>
     * <li> {list_id:(name_of_the_list,[(ID_number_of_the_candidate,name_of_the_candidate)])}
     * </ul>
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> getLists(int neleicao) throws java.rmi.RemoteException;
    /**
     * Creates a new vote for a given user and increments the number of votes for a given list in a given election.
     * @param username the username of the user who is voting.
     * @param neleicao the election the candidate list belongs to.
     * @param nlista the candidate list the user is voting.
     * @param mesa the id of the department where the user is voting.
     * @return An integer indicating if the operation was successful. 
     * <ul> 
     * <li> 1 if the vote was successfully added to the database. 
     * <li> -1 if the user has already voted for this election.
     * <li> -2 if the user's account was created after the election has begun.
     * </ul> 
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public int vote(String username, int neleicao, int nlista, int mesa) throws java.rmi.RemoteException;
    /**
     * returns the results for a selection of finished elections.
     * @param neleicao the id of the election (it may be 0).
     * @return an {@link HashMap}<{@link Integer},{@link Pair}<{@link String},{@link HashMap}<{@link Integer},{@link Pair}<{@link String}, {@link Integer}>>>>
     * with the number of votes for each list of each election.
     * <ul>
     * <li> {election_id: (election_name, {list_id: (list_name, number_of_votes)})}
     * </ul> 
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> getResults(int neleicao) throws java.rmi.RemoteException;
    /**
     * returns the names of the elections an user has voted in the past, the dates and times the user has voted and the department where the user has voted.
     * @param username the username of the user.
     * @return a {@link HashMap}<{@link Integer},{@link Pair}<{@link Integer},{@link String}>> with data about the user's votes
     * <ul>
     * <li> {election_id: (department_id, date)}
     * </li>
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public HashMap<Integer,Pair<Integer,String>> getUserVotes(String username) throws java.rmi.RemoteException;
    /**
     * Returns the number of votes per voting station for each active election.
     * @return the {@link HashMap}<{@link String},{@link HashMap}<{@link String},{@link Integer}>> {@link RMIServer#rtstations}
     * <ul>
     * <li>{election_name: {department_name: number_of_votes}}
     * </ul>
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public HashMap<String,HashMap<String,Integer>> getNumberVotesPerStation() throws java.rmi.RemoteException;
    /**
     * Returns whether the Server is connected to the database or not.
     * @return {@link RMIServer#isWorking}
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public boolean isWorking() throws java.rmi.RemoteException;
    /**
     * Returns the number of active and available voting terminals per Voting Station.
     * @return the {@link HashMap}<{@link String},{@link Pair},<{@link Integer},{@link Integer}>> {@link RMIServer#rtterminals}
     * <ul>
     * <li> {department_name : (number_of_available, number_of_active)}
     * </li>
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public HashMap<String,Pair<Integer,Integer>> getActiveStationStatus() throws java.rmi.RemoteException;
    /**
     * Updates the structure {@link RMIServer#rtterminals}
     * @param ndep id of the department
     * @param availableTerms number of available voting terminals
     * @param beingUsedTerms number of active voting terminals
     * @throws java.rmi.RemoteException if a communication-related exception occurs.
     */
    public void changeActiveStationStatus(int ndep, int availableTerms, int beingUsedTerms)throws java.rmi.RemoteException;
    public void setAdmin(Admin admin)throws java.rmi.RemoteException;
    public void setWeb(Web web)throws java.rmi.RemoteException;
    public void removeWeb(Web web)throws java.rmi.RemoteException;
}
