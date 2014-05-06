package gui.rsa;

import java.awt.event.*;
import javax.swing.*;
import java.math.*;
import java.util.*;
import java.awt.*;

import cryptix.provider.rsa.*;

import gui.*;

public class RSASupport {

    /** Name of the last selected public key */
    private String curPubKey = "";
    /** Name of the last selected private key */
    private String curPrivKey = "";

    /** Reference to RSAKeyManager instance */
    private RSAKeyManager rsaKeyMng = new RSAKeyManager();

    /** Reference to the RSAKeyProducer instance */
    private RSAKeyProducer rsaProducer;
    /** JDialog for key generation*/
    private JDialog gKDialog;
    /** Text field to display public exponent */
    private JTextField pubExpTF;
    /** Text field to display private exponent */
    private JTextField privExpTF;
    /** Text field to display modulus */
    private JTextField pubModTF;
    /** Text field to get requested modulus length (in bits) */
    private JTextField tfModLen;
    /** Button to launch generation process */
    private JButton genBut;
    /** Text field to set keys pair name */
    private JTextField keyNameTF;
    /** Button to save generated keys */
    private JButton keySaveBut;

    /** JDialog for recording private/public RSA keys */
    private JDialog sKDialog;
    /** Radio button for recording a public key */
    private JRadioButton pubBut;
    /** Text field to input key exponent */
    private JTextField expTF;
    /** Text field to input key modulus */
    private JTextField modTF;

    /** JDialog for managing RSA keys pairs */
    private JDialog mngKeyDialog;
    /** JList for private keys */
    private JList listPriv;
    /** JList for public keys */
    private JList listPub;
    /** Display both public and private keys */
    public static final int DISPLAY_BOTH = 1;
    /** Display only public keys */
    public static final int DISPLAY_PUBLIC = 2;
    /** Display only private keys */
    public static final int DISPLAY_PRIVATE = 3;
    
    /** Returns the name of the currently selected private key */
    public String getPrivKeyName() {
	return curPrivKey;
    }

    /** Returns the name of the currently selected public key */
    public String getPubKeyName() {
	return curPubKey;
    }

    /** Returns the RawRSAPrivateKey instance corresponding to the
     * currently selected private key */
    public RawRSAPrivateKey getRawRSAPrivKey() {
	if(curPrivKey==null || curPrivKey.equals("")) {
	    return null;
	} else {
	    Object[] obj = 
		(Object[])rsaKeyMng.getPrivateKeys().get(curPrivKey);
	    if(obj==null) return null;
	    return new RawRSAPrivateKey((BigInteger)obj[1],
					(BigInteger)obj[0]);
	}
    }

    /** Returns the RawRSAPublicKey instance corresponding to the
     * currently selected public key */
    public RawRSAPublicKey getRawRSAPubKey() {
	if(curPubKey==null || curPubKey.equals("")) {
	    return null;
	} else {
	    Object[] obj = (Object[])rsaKeyMng.getPublicKeys().get(curPubKey);
	    if(obj==null) return null;
	    return new RawRSAPublicKey((BigInteger)obj[1],(BigInteger)obj[0]);
	}
    }

    /** Save keys to files when closing the application */
    public void terminate() {
	rsaKeyMng.saveToFile();
    }

    /** Display the exponent and the modulus of the selected key */
    private void displayKey(Object[] obj, String name) {
	JPanel pan = new JPanel(new GridLayout(2,1));

	// Exponent
	JPanel pan1 = new JPanel();
	pan1.add(new JLabel("Exp: "));
	JTextField tf1 = new JTextField(((BigInteger)obj[0]).toString(),30);
	tf1.setEditable(false);
	pan1.add(tf1);
	pan.add(pan1);

	// Modulus
	JPanel pan2 = new JPanel();
	pan2.add(new JLabel("Mod: "));
	JTextField tf2 = new JTextField(((BigInteger)obj[1]).toString(),30);
	tf2.setEditable(false);
	pan2.add(tf2);
	pan.add(pan2);

	JOptionPane.showMessageDialog(null,pan,"RSA key value: "+name,
				      JOptionPane.INFORMATION_MESSAGE);
    }

