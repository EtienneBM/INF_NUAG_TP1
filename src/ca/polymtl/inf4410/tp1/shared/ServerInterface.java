package ca.polymtl.inf4410.tp1.shared;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public interface ServerInterface extends Remote {
	int generateclientid() throws RemoteException ;
	boolean create(String nom) throws Exception;
	HashMap<String,String> list() throws RemoteException;
	ArrayList<ArrayList<String>> syncLocalDir() throws RemoteException, IOException;
	ArrayList<String> get(String nom, String checksum) throws IOException, NoSuchAlgorithmException;
	ArrayList<String> lock(String nom, String clientid, String checksum) throws IOException, NoSuchAlgorithmException;
	boolean push(String nom, ArrayList<String> contenu, String  clientid) throws IOException;
}
