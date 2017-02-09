package ca.polymtl.inf4410.tp1.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {
	// Permet de savoir si le fichier est verouillé.
	private HashMap<String,String> verrouillage ; 
	// Permet de stocker l'identifiant courant
	private int id = 0;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
		// Creation de la HashMap vide
		this.verrouillage=new HashMap<String,String>(50); // Mise a jour de la HashMap au lancement du serveur
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
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	
	
	//---------------------------- part 2 --------------------------------------
	// Cette methode renvoie un id. 
	// A chaque fois, que le serveur est arreté puis relancé la liste des id recommence à 1
	public int generateclientid() throws RemoteException{
		this.id = this.id + 1;
		return this.id;
	}
	
	// Create permet de creer un fichier si il n'existe pas. La fonction renvoie un booléen de valeur true si le fichier
	// a bien été créé, et false sinon ce qui permet au client de vérifier l'execution de la méthode distante.
	public boolean create(String nom) throws Exception, RemoteException{
		if (new File(nom).exists()){
			return false;
		}
		else 
		{
			//On met a jour la HashMap avec le nouveau fichier etant deverouillé
			this.verrouillage.put(nom, ""); 
			return new File(nom).createNewFile();
			}
	}
	 
	// Retourne le contenu du fichier seulement si les checksum du client et du server sont différents
		public ArrayList<String> get(String nom, String checksum) throws IOException, NoSuchAlgorithmException, RemoteException{
			File file = new File(nom);
			// Force l'envoie du fichier avec la valeur -1 envoyé par le client
			if (Integer.parseInt(checksum)==-1){
				return Server.Contenu(nom); 
			}
			else {
				// Recupere le checksum du fichier enregistré sur le serveur. 
				String localChecksum = getFileChecksum(file);
				// Retourne le contenu du fichier si et seulement si les deux checksums sont differents.
				if(localChecksum == checksum){
					return null;
				}
				else{
					// On appelle la fonction Contenu qui permet de recuperer une ArrayLisy de String
					return Server.Contenu(nom);
				}
			}
		}
		
		
		
		
		//----------- other functions we need --------
		//Permet de retourner le checksum associé à un fichier
		private static String getFileChecksum( File file) throws IOException, NoSuchAlgorithmException
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
		    //Creation d'un FileInputStream 
		    FileInputStream fis = new FileInputStream(file);
		    byte[] byteArray = new byte[1024];
		    int bytesCount = 0; 
		    //Mise a jour apres lecture des données
		    while ((bytesCount = fis.read(byteArray)) != -1) {
		        digest.update(byteArray, 0, bytesCount);
		    };
		    //Fermeture du FileInputStream
		    fis.close();
		    //Recuperation des bytes
		    byte[] bytes = digest.digest();
		    //Convertisseur du format des bytes
		    StringBuilder sb = new StringBuilder();
		    for(int i=0; i< bytes.length ;i++)
		    {
		        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    //return le checksum
		   return sb.toString();
		}
		
		//retourne la liste des fichiers présents dans le dossier courant avec l'identifiant du client
	public HashMap<String,String> list() throws RemoteException{
			return this.verrouillage; 
	}

	// syncLocalDir() renvoie la liste des fichiers qui sont sur le serveur..
	public ArrayList<ArrayList<String>> syncLocalDir() throws IOException{
		//recuperation de la liste des noms du fichier.
		HashMap<String,String> listeNom = this.list();
		// On va stocke les contenus dans une ArrayList
		ArrayList<ArrayList<String>> listeContenus = new ArrayList<ArrayList<String>>();
		Set<String> cles = listeNom.keySet();
		Iterator<String> it = cles.iterator();
		while (it.hasNext()){
			String key = it.next();
			// Pour chaque fichier, on stocke son contenu dans la HashMap
			listeContenus.add(Server.Contenu(key));
		}
		return listeContenus;
	}
		
	// La fonction push permet de réécrire le fichier nom avec le contenu fourni si le client est celui qui a verouille
	public boolean push(String nom, ArrayList<String> contenu, String clientid) throws IOException, RemoteException{
		// verification de l'existance du fichier et du verouillage du fichier par le bon client
		if(this.verrouillage.containsKey(nom) && this.verrouillage.get(nom).equals(clientid) ){
			// remplacement du contenu du fichier 

				FileWriter fw = new FileWriter(nom);
				BufferedWriter output = new BufferedWriter(fw);
				//on parcourt le contenu envoyé
				for (String line : contenu){
					// on réécrit chaque ligne dans le fichier
					output.write(line);
					output.newLine();
				} 
				output.flush(); 
				output.close(); 
		    this.verrouillage.put(nom, ""); 
			return true; 
		}
		else {
			// soit le fichier n'existe pas, soit le client n'avait pas verouille le fichier 
			return false; 
		}
	}

	// La fonction Contenu permet de convertir le contenu d'un fichier en un ArrayList de String. 
	// Chaque string correspond à une ligne du fichier. 
	public static ArrayList<String> Contenu (String fileName) throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(fileName)));
		try {
			ArrayList<String> list = new ArrayList<String>();
			String line;
			while ( (line=reader.readLine()) != null) {
				list.add(line);
			}
			// minimise la memoire que prend l'ArrayList
			list.trimToSize();
			return list;
		} finally {
			reader.close();
		}
	}
	//la fonction lock() permet au client de verouiller un fichier s'il ne l'est pas déjà.
	public ArrayList<String> lock(String nom, String clientid, String checksum) throws IOException, RemoteException, NoSuchAlgorithmException {
		// verification si le fichier existe et s'il n'est pas déja verouillé.
		if (this.verrouillage.containsKey(nom) && this.verrouillage.get(nom)=="" ){
			this.verrouillage.put(nom, clientid);
			File file = new File(nom);
			// Retourne le checksum
			String localChecksum = getFileChecksum(file);
			
			//retourne le contenu d'un fichier si et seulement si les deux checksum sont différents.
			if(localChecksum == checksum){
				return null;
			}
			else{
				return Server.Contenu(nom);
			}
		}
		else {
			return null;
		}
	}

	
}
