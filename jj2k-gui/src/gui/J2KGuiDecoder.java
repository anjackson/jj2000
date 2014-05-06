package gui;

import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.awt.*;
import java.io.*;

import cryptix.provider.rsa.*;

import gui.rsa.*;

import jj2000.j2k.decoder.*;
import jj2000.j2k.util.*;

public class J2KGuiDecoder implements ActionListener, ChangeListener {
    private Main mainFrame = null;
    private File inFile = null;
    private OpenImage oi;
    private JDesktopPane desktop;
    private int width, height;

    /** Simple decoding options components */
    private JDialog simpDecOptDialog;
    private DecimalFormat f = new DecimalFormat("##0.000");
    private static int decSliderPrec = 1000;
    private double maxRate;
    private JTextField decRateField;
    private JRadioButton decParsOn, decParsOff;
    public JButton decOptOkBut;
    private JButton decOptCancelBut, decOptAdvancedBut;
    private JSlider decRateSlider;
    
    /** Advanced decoding options components */
    private JDialog advDecOptDialog;
    public JButton decOkBut;
    private JButton decDefBut, decCancelBut;
    private JCheckBox showDecInf, decResChkBox, csIgnoreBox;
    private JRadioButton cerOn, cerOff, cverberOn;
    private JRadioButton cverberOff, noRoiOn, noRoiOff;
    private JRadioButton codeStrInfoOn, codeStrInfoOff;
    private JTextField decResTf;

    /** Security options */
    private JTextField selectedKeyTF;
    private RawRSAPublicKey rsaPubKey;
    private RSASupport rsaSupport;

    /** Class constructor */
    public J2KGuiDecoder(Main mainFrame,JDesktopPane desktop, OpenImage oi, 
                         File inFile, int width, int height) {
        this.mainFrame = mainFrame;
        this.desktop = desktop;
        this.inFile = inFile;
        this.oi = oi;
        this.width = width;
        this.height = height;
	rsaSupport = mainFrame.getRSASupport();
    }

    public void start() {
        createSimpleGui();
        simpDecOptDialog.setVisible(true);
    }

    public double getRate() {
        return (new Double(decRateField.getText())).doubleValue();
    }

    /** Create dialog box for simple decoding options */
    private void createSimpleGui() {
        // Open JDialog for simple decoding options
        simpDecOptDialog = new JDialog(mainFrame,"Decoding rate");
        JPanel simpDecOptPan = new JPanel(new GridLayout(3,1));
        
        // Rate option
        JPanel decRatePan = new JPanel();
        maxRate = inFile.length()*8d/width/height;
        decRateSlider = new JSlider(JSlider.HORIZONTAL,0,
                                     (int)(maxRate*decSliderPrec),
                                     (int)(maxRate*decSliderPrec));
        decRatePan.add(decRateSlider);
        decRateSlider.addChangeListener(this);
        decRateField = new JTextField(f.format(maxRate),5);
        decRateField.addActionListener(this);
        decRatePan.add(decRateField);	
        decRatePan.add(new JLabel("kB"));
        simpDecOptPan.add(decRatePan);
        
        // Truncation/parsing mode
        JPanel decTruncPan = new JPanel();
        ButtonGroup decParsGrp = new ButtonGroup();
        decParsOn = new JRadioButton("parsing",true);
        decParsGrp.add(decParsOn);
        decTruncPan.add(decParsOn);
        decParsOff = new JRadioButton("truncation",false);
        decParsGrp.add(decParsOff);
        decTruncPan.add(decParsOff);
        simpDecOptPan.add(decTruncPan);
        
        // Confirmation buttons
        JPanel decConfirmPan = new JPanel();
        decOptOkBut = new JButton("OK");
        decOptOkBut.addActionListener(oi);
        decConfirmPan.add(decOptOkBut);
        decOptCancelBut = new JButton("Cancel");
        decOptCancelBut.addActionListener(this);
        decConfirmPan.add(decOptCancelBut);
        decOptAdvancedBut = new JButton("Advanced...");
        decOptAdvancedBut.addActionListener(this);
        decConfirmPan.add(decOptAdvancedBut);
        simpDecOptPan.add(decConfirmPan);
        
        simpDecOptDialog.getContentPane().add(simpDecOptPan);
        simpDecOptDialog.setModal(true);
        simpDecOptDialog.pack();
        simpDecOptDialog.setLocationRelativeTo(mainFrame);
    }
    
