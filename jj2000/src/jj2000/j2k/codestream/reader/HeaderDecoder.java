/*
 * CVS identifier:
 *
 * $Id: HeaderDecoder.java,v 1.40 2001/02/26 11:08:23 grosbois Exp $
 *
 * Class:                   HeaderDecoder
 *
 * Description:             Reads main and tile-part headers.
 *
 *
 *
 * COPYRIGHT:
 * 
 * This software module was originally developed by Raphaël Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askelöf (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, Félix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * 
 * Copyright (c) 1999/2000 JJ2000 Partners.
 * */
package jj2000.j2k.codestream.reader;

import jj2000.j2k.quantization.dequantizer.*;
import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.entropy.decoder.*;
import jj2000.j2k.quantization.*;
import jj2000.j2k.codestream.*;
import jj2000.j2k.wavelet.*;
import jj2000.j2k.entropy.*;
import jj2000.j2k.decoder.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.roi.*;
import jj2000.j2k.io.*;
import jj2000.j2k.*;

import java.io.*;
import java.util.*;


/**
 * This class reads Main and Tile-part headers from the codestream. It is
 * created by the run() method of the Decoder instance.
 *
 * <p>A marker segment includes a marker and eventually marker segment
 * parameters. It is designed by the three letters code of the marker
 * associated with the marker segment. JPEG 2000 part 1 defines 6 types of
 * markers:
 *
 * <ul> 
 * <li> Delimiting : SOC,SOT (read in FileBitstreamReaderAgent),SOD,EOC
 * (read in FileBitstreamReaderAgent).</li> <li> Fixed information: SIZ.</li>
 *
 * <li> Functional: COD,COC,RGN,QCD,QCC,POC.</li> <li> In bit-stream:
 * SOP,EPH.</li>
 *
 * <li> Pointer: TLM,PLM,PLT,PPM,PPT.</li>
 *
 * <li> Informational: CRG,COM.</li>
 * </ul>
 *
 * <p>The main header is read when the constructor is called whereas tile-part
 * headers are read when the FileBitstreamReaderAgent instance is created. The
 * reading is done in 2 passes:
 * 
 * <ul> 
 * <li>All marker segments are buffered and their corresponding flag is
 * activated (extractMainMarkSeg and extractTilePartMarkSeg methods).</li>
 *
 * <li>Buffered marker segment are analyzed in a logical way and
 * specifications are stored in appropriate member of DecoderSpecs instance
 * (readFoundMainMarkSeg and readFoundTilePartMarkSeg methods).</li>
 * </ul>
 *
 * <p>Whenever a marker segment is not recognized a warning message is
 * displayed and its length parameter is used to skip it.
 *
 * @see DecoderSpecs
 * @see Decoder
 * @see FileBitstreamReaderAgent
 * */
