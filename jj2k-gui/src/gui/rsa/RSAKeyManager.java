package gui.rsa;

import jj2000.j2k.util.*;

import java.util.*;
import java.math.*;
import java.io.*;

public class RSAKeyManager {

    /** Hashtable containing RSA private keys */
    private Hashtable privKeyTb = new Hashtable();

    /** Hashtable containing RSA public keys */
    private Hashtable pubKeyTb = new Hashtable();

    /** Construct a RSAKeyManager instance and load saved public and
     * private keys from file. */
    public RSAKeyManager() {
	loadFromFile();
    }

    /** */
    public Hashtable getPrivateKeys() {
	return privKeyTb;
    }

    /** */
    public Hashtable getPublicKeys() {
	return pubKeyTb;
    }

    public void removePubKey(String key) {
	pubKeyTb.remove(key);
    }

    public void removePrivKey(String key) {
	privKeyTb.remove(key);
    }

    /** Save a public RSA consisting in an exponent and a modulus */
    public boolean savePublicKey(String name,BigInteger exponent,
				 BigInteger modulus) {
	String str;
	for(Enumeration e=pubKeyTb.keys(); e.hasMoreElements(); ) {
	    str = (String)e.nextElement();
	    if(str.equals(name)) {
		return false;
	    }
	}
	Object[] obj = {exponent,modulus};
	pubKeyTb.put(name,obj);
	return true;
    }

    /** Save a private key consisting in an exponent and a modulus */
    public boolean savePrivateKey(String name,BigInteger exponent,
				  BigInteger modulus) {
	String str;
	for(Enumeration e=privKeyTb.keys(); e.hasMoreElements(); ) {
	    str = (String)e.nextElement();
	    if(str.equals(name)) {
		return false;
	    }
	}
	Object[] obj = {exponent, modulus};
	privKeyTb.put(name,obj);
	return true;
    }

