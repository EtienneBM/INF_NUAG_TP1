package ca.polymtl.inf4410.tp1.shared;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	int execute(int a, int b) throws RemoteException;
	int generateclientid();
	boolean create(String nom) throws Exception;
	String [] list();
	File[] syncLocalDir();
	File get(String nom, int checksum);
	void lock(String nom, int clientid, int checksum);
	void push(/*nom, contenu, clientid*/);
}