    /** 
     * Gets simple parameters values, creates the ParameterList instance and
     * close all dialog windows 
     * */
    public ParameterList getSimpParameters() {
	// Initialize default parameters
        ParameterList defpl = new ParameterList();
	String[][] param = Decoder.getAllParameters();
	for (int i=param.length-1; i>=0; i--) {
	    if(param[i][3]!=null)
		defpl.put(param[i][0],param[i][3]);
        }

	// Create parameter list using defaults
        ParameterList pl = new ParameterList(defpl);
	
	// Put arguments into ParameterList
	double curRate = (new Double(decRateField.getText())).doubleValue();
	if(Math.abs(curRate-maxRate)>0.001) {
            pl.put("rate",""+decRateField.getText());
        }
        if(decParsOn.isSelected()) {
            pl.put("parsing","on");
        } else {
            pl.put("parsing","off");
        }
        pl.put("verbose","off");
        simpDecOptDialog.setVisible(false);
        simpDecOptDialog = null;
        
        return pl;
    }

    /** 
     * Creates a dialog box for advanced decoding options 
     * */
    private void createAdvancedGui() {
	advDecOptDialog = new JDialog(mainFrame,"Decoding Parameters");

        // Panel for dialog box
	JPanel decDialogPane = new JPanel(new BorderLayout());
	advDecOptDialog.setContentPane(decDialogPane);
	
	JPanel buttonsPane = new JPanel();
	decOkBut = new JButton("OK");
	decOkBut.addActionListener(oi);
	buttonsPane.add(decOkBut);

	decDefBut = new JButton("Default");
	decDefBut.addActionListener(this);
	buttonsPane.add(decDefBut);

	decCancelBut = new JButton("Cancel");
	decCancelBut.addActionListener(this);
	buttonsPane.add(decCancelBut);

	decDialogPane.add(buttonsPane,BorderLayout.SOUTH);
	
	//****** General Options Tab ***********
	JPanel genTab = new JPanel(new BorderLayout());
	JPanel genTabSuper = new JPanel(new GridLayout(2,1));

        // Show decoding info
        JPanel showInfPan = new JPanel();
        showDecInf = new JCheckBox("Display decoding info",false);
        showDecInf.setToolTipText("Show decoding info and warning"+
                                  " messages");
        showInfPan.add(showDecInf);

	// Rate option
	JPanel genTabPane2 = new JPanel();

	genTabPane2.add(new JLabel("Decoding rate:"));

	genTabPane2.add(decRateField);	
	genTabPane2.add(new JLabel("bpp"));
	
	genTabSuper.add(genTabPane2);
        genTabSuper.add(showDecInf);
	genTab.add(genTabSuper,BorderLayout.CENTER);
	
	// ****** Advanced Otions Tab *********
	JPanel advTab = new JPanel(new BorderLayout());
	JPanel advTabSuper = new JPanel(new GridLayout(3,1));

	// Parsing option
	JPanel advTab2 = new JPanel();
	advTab2.add(new JLabel("Parsing:"));
	decParsOn = new JRadioButton("on",true);
	decParsOff = new JRadioButton("off",false);
	ButtonGroup parsingGrp = new ButtonGroup();
	parsingGrp.add(decParsOn);
	parsingGrp.add(decParsOff);
	advTab2.add(decParsOn);
	advTab2.add(decParsOff);
	advTabSuper.add(advTab2);
	// CodeStream Info
	JPanel advTab3 = new JPanel();
	advTab3.add(new JLabel("Codestream Info:"));
	codeStrInfoOn = new JRadioButton("on",false);
	codeStrInfoOff = new JRadioButton("off",true);
	ButtonGroup codeStrInfoGrp = new ButtonGroup();
	codeStrInfoGrp.add(codeStrInfoOn);
	codeStrInfoGrp.add(codeStrInfoOff);
	advTab3.add(codeStrInfoOn);
	advTab3.add(codeStrInfoOff);
	advTabSuper.add(advTab3);
	//resolution level option
	JPanel advTab4 = new JPanel();
	decResChkBox = new JCheckBox("Resolution level:",false);
	advTab4.add(decResChkBox);
	decResTf = new JTextField("",5);
	advTab4.add(decResTf);
	advTabSuper.add(advTab4);

	advTab.add(advTabSuper,BorderLayout.NORTH);
	

	//******* Entropy Decoding option *******
	JPanel entropyDecTab = new JPanel(new BorderLayout());
	JPanel entrDecSuper = new JPanel(new GridLayout(2,1));
	
	//error detecttion option
	JPanel entrDec1 = new JPanel();
	entrDec1.add(new JLabel("Detect synchronization errors:"));
	cerOn = new JRadioButton("on",true);
	cerOff = new JRadioButton("off",false);

	ButtonGroup cerGrp = new ButtonGroup();
	cerGrp.add(cerOn);
	cerGrp.add(cerOff);
	entrDec1.add(cerOn);
	entrDec1.add(cerOff);
	entrDecSuper.add(entrDec1);

	//cverber option
	JPanel entrDec2 = new JPanel();
	entrDec2.add(new JLabel("Display synchronization errors:"));
	cverberOn = new JRadioButton("on",true);
	cverberOff = new JRadioButton("off",false);
	ButtonGroup cverberGrp = new ButtonGroup();
	cverberGrp.add(cverberOn);
	cverberGrp.add(cverberOff);
	entrDec2.add(cverberOn);
	entrDec2.add(cverberOff);
	entrDecSuper.add(entrDec2);
	
	entropyDecTab.add(entrDecSuper,BorderLayout.NORTH);

	//********* Dec ROI ********
	JPanel decROITab = new JPanel(new BorderLayout());
	JPanel noRoiPan = new JPanel();
	noRoiPan.add(new JLabel("Process ROI : "));
	noRoiOn = new JRadioButton("on",true);
	noRoiOff = new JRadioButton("off",false);
	ButtonGroup noRoiGrp = new ButtonGroup();
	noRoiGrp.add(noRoiOn);
	noRoiGrp.add(noRoiOff);
	noRoiPan.add(noRoiOn);
	noRoiPan.add(noRoiOff);
	decROITab.add(noRoiPan,BorderLayout.CENTER);

	// *************** Color space ***************
	JPanel csTab = new JPanel();
	csIgnoreBox = new JCheckBox("Ignore color space information");
	csTab.add(csIgnoreBox);

	// *************** Security ***************
	JPanel secTab = new JPanel();
	secTab.add(new JLabel("Public key"));
	selectedKeyTF = new JTextField("",10);
	rsaPubKey = rsaSupport.getRawRSAPubKey();
	selectedKeyTF.setText(rsaSupport.getPubKeyName());
	selectedKeyTF.setEnabled(false);
	secTab.add(selectedKeyTF);
	JButton selectPubKey = new JButton("Select Key");
	selectPubKey.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rsaSupport.createMngKeyUI(mainFrame,RSASupport.
					      DISPLAY_PUBLIC);
		    rsaPubKey = rsaSupport.getRawRSAPubKey();
		    selectedKeyTF.setText(rsaSupport.getPubKeyName());
		}
	    });
	secTab.add(selectPubKey);
