package gui;

import jj2000.j2k.image.input.*;
import jj2000.j2k.encoder.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;

import com.sun.image.codec.jpeg.*;

import javax.swing.event.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.awt.*;
import java.io.*;

/** Class to save displayed image */
public class SaveImage implements Runnable, ActionListener, ThreadSurvey {
    /** Reference to the main frame */
    private Main mainFrame = null;
    /** Reference to the main frame's desktop */
    private JDesktopPane desktop = null;
    /** Reference to the J2KGuiEncoder instance */
    private J2KGuiEncoder j2kencoder = null;

    /** Reference to the encoder instance (for JPEG 2000 encoding) */
    private Encoder enc = null;

    /** Format float for display (3 fractionnal digits) */
    private static final DecimalFormat df = new DecimalFormat("##0.000");

    /** Reference to the JJImgPanel instance where the input image is
     * displayed */
    private JJImgPanel imgPan = null;
    /** Reference to the input file */
    private File inputFile;
    /** Input image dimension */
    private Dimension inDim;

    /** Whether or not a file has been selected for the saving operation */
    private boolean fileSelected = false;

    /** Initial directory to open images */
    private static File curDir = new File("/home/grosbois/");

    /** Selected output file */
    private File outFile;
    /** Type of selected output file */
    private int fileType;

    private JDialog jpgDialog;
    private JSlider jpgQuality;

    /** Raw images file filter description */
    private final static String rawImgDesc = "Raw images .pgm, .ppm";
    /** Raw images files supported extensions */
    private final static String[] rawext = { "pgm","ppm" };
    /** JPEG 2000 file filter description */
    private final static String jpeg2kDesc = "JPEG 2000 images .j2k .jpc .jp2";
    /** JPEG 2000 files supported extensions */
    private final static String[] j2kext = { "j2k", "jpc", "jp2"};
    /** JPEG files filter description */
    private final static String jpgDesc = "JPEG images .jpg";
    /** JPEG images supported extensions */
    private final static String[] jpgext = {"jpg"};

    /** Output image type is unsupported */
    public static final int FILE_TYPE_UNKNOWN = 0;
    /** Output image is JPEG */
    public static final int FILE_TYPE_JPEG = 1;
    /** Output image is PGM */
    public static final int FILE_TYPE_PGM = 2;
    /** Output image is PPM */
    public static final int FILE_TYPE_PPM = 3;
    /** Output image is PGX */
    public static final int FILE_TYPE_PGX = 4;
    /** Output image is JPEG 2000 codestream */
    public static final int FILE_TYPE_J2K_COD = 5;
    /** Output type is JP2 file format */
    public static final int FILE_TYPE_JP2 = 6;

    /** 
     * Class constructor. It opens a JFileChooser in order to select the *
     * output file of the saving operation. Then it determines the output file
     * type. 
     * */
    public SaveImage(Main mainFrame, JDesktopPane desktop,File inputFile,
                     Dimension inDim, JJImgPanel imgPan) {
        this.mainFrame = mainFrame;
        this.desktop = desktop;
        this.inputFile = inputFile;
        this.inDim = inDim;
        this.imgPan = imgPan;
        
        JFileChooser fcSave = new JFileChooser(curDir);
        fcSave.addChoosableFileFilter(new JJFileFilter(rawImgDesc,rawext));
        fcSave.addChoosableFileFilter(new JJFileFilter(jpgDesc,jpgext));
        fcSave.addChoosableFileFilter(new JJFileFilter(jpeg2kDesc,j2kext));
        fcSave.setApproveButtonText("Save");
        
        if(fcSave.showDialog(desktop,"Save")==JFileChooser.APPROVE_OPTION) {
            outFile = fcSave.getSelectedFile();

            // Check that this is a "real" file
            if(outFile.isDirectory()) {
                return;
            }
            
            curDir = fcSave.getCurrentDirectory();
        } else {
            return;
        }
        fileSelected = true;
        fileType = determineFileType(outFile);
    }

