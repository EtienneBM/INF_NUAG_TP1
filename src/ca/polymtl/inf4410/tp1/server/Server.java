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
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lanc� ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	
	
	//---------------------------- part 2 --------------------------------------
	//cette methode renvoie un id. 
	//A chaque fois, que le serveur est arret� puis relanc� la liste des id recommence � 1
	public int generateclientid() throws RemoteException{
		this.id = this.id + 1;
		return this.id;
	}
	
	//Create permet de creer un fichier si il n'existe pas. La fonction renvoie un bool�en de valeur true si le fichier
	//a bien �t� cr��, et false sinon ce qui permet au client de v�rifier l'execution de la m�thode distante.
	public boolean create(String nom) throws Exception, RemoteException{
		if (new File(nom).exists()){
			return false;
		}
		else 
		{
			this.verrouillage.put(nom, ""); //On met a jour la HashMap avec le nouveau fichier etant deverouill�
			return new File(nom).createNewFile();
			}
	}
	 
	//retourne le fichier seulement si les checksum du client et du server sont diff�rents
		public ArrayList<String> get(String nom, String checksum) throws IOException, NoSuchAlgorithmException, RemoteException{
			//Create checksum for this file
			File file = new File(nom);
			// Forcer l'envoie du fichier avec la valeur -1 envoy� par le client
			if (Integer.parseInt(checksum)==-1){
				return Server.Contenu(nom); 
			}
			else {
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
		//Permet de retourner le checksum associ� � un fichier
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
		//retourne la liste des fichiers pr�sents dans le dossier courant avec l'identifiant du client
		//ne permet pas encore le nom du client 
	public HashMap<String,String> list() throws RemoteException{
			return this.verrouillage; 
	}

	// syncLocalDir() renvoie la liste des fichiers qui sont sur le serveur. On r�cupere le chemin grace a un fichier f 
	//que l'on cr�e.
	public ArrayList<ArrayList<String>> syncLocalDir() throws IOException{
		HashMap<String,String> listeNom = this.list();
		ArrayList<ArrayList<String>> listeContenus = new ArrayList<ArrayList<String>>();
		Set<String> cles = listeNom.keySet();
		Iterator<String> it = cles.iterator();
		while (it.hasNext()){
			String key = it.next();
			listeContenus.add(Server.Contenu(key));
			System.out.println("Ajout de "+key);
		}
		return listeContenus;
	}
		
	// La fonction push permet de r��crire le fichier nom avec le contenu fourni si le client est celui qui a verouille
	public boolean push(String nom, ArrayList<String> contenu, String clientid) throws IOException, RemoteException{
		// verification de l'existance du fichier et du verouillage du fichier par le bon client
		if(this.verrouillage.containsKey(nom) && this.verrouillage.get(nom).equals(clientid) ){
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
			// soit le fichier n'existe pas, soit le client n'avait pas verouille le fichier 
			return false; 
		}
	}

	public static ArrayList<String> Contenu (String fileName) throws IOException {
		 
		// Note : on devrait sp�cifier le Charset !!!!
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
	//la fonction lock() permet au client de verouiller un fichier s'il ne l'est pas d�j�.
	public ArrayList<String> lock(String nom, String clientid, String checksum) throws IOException, RemoteException, NoSuchAlgorithmException {
		// verification si le fichier existe et s'il n'est pas d�ja verouill�.
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
			return null;
		}
	}

	
}
