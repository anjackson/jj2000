package gui;

import jj2000.j2k.encoder.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;

import cryptix.provider.rsa.*;

import gui.rsa.*;

import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.awt.*;
import java.io.*;

/** Class to graphically collect JPEG 2000 encoding parameters before
 * launching the encoding process. */
public class J2KGuiEncoder implements ChangeListener, ActionListener, 
                                      ItemListener, 
                                      MouseListener, MouseMotionListener {
    /** Reference to the main frame */
    private Main mainFrame;
    /** Reference to the SaveImage instance that called this object */
    private SaveImage si;
    /** Reference to the file containing the image to encode */
    private File inputFile;
    /** Reference to the panel used to display the input image */
    private JJImgPanel imgPan = null;
    /** Reference to the RSA support modules */
    private RSASupport rsaSupport;
    
    /** Dimension of the input image (used to compute the maximum bit-rate) */
    private Dimension inDim;

    /** Image offset in canvas */
    private Coord imgOff = new Coord(0,0);
    /** Saved image offset before moving image with the mouse */
    private Coord savedImgOff = new Coord(0,0);

    /** Image security */
    private JRadioButton secNoShape, secWholeImage, secUserDef;
    private JTextField secUlxTF,secUlyTF,secWTF,secHTF,
	secSeedTF,secResStartTF,secBrStartTF;
    private JRadioButton secWavMeth,secBitMeth;

    /** Format of the displayed floats (3 fractional digits) */
    private static final DecimalFormat f = new DecimalFormat("##0.000");

    /** Initial point of the selection (Corner of a rectangular ROI,
     * center of a circular ROI,...). */
    private Coord selectOrig = new Coord(0,0);

    /** No ROI is selected */
    public final static int NONE = 0;
    /** A rectangular ROI is selected */
    public final static int RECT_ROI = 1;
    /** A circular ROI is selected */
    public final static int CIRC_ROI = 2;
    /** Set image offset in canvas */
    public final static int MOVE_IMAGE = 3;
    /** Define tile partition with mouse */
    public final static int TILE_PARTITION = 4;
    /** A rectangular area is selected for scrambling */
    public final static int RECT_SECURE = 5;
    /** Type of the current selection tool */
    private int selectType = NONE;

    private Rectangle rectSelect = new Rectangle();
    private int cROIx, cROIy, cROIrad;
    private double zf;
    
    /** Simple encoding options */
    private JDialog simpEncOptDialog;
    private JSlider encRateSlider;
    private JTextField encRateField;
    private JRadioButton lossyBut, losslessBut,mouseImgOff,mouseTile;
    public JButton encOptOkBut;
    private JButton encOptCancelBut, encOptAdvancedBut;
    /** Precision of the slider used to select the output bit-rate */
    private static float encSliderPrec = 1000;
    private double maxRate;

    /** Advanced encoding options */
    private JDialog advEncOptDialog;
    private JCheckBox mctCheckBox, cppCheckBox, ralignBox;
    private JRadioButton mctOn, mctOff;
    private JTextField qstepTf;
    private JComboBox gbCombo, qtypeCombo, rMaskComboBox, roiStartLvlComb;
    private JComboBox cLenCalcCombo, wLevCb;
    private JTextField rectLeftTf, rectTopTf, rectWidthTf, rectHeightTf;
    private JTextField circColTf, circRowTf, circRadTf, cppDimTf;
    private JButton maskBrowseButton;
    private JCheckBox cSegSymbBox,cCausalBox,cRegTermBox,cResetMQBox,
	cBypassBox;
    private JComboBox ctermCombo;
    private JCheckBox showEncInf;
    private JComboBox cblkSizHeightCb, cblkSizWidthCb;
    private JTextField aLayersTf;
    private JComboBox apTypeCombo;
    private JCheckBox pphMainBox, pphTileBox, pEphBox, pSopBox;
    private JRadioButton lossless, lossy;
    private JTextField tilePartsField, tileWidth, tileHeight;
    private JCheckBox tileBox;
    private JTextField refxTF, refyTF, trefxTF, trefyTF;
    private JRadioButton mouseRectROI, mouseCircROI;
    public JButton encOkBut;
    private JButton encDefBut, encCancelBut;
    private String roiMaskAbsName;
    private JLabel roiMaskNameLabel;

    private RawRSAPrivateKey rsaPrivKey = null;
    private JTextField selectedKeyTF;
    private JRadioButton useRSAEncryption;

    /** Class constructor */
    public J2KGuiEncoder(Main mainFrame,SaveImage si,File inputFile,
                         Dimension inDim, JJImgPanel imgPan) {
        this.mainFrame = mainFrame;
        this.inputFile = inputFile;
        this.si = si;
        this.inDim = inDim;
        this.imgPan = imgPan;
        zf = imgPan.getZoomFactor();
	rsaSupport = mainFrame.getRSASupport();
        imgPan.addMouseListener(this);
        imgPan.addMouseMotionListener(this);
    }

    /** Start the encoding operation by first displaying dialog box(es) in
     * order to collect parameters */
    public void start() {
        mainFrame.enableZoom(false);
        createSimpleGUI();
        simpEncOptDialog.setVisible(true);
    }

    /** Creates the dialog box used to collect simple encoding parameters */
    private void createSimpleGUI() {
        simpEncOptDialog = new JDialog(mainFrame,"Encoding rate");
        JPanel simpEncOptPan = new JPanel(new GridLayout(3,1));
                        
        // Lossy coding rate
        JPanel encLossyPan = new JPanel();
        lossyBut = new JRadioButton("Lossy",true);
        lossyBut.addItemListener(this);
        encLossyPan.add(lossyBut);
        // Search maximum bit-depth 
        maxRate = inputFile.length()*8d/inDim.width/inDim.height;
        encRateSlider = new JSlider(JSlider.HORIZONTAL,0,
                                    (int)(maxRate*encSliderPrec),
                                    (int)(maxRate*encSliderPrec));
        encRateSlider.addChangeListener(this);
        encLossyPan.add(encRateSlider);
        
        encRateField = new JTextField(""+f.format(maxRate),5);
        encRateField.addActionListener(this);
        encLossyPan.add(encRateField);
        encLossyPan.add(new JLabel("bpp"));
        simpEncOptPan.add(encLossyPan);
        
        // Lossless mode
        JPanel encLosslessPan = new JPanel();
        losslessBut = new JRadioButton("Lossless",false);
        losslessBut.addItemListener(this);
        encLosslessPan.add(losslessBut);
        simpEncOptPan.add(encLosslessPan);
        
        ButtonGroup lossButGrp = new ButtonGroup();
        lossButGrp.add(lossyBut);
        lossButGrp.add(losslessBut);
        
        // Confirmation buttons
        JPanel encConfirmPan = new JPanel();
        encOptOkBut = new JButton("OK");
        encOptOkBut.addActionListener(si);
        encConfirmPan.add(encOptOkBut);
        encOptCancelBut = new JButton("Cancel");
        encOptCancelBut.addActionListener(this);
        encConfirmPan.add(encOptCancelBut);
        encOptAdvancedBut = new JButton("Advanced...");
        encOptAdvancedBut.addActionListener(this);
        encConfirmPan.add(encOptAdvancedBut);
        simpEncOptPan.add(encConfirmPan);
        
        simpEncOptDialog.getContentPane().add(simpEncOptPan);
        simpEncOptDialog.setModal(true);
        simpEncOptDialog.pack();
        simpEncOptDialog.setLocationRelativeTo(mainFrame);
   }

    /** 
     * Get parameters collected by the simple dialog box and build a
     * ParameterList instance from them. 
     * */
    public ParameterList getSimpParameters() {
	// Initialize default parameters
        ParameterList defpl = new ParameterList();
	String[][] param = Encoder.getAllParameters();
	for (int i=param.length-1; i>=0; i--) {
	    if(param[i][3]!=null)
		defpl.put(param[i][0],param[i][3]);
        }

	// Create parameter list using defaults
        ParameterList pl = new ParameterList(defpl);

        // Put arguments in ParameterList
        if(losslessBut.isSelected()) {
            pl.put("lossless","on");
        } else {
	    double curRate = (new Double(encRateField.getText())).
		doubleValue();
	    if(Math.abs(curRate-maxRate)>0.001) {
		pl.put("rate",encRateField.getText());
	    }
        }
        pl.put("verbose","off");
        simpEncOptDialog.setVisible(false);
        simpEncOptDialog = null;

        return pl;
    }

    /** 
     * Creates a dialog box for collecting advanced encoding options
     * */
    public void createAdvancedGui() {
	advEncOptDialog = new JDialog(mainFrame,"Encoding Parameters");

	// ********************* General Tab ************************
	JPanel genTab = new JPanel();
        
	ButtonGroup lossButGrp = new ButtonGroup();
	// Lossless mode
	JPanel losslessPan = new JPanel();
	lossless = new JRadioButton("Lossless",false);
	lossButGrp.add(lossless);
	lossless.addItemListener(this);
	losslessPan.add(lossless);
	genTab.add(losslessPan);

        // Lossy mode
        JPanel genTab2 = new JPanel();
	lossy = new JRadioButton("Lossy",true);
	lossButGrp.add(lossy);
	lossy.addItemListener(this);
	genTab2.add(lossy);

        genTab2.add(new JLabel("Rate:"));
        encRateField.addActionListener(this);
	genTab2.add(encRateField);
        genTab2.add(new JLabel("bpp"));
        genTab.add(genTab2);

        // Show encoding information
        showEncInf = new JCheckBox("Show encoding information",false);
        showEncInf.setToolTipText("Show information and warnings related "+
                                  "to the encoding of the image");
        genTab.add(showEncInf);
	genTab.setLayout(new BoxLayout(genTab,BoxLayout.Y_AXIS));

	//******************* Canvas Tab **********************
	JPanel canvasTab = new JPanel(new GridLayout(2,1));

	// Image offset
	JPanel opt1 = new JPanel();
	opt1.add(new JLabel("Image offset:"));
	
	// X-offset
	opt1.add(new JLabel("x:"));
	refxTF = new JTextField("0",5);
	refxTF.addActionListener(this);
	opt1.add(refxTF);

	// Y-offset
	opt1.add(new JLabel("y:"));
	refyTF = new JTextField("0",5);
	refyTF.addActionListener(this);
	opt1.add(refyTF);

	mouseImgOff = new JRadioButton("Mouse defined",false);
	mouseImgOff.addItemListener(this);
	opt1.add(mouseImgOff);
	canvasTab.add(opt1);	
	
	// Tiles
	JPanel opt2 = new JPanel(new GridLayout(2,3));
	opt2.setBorder(BorderFactory.createTitledBorder("Tiles"));

	// Enable/disable
	tileBox = new JCheckBox("Tiling",false);
	tileBox.addItemListener(this);
	opt2.add(tileBox);

	// Tile partition offset
	JPanel tpoxPan = new JPanel();
	tpoxPan.add(new JLabel("x:"));
	trefxTF = new JTextField("0",5);
	trefxTF.setEditable(false);
	tpoxPan.add(trefxTF);
	opt2.add(tpoxPan);
	
	JPanel tpoyPan = new JPanel();
	tpoyPan.add(new JLabel("y:"));
	trefyTF = new JTextField("0",5);
	trefyTF.setEditable(false);
	tpoyPan.add(trefyTF);
	opt2.add(tpoyPan);

	// mouse defined
	mouseTile = new JRadioButton("Mouse defined",false);
	mouseTile.addItemListener(this);
	opt2.add(mouseTile);

	// Tiles dimensions
	JPanel tdwPan = new JPanel();
	tdwPan.add(new JLabel("width:"));
	tileWidth = new JTextField("0",5);
	tileWidth.setEditable(false);
	tdwPan.add(tileWidth);
	opt2.add(tdwPan);

	JPanel tdhPan = new JPanel();
	tdhPan.add(new JLabel("height:"));
	tileHeight = new JTextField("0",5);
	tileHeight.setEditable(false);
	tdhPan.add(tileHeight);
	opt2.add(tdhPan);

	canvasTab.add(opt2);

	JPanel encTab = new JPanel();
	encTab.add(canvasTab);

	//***** Multi-component transform Tab ********
	JPanel mTab = new JPanel();
	mctCheckBox = new JCheckBox("Component Transformation:",false);
	mctCheckBox.addItemListener(this);
	mTab.add(mctCheckBox);

	mctOn = new JRadioButton("on",false);
	mctOn.setEnabled(false);
	mTab.add(mctOn);

	mctOff = new JRadioButton("off",true);
	mctOff.setEnabled(false);
	mTab.add(mctOff);

	ButtonGroup mctButGrp = new ButtonGroup();
	mctButGrp.add(mctOn);
	mctButGrp.add(mctOff);
	
	//****** Wavelet Transform Tab *******
	JPanel wTab = new JPanel();
        wLevCb = new JComboBox();
        for(int i=0; i<=10; i++) {
            wLevCb.addItem(""+i);
        }
        wLevCb.setSelectedItem("5");
	wTab.add(new JLabel("Number of decomposition levels:"));
	wTab.add(wLevCb);

	//***** Quantization Tab *********
	JPanel qTab = new JPanel(new GridLayout(3,1));

	JPanel qTab1 = new JPanel();
	qTab1.add(new JLabel("Number of guard bits:"));

	gbCombo = new JComboBox();
        for(int i=0; i<10; i++) {
            gbCombo.addItem(""+i);
        }
        gbCombo.setSelectedItem("2");
	qTab1.add(gbCombo);
	qTab.add(qTab1);

	JPanel qTab2 = new JPanel();
	qTab2.add(new JLabel("Step size :"));

	qstepTf = new JTextField("0.0078125",15);
	qTab2.add(qstepTf);
	qTab.add(qTab2);

	JPanel qTab3 = new JPanel();
	qTab3.add(new JLabel("Quantization type:"));

	qtypeCombo = new JComboBox();
        qtypeCombo.removeAllItems();
	qtypeCombo.addItem("derived");
	qtypeCombo.addItem("expounded");
	qtypeCombo.setSelectedItem("expounded");
	qTab3.add(qtypeCombo);
	qTab.add(qTab3);

	//****** Region Of Interest Tab ***********
	JPanel rTab = new JPanel(new BorderLayout());
	
	JPanel rTab1 = new JPanel();
	rTab1.add(new JLabel("ROI Start level :"));
	roiStartLvlComb = new JComboBox();
        for(int i=-1; i<10; i++) {
            roiStartLvlComb.addItem(""+i);
        }
        roiStartLvlComb.setSelectedItem("-1");
	rTab1.add(roiStartLvlComb);

	JPanel rTab2 = new JPanel();
	ralignBox = new JCheckBox("Mask aligned on code-blocks",false);
	rTab2.add(ralignBox);
	
	JPanel rTab12 = new JPanel(new GridLayout(1,2));
	rTab12.add(rTab1);
	rTab12.add(rTab2);
	rTab.add(rTab12,BorderLayout.NORTH);

        // ROI's shape and location
	JPanel rTab4 = new JPanel();
	JPanel rTab4West = new JPanel();
	rTab4West.add(new JLabel("Mask:"));
	rMaskComboBox = new JComboBox();
	rMaskComboBox.addItem("Disabled");
	rMaskComboBox.addItem("Rectangular");
	rMaskComboBox.addItem("Circular");
	rMaskComboBox.addItem("Arbitrary");
	rMaskComboBox.setSelectedItem("Disabled");
	rMaskComboBox.addActionListener(this);
	rTab4West.add(rMaskComboBox);
	rTab4.add(rTab4West,BorderLayout.WEST);

	JPanel rTab4Center = new JPanel(new GridLayout(3,1));
	JPanel rTab4Center1 = new JPanel(new GridLayout(2,1));
	JPanel rTab4Center1Up = new JPanel();

	rTab4Center1Up.add(new JLabel("x:"));
	rectLeftTf = new JTextField("",5);

	rTab4Center1Up.add(rectLeftTf);
	
	rTab4Center1Up.add(new JLabel("y:"));
	rectTopTf = new JTextField("",5);

	rTab4Center1Up.add(rectTopTf);
	
	rTab4Center1Up.add(new JLabel("width:"));
	rectWidthTf = new JTextField("",5);
	
	rTab4Center1Up.add(rectWidthTf);

	rTab4Center1Up.add(new JLabel("height:"));
	rectHeightTf = new JTextField("",5);

	rTab4Center1Up.add(rectHeightTf);

	rectLeftTf.setEditable(false);
	rectTopTf.setEditable(false);
	rectWidthTf.setEditable(false);
	rectHeightTf.setEditable(false);
	
	JPanel rTab4Center1Down = new JPanel();
	mouseRectROI = new JRadioButton("Mouse defined",false);
	mouseRectROI.setEnabled(false);
	mouseRectROI.addItemListener(this);
	rTab4Center1Down.add(mouseRectROI);

	rTab4Center1.add(rTab4Center1Up);
	rTab4Center1.add(rTab4Center1Down);
	rTab4Center1.setBorder(BorderFactory.
			       createTitledBorder("Rectangular"));
	rTab4Center.add(rTab4Center1);

	JPanel rTab4Center2 = new JPanel(new GridLayout(2,1));
	JPanel rTab4Center2Up = new JPanel();

	rTab4Center2Up.add(new JLabel("x:"));
	circColTf = new JTextField("",5);

	rTab4Center2Up.add(circColTf);

	rTab4Center2Up.add(new JLabel("y:"));
	circRowTf = new JTextField("",5);

	rTab4Center2Up.add(circRowTf);

	rTab4Center2Up.add(new JLabel("radius:"));
	circRadTf = new JTextField("",5);

	rTab4Center2Up.add(circRadTf);
	
	circColTf.setEditable(false);
	circRowTf.setEditable(false);
	circRadTf.setEditable(false);
	
	rTab4Center2.setBorder(BorderFactory.createTitledBorder("Circular"));
	rTab4Center2.add(rTab4Center2Up);
	
	JPanel rTab4Center2Down = new JPanel();
	mouseCircROI = new JRadioButton("Mouse defined",false);
	mouseCircROI.setEnabled(false);
	mouseCircROI.addItemListener(this);
	rTab4Center2Down.add(mouseCircROI);

	rTab4Center2.add(rTab4Center2Down);
	
	rTab4Center.add(rTab4Center2);

	JPanel rTab4Center3 = new JPanel();
	
	rTab4Center3.add(new JLabel("Choose .pgm file:"));
	maskBrowseButton = new JButton("Browse");
        maskBrowseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc3 = new JFileChooser();
                    fc3.
                        addChoosableFileFilter(new JJFileFilter("PGM images",
                                                                "pgm"));
                    int returnVal3 = fc3.showOpenDialog(mainFrame);

                    if(returnVal3==JFileChooser.APPROVE_OPTION) {
                        File choosedMaskFile = fc3.getSelectedFile();
                        roiMaskAbsName = choosedMaskFile.getAbsolutePath();
                        roiMaskNameLabel.setText(choosedMaskFile.getName());
                    }
                }
            });
	maskBrowseButton.addActionListener(this);
	maskBrowseButton.setEnabled(false);
	rTab4Center3.add(maskBrowseButton);

	roiMaskNameLabel = new JLabel("");
        rTab4Center3.add(roiMaskNameLabel);
	
	rTab4Center3.setBorder(BorderFactory.createTitledBorder("Arbitrary"));
	rTab4Center.add(rTab4Center3);
	rTab4.add(rTab4Center,BorderLayout.CENTER);
	rTab4.setBorder(BorderFactory.createTitledBorder("ROI's shape"+
							 " & location :"));
	rTab.add(rTab4,BorderLayout.CENTER);

	
	//**************** Entropy encoder Tab **************
	JPanel cTab = new JPanel(new GridLayout(9,1));
	
	JPanel cblkSizPanel = new JPanel();
	cblkSizPanel.add(new JLabel("Code-block size:"));
	cblkSizPanel.add(new JLabel("w:"));
	cblkSizWidthCb = new JComboBox();
	cblkSizHeightCb = new JComboBox();
	int[] cblkSizeVal = { 4, 8, 16, 32, 64, 128, 256, 512, 1024};
	for(int i=0; i<cblkSizeVal.length; i++) {
	    cblkSizHeightCb.addItem(""+cblkSizeVal[i]);
	    cblkSizWidthCb.addItem(""+cblkSizeVal[i]);
	}
	cblkSizHeightCb.setSelectedItem("64");
	cblkSizWidthCb.setSelectedItem("64");
	cblkSizPanel.add(cblkSizWidthCb);
	cblkSizPanel.add(new JLabel("h:"));
	cblkSizPanel.add(cblkSizHeightCb);
	cTab.add(cblkSizPanel);

	JPanel cppPanel = new JPanel();
	cppCheckBox = new JCheckBox("Precinct partition:",false);
	cppCheckBox.addItemListener(this);
	cppPanel.add(cppCheckBox);
	cppPanel.add(new JLabel("Dimensions:"));
	cppDimTf = new JTextField("",8);
	cppDimTf.setEditable(false);
	cppPanel.add(cppDimTf);
	cTab.add(cppPanel);

	JPanel cLenCalcPanel = new JPanel();
	cLenCalcPanel.add(new JLabel("MQ length calculation:"));
	String[] lenCalcOpt = {"near_opt", "lazy_good", "lazy"};
	cLenCalcCombo = new JComboBox(lenCalcOpt);
	cLenCalcCombo.setSelectedItem("near_opt");
	cLenCalcPanel.add(cLenCalcCombo);
	cTab.add(cLenCalcPanel);

	JPanel ctermPanel = new JPanel();
	ctermPanel.add(new JLabel("MQ termination type:"));
	String[] ctermOpt = {"near_opt", "easy", "predict", "full"};
	ctermCombo = new JComboBox(ctermOpt);
	ctermCombo.setSelectedItem("near_opt");
	ctermPanel.add(ctermCombo);
	cTab.add(ctermPanel);

	cSegSymbBox = new JCheckBox("Segmentation symbols",false);
	cTab.add(cSegSymbBox);

	cCausalBox = new JCheckBox("Causal context",false);
	cTab.add(cCausalBox);

	cRegTermBox = new JCheckBox("Terminate each coding-pass");
	cTab.add(cRegTermBox);

	cResetMQBox = new JCheckBox("Reset MQ");
	cTab.add(cResetMQBox);

	cBypassBox = new JCheckBox("MQ bypass");
	cTab.add(cBypassBox);

	//********************** Rate allocator Tab *************************
	JPanel aTab = new JPanel(new GridLayout(2,1));
	
	JPanel aTabNorth = new JPanel();
	aTabNorth.add(new JLabel("Layers:"));
	aLayersTf = new JTextField("0.015 +20 2.0 +10",20);
	aTabNorth.add(aLayersTf);
	aTab.add(aTabNorth);
	
	JPanel aTabCenter = new JPanel();
	aTabCenter.add(new JLabel("Progression type :"));
	String[] apTypeOpt = {"Resolution", "Layer", "RPCL", "PCRL", "CPRL"};
	apTypeCombo = new JComboBox(apTypeOpt);
	apTypeCombo.setSelectedItem("Layer");
	aTabCenter.add(apTypeCombo);
	aTab.add(aTabCenter);
	
	//********************** Packet Tab *************************
	JPanel pTab = new JPanel(new GridLayout(5,1));
	
	pphTileBox = new JCheckBox("Packed packet header in tile header",
				   false);
	pTab.add(pphTileBox);

	pphMainBox = new JCheckBox("Packed packet header in main header:",
				   false);
	pTab.add(pphMainBox);

	pEphBox = new JCheckBox("End Of Packet markers (EPH)",false);
	pTab.add(pEphBox);

	pSopBox = new JCheckBox("Start Of Packet makers (SOP)",false);
	pTab.add(pSopBox);
	
	// Fifth option
	JPanel pOpt5 = new JPanel();
	pOpt5.add(new JLabel("Packets per tile-part:"));
	tilePartsField = new JTextField("0",5);
	pOpt5.add(tilePartsField);
	pTab.add(pOpt5);

	// ******************** Security Tab ********************
	JPanel secTab = new JPanel(new GridLayout(4,1));
	
	// Shape type
	JPanel secTab1 = new JPanel(new GridLayout(1,3));
	secTab1.setBorder(BorderFactory.createTitledBorder("Shape Type"));
	ButtonGroup secShapeGroup = new ButtonGroup();
	secNoShape = new JRadioButton("No Protection",true);
	secNoShape.addActionListener(this);
	secShapeGroup.add(secNoShape);
	secTab1.add(secNoShape);
	secWholeImage = new JRadioButton("Whole image");
	secWholeImage.addActionListener(this);
	secShapeGroup.add(secWholeImage);
	secTab1.add(secWholeImage);
	secUserDef = new JRadioButton("Mouse defined shape");
	secUserDef.addActionListener(this);
	secUserDef.addItemListener(this);
	secShapeGroup.add(secUserDef);
	secTab1.add(secUserDef);

	secUlxTF = new JTextField("0",5);
	secUlyTF = new JTextField("0",5);
	secWTF = new JTextField("0",5);
	secHTF = new JTextField("0",5);

	// Method type
	JPanel secTab2 = new JPanel(new GridLayout(1,2));
	secTab2.setBorder(BorderFactory.createTitledBorder("Method Type"));
	ButtonGroup secMethGrp = new ButtonGroup();
	secWavMeth = new JRadioButton("Wavelet domain",true);
	secWavMeth.addActionListener(this);
	secMethGrp.add(secWavMeth);
	secTab2.add(secWavMeth);
	secBitMeth = new JRadioButton("Bit stream domain");
	secBitMeth.addActionListener(this);
	secMethGrp.add(secBitMeth);
	secTab2.add(secBitMeth);

	// Method parameters
	JPanel secTab3 = new JPanel(new GridLayout(1,3));
	secTab3.setBorder(BorderFactory.createTitledBorder("Parameters"));
	JPanel secLayerPan = new JPanel();
	secLayerPan.add(new JLabel("Bit-rate start:"));
	float initRate = (new Float(encRateField.getText())).floatValue()/10f;
	secBrStartTF = new JTextField(""+initRate,5);
	secBrStartTF.setEditable(false);
	secLayerPan.add(secBrStartTF);
	secTab3.add(secLayerPan);

	JPanel secResPan = new JPanel();
	secResPan.add(new JLabel("Resolution level start:"));
	secResStartTF = new JTextField("-1",3);
	secResPan.add(secResStartTF);
	secTab3.add(secResPan);

	JPanel secSeedPan = new JPanel();
	secSeedPan.add(new JLabel("Seed:"));
	secSeedTF = new JTextField("16449",5);
	secSeedPan.add(secSeedTF);
	secTab3.add(secSeedPan);

	// RSA encryption
	JPanel secRSAPan = new JPanel();
	secRSAPan.setBorder(BorderFactory.createTitledBorder("RSA private "+
							     "key"));
	useRSAEncryption = new JRadioButton("Use encryption");
	secRSAPan.add(useRSAEncryption);
	selectedKeyTF = new JTextField("",10);
	rsaPrivKey = rsaSupport.getRawRSAPrivKey();
	selectedKeyTF.setText(rsaSupport.getPrivKeyName());
	selectedKeyTF.setEnabled(false);
	secRSAPan.add(selectedKeyTF);
	JButton selectPrivKey = new JButton("Select Key");
	selectPrivKey.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    rsaSupport.createMngKeyUI(mainFrame,RSASupport.
					      DISPLAY_PRIVATE);
		    rsaPrivKey = rsaSupport.getRawRSAPrivKey();
		    selectedKeyTF.setText(rsaSupport.getPrivKeyName());
		}
	    });
	secRSAPan.add(selectPrivKey);

	secTab.add(secTab1);
	secTab.add(secTab2);
	secTab.add(secTab3);
	secTab.add(secRSAPan);

	//******** End of encoding parameters Tab ********************  
	// Tabbed pane
	JTabbedPane tabPane = new JTabbedPane();
	
	tabPane.addTab("General",genTab);
	tabPane.addTab("Canvas",encTab);
	tabPane.addTab("MCT",mTab);
	tabPane.addTab("DWT",wTab);
	tabPane.addTab("Quantization",qTab);
	tabPane.addTab("ROI",rTab);
	tabPane.addTab("Entropy Coder",cTab);
	tabPane.addTab("Rate Allocator",aTab);
	tabPane.addTab("Packets",pTab);
	tabPane.addTab("Security",secTab);

	//***** Panel for dialog box
	Container encDialogPane = advEncOptDialog.getContentPane();
	encDialogPane.add(tabPane,BorderLayout.CENTER);
	
	JPanel buttons_panel = new JPanel();
	
	encOkBut = new JButton("OK");
	encOkBut.addActionListener(si);
	buttons_panel.add(encOkBut);
	
	encDefBut = new JButton("Default");
	encDefBut.addActionListener(this);
	buttons_panel.add(encDefBut);
	
	encCancelBut = new JButton("Cancel");
	encCancelBut.addActionListener(this);
	buttons_panel.add(encCancelBut);
	
	encDialogPane.add(buttons_panel,BorderLayout.SOUTH);
       
	advEncOptDialog.setModal(false);
	advEncOptDialog.pack();
        advEncOptDialog.setLocationRelativeTo(mainFrame);
    }

    /** 
     * Collect the advanced encoding parameters from the dialog box and build
     * a ParameterList instance 
     * */
    public ParameterList getAdvParameters() {
	// Initialize default parameters
        ParameterList defpl = new ParameterList();
	String[][] param = Encoder.getAllParameters();
	for (int i=param.length-1; i>=0; i--) {
	    if(param[i][3]!=null)
		defpl.put(param[i][0],param[i][3]);
        }

	// Create parameter list using defaults
        ParameterList pl = new ParameterList(defpl);

        // Put arguments in ParameterList
        // --- General Tab ---
        if(lossless.isSelected()) {
            pl.put("lossless","on");
        } else {
            pl.put("rate",encRateField.getText());
        }
        if(showEncInf.isSelected()) {
            pl.put("verbose","on");
        } else {
            pl.put("verbose","off");
        }
        // --- Canvas Tab ---
        pl.put("tile_parts", tilePartsField.getText());
        if(tileBox.isSelected()) {
            pl.put("tiles", tileWidth.getText()+" "+tileHeight.getText());
	    pl.put("tref", trefxTF.getText()+" "+trefyTF.getText());
        }
        pl.put("ref", refxTF.getText()+" "+refyTF.getText());
        // --- MCT ---
        if(mctCheckBox.isSelected()) {
            pl.put("Mct", "on");
        }
        // --- DWT ---
        pl.put("Wlev", (String)(wLevCb.getSelectedItem()));
        // -- Quantization ---
        pl.put("Qguard_bits", (String)(gbCombo.getSelectedItem()));
        if(qstepTf.isEditable()) {
            pl.put("Qstep", qstepTf.getText());
        }
        pl.put("Qtype", (String)(qtypeCombo.getSelectedItem()));
        // --- ROI ---
        pl.put("Rstart_level", (String)(roiStartLvlComb.getSelectedItem()));
        if(ralignBox.isSelected()) {
            pl.put("Ralign", "on");
        } else {
            pl.put("Ralign", "off");
        }
        String rMaskType = (String)(rMaskComboBox.getSelectedItem());
        if(rMaskType.equals("Rectangular")) {
            pl.put("Rroi", "R "+rectLeftTf.getText()+" "+
                   rectTopTf.getText()+" "+rectWidthTf.getText()+" "+
                   rectHeightTf.getText());
        } else if(rMaskType.equals("Circular")) {
            pl.put("Rroi", "C "+circColTf.getText()+" "+
                   circRowTf.getText()+" "+circRadTf.getText());
        } else if(rMaskType.equals("Arbitrary")) {
            pl.put("Rroi", "A "+roiMaskAbsName);
        }
        // --- Entropy coder ---
        if(cppCheckBox.isSelected()) {
            pl.put("Cpp", cppDimTf.getText());
        }
        pl.put("Clen_calc", (String)(cLenCalcCombo.getSelectedItem()));
        pl.put("Cterm_type", (String)(ctermCombo.getSelectedItem()));
        if(cSegSymbBox.isSelected()) {
            pl.put("Cseg_symbol", "on");
        } else {
            pl.put("Cseg_symbol", "off");
        }
        if(cCausalBox.isSelected()) {
            pl.put("Ccausal", "on");
        } else {
            pl.put("Ccausal", "off");
        }
        if(cRegTermBox.isSelected()) {
            pl.put("Cterminate", "on");
        } else {
            pl.put("Cterminate", "off");
        }
        if(cResetMQBox.isSelected()) {
            pl.put("CresetMQ", "on");
        } else {
            pl.put("CresetMQ", "off");
        }
        if(cBypassBox.isSelected()) {
            pl.put("Cbypass", "on");
        } else {
            pl.put("Cbypass", "off");
        }
	pl.put("Cblksiz", cblkSizWidthCb.getSelectedItem()+" "+
	       cblkSizHeightCb.getSelectedItem());
        // --- Rate Allocator ---
        pl.put("Alayers", aLayersTf.getText());
        String prog = (String)(apTypeCombo.getSelectedItem());
        if(prog.equals("Resolution")) {
            pl.put("Aptype", "res");
        } else if(prog.equals("Layer")) {
            pl.put("Aptype", "layer");
        } else if(prog.equals("RPCL")) {
            pl.put("Aptype", "res-pos");
        } else if(prog.equals("PCRL")){
            pl.put("Aptype", "pos-comp");
        } else if(prog.equals("CPRL")) {
            pl.put("Aptype", "comp-pos");
        }
        // --- Packets ---
        if(pphTileBox.isSelected()) {
            pl.put("pph_tile", "on");
        } else {
            pl.put("pph_tile", "off");
        }
        if(pphMainBox.isSelected()) {
            pl.put("pph_main", "on");
        } else {
            pl.put("pph_main", "off");
        }
        if(pEphBox.isSelected()) {
            pl.put("Peph", "on");
        } else {
            pl.put("Peph", "off");
        }
        if(pSopBox.isSelected()) {
            pl.put("Psop", "on");
        } else {
            pl.put("Psop", "off");
        }
	// --- Security ---
	if(secWavMeth.isSelected()) {
	    pl.put("Smethod", "wavelet");
	} else {
	    pl.put("Smethod", "layer");
	}
	if(secNoShape.isSelected()) {
	    pl.put("Sshape", "none");
	} else if(secWholeImage.isSelected()) {
	    pl.put("Sshape", "all");
	} else if(secUserDef.isSelected()) {
	    pl.put("Sshape", secUlxTF.getText()+" "+secUlyTF.getText()+
		   " "+secWTF.getText()+" "+secHTF.getText());
	}
	pl.put("Sseed", secSeedTF.getText());
	pl.put("Slevel_start", secResStartTF.getText());
	pl.put("Srate", secBrStartTF.getText());
	if(useRSAEncryption.isSelected()) {
	    if(rsaPrivKey==null) {
		pl.put("Sprivate_key", "-1 -1");
	    } else {
		pl.put("Sprivate_key", rsaPrivKey.getExponent().toString()+" "
		       +rsaPrivKey.getModulus().toString());
	    }
	} else {
	    pl.put("Sprivate_key", "-1 -1");
	}

        advEncOptDialog.setVisible(false);
        advEncOptDialog = null;
        return pl;
    }

    /** Handle action performed on registered components */
    public void actionPerformed(ActionEvent e) {
	Object o = e.getSource();
        
        if(o==encRateField) { // Modification of the decoding bit-rate
            // in the text field
            if(encRateSlider!=null) {
                int max = encRateSlider.getMaximum();
                int min = encRateSlider.getMinimum();
                double val = max;
                try {
                    val = (new Double(encRateField.getText())).doubleValue()*
                        encSliderPrec;
                } catch(NumberFormatException nfe) {
                    encRateSlider.setValue(max);
                    encRateField.setText(""+((float)max/encSliderPrec));
                }
                if(val<min) {
                    encRateSlider.setValue(min);
                } else if(val>max) {
                    encRateSlider.setValue(max);
                } else {
                    encRateSlider.setValue((int)val);
                }
            }
        } else if(o==encOptCancelBut) { // Cancel simple encoding
            simpEncOptDialog.setVisible(false);
            simpEncOptDialog = null;
            mainFrame.enableZoom(true);
        } else if(o==encCancelBut) { // Cancel advanced encoding options
            imgPan.enableDrawing(false);
            advEncOptDialog.setVisible(false);
            advEncOptDialog = null;
            mainFrame.enableZoom(true);
            selectType = NONE;
	    imgPan.setOffset(0,0);
        } else if(o==rMaskComboBox) {
	    String selMask = (String)(rMaskComboBox.getSelectedItem());
	    if(selMask.equals("Disabled")) { // No ROI
                // Disable selection of ROI shape
		rectLeftTf.setEditable(false);
		rectTopTf.setEditable(false);
		rectWidthTf.setEditable(false);
		rectHeightTf.setEditable(false);
		mouseRectROI.setEnabled(false);
		circColTf.setEditable(false);
		circRowTf.setEditable(false);
		circRadTf.setEditable(false);
		mouseCircROI.setEnabled(false);
		maskBrowseButton.setEnabled(false);
		selectType = NONE;
		imgPan.enableDrawing(false);
	    } else if(selMask.equals("Rectangular")) { // Rectangular shape
                // Enable manual and mouse-defined rectangular ROI selection
		rectLeftTf.setEditable(true);
		rectTopTf.setEditable(true);
		rectWidthTf.setEditable(true);
		rectHeightTf.setEditable(true);
		mouseRectROI.setEnabled(true);
		if(mouseRectROI.isSelected()) {
		    mouseImgOff.setSelected(false);
		    mouseTile.setSelected(false);
		    mouseCircROI.setSelected(false);
		    selectType = RECT_ROI;
		    imgPan.enableDrawing(true);
		} else {
		    selectType = NONE;
		    imgPan.enableDrawing(false);
		}

                // Disable all ROI selection modes
		circColTf.setEditable(false);
		circRowTf.setEditable(false);
		circRadTf.setEditable(false);
		mouseCircROI.setEnabled(false);
		maskBrowseButton.setEnabled(false);
	    } else if(selMask.equals("Circular")) { // Circular shape
                // Enable manual and mouse-defined circular ROI selection
		circColTf.setEditable(true);
		circRowTf.setEditable(true);
		circRadTf.setEditable(true);
		mouseCircROI.setEnabled(true);
		if(mouseCircROI.isSelected()) {
		    mouseImgOff.setSelected(false);
		    mouseTile.setSelected(false);
		    mouseRectROI.setSelected(false);
		    selectType = CIRC_ROI;  
		    imgPan.enableDrawing(true);
		} else {
		    selectType = NONE;
		    imgPan.enableDrawing(false);
		}

                // Disable other ROI selection modes
		rectLeftTf.setEditable(false);
		rectTopTf.setEditable(false);
		rectWidthTf.setEditable(false);
		rectHeightTf.setEditable(false);
		mouseRectROI.setEnabled(false);
		maskBrowseButton.setEnabled(false);
	    } else if(selMask.equals("Arbitrary")) { // Arbitrary shape
                // Enable PGM file selection
		maskBrowseButton.setEnabled(true);
		imgPan.enableDrawing(false);
		selectType = NONE;

                // Disable other ROI selection modes
		rectLeftTf.setEditable(false);
		rectTopTf.setEditable(false);
		rectWidthTf.setEditable(false);
		rectHeightTf.setEditable(false);
		mouseRectROI.setEnabled(false);
		circColTf.setEditable(false);
		circRowTf.setEditable(false);
		circRadTf.setEditable(false);
		mouseCircROI.setEnabled(false);
	    } 
        } else if(o==encOptAdvancedBut) {
            simpEncOptDialog.setVisible(false);
            simpEncOptDialog = null;
            createAdvancedGui();
            advEncOptDialog.setVisible(true);
        } else if(o==encDefBut) {
	    lossy.setSelected(true);
	    lossless.setSelected(false);
	    encRateField.setText("");
	    encRateField.setEditable(true);
	    pphTileBox.setSelected(true);
	    pphMainBox.setSelected(true);
	    tilePartsField.setText("0");
	    tileWidth.setText("0");
	    tileWidth.setEditable(false);
	    tileHeight.setText("0");
	    tileHeight.setEditable(false);
	    tileBox.setSelected(false);
	    refxTF.setText("0");
	    refyTF.setText("0");
	    trefxTF.setText("0");
	    trefyTF.setText("0");
	    mouseImgOff.setSelected(false);
	    mouseTile.setSelected(false);
	    wLevCb.setSelectedItem("5");
	    gbCombo.setSelectedItem("2");
	    qstepTf.setText("0.0078125");
            qtypeCombo.removeAllItems();
            qtypeCombo.addItem("expounded");
            qtypeCombo.addItem("derived");
	    qtypeCombo.setSelectedItem("expounded");
	    roiStartLvlComb.setSelectedItem("-1");
	    ralignBox.setSelected(false);
	    rMaskComboBox.setSelectedItem("Disabled");
	    rectLeftTf.setEditable(false);
	    rectLeftTf.setText("");
	    rectTopTf.setEditable(false);
	    rectTopTf.setText("");
	    rectWidthTf.setEditable(false);
	    rectWidthTf.setText("");
	    rectHeightTf.setEditable(false);
	    rectHeightTf.setText("");
	    circColTf.setEditable(false);
	    circColTf.setText("");
	    circRowTf.setEditable(false);
	    circRowTf.setText("");
	    circRadTf.setEditable(false);
	    circRadTf.setText("");
	    maskBrowseButton.setEnabled(false);
            roiMaskAbsName = "";
            roiMaskNameLabel.setText("");
	    mouseRectROI.setEnabled(false);
	    cLenCalcCombo.setSelectedItem("near_opt");
	    ctermCombo.setSelectedItem("near_opt");
	    cSegSymbBox.setSelected(false);
	    cCausalBox.setSelected(false);
	    cRegTermBox.setSelected(false);
	    cResetMQBox.setSelected(false);
	    cBypassBox.setSelected(false);
	    cblkSizWidthCb.setSelectedItem("64");
	    cblkSizHeightCb.setSelectedItem("64");
	    aLayersTf.setText("0.015 +20 2.0 +10");
	    apTypeCombo.setSelectedItem("Layer");
	    pEphBox.setSelected(false);
	    pSopBox.setSelected(false);
	    mctCheckBox.setSelected(false);
	    mctOn.setEnabled(false);
	    mctOff.setEnabled(false);
	    mctOff.setSelected(true);
	    cppCheckBox.setSelected(false);
	    cppDimTf.setText("");
	    cppDimTf.setEditable(false);
        } else if(o==refxTF) { // Image horizontal offset in canvas
	    imgOff.x = (new Integer(refxTF.getText())).intValue();
	    if(imgOff.x<0) {
		imgOff.x = 0;
		refxTF.setText("0");
	    }
	    imgPan.setOffset(imgOff.x,imgOff.y);
	} else if(o==refyTF) { // Image vertical offset in canvas
	    imgOff.y = (new Integer(refyTF.getText())).intValue();
	    if(imgOff.y<0) {
		imgOff.y = 0;
		refyTF.setText("0");
	    }
	    imgPan.setOffset(imgOff.x,imgOff.y);
	} else if(o==secNoShape) {
	    secUlxTF.setEditable(false);
	    secUlyTF.setEditable(false);
	    secWTF.setEditable(false);
	    secHTF.setEditable(false);
// 	    secMouseDef.setEnabled(false);
	} else if(o==secWholeImage) {
	    secUlxTF.setEditable(false);
	    secUlyTF.setEditable(false);
	    secWTF.setEditable(false);
	    secHTF.setEditable(false);
// 	    secMouseDef.setEnabled(false);
	} else if(o==secWavMeth) {
	    secBrStartTF.setEditable(false);
	} else if(o==secBitMeth) {
	    secBrStartTF.setEditable(true);
	}
    }

    public void itemStateChanged(ItemEvent e) {
        Object o = e.getSource();

        if(o==mctCheckBox) {
	    if(mctCheckBox.isSelected()){
		mctOn.setEnabled(true);
		mctOff.setEnabled(true);	
	    } else {
		mctOn.setEnabled(false);
		mctOff.setEnabled(false);
	    }
        } else if (o==cppCheckBox) {
	    if(cppCheckBox.isSelected()){
		cppDimTf.setText("");
		cppDimTf.setEditable(true);
	    } else {
		cppDimTf.setText("");
		cppDimTf.setEditable(false);	
	    }            
        } else if(o==lossy) { // lossy mode (advanced options)
	    encRateField.setEditable(true);
            encRateField.setText(f.format((encRateSlider.getValue()/
                                           encSliderPrec)));
            qtypeCombo.removeAllItems();
            qtypeCombo.addItem("expounded");
            qtypeCombo.addItem("derived");
	    qtypeCombo.setSelectedItem("expounded");
            qstepTf.setEditable(true);
        } else if(o==lossless) { // lossless coding mode (advanced options)
	    encRateField.setEditable(false);
	    encRateField.setText("");
            qtypeCombo.removeAllItems();
            qtypeCombo.addItem("reversible");
	    qtypeCombo.setSelectedItem("reversible");
            qstepTf.setEditable(false);
	} else if(o==tileBox) { // Enable/disable nominal tiles dimension
            // selection
	    if(tileBox.isSelected()) {
		tileWidth.setEditable(true);
		tileHeight.setEditable(true);
		trefxTF.setEditable(true);
		trefyTF.setEditable(true);
	    } else {
		tileWidth.setEditable(false);
		tileHeight.setEditable(false);
		trefxTF.setEditable(false);
		trefyTF.setEditable(false);
	    }
	} else if(o==lossyBut) {
            encRateField.setEditable(true);
            encRateSlider.setEnabled(true);
        } else if(o==losslessBut) {
            encRateField.setEditable(false);
            encRateSlider.setEnabled(false);            
        } else if(o==lossy) {
            encRateField.setEditable(true);
        } else if(o==lossless) {
            encRateField.setEditable(false);
        } else if(o==mouseImgOff) {
	    if(mouseImgOff.isSelected()) {
		mouseRectROI.setSelected(false);
		mouseTile.setSelected(false);
		mouseCircROI.setSelected(false);
		selectType = MOVE_IMAGE;
	    } else {
		selectType = NONE;
	    }
	} else if(o==mouseTile) {
	    if(mouseTile.isSelected()) {
		mouseRectROI.setSelected(false);
		mouseImgOff.setSelected(false);
		mouseCircROI.setSelected(false);
		selectType = TILE_PARTITION;
		imgPan.enableDrawing(true);
 	    } else {
		selectType = NONE;
		imgPan.enableDrawing(false);
 	    }
	} else if(o==mouseRectROI) {
            if(mouseRectROI.isSelected()) {
                selectType = RECT_ROI;
                imgPan.enableDrawing(true);
            } else {
                selectType = NONE;
                imgPan.enableDrawing(false);
            }
        } else if(o==mouseCircROI) {
            if(mouseCircROI.isSelected()) {
                selectType = CIRC_ROI;  
                imgPan.enableDrawing(true);
            } else {
                selectType = NONE;
                imgPan.enableDrawing(false);
            }
        } else if(o==secUserDef) {
	    if(secUserDef.isSelected()) {
		selectType = RECT_SECURE;
		imgPan.enableDrawing(true);
	    } else {
		selectType = NONE;
		imgPan.enableDrawing(false);
	    }
	}
    }

    /** 
     * Method handling modification of the sliders values 
     * */
    public void stateChanged(ChangeEvent e) {
        double val = (double)encRateSlider.getValue()/encSliderPrec;
        encRateField.setText(f.format(val));
    }

    /** Handles mouse event happening on the opened image when selecting a
     * Region of Interest */
    public void mouseClicked(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { 
	switch(selectType) {
	case MOVE_IMAGE:
	    imgPan.setCursor(new Cursor(Cursor.MOVE_CURSOR));
	    break;
	case TILE_PARTITION:
	case RECT_ROI:
	case RECT_SECURE:
	case CIRC_ROI:
            imgPan.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	    break;
	}
    }
    public void mouseExited(MouseEvent e) { 
	switch(selectType) {
	case TILE_PARTITION:
	case MOVE_IMAGE:
	case RECT_ROI:
	case RECT_SECURE:
	case CIRC_ROI:
            imgPan.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    break;
	}
    }
    public void mousePressed(MouseEvent e) { 
        selectOrig.x = e.getX();
        selectOrig.y = e.getY();
	switch(selectType) {
	case TILE_PARTITION:
	    if(selectOrig.x>imgPan.getOffX()/zf) {
		selectOrig.x = (int)((imgPan.getOffX())/zf);
	    } 
	    if(selectOrig.y>imgPan.getOffY()/zf) {
		selectOrig.y = (int)((imgPan.getOffY())/zf);
	    } 
            rectSelect = new Rectangle(selectOrig.x,selectOrig.y,1,1);
	    trefxTF.setText(""+(int)(rectSelect.x*zf));
	    trefyTF.setText(""+(int)(rectSelect.y*zf));
	    tileWidth.setText(""+rectSelect.width);
	    tileHeight.setText(""+rectSelect.height);
	    break;
	case MOVE_IMAGE:
	    savedImgOff.x = imgOff.x;
	    savedImgOff.y = imgOff.y;
	    break;
	case RECT_ROI:
            rectSelect = new Rectangle(selectOrig.x,selectOrig.y,1,1);
            rectLeftTf.setText(""+(int)(rectSelect.x*zf));
            rectTopTf.setText(""+(int)(rectSelect.y*zf));
            rectWidthTf.setText(""+rectSelect.width);
            rectHeightTf.setText(""+rectSelect.height);
	    break;
	case RECT_SECURE:
            rectSelect = new Rectangle(selectOrig.x,selectOrig.y,1,1);
            secUlxTF.setText(""+(int)(rectSelect.x*zf));
            secUlyTF.setText(""+(int)(rectSelect.y*zf));
            secWTF.setText(""+rectSelect.width);
            secHTF.setText(""+rectSelect.height);
	    break;
        case CIRC_ROI:
            cROIx = selectOrig.x; cROIy = selectOrig.y; cROIrad = 1;
            circRowTf.setText(""+(int)(cROIy*zf));
            circColTf.setText(""+(int)(cROIx*zf));
            circRadTf.setText(""+cROIrad);
	    break;
        }
    }
    public void mouseReleased(MouseEvent e) { 
	switch(selectType) {
	case TILE_PARTITION:
	    imgPan.setTilePartition(rectSelect);
	    break;
	}
    }
    public void mouseDragged(MouseEvent e) { 
        switch(selectType) {
	case MOVE_IMAGE: {
            int dx = e.getX()-selectOrig.x;
            int dy = e.getY()-selectOrig.y;

	    if(savedImgOff.x+dx<0) {
		imgOff.x = 0;
	    } else {
		imgOff.x = savedImgOff.x+dx;
	    }
	    if(savedImgOff.y+dy<0) {
		imgOff.y = 0;
	    } else {
		imgOff.y = savedImgOff.y+dy;
	    }
	    imgPan.setOffset(imgOff.x,imgOff.y);
	    refxTF.setText(""+imgOff.x);
	    refyTF.setText(""+imgOff.y);
	    
	    break; }
	case TILE_PARTITION: {
	    int x0 = rectSelect.x;
            int y0 = rectSelect.y;

            int nx = e.getX();
            int ny = e.getY();
	    int offx = imgPan.getOffX();
	    int offy = imgPan.getOffY();
            int oWidth = (int)((imgPan.getOrigWidth()+offx)/zf);
            int oHeight = (int)((imgPan.getOrigHeight()+offy)/zf);
            if(nx<0) nx = 0;
            if(ny<0) ny = 0;
            if(nx>=oWidth) nx = oWidth-1;
            if(ny>=oHeight) ny = oHeight-1;

            int w = nx-selectOrig.x+1;
            int h = ny-selectOrig.y+1;
            if(w<0) {
                w = -w;
                x0 = nx;
                trefxTF.setText(""+(int)(x0*zf));
                rectSelect.x = x0;
            } 
            if(h<0) {
                h = -h;
                y0 = ny;
                trefyTF.setText(""+(int)(y0*zf));
                rectSelect.y = y0;
            }

            rectSelect.width = w; rectSelect.height = h;
            tileHeight.setText(""+(int)(h*zf));
            tileWidth.setText(""+(int)(w*zf));
            imgPan.drawRect(x0,y0,w,h);
	    break;}
	case RECT_SECURE: {
            int x0 = rectSelect.x;
            int y0 = rectSelect.y;

            int nx = e.getX();
            int ny = e.getY();
	    int offx = imgPan.getOffX();
	    int offy = imgPan.getOffY();
            int oWidth = (int)((imgPan.getOrigWidth()+offx)/zf);
            int oHeight = (int)((imgPan.getOrigHeight()+offy)/zf);
            if(nx<0) nx = 0;
            if(ny<0) ny = 0;
            if(nx>=oWidth) nx = oWidth-1;
            if(ny>=oHeight) ny = oHeight-1;

            int w = nx-selectOrig.x+1;
            int h = ny-selectOrig.y+1;
            if(w<0) {
                w = -w;
                x0 = nx;
                secUlxTF.setText(""+(int)(x0*zf));
                rectSelect.x = x0;
            } 
            if(h<0) {
                h = -h;
                y0 = ny;
                secUlyTF.setText(""+(int)(y0*zf));
                rectSelect.y = y0;
            }

            rectSelect.width = w; rectSelect.height = h;
            secHTF.setText(""+(int)(h*zf));
            secWTF.setText(""+(int)(w*zf));
            imgPan.drawRect(x0,y0,w,h);
	    break; }
	case RECT_ROI: {
            int x0 = rectSelect.x;
            int y0 = rectSelect.y;

            int nx = e.getX();
            int ny = e.getY();
	    int offx = imgPan.getOffX();
	    int offy = imgPan.getOffY();
            int oWidth = (int)((imgPan.getOrigWidth()+offx)/zf);
            int oHeight = (int)((imgPan.getOrigHeight()+offy)/zf);
            if(nx<0) nx = 0;
            if(ny<0) ny = 0;
            if(nx>=oWidth) nx = oWidth-1;
            if(ny>=oHeight) ny = oHeight-1;

            int w = nx-selectOrig.x+1;
            int h = ny-selectOrig.y+1;
            if(w<0) {
                w = -w;
                x0 = nx;
                rectLeftTf.setText(""+(int)(x0*zf));
                rectSelect.x = x0;
            } 
            if(h<0) {
                h = -h;
                y0 = ny;
                rectTopTf.setText(""+(int)(y0*zf));
                rectSelect.y = y0;
            }

            rectSelect.width = w; rectSelect.height = h;
            rectHeightTf.setText(""+(int)(h*zf));
            rectWidthTf.setText(""+(int)(w*zf));
            imgPan.drawRect(x0,y0,w,h);
	    break; }
        case CIRC_ROI: {
            int x0 = cROIx;
            int y0 = cROIy;

            int nx = e.getX();
            int ny = e.getY();
	    int offx = imgPan.getOffX();
	    int offy = imgPan.getOffY();
            int oWidth = (int)((imgPan.getOrigWidth()+offx)/zf);
            int oHeight = (int)((imgPan.getOrigHeight()+offy)/zf);

            int w = nx-x0;
            int h = ny-y0;
            int radius = (int)Math.sqrt(w*w+h*h);
            int radiusS2 = (int)Math.sqrt(2*w*w+2*h*h);
            if(x0-radiusS2<0) radiusS2 = x0;
            if(y0-radiusS2<0) radiusS2 = y0;
            if(x0+radiusS2>=oWidth) radiusS2 = oWidth-1-x0;
            if(y0+radiusS2>=oHeight) radiusS2 = oHeight-1-y0;

            cROIrad = radiusS2;
            circRadTf.setText(""+(int)(radiusS2*zf));
            imgPan.drawOval(x0-radiusS2,y0-radiusS2,2*radiusS2,2*radiusS2);
	    break; }
        }
    }
    public void mouseMoved(MouseEvent e) { }

    public void setSelectType(int type) {
        selectType = type;
        imgPan.enableDrawing(false);
    }
}
