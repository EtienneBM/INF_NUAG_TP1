package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	int execute(int a, int b) throws RemoteException;
	int generateclientid();
	boolean create(String nom) throws Exception;
	String [] list();
	void syncLocalDir();
	void get(/*nom, checksum*/);
	void lock(/*nom, clientid, checksum*/);
	void push(/*nom, contenu, clientid*/);
}
