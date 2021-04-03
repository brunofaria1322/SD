package Commun;

import java.io.Serializable;
import java.rmi.Remote;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;



public interface database extends Remote {
    class Pair<T,T2> implements Serializable{
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
    }
    public int createUser(String cargo, int ndep, String nome, String morada, String telefone, String numcc, Date valcc, String username, String password) throws java.rmi.RemoteException;
    public HashMap<Integer,String> getDepartments() throws java.rmi.RemoteException;
    public boolean login(String username, String num_cc, String password) throws java.rmi.RemoteException;
    public int createElection(String cargos, String departamentos, String mesas, String titulo, String desc, LocalDateTime inicio, LocalDateTime fim) throws java.rmi.RemoteException;
    public int editElection(int neleicao, boolean remove, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String departamentos, String mesas, Integer[] listas) throws java.rmi.RemoteException;
    public HashMap<Integer,HashMap<String,String>> getElections(String username, String dep_mesa) throws java.rmi.RemoteException;
    public String[] getUser(String usernameOrCC) throws java.rmi.RemoteException;
    public int createOrEditList(int neleicao, String nome, ArrayList<Pair<String,String>> users, boolean remove) throws java.rmi.RemoteException;
    public HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> getLists(int neleicao) throws java.rmi.RemoteException;
    public int vote(String username, int neleicao, int nlista, int mesa) throws java.rmi.RemoteException;
    public HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> getResults(int neleicao) throws java.rmi.RemoteException;
    public HashMap<Integer,Pair<Integer,String>> getUserVotes(String username) throws java.rmi.RemoteException;
    public HashMap<String,HashMap<String,Integer>> getNumberVotesPerStation() throws java.rmi.RemoteException;
}
