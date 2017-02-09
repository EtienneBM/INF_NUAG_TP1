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
	private HashMap<String,String> verrouillage ; 
	private int id = 0;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
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
	//cette methode renvoie un id. 
	//A chaque fois, que le serveur est arreté puis relancé la liste des id recommence à 1
	public int generateclientid() throws RemoteException{
		this.id = this.id + 1;
		return this.id;
	}
	
	//Create permet de creer un fichier si il n'existe pas. La fonction renvoie un booléen de valeur true si le fichier
	//a bien été créé, et false sinon ce qui permet au client de vérifier l'execution de la méthode distante.
	public boolean create(String nom) throws Exception, RemoteException{
		if (new File(nom).exists()){
			return false;
		}
		else 
		{
			this.verrouillage.put(nom, ""); //On met a jour la HashMap avec le nouveau fichier etant deverouillé
			return new File(nom).createNewFile();
			}
	}
	 
	//retourne le fichier seulement si les checksum du client et du server sont différents
		public ArrayList<String> get(String nom, String checksum) throws IOException, NoSuchAlgorithmException, RemoteException{
			//Create checksum for this file
			System.out.println("Fonction get appelé");
			File file = new File(nom);
			// Forcer l'envoie du fichier avec la valeur -1 envoyé par le client
			if (Integer.parseInt(checksum)==-1){
				System.out.println("Le checksum est bien -1");
				System.out.println(file.getName());
				return Server.Contenu(nom); 
			}
			else {
				System.out.println("Le checksum n'set pas -1"); 
				//Get the checksum
				String localChecksum = getFileChecksum(file);
				//return file only if the client and the server checksum are differents
				if(localChecksum == checksum){
					return null;
				}
				else{
					return Server.Contenu(nom);
				}
			}
		}
		
		
		
		
		//----------- other functions we need --------
		//Permet de retourner le checksum associé à un fichier
		private static String getFileChecksum( File file) throws IOException, NoSuchAlgorithmException
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");

			
		    //Get file input stream for reading the file content
		    FileInputStream fis = new FileInputStream(file);
		     
		    //Create byte array to read data in chunks
		    byte[] byteArray = new byte[1024];
		    int bytesCount = 0; 
		      
		    //Read file data and update in message digest
		    while ((bytesCount = fis.read(byteArray)) != -1) {
		        digest.update(byteArray, 0, bytesCount);
		    };
		     
		    //close the stream; We don't need it now.
		    fis.close();
		     
		    //Get the hash's bytes
		    byte[] bytes = digest.digest();
		     
		    //This bytes[] has bytes in decimal format;
		    //Convert it to hexadecimal format
		    StringBuilder sb = new StringBuilder();
		    for(int i=0; i< bytes.length ;i++)
		    {
		        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		     
		    //return complete hash
		   return sb.toString();
		}
		//retourne la liste des fichiers présents dans le dossier courant avec l'identifiant du client
		//ne permet pas encore le nom du client 
	public HashMap<String,String> list() throws RemoteException{
			return this.verrouillage; 
	}

	// syncLocalDir() renvoie la liste des fichiers qui sont sur le serveur. On récupere le chemin grace a un fichier f 
	//que l'on crée.
	public ArrayList<ArrayList<String>> syncLocalDir() throws IOException{
		HashMap<String,String> listeNom = this.list();
		ArrayList<ArrayList<String>> listeContenus = new ArrayList<ArrayList<String>>();
		Set<String> cles = listeNom.keySet();
		Iterator<String> it = cles.iterator();
		while (it.hasNext()){
			String key = it.next();
			listeContenus.add(Server.Contenu(key));
		}
		return listeContenus;
	}
		
	// La fonction push permet de réécrire le fichier nom avec le contenu fourni si le client est celui qui a verouille
	public boolean push(String nom, ArrayList<String> contenu, String clientid) throws IOException, RemoteException{
		// verification de l'existance du fichier et du verouillage du fichier par le bon client
		if(this.verrouillage.containsKey(nom)){
			System.out.println("La clé existe");
			System.out.println(this.verrouillage.get(nom));
			System.out.println(clientid);
			if ( this.verrouillage.get(nom).equals(clientid) ){
				System.out.println("lid du client est le bon. On est dans la boucle");//pb on rentre pas 
			// remplacement du contenu du fichier par lecture et ecriture
				FileWriter fw = new FileWriter(nom);
				BufferedWriter output = new BufferedWriter(fw);
				for (String line : contenu){
					output.write(line);
					output.newLine();
				} 
				output.flush(); 
				output.close(); 
		    this.verrouillage.put(nom, ""); 
			return true; 
		}
			else {
				return false ; 
			}}
		else {
			// soit le fichier n'existe pas, soit le client n'avait pas verouille le fichier 
			return false; 
		}
	}

	public static ArrayList<String> Contenu (String fileName) throws IOException {
		 
		// Note : on devrait spécifier le Charset !!!!
		LineNumberReader reader = new LineNumberReader(
				new InputStreamReader(new FileInputStream(fileName)));
		try {
			ArrayList<String> list = new ArrayList<String>();
			String line;
			while ( (line=reader.readLine()) != null) {
				list.add(line);
			}
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
			//Get the checksum
			String localChecksum = getFileChecksum(file);
			
			//return file only if the client and the server checksum are differents
			if(localChecksum == checksum){
				return null;
			}
			else{
				return Server.Contenu(nom);
			}
		}
		else {
			System.out.println("Le fichier est déja verouillé par le client "+clientid);
			return null;
		}
	}

	
}
