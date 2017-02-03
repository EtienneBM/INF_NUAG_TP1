package ca.polymtl.inf4410.tp1.server;


import java.io.File;
import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {
	
	private int id = 0;

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
	//cette medthode renvoie un id. 
	//A chaque fois que le serveur est arret� puis relanc� la liste des id reccomence � 1
	public int generateclientid(){
		this.id = this.id + 1;
		return this.id;
	}
	//Create permet de creer un fichier si il n'existe pas. La fonction renvoie un bool�en de valeur true si le fichier a bien �t� cr��, et false sinon.
	public boolean create(String nom) throws Exception{//def le type + ecrire methode
		if (new File(nom).exists()){
			System.out.println("Le fichier " + nom + "existe deja"); 
			return false; 
		}
		else 
		{
			return new File(nom).createNewFile();}
	}
	
		
	public String[] list(){//def le type + ecrire methode
		return null;
	}

	// syncLocalDir() renvoie la liste des fichiers qui sont sur le serveur. On r�cupere le chemin grace a un fichier f que l'on cr�e.
	public File[] syncLocalDir(){//def le type + ecrire methode
		File f = new File("");
		String[] liste = this.list(); 
		File[] listeFile = new File[liste.length];
		listeFile= f.listFiles(); 
		return listeFile; 
	}
	
	public File get( String nom,int checksum){//def le type + ecrire methode
		return null;
	}
	
	public void lock( String nom, int clientid, int checksum){
				
	}
	
	public void push(/*nom, contenu, clientid*/){//def le type + ecrire methode
		
	}
	

	
	
	
}
