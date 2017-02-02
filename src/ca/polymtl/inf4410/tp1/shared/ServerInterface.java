package ca.polymtl.inf4410.tp1.shared;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface ServerInterface extends Remote {
	int execute(int a, int b) throws RemoteException;
	int generateclientid();
	boolean create(String nom) throws Exception;
	File[] list();
	void syncLocalDir();
	File get(String nom, String checksum) throws IOException, NoSuchAlgorithmException;
	void lock(/*nom, clientid, checksum*/);
	void push(/*nom, contenu, clientid*/);
}
