package security;

import jj2000.j2k.quantization.quantizer.*;
import jj2000.j2k.wavelet.analysis.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;

import java.util.*;

/** Class to scramble code-blocks in the wavelet domain */
public class Scrambling extends ImgDataAdapter 
    implements CBlkQuantDataSrcEnc, ScramblingTypes {

    /** The prefix for wavelet scrambling options: 'S' */
    public final static char OPT_PREFIX = 'S';

    /** Reference to the ParameterList instance */
    private ParameterList pl;

    /** Minimum length of side-info to append when wavelet scrambling
     * of the code-block (Stopping marker: 2, marker length: 2, Psee:
     * 1) */
    public static final int MIN_SIDE_INFO_LEN = 5;

    /** First resolution level to apply the securing process */
    private int lvlStart = -1;

    /** Reference to the source module */
    private CBlkQuantDataSrcEnc src;

    /** Type of shape to secure */
    private int shapeType = NONE;
    /** None to secure */
    private static final int NONE = 0;
    /** The whole image has to be secure */
    private static final int WHOLE = 1;
    /** Only the specified rectangular region has to be secured */
    private static final int RECTANGLE = 2;

    /** Scrambling method type */
    private int methodType = WAV_DOMAIN_SCRAMBLING;

    /** Position and dimension of the rectangular area to secure */
    private int ulx,uly,lrx,lry;

    /** The list of parameters that is accepted for wavelet
     * scrambling. Options for wavelet scrambling start with 'S'. */
    private final static String [][] pinfo = {
	{ "Sseed", "<seed value>",
	  "Specifies the non-encrypted seed to be used for the scrambling "+
	  "operation.", "16449"},
	{ "Sprivate_key", "<exponent> <modulus>",
	  "Exponent and modulus of the RSA private key to use for seeds "+
	  "encryption. If exponent and modulus are both equal to -1, no "+
	  "encryption will be applied.", "-1 -1"},
	{ "Sshape", "[<x> <y> <w> <h>] | none | all",
	  "Specifies the position and the dimension of the area to secure. "+
	  "The coordinates are relative to the canvas coordinate system. "+
	  "If equals to 'none', no region will be secure. If equals to all,"+
	  " the whole image will be secure.", "none"},
	{ "Slevel_start", "<level index>",
	  "The index of the resolution level to start the securing process. "+
	  "If equals to -1, all the levels will be secured", "-1"},
	{ "Smethod", "[wavelet | layer]",
	  "Scrambling method to use: Currently either in the wavelet domain"+
	  " or at layers level.", "wavelet"},
	{ "Srate", "<bit-rate>",
	  "Bit rate from which layers are scrambled. This option is "+
	  "taken into account only if 'Smethod' equals 'layer'.", "100"}
    };

    /** Class constructor */
    public Scrambling(CBlkQuantDataSrcEnc src, ParameterList pl) {
	super(src);
	this.src = src;
	this.pl = pl;

	// Shape of the area to secure
	if(pl.getParameter("Sshape").equalsIgnoreCase("none")) {
	    shapeType = NONE;
	} else if(pl.getParameter("Sshape").equalsIgnoreCase("all")) {
	    shapeType = WHOLE;
	} else {
	    shapeType = RECTANGLE;
	    String str = pl.getParameter("Sshape");
	    StringTokenizer stk = new StringTokenizer(str);
	    if(stk.countTokens()!=4) {
		throw new IllegalArgumentException("Invalid number of "+
						   "arguments for the "+
						   "rectangular area to "+
						   "secure.");
	    }
	    ulx = (new Integer(stk.nextToken())).intValue();
	    uly = (new Integer(stk.nextToken())).intValue();
	    lrx = ulx+ (new Integer(stk.nextToken())).intValue() -1;
	    lry = uly + (new Integer(stk.nextToken())).intValue() -1;
	}
	// Resolution level start index
	lvlStart = pl.getIntParameter("Slevel_start");

	// Scrambling method type
	if(pl.getParameter("Smethod").equalsIgnoreCase("wavelet")) {
	    methodType = WAV_DOMAIN_SCRAMBLING;
	} else if(pl.getParameter("Smethod").equalsIgnoreCase("layer")) {
	    methodType = COMP_DOMAIN_SCRAMBLING;
	}

	// Check if seed is valid
	int seed = pl.getIntParameter("Sseed");
    }

    /** Returns the array of supported options for wavelet scrambling */
    public static String[][] getParameterInfo() {
        return pinfo;
    }

    /** Request a <i>scrambled</i> (or not) code-block */
    public CBlkWTData getNextCodeBlock(int c,CBlkWTData cblk) {
	return processCBlk(src.getNextCodeBlock(c,cblk));
    }

    /** Request a <i>scrambled</i> (or not) code-block */
    public CBlkWTData getNextInternCodeBlock(int c,CBlkWTData cblk) {
	return processCBlk(src.getNextInternCodeBlock(c,cblk));
    }

    private CBlkWTData processCBlk(CBlkWTData srcblk) {
	if(srcblk==null) return null;

 	if(isCblkToSecure(srcblk)) {
	    srcblk.scrambled = true;
	    srcblk.seed = pl.getIntParameter("Sseed");
	    if(methodType==WAV_DOMAIN_SCRAMBLING) {
		srcblk.scramblingType = WAV_DOMAIN_SCRAMBLING;
	    } else {
		srcblk.scramblingType = COMP_DOMAIN_SCRAMBLING;
		return srcblk;
	    }

	    // Pseudo-random inversion of wavelet coefficients signs
	    // (if needed)
	    Random rand = new Random(srcblk.seed);
	    int w = srcblk.w;
	    int h = srcblk.h;
	    
	    int p = srcblk.offset;
	    int[] data = (int[])srcblk.getData();
	    int setPos = ~(1<<31);
	    int setNeg = 1<<31;
	    
	    for(int k=0; k<h; k++,p+=srcblk.scanw-w) { // Code-block's rows
		for(int l=0; l<w; l++,p++) { // Code-block's columns
		    if(rand.nextBoolean()) {
			if(data[p]<=0) {
			    data[p] &= setPos;
			} else {
			    data[p] |= setNeg;
			}
		    }
		} // Code-block's columns
	    } // Code-block's rows
	} else {
	    srcblk.scrambled = false;
	    srcblk.scramblingType = ScramblingTypes.NO_SCRAMBLING;
	    srcblk.seed = 0;
	}
	return srcblk;
    }

    /** Check if specified code-block has to be secure or not
     * (depending on its influencing zone in the spatial domain). For
     * simplicity, the limits of a code-block influence zone are
     * determined as if synthesis wavelet filters have their lengths
     * equal to 1. */
    private boolean isCblkToSecure(CBlkWTData srcblk) {
	if(srcblk==null) return false;

	SubbandAn sb = srcblk.sb;
	int r = sb.resLvl;
	int s = sb.sbandIdx;

	if(shapeType==NONE) return false;
	if(shapeType==WHOLE) {
	    if(r<lvlStart) {
		return false;
	    } else {
		return true;
	    }
	}

	// Current resolution level has not to be secure
	if(r<lvlStart) return false;

	// Code-block position in subband
	int culx = srcblk.ulx - sb.ulx;
	int culy = srcblk.uly - sb.uly;
	int clrx = culx + srcblk.w -1;
	int clry = culy + srcblk.h -1;

	// Projected coordinates in the spatial domain
	int pulx = 0;
	int puly = 0;
	int plrx = 0;
	int plry = 0;
	if(s==0) {
	    pulx = culx;
	    puly = culy;
	    plrx = clrx;
	    plry = clry;	    
	} else if(s==1) {
	    pulx = culx+1;
	    puly = culy;
	    plrx = clrx+1;
	    plry = clry;
	} else if(s==2) {
	    pulx = culx;
	    puly = culy+1;
	    plrx = clrx;
	    plry = clry+1;
	} else if(s==3) {
	    pulx = culx+1;
	    puly = culy+1;
	    plrx = clrx+1;
	    plry = clry+1;	    
	}
	int lvl = sb.level;
	while(lvl>0) {
	    pulx <<= 1;
	    puly <<= 1;
	    plrx <<= 1;
	    plry <<= 1;
	    lvl--;
	}

	if((plrx>=ulx && pulx<=lrx && plry>=uly && puly<=lry)) {
	    // Code-block influence to the rectangular zone to secure
	    return true;
	} else {
	    return false;
	}
    }

    /** Whether or not specified tile-component is reversible (i.e. no
     * quantization and reversible wavelet transform) */
    public boolean isReversible(int t,int c) {
	return src.isReversible(t,c);
    }

    /** Returns the wavelet subband tree associated to the specified
     * tile-component. */
    public SubbandAn getAnSubbandTree(int t,int c) {
	return src.getAnSubbandTree(t,c);
    }

    /** Code-blocks horizontal offset in canvas */
    public int getCbULX() {
	return src.getCbULX();
    }

    /** Code-blocks vertical offset in canvas */
    public int getCbULY() {
	return src.getCbULY();
    }
}
