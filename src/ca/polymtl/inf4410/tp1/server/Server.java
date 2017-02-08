package ca.polymtl.inf4410.tp1.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
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
		this.verrouillage=new HashMap<String,String>(50);
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
	//cette medthode renvoie un id. 
	//A chaque fois que le serveur est arreté puis relancé la liste des id reccomence à 1
	public int generateclientid(){
		this.id = this.id + 1;
		return this.id;
	}
	//Create permet de creer un fichier si il n'existe pas. La fonction renvoie un booléen de valeur true si le fichier a bien été créé, et false sinon.
	public boolean create(String nom) throws Exception{//def le type + ecrire methode
		if (new File(nom).exists()){
			System.out.println("Le fichier " + nom + "existe deja"); 
			this.verrouillage.put(nom, "");
			return false;
		}
		else 
		{
			return new File(nom).createNewFile();}
	}
	 
	//retourne le fichier seulement si les checksum du client et du server sont différents
		public File get(String nom, String checksum) throws IOException, NoSuchAlgorithmException{
			//Create checksum for this file
			File file = new File(nom);
			if (Integer.parseInt(checksum)==-1){
				return file; 
			}
			else {
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
		}
		
		
		
		
		//----------- other functions we need --------
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
		//retourne la liste des fichiers présents dans le dossier courant
		//ne permet pas encore d'avoir l'info si le fichier est lock ou unlock
		public HashMap<String,String> list(){
			return this.verrouillage; 
		}

	// syncLocalDir() renvoie la liste des fichiers qui sont sur le serveur. On récupere le chemin grace a un fichier f que l'on crée.
	public File[] syncLocalDir(){//def le type + ecrire methode
		File curDir = new File(".");
		return curDir.listFiles();
	}
		
	public boolean push(String nom, File contenu, String clientid) throws IOException{//def le type + ecrire methode
		if(this.verrouillage.containsKey(nom) && this.verrouillage.get(nom)==clientid ){
			FileInputStream src = new FileInputStream(contenu);
		    FileOutputStream dest = new FileOutputStream(nom);
		 
		    FileChannel inChannel = src.getChannel();
		    FileChannel outChannel = dest.getChannel();
		 
		    for (ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
		         inChannel.read(buffer) != -1;
		         buffer.clear()) {
		       buffer.flip();
		       while (buffer.hasRemaining()) outChannel.write(buffer);
		    }
		 
		    src.close();
		    dest.close();
			return true; 
		}
		else {
			return false; 
		}
	}

	public File lock(String nom, String clientid, String checksum) throws IOException, NoSuchAlgorithmException {
		if (this.verrouillage.containsKey(nom) && this.verrouillage.get(nom)=="" ){
			this.verrouillage.put(nom, clientid);
			File file = new File(nom);
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
		else {
			System.out.println("Le fichier est déja verouillé par le client "+clientid);
			return null;
		}
	}

	
}