    /** Create the user interface to manage RSA keys pair. It display
     * either public keys or private keys or both depending on the
     * displayType variable. */
    public void createMngKeyUI(Main mainFrame, int displayType) {
	mngKeyDialog = new JDialog(mainFrame,"Select/Remove "+
				   "public and private RSA keys",true);
	Container content = mngKeyDialog.getContentPane();
	content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));

	// List public and private keys
	JPanel listPan;
	if(displayType!=DISPLAY_BOTH) {
	    listPan = new JPanel();
	} else {
	    listPan = new JPanel(new GridLayout(1,2));
	}
		    
	// Private keys
	if(displayType!=DISPLAY_PUBLIC) {
	    JPanel privKeyPan = new JPanel();
	    privKeyPan.setLayout(new BoxLayout(privKeyPan,BoxLayout.Y_AXIS));
	    privKeyPan.setBorder(BorderFactory.
				 createTitledBorder("Private keys"));
	    Hashtable privKeyTb = rsaKeyMng.getPrivateKeys();
// 	Object[] obj = new Object[privKeyTb.size()+1];
	    Object[] obj = new Object[privKeyTb.size()];
	    int i=0;
// 	obj[0] = "No selection";
	    for(Enumeration en=privKeyTb.keys(); 
		en.hasMoreElements(); i++) {
// 	    obj[i+1] = (String)en.nextElement();
		obj[i] = (String)en.nextElement();
	    }
	    listPriv = new JList(obj);
	    listPriv.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    listPriv.setSelectedValue(curPrivKey,true);
	    JScrollPane scrollPane1 = new JScrollPane();
	    scrollPane1.getViewport().setView(listPriv);
	    privKeyPan.add(scrollPane1);
	    JButton viewPriv = new JButton("View");
	    viewPriv.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			String keyName = (String)listPriv.getSelectedValue();
			if(keyName.equals("") || 
			   keyName.equals("No selection")) {
			    return;
			} else {
			    displayKey((Object[])rsaKeyMng.getPrivateKeys().
				       get(keyName), keyName+" (private)");
			}
		    }
		});
	    privKeyPan.add(viewPriv);
	    listPan.add(privKeyPan);
	}

	// Public keys
	if(displayType!=DISPLAY_PRIVATE) {
	    JPanel pubKeyPan = new JPanel();
	    pubKeyPan.setLayout(new BoxLayout(pubKeyPan,BoxLayout.Y_AXIS));
	    pubKeyPan.setBorder(BorderFactory.
				createTitledBorder("Public keys"));
	    Hashtable pubKeyTb = rsaKeyMng.getPublicKeys();
// 	obj = new Object[pubKeyTb.size()+1];
	    Object[] obj = new Object[pubKeyTb.size()];
	    int i=0;
// 	obj[0] = "No selection";
	    for(Enumeration en=pubKeyTb.keys(); 
		en.hasMoreElements(); i++) {
// 	    obj[i+1] = (String)en.nextElement();
		obj[i] = (String)en.nextElement();
	    }
	    listPub = new JList(obj);
	    listPub.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    listPub.setSelectedValue(curPubKey,true);
	    JScrollPane scrollPane2 = new JScrollPane();
	    scrollPane2.getViewport().setView(listPub);
	    pubKeyPan.add(scrollPane2);
	    JButton viewPub = new JButton("View");
	    viewPub.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			String keyName = (String)listPub.getSelectedValue();
			if(keyName.equals("") || 
			   keyName.equals("No selection")) {
			    return;
			} else {
			    displayKey((Object[])rsaKeyMng.getPublicKeys().
				       get(keyName),keyName+" (public)");
			}
		    }
		});
	    pubKeyPan.add(viewPub);
	    listPan.add(pubKeyPan);
	}
	content.add(listPan);

	// Command on selected keys
	JPanel commandPan = new JPanel();
	JButton delBut = new JButton("Delete");
	commandPan.add(delBut);
	JButton selectBut = new JButton("Select");
	commandPan.add(selectBut);
	if(displayType==DISPLAY_BOTH) {
	    delBut.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			rsaKeyMng.removePubKey((String)listPub.
					       getSelectedValue());
			rsaKeyMng.removePrivKey((String)listPriv.
						getSelectedValue());
			mngKeyDialog.setVisible(false);
			mngKeyDialog = null;
		    }
		});
	    selectBut.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			curPubKey = (String)listPub.getSelectedValue();
			curPrivKey = (String)listPriv.getSelectedValue();
			mngKeyDialog.setVisible(false);
			mngKeyDialog = null;
		    }
		});
	} else if(displayType==DISPLAY_PRIVATE) {
	    delBut.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			rsaKeyMng.removePrivKey((String)listPriv.
						getSelectedValue());
			mngKeyDialog.setVisible(false);
			mngKeyDialog = null;
		    }
		});
	    selectBut.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			curPrivKey = (String)listPriv.getSelectedValue();
			mngKeyDialog.setVisible(false);
			mngKeyDialog = null;
		    }
		});
	} else {
	    delBut.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			rsaKeyMng.removePubKey((String)listPub.
					       getSelectedValue());
			mngKeyDialog.setVisible(false);
			mngKeyDialog = null;
		    }
		});
	    selectBut.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			curPubKey = (String)listPub.getSelectedValue();
			mngKeyDialog.setVisible(false);
			mngKeyDialog = null;
		    }
		});
	}
	content.add(commandPan);

	mngKeyDialog.pack();
	mngKeyDialog.setVisible(true);
    }

    /** Create the user interface to save RSA keys */
    public void createSaveKeyUI(Main mainFrame) {
	sKDialog = new JDialog(mainFrame,"Save public/private key",true);
	Container content = sKDialog.getContentPane();
	content.setLayout(new GridLayout(4,1));

	// ---- Choose between public and private ----
	JPanel chPan = new JPanel();
	ButtonGroup pubPrivGrp = new ButtonGroup();
	pubBut = new JRadioButton("Public",true);
	pubPrivGrp.add(pubBut);
	chPan.add(pubBut);
	JRadioButton privBut = new JRadioButton("Private",false);
	pubPrivGrp.add(privBut);
	chPan.add(privBut);
	content.add(chPan);

	// ---- Exponent ----
	JPanel expPan = new JPanel();
	expPan.setBorder(BorderFactory.createTitledBorder("Exponent"));
	expTF = new JTextField("",30);
	expPan.add(expTF);
	content.add(expPan);

	// ---- Modulus ----
	JPanel modPan = new JPanel();
	modPan.setBorder(BorderFactory.createTitledBorder("Modulus"));
	modTF = new JTextField("",30);
	modPan.add(modTF);
	content.add(modPan);

	// ---- Save ----
	JPanel savePan = new JPanel();
	savePan.add(new JLabel("Name:"));
	keyNameTF = new JTextField("Key name",10);
	savePan.add(keyNameTF);
	JButton okButton = new JButton("Save");
	okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if(pubBut.isSelected() && keyNameTF.getText()!="" &&
		       expTF.getText()!="" && modTF.getText()!="") {
			if(!rsaKeyMng.
			   savePublicKey(keyNameTF.getText(),
					 new BigInteger(expTF.getText()),
					 new BigInteger(modTF.getText()))) {
			    JOptionPane.
				showMessageDialog(null,
						  "Public Key is already "+
						  "existing. "+
						  "Remove the old one before "+
						  "or use an other name.",
						  "Error",
						  JOptionPane.ERROR_MESSAGE);
			    return;
			}
		    } else if(keyNameTF.getText()!="" && expTF.getText()!="" &&
			      modTF.getText()!="") {
			if(!rsaKeyMng.
			   savePrivateKey(keyNameTF.getText(),
					  new BigInteger(expTF.getText()),
					  new BigInteger(modTF.getText()))) {
			    JOptionPane.
				showMessageDialog(null,
						  "Private Key is already "+
						  "existing. "+
						  "Remove the old one before "+
						  "or use an other name.",
						  "Error",
						  JOptionPane.ERROR_MESSAGE);
			    return;
			}
		    }
		}
	    });
	savePan.add(okButton);
	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    sKDialog.setVisible(false);
		    sKDialog = null;
		}
	    });
	savePan.add(cancelButton);
	content.add(savePan);

	sKDialog.pack();
	sKDialog.setVisible(true);
    }

    /** Create the user interface to generate and save RSA keys pairs */
    public void createKeyGenUI(Main mainFrame) {
	// Create JDialog and its layout
	gKDialog = new JDialog(mainFrame,"Generate RSA keys pair",true);
	Container content = gKDialog.getContentPane();
	content.setLayout(new GridLayout(5,1));

	// ---- Public key ----
	JPanel pubPan = new JPanel();
	pubPan.setBorder(BorderFactory.createTitledBorder("Public exponent"));
	pubExpTF = new JTextField("",30);
	pubExpTF.setEditable(false);
	pubPan.add(pubExpTF);
	content.add(pubPan);

	// ---- Private key ----
	JPanel privPan = new JPanel();
	privPan.setBorder(BorderFactory.
			  createTitledBorder("Private exponent"));
	privExpTF = new JTextField("",30);
	privExpTF.setEditable(false);
	privPan.add(privExpTF);
	content.add(privPan);

	// ---- Modulus ----
	JPanel modPan = new JPanel();
	modPan.setBorder(BorderFactory.createTitledBorder("Modulus"));
	pubModTF = new JTextField("",30);
	pubModTF.setEditable(false);
	modPan.add(pubModTF);
	content.add(modPan);

	// ---- Generate pair ----
	JPanel genPan = new JPanel(new GridLayout(1,2));
		    
	// Modulus length
	JPanel modLenPan = new JPanel();
	modLenPan.add(new JLabel("Modulus length (bits):"));
	tfModLen = new JTextField("128",4);
	modLenPan.add(tfModLen);
	genPan.add(modLenPan);

	// Generate button
	JPanel genButPan = new JPanel();
	genBut = new JButton("Generate Pair");
	genBut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int modLen = (new Integer(tfModLen.getText())).intValue();
		    rsaProducer = new RSAKeyProducer(genBut,keySaveBut,
						     privExpTF,pubExpTF,
						     pubModTF,modLen);
		    Thread thRsa = new Thread(rsaProducer);
		    thRsa.start();
		}
	    });
	genButPan.add(genBut);
	genPan.add(genButPan);
	content.add(genPan);
		    
	// ---- Save ----
	JPanel savePan = new JPanel(new GridLayout(1,2));
	// Keys pair Name
	JPanel keyNamePan = new JPanel();
	keyNamePan.add(new JLabel("Name:"));
	keyNameTF = new JTextField("Key",10);
	keyNamePan.add(keyNameTF);
	savePan.add(keyNamePan);

	// Save keys pair
	JPanel keySavePan = new JPanel();
	keySaveBut = new JButton("Save");
	keySaveBut.setEnabled(false);
	keySaveBut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if(keyNameTF.getText()!="" && pubExpTF.getText()!="" &&
		       pubModTF.getText()!="") {
			if(!rsaKeyMng.
			   savePublicKey(keyNameTF.getText(),
					 new BigInteger(pubExpTF.getText()),
					 new BigInteger(pubModTF.getText()))) {
			    JOptionPane.
				showMessageDialog(null,
						  "Public key is already "+
						  "existing. "+
						  "Remove the old one before "+
						  "or use an other name.",
						  "Error",
						  JOptionPane.ERROR_MESSAGE);
			    return;
			}
		    }
		    if(keyNameTF.getText()!="" && privExpTF.getText()!="" &&
		       pubModTF.getText()!="") {
			if(!rsaKeyMng.
			   savePrivateKey(keyNameTF.getText(),
					  new BigInteger(privExpTF.getText()),
					  new BigInteger(pubModTF.getText()))){
			    JOptionPane.
				showMessageDialog(null,
						  "Private key is already "+
						  "existing. "+
						  "Remove the old one before "+
						  "or use an other name.",
						  "Error",
						  JOptionPane.ERROR_MESSAGE);
			    return;
			}
		    }
		    gKDialog.setVisible(false);
		    gKDialog = null;
		}
	    });
	keySavePan.add(keySaveBut);
	savePan.add(keySavePan);
	content.add(savePan);
		    
	gKDialog.pack();
	gKDialog.setVisible(true);
    }
}
