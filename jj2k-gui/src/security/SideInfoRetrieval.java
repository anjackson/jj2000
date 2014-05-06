package security;

import jj2000.j2k.codestream.writer.*;
import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.entropy.decoder.*;
import jj2000.j2k.util.*;

import cryptix.provider.rsa.*;

import java.math.*;
import java.util.*;

/** Class to retrieve side information found in code-block's bit
 * stream (i.e. found after a stopping marker). Instances are used
 * between the codestream reading and the entropy decoding
 * modules.  */
public class SideInfoRetrieval extends MultiResImgDataAdapter
    implements CodedCBlkDataSrcDec {

    /** Number of code-blocks found so far with embedded
     * side-information */
    private int numProtectCblk = 0;
    
    /** Reference to the codestream reader */
    private CodedCBlkDataSrcDec src;

    /** Reference to the ParameterList instance */
    private ParameterList pl;

    /** RSA public exponent to decrypt seed values */
    private BigInteger rsaExp;
    /** RSA public modulus to decrypt seed values */
    private BigInteger rsaMod;

    /** Whether or not current seed is encrypted */
    private boolean isEncrypted = false;

    public SideInfoRetrieval(CodedCBlkDataSrcDec src,ParameterList pl) {
	super(src);
	this.src = src;
	this.pl = pl;
	String str = pl.getParameter("Spublic_key");
	if(str!=null) {
	    StringTokenizer stk = new StringTokenizer(str);
	    if(stk.countTokens()==2) {
		rsaExp = new BigInteger(stk.nextToken());
		rsaMod = new BigInteger(stk.nextToken());
	    }
	}
    }

    /** Get a code-block's codewords from the codestream reader and
     * parse them to find eventual embedded side-information. */
    public DecLyrdCBlk getCodeBlock(int c,int m,int n,SubbandSyn sb,int fl,
				    int nl,DecLyrdCBlk ccb) {
	DecLyrdCBlk dlCblk = src.getCodeBlock(c,m,n,sb,fl,nl,ccb);
	
	if(dlCblk==null) return null;

	// Get codewords array
	byte[] ldata = dlCblk.data;
	int len = dlCblk.dl;

	// Search for a terminating marker and display found information if any
	int ff = 0xFF;
	int n0 = 0x90;
	boolean found = false;
	int markLen = 0;
	int markOff = 0;
	for(int i=0; i<len-1; i++) {
	    if((ff&ldata[i])==ff) {
		if((ff&ldata[i+1])>=n0) {
		    if(ldata[i+1]==(byte)0x94) { // Secure termination
			markOff = i;
			i+=2;

			// Marker length
			markLen = ((ldata[i]<<8)&0xFF00) + (ldata[i+1]&0xFF);
			i += 2;

			// Method type
			if((ldata[i]&0xF)==0x1) {
			    dlCblk.scramblingType = 
				ScramblingTypes.WAV_DOMAIN_SCRAMBLING;
			} else if((ldata[i]&0xF)==0x2){
			    dlCblk.scramblingType = 
				ScramblingTypes.COMP_DOMAIN_SCRAMBLING;
			} else {
			    dlCblk.scrambled = false;
			    dlCblk.scramblingType = 
				ScramblingTypes.NO_SCRAMBLING;
			    continue;
			}
			// Encryption method
			if((ldata[i]&0xF0)==0x0) {
			    isEncrypted = false;
			} else {
			    isEncrypted = true;
			}
			i += 1;

			// PPsee
			if(dlCblk.scramblingType==
					ScramblingTypes.
					COMP_DOMAIN_SCRAMBLING) {
			    dlCblk.scrambOff = ((ldata[i]<<8)&0xFF00) + 
				(ldata[i+1]&0xFF);
			    i += 2;
			}

			// Seed
			if(isEncrypted) {
			    byte[] val = new byte[markLen-i+markOff];
			    int p = 0;
			    for(; i<markLen+markOff; i++,p++) {
				val[p] = ldata[i];
			    }
			    BigInteger eSeed = new BigInteger(val);
			    BigInteger unSeed = RSAAlgorithm.rsa(eSeed,
								 rsaMod,
								 rsaExp);
			    dlCblk.seed = unSeed.longValue();
			} else {
			    dlCblk.seed = ((ldata[i]<<32)&0xFF00) + 
				((ldata[i+1]<<16)&0xFF00) +  
				((ldata[i+2]<<8)&0xFF00) +
				(ldata[i+3]&0xFF);
			    i += 4;
			}

			dlCblk.scrambled = true;
			numProtectCblk++;
			found = true;

			break;
		    } else {
			dlCblk.scrambled = false;
			dlCblk.scramblingType = ScramblingTypes.NO_SCRAMBLING;
		    }
		}
	    }
	}
	// No secure termination found. Code-block is not protected
	if(!found) {
	    dlCblk.scrambled = false;
	    dlCblk.scramblingType = ScramblingTypes.NO_SCRAMBLING;
	}

	// Unscramble layers if needed and authorized user
	if(dlCblk.scrambled && (dlCblk.scramblingType ==
				ScramblingTypes.COMP_DOMAIN_SCRAMBLING)) {
	    Random rd = new Random(dlCblk.seed);

	    // Bit reader from byte buffer
	    ByteInputBuffer bib = 
		new ByteInputBuffer(dlCblk.data,dlCblk.scrambOff,
				    markOff-dlCblk.scrambOff);
	    ByteToBitInput bbi = new ByteToBitInput(bib);

	    // Copy input to output
	    int tmpIn, tmpOut, tmp;
	    byte[] out = new byte[markOff-dlCblk.scrambOff];
// 	    System.out.println(" -> length="+out.length);
	    for(int i=dlCblk.scrambOff; i<markOff; i++) {
		tmpOut = 0;
		for(int j=7; j>=0; j--) {
		    tmpIn = bbi.readBit();
		    if(rd.nextBoolean()) {
			tmpOut |= (1-tmpIn)<<j;
		    } else {
			tmpOut |= tmpIn<<j;
		    }
		}
		out[i-dlCblk.scrambOff] = (byte)(0xff&tmpOut);
	    }
	    
	    // Recopy buffer
	    for(int i=dlCblk.scrambOff; i<markOff; i++) {
		dlCblk.data[i] = out[i-dlCblk.scrambOff];
	    }
	}
	

	return dlCblk;
    }

    public SubbandSyn getSynSubbandTree(int t,int c) {
	return src.getSynSubbandTree(t,c);
    }

    public int getCbULX() {
	return src.getCbULX();
    }

    public int getCbULY() {
	return src.getCbULY();
    }
}
