package gui;

import jj2000.j2k.image.input.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.disp.*;
import jj2000.j2k.*;

import security.*;
import gui.rsa.*;

import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.util.*;
import java.awt.*;
import java.io.*;

public class Main extends JFrame 
    implements InternalFrameListener, ProgressWatch {
    /** JDesktopPane of the main frame*/
    protected JDesktopPane desktop;

    /** Properties instance keeping configuration */
    protected Properties configProp = new Properties();

    /** Original offset between successive internal frames */
    public final static int FRAME_INCR = 25;

    /** Action to open files */
    private Action openFile;
    /** Action to save files */
    private Action saveFile;
    /** Action to show image properties */
    private Action infoFile;

    /** Generate/save a public/private keys pair */
    private JMenuItem genKey, saveKey;

    /** Number of opened frame (even if one has been closed meanwhile) */
    private int numOpenedFrame = 0;

    /** Zoom in the image */
    private JMenuItem zoomIn;
    /** Zoom out of the image */
    private JMenuItem zoomOut;
    /** Display the image at its full resolution */
    private JMenuItem zoomOrig;

    /** Reference to the RSASupport instance */
    private RSASupport rsaSupport = new RSASupport();

    /** Window menu */
    protected JMenu menuWindow;
    /** Button group for the window menu */
    protected ButtonGroup windowGroup = new ButtonGroup();
    /** Hashtable for the window menu (The key is the frame and the
     * value is the JCheckBoxMenuItem in the window Menu) */
    protected Hashtable windowHt = new Hashtable();

    /** Whether or not to display rules for the selected frame */
    private JCheckBoxMenuItem rulesItem;

    /** To keep reference to the opened images and extra information
     * stored in a FrameAttribute instance */
    protected Hashtable intFrameHt = new Hashtable();

    /** Label at the bottom of the frame to display some image's
     * information */
    protected JLabel statusLbl = null;

    /** Progress bar at the bottom of the frame to display some task's
     * advancement. */
    protected JProgressBar progressBar = new JProgressBar();

    /** Five last opened files */
    private File[] lastOpenedFiles = new File[5];
    /** Menu items for the last opened files */
    private JMenuItem[] lofItem = new JMenuItem[5];
    /** Menu for last opened files */
    private JMenu mOpenedFile = new JMenu("Recent images");

    /** Message to be displayed in the About box. */
    private String aboutMessage = "GUI v1.5b - EPFL\n"+
	"Wrapped around JJ2000 "+JJ2KInfo.version+"\n\nAuthors:\n Raphael "+
	"Grosbois, Mohamed Tahar Ktari\n"+
        "\nThis interface was "+
	"developed in\nthe framework of the 2KAN project.";

//     private ImageIcon iconOpen = new ImageIcon("gui/icons/open.gif");
//     private ImageIcon iconSave = new ImageIcon("gui/icons/save.gif");
//     private ImageIcon iconInfo = new ImageIcon("gui/icons/info.gif");
    private ImageIcon iconOpen = 
        new ImageIcon(this.getClass().getResource("icons/open.gif"));
    private ImageIcon iconSave = 
        new ImageIcon(this.getClass().getResource("icons/save.gif"));
    private ImageIcon iconInfo = 
            new ImageIcon(this.getClass().getResource("icons/info.gif"));

    /** Class constructor. Creates the menu bar, the status bar and
     * the tool bar.  */
    public Main() {
        super("JPEG 2000 Graphic User Interface");

	// Read save settings
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
	File configFile = new File(jj2kDir.getPath()+File.separator+"config");
	if(configFile.exists()) {
	    try {
		FileInputStream fis = new FileInputStream(configFile);
		configProp.load(fis);
	    } catch(IOException e) {}
	    
	    // Read last opened files
	    if(configProp.get("lastOpenedFiles")!=null) {
		String str = (String)configProp.get("lastOpenedFiles");
		StringTokenizer stk = new StringTokenizer(str);
		File file;
		while(stk.hasMoreElements()) {
		    file = new File((String)stk.nextElement());
		    saveOpenedFile(file);
		}
	    }
	}

        // Dimension
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	setSize(dim.width*3/4,dim.height*3/4);

        // Define a JDesktopPane
	desktop = new JDesktopPane();
 	desktop.setBackground(Color.white);
        desktop.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        // Status and progress bar
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	JPanel bottomPanel = new JPanel(gridbag);
	c.fill = GridBagConstraints.BOTH;

        statusLbl = new JLabel("");
        statusLbl.
            setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	statusLbl.setFont(new Font("SansSerif",Font.PLAIN,12));
	c.weightx = 5.0;
	gridbag.setConstraints(statusLbl,c);
	bottomPanel.add(statusLbl);

	progressBar.setIndeterminate(true);
	progressBar.setOrientation(JProgressBar.HORIZONTAL);
        progressBar.
            setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	progressBar.setFont(new Font("SansSerif",Font.PLAIN,10));
	c.weightx = 1.0;
	gridbag.setConstraints(progressBar,c);
	bottomPanel.add(progressBar);

        Container content = getContentPane();
        content.add(desktop,BorderLayout.CENTER);
        content.add(bottomPanel,BorderLayout.SOUTH);
       
        // Actions
        createActions();

        // MenuBar
        setJMenuBar(createMenuBar());

        // Toolbar
        JToolBar toolBar = createToolBar();
	getContentPane().add(toolBar,BorderLayout.NORTH);

        // Look and feel
        try {
            UIManager.
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {}

        // Main frame's listeners
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) { 
		    terminateApplication(); 
		}
            });
    }

    /** Add the current file to the list of last opened files */
    protected void saveOpenedFile(File file) {
	// Check if this file path has already been saved
	for(int i=0; i<lastOpenedFiles.length; i++) {
	    if(lastOpenedFiles[i]!=null && 
	       file.getPath().equalsIgnoreCase(lastOpenedFiles[i].
					       getPath())) {
		return;
	    }
	}

	// Put this file at the beginning of the list
	for(int i=lastOpenedFiles.length-1; i>0; i--) {
	    lastOpenedFiles[i] = lastOpenedFiles[i-1];
	}
	lastOpenedFiles[0] = file;

	// Create a new menu item for this file
	lofItem[0] = new JMenuItem(lastOpenedFiles[0].getPath());
	lofItem[0].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    OpenImage oi = new OpenImage(Main.this,desktop,
						 e.getActionCommand());
		    if(oi.isFileSelected()) {
			oi.run();
		    }
		}
	    });

	// Remove actual menu items
	for(int i=0; i<lastOpenedFiles.length; i++) {
	    if(lofItem[i]!=null) {
		mOpenedFile.remove(lofItem[i]);
	    }
	}

	// Add new ones
	for(int i=0; i<lastOpenedFiles.length; i++) {
	    if(lofItem[i]!=null) {
		mOpenedFile.add(lofItem[i]);
	    }
	}
    }

    /** 
     * Initialize the progress watching process 
     * */
    public void initProgressWatch(int min, int max,String info) {
	progressBar.setMinimum(min);
	progressBar.setMaximum(max);
	progressBar.setIndeterminate(false);
	progressBar.setValue(min);
	progressBar.setStringPainted(true);
	statusLbl.setText(info);
    }

    /** 
     * Returns the reference to the RSA support module
     * */
    public RSASupport getRSASupport() {
	return rsaSupport;
    }

    /** 
     * Terminate the application 
     * */
    private void terminateApplication() {
	// Remove temporary files if needed
	JInternalFrame[] frame = desktop.getAllFrames();
	if(frame!=null) {
	    for(int i=0; i<frame.length; i++) {
		FrameAttribute fa = 
		    (FrameAttribute)intFrameHt.get(frame[i]);
		if(fa.isTemporaryFile) {
		    String name = fa.srcFile.getPath();
		    String ext = name.
			substring(name.lastIndexOf('.'),
				  name.length());
		    String prefix = name.
			substring(0,name.lastIndexOf('.'));
		    if((ext.equalsIgnoreCase(".pgm") && fa.nc!=1) 
		       ||
		       (ext.equalsIgnoreCase(".pgx") && fa.nc!=1)){
			for(int c=1; c<=fa.nc; c++) {
			    (new File(prefix+"-"+c+ext)).delete();
			}
		    } else {
			fa.srcFile.delete();
		    }
		}
	    }
	}

	// Terminate RSA module
	rsaSupport.terminate();

	// Save last opened files
	String str = "";
	for(int i=0; i<lastOpenedFiles.length; i++) {
	    if(!(lastOpenedFiles[i]==null)) {
		str += lastOpenedFiles[i]+" ";
	    }
	}
	configProp.put("lastOpenedFiles", str);

	// Save configuration
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
	File configFile = new File(jj2kDir.getPath()+File.separator+"config");
	try {
	    FileOutputStream fos = new FileOutputStream(configFile);
	    configProp.store(fos, "JJ2000 GUI settings");
	} catch(IOException e) { }

	// Exit the application
	System.exit(0); 
    }

    /** 
     * Update the progress watching process to the specified value
     * */
    public void updateProgressWatch(int val,String info) {
	if(val>progressBar.getValue()) {
	    int start = progressBar.getValue();
	    for(int i=start; i<=val; i++) {
		progressBar.setValue(i);
	    }
	} else {
	    progressBar.setValue(val);
	}
	if(info!=null) {
	    statusLbl.setText(info);
	}
    }

    /** 
     * Terminate the progress watch process
     * */
    public void terminateProgressWatch() {
	progressBar.setValue(progressBar.getMinimum());
	progressBar.setStringPainted(false);
	statusLbl.setText("");
    }

    /** 
     * Create Action instances for opening and saving files and for
     * displaying internal frame information. These Actions are then
     * used in the menu bar and in the tool bar. 
     * */
    private void createActions() {
	// Opening files
        openFile = new AbstractAction("Open...",iconOpen) {
                public void actionPerformed(ActionEvent e) {
                    OpenImage oi = new OpenImage(Main.this,desktop);
                    if(oi.isFileSelected()) {
			oi.run();
                    }
                }
            };
	// Display file information (disabled at startup as no image is
	// displayed)
        infoFile = new AbstractAction("Properties",iconInfo) {
                public void actionPerformed(ActionEvent e) {
                    JInternalFrame f = desktop.getSelectedFrame();
                    FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
                    if(fA==null) return;

                    String info = "Input: "+fA.title+"\n";
                    info += "Number of components: "+fA.nc+"\n";
                    info += "Dimension: "+fA.imgCo.width+"x"+
			fA.imgCo.height+"\n";
		    info += "Offset: "+fA.imgCo.x+","+fA.imgCo.y+"\n";
                    info += "Zoom Factor: "+fA.imgPan.getZoomFactor()+"\n";
                    if(fA.info!=null) {
                        info += fA.info;
                    }
                    JOptionPane.showMessageDialog(Main.this,info,
                                                  "Image information",
                                                  JOptionPane.
                                                  INFORMATION_MESSAGE);
                }
            };
        infoFile.setEnabled(false);
	// Saving files (disabled at startup as no image can be saved)
        saveFile = new AbstractAction("Save As...",iconSave) {
                public void actionPerformed(ActionEvent e) {
                    JInternalFrame f = desktop.getSelectedFrame();
                    FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
                    if(fA==null) return;

		    Dimension inDim = 
			new Dimension(fA.imgCo.width,fA.imgCo.height);
                    SaveImage si = new SaveImage(Main.this,desktop,fA.srcFile,
                                                 inDim,fA.imgPan);
                    if(si.isFileSelected()) {
                        if(si.getFileType()==SaveImage.FILE_TYPE_UNKNOWN) {
                            JOptionPane.showMessageDialog(null,
                                                          "Unknown "+
                                                          "output file type",
                                                          "Error",
                                                          JOptionPane.
                                                          ERROR_MESSAGE);
                        }
			fA.imgPan.setOffset(0,0);
                        new Thread(si).start();
                    }
                }
            };
        saveFile.setEnabled(false);
     }

    /** Reset everything when all internal frames have been closed. */
    public void internalFrameClosed(InternalFrameEvent e) {
        int nf = desktop.getAllFrames().length;
        if(nf==0) {
            saveFile.setEnabled(false);
            infoFile.setEnabled(false);
            statusLbl.setText("");
            enableZoom(false);
            return;
        }
    }
    /** Remove frame from Hashtable when closing it */
    public void internalFrameClosing(InternalFrameEvent e) {
        JInternalFrame f = e.getInternalFrame();
	FrameAttribute fa = (FrameAttribute)intFrameHt.get(f);
	if(fa.isTemporaryFile) {
	    String name = fa.srcFile.getPath();
	    String ext = name.substring(name.lastIndexOf('.'),name.length());
	    String prefix = name.substring(0,name.lastIndexOf('.'));
	    if((ext.equalsIgnoreCase(".pgm") && fa.nc!=1) ||
	       (ext.equalsIgnoreCase(".pgx") && fa.nc!=1)) {
		for(int c=1; c<=fa.nc; c++) {
		    (new File(prefix+"-"+c+ext)).delete();
		}
	    } else {
		fa.srcFile.delete();
	    }
	}
        intFrameHt.remove(f);
	menuWindow.remove((JCheckBoxMenuItem)windowHt.get(f));
	windowHt.remove(f);
	rulesItem.setEnabled(false);
    }
    public void internalFrameDeactivated(InternalFrameEvent e) {}
    public void internalFrameDeiconified(InternalFrameEvent e) {}
    public void internalFrameIconified(InternalFrameEvent e) {}
    public void internalFrameOpened(InternalFrameEvent e) {}
    /** An internal frame is activated when selected by the mouse or
     * the menu or when an other one has been closed. */
    public void internalFrameActivated(InternalFrameEvent e) {
        JInternalFrame f = desktop.getSelectedFrame();
        FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
        if(fA==null) return;

	// Enable image saving operation.
	saveFile.setEnabled(true);
	// Enable display of information
	infoFile.setEnabled(true);

	// Update rules check box
	rulesItem.setEnabled(true);
	if(fA.scrollPane!=null) {
	    rulesItem.setEnabled(true);
	    if(fA.scrollPane.getRowHeader()==null) {
		rulesItem.setSelected(false);
		enableZoom(true);
	    } else {
		rulesItem.setSelected(true);
		enableZoom(false);
	    }
	} else {
	    rulesItem.setSelected(false);
	    rulesItem.setEnabled(false);
	}
	
	// Update window Menu
	JCheckBoxMenuItem fcb = (JCheckBoxMenuItem)windowHt.get(f);
	fcb.setSelected(true);
	if(f.isIcon()) {
	    try {
		f.setMaximum(true);
	    } catch (java.beans.PropertyVetoException pve) {}
	}

	// Display number of components and image dimension in the status bar
        if(fA.nc==1) {
            statusLbl.setText("1 component, "+fA.imgCo.width+
			      "x"+fA.imgCo.height);
        } else if(fA.nc>1) {
            statusLbl.setText(fA.nc+" components, "+fA.imgCo.width+
			      "x"+fA.imgCo.height);
        }
    }

    /** Create the menuBar of the application */
    private JMenuBar createMenuBar() {
        ///// File /////
        JMenu file = new JMenu("File");
        file.add(openFile);
        file.add(saveFile);
        file.add(infoFile);
        file.addSeparator();
	//// Encryption keys ////
	JMenu encryptKey = new JMenu("Encryption keys");
	genKey = new JMenuItem("Generate RSA keys pair...");
	encryptKey.add(genKey);
	saveKey = new JMenuItem("Save RSA key...");
	encryptKey.add(saveKey);
	JMenuItem mngKey = new JMenuItem("Manage RSA key...");
	encryptKey.add(mngKey);
	file.add(encryptKey);
	//// Last Opened files ////
	file.add(mOpenedFile);
	for(int i=0; i<lastOpenedFiles.length; i++) {
	    if(lastOpenedFiles[i]==null) {
		continue;
	    }
	    lofItem[i] = new JMenuItem(lastOpenedFiles[i].getPath());
	    lofItem[i].addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			OpenImage oi = new OpenImage(Main.this,desktop,
						     e.getActionCommand());
			if(oi.isFileSelected()) {
			    oi.run();
			}
		    }
		});
	    mOpenedFile.add(lofItem[i]);
	}
	//// Exit ////
	file.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        file.add(exit);

        ///// Image /////
        JMenu imageMenu = new JMenu("Image");
        ///// Zoom /////
        JMenu zoomMenu = new JMenu("Zoom");
        zoomIn = new JMenuItem("Zoom in");
        zoomMenu.add(zoomIn);
        zoomOut = new JMenuItem("Zoom out");
        zoomMenu.add(zoomOut);
        zoomOrig = new JMenuItem("Full resolution");
        zoomMenu.add(zoomOrig);
        enableZoom(false);
        imageMenu.add(zoomMenu);

	///// Tools /////
	JMenu toolMenu = new JMenu("Tools");
        rulesItem = new JCheckBoxMenuItem("Ruler",false);
	rulesItem.setEnabled(false);
        toolMenu.add(rulesItem);

	///// Window /////
	menuWindow = new JMenu("Window");
	JMenuItem reorgWin = new JMenuItem("Reorganize windows");
	menuWindow.add(reorgWin);
	menuWindow.addSeparator();

        ///// Help /////
        JMenu menuHelp = new JMenu("Help");
        JMenuItem aboutGuiMenu = new JMenuItem("About GUI");
        menuHelp.add(aboutGuiMenu);
	JMenuItem aboutCryptix = new JMenuItem("About Cryptix");
	menuHelp.add(aboutCryptix);

        // Accelerators
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,0));
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,0));
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,0));
        zoomOrig.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,0));

        // Listener's
	mngKey.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rsaSupport.createMngKeyUI(Main.this,RSASupport.
					      DISPLAY_BOTH);
		}
	    });
	saveKey.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rsaSupport.createSaveKeyUI(Main.this);
		}
	    });
	genKey.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rsaSupport.createKeyGenUI(Main.this);
		}
	    });
        exit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { 
		    terminateApplication();
		}
            });
	reorgWin.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    JInternalFrame[] frames = desktop.getAllFrames();
		    numOpenedFrame = 0;
                    for(int i=0; i<frames.length; i++) {
			frames[i].setLocation(FRAME_INCR*numOpenedFrame,
					      FRAME_INCR*numOpenedFrame);
			numOpenedFrame++;
			try {
			    frames[i].setSelected(true);
			} catch(java.beans.PropertyVetoException pve) {}
		    }		    
		}
	    });
        zoomOrig.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    zoomOriginal();
                }
            });
        zoomIn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    zoomInImage();
                }
            });
        zoomOut.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    zoomOutImage();
                }
            });
        aboutGuiMenu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
