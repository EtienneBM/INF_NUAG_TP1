package ca.polymtl.inf4410.tp1.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Client {
	public static void main(String[] args) {
		String distantHostname = null;

		if (args.length > 0) {
			distantHostname = args[0];
		}

		Client client = new Client(distantHostname);
	}

	
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		
		localServerStub = loadServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
		}
	}

	

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas dÃ©fini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}


	
	//-------------------Part 2---------------------------
	//Test si le client a déjà un fichier avec un Id
	//S'il en a déjà un il ne se passe rien
	//s'il n'en a pas crée un fichier et y stock un nouvel id généré par le serveur.
	@SuppressWarnings("unused")
	private void haveAnId(String path){
		if (new File(path).exists() || (new File(path).length() > 0)){
			System.out.println("You already have an Id");
		}
		else{
			File myIdFile = new File(path);
			FileOutputStream is;
			int id = distantServerStub.generateclientid();
			try {
				is = new FileOutputStream(myIdFile);
				OutputStreamWriter osw = new OutputStreamWriter(is);    
	            Writer w = new BufferedWriter(osw);
	            w.write(id);
	            w.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("File not found exception " + e);
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Err in file creation " + e);
				e.printStackTrace();
			}
		}
	}
	
	


	@SuppressWarnings("unused")
	private void list() {
		File liste[] = distantServerStub.list();
		for (int i =0 ; i<liste.length;i++){
			System.out.println("* " + liste[i]);
		}
		System.out.println(liste.length + " fichier(s)");
	}

	private void create (String nom) throws Exception {
		boolean success = distantServerStub.create(nom);
		if (success == true){
			System.out.println(nom +" ajouté.");
		}
		else {
			System.out.println(nom + " existe déjà.");
		}
		
	}
	
	
}
