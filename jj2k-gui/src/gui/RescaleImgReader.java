package gui;

import jj2000.j2k.image.input.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;

/** Class to display pgx files with signed data or bit-depth greater
 * than 8 bits. The data is rescaled in order to fit in the range
 * 0-255. */
public class RescaleImgReader extends ImgDataAdapter implements BlkImgDataSrc {
    
    /** Reference to the image reader instance */
    private ImgReader src;

    /** Flag indicating that source needs no rescaling */
    private final static int NO_RESCALE = 0;

    /** Flag indicating that source needs rescaling */
    private final static int RESCALING = 1;

    /** State of the rescaler */
    private int rescaleMode = NO_RESCALE;

    /** Whether or not input components are signed */
    private boolean[] isSigned;

    /** Bit-depth of each component */
    private int[] bitDepth;

    /** Check if input image reader needs rescaling. Note: an image
     * reader needs rescaling if data are signed and/or component
     * bit-depth is larger than 8bpp. */
    public RescaleImgReader(ImgReader src) {
	super(src);
	this.src = src;

	int nc = src.getNumComps();
	isSigned = new boolean[nc];
	bitDepth = new int[nc];
	for(int c=0; c<nc; c++) {
	    isSigned[c] = src.isOrigSigned(c);
	    bitDepth[c] = src.getNomRangeBits(c);
	    if(isSigned[c] || bitDepth[c]>8) {
		rescaleMode = RESCALING;
	    }
	}
    }

    public int getFixedPoint(int c) {
	return 0;
    } 

    public DataBlk getInternCompData(DataBlk blk,int c) {
	if(rescaleMode==NO_RESCALE) {
	    return src.getInternCompData(blk,c);
	} else {
	    return rescaleBlk(src.getInternCompData(blk,c),c);
	}
    }

    public DataBlk getCompData(DataBlk blk,int c) {
	if(rescaleMode==NO_RESCALE) {
	    return src.getCompData(blk,c);
	} else {
	    return rescaleBlk(src.getCompData(blk,c),c);
	}
    }

    public int getNomRangeBits(int c) {
	return 8;
    }
    
    public boolean isOrigSigned(int c) {
	return false;
    }

    private DataBlk rescaleBlk(DataBlk blk,int c) {
	int dataType = blk.getDataType();

	switch(dataType) {
	case DataBlk.TYPE_INT:
	    int[] dataI = (int[])blk.getData();
	    int maxI = (isSigned[c] ? 1<<(bitDepth[c]-1) : 1<<bitDepth[c])-1;
	    int minI = isSigned[c] ? -(1<<(bitDepth[c]-1)) : 0;
	    int rg = maxI-minI;
	    int levShift = isSigned[c] ? 0 : 1<<(bitDepth[c]-1);

	    int pos = blk.offset;
	    for(int i=0; i<blk.h; i++, pos+=blk.scanw-blk.w) {
		for(int j=0; j<blk.w; j++,pos++) {
		    dataI[pos] = ((dataI[pos]+levShift-minI)*255/rg)-128;
		}
	    }
	    break;
	case DataBlk.TYPE_FLOAT:
	    float[] dataF = (float[])blk.getData();
	    float maxF = (isSigned[c] ? 1<<(bitDepth[c]-1) : 1<<bitDepth[c])-1;
	    float minF = isSigned[c] ? -(1<<(bitDepth[c]-1)) : 0;
	    float rgF = maxF-minF;
	    float levShiftF = isSigned[c] ? 0 : 1<<(bitDepth[c]-1);
	    int pos2 = blk.offset;
	    for(int i=0; i<blk.h; i++, pos2+=blk.scanw-blk.w) {
		for(int j=0; j<blk.w; j++,pos2++) {
		    dataF[pos2] = ((dataF[pos2]+levShiftF-minF)*255/rgF)-128;
		}
	    }
	    break;
	default:
	    throw new IllegalArgumentException("Unsupported data type");
	}
	return blk;
    }
}
