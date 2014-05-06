package gui.rsa;

import cryptix.provider.rsa.*;

import jj2000.j2k.util.*;

import java.security.interfaces.*;
import java.security.*;
import javax.swing.*;
import java.math.*;
import java.awt.*;

/** Threading ready interface for generation of RSA keys pair using
 * the cryptix implementation of RSA. */
public class RSAKeyProducer implements Runnable {

    /** Reference to the button pressed in the GUI to launch the
     * production process. */
    private JButton genBut;
    /** Reference to the button pressed for saving the generated keys */
    private JButton keySaveBut;
    /** Reference to the text fields used to display computed keys */
    private JTextField privModTF, privExpTF, pubModTF, pubExpTF;

    /** Desired modulus length (in bits) */
    private int modLen;

    /** Class constructor */
    public RSAKeyProducer(JButton genBut,JButton keySaveBut,
			  JTextField privExpTF,JTextField pubExpTF,
			  JTextField pubModTF,int modLen) { 
	this.genBut = genBut; 
	this.privExpTF = privExpTF;
	this.pubModTF = pubModTF;
	this.pubExpTF = pubExpTF;
	this.modLen = modLen;
	this.keySaveBut = keySaveBut;
    }

    /** Throw the key generation process */
    public void run() {
	// Disable buttons during computation
	genBut.setEnabled(false);
	keySaveBut.setEnabled(false);

	// Initialize key pair generator
	BaseRSAKeyPairGenerator kGen = new BaseRSAKeyPairGenerator();
	SecureRandom srand = new SecureRandom();
	kGen.initialize(modLen,srand);

	// Generate pair
	KeyPair keys = kGen.generateKeyPair();

	// Private key
	RawRSAPrivateKey privKey = (RawRSAPrivateKey)keys.getPrivate();
	privExpTF.setText(""+privKey.getExponent());

	// Public key and modulus
	RawRSAPublicKey pubKey = (RawRSAPublicKey)keys.getPublic();
	pubModTF.setText(""+pubKey.getModulus());
	pubExpTF.setText(""+pubKey.getExponent());

	// Re-enable buttons after computation
	genBut.setEnabled(true);
	keySaveBut.setEnabled(true);
    }

    /** Simple method to generate a pair of RSA keys */
    public static final void main(String[] argv) {
	// Initialize key pair generator
	BaseRSAKeyPairGenerator kGen = new BaseRSAKeyPairGenerator();
	SecureRandom srand = new SecureRandom();

	int modLen = 128;
	if(argv.length==1) {
	    modLen = (new Integer(argv[0])).intValue();
	}
	kGen.initialize(modLen,srand);
	
	// Generate pair
	KeyPair keys = kGen.generateKeyPair();

	// Private key
	RawRSAPrivateKey privKey = (RawRSAPrivateKey)keys.getPrivate();
	// Public key and modulus
	RawRSAPublicKey pubKey = (RawRSAPublicKey)keys.getPublic();
	
	// Display keys
	FacilityManager.getMsgLogger().
	    println("Modulus length: "+modLen+" bits",0,2);
	FacilityManager.getMsgLogger().
	    println("Private exponent: "+privKey.getExponent(),0,2);
	FacilityManager.getMsgLogger().
	    println("Public exponent: "+pubKey.getExponent(),0,2);
	FacilityManager.getMsgLogger().
	    println("Modulus: "+pubKey.getModulus(),0,2);
    }
}
