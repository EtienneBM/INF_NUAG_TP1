package ca.polymtl.inf4410.tp1.server;

import java.io.File;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lanc� ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/*
	 * Méthode accessible par RMI. Additionne les deux nombres passés en
	 * paramètre.
	 */
	@Override
	public int execute(int a, int b) throws RemoteException {
		return a + b;
	}
	
	
	
	//---------------------------- part 2 --------------------------------------
	public int generateclientid(){//ecrire methode
		int id = 0;
		return id;
	}
	
	public void create(String nom){//def le type + ecrire methode

	}
	
	public void list(){//def le type + ecrire methode
		
	}
	
	public void syncLocalDir(){//def le type + ecrire methode
		
	}
	
	public void get(/*nom, checksum*/){//def le type + ecrire methode
		
	}
	
	public void lock(/*nom, clientid, checksum*/){//def le type + ecrire methode
		
	}
	
	public void push(/*nom, contenu, clientid*/){//def le type + ecrire methode
		
	}
	

	
	
	
}
