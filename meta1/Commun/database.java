package Commun;

import java.rmi.Remote;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

public interface database extends Remote {
    public int createUser(String cargo, int ndep, String nome, String morada, String telefone, String numcc, Date valcc, String username, String password) throws java.rmi.RemoteException;
    public HashMap<Integer,String> getDepartments() throws java.rmi.RemoteException;
    public boolean login(String username, String password) throws java.rmi.RemoteException;
    public int createElection(String cargos, String departamentos, String mesas, String titulo, String desc, LocalDateTime inicio, LocalDateTime fim) throws java.rmi.RemoteException;
    public int editElection(int neleicao, boolean remove, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String departamentos, String mesas, Integer[] listas) throws java.rmi.RemoteException;
    public HashMap<Integer,HashMap<String,String>> getElections(String username, String dep_mesa) throws java.rmi.RemoteException;
    public String[] getUser(String usernameOrCC) throws java.rmi.RemoteException;
    public boolean logOut(String username) throws java.rmi.RemoteException;
    public int createOrEditList(int neleicao, String nome, ArrayList<Pair<String,String>> users, boolean remove) throws java.rmi.RemoteException;
    public HashMap<Integer,Pair<String,ArrayList<Pair<String,String>>>> getLists(int neleicao) throws java.rmi.RemoteException;
    public int vote(String username, int neleicao, int nlista, int mesa) throws java.rmi.RemoteException;
    public HashMap<Integer,Pair<String,HashMap<Integer,Pair<String,Integer>>>> getResults(int neleicao) throws java.rmi.RemoteException;
}
