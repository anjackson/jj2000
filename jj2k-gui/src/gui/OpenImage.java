package gui;

import javax.swing.event.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.awt.*;
import java.io.*;

import jj2000.j2k.quantization.dequantizer.*;
import jj2000.j2k.image.invcomptransf.*;
import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.fileformat.reader.*;
import jj2000.j2k.codestream.reader.*;
import jj2000.j2k.entropy.decoder.*;
import jj2000.j2k.image.input.*;
import jj2000.j2k.codestream.*;
import jj2000.j2k.decoder.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.io.*;
import jj2000.disp.*;

import colorspace.*;

import com.sun.image.codec.jpeg.*;

/** Class to open and display all supported image types */
public class OpenImage 
    implements Runnable, Markers, ActionListener, ThreadSurvey {

    /** Reference to the temporary raw output file */
    private File tmpDecFile = null;

    /** Reference to the decoder instance (for JPEG 2000 files) */
    private Decoder dec = null;

    /** Format float for display (3 fractionnal digits) */
    private static final DecimalFormat df = new DecimalFormat("##0.000");
    /** Initial directory to open images */
    private static File curDir = new File(System.getProperty("user.home"));

    /** Reference to the main frame */
    private Main mainFrame;
    /** Reference to the main frame's desktop */
    private JDesktopPane desktop = null;
    /** Reference to the J2KGuiDecoder instance */
    private J2KGuiDecoder j2kdecoder;

    /** Image created from the file */
    private Image image = null;
    /** Source file used to create the image */
    private File inFile = null;
    /** File with decoded data (PPM or PPM) */
    private File rawFile = null;

    /** Number of components in the codestream (when it's needed) */
    private int ncCod = 1;
    /** Number of components in the decoded image */
    private int ncImg = 1;
    /** Bit-depth of each component in the image */
    private int[] depth =  {8};
    /** Whether or not pgx output file is needed (i.e. one component
     * at least has a bit-depth greater than 8) */
    private boolean pgxNeeded = false;
    /** Input image width */
    private int width = 0;
    /** Input image height */
    private int height = 0;
    /** Input image type */
    private int fileType;
    /** Bit-rate of the source file */
    private double rate;

    /** Raw images file filter description */
    private final static String rawImgDesc = "Raw images .pgm, .ppm, .pgx";
    /** Raw images files supported extensions */
    private final static String[] rawext = { "pgm","ppm", "pgx" };
    /** JPEG 2000 file filter description */
    private final static String jpeg2kDesc = "JPEG 2000 images .j2k .jpc .jp2";
    /** JPEG 2000 files supported extensions */
    private final static String[] j2kext = { "j2k", "jpc", "jp2"};
    /** JPEG files filter description */
    private final static String jpgDesc = "JPEG images .jpg";
    /** JPEG images supported extensions */
    private final static String[] jpgext = {"jpg"};

    /** Input image is JPEG */
    public static final int FILE_TYPE_JPEG = 1;
    /** Input image is PGM */
    public static final int FILE_TYPE_PGM = 2;
    /** Input image is PPM */
    public static final int FILE_TYPE_PPM = 3;
    /** Input image is PGX */
    public static final int FILE_TYPE_PGX = 4;
    /** Input image is JPEG 2000 */
    public static final int FILE_TYPE_JPEG2000 = 5;
    /** Input image type is unsupported */
    public static final int FILE_TYPE_UNKNOWN = 6;

    /** Whether or not a file has been selected for opening. This allows
     * dealing with cases where the Cancel button is pressed. */
    private boolean fileSelected = false;

    /** 
     * Class constructor. This is used when input file is not provided
     * from a JFileChooser */
    public OpenImage(Main mainFrame, JDesktopPane desktop, 
		     String fileName) {
        this.mainFrame = mainFrame;
        this.desktop = desktop;
	this.inFile = new File(fileName);
	if(inFile.isDirectory() || !inFile.exists() || 
	   inFile.length()<=0) {
	    return;
	}
	fileSelected = true;
	fileType = determineFileType();
    }
    
    /** 
     * Class constructor. Creates a JFileChooser in order to select the input
     * file and determine the file type. 
     * */
    public OpenImage(Main mainFrame, JDesktopPane desktop) {
        this.mainFrame = mainFrame;
        this.desktop = desktop;

        JFileChooser fcOpen = new JFileChooser(curDir);
        fcOpen.addChoosableFileFilter(new JJFileFilter(rawImgDesc,rawext));
        fcOpen.addChoosableFileFilter(new JJFileFilter(jpgDesc,jpgext));
        fcOpen.addChoosableFileFilter(new JJFileFilter(jpeg2kDesc,j2kext));
        
        if(fcOpen.showDialog(desktop,"Open")==JFileChooser.APPROVE_OPTION) {
            inFile = fcOpen.getSelectedFile();
                
            // Check that this is a "real" file
            if(inFile.isDirectory() || !inFile.exists() || 
               inFile.length()<=0) {
                return;
            }

            // Save current directory for next opening operation
            curDir = fcOpen.getCurrentDirectory();
        } else {
            return;
        }
        fileSelected = true;
        fileType = determineFileType();
    }

    /** Whether or not a file has been selected for opening */
    public boolean isFileSelected() {
        return fileSelected;
    }

    /** 
     * Determine the input file type by reading and analizing its first 12
     * bytes. In the case of JPEG 2000 codestream, it also retrieves the image
     * dimension and verify that the bit-depth and the number of components is
     * are supported by the current display module. 
     * */
    private int determineFileType() {
        // Read the first twelve bytes of the bit stream
        byte[] b = new byte[12];
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(inFile));
            bis.read(b,0,12);
            bis.close();
        } catch(IOException ioe) { 
            return FILE_TYPE_UNKNOWN; 
        }

        if(b[0]==(byte)0xff && b[1]==(byte)0xd8 && b[2]==(byte)0xff) { // JPEG
            return FILE_TYPE_JPEG;
        } else if(b[0]=='P' && b[1]=='5') { // PGM file
            return FILE_TYPE_PGM;
        } else if(b[0]=='P' && b[1]=='6') { // PPM file
            return FILE_TYPE_PPM;
        } else if(b[0]=='P' && b[1]=='G') { // PGX file
            return FILE_TYPE_PGX;
        } else if((b[0]==(byte)0xFF && b[1]==(byte)0x4F) ||
		  (b[0]==(byte)0x00 && b[1]==(byte)0x00 &&
		   b[2]==(byte)0x00 && b[3]==(byte)0x0c &&
		   b[4]==(byte)0x6a && b[5]==(byte)0x50 &&
		   b[6]==(byte)0x20 && b[7]==(byte)0x20 &&
		   b[8]==(byte)0x0d && b[9]==(byte)0x0a &&
		   b[10]==(byte)0x87 && b[11]==(byte)0x0a) ) { // J2K image

	    // Initialize default parameters
	    ParameterList defpl = new ParameterList();
	    String[][] param = Decoder.getAllParameters();
	    
	    for (int i=param.length-1; i>=0; i--) {
		if(param[i][3]!=null)
		    defpl.put(param[i][0],param[i][3]);
	    }
	    // Create parameter list using defaults
	    ParameterList pl = new ParameterList(defpl);

	    // Open RandomAccessIO to read JPEG 2000 image information
	    RandomAccessIO in = null;
	    FileFormatReader ff = null;
	    HeaderDecoder hd = null;
	    BlkImgDataSrc palettized, channels, resampled, color;
	    ColorSpace csMap;
	    try {
		in = new BEBufferedRandomAccessFile(inFile,"r");

		// File format reader (if needed)
		ff = new FileFormatReader(in);
		ff.readFileFormat();
		if(ff.JP2FFUsed) {
		    in.seek(ff.getFirstCodeStreamPos());
		}
		// Header decoder
		HeaderInfo hi = new HeaderInfo();
		hd = new HeaderDecoder(in,pl,hi);
		if(ff.JP2FFUsed) {
		    // Bit stream reader
		    DecoderSpecs decSpec = hd.getDecoderSpecs();
		    BitstreamReaderAgent breader = BitstreamReaderAgent.
			createInstance(in,hd,pl,decSpec,
				       pl.getBooleanParameter("cdstr_info"),
				       hi);
		    // Entropy decoder
		    EntropyDecoder entdec = hd.
			createEntropyDecoder(breader,pl);
		    // Dequantizer
		    ncCod = hd.getNumComps();
		    depth = new int[ncCod];
		    for(int i=0; i<ncCod;i++) { 
			depth[i] = hd.getOriginalBitDepth(i); 
		    }
		    Dequantizer deq = hd.
			createDequantizer(entdec,depth,decSpec);
		    // Inverse wavelet 
		    InverseWT invWT = InverseWT.createInstance(deq,decSpec);
		    // Converter
		    ImgDataConverter converter = new ImgDataConverter(invWT,0);
		    // Inverse component transform
		    InvCompTransf ictransf = 
			new InvCompTransf(converter,decSpec,depth,pl);
		    // Colorspace mapping
		    csMap = new ColorSpace(in,hd,pl);
		    channels = hd.
			createChannelDefinitionMapper(ictransf,csMap);
		    resampled  = hd.createResampler(channels,csMap);
		    palettized = hd.
			createPalettizedColorSpaceMapper(resampled,csMap);
		    color = hd.createColorSpaceMapper(palettized,csMap);
		    int res = breader.getImgRes();
		    invWT.setImgResLevel(res);
		    if(color!=null) {
			ncImg = color.getNumComps();
			width = color.getImgWidth();
			height = color.getImgHeight();
			if(ncImg!=ncCod) {
			    depth = new int[ncImg];
			    for(int c=0; c<ncImg; c++) {
				depth[c] = color.getNomRangeBits(c);
			    }
			}
		    } else {
			ncImg = ncCod;
			width = hd.getImgWidth();
			height = hd.getImgHeight();
		    }
		} else {
 		    ncImg = hd.getNumComps();
		    depth = new int[ncImg];
		    for(int c=0; c<ncImg; c++) {
			depth[c] = hd.getOriginalBitDepth(c);
		    }
		    width = hd.getImgWidth();
		    height = hd.getImgHeight();
		}
		in.close();
	    } catch(Exception e) {
                JOptionPane.showMessageDialog(desktop,"Unable to read info "+
					      "from "+
					      "file "+inFile.getName()+":\n"+
					      e.getMessage()+"\n",
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
                try { bis.close(); } catch(IOException ioe2) {}
                fileSelected = false;
                return FILE_TYPE_UNKNOWN;
	    } 
            
            // Check if found codestream will be supported
            if(ncCod!=1 && ncCod!=3) {
                JOptionPane.showMessageDialog(desktop,"Codestream with "+ncCod+
                                              " components not supported.",
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
                fileSelected = false;
                return FILE_TYPE_UNKNOWN;
            }
            return FILE_TYPE_JPEG2000;
        } else {
            return FILE_TYPE_UNKNOWN;
        }
    }

    /** Start image opening (and decoding if needed) operation(s) */
    public void run() {
        // +---------------------+
        // | Create Image object |
        // +---------------------+
        ImgReader imr;
	mainFrame.saveOpenedFile(inFile);
        switch(fileType) {
        case FILE_TYPE_JPEG:
            openJPEG(inFile);
	    displayImage(null,true);
            break;
        case FILE_TYPE_PGM:
            openPGM(inFile);
            rawFile = inFile;
	    displayImage(null,false);
            break;
        case FILE_TYPE_PPM:
            openPPM(inFile);
            rawFile = inFile;
	    displayImage(null,false);
            break;
        case FILE_TYPE_PGX:
            openPGX(inFile);
            rawFile = inFile;
	    displayImage(null,false);
            break;
        case FILE_TYPE_JPEG2000:
            j2kdecoder = new J2KGuiDecoder(mainFrame,desktop,this,inFile,width,
                                           height);
            j2kdecoder.start();
	    displayImage(null,true);
            return;
        case FILE_TYPE_UNKNOWN:
        default:
            // +--------------------+
            // | Unknown file type  |
            // +--------------------+
             JOptionPane.showMessageDialog(desktop,inFile.getPath(),
                                          "Unrecognized image format",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    /** 
     * Display the Image instance in a panel (JJImgPanel). Then it inserts
     * the panel in an internal frame 
     * */
    private void displayImage(String info, boolean isTemporaryFile) {
        if(image==null) return; // No image has been created

        // +---------------------------------+
        // | Display Image in internal frame |
        // +---------------------------------+
        mainFrame.setCursor(Cursor.WAIT_CURSOR);
        JInternalFrame f = new JInternalFrame("",true,true,true,true);

        // Set preferred size for using scroll-bars appropriately
        JJImgPanel imgPanel = 
	    new JJImgPanel(image,new Rectangle(0,0,width,height),ncImg,
			   mainFrame);

        // Scroll bars, rules and corners
        JScrollPane imgScrollPane = new JScrollPane(imgPanel);
        f.getContentPane().add(imgScrollPane);  
        f.pack();

        // Display the decoding rate in the title
	Rectangle rect = new Rectangle(0,0,width,height);
	FrameAttribute fA = new FrameAttribute(ncImg,rawFile,rect,imgPanel,
					       inFile.getName()+" @ "+
					       df.format(rate)+" bpp",info,
					       imgScrollPane,isTemporaryFile);
        mainFrame.addInternalFrame(f,fA);
        mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
        return;
    }

    /** Opens three PGX files and display them as a RGB image */
    private void openThreePGX(File in) {
	mainFrame.setCursor(Cursor.WAIT_CURSOR);

	// Retrieve file names prefix
	String prefix = in.getPath().
	    substring(0,in.getPath().lastIndexOf('.'));
	
	// Image readers
	ImgReaderPGX[] imr = new ImgReaderPGX[3];
	ImgDataJoiner joiner;
	try {
	    imr[0] = new ImgReaderPGX(prefix+"-1.pgx");
	    imr[1] = new ImgReaderPGX(prefix+"-2.pgx");
	    imr[2] = new ImgReaderPGX(prefix+"-3.pgx");
	    BlkImgDataSrc[] src = new BlkImgDataSrc[3];
	    src[0] = new RescaleImgReader(imr[0]); 
	    src[1] = new RescaleImgReader(imr[1]);
	    src[2] = new RescaleImgReader(imr[2]);
	    int[] cc = new int[3];
	    cc[0] = cc[1] = cc[2] = 0;
	    joiner = new ImgDataJoiner(src,cc);
	    width = imr[0].getImgWidth();
	    height = imr[0].getImgHeight();
	} catch(IOException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot open specified images: "+
                                          prefix+"-{0,1,2}.pgx","Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        // Image producer
        try {
            image = BlkImgDataSrcImageProducer.createImage(joiner);
        } catch(IllegalArgumentException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot create image producer",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        rate = (inFile.length()*8f)/(width*height);
    }

    /** Opens the specified PGX file and creates the Image instance */
    private void openPGX(File in) {
        mainFrame.setCursor(Cursor.WAIT_CURSOR);
        
        // Image reader
        ImgReaderPGX imr;
        try {
            imr = new ImgReaderPGX(in.getPath());
            width = imr.getImgWidth();
            height = imr.getImgHeight();
        } catch(IOException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot open specified image: "+
                                          in.getPath(),"Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        // Image producer
        try {
            image = BlkImgDataSrcImageProducer.
		createImage(new RescaleImgReader(imr));
        } catch(IllegalArgumentException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot create image producer",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        rate = inFile.length()*8f/width/height;
    }

    /** Open the PPM file and creates the Image instance */
    private void openPPM(File in) {
        mainFrame.setCursor(Cursor.WAIT_CURSOR);
        
        // Image reader
        ImgReaderPPM imr;
        try {
            imr = new ImgReaderPPM(in.getPath());
            width = imr.getImgWidth();
            height = imr.getImgHeight();
            ncImg = 3;
	    depth = new int[3];
	    depth[0] = depth[1] = depth[2] = 8;
        } catch(IOException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot open specified image: "+
                                          in.getPath(),"Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        // Image producer
        try {
            image = BlkImgDataSrcImageProducer.createImage(imr);
        } catch(IllegalArgumentException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot create image reader",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        rawFile = inFile;
        rate = inFile.length()*8f/width/height;
    }

    /** Open the PGM file and creates the Image instance */
    private void openPGM(File in) {
        mainFrame.setCursor(Cursor.WAIT_CURSOR);
        
        // Image reader
        ImgReaderPGM imr;
        try {
            imr = new ImgReaderPGM(in.getPath());
            width = imr.getImgWidth();
            height = imr.getImgHeight();
        } catch(IOException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot open specified image: "+
                                          in.getPath(),"Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        // Image producer
        try {
            image = BlkImgDataSrcImageProducer.createImage(imr);
        } catch(IllegalArgumentException e) {
            JOptionPane.showMessageDialog(desktop,
                                          "Cannot create image reader",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        rawFile = inFile;
        rate = inFile.length()*8f/width/height;
    }

    /** Open the JPEG file, creates the Image instance and the rawFile */
    private void openJPEG(File in) {
        mainFrame.setCursor(Cursor.WAIT_CURSOR);
        
        JPEGImageDecoder decoder = null;
        try {
            // Decode the JPEG file and create the BufferedImage instance
            decoder = JPEGCodec.createJPEGDecoder(new FileInputStream(in));
            image = decoder.decodeAsBufferedImage();
            Raster raster = ((BufferedImage)image).getData();
            ncImg = raster.getNumBands();
            height = raster.getHeight();
            width = raster.getWidth();
            
            // Save decoded image in a temporary PGM or PPM file
            if(ncImg==1) { // PGM file
                String header = "P5\n"+width+" "+height+"\n255\n";
                
                rawFile = File.createTempFile("decoded",".pgm");
                BufferedOutputStream bos = 
                    new BufferedOutputStream(new FileOutputStream(rawFile));
                bos.write(header.getBytes());
                for(int i=0; i<height; i++) {
                    for(int j=0; j<width; j++) {
                        bos.write(raster.getSample(j,i,0));
                    }
                }
                bos.close();
            } else if(ncImg==3) { // PPM file
                String header = "P6\n"+width+" "+height+"\n255\n";
                
                rawFile = File.createTempFile("decoded",".ppm");
                BufferedOutputStream bos = 
                    new BufferedOutputStream(new FileOutputStream(rawFile));
                bos.write(header.getBytes());
                for(int i=0; i<height; i++) {
                    for(int j=0; j<width; j++) {
                        bos.write(raster.getSample(j,i,0));
                        bos.write(raster.getSample(j,i,1));
                        bos.write(raster.getSample(j,i,2));
                    }
                }
                bos.close();
            } else { // Not supported number of components
                JOptionPane.showMessageDialog(desktop,"Error",
                                              "Not supported JPEG format"+
                                              inFile.getPath(),
                                              JOptionPane.ERROR_MESSAGE);
                mainFrame.setCursor(Cursor.DEFAULT_CURSOR);
                return;
            }
            
        } catch(IOException e) { // Error when decoding JPEG file
            JOptionPane.showMessageDialog(desktop,inFile.getPath(),
                                          "Unable to open JPEG file: "+
                                          inFile.getPath(),
                                          JOptionPane.ERROR_MESSAGE);
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);                
            return;
        }
        rate = inFile.length()*8f/width/height;
    }

    /** 
     * Open the JPEG 2000 file/codestream, creates the Image instance and the
     * rawFile 
     * */
    private void openJPEG2000(ParameterList pl) {
        // Input file
        pl.put("i",inFile.getPath());
	
        // Ouput file
        tmpDecFile = null;
	for(int c=0; c<ncImg; c++) {
	    if(depth[c]>8) {
		pgxNeeded = true;
	    }
	}
        try {
	    if(pgxNeeded) {
		tmpDecFile = File.createTempFile("decoded", ".pgx");
	    } else if(ncImg==3) {
                tmpDecFile = File.createTempFile("decoded",".ppm");
            } else {
                tmpDecFile = File.createTempFile("decoded",".pgm");
            } 
        } catch(IOException ioe) {
            JOptionPane.showMessageDialog(null,inFile.getPath(),
                                          "Cannot decode the codestream",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
	tmpDecFile.delete();
	tmpDecFile.deleteOnExit();
        pl.put("o",tmpDecFile.getPath());
	
	// Register the FacilityManager (Dialogs)
        JJPopup mypop = new JJPopup(desktop);
	FacilityManager.registerMsgLogger(null,mypop);
	FacilityManager.registerProgressWatch(null,mainFrame);
	
	// Instantiate decoder
        dec = new Decoder(pl);
        if(dec.getExitCode() != 0) { // An error ocurred
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.ERROR,"An error ocurred "+
			 "while instantiating the decoder.");
            FacilityManager.getMsgLogger().flush();
            return;
        }
	
        // Run the decoder
        dec.setChildProcess(true);
	Thread thdec = new Thread(dec);
	
	// Watch decoder termination
	ThreadWatch tw = new ThreadWatch(this,thdec);
	Thread thWatch = new Thread(tw);
	thWatch.start();
    }
    
    /** Display the decoded image once the decoding is over */
    public void terminatedThread() {
        // Create the image
        if(ncImg==1) {
	    if(pgxNeeded) {
		openPGX(tmpDecFile);
	    } else {
		openPGM(tmpDecFile);
	    }
        } else if(ncImg==3) {
	    if(pgxNeeded) {
		openThreePGX(tmpDecFile);
	    } else {
		openPPM(tmpDecFile);
	    }
        }
        rawFile = tmpDecFile;
        rate = j2kdecoder.getRate();
        
        // Display image
        String[] info = dec.getCOMInfo();
        for(int i=1; i<info.length; i++) {
            info[0] += " - "+info[i];
        }
	if(info.length>0) {
	    displayImage(info[0],true);
	} else {
	    displayImage("",true);
	}
    }

    /** 
     * Handles events happening to registered components used to open and
     * display supported images. 
     * */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(j2kdecoder!=null) {
            if(o==j2kdecoder.decOptOkBut) { // Simple JPEG 2000 decoding
                ParameterList pl = j2kdecoder.getSimpParameters();
                openJPEG2000(pl);
            } else if(o==j2kdecoder.decOkBut) { // Advanced JPEG 2000 decoding
                ParameterList pl = j2kdecoder.getAdvParameters();
                openJPEG2000(pl);
            } 
        }
    }

}