public class HeaderDecoder implements ProgressionType, Markers,
                                      StdEntropyCoderOptions {

    /** The prefix for header decoder options: 'H' */
    public final static char OPT_PREFIX = 'H';

    /** The list of parameters that is accepted for quantization. Options 
     * for quantization start with 'Q'. */
    private final static String [][] pinfo = null;

    /** Whether to display header information */
    private boolean printHeader = false;

    /** Current header information in a string */
    private String hdStr = "";

    /** Whether to print 'INFO' type info */
    private boolean printInfo;

    /** The ParameterList instance of the decoder */
    private ParameterList pl;

    /** The number of tiles within the image */
    private int nTiles;

    /** The number of tile parts per tile */
    public int[] nTileParts;

    /** Used to store which markers have been already read, by using flag
     * bits. The different markers are marked with XXX_FOUND flags, such as
     * SIZ_FOUND */
    int markersFound = 0;

    /** Counts number of COC markers found in the header */
    private int nCOCmarker=0;

    /** Counts number of QCC markers found in the header */
    private int nQCCmarker=0;

    /** Counts number of COM markers found in the header */
    private int nCOMmarker=0;

    /** Counts number of RGN markers found in the header */
    private int nRGNmarker=0;
    
    /** Counts number of PPM markers found in the header */
    private int nPPMmarker=0;
    
    /** Counts number of PPT markers found in the header */
    private int[] nPPTmarker;
    
    /** Number of read PPT markers found in the header */
    private int nReadPPMmarker=0;
    
    /** Number of read PPT markers found in the header */
    private int[] nReadPPTmarker;
    
    /** Flag bit for SIZ marker segment found */
    private static final int SIZ_FOUND = 1;

    /** Flag bit for COD marker segment found */
    private static final int COD_FOUND = 1<<1;

    /** Flag bit for COC marker segment found */
    private static final int COC_FOUND = 1<<2;

    /** Flag bit for QCD marker segment found */
    private static final int QCD_FOUND = 1<<3;

    /** Flag bit for TLM marker segment found */
    private static final int TLM_FOUND = 1<<4;

    /** Flag bit for PLM marker segment found */
    private static final int PLM_FOUND = 1<<5;

    /** Flag bit for SOT marker segment found */
    private static final int SOT_FOUND = 1<<6;

    /** Flag bit for PLT marker segment found */
    private static final int PLT_FOUND = 1<<7;

    /** Flag bit for QCC marker segment found */
    private static final int QCC_FOUND = 1<<8;

    /** Flag bit for RGN marker segment found */
    private static final int RGN_FOUND = 1<<9;

    /** Flag bit for POC marker segment found */
    private static final int POC_FOUND = 1<<10;

    /** Flag bit for COM marker segment found */
    private static final int COM_FOUND = 1<<11;

    /** Flag bit for SOD marker segment found */
    public static final int SOD_FOUND = 1<<13;

    /** Flag bit for SOD marker segment found */
    public static final int PPM_FOUND = 1<<14;

    /** Flag bit for SOD marker segment found */
    public static final int PPT_FOUND = 1<<15;

    /** Flag bit for CRG marker segment found */
    public static final int CRG_FOUND = 1<<16;

    /** The reset mask for new tiles */
    private static final int TILE_RESET = ~(PLM_FOUND|SIZ_FOUND|RGN_FOUND);

    /** HashTable used to store marker segment byte buffers */
    private Hashtable ht = new Hashtable();

    /** The total header length, including SOC marker segment, header length
     * number, and encoded header length */
    private int thlen;

    /** Denotes the capabilities of the codestream (e.g., error-resilience,
     * ROI, etc.), as found in the SIZ tag. The values are defined in
     * Markers. */
    private int cdstrmCap;

    /** The image width, in the reference grid */
    private int imgW;

    /** The image height, in the reference grid */
    private int imgH;

    /** The horizontal image origin, with respect to the canvas origin, in the 
     * reference grid */
    private int imgOrigX;

    /** The vertical image origin, with respect to the canvas origin, in the 
     * reference grid */
    private int imgOrigY;

    /** The nominal tile width, in the reference grid */
    private int tileW;

    /** The nominal tile width, in thr reference grid */
    private int tileH;

    /** The horizontal tiling origin, with respect to the canvas origin, in the 
     * reference grid */
    private int tilingOrigX;

    /** The vertical tiling origin, with respect to the canvas origin, in the 
     * reference grid */
    private int tilingOrigY;

    /** The number of components in the image */
    private int nComp;

    /** Downsampling factors along X-axis for each component of the image */
    private int compSubsX[];
    
    /** Downsampling factors along Y-axis for each component of the image */
    private int compSubsY[];
    
    /** The bit-depth (i.e. precision) for each component, before the multiple 
     * component transform. */
    private int origBitDepth[];

    /** If the data was originally signed, for each component */
    private boolean isOrigSigned[];

    /** The horizontal code-block and cell partitioning origin, with respect 
     * to the canvas origin. */
    private int partOrigX;

    /** The vertical code-block and cell partitioning origin, with respect 
     * to the canvas origin. */
    private int partOrigY;

    /** The decoder specifications */
    private DecoderSpecs decSpec;

    /** The boost value used if maxshift was used */
    private int[] maxBoost;
    
    /** Is the precinct partition used */
    boolean precinctPartitionIsUsed;
    
    /** The starting position of the codestream in the file */
    public int initPos;

    /** Vector containing info as to which tile each tilepart belong */
    public Vector tileOfTileParts;

    /** The packed packet headers if the PPM markers are used */
    private Vector unsortedPkdPktHeaders;

    /** The packed packet headers if the PPM or PPT markers are used */
    private ByteArrayOutputStream[] pkdPktHeaders;

    /** The remaining Ippm data to read */
    private int remPPMData;

    /** The last non-finished Ippm field read */
    private byte[] readIPPMData;

    /** The length of the packed packet headers for each tile part */
    private Vector[] tilePartPktHeadLen;

    /** The last tile part that was read for each tile */
    private int[] lastTilePartRead;

    /**
     * Returns the total header length, including the magic number, header
     * length number and encoded header length.
     *
     * @return The total header length.
     * */
    public final int getTotalHeaderLength() {
        return thlen;
    }

    /**
     * Returns the codestream capabilities, as defined in the 'Markers'
     * interface. These capabilities are flag bits, the different flags are
     * defined in 'Markers' as constants with the 'RSIZ' prefix.
     *
     * @return The codestream capabilities
     *
     * @see Markers
     * */
    public final int getCodeStreamCaps() {
        return cdstrmCap;
    }

    /**
     * Returns the image width, in the reference grid.
     *
     * @return The image width in the reference grid
     * */
    public final int getImgWidth() {
        return imgW;
    }

    /**
     * Returns the image height, in the reference grid.
     *
     * @return The image height in the reference grid
     * */
    public final int getImgHeight() {
        return imgH;
    }

    /**
     * Return the horizontal coordinate of the image origin with respect to
     * the canvas one, in the reference grid.
     *
     * @return The horizontal coordinate of the image origin.
     * */
    public final int getImgULX() {
        return imgOrigX;
    }

    /**
     * Return the vertical coordinate of the image origin with respect to the
     * canvas one, in the reference grid.
     *
     * @return The vertical coordinate of the image origin.
     * */
    public final int getImgULY() {
        return imgOrigY;
    }

    /**
     * Returns the nominal width of the tiles in the reference grid.
     *
     * @return The nominal tile width, in the reference grid.
     * */
    public final int getNomTileWidth() {
        return tileW;
    }

    /**
     * Returns the nominal width of the tiles in the reference grid.
     *
     * @return The nominal tile width, in the reference grid.
     * */
    public final int getNomTileHeight(){
        return tileH;
    }

    /**
     * Returns the tiling origin, refferred to as '(Px,Py)' in the 'ImgData'
     * interface.
     *
     * @param co If not null this object is used to return the information. If
     * null a new one is created and returned.
     *
     * @return The coordinate of the tiling origin, in the canvas system, on
     * the reference grid.
     *
     * @see ImgData
     * */
    public final Coord getTilingOrigin(Coord co) {
        if (co != null) {
            co.x = tilingOrigX;
            co.y = tilingOrigY;
            return co;
        }
        else {
            return new Coord(tilingOrigX,tilingOrigY);
        }
    }

    /**
     * Returns true if the original data of the specified component was
     * signed. If the data was not signed a level shift has to be applied at
     * the end of the decompression chain.
     *
     * @param c The index of the component
     *
     * @return True if the original image component was signed.
     * */
    public final boolean isOriginalSigned(int c) {
        return isOrigSigned[c];
    }

    /**
     * Returns the original bitdepth of the specified component.
     *
     * @param c The index of the component
     *
     * @return The bitdepth of the component
     * */
    public final int getOriginalBitDepth(int c) {
        return origBitDepth[c];
    }

    /**
     * Returns the number of components in the image.
     *
     * @return The number of components in the image.
     * */
    public final int getNumComps() {
        return nComp;
    }

    /**
     * Returns the component subsampling factor, with respect to the reference
     * grid,along the horizontal direction for the specified component.
     *
     * @param c The index of the component
     *
     * @return The component subsampling factor X-wise.
     * */
    public final int getCompSubsX(int c) {
        return compSubsX[c];
    }

    /**
     * Returns the component subsampling factor, with respect to the reference
     * grid, along the vertical direction for the specified component.
     *
     * @param c The index of the component
     *
     * @return The component subsampling factor Y-wise.
     * */
    public final int getCompSubsY(int c) {
        return compSubsY[c];
    }

    /**
     * Returns the dequantizer parameters. Dequantizer parameters normally are
     * the quantization step sizes, see DequantizerParams.
     *
     * @param src The source of data for the dequantizer.
     *
     * @param rb The number of range bits for each component. Must be
     * the number of range bits of the mixed components.
     *
     * @return The dequantizer
     * */
    public final Dequantizer createDequantizer(CBlkQuantDataSrcDec src,
					       int rb[]) {
        return new StdDequantizer(src,rb,decSpec);
    }

    /**
     * Returns the horizontal coordinate of the origin of the cell and
     * code-block partition, with respect to the canvas origin, on the
     * reference grid. Allowable values are 0 and 1, nothing else.
     *
     * @return The horizontal coordinate of the origin of the cell and
     * code-block partitions, with respect to the canvas origin, on the
     * reference grid.
     * */
    public final int getPartitionULX() {
        // NOTE: This will probably make more sense to store it in the wavelet
        // decomposition spec.
        return partOrigX;
    }

    /**
     * Returns the vertical coordinate of the origin of the cell and
     * code-block partition, with respect to the canvas origin, on the
     * reference grid. Allowable values are 0 and 1, nothing else.
     *
     * @return The vertical coordinate of the origin of the cell and
     * code-block partitions, with respect to the canvas origin, on the
     * reference grid.
     * */
    public final int getPartitionULY() {
        // NOTE: This will probably make more sense to store it in the wavelet
        // decomposition spec.
        return partOrigY;
    }

    /**
     * Returns the scaling value to use if the maxshift method was specified
     *
     * <P>NOTE: All ROI parameters should be grouped in an ROI spec object.
     *
     * @return The scaling value to use if the maxshift method was specifiedn 
     * */
    public final int[] getMaxBoost(){
        return maxBoost;
    }

    /**
     * Returns the precinct partition width for the specified component, tile
     * and resolution level.
     *
     * @param c the component
     *
     * @param t the tile index
     *
     * @param rl the resolution level
     *
     * @return The precinct partition width for the specified component, 
     * tile and resolution level
     * */
    public final int getPPX(int t, int c, int rl) {
        return decSpec.pss.getPPX(t, c, rl);
    }
    
    /**
     * Returns the precinct partition height for the specified component, tile
     * and resolution level.
     *
     * @param c the component
     *
     * @param t the tile index
     *
     * @param rl the resolution level
     *
     * @return The precinct partition height for the specified component, 
     * tile and resolution level
     * */
    public final int getPPY(int t, int c, int rl) {
        return decSpec.pss.getPPY(t, c, rl);
    }
    
    /** 
     * Returns the boolean used to know if the precinct partition is used
     **/
    public final boolean precinctPartitionUsed() {
        return precinctPartitionIsUsed;
    }

    /**
     * Reads a wavelet filter from the codestream and returns the filter
     * object that implements it.
     *
     * @param ehs The encoded header stream from where to read the info
     * */
    private SynWTFilter readFilter(DataInputStream ehs)
        throws IOException {
        int kid; // the filter id

        kid = ehs.readUnsignedByte();
        if (kid >= (1<<7)) {
            throw new NotImplementedError("Custom filters not supported");
        }
        // Return filter based on ID
        switch (kid) {
        case FilterTypes.W9X7:
            return new SynWTFilterFloatLift9x7();
        case FilterTypes.W5X3:
            return new SynWTFilterIntLift5x3();
        default:
            throw new CorruptedCodestreamException("Specified wavelet filter "+
						  "not"+
                                                  " JPEG 2000 part I "+
						  "compliant");
        }
    }

    /**
     * Checks that the marker length is correct. The filler (i.e. "res") byte
     * is skipped if there is one. If less bytes than the given length
     * ('mlen') are read a warning is printed and the "useless" bytes are
     * skipped. If more bytes than the given length ('mlen') have been read
     * then a 'CorruptedCodestreamException' is thrown.
     *
     * @param ehs The encoded header stream
     *
     * @param spos The marker start position in 'ehs'
     *
     * @param mlen The marker length, as written in the codestream
     *
     * @param str The string identifying the marker, such as "SIZ marker"
     *
     * @exception CorruptedCodestreamException If too much marker data was
     * read, according to the given length.
     *
     * @exception IOException If an I/O error occurs
     * */
    public void checkMarkerLength(DataInputStream ehs, String str) 
	throws IOException {
        if (ehs.available()!=0) {
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,
                         str+" length was short, attempting to resync.");
        }
    }

    /**
     * Read SIZ marker segment, and realign the codestream at the point where
     * the next marker should be found. It is a fixed information marker
     * segment containing informations about image and tile sizes. It is
     * required in the main header immediately after SOC marker segment.
     *
     * @param ehs The encoded header stream
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoded header stream
     * */
    private void readSIZ(DataInputStream ehs) throws IOException {
        int curMarkSegLen;  // Store current marker segment length
        int tmp;

	// Read the length of SIZ fields (Lsiz)
	curMarkSegLen = ehs.readUnsignedShort();

	// Read the capability of the codestream (Rsiz)
	cdstrmCap = ehs.readUnsignedShort();
	if(cdstrmCap!=0)
	    throw new Error("Codestream capabiities not JPEG 2000 - Part I"+
			    " compliant");

	// Read image size
	imgW = ehs.readInt(); // Xsiz
	imgH = ehs.readInt(); // Ysiz
        if (imgW<=0 || imgH<=0 ) {
            throw new IOException("JJ2000 does not support images whose "+
                                  "width and/or height not in the "+
                                  "range: 1 -- (2^31)-1");
        }
	
	// Read image offset
	imgW -= imgOrigX = ehs.readInt(); // XOsiz
	imgH -= imgOrigY = ehs.readInt(); // Y0siz
        if (imgOrigX<0 || imgOrigY<0 ) {
            throw new IOException("JJ2000 does not support images offset "+
                                  "not in the range: 0 -- (2^31)-1");
        }

	// Read size of tile
	tileW = ehs.readInt();
        tileH = ehs.readInt();
        if ( tileW<=0 || tileH<=0 ) {
            throw new IOException("JJ2000 does not support tiles whose "+
                                  "width and/or height are not in  "+
                                  "the range: 1 -- (2^31)-1");
        }

	// Read upper-left tile offset
	tilingOrigX = ehs.readInt(); // XTOsiz
	tilingOrigY = ehs.readInt(); // YTOsiz
        if ( tilingOrigX<0 || tilingOrigY<0 ){
            throw new IOException("JJ2000 does not support tiles whose "+
                                  "offset is not in  "+
                                  "the range: 0 -- (2^31)-1");
        }

	// Read number of components and initialize related arrays
      	nComp = ehs.readUnsignedShort();
        if (nComp<1 || nComp>16384) {
            throw new IllegalArgumentException("Number of component out of "+
                                               "range 1--16384: "+nComp);
        }

	origBitDepth = new int[nComp];
	isOrigSigned = new boolean[nComp];
	compSubsX = new int[nComp];
	compSubsY = new int[nComp];

	// Read bitdepth and downsampling factors for each component
	for(int i = 0; i<nComp; i++) {
	    tmp = ehs.readUnsignedByte();
            isOrigSigned[i] = ((tmp>>>SSIZ_DEPTH_BITS)==1);
            origBitDepth[i] = (tmp & ((1<<SSIZ_DEPTH_BITS)-1))+1;
	    if( (origBitDepth[i]+(isOrigSigned[i]?1:0)) > MAX_COMP_BITDEPTH)
		throw new Error("More than "+MAX_COMP_BITDEPTH+" bit-planes "+
				"signalled for component "+i);
            compSubsX[i] = ehs.readUnsignedByte();
            compSubsY[i] = ehs.readUnsignedByte();
        }

        // Check marker length
        checkMarkerLength(ehs,"SIZ marker");

        // Create needed ModuleSpec
        nTiles = 
            ((imgOrigX+imgW-tilingOrigX+tileW-1) / tileW) * 
            ((imgOrigY+imgH-tilingOrigY+tileH-1) / tileH);

        // Finish initialization of decSpec
        decSpec = new DecoderSpecs(nTiles,nComp);

        if(printInfo){
            String info = 
                nComp+" component(s), "+nTiles+" tile(s)\n";
            info += "Image dimension: "+imgW+"x"+imgH;
            if(nTiles!=1)
                info += "\nNominal Tile dimension: "+tileW+"x"+tileH;
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,info);
        }



	// Store information in hdStr if required
	if(printHeader){
	    hdStr += " --- SIZ ---\n";
	    hdStr += " Capabilities: "+cdstrmCap+"\n";
	    hdStr += " Image dim.  : "+imgW+"x"+imgH+", (off="+imgOrigX+","
		+imgOrigY+")\n";
	    hdStr += " Tile dim.   : "+tileW+"x"+tileH+", (off="+tilingOrigX+","
		+tilingOrigY+")\n";
	    hdStr += " Component(s): "+nComp+"\n";
	    hdStr += " Orig. depth :";
	    for(int i=0; i<nComp; i++)
		hdStr += " "+origBitDepth[i];
	    hdStr += "\n";
	    hdStr += " Orig. signed:";
	    for(int i=0; i<nComp; i++)
		hdStr += " "+isOrigSigned[i];
	    hdStr += "\n";
	    hdStr += " Subs. factor:";
	    for(int i=0; i<nComp; i++)
		hdStr += " "+compSubsX[i]+","+compSubsY[i];
            hdStr += "\n";
	}
    }

    /** 
     * Reads the CRG marker segment and check segment length 
     * */
    private void readCRG(DataInputStream ehs) throws IOException {
        int curMarkSegLen; // Store the length of the current segment

        curMarkSegLen = ehs.readUnsignedShort();
        FacilityManager.getMsgLogger().
            printmsg(MsgLogger.WARNING,"Information in CRG marker segment "+
                     "not taken into account. This may affect the display "+
                     "of the decoded image.");
        ehs.skipBytes(curMarkSegLen-2);

        // Check marker length
        checkMarkerLength(ehs,"CRG marker");
    }
    
    /**
     * Reads COM marker segments and realign the bistream at the point where
     * the next marker should be found.
     *
     * @param ehs The encoded header stream
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoded header stream
     * */
    private void readCOM(DataInputStream ehs) throws IOException{
        int curMarkSegLen;  // Store the length of current segment
        
	// Read length of COM field
	curMarkSegLen = ehs.readUnsignedShort();
        
	// Read the registration value of the COM marker segment
        int rcom = ehs.readUnsignedShort();
	switch(rcom) {
        case RCOM_GEN_USE:
            byte[] b = new byte[curMarkSegLen-4];
            for(int i=0; i<b.length; i++) {
                b[i] = ehs.readByte();
            }
            if(pl.getBooleanParameter("verbose")) {
                FacilityManager.getMsgLogger().println(new String(b),8,2);
            }
            break;
	default:
            // --- Unknown and unsupported markers ---
            // (skip them and see if we can get way with it)
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,
                         "COM marker registered as 0x"+Integer.
                         toHexString(rcom)+
                         " unknown, ignoring (this might crash the "+
                         "decoder or decode a quality degraded or even "+
                         "useless image)");
            ehs.skipBytes(curMarkSegLen-4); //Ignore this field for the moment
	    break;
	}
        
        // Check marker length
        checkMarkerLength(ehs,"COM marker");
    }

    /**
     * Reads QCD marker segment and realigns the codestream at the point where
     * the next marker should be found.
     * 
     * @param ehs The encoded stream.
     *
     * @param mainh Flag indicating whether or not the main header is read
     *
     * @param tileIdx The index of the current tile
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoded header stream.
     * */
    private void readQCD(DataInputStream ehs, boolean mainh, int tileIdx) 
        throws IOException {
        int curMarkSegLen;  // Store the length of current field
	StdDequantizerParams qParms;
        int guardBits;
	int[][] exp;
	float[][] nStep = null;
	
	// Lqcd (length of QCD field)
	curMarkSegLen = ehs.readUnsignedShort();
	
	// Sqcd (quantization style)
        int qStyle = ehs.readUnsignedByte();
	
	guardBits = (qStyle>>SQCX_GB_SHIFT)&SQCX_GB_MSK;
	qStyle &= ~(SQCX_GB_MSK<<SQCX_GB_SHIFT);

        if(mainh){
            // If the main header is being read set default value of
            // dequantization spec
            switch (qStyle) {
            case SQCX_NO_QUANTIZATION:
                decSpec.qts.setDefault("reversible");
                break;
            case SQCX_SCALAR_DERIVED:
                decSpec.qts.setDefault("derived");
                break;
            case SQCX_SCALAR_EXPOUNDED:
                decSpec.qts.setDefault("expounded");
                break;
            default:
                throw new CorruptedCodestreamException("Unknown or "+
                                                       "unsupported "+
                                                      "quantization style "+
                                                      "in Sqcd field, QCD "+
                                                      "marker main header");
            }
        } else {
            // If the tile header is being read set default value of
            // dequantization spec for tile
            switch (qStyle) {
            case SQCX_NO_QUANTIZATION:
                decSpec.qts.setTileDef(tileIdx, "reversible");
                break;
            case SQCX_SCALAR_DERIVED:
                decSpec.qts.setTileDef(tileIdx, "derived");
                break;
            case SQCX_SCALAR_EXPOUNDED:
                decSpec.qts.setTileDef(tileIdx, "expounded");
                break;
            default:
                throw new CorruptedCodestreamException("Unknown or "+
                                                       "unsupported "+
                                                      "quantization style "+
                                                      "in Sqcd field, QCD "+
                                                      "marker, tile header");
            }
        }

        qParms = new StdDequantizerParams();

        if(qStyle == SQCX_NO_QUANTIZATION) {
	    int maxrl = (qStyle == SQCX_SCALAR_DERIVED) ? 0 :
                ( mainh ?
                 ((Integer)decSpec.dls.getDefault()).intValue() :
                 ((Integer)decSpec.dls.getTileDef(tileIdx)).intValue());
            int i,j,rl;
            int minb,maxb,hpd;
            int tmp;

	    exp = qParms.exp = new int[maxrl+1][];
		
            for (rl=0; rl <= maxrl; rl++) { // Loop on resolution levels
                // Find the number of subbands in the resolution level
                if (rl == 0) { // Only the LL subband
                    minb = 0;
                    maxb = 1;
                }
                else {
                    // Dyadic decomposition
                    hpd = 1;

                    // Adapt hpd to resolution level
                    if (hpd > maxrl-rl) {
                        hpd -= maxrl-rl;
                    }
                    else {
                        hpd = 1;
                    }
                    // Determine max and min subband index
                    minb = 1<<((hpd-1)<<1); // minb = 4^(hpd-1)
                    maxb = 1<<(hpd<<1); // maxb = 4^hpd
                }
                // Allocate array for subbands in resolution level
		exp[rl] = new int[maxb];

                for(j=minb; j<maxb; j++) {
                    tmp = ehs.readUnsignedByte();
		    exp[rl][j] = (tmp>>SQCX_EXP_SHIFT)&SQCX_EXP_MASK;
                }// end for j
            }// end for rl
	}
	else{
	    int maxrl = (qStyle == SQCX_SCALAR_DERIVED) ? 0 :
                ( mainh ?
                 ((Integer)decSpec.dls.getDefault()).intValue() :
                 ((Integer)decSpec.dls.getTileDef(tileIdx)).intValue());
            int i,j,rl;
            int minb,maxb,hpd;
            int tmp;

            exp = qParms.exp = new int[maxrl+1][];
            nStep = qParms.nStep = new float[maxrl+1][];

            for (rl=0; rl <= maxrl; rl++) { // Loop on resolution levels
                // Find the number of subbands in the resolution level
                if (rl == 0) { // Only the LL subband
                    minb = 0;
                    maxb = 1;
                }
                else {
                    // Dyadic decomposition
                    hpd = 1;

                    // Adapt hpd to resolution level
                    if (hpd > maxrl-rl) {
                        hpd -= maxrl-rl;
                    }
                    else {
                        hpd = 1;
                    }
                    // Determine max and min subband index
                    minb = 1<<((hpd-1)<<1); // minb = 4^(hpd-1)
                    maxb = 1<<(hpd<<1); // maxb = 4^hpd
                }
                // Allocate array for subbands in resolution level
		exp[rl] = new int[maxb];
		nStep[rl] = new float[maxb];

                for(j=minb; j<maxb; j++) {
                    tmp = ehs.readUnsignedShort();
		    exp[rl][j] = (tmp>>11) & 0x1f;
		    // NOTE: the formula below does not support more
		    // than 5 bits for the exponent, otherwise
		    // (-1<<exp) might overflow (the - is used to be
		    // able to represent 2**31)
		    nStep[rl][j] =
			(-1f-((float)(tmp & 0x07ff))/(1<<11))/
			(-1<<exp[rl][j]);
                }// end for j
            }// end for rl
        } // end if (qStyle != SQCX_NO_QUANTIZATION)

	// Fill qsss, gbs
	if(mainh){
	    decSpec.qsss.setDefault(qParms);
            decSpec.gbs.setDefault(new Integer(guardBits));
        }
	else{
	    decSpec.qsss.setTileDef(tileIdx,qParms);
            decSpec.gbs.setTileDef(tileIdx,new Integer(guardBits));
        }

        // Check marker length
        checkMarkerLength(ehs,"QCD marker");

	if(printHeader){
	    hdStr += " --- QCD ---\n";
	    hdStr += " Quant. type : "+qStyle+"\n";
	    hdStr += " Guard bits  : "+guardBits+"\n";
	    if(qStyle == SQCX_NO_QUANTIZATION){
		hdStr += " Exponent    : \n";
		for(int i=0; i<exp.length; i++){
		    for(int j=0; j<exp[i].length; j++){
			hdStr += "   "+i+","+j+" = "+exp[i][j]+"\n";
		    }
		}
	    }
	    else{
		hdStr += " Exp / nStep : \n";
		for(int i=0; i<exp.length; i++){
		    for(int j=0; j<exp[i].length; j++){
			hdStr += "   "+i+","+j+" = "+exp[i][j]+"  "+
			    nStep[i][j]+"\n";
		    }
		}		
	    }
	}
    }

    /**
     * Reads QCC marker segment and realigns the codestream at the point where
     * the next marker should be found.
     * 
     * @param ehs The encoded stream.
     *
     * @param mainh Flag indicating whether or not the main header is read
     *
     * @param tileIdx The index of the current tile
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoded header stream.
     * */
    private void readQCC(DataInputStream ehs, boolean mainh, int tileIdx)
        throws IOException {
        int curMarkSegLen;  // Store the length of current field
        int cComp;          // current component
        int tmp;
	StdDequantizerParams qParms;
        int guardBits;
	int[][] expC;
	float[][] nStepC = null;

	// Lqcc (length of QCC field)
	curMarkSegLen = ehs.readUnsignedShort();
	
        // Cqcc
        if (nComp < 257) {
            cComp = ehs.readUnsignedByte();
        }
        else {
            cComp = ehs.readUnsignedShort();
        }
        if (cComp >= nComp) {
            throw new CorruptedCodestreamException("Invalid component "+
                                                   "index in QCC marker");
        }
	
	// Sqcc (quantization style)
	tmp = ehs.readUnsignedByte();
	guardBits =((tmp>>SQCX_GB_SHIFT)&SQCX_GB_MSK);
	tmp &= ~(SQCX_GB_MSK<<SQCX_GB_SHIFT);
	int qStyle = tmp;

        if(mainh){
            // If main header is being read, set default for component in all
            // tiles
            switch (qStyle) {
            case SQCX_NO_QUANTIZATION:
                decSpec.qts.setCompDef(cComp,"reversible");
                break;
            case SQCX_SCALAR_DERIVED:
                decSpec.qts.setCompDef(cComp,"derived");
                break;
            case SQCX_SCALAR_EXPOUNDED:
                decSpec.qts.setCompDef(cComp,"expounded");
                break;
            default:
                throw new CorruptedCodestreamException("Unknown or "+
                                                       "unsupported "+
                                                       "quantization style "+
                                                       "in Sqcd field, QCD "+
                                                       "marker, main header");
            }
        }else{
            // If tile header is being read, set value for component in
            // this tiles
            switch (qStyle) {
            case SQCX_NO_QUANTIZATION:
                decSpec.qts.setTileCompVal(tileIdx, cComp,"reversible");
                break;
            case SQCX_SCALAR_DERIVED:
                decSpec.qts.setTileCompVal(tileIdx, cComp,"derived");
                break;
            case SQCX_SCALAR_EXPOUNDED:
                decSpec.qts.setTileCompVal(tileIdx, cComp,"expounded");
                break;
            default:
                throw new CorruptedCodestreamException("Unknown or "+
                                                       "unsupported "+
                                                       "quantization style "+
                                                       "in Sqcd field, QCD "+
                                                       "marker, main header");
            }
        }

	// Decode all dequantizer params
        qParms = new StdDequantizerParams();

        if (qStyle == SQCX_NO_QUANTIZATION) {
	    int maxrl = (qStyle == SQCX_SCALAR_DERIVED) ? 0 :
                ( mainh ?
                 ((Integer)decSpec.dls.getCompDef(cComp)).intValue() :
                 ((Integer)decSpec.dls.getTileCompVal(tileIdx,cComp)).
		  intValue());
            int i,j,rl;
            int minb,maxb,hpd;
        
	    expC = qParms.exp = new int[maxrl+1][];

            for (rl=0; rl <= maxrl; rl++) { // Loop on resolution levels
                // Find the number of subbands in the resolution level
                if (rl == 0) { // Only the LL subband
                    minb = 0;
                    maxb = 1;
                }
                else {
                    // Dyadic decomposition
                    hpd = 1;

                    // Adapt hpd to resolution level
                    if (hpd > maxrl-rl) {
                        hpd -= maxrl-rl;
                    }
                    else {
                        hpd = 1;
                    }
                    // Determine max and min subband index
                    minb = 1<<((hpd-1)<<1); // minb = 4^(hpd-1)
                    maxb = 1<<(hpd<<1); // maxb = 4^hpd
                }
                // Allocate array for subbands in resolution level
		expC[rl] = new int[maxb];

                for(j=minb; j<maxb; j++) {
                    tmp = ehs.readUnsignedByte();
		    expC[rl][j] = (tmp>>SQCX_EXP_SHIFT)&SQCX_EXP_MASK;
                }// end for j
            }// end for rl
	}
	else{
	    int maxrl = (qStyle == SQCX_SCALAR_DERIVED) ? 0 :
                ( mainh ?
                 ((Integer)decSpec.dls.getCompDef(cComp)).intValue() :
                 ((Integer)decSpec.dls.getTileCompVal(tileIdx,cComp)).
		  intValue());
            int i,j,rl;
            int minb,maxb,hpd;

            nStepC = qParms.nStep = new float[maxrl+1][];
            expC =  qParms.exp = new int[maxrl+1][];

            for (rl=0; rl <= maxrl; rl++) { // Loop on resolution levels
                // Find the number of subbands in the resolution level
                if (rl == 0) { // Only the LL subband
                    minb = 0;
                    maxb = 1;
                }
                else {
                    // Dyadic decomposition
                    hpd = 1;

                    // Adapt hpd to resolution level
                    if (hpd > maxrl-rl) {
                        hpd -= maxrl-rl;
                    }
                    else {
                        hpd = 1;
                    }
                    // Determine max and min subband index
                    minb = 1<<((hpd-1)<<1); // minb = 4^(hpd-1)
                    maxb = 1<<(hpd<<1); // maxb = 4^hpd
                }
                // Allocate array for subbands in resolution level
		expC[rl] = new int[maxb];
		nStepC[rl] = new float[maxb];
                
                for(j=minb; j<maxb; j++) {
                    tmp = ehs.readUnsignedShort();
                    expC[rl][j] = (tmp>>11) & 0x1f;
                    // NOTE: the formula below does not support more
                    // than 5 bits for the exponent, otherwise
                    // (-1<<exp) might overflow (the - is used to be
                    // able to represent 2**31)
                    nStepC[rl][j] =
                        (-1f-((float)(tmp & 0x07ff))/(1<<11))/
                        (-1<<expC[rl][j]);
                }// end for j
            }// end for rl
        } // end if (qStyle != SQCX_NO_QUANTIZATION)

	// Fill qsss, gbs
	if(mainh){
	    decSpec.qsss.setCompDef(cComp,qParms);
            decSpec.gbs.setCompDef(cComp,new Integer(guardBits));
        }
	else{
	    decSpec.qsss.setTileCompVal(tileIdx,cComp,qParms);
            decSpec.gbs.setTileCompVal(tileIdx,cComp,new Integer(guardBits));
        }

        // Check marker length
        checkMarkerLength(ehs,"QCC marker");

	if(printHeader){
	    hdStr += " --- QCC("+cComp+") ---\n";
	    hdStr += " Quant. type : "+qStyle+"\n";
	    hdStr += " Guard bits  : "+guardBits+"\n";
	    if(qStyle == SQCX_NO_QUANTIZATION){
		hdStr += " Exponent    : \n";
		for(int i=0; i<expC.length; i++){
		    for(int j=0; j<expC[i].length; j++){
			hdStr += "   "+i+","+j+" = "+expC[i][j]+"\n";
		    }
		}
	    }
	    else{
		hdStr += " Exp / nStep : \n";
		for(int i=0; i<expC.length; i++){
		    for(int j=0; j<expC[i].length; j++){
			hdStr += "   "+i+","+j+" = "+expC[i][j]+"  "+
			    nStepC[i][j]+"\n";
		    }
		}		
	    }
	}
	
    }

    /**
     * Reads COD marker segment and realigns the codestream where the next
     * marker should be found.
     *
     * @param ehs The encoder header stream.
     *
     * @param tileh Flag indicating whether the main header is read
     *
     * @param tileIdx The index of the current tile
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readCOD(DataInputStream ehs, boolean mainh, int tileIdx) 
        throws IOException {
        int curMarkSegLen;  // Store current marker segment length
        int cstyle;         // The block style
        SynWTFilter hfilters[],vfilters[];
        int l;
        Integer cblk[];
        String errMsg;
        boolean sopUsed = false;
        boolean ephUsed = false;

        // Lcod (marker length)
        curMarkSegLen = ehs.readUnsignedShort();

        // Scod (block style)
        // We only support wavelet transformed data
        int origcstyle = cstyle = ehs.readUnsignedByte();

        if( (cstyle&SCOX_PRECINCT_PARTITION) != 0 ){
            precinctPartitionIsUsed = true;
            // Remove flag
            cstyle &= ~(SCOX_PRECINCT_PARTITION);
        }
        else {
            precinctPartitionIsUsed = false;
        } 

        // SOP markers
        if (mainh) {
            if( (cstyle&SCOX_USE_SOP) != 0 ){
                // SOP markers are used
                decSpec.sops.setDefault(new Boolean("true"));
                sopUsed = true;
                // Remove flag
                cstyle &= ~(SCOX_USE_SOP);
            }
            else {
                // SOP markers are not used
                decSpec.sops.setDefault(new Boolean("false"));
            }
        }
        else {
            if( (cstyle&SCOX_USE_SOP) != 0 ){
                // SOP markers are used
                decSpec.sops.setTileDef(tileIdx, new Boolean("true"));
                sopUsed = true;
                // Remove flag
                cstyle &= ~(SCOX_USE_SOP);
            }
            else {
                // SOP markers are not used
                decSpec.sops.setTileDef(tileIdx, new Boolean("false"));
            }
        }

        // EPH markers
        if (mainh) {
            if( (cstyle&SCOX_USE_EPH) != 0 ){
                // EPH markers are used
                decSpec.ephs.setDefault(new Boolean("true"));
                ephUsed = true;
                // Remove flag
                cstyle &= ~(SCOX_USE_EPH);
            }
            else {
                // EPH markers are not used
                decSpec.ephs.setDefault(new Boolean("false"));
            }
        }
        else {
            if( (cstyle&SCOX_USE_EPH) != 0 ){
                // EPH markers are used
                decSpec.ephs.setTileDef(tileIdx, new Boolean("true"));
                ephUsed = true;
                // Remove flag
                cstyle &= ~(SCOX_USE_EPH);
            }
            else {
                // EPH markers are not used
                decSpec.ephs.setTileDef(tileIdx, new Boolean("false"));
            }
        }

        // SGcod
        // Read the progressive order
        int progType = ehs.readUnsignedByte();

        // Read the number of layers
        int numLayers = ehs.readUnsignedShort();
        if (numLayers<=0 || numLayers>65535 ) {
            throw new CorruptedCodestreamException("Number of layers out of "+
                                                   "range: 1--65535");
        }
            
	// Multiple component transform
	int mct = ehs.readUnsignedByte();
	        
        // SPcod
        // decomposition levels
        int mrl = ehs.readUnsignedByte();
        if( mrl>32 ){
            throw new CorruptedCodestreamException("Number of decomposition "+
                                                   "levels out of range: "+
                                                   "0--32");
        }
        
        // Read the code-blocks dimensions
        cblk = new Integer[2];
        cblk[0] = new Integer(1<<(ehs.readUnsignedByte()+2));
        if ( cblk[0].intValue() < StdEntropyCoderOptions.MIN_CB_DIM ||
             cblk[0].intValue() > StdEntropyCoderOptions.MAX_CB_DIM  ) {
            errMsg = "Non-valid code-block width in SPcod field, "+
                "COD marker";
            throw new CorruptedCodestreamException(errMsg);
        }
        cblk[1] = new Integer(1<<(ehs.readUnsignedByte()+2));
        if ( cblk[1].intValue() < StdEntropyCoderOptions.MIN_CB_DIM || 
             cblk[1].intValue() > StdEntropyCoderOptions.MAX_CB_DIM ) {
            errMsg = "Non-valid code-block height in SPcod field, "+
                "COD marker";
            throw new CorruptedCodestreamException(errMsg);
        }
        if ( (cblk[0].intValue()*cblk[1].intValue()) > 
             StdEntropyCoderOptions.MAX_CB_AREA ) {
            errMsg = "Non-valid code-block area in SPcod field, "+
                "COD marker";
            throw new CorruptedCodestreamException(errMsg);
        }
        if ( mainh ) {
            decSpec.cblks.setDefault(cblk);
        }
        else {
            decSpec.cblks.setTileDef(tileIdx, cblk);
        }

        // Style of the code-block coding passes
        int ecOptions = ehs.readUnsignedByte();
        if ((ecOptions &
             ~(OPT_BYPASS|OPT_RESET_MQ|OPT_REG_TERM|
	       OPT_VERT_STR_CAUSAL|OPT_ER_TERM | OPT_SEG_MARKERS)) != 0){
            throw
                new CorruptedCodestreamException("Unknown \"code-block "+
                                                "style\" in SPcod field, "+
                                                "COD marker: 0x"+
                                                Integer.
                                                 toHexString(ecOptions));
        }

        // Read wavelet filter for tile or image
	hfilters = new SynWTFilter[1];
	vfilters = new SynWTFilter[1];
	hfilters[0] = readFilter(ehs);
	vfilters[0] = hfilters[0];

        // Fill the filter spec
        // If this is the main header, set the default value, if it is the
        // tile header, set default for this tile 
        SynWTFilter[][] hvfilters = new SynWTFilter[2][];
        hvfilters[0]=hfilters;
        hvfilters[1]=vfilters;

        // Get precinct partition sizes
        Vector v[] = new Vector[2];
        v[0] = new Vector();
        v[1] = new Vector();
        int val = PRECINCT_PARTITION_DEF_SIZE;
        if ( !precinctPartitionIsUsed ) {
            Integer w, h;
            w = new Integer(1<<(val & 0x000F));
            v[0].addElement(w);
            h = new Integer(1<<(((val & 0x00F0)>>4)));
            v[1].addElement(h);
        }
        else {
            for ( int rl=mrl ; rl>=0 ; rl-- ) {
                Integer w, h;
                val = ehs.readUnsignedByte();
                w = new Integer(1<<(val & 0x000F));
                v[0].insertElementAt(w, 0);
                h = new Integer(1<<(((val & 0x00F0)>>4)));
                v[1].insertElementAt(h, 0);
            }
        }
        if ( mainh ) {
            decSpec.pss.setDefault(v);
        }
        else {
            decSpec.pss.setTileDef(tileIdx, v);
        }
	precinctPartitionIsUsed = true;
        
        // Check marker length
        checkMarkerLength(ehs,"COD marker");

        // Store specifications in decSpec
        if(mainh){
            decSpec.wfs.setDefault(hvfilters);
            decSpec.dls.setDefault(new Integer(mrl));
            decSpec.ecopts.setDefault(new Integer(ecOptions));
	    decSpec.cts.setDefault(new Integer(mct));
            decSpec.nls.setDefault(new Integer(numLayers));
            decSpec.pos.setDefault(new Integer(progType));
	}
        else{
            decSpec.wfs.setTileDef(tileIdx, hvfilters);
            decSpec.dls.setTileDef(tileIdx,new Integer(mrl));
            decSpec.ecopts.setTileDef(tileIdx,new Integer(ecOptions));
	    decSpec.cts.setTileDef(tileIdx,new Integer(mct));
            decSpec.nls.setTileDef(tileIdx,new Integer(numLayers));
            decSpec.pos.setTileDef(tileIdx,new Integer(progType));
        }

        if(printHeader){
	    hdStr += " --- COD ---\n";
            hdStr += " Coding style  : "+origcstyle+"\n";
            hdStr += " Num. of levels: "+mrl+"\n";

            switch(progType) {
            case ProgressionType.LY_RES_COMP_POS_PROG:
                hdStr += " Progress. type: LY_RES_COMP_POS_PROG\n";
                break;
            case ProgressionType.RES_LY_COMP_POS_PROG:
                hdStr += " Progress. type: RES_LY_COMP_POS_PROG\n";
                break;
            case ProgressionType.RES_POS_COMP_LY_PROG:
                hdStr += " Progress. type: RES_POS_COMP_LY_PROG\n";
                break;
            case ProgressionType.POS_COMP_RES_LY_PROG:
                hdStr += " Progress. type: POS_COMP_RES_LY_PROG\n";
                break;
            case ProgressionType.COMP_POS_RES_LY_PROG:
                hdStr += " Progress. type: COMP_POS_RES_LY_PROG\n";
                break;
            }

            hdStr += " Num. of layers: "+numLayers+"\n";
            hdStr += " Cblk width    : "+cblk[0]+"\n";
            hdStr += " Cblk height   : "+cblk[1]+"\n";
            hdStr += " EC options    : "+ecOptions+"\n"; 
            hdStr += " Filter        : "+hfilters[0]+"\n";
	    hdStr += " Multi comp tr.: "+(mct==1)+"\n";  
            hdStr += " Precincts     : w:"+v[0]+", h:"+v[1]+"\n";
            hdStr += " SOP markers   : "+sopUsed+"\n";
            hdStr += " EPH markers   : "+ephUsed+"\n";
        }
    }

    /**
     * Reads the COC marker segment and realigns the codestream where the next
     * marker should be found.
     *
     * @param ehs The encoder header stream.
     *
     * @param mainh Flag indicating whether or not the main header is read
     *
     * @param tileIdx The index of the current tile
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readCOC(DataInputStream ehs, boolean mainh, int tileIdx) 
        throws IOException {
	int curMarkSegLen; // Store current marker segment length
        int cComp;         // current component
        int cstyle;        // The block style
        SynWTFilter hfilters[],vfilters[];
        int tmp,l;
        int ecOptions;
        Integer cblk[];
        String errMsg;

	// Lcoc (marker length)
        curMarkSegLen = ehs.readUnsignedShort();

        // Ccoc
        if (nComp < 257) {
            cComp = ehs.readUnsignedByte();
        }
        else {
            cComp = ehs.readUnsignedShort();
        }
        if (cComp >= nComp) {
            throw new CorruptedCodestreamException("Invalid component index "+
						   "in QCC marker");
        }

	// Scoc (block style)
	int origcstyle = cstyle = ehs.readUnsignedByte();
        if( (cstyle&SCOX_PRECINCT_PARTITION) != 0 ){
            precinctPartitionIsUsed = true;
            // Remove flag
            cstyle &= ~(SCOX_PRECINCT_PARTITION);
        }
        else {
            precinctPartitionIsUsed = false;
        }

        // SPcoc

	// decomposition levels
        int mrl = ehs.readUnsignedByte();

        // Read the code-blocks dimensions
        cblk = new Integer[2];
        cblk[0] = new Integer(1<<(ehs.readUnsignedByte()+2));
        if ( cblk[0].intValue() < StdEntropyCoderOptions.MIN_CB_DIM ||
             cblk[0].intValue() > StdEntropyCoderOptions.MAX_CB_DIM  ) {
            errMsg = "Non-valid code-block width in SPcod field, "+
                "COC marker";
            throw new CorruptedCodestreamException(errMsg);
        }
        cblk[1] = new Integer(1<<(ehs.readUnsignedByte()+2));
        if ( cblk[1].intValue() < StdEntropyCoderOptions.MIN_CB_DIM || 
             cblk[1].intValue() > StdEntropyCoderOptions.MAX_CB_DIM ) {
            errMsg = "Non-valid code-block height in SPcod field, "+
                "COC marker";
            throw new CorruptedCodestreamException(errMsg);
        }
        if ( (cblk[0].intValue()*cblk[1].intValue()) > 
             StdEntropyCoderOptions.MAX_CB_AREA ) {
            errMsg = "Non-valid code-block area in SPcod field, "+
                "COC marker";
            throw new CorruptedCodestreamException(errMsg);
        }
        if ( mainh ) {
            decSpec.cblks.setCompDef(cComp, cblk);
        }
        else {
            decSpec.cblks.setTileCompVal(tileIdx, cComp, cblk);
        }

        // Read entropy block mode options
        // NOTE: currently OPT_SEG_MARKERS is not included here
        ecOptions = ehs.readUnsignedByte();
        if ((ecOptions &
             ~(OPT_BYPASS|OPT_RESET_MQ|OPT_REG_TERM|
	       OPT_VERT_STR_CAUSAL|OPT_ER_TERM|OPT_SEG_MARKERS)) != 0){
            throw
                new CorruptedCodestreamException("Unknown \"code-block "+
                                                 "context\" in SPcoc field, "+
                                                 "COC marker: 0x"+
                                                 Integer.
                                                 toHexString(ecOptions));
        }

        // Read wavelet filter for tile or image
	hfilters = new SynWTFilter[1];
	vfilters = new SynWTFilter[1];
	hfilters[0] = readFilter(ehs);
	vfilters[0] = hfilters[0];

        // Fill the filter spec
        // If this is the main header, set the default value, if it is the
        // tile header, set default for this tile 
        SynWTFilter[][] hvfilters = new SynWTFilter[2][];
        hvfilters[0]=hfilters;
        hvfilters[1]=vfilters;

        // Get precinct partition sizes
        Vector v[] = new Vector[2];
        v[0] = new Vector();
        v[1] = new Vector();
        int val = PRECINCT_PARTITION_DEF_SIZE;
        if ( !precinctPartitionIsUsed ) {
            Integer w, h;
            w = new Integer(1<<(val & 0x000F));
            v[0].addElement(w);
            h = new Integer(1<<(((val & 0x00F0)>>4)));
            v[1].addElement(h);
        }
        else {
            for ( int rl=mrl ; rl>=0 ; rl-- ) {
                Integer w, h;
                val = ehs.readUnsignedByte();
                w = new Integer(1<<(val & 0x000F));
                v[0].insertElementAt(w, 0);
                h = new Integer(1<<(((val & 0x00F0)>>4)));
                v[1].insertElementAt(h, 0);
            }
        }
        if ( mainh ) {
            decSpec.pss.setCompDef(cComp, v);
        }
        else {
            decSpec.pss.setTileCompVal(tileIdx, cComp, v);
        }
	precinctPartitionIsUsed = true;

        // Check marker length
        checkMarkerLength(ehs,"COD marker");

        if(mainh){
            decSpec.wfs.setCompDef(cComp,hvfilters);
            decSpec.dls.setCompDef(cComp,new Integer(mrl));
            decSpec.ecopts.setCompDef(cComp,new Integer(ecOptions));
        }
        else{
            decSpec.wfs.setTileCompVal(tileIdx,cComp,hvfilters);
            decSpec.dls.setTileCompVal(tileIdx,cComp,new Integer(mrl));
            decSpec.ecopts.setTileCompVal(tileIdx,cComp,
                                          new Integer(ecOptions));
        }

	if(printHeader){
	    hdStr += " --- COC("+cComp+") ---\n";
	    hdStr += " Coding style  : "+origcstyle+"\n";
	    hdStr += " Num. of levels: "+
		(((Integer)decSpec.dls.getDefault()).intValue())+"\n";
            hdStr += " Cblk width    : "+cblk[0]+"\n";
            hdStr += " Cblk height   : "+cblk[1]+"\n";
	    hdStr += " EC options    : "+ecOptions+"\n";
            hdStr += " Filter        : "+hfilters[0]+"\n";
            hdStr += " Precincts     : w:"+v[0]+", h:"+v[1]+"\n";
	}
    }

    /** 
     * Reads the POC marker segment and realigns the codestream where the next
     * marker should be found.
     *
     * @param ehs The encoder header stream.
     *
     * @param mainh Flag indicating whether or not the main header is read
     *
     * @param t The index of the current tile
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readPOC(DataInputStream ehs,boolean mainh, int t)
    throws IOException {
	boolean useShort = (nComp>=256) ? true : false;

        // Lpos
        int lpos = ehs.readUnsignedShort();

        // Compute the number of progression changes
        // nChg = (lpos - Lpos(2)) / (RSpos(1) + CSpos(2) +
        //  LYEpos(2) + REpos(1) + CEpos(2) + Ppos (1) )
        int nChg = (lpos-2)/(5+ (useShort?4:2));

        int[][] change = new int[nChg][6];

        for(int chg=0; chg<nChg; chg++){
            
            // RSpos
            change[chg][0] = ehs.readUnsignedByte();

            // CSpos
	    if(useShort)
		change[chg][1] = ehs.readUnsignedShort();
	    else
		change[chg][1] = ehs.readUnsignedByte();


            // LYEpos
	    change[chg][2] = ehs.readUnsignedShort();

            // REpos
            change[chg][3] = ehs.readUnsignedByte();

            // CEpos
	    if(useShort)
		change[chg][4] = ehs.readUnsignedShort();
	    else
		change[chg][4] = ehs.readUnsignedByte();

            // Ppos
            change[chg][5] = ehs.readUnsignedByte();
        }

        // Check marker length
        checkMarkerLength(ehs,"POC marker");

        // Register specifications
        if(mainh){
            decSpec.pcs.setDefault(change);
        }
        else{
            decSpec.pcs.setTileDef(t,change);
        }

	if(printHeader){
	    hdStr += " --- POC ---\n";
	    hdStr += " Chg_idx RSpos CSpos LYEpos REpos CEpos Cpos\n";
            for(int chg=0;chg<nChg;chg++)
                hdStr += "   "+chg
                    +"      "+change[chg][0]
                    +"     "+change[chg][1]
                    +"     "+change[chg][2]
                    +"      "+change[chg][3]
                    +"     "+change[chg][4]
                    +"     "+change[chg][5];
	}
    }

    /**
     * Reads TLM marker segment and realigns the codestream where the next
     * marker should be found. Informations stored in these fields are
     * currently NOT taken into account.
     *
     * @param ehs The encoder header stream.
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readTLM(DataInputStream ehs) throws IOException {
	int length;
	
	length = ehs.readUnsignedShort();
	//Ignore all informations contained
	ehs.skipBytes(length-2);

        FacilityManager.getMsgLogger().
            printmsg(MsgLogger.INFO,"Skipping unsupported TLM marker");
    }

    /**
     * Reads PLM marker segment and realigns the codestream where the next
     * marker should be found. Informations stored in these fields are
     * currently not taken into account.
     *
     * @param ehs The encoder header stream.
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readPLM(DataInputStream ehs) throws IOException{
	int length;
	
	length = ehs.readUnsignedShort();
	//Ignore all informations contained
	ehs.skipBytes(length-2);

        FacilityManager.getMsgLogger().
            printmsg(MsgLogger.INFO,"Skipping unsupported PLM marker");
    }

    /**
     * Reads the PLT fields and realigns the codestream where the next marker
     * should be found. Informations stored in these fields are currently NOT
     * taken into account.
     *
     * @param ehs The encoder header stream.
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readPLTFields(DataInputStream ehs) throws IOException{
	int length;
	
	length = ehs.readUnsignedShort();
	//Ignore all informations contained
	ehs.skipBytes(length-2);

        FacilityManager.getMsgLogger().
            printmsg(MsgLogger.INFO,"Skipping unsupported PLT marker");
    }

    /**
     * Reads the RGN marker segment of the codestream header.
     *
     * <P>May be used in tile or main header. If used in main header, it
     * refers to the maxshift value of a component in all tiles. When used in
     * tile header, only the particular tile-component is affected.
     *
     * @param ehs The encoder header stream.
     *
     * @param mainh Flag indicating whether or not the main header is read
     *
     * @param tileIdx The index of the current tile
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readRGN(DataInputStream ehs, boolean mainh, int tileIdx)
        throws IOException{
	int roiType;
        int curMarkSegLen;  // Current marker length
        int comp;           // ROI component
        int i;              // loop variable
        int tempComp;       // Component for
        int val;            // The roi scaling value

	// Lrgn (marker length)
        curMarkSegLen = ehs.readUnsignedShort();

        // Read component
        comp = (nComp < 257) ? ehs.readUnsignedByte():
            ehs.readUnsignedShort();
        if (comp >= nComp) {
            throw new CorruptedCodestreamException("Invalid component "+
                                                  "index in RGN marker"+
                                                  comp);
        }

        // Read type of RGN.(Srgn) 
        roiType = ehs.readUnsignedByte();

        // Check that we can handle it.
        if(roiType != SRGN_IMPLICIT)
            throw new CorruptedCodestreamException("Unknown or unsupported "+
                                                  "Srgn parameter in ROI "+
                                                  "marker");

        // SPrgn
	if(decSpec.rois == null){ // No maxshift spec defined
	    // Create needed ModuleSpec
	    decSpec.rois=new MaxShiftSpec(nTiles, nComp,
                                          ModuleSpec.SPEC_TYPE_TILE_COMP);
	}
	
	if(mainh){
            val = ehs.readUnsignedByte();
	    decSpec.rois.setCompDef(comp, new Integer(val));
        }
	else{
            val = ehs.readUnsignedByte();
	    decSpec.rois.setTileCompVal(tileIdx,comp,new Integer(val));
        }

        // Check marker length
        checkMarkerLength(ehs,"RGN marker");

        if(printHeader){
            hdStr += " --- RGN("+comp+") ---\n";
            hdStr += " ROI scaling value: "+val+"\n";
	}
    }

    /**
     * Reads the PPM marker segment of the main header.
     *
     * @param ehs The encoder header stream.
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readPPM(DataInputStream ehs)
        throws IOException{
        int curMarkSegLen;
        int i,indx,len,off;
        int remSegLen;
        byte[] b;

        if(unsortedPkdPktHeaders == null){
            tileOfTileParts = new Vector();
            unsortedPkdPktHeaders = new Vector();
            decSpec.pphs.setDefault(new Boolean(true));
        }

	// Lppm (marker length)
        curMarkSegLen = ehs.readUnsignedShort();
        remSegLen = curMarkSegLen - 3;

        // Zppm (index of PPM marker)
        indx = ehs.readUnsignedByte();

        // Nppm (lengths of individual tileparts packed headers)
        while( remSegLen > 0 ){
            // If no more info is to be read for this tile part get
            // new Nppm value
            if(remPPMData == 0){
                remPPMData = ehs.readInt();
                remSegLen -= 4;
                off = 0;
            }
            else
                off = readIPPMData.length - remPPMData;
            
            // Read packet header data
            len = (remSegLen < remPPMData) ? remSegLen:remPPMData;
            b = new byte[len];
            ehs.read(b,0,len);
            
            remPPMData -= len;
            remSegLen -= len;

            if(remPPMData == 0){
                unsortedPkdPktHeaders.addElement(b);  
            }
            else{
                for(i=0 ; i<len ; i++,off++)
                    readIPPMData[off]=b[i];
                if(remPPMData == 0)
                    unsortedPkdPktHeaders.addElement(readIPPMData);  
            }
            nReadPPMmarker++;
        }

        // Check marker length
        checkMarkerLength(ehs,"PPM marker");
    }

    /**
     * Reads the PPT marker segment of the main header.
     *
     * @param ehs The encoder header stream.
     *
     * @param tile The tile to which the current tile part belongs
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    private void readPPT(DataInputStream ehs, int tile)
        throws IOException{
        int curMarkSegLen;
        int indx,len=0;
        byte[] temp;
        byte[][] tilePkdPktHeaders;

        // If this is first PPT marker, initiate marker counters
        if(nReadPPTmarker == null){
            nReadPPTmarker = new int[nTiles];
        }
            

        tilePkdPktHeaders = new byte[nPPTmarker[tile]][];

	// Lppt (marker length)
        curMarkSegLen = ehs.readUnsignedShort();

        // Zppt (index of PPM marker)
        indx = ehs.readUnsignedByte();
        
        // Ippt (packed packet headers)
        temp = new byte[curMarkSegLen-3];
        ehs.read(temp);
        tilePkdPktHeaders[indx]=temp;
 
        // Check marker length
        checkMarkerLength(ehs,"PPT marker");

        decSpec.pphs.setTileDef(tile, new Boolean(true));
        
        nReadPPTmarker[tile]++;

        // If this is the last PPT marker in the tilepart, copy packed
        // packet headers to the pkdPktHeaders buffer
        if(nReadPPTmarker[tile] == nPPTmarker[tile]){
            int i;

            if(pkdPktHeaders == null)
                pkdPktHeaders = new ByteArrayOutputStream[nTiles];

            if(pkdPktHeaders[tile] == null)
                pkdPktHeaders[tile] = new ByteArrayOutputStream();

            // Write all packed packet headers to pkdPktHeaders
            for(i=0 ; i<nPPTmarker[tile] ; i++){
                len += tilePkdPktHeaders[i].length;
                pkdPktHeaders[tile].write(tilePkdPktHeaders[i],0,
                                          tilePkdPktHeaders[i].length);
            }
        }
    }

    /** 
     * This method extract a marker segment in main header and stores it into
     * a byte buffer for the second pass. The marker segment is first
     * recognized, then its flag is activated and, finally, its content is
     * buffered in an element of byte arrays accessible thanks to a
     * hashTable. If a marker segment is not recognized, it prints a warning
     * and skip it according to its length. SIZ marker segment shall be the
     * first encountered marker segment.
     *
     * @param marker The marker to process
     *
     * @param ehs The encoded header stream
     * */
    private void extractMainMarkSeg(short marker, RandomAccessIO ehs) 
	throws IOException{
	if(markersFound == 0){ // First non-delimiting marker of the header
	    // JPEG 2000-part I specify that it must be SIZ
	    if(marker != SIZ)
		throw new CorruptedCodestreamException("First marker after "+
                                                       "SOC "+
                                                       "must be SIZ "+
                                                       Integer.
                                                       toHexString(marker));
	}

	String htKey=""; // Name used as a hash-table key

	switch(marker){
	case SIZ:
            if ((markersFound & SIZ_FOUND) != 0) {
                throw 
                  new CorruptedCodestreamException("More than one SIZ flag "+
                                                      "found in main header");
            }
            markersFound |= SIZ_FOUND;
	    htKey = "SIZ";
	    break;
	case SOD:
	    throw new CorruptedCodestreamException("SOD found in main header");
        case EOC:
            throw new CorruptedCodestreamException("EOC found in main header");
	case SOT:
            if ((markersFound & SOT_FOUND) != 0) {
                throw new CorruptedCodestreamException("More than one SOT "+
                                                       "marker "+
                                                       "found right after "+
                                                       "main "+
                                                       "or tile header");
            }
	    markersFound |= SOT_FOUND;
	    break;
	case COD:
	    if((markersFound & COD_FOUND) != 0) {
		throw new CorruptedCodestreamException("More than one COD "+
                                                      "marker "+
                                                      "found in main header");
	    }
            markersFound |= COD_FOUND;
	    htKey = "COD";
	    break;
	case COC:
	    markersFound |= COC_FOUND;
	    htKey = "COC"+(nCOCmarker++);
            break;
	case QCD:
	    if((markersFound & QCD_FOUND) != 0) {
		throw new CorruptedCodestreamException("More than one QCD "+
                                                      "marker "+
                                                      "found in main header");
	    }
	    markersFound |= QCD_FOUND;
	    htKey = "QCD";
	    break;
        case QCC:
            markersFound |= QCC_FOUND;
	    htKey = "QCC"+(nQCCmarker++);
            break;
	case RGN:
            markersFound |= RGN_FOUND;
	    htKey = "RGN"+(nRGNmarker++);
            break;
	case COM:
	    markersFound |= COM_FOUND;
	    htKey = "COM"+(nCOMmarker++);
	    break;
        case CRG:
            if((markersFound & CRG_FOUND) != 0) {
		throw new CorruptedCodestreamException("More than one CRG "+
                                                      "marker "+
                                                      "found in main header");
	    }
            markersFound |= CRG_FOUND;
            htKey = "CRG";
            break;
	case PPM:
	    markersFound |= PPM_FOUND;
	    htKey = "PPM"+(nPPMmarker++);
	    break;
	case TLM:
	    if((markersFound & TLM_FOUND) != 0) {
		throw new CorruptedCodestreamException("More than one TLM "+
                                                      "marker "+
                                                      "found in main header");
	    }
            markersFound |= TLM_FOUND;
	    break;
	case PLM:
	    if((markersFound & PLM_FOUND) != 0) {
		throw new CorruptedCodestreamException("More than one PLM "+
                                                      "marker "+
                                                      "found in main header");
	    }
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,"PLM marker segment found but "+
                         "not used by by JJ2000 decoder.");
	    markersFound |= PLM_FOUND;
	    htKey = "PLM";
	    break;
	case POC:
            if( (markersFound&POC_FOUND) != 0)
                throw new CorruptedCodestreamException("More than one POC "+
                                                       "marker segment found "+
                                                       "in main header");
            markersFound |= POC_FOUND;
            htKey = "POC";
            break;
	case PLT:
            throw new CorruptedCodestreamException("PPT found in main header");
	case PPT:
            throw new CorruptedCodestreamException("PPT found in main header");
	default:
            htKey = "UNKNOWN";
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,"Non recognized marker segment (0x"+
                         Integer.toHexString(marker)+") in main header!");
            break;
        }

        if(htKey.equals(""))
            return;

	// Read marker segment length and create corresponding byte buffer
	int markSegLen = ehs.readUnsignedShort();
	byte[] buf = new byte[markSegLen];

	// Copy data (after re-insertion of marker segment length);
	buf[0]= (byte)((markSegLen>>8) & 0xFF);
	buf[1]= (byte)(markSegLen & 0xFF);
	ehs.readFully(buf,2,markSegLen-2);

        if(!htKey.equals("UNKNOWN")){
            // Store array in hashTable
            ht.put(htKey,buf);
        }
    }

    /** 
     * This method extract a marker segment in a tile-part header and stores
     * it into a byte buffer for the second pass. The marker is first
     * recognized, then its flag is activated and, finally, its content is
     * buffered in an element of byte arrays accessible thanks to a hashTable.
     * If a marker segment is not recognized, it prints a warning and skip it
     * according to its length.
     *
     * @param marker The marker to process
     *
     * @param ehs The encoded header stream
     *
     * @param tileIdx The index of the current tile
     * */
    public void extractTilePartMarkSeg(short marker, RandomAccessIO ehs,
                                   int tileIdx)	throws IOException{
	String htKey=""; // Name used as a hash-table key

	switch(marker){
	case SIZ:
            throw new CorruptedCodestreamException("SIZ found in tile-part"+
                                                   " header");
        case EOC:
            throw new CorruptedCodestreamException("EOC found in tile-part"+
                                                   " header");
	case SOT:
	    throw new CorruptedCodestreamException("New SOT found in "+
                                                   "tile-part header");
	case TLM:
            throw new CorruptedCodestreamException("TLM found in tile-part"+
                                                   " header");
	case PLM:
            throw new CorruptedCodestreamException("PLM found in tile-part"+
                                                   " header");
	case PPM:
            throw new CorruptedCodestreamException("PPM found in tile-part"+
                                                   " header");
	case COD:
	    if((markersFound & COD_FOUND) != 0) {
		throw new CorruptedCodestreamException("More than one COD "+
                                                       "marker "+
                                                       "found in tile-part"+
                                                       " header");
	    }
            markersFound |= COD_FOUND;
	    htKey = "COD";
	    break;
	case COC:
	    markersFound |= COC_FOUND;
	    htKey = "COC"+(nCOCmarker++);
            break;
	case QCD:
	    if((markersFound & QCD_FOUND) != 0) {
		throw new CorruptedCodestreamException("More than one QCD "+
                                                       "marker "+
                                                       "found in tile-part"+
                                                       " header");
	    }
	    markersFound |= QCD_FOUND;
	    htKey = "QCD";
	    break;
        case QCC:
            markersFound |= QCC_FOUND;
	    htKey = "QCC"+(nQCCmarker++);
            break;
	case RGN:
            markersFound |= RGN_FOUND;
	    htKey = "RGN"+(nRGNmarker++);
            break;
	case COM:
	    markersFound |= COM_FOUND;
	    htKey = "COM"+(nCOMmarker++);
	    break;
        case CRG:
            throw new CorruptedCodestreamException("CRG marker found in "+
                                                   "tile-part header");
	case PPT:
	    markersFound |= PPT_FOUND;
            if(nPPTmarker == null){
                nPPTmarker = new int[nTiles];
            }
	    htKey = "PPT"+(nPPTmarker[tileIdx]++);
	    break;
	case SOD:
	    markersFound |= SOD_FOUND;
	    break;
	case POC:
            if( (markersFound&POC_FOUND) != 0)
                throw new CorruptedCodestreamException("More than one POC "+
                                                       "marker segment found "+
                                                       "in tile-part"+
                                                       " header");
            markersFound |= POC_FOUND;
            htKey = "POC";
            break;
	case PLT:
	    if((markersFound & PLM_FOUND) != 0) {
		throw new CorruptedCodestreamException("PLT marker found even"+
                                                       "though PLM marker "+
                                                       "found in main header");
            }
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,"PLT marker segment found but "+
                         "not used by JJ2000 decoder.");
	default:
            htKey = "UNKNOWN";
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,"Non recognized marker segment (0x"+
                         Integer.toHexString(marker)+") in tile-part header"+
                         " of tile "+tileIdx+" !");
            break;
        }

        if(htKey.equals(""))
            return;

	// Read marker segment length and create corresponding byte buffer
	int markSegLen = ehs.readUnsignedShort();
	byte[] buf = new byte[markSegLen];

	// Copy data (after re-insertion of marker segment length);
	buf[0]= (byte)((markSegLen>>8) & 0xFF);
	buf[1]= (byte)(markSegLen & 0xFF);
	ehs.readFully(buf,2,markSegLen-2);

        if(!htKey.equals("UNKNOWN")){
            // Store array in hashTable
            ht.put(htKey,buf);
        }
    }

    /** 
     * Retrieve and read all marker segments previously found in the main
     * header.
     * */
    private void readFoundMainMarkSeg() throws IOException{
	DataInputStream dis;
	ByteArrayInputStream bais;

        if(pl.getBooleanParameter("cdstr_info"))
	    printHeader = true;

	// SIZ marker segment
	if((markersFound&SIZ_FOUND) != 0){
	    bais = new ByteArrayInputStream( (byte[])(ht.get("SIZ")) );
	    readSIZ(new DataInputStream(bais));
	}

	// COM marker segments
	if((markersFound&COM_FOUND) != 0){
	    for(int i=0; i<nCOMmarker; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("COM"+i)) );
		readCOM(new DataInputStream(bais));
	    }
	}

        // CRG marker segment
        if((markersFound&CRG_FOUND) != 0){
            bais = new ByteArrayInputStream( (byte[])(ht.get("CRG")) );
            readCRG(new DataInputStream(bais));
        }

	// COD marker segment
	if((markersFound&COD_FOUND) != 0){
	    bais = new ByteArrayInputStream( (byte[])(ht.get("COD")) );
	    readCOD(new DataInputStream(bais),true, 0);
	}

	// COC marker segments
	if((markersFound&COC_FOUND) != 0){
	    for(int i=0; i<nCOCmarker; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("COC"+i)) );
		readCOC(new DataInputStream(bais), true, 0);
	    }
	}

	// RGN marker segment
	if((markersFound&RGN_FOUND) != 0){
            for(int i=0; i<nRGNmarker; i++){
                bais = new ByteArrayInputStream( (byte[])(ht.get("RGN"+i)) );
                readRGN(new DataInputStream(bais), true, 0);
            }
	}

	// QCD marker segment
	if((markersFound&QCD_FOUND) != 0){
	    bais = new ByteArrayInputStream( (byte[])(ht.get("QCD")) );
	    readQCD(new DataInputStream(bais),true, 0);
	}

	// QCC marker segments
	if((markersFound&QCC_FOUND) != 0){
	    for(int i=0;i<nQCCmarker; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("QCC"+i)) );
		readQCC(new DataInputStream(bais),true, 0);
	    }
	}
        
        // POC marker segment
        if( (markersFound&POC_FOUND) != 0){
            bais = new ByteArrayInputStream( (byte[])(ht.get("POC")));
            readPOC(new DataInputStream(bais),true,0);
        }
        
	// PPM marker segments
	if((markersFound&PPM_FOUND) != 0){
	    for(int i=0;i<nPPMmarker; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("PPM"+i)) );
		readPPM(new DataInputStream(bais));
	    }
	}

        if(printHeader){
	    String tmp = toString();
	    if(tmp!="")
		FacilityManager.getMsgLogger().
                    printmsg(MsgLogger.INFO,"Main Header:\n"+toString());
	    hdStr = "";
	}
    }

    /** 
     * Retrieve and read all marker segment previously found in the tile
     * header.
     *
     * @param tileIdx The index of the current tile
     * */
    public void readFoundTilePartMarkSeg(int tileIdx) throws IOException{
	DataInputStream dis;
	ByteArrayInputStream bais;

	// COD marker segment
	if((markersFound&COD_FOUND) != 0){
	    bais = new ByteArrayInputStream( (byte[])(ht.get("COD")) );
	    readCOD(new DataInputStream(bais), false, tileIdx);
	}

	// COC marker segments
	if((markersFound&COC_FOUND) != 0){
	    for(int i=0; i<nCOCmarker; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("COC"+i)) );
		readCOC(new DataInputStream(bais),false, tileIdx);
	    }
	}

	// RGN marker segment
	if((markersFound&RGN_FOUND) != 0){
            for(int i=0; i<nRGNmarker; i++){
                bais = new ByteArrayInputStream( (byte[])(ht.get("RGN"+i)) );
                readRGN(new DataInputStream(bais), false, tileIdx);
            }
	}

	// QCD marker segment
	if((markersFound&QCD_FOUND) != 0){
	    bais = new ByteArrayInputStream( (byte[])(ht.get("QCD")) );
	    readQCD(new DataInputStream(bais),false, tileIdx);
	}

	// QCC marker segments
	if((markersFound&QCC_FOUND) != 0){
	    for(int i=0;i<nQCCmarker; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("QCC"+i)) );
		readQCC(new DataInputStream(bais),false, tileIdx);
	    }
	}

        // POC marker segment
        if( (markersFound&POC_FOUND) != 0){
            bais = new ByteArrayInputStream( (byte[])(ht.get("POC")));
            readPOC(new DataInputStream(bais),false,tileIdx);
        }

	// COM marker segments
	if((markersFound&COM_FOUND) != 0){
	    for(int i=0; i<nCOMmarker; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("COM"+i)) );
		readCOM(new DataInputStream(bais));
            }
	}

	// PPT marker segments
	if((markersFound&PPT_FOUND) != 0){
	    for(int i=0;i<nPPTmarker[tileIdx]; i++){
		bais = new ByteArrayInputStream( (byte[])(ht.get("PPT"+i)) );
		readPPT(new DataInputStream(bais), tileIdx);
	    }
	}

        if(printHeader){
	    String tmp = toString();
	    if(tmp!="")
		FacilityManager.getMsgLogger().
                    printmsg(MsgLogger.INFO,"Tile Header ("+tileIdx+
                             "):\n"+toString());
	    hdStr = "";
	}
    }

    /** 
     * Return the DecoderSpecs instance filled when reading the headers
     * 
     * @return The DecoderSpecs of the decoder
     * */
    public DecoderSpecs getDecoderSpecs(){
	return decSpec;
    }

    /**
     * Creates a HeaderDecoder instance and read in two passes the main header
     * of the codestream. The first and last marker segments shall be
     * respectively SOC and SOT.
     *
     * @param ehs The encoded header stream where marker segment are
     * extracted.
     *
     * @param info If to print 'INFO' type information (warnings are always
     * written) to the message logging facility.
     *
     * @param pl The parameter list of the decoder
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoded header stream.
     *
     * @exception EOFException If the end of the encoded header stream is
     * reached before getting all the data.
     *
     * @exception CorruptedCodestreamException If invalid data is found in the
     * codestream main header.
     * */
    public HeaderDecoder(RandomAccessIO ehs,boolean info,ParameterList pl)
        throws IOException {

        int i,j;
        int tmp;
        this.pl = pl;
        this.printInfo = info;        
        
        pl.checkList(OPT_PREFIX,pl.toNameArray(pinfo));

        initPos = ehs.getPos();

        if( ((short)ehs.readShort())!=Markers.SOC )
            throw new CorruptedCodestreamException("SOC marker segment not "+
                                                   " found at the "+
                                                   "beginning of the "+
                                                   "codestream.");

	// Decode and store the main header (i.e. until a SOT marker segment
	// is found)
        do {
	    extractMainMarkSeg(ehs.readShort(),ehs);
        } while((markersFound & SOT_FOUND)==0); //Stop when SOT is found
	ehs.seek(ehs.getPos()-2); // Realign codestream on SOT marker

	// Read each marker segment previously found
	readFoundMainMarkSeg();

	thlen = ehs.getPos()-initPos; // length of main header
    }
    
    /**
     * Creates and returns the entropy decoder corresponding to the
     * information read from the codestream header and with the special
     * additional parameters from the parameter list.
     *
     * @param src The bit stream reader agent where to get code-block data
     * from.
     *
     * @param pl The parameter list containing parameters applicable to the
     * entropy decoder (other parameters can also be present).
     *
     * @return The entropy decoder
     * */
    public EntropyDecoder createEntropyDecoder(BitstreamReaderAgent src,
                                               ParameterList pl){
        boolean doer;
        boolean verber;
        // Check parameters
        pl.checkList(EntropyDecoder.OPT_PREFIX,
                     pl.toNameArray(EntropyDecoder.getParameterInfo()));
        // Get error detection option
        doer = pl.getBooleanParameter("Cer");
        // Get verbose error detection option
        verber = pl.getBooleanParameter("Cverber");
        return new StdEntropyDecoder(src, decSpec, doer, verber);
    }

    /**
     * Creates and returns the ROIDeScaler corresponding to the information
     * read from the codestream header and with the special additional
     * parameters from the parameter list.
     *
     * @param src The bit stream reader agent where to get code-block data
     * from.
     *
     * @param pl The parameter list containing parameters applicable to the
     * entropy decoder (other parameters can also be present).
     *
     * @return The ROI descaler
     * */
    public ROIDeScaler createROIDeScaler(CBlkQuantDataSrcDec src,
                                            ParameterList pl){
        return ROIDeScaler.createInstance(src, pl, decSpec);
    }

    /** 
     * Method that resets members indicating which markers have already 
     * been found 
     * */
    public void resetHeaderMarkers(){
        // The found satus of PLM remains since only PLM OR PLT allowed
        // Same goes for PPM and PPT
        markersFound = markersFound & (PLM_FOUND | PPM_FOUND);
        nCOCmarker = 0;
        nQCCmarker = 0;
        nCOMmarker = 0;
        nRGNmarker = 0;   
        nPPTmarker = new int[nTiles];
        nReadPPTmarker = new int[nTiles];
        ht = new Hashtable();
    }

    /** 
     * Print information about the current header.
     * 
     * @return Information in a String
     * */
    public String toString(){
        return hdStr;
    }
        
    /**
     * Returns the parameters that are used in this class. It returns a 2D
     * String array. Each of the 1D arrays is for a different option, and they
     * have 3 elements. The first element is the option name, the second one
     * is the synopsis and the third one is a long description of what the
     * parameter is. The synopsis or description may be 'null', in which case
     * it is assumed that there is no synopsis or description of the option,
     * respectively.
     *
     * @return the options name, their synopsis and their explanation.
     * */
    public static String[][] getParameterInfo() {
        return pinfo;
    }

    /** 
     * Return the number of tiles in the image
     *
     * @return The number of tiles
     * */
    public int getNumTiles(){
        return nTiles;
    }

    /**
     * Return the packed packet headers for a given tile 
     *
     * @return An input stream containing the packed packet headers for a
     * particular tile
     *
     * @exception IOException If an I/O error occurs while reading from the
     * encoder header stream
     * */
    public ByteArrayInputStream getPackedPktHead(int tile)
        throws IOException{
        ByteArrayOutputStream pph;
        
        if(nPPMmarker != 0){
            int i;
            int nTileParts = tileOfTileParts.size();
            byte[] temp;

            pph = new ByteArrayOutputStream();

            for(i=0; i<nTileParts ; i++){
                if(((Integer)
                    (tileOfTileParts.elementAt(i))).intValue() == tile){
                    temp = (byte[])unsortedPkdPktHeaders.elementAt(i);
                    pph.write(temp);
                }
            }
        }
        else pph = pkdPktHeaders[tile];

        return new ByteArrayInputStream(pph.toByteArray());
    }

    /**
     * Sets the tile of each tile part in order. This information is needed
     * for identifying which packet header belongs to which tile when using
     * the PPM marker
     *
     * @param tile The tile number that the present tile part belongs to
     * */
    public void setTileOfTileParts(int tile){
        if(nPPMmarker != 0)
            tileOfTileParts.addElement(new Integer(tile));

    }
} 