    /** Save public and private to specific files (erase any prior
     * information contained in these files if needed) */
    public void saveToFile() {
	File homeDir = new File(System.getProperty("user.home"));
	if(!homeDir.exists()) {
	    homeDir = new File(".");
	}
	File jj2kDir = new File(homeDir.getPath()+File.separator+".jj2000");
	if(jj2kDir.exists() && jj2kDir.isFile()) {
	    jj2kDir.renameTo(new File(homeDir.getPath()+File.separator+
				      ".jj2000-saved"));
	} else if(!jj2kDir.exists()) {
	    jj2kDir.mkdir();
	}

	// Get Public keys (if file exists)
	File pubKeyFile = 
	    new File(jj2kDir.getPath()+File.separator+".jj2k-RSA_public_keys");
	try {
	    // Open the input stream
	    FileOutputStream fis = new FileOutputStream(pubKeyFile);
	    ObjectOutputStream ois = new ObjectOutputStream(fis);

	    // Retrieve all the public keys from the Hashtable
	    String htKey;
	    Object[] obj;
	    BigInteger exp, mod;
	    Integer nKeys = new Integer(pubKeyTb.size());
	    ois.writeObject(nKeys);
	    Object tmp;
	    for(Enumeration e=pubKeyTb.keys(); e.hasMoreElements(); ) {
 		htKey = (String)e.nextElement();
		ois.writeObject(htKey);

		obj = (Object[])pubKeyTb.get(htKey);
		exp = (BigInteger)obj[0];
		ois.writeObject(exp);
		mod = (BigInteger)obj[1];
		ois.writeObject(mod);
	    }

	    // Close the input stream
	    fis.close();
	} catch(IOException e) {
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.ERROR,"Error when saving RSA keys");
	}
	// Get private keys (if file exists)
	File privKeyFile = 
	    new File(jj2kDir.getPath()+File.separator+
		     ".jj2k-RSA_private_keys");
	try {
	    // Open the input stream
	    FileOutputStream fis = new FileOutputStream(privKeyFile);
	    ObjectOutputStream ois = new ObjectOutputStream(fis);

	    // Retrieve all the private keys from the Hashtable
	    String htKey;
	    Object[] obj;
	    BigInteger exp, mod;
	    Integer nKeys = new Integer(privKeyTb.size());
	    ois.writeObject(nKeys);
	    for(Enumeration e=privKeyTb.keys(); e.hasMoreElements(); ) {
 		htKey = (String)e.nextElement();
		ois.writeObject(htKey);

		obj = (Object[])privKeyTb.get(htKey);
		exp = (BigInteger)obj[0];
		ois.writeObject(exp);
		mod = (BigInteger)obj[1];
		ois.writeObject(mod);
	    }

	    // Close the input stream
	    fis.close();
	} catch(IOException e) {
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.ERROR,"Error when saving RSA keys");
	}
    }

    /** Load public and private keys as they are saved in appropriate files */
    private void loadFromFile() {
	File homeDir = new File(System.getProperty("user.home"));
	if(!homeDir.exists()) {
	    homeDir = new File(".");
	}
	File jj2kDir = new File(homeDir.getPath()+File.separator+".jj2000");
	if(jj2kDir.exists() && jj2kDir.isFile()) {
	    jj2kDir.renameTo(new File(homeDir.getPath()+File.separator+
				      ".jj2000-saved"));
	} else if(!jj2kDir.exists()) {
	    jj2kDir.mkdir();
	}

	// Get public keys (if file exists)
	File pubKeyFile = 
	    new File(jj2kDir.getPath()+File.separator+".jj2k-RSA_public_keys");
	if(pubKeyFile.exists()) {
	    try {
		// Open the input stream
		FileInputStream fis = new FileInputStream(pubKeyFile);
		ObjectInputStream ois = new ObjectInputStream(fis);

		// Read number of saved public keys
		int nKeys = ((Integer)ois.readObject()).intValue();
		String htKey;
		BigInteger exp, mod;
		for(int i=0; i<nKeys; i++) {
		    htKey = (String)ois.readObject();
		    exp = (BigInteger)ois.readObject();
		    mod = (BigInteger)ois.readObject();
		    if(!pubKeyTb.containsKey(htKey)) {
			Object[] obj = {exp, mod};
			pubKeyTb.put(htKey,obj);
		    }
		}
		// Close the input stream
		fis.close();
	    } catch(IOException e) {
		FacilityManager.getMsgLogger().
		    printmsg(MsgLogger.ERROR,"Error reading from "+pubKeyFile.
			     getPath());
	    } catch(ClassNotFoundException e) {
		FacilityManager.getMsgLogger().
		    printmsg(MsgLogger.ERROR,"Invalid RSA public key file: "+
			     pubKeyFile.getPath());
	    }
	} 
	
	// Get private keys (if file exists)
	File privKeyFile = new File(jj2kDir.getPath()+File.separator+
				    ".jj2k-RSA_private_keys");
	if(privKeyFile.exists()) {
	    try {
		// Open the input stream
		FileInputStream fis = new FileInputStream(privKeyFile);
		ObjectInputStream ois = new ObjectInputStream(fis);

		// Read number of saved private keys
		int nKeys = ((Integer)ois.readObject()).intValue();
		String htKey;
		BigInteger exp, mod;
		for(int i=0; i<nKeys; i++) {
		    htKey = (String)ois.readObject();
		    exp = (BigInteger)ois.readObject();
		    mod = (BigInteger)ois.readObject();
		    if(!privKeyTb.containsKey(htKey)) {
			Object[] obj = {exp, mod};
			privKeyTb.put(htKey,obj);
		    }
		}
		// Close the input stream
		fis.close();
	    } catch(IOException e) {
		FacilityManager.getMsgLogger().
		    printmsg(MsgLogger.ERROR,"Error reading from "+privKeyFile.
			     getPath());
	    } catch(ClassNotFoundException e) {
		FacilityManager.getMsgLogger().
		    printmsg(MsgLogger.ERROR,"Invalid RSA private key file: "+
			     privKeyFile.getPath());
	    }
	} 
    }
}
