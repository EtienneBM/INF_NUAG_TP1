package ca.polymtl.inf4410.tp1.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Client {
	public static void main(String[] args) throws Exception {
		//L'utilsateur n'a plus besoin de rentrer l'IP flottante du serveur qui est toujours la même.
		Client client = new Client("132.207.12.226");
		// En argument, le client rentre une commande parmi les 6 accessibles qui execute la fonction demandé
		// Certaines fonctions appellent un argument en plus. 
		String commande = "";
		if (args.length > 0) {
			commande = args[0];
		}
		if (commande.equals("list")){
			client.list(); 
		} else {
		if (commande.equals("create")){
			if (args.length > 1) {
				client.create(args[1]);
			}
			else {
				System.out.println("Vous devez nommer le fichier que vous creez.");
			}
		} else {
		if (commande.equals("syncLocalDir")){
			client.syncLocalDir(); 
		} else {
		if (commande.equals("get")){
			if (args.length > 1) {
				client.get(args[1]);
			}
			else {
				System.out.println("Vous devez nommer le fichier que vous voulez synchronisez.");
			}
		}else {
		if (commande.equals("lock")){
			if (args.length > 1) {
				client.lock(args[1]);
			}
			else {
				System.out.println("Vous devez nommer le fichier que vous voulez verouillez.");
			}
		}else {
		if (commande.equals("push")){
			if (args.length > 1) {
				client.push(args[1]);
			}
			else {
				System.out.println("Vous devez nommer le fichier que vous voulez mettre a jour sur le serveur.");
			}	 
		}}}}}}
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
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas dÃ©fini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}


	//-------------------Part 2---------------------------
	
	
	// affiche la liste des fichiers sur le serveur
	private void list() throws RemoteException {
			HashMap<String,String> liste = distantServerStub.list();
			// On parcours la HashMap avec un iterator
			Set<String> cles = liste.keySet();
			Iterator<String> it = cles.iterator();
			while (it.hasNext()){
			   Object cle = it.next(); 
			   //Pour chaque fichier, on verifie s'il est verouille.
			   if (liste.get(cle).equals("")){
				   System.out.println("* " + cle + " non verrouillé");
			   }
			   else {
				   System.out.println("* " + cle + " verrouillé par client "+liste.get(cle));
			   }
			}
			//On ecrit le nombre de fichiers stocké sur le serveur.
			System.out.println(liste.size()+" fichier(s)");
	}

	
	//La fonction get permet de recuperer un fichier qui est sur le serveur s'il est different de notre version. 
	private void get (String nom) throws NoSuchAlgorithmException, IOException{
		ArrayList<String> f =null;
		//si le fichier existe, on recupere le contenu du fichier si les checksums sont différents, sinon, null.
		//La fonction getFileCheksum permet de calculer le checksum associe à un fichier.
		f = distantServerStub.get(nom, getFileChecksum(new File(nom)));
		if (f!=null){
			// Si on a bien recupere le contenu d'un fichier, on le copie localement
			this.copieLocale(f,nom);
		}
		System.out.println(nom + " synchronisé");
	}



	//  create permet de créer un nouveau fichier avec un nom donné en paramètre. La fonction appelle la fonction create() sur le serveur distant qui crée le fichier et
	// qui renvoie un booléen si l'opération a réussi. 
		private void create (String nom) throws Exception {
			boolean success = distantServerStub.create(nom);
			if (success == true){
				System.out.println(nom +" ajouté.");
			}
			else {
				System.out.println(nom + " existe déjà.");
			}
			
		}


		// verouille le fichier et recupere le contenu pour le copier localement si le fichier a un cheksum different 
		// de celui du serveur
	private void lock(String nom) throws NoSuchAlgorithmException, IOException{
			ArrayList<String> f  = distantServerStub.lock(nom, this.haveAnId(), getFileChecksum(new File(nom))); 
			if (f!=null){
				this.copieLocale(f,nom);
			}
			HashMap<String,String> liste = distantServerStub.list();
			if (liste.get(nom).equals(this.haveAnId())){
				System.out.println("Le fichier a ete verouillé avec succes.");
			}
			else {
				System.out.println("Le fichier est déja verouillé par le client "+liste.get(nom));
			}
		}


	// Envoie un fichier et un contenu au serveur pour mettre a jour son fichier du meme nom. 
	// Il faut que le client verouille le fichier d'abord.
	private void push(String nom) throws IOException{
		// done permet de savoir si l'operation a bien eu lieu 
		boolean done = distantServerStub.push(nom, Client.Contenu(nom), this.haveAnId());
		if (done){
			System.out.println(nom + " a été envoyé au serveur");
		}
		else {
			System.out.println("operation refusée : vous devez d'abord verrouiller le fichier");
		}
	}



	// syncLocalDir récupere la liste des Files qui sont enregistrés sur le serveur. Puis la fonction, crée les fichiers en écrasant ceux qui existent déja. 
	private void syncLocalDir() throws IOException, NoSuchAlgorithmException {
		// Recuperation de la liste des contenus des fichiers
		ArrayList<ArrayList<String>> liste = distantServerStub.syncLocalDir();
		// Recuperation des noms des fichiers 
		HashMap<String, String> nomsFichiers = distantServerStub.list(); 
		// Mise en place d'un iterator pour parcourir la liste des contenus dans le meme ordre que la liste des noms 
		// du fichier
		Set<String> cles = nomsFichiers.keySet();
		Iterator<String> it = cles.iterator();
		for (ArrayList<String> f : liste){
			String key = it.next();
			// Pour chaque fichier, on copie localement la fonction 
			System.out.println("nom fichier : " + key);
			this.copieLocale(f,key);
		}
		}


	
	

	//Test si le client a déjà un fichier avec un Id
	//S'il en a déjà un il retourne l'id
	//s'il n'en a pas crée un fichier, y stock un nouvel id généré par le serveur et retourne l'id
	private String haveAnId() throws IOException{
		String path="idFile" ;
		if (new File(path).exists() || (new File(path).length() > 0)){
			// Le client a deja un id, on lit le fichier pour recuperer l'id 
			InputStream ips=new FileInputStream(path); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String line;
			line=br.readLine();
			br.close(); 
			return line;
		}
		else{
			// Le client n'a pas d'id.
			File myIdFile = new File(path);
			FileOutputStream is;
			// recuperation d'un id via le serveur
			int id = distantServerStub.generateclientid();
			try {
				is = new FileOutputStream(myIdFile);
				OutputStreamWriter osw = new OutputStreamWriter(is);    
	            Writer w = new BufferedWriter(osw);
	            w.write(""+id);
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
	
	
	// Contenu permet de mettre les lignes d'un fichier texte dans un tableau (ArrayList) de chaines de caractères
	public static ArrayList<String> Contenu (String fileName) throws IOException {
		LineNumberReader reader = new LineNumberReader(
				new InputStreamReader(new FileInputStream(fileName)));
		try {
			// Creation de la ArrayList
			ArrayList<String> list = new ArrayList<String>();
			String line;
			// Pour chaque ligne on la stocke dans le tableau
			while ( (line=reader.readLine()) != null) {
				list.add(line);
			}
			list.trimToSize();
			return list;
		} finally {
			reader.close();
		}
	}
	
	
	// La fonction getFileCheksum permet de calculer le cheksum d'un fichier 
	private static String getFileChecksum( File file) throws IOException, NoSuchAlgorithmException
	{
		if (!new File(file.getName()).exists()){
			// Si le fichier n'existe pas, la fonction renvoie "-1"
			return "-1";
		}
		else {
			// Calcul du checksum 
			MessageDigest digest = MessageDigest.getInstance("MD5");
		    FileInputStream fis = new FileInputStream(file);
		    byte[] byteArray = new byte[1024];
		    int bytesCount = 0; 
		    while ((bytesCount = fis.read(byteArray)) != -1) {
		        digest.update(byteArray, 0, bytesCount);
		    };
		    fis.close();
		    byte[] bytes = digest.digest();
		    StringBuilder sb = new StringBuilder();
		    for(int i=0; i< bytes.length ;i++)
		    {
		        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		   return sb.toString();
		}
	}


	// La fonction copieLocale permet de remplacer le contenu d'un fichier a partir de son contenu 
	// stocké dans une ArrayList
	private void copieLocale (ArrayList<String> f,String nom) throws NoSuchAlgorithmException, IOException{
		FileWriter fw = new FileWriter(nom);
		BufferedWriter output = new BufferedWriter(fw);
		for (String line : f){
			output.write(line);
			output.newLine();
		} 
		output.flush(); 
		output.close(); 
	}
	
}