// 	JPanel secTab = new JPanel();
// 	secTab.add(new JLabel("Public key:"));
// 	secSeedTF = new JTextField("-1",5);
// 	secTab.add(secSeedTF);
	
	// *************** Tabbed pane ***************
	JTabbedPane tabPane = new JTabbedPane();
		
	tabPane.add("General",genTab);
	tabPane.add("Advanced",advTab);
	tabPane.add("Entropy Coder",entropyDecTab);
	tabPane.add("Region of Interest",decROITab);
	tabPane.add("Colorspace",csTab);
	tabPane.add("Security",secTab);

	decDialogPane.add(tabPane,BorderLayout.CENTER);

	advDecOptDialog.setModal(true);
	advDecOptDialog.pack();
        advDecOptDialog.setLocationRelativeTo(mainFrame);
    }

    /** 
     * Gets advanced parameters values, creates the ParameterList instance and
     * close all dialog windows 
     * */
    public ParameterList getAdvParameters() {
	// Initialize default parameters
        ParameterList defpl = new ParameterList();
	String[][] param = Decoder.getAllParameters();
	for (int i=param.length-1; i>=0; i--) {
	    if(param[i][3]!=null)
		defpl.put(param[i][0],param[i][3]);
        }

	// Create parameter list using defaults
        ParameterList pl = new ParameterList(defpl);
	
	// Put arguments into ParameterList
        pl.put("rate",""+decRateField.getText());
        if(showDecInf.isSelected()) {
            pl.put("verbose","on");
        }
        if(decParsOn.isSelected()) {
            pl.put("parsing","on");
        } else {
            pl.put("parsing","off");
        }
        if(codeStrInfoOn.isSelected()) {
            pl.put("cdstr_info","on");
            pl.put("verbose","on");
        }
        if(decResChkBox.isSelected()) {
            pl.put("res",decResTf.getText());
        }
        if(cerOn.isSelected()) {
            pl.put("Cer","on");
        }
        if(cverberOn.isSelected()) {
            pl.put("Cverber","on");
        }
        if(noRoiOn.isSelected()) {
            pl.put("Rno_roi","on");
        }
	if(csIgnoreBox.isSelected()) {
	    pl.put("nocolorspace", "on");
	}
	pl.put("debug","off");
	if(rsaPubKey!=null) {
	    pl.put("Spublic_key", rsaPubKey.getExponent().toString()+" "
		   +rsaPubKey.getModulus().toString());
	}
        advDecOptDialog.setVisible(false);
        advDecOptDialog = null;
        
        return pl;
    }

    public void actionPerformed(ActionEvent e) {
	Object o = e.getSource();

        if(o==decRateField) { // Modification of the decoding bit-rate
            // in the text field
            if(decRateSlider!=null) {
                int max = decRateSlider.getMaximum();
                int min = decRateSlider.getMinimum();
                double val = max;
                try {
                    val = (new Double(decRateField.getText())).doubleValue()*
                        decSliderPrec;
                } catch(NumberFormatException nfe) {
                    decRateSlider.setValue(max);
                    decRateField.setText(""+((float)max/decSliderPrec));
                }
                if(val<min) {
                    decRateSlider.setValue(min);
                } else if(val>max) {
                    decRateSlider.setValue(max);
                } else {
                    decRateSlider.setValue((int)val);
                }
            }
        } else if(o==decOptCancelBut) { // Cancel simple decoding
            simpDecOptDialog.setVisible(false);
            simpDecOptDialog = null;
        } else if(o==decOptAdvancedBut) { // Request advanced option menu
            simpDecOptDialog.setVisible(false);
            simpDecOptDialog = null;
            createAdvancedGui();
            advDecOptDialog.setVisible(true);
        } else if(o==decDefBut) { // Default values for advanced options
            showDecInf.setSelected(false);
	    cerOn.setSelected(true);
	    cverberOn.setSelected(true);
	    codeStrInfoOff.setSelected(true);
	    decParsOn.setSelected(true);
	    noRoiOn.setSelected(true);
	    decRateField.setText("");
	    decResChkBox.setSelected(false);
	    decResTf.setText("");
        } else if(o==decCancelBut) { // Cancel advanced decoding
            advDecOptDialog.setVisible(false);
            advDecOptDialog = null;
        }
    }

    /** 
     * Method handling modification of the sliders values 
     * */
    public void stateChanged(ChangeEvent e) {
        double val = (double)decRateSlider.getValue()/decSliderPrec;
        decRateField.setText(f.format(val));
    }
}
