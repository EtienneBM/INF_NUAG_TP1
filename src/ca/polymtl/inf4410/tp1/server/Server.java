package ca.polymtl.inf4410.tp1.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/*
	 * MÃ©thode accessible par RMI. Additionne les deux nombres passÃ©s en
	 * paramÃ¨tre.
	 */
	@Override
	public int execute(int a, int b) throws RemoteException {
		return a + b;
	}
	
	
	
	//---------------------------- part 2 --------------------------------------
	//cette medthode renvoie un id. 
	//A chaque fois que le serveur est arreté puis relancé la liste des id reccomence à 1
	public int generateclientid(){
		this.id = this.id + 1;
		return this.id;
	}
	
	public boolean create(String nom) throws Exception{//def le type + ecrire methode
		if (new File(nom).exists()){
			System.out.println("Le fichier " + nom + "existe deja"); 
			return false; 
		}
		else 
		{
			return new File(nom).createNewFile();}
	}
	
	//retourne la liste des fichiers présents dans le dossier courant
	//ne permet pas encore d'avoir l'info si le fichier est lock ou unlock
	public File[] list(){
		File curDir = new File(".");;
		return curDir.listFiles();
	}

	
	public void syncLocalDir(){//def le type + ecrire methode
		
	}
	
	//retourne le fihcier seulement si les checksum du client et du server sont différents
	public File get(String nom, String checksum) throws IOException, NoSuchAlgorithmException{//def le type + ecrire methode
		//Create checksum for this file
		File file = new File(nom);
		 
		//Use MD5 algorithm
		MessageDigest md5Digest = MessageDigest.getInstance("MD5");
		 
		//Get the checksum
		String localChecksum = getFileChecksum(md5Digest, file);
		
		//return file only if the client and the server checksum are differents
		if(localChecksum == checksum){
			return null;
		}
		else{
			return file;
		}
	}
	
	
	public void lock(/*nom, clientid, checksum*/){//def le type + ecrire methode
		
	}
	
	public void push(/*nom, contenu, clientid*/){//def le type + ecrire methode
		
	}
	
	
	
	
	//----------- other functions --------
	private static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
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

	
	
	
}