    /** Run the saving operation */
    public void run() {
        int iFileType = determineFileType(inputFile);

        switch(fileType) {
        case FILE_TYPE_PGM:
        case FILE_TYPE_PPM:
        case FILE_TYPE_PGX:
            if(iFileType!=fileType) {
                JOptionPane.showMessageDialog(null,
                                              "Invalid output file","Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(copyFile(inputFile,outFile)) {
                mainFrame.renameFrame(outFile);
            }
            break;
        case FILE_TYPE_JP2:
        case FILE_TYPE_J2K_COD:
            j2kencoder = new J2KGuiEncoder(mainFrame,this,inputFile,inDim,
                                           imgPan);
            j2kencoder.start();            
            break;
        case FILE_TYPE_JPEG:
            saveJPEG();
            break;
        default:
            JOptionPane.showMessageDialog(null,
                                          "Unknown output file type","Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Determine the output file type by looking at its extension */
    private int determineFileType(File file) {
        String ext = null;
        String s = file.getPath();
        int i = s.lastIndexOf('.');
        if (i>0 && i<s.length()-1) {
            ext = s.substring(i+1).toLowerCase();
        }
        
        if(ext.equalsIgnoreCase("pgm")) {
            return FILE_TYPE_PGM;
        } else if(ext.equalsIgnoreCase("pgx")) {
            return FILE_TYPE_PGX;
        } else if(ext.equalsIgnoreCase("ppm")) {
            return FILE_TYPE_PPM;
        } else if(ext.equalsIgnoreCase("jp2")) {
            return FILE_TYPE_JP2;
        } else if(ext.equalsIgnoreCase("jpg")) {
            return FILE_TYPE_JPEG;
        } else {
            for(int j=0; j<j2kext.length; j++) {
                if(ext.equalsIgnoreCase(j2kext[j])) {
                    return FILE_TYPE_J2K_COD;
                }
            }
            return FILE_TYPE_UNKNOWN;
        }
    }

    /** Returns the type of the output file */
    public int getFileType() {
        return fileType;
    }

    /** 
     * Whether or not a file has been selected for the saving operation. A
     * file may not have been selected when the Cancel button has been
     * pressed. 
     * */
    public boolean isFileSelected() {
        return fileSelected;
    }

    /** 
     * Copies one specified file to another one and returns whether or not the
     * operation has succedded. 
     * */
    private boolean copyFile(File in, File out) {
        int confirm = JOptionPane.OK_OPTION;
        if(out.exists()) {
            confirm = 
                JOptionPane.showConfirmDialog(null,out+" already exists. Do "+
                                              "you want to overwrite it ?",
                                              "Warning",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.WARNING_MESSAGE);
        }
        
        if(confirm!=JOptionPane.OK_OPTION) {
            return false;
        }

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(in));
            bos = new BufferedOutputStream(new FileOutputStream(out));
            long len = in.length();

            for(int i=0; i<len; i++) {
                bos.write(bis.read());
            }
            bos.close();
            bis.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"Cannot save to file "+out,
                                          "Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
        
    }

    /** 
     * Encode the selected image with the JPEG format. It first ask for a
     * quality factor thanks to a simple dialog box. 
     * */
    private void saveJPEG() {
        jpgQuality = new JSlider(JSlider.HORIZONTAL,0,100,75);
        jpgQuality.setMajorTickSpacing(10);
        jpgQuality.setMinorTickSpacing(1);
        jpgQuality.setPaintTicks(true);
        jpgQuality.setPaintLabels(true);
        jpgQuality.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        jpgQuality.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    jpgDialog.setTitle("JPEG quality factor: "+
                                      jpgQuality.getValue());
                }
            });

        jpgDialog = new JDialog(mainFrame,"JPEG quality factor: "+
                                jpgQuality.getValue());
        JPanel pan = new JPanel();
        pan.add(jpgQuality);
        JButton okBut = new JButton("OK");
        okBut.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jpgDialog.setVisible(false);
                    try {
                        encodeJPEG();
                    } catch(IOException ioe) {
                        JOptionPane.showMessageDialog(mainFrame,
                                                      "Cannot save to JPEG",
                                                      "Error",
                                                      JOptionPane.
                                                      ERROR_MESSAGE);
                    }
                    jpgDialog = null;
                }
            });
        JButton cancelBut = new JButton("Cancel");
        cancelBut.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jpgDialog.setVisible(false);
                    jpgDialog = null;
                }
            });
        pan.add(okBut);
        pan.add(cancelBut);

        jpgDialog.getContentPane().add(pan);
        jpgDialog.pack();
        jpgDialog.show();
    }

    private void encodeJPEG() throws IOException {
        FileOutputStream os;
        JPEGImageEncoder encoder;
        JPEGEncodeParam param;
        BufferedImage img;
        int[] data;
        ImgReader imr;
        int nc = imgPan.getNumComps();
        int width = imgPan.getOrigWidth();
        int height = imgPan.getOrigHeight();

        // Opeb BufferedImage
        img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        if(nc==1) {
            // Use ImgReaderPGM
            imr = new ImgReaderPGM(inputFile.getPath());

            // Get data
            DataBlkInt db = new DataBlkInt();
            db.ulx = db.uly = 0;
            db.w = width; db.h = height;
            db = (DataBlkInt)imr.getInternCompData(db,0);
            data = db.getDataInt();
            
            // Remove DC offset and prepare the RGB data buffer
            for(int i=width*height-1; i>=0; i--) {
                data[i] = 0xff&(data[i]+128);
                data[i] = (data[i]<<16) | (data[i]<<8) | data[i];
            }

        } else {
            // Use ImgReaderPPM
            imr = new ImgReaderPPM(inputFile.getPath());

            // Get data
            DataBlkInt db0 = new DataBlkInt();
            db0.ulx = db0.uly = 0;
            db0.w = width; db0.h = height;
            db0 = (DataBlkInt)imr.getInternCompData(db0,0);

            DataBlkInt db1 = new DataBlkInt();
            db1.ulx = db1.uly = 0;
            db1.w = width; db1.h = height;
            db1 = (DataBlkInt)imr.getInternCompData(db1,1);

            DataBlkInt db2 = new DataBlkInt();
            db2.ulx = db2.uly = 0;
            db2.w = width; db2.h = height;
            db2 = (DataBlkInt)imr.getInternCompData(db2,2);


            data = new int[width*height];
            int[] data0 = db0.getDataInt();
            int[] data1 = db1.getDataInt();
            int[] data2 = db2.getDataInt();
            // Remove DC offset and prepare the RGB data buffer
            for(int i=width*height-1; i>=0; i--) {
                data0[i] = 0xff&(data0[i]+128);
                data1[i] = 0xff&(data1[i]+128);
                data2[i] = 0xff&(data2[i]+128);

                data[i] = (data0[i]<<16) | (data1[i]<<8) | data2[i];
            }
        }

        img.setRGB(0,0,width,height,data,0,width);
        imr.close();

        os = new FileOutputStream(outFile);
        encoder = JPEGCodec.createJPEGEncoder(os);
        param = encoder.getDefaultJPEGEncodeParam(img);
        param.setQuality(jpgQuality.getValue()/100f,true);
        encoder.encode(img,param);
        os.close();

        // Show final bit-rate
        double rate = outFile.length()*8d/width/height;
        JOptionPane.showMessageDialog(mainFrame,"Ecoding bit-rate: "+
                                      df.format(rate),"Info",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    /** 
     * Encode the selected image with the JPEG 2000 format. It first
     * terminates the ParameterList instance initialization and then call the
     * JJ2000 encoder. 
     * */
    private void saveJPEG2000(ParameterList pl) {
        // Input file
        pl.put("i",inputFile.getPath());
        
        // Output file
        pl.put("o",outFile.getPath());
        
        if(fileType==FILE_TYPE_JP2) {
            pl.put("file_format", "on");
        }

        pl.put("debug","on");

	// Register the FacilityManager (Dialogs)
        JJPopup mypop = new JJPopup(desktop);
	FacilityManager.registerMsgLogger(null,mypop);
	FacilityManager.registerProgressWatch(null,mainFrame);
        
	// Instantiate encoder
        enc = new Encoder(pl);
        if (enc.getExitCode() != 0) { // An error ocurred
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.ERROR,"An error ocurred "+
			 "while instantiating the encoder.");
            FacilityManager.getMsgLogger().flush();
            return;
        }

        // Run the encoder
	Thread thenc = new Thread(enc);
	
	// Watch the encoder termination
	ThreadWatch tw = new ThreadWatch(this,thenc);
	Thread thWatch = new Thread(tw);
	thWatch.start();
    }

    /** Displayed encoding bit-rate when encoder is over */
    public void terminatedThread() {
        // Show final bit-rate
        int width = imgPan.getOrigWidth();
        int height = imgPan.getOrigHeight();
        double rate = outFile.length()*8d/width/height;
        JOptionPane.showMessageDialog(mainFrame,"Encoding bit-rate: "+
                                      df.format(rate),"Info",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    /** Handle operations happened on registered components */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if(j2kencoder!=null) {
            if(o==j2kencoder.encOptOkBut) { // Simple JPEG 2000 encoding
                ParameterList pl = j2kencoder.getSimpParameters();
                saveJPEG2000(pl);
                mainFrame.enableZoom(true);
            } else if(o==j2kencoder.encOkBut) { // Advanced JPEG 2000 encoding)
                j2kencoder.setSelectType(J2KGuiEncoder.NONE);
		imgPan.setOffset(0,0);
                ParameterList pl = j2kencoder.getAdvParameters();
                saveJPEG2000(pl);
                mainFrame.enableZoom(true);
            }
        }
    }
}
