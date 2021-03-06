package ca.polymtl.inf4410.tp1.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Client {
	public static void main(String[] args) throws Exception {
		String distantHostname = null;
		String commande = "";
		
		if (args.length > 0) {
			distantHostname = args[0];
		}

		@SuppressWarnings("unused")
		Client client = new Client(distantHostname);
		if (args.length > 1) {
			commande = args[1];
		}
		if (commande.equals("list")){
			client.list(); 
		}
		if (commande.equals("create")){
			client.create(args[2]); 
		}
		if (commande.equals("syncLocalDir")){
			client.syncLocalDir(); 
		}
		if (commande.equals("get")){
			client.get(args[2]); 
		}
		if (commande.equals("lock")){
			client.lock(args[2]); 
		}
		if (commande.equals("push")){
			client.push(args[2]); 
		}
	}

	
	private ServerInterface distantServerStub = null;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

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
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}


	
	//-------------------Part 2---------------------------
	//Test si le client a d�j� un fichier avec un Id
	//S'il en a d�j� un il ne se passe rien
	//s'il n'en a pas cr�e un fichier et y stock un nouvel id g�n�r� par le serveur.
	private String haveAnId(String path) throws IOException{
		if (new File(path).exists() || (new File(path).length() > 0)){
			//recuperation de id 
			BufferedReader in = new BufferedReader(new FileReader(path));
			String line = in.readLine() ;
			in.close();
			return line;
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
				System.out.println("File not found exception " + e);
				e.printStackTrace();
			}catch (IOException e) {
				System.out.println("Err in file creation " + e);
				e.printStackTrace();
			}
			return ""+id;
		}
	}
	
	

// affiche la liste des fichiers sur le serveur
	@SuppressWarnings("unused")
	private void list() throws RemoteException {
			HashMap<String,String> liste = distantServerStub.list();
			Set<String> cles = liste.keySet();
			Iterator<String> it = cles.iterator();
			while (it.hasNext()){
			   Object cle = it.next(); 
			   if (liste.get(cle).equals("")){
				   System.out.println("* " + cle + " non verrouill�");
			   }
			   else {
				   System.out.println(liste.get(cle));
				   System.out.println("* " + cle + " verrouill� par client "+liste.get(cle));
			   }
			}
			System.out.println(liste.size()+" fichier(s)");
	}

//  create permet de cr�er un nouveau fichier avec un nom donn� en param�tre. La fonction appelle la fonction create() sur le serveur distant qui cr�e le fichier et
// qui renvoie un bool�en si l'op�ration a r�ussi. 
	@SuppressWarnings("unused")
	private void create (String nom) throws Exception {
		boolean success = distantServerStub.create(nom);
		if (success == true){
			System.out.println(nom +" ajout�.");
		}
		else {
			System.out.println(nom + " existe d�j�.");
		}
		
	}
	
// syncLocalDir r�cupere la liste des Files qui sont enregistr�s sur le serveur. Puis la fonction, cr�e les fichiers en �crasant ceux qui existent d�ja. 
	@SuppressWarnings("unused")
	private void syncLocalDir() throws IOException {
		File[] liste = distantServerStub.syncLocalDir();
		for (int i =0 ; i<liste.length;i++){
			if (liste[i].exists())
			{liste[i].delete();}
			liste[i].createNewFile();
			}
		}
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
	@SuppressWarnings("unused")
	private void get (String nom) throws NoSuchAlgorithmException, IOException{
		File f ;
		if (new File(nom).exists()){
			f = distantServerStub.get(nom, getFileChecksum(new File(nom)));
		}
		else {
			f = distantServerStub.get(nom, "-1");		
		}
		if (f!=null){
			this.copieLocale(f);
		}
		System.out.println(nom + " synchronis�");
	}
	
	@SuppressWarnings("unused")
	private void lock(String nom) throws NoSuchAlgorithmException, IOException{
		String checksum = "-1"; 
		File f  = distantServerStub.lock(nom, this.haveAnId(nom), checksum); 
		if (f!=null){
			this.copieLocale(f);
		}
	}
	
	@SuppressWarnings("unused")
	private void push(String nom) throws IOException{
		File f = new File(nom); 
		boolean done = distantServerStub.push(nom, f, this.haveAnId(nom));
		if (done){
			System.out.println(nom + " a �t� envoy� au serveur");
		}
		else {
			System.out.println("operation refus�e : vous devez d'abord verrouiller le fichier");
		}
		}
		
	private void copieLocale (File f){
		FileInputStream fis = null;
		FileOutputStream fos = null;
		System.out.println(f.getName());
	      try {	
	    	  System.out.println("3");
	         fis = new FileInputStream(f);//pb
	         System.out.println("4");
	         fos = new FileOutputStream(new File(f.getName()));
	         System.out.println("5");
	         byte[] buf = new byte[8];
	         System.out.println("6");
	         int n =0;
	         System.out.println("7");
	         while ((n = fis.read(buf)) >= 0) {
		            // On �crit dans notre deuxi�me fichier avec l'objet ad�quat
		            fos.write(buf);
		            // On affiche ce qu'a lu notre boucle au format byte et au
		            // format char
		            for (byte bit : buf) {
		               System.out.print("\t" + bit + "(" + (char) bit + ")");
		            }
		            System.out.println("");
		            //Nous r�initialisons le buffer � vide
		            //au cas o� les derniers byte lus ne soient pas un multiple de 8
		            //Ceci permet d'avoir un buffer vierge � chaque lecture et ne pas avoir de doublon en fin de fichier
		            buf = new byte[8];
	         }
		   } catch (FileNotFoundException e) {
		         // Cette exception est lev�e si l'objet FileInputStream ne trouve
		         // aucun fichier
		         e.printStackTrace();
		      } catch (IOException e) {
		         // Celle-ci se produit lors d'une erreur d'�criture ou de lecture
		         e.printStackTrace();
		      } finally {
		         // On ferme nos flux de donn�es dans un bloc finally pour s'assurer
		         // que ces instructions seront ex�cut�es dans tous les cas m�me si
		         // une exception est lev�e !
		         try {
		            if (fis != null)
		               fis.close();
		         } catch (IOException e) {
		            e.printStackTrace();
		         }
		         try {
		            if (fos != null)
		               fos.close();
		         } catch (IOException e) {
		            e.printStackTrace();
		         }
		      }
	}
}
