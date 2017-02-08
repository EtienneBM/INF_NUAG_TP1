package ca.polymtl.inf4410.tp1.shared;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public interface ServerInterface extends Remote {
	int generateclientid();
	boolean create(String nom) throws Exception;
	HashMap<String,String> list();
	File[] syncLocalDir();
	File get(String nom, String checksum) throws IOException, NoSuchAlgorithmException;
	File lock(String nom, String clientid, String checksum) throws IOException, NoSuchAlgorithmException;
	boolean push(String nom, File contenu, String  clientid) throws IOException;
}
