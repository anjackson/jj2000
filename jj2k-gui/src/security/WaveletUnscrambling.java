package security;

import jj2000.j2k.quantization.dequantizer.*;
import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;

import java.util.*;

public class WaveletUnscrambling extends MultiResImgDataAdapter
    implements CBlkQuantDataSrcDec {

    /** The prefix for wavelet unscrambling options: 'S' */
    public final static char OPT_PREFIX = 'S';

    /** Reference to the ParameterList instance */
    private ParameterList pl;

    /** Reference to the source module */
    private CBlkQuantDataSrcDec src;

    /** Public key to decrypt seed values */
    private int pubKey = -1;

    /** The list of parameters that is accepted for wavelet
     * scrambling. Options for wavelet scrambling start with 'S'. */
    private final static String [][] pinfo = {
	{ "Spublic_key", "<value>",
	  "Public key to decrypt the seed values found in the codestream.", 
	  "-1"},
    };

    /** Class constructor */
    public WaveletUnscrambling(CBlkQuantDataSrcDec src, ParameterList pl) {
	super(src);
	this.src = src;
	this.pl = pl;
	pubKey = pl.getIntParameter("Spublic_key");
    }

    /** Returns the array of supported options for wavelet scrambling */
    public static String[][] getParameterInfo() {
        return pinfo;
    }

    public DataBlk getCodeBlock(int c,int m,int n,SubbandSyn sb,
				DataBlk cblk) {
	return processCBlk(src.getCodeBlock(c,m,n,sb,cblk));
    }

    public DataBlk getInternCodeBlock(int c,int m,int n,SubbandSyn sb,
				      DataBlk cblk) {
	return processCBlk(src.getInternCodeBlock(c,m,n,sb,cblk));
    }

    /** Returns the subband tree of the specified tile-component */
    public SubbandSyn getSynSubbandTree(int t,int c) {
	return src.getSynSubbandTree(t,c);
    }

    /** Unscramble the sign bit-plane if needed */
    private DataBlk processCBlk(DataBlk srcblk) {
	if(srcblk.scrambled && 
	   srcblk.scramblingType==ScramblingTypes.WAV_DOMAIN_SCRAMBLING &&
	   pubKey != -1) {
	    
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
	    
	}
	return srcblk;
    }

    /** Returns the code-blocks horizontal offset in the canvas */
    public int getCbULX() {
	return src.getCbULX();
    }

    /** Returns the code-blocks vertical offset in the canvas */
    public int getCbULY() {
	return src.getCbULY();
    }
}