//                     JOptionPane.
//                         showMessageDialog(null,aboutMessage,"About GUI",
//                                           JOptionPane.INFORMATION_MESSAGE,
//                                           new ImageIcon("gui/icons/"+
// 							"2kancolor.jpg"));
                    JOptionPane.
                        showMessageDialog(null,aboutMessage,"About",
                                          JOptionPane.INFORMATION_MESSAGE,
                                          new ImageIcon(this.getClass().
							getResource("icons/"+
							"2kancolor.jpg")));
                }
            });
// 	aboutCryptix.addActionListener(new ActionListener() {
// 		public void actionPerformed(ActionEvent e) {
// 		    JOptionPane.
// 			showMessageDialog(null,"Cryptix 3.2.0\n"+
// 					  "Copyright(c) 1995, 1996, 1997,\n"+
// 					  "1998, 1999, 2000 The Cryptix "+
// 					  "Foundation Limited.\nAll rights "+
// 					  "reserved.", "About Cryptix",
// 					  JOptionPane.INFORMATION_MESSAGE,
// 					  new ImageIcon("gui/icons/"+
// 							"cryptix.gif"));
// 		}
// 	    });
	aboutCryptix.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JOptionPane.
			showMessageDialog(null,"Cryptix 3.2.0\n"+
					  "Copyright(c) 1995, 1996, 1997,\n"+
					  "1998, 1999, 2000 The Cryptix "+
					  "Foundation Limited.\nAll rights "+
					  "reserved.", "About Cryptix",
					  JOptionPane.INFORMATION_MESSAGE,
					  new ImageIcon(this.getClass().
							getResource("icons/"+
							"cryptix.gif")));
		}
	    });
	rulesItem.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
                    JInternalFrame f = desktop.getSelectedFrame();
		    FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
		    if(fA==null) return;
		    if(fA.scrollPane==null) return;
		    
 		    if(rulesItem.isSelected()) {
			Rule columnView = new Rule(Rule.HORIZONTAL,fA.imgPan,
						   fA.imgCo,fA.scrollPane);
			columnView.setPreferredWidth(20);
			Rule rowView = new Rule(Rule.VERTICAL,fA.imgPan,
						fA.imgCo,fA.scrollPane);
			rowView.setPreferredHeight(20);
			fA.scrollPane.setRowHeaderView(rowView);
			fA.scrollPane.setColumnHeaderView(columnView);
		    } else {
			fA.scrollPane.setRowHeaderView(null);
			fA.scrollPane.setColumnHeaderView(null);
		    }
		}
	    });
	JMenuBar menuBar = new JMenuBar();
        menuBar.add(file);
        menuBar.add(imageMenu);
        menuBar.add(toolMenu);
	menuBar.add(menuWindow);
        menuBar.add(menuHelp);
	return menuBar;
    }

    /** Zoom out of the current image and update the frame title */
    private void zoomOutImage() {
        JInternalFrame f = desktop.getSelectedFrame();
        FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
        if(fA==null) return;

        fA.imgPan.zoomOut();
        double zf = fA.imgPan.getZoomFactor();
        if(zf==1) {
            f.setTitle(fA.title+" (100%)");
        } else {
            f.setTitle(fA.title+" ("+(1/zf*100)+"%)");
        } 
    }

    /** Zoom in the current image and update the frame title */
    private void zoomInImage() {
        JInternalFrame f = desktop.getSelectedFrame();
        FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
        if(fA==null) return;

        fA.imgPan.zoomIn();
        double zf = fA.imgPan.getZoomFactor();
        if(zf==1) {
            f.setTitle(fA.title+" (100%)");
        } else {
            f.setTitle(fA.title+" ("+(1/zf*100)+"%)");
        } 
    }

    /** Display the current image at its full resolution and update
     * the title */
    private void zoomOriginal() {
        JInternalFrame f = desktop.getSelectedFrame();
        FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
        if(fA==null) return;

        fA.imgPan.zoomFull();
        f.setTitle(fA.title+" (100%)");
    }

    /** Create the toolBar of the application */
    private JToolBar createToolBar() {
	JToolBar toolBar = new JToolBar();
	toolBar.setBackground(Color.lightGray);
	
	// Open Button
        toolBar.add(openFile);
	toolBar.add(saveFile);
	toolBar.addSeparator();
        toolBar.add(infoFile);
        return toolBar;
    }

    /** 
     * Method called to add an internal frame (containing a decoded image) to
     * the main application 
     *
     * @param f JInternalFrame containing the displayed image.
     * @param fa FrameAttribute instance
     * */ 
   public void addInternalFrame(JInternalFrame f,FrameAttribute fA) {
        // Terminate frame initialization
        f.addInternalFrameListener(this);
	if(desktop.getSelectedFrame()!=null) {
	    Point pt = desktop.getSelectedFrame().getLocation();
	    f.setLocation(pt.x+FRAME_INCR,pt.y+FRAME_INCR);
	} else {
	    f.setLocation(FRAME_INCR*numOpenedFrame,FRAME_INCR*numOpenedFrame);
	}
	numOpenedFrame++;

	// Add a border around the Viewport 
	if(fA.scrollPane!=null) {
	    fA.scrollPane.
		setViewportBorder(BorderFactory.
				  createBevelBorder(BevelBorder.LOWERED));
	}
	f.pack();	

        // Add the frame in the frame hash table
        intFrameHt.put(f,fA);

	// Add zoom percentage to the title
	if(fA.imgPan!=null) {
	    double zf = fA.imgPan.getZoomFactor();
	    if(zf==1) {
		f.setTitle(fA.title+" (100%)");
	    } else {
		f.setTitle(fA.title+" ("+(1/zf*100)+"%)");
	    } 
	}
        f.setFrameIcon(null);
	f.setVisible(true);

	// Add frame in the window hash table and in the window menu
	JCheckBoxMenuItem fcb = new JCheckBoxMenuItem(fA.title,true);
	menuWindow.add(fcb);
	windowGroup.add(fcb);
	windowHt.put(f,fcb);
	fcb.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if(e.getStateChange()==ItemEvent.SELECTED) {
			JInternalFrame frame = null;
			Enumeration keys = windowHt.keys();
			while(keys.hasMoreElements()) {
			    JInternalFrame fTmp = 
				(JInternalFrame)keys.nextElement();

			    if(((JCheckBoxMenuItem)windowHt.get(fTmp))==
			       ((JCheckBoxMenuItem)e.getItem())) {
				frame = fTmp;
				try {
				    frame.setSelected(true);
				} catch(java.beans.PropertyVetoException pve)
				    {}
			    }
			}
		    }
		}
	    });

	// Finally add the frame to the desktop and give it the focus
        desktop.add(f);
        try {
            f.setSelected(true);
        } catch(java.beans.PropertyVetoException e) {}
    }

    /** 
     * Rename the internal frame and modify the value in the
     * Hashtable. This is normally called when the current image is
     * saved to a file. Then the old file is deleted if it was
     * temporary and the new file is no more considered as
     * temporary. */
    public void renameFrame(File newFile) {
        JInternalFrame f = desktop.getSelectedFrame();
        FrameAttribute fA = (FrameAttribute)intFrameHt.get(f);
	if(fA.isTemporaryFile) {
	    String name = fA.srcFile.getPath();
	    String ext = name.substring(name.lastIndexOf('.'),name.length());
	    String prefix = name.substring(0,name.lastIndexOf('.'));
	    if((ext.equalsIgnoreCase(".pgm") && fA.nc!=1) ||
	       (ext.equalsIgnoreCase(".pgx") && fA.nc!=1)) {
		for(int c=1; c<=fA.nc; c++) {
		    (new File(prefix+"-"+c+ext)).delete();
		}
	    } else {
		fA.srcFile.delete();
	    }
	}
        fA.srcFile = newFile;
	fA.isTemporaryFile = false;
        f.setTitle(newFile.getName());
    }

    /** Enable or disable all zooming operations */
    public void enableZoom(boolean status) {
        zoomIn.setEnabled(status);
        zoomOut.setEnabled(status);
        zoomOrig.setEnabled(status);
    }

    /** Main method */
    public static void main(String[] argv) {
        Main frame = new Main();
        frame.setVisible(true);
    }
}
