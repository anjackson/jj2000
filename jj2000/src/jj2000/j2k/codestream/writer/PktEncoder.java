/*
 * CVS identifier:
 *
 * $Id: PktEncoder.java,v 1.23 2001/03/01 17:00:44 grosbois Exp $
 *
 * Class:                   PktEncoder
 *
 * Description:             Builds bit stream packets and keeps
 *                          interpacket dependencies.
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
package jj2000.j2k.codestream.writer;

import jj2000.j2k.wavelet.analysis.*;
import jj2000.j2k.entropy.encoder.*;
import jj2000.j2k.codestream.*;
import jj2000.j2k.encoder.*;
import jj2000.j2k.wavelet.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.*;

import java.util.Vector;

/**
 * This class builds packets and keeps the state information of packet
 * interdependencies. It also supports saving the state and reverting
 * (restoring) to the last saved state, with the save() and restore() methods.
 *
 * <P>Each time the encodePacket() method is called a new packet is encoded,
 * the packet header is returned by the method, and the packet body can be
 * obtained with the getLastBodyBuf() and getLastBodyLen() methods.
 * */
public class PktEncoder {
    
    /** The prefix for packet encoding options: 'P' */
    public final static char OPT_PREFIX = 'P';

    /** The list of parameters that is accepted for packet encoding.*/
    private final static String [][] pinfo = {
        { "Psop", "[<tile idx>] on|off"+
         "[ [<tile idx>] on|off ...]",
          "Specifies whether start of packet (SOP) markers should be used. "+
          "'on' enables, 'off' disables it.","off"},
        { "Peph", "[<tile idx>] on|off"+
         "[ [<tile  idx>] on|off ...]",
          "Specifies whether end of packet header (EPH) markers should be "+
          " used. 'on' enables, 'off' disables it.","off"}
    };

    /** The initial value for the lblock */
    private final static int INIT_LBLOCK = 3;
    
    /** The source object */
    private CodedCBlkDataSrcEnc infoSrc;
    
    /** The encoder specs */
    EncoderSpecs encSpec;

    /**
     * The tag tree for inclusion information. The indexes are outlined
     * below. Note that the layer indexes start at 1, therefore, the layer
     * index minus 1 is used. The subband indices are used as they are defined
     * in the Subband class. The tile indices start at 0 and follow a
     * lexicographical order.
     *
     * <ul>
     * <li>1st index: tile index, in lexicographical order</li>
     * <li>2nd index: component index </li>
     * <li>3rd index: resolution level </li>
     * <li>4th index: subband index </li>
     * <li>5th index: precinct index </li>
     * </ul>
     **/
    private TagTreeEncoder ttIncl[][][][][];

    /**
     * The tag tree for the maximum significant bit-plane. The indexes are
     * outlined below. Note that the layer indexes start at 1, therefore, the
     * layer index minus 1 is used. The subband indices are used as they are
     * defined in the Subband class. The tile indices start at 0 and follow a
     * lexicographical order.
     *
     * <ul>
     * <li>1st index: tile index, in lexicographical order</li>
     * <li>2nd index: component index </li>
     * <li>3rd index: resolution level </li>
     * <li>4th index: subband index - subband index offset </li>
     * <li>5th index: precinct index </li>
     * </ul>
     * */
    private TagTreeEncoder ttMaxBP[][][][][];

    /**
     * The base number of bits for sending code-block length information
     * (referred as Lblock in the JPEG 2000 standard). The indexes are
     * outlined below. Note that the layer indexes start at 1, therefore, the
     * layer index minus 1 is used. The subband indices are used as they are
     * defined in the Subband class. The tile indices start at 0 and follow a
     * lexicographical order.
     *
     * <ul>
     * <li>1st index: tile index, in lexicographical order </li>
     * <li>2nd index: component index </li>
     * <li>3rd index: resolution level </li>
     * <li>4th index: subband index - subband index offset </li>
     * <li>5th index: code-block index, in lexicographical order</li>
     * </ul>
     * */
    private int lblock[][][][][];

    /**
     * The last encoded truncation point for each code-block. A negative value
     * means that no information has been included for the block, yet. The
     * indexes are outlined below. The subband indices are used as they are
     * defined in the Subband class. The tile indices start at 0 and follow a
     * lexicographical order. The code-block indices follow a lexicographical
     * order within the subband tile.
     *
     * <P>What is actually stored is the index of the element in
     * CBlkRateDistStats.truncIdxs that gives the real truncation point.
     *
     * <ul>
     * <li>1st index: tile index, in lexicographical order </li>
     * <li>2nd index: component index </li>
     * <li>3rd index: resolution level </li>
     * <li>4th index: subband index - subband index offset </li>
     * <li>5th index: code-block index, in lexicographical order </li>
     * </ul>
     *  */
    private int prevtIdxs[][][][][];

    /**
     * The saved base number of bits for sending code-block length
     * information. It is used for restoring previous saved state by
     * restore(). The indexes are outlined below. Note that the layer indexes
     * start at 1, therefore, the layer index minus 1 is used. The subband
     * indices are used as they are defined in the Subband class. The tile
     * indices start at 0 and follow a lexicographical order.
     * 
     * <ul> 
     * <li>1st index: tile index, in lexicographical order </li>
     * <li>2nd index: component index </li>
     * <li>3rd index: resolution level </li>
     * <li>4th index: subband index - subband index offset </li>
     * <li>5th index: code-block index, in lexicographical order</li>
     * </ul>
     * */
    private int bak_lblock[][][][][];

    /**
     * The saved last encoded truncation point for each code-block. It is used
     * for restoring previous saved state by restore(). A negative value means
     * that no information has been included for the block, yet. The indexes
     * are outlined below. The subband indices are used as they are defined in
     * the Subband class. The tile indices start at 0 and follow a
     * lexicographical order. The code-block indices follow a lexicographical
     * order within the subband tile.
     *
     * <ul>
     * <li>1st index: tile index, in lexicographical order </li>
     * <li>2nd index: component index </li>
     * <li>3rd index: resolution level </li>
     * <li>4th index: subband index - subband index offset </li>
     * <li>5th index: code-block index, in lexicographical order </li>
     * </ul>
     *  */
    private int bak_prevtIdxs[][][][][];

    /** The body buffer of the last encoded packet */
    private byte[] lbbuf;

    /** The body length of the last encoded packet */
    private int lblen;

    /** The saved state */
    private boolean saved;

    /** Whether or not there is ROI information in the last encoded Packet */
    private boolean roiInPkt = false;
    
    /** Length to read in current packet body to get all the ROI information */
    private int roiLen = 0;

    /**
     *  Array containing the coordinates, width, height, indexes, ... of the
     *  precincts
     *
     * <ul>
     * <li> 1st dim: tile index.</li>
     * <li> 2nd dim: component index.</li>
     * <li> 3nd dim: resolution level index.</li>
     * <li> 4th dim: subband index.</li>
     * <li> 5th dim: precinct index.</li>
     * </ul> 
     * */
    private PrecCoordInfo precArrayI[][][][][];
    
    /**
     *  Array containing the coordinates, width, height, indexes, ... of the
     *  code-blocks
     *
     * <ul>
     * <li> 1st dim: tile index.</li>
     * <li> 2nd dim: component index.</li>
     * <li> 3nd dim: resolution level index.</li>
     * <li> 4th dim: subband index.</li>
     * <li> 5th dim: code-block index.</li>
     * </ul> */
    private CBlkCoordInfo cbArrayI[][][][][];
    
    /** 
     * Maximum number of precincts in each resolution level :
     *
     * <ul>
     * <li> 1st dim: tile index.</li>
     * <li> 2nd dim: component index.</li>
     * <li> 3nd dim: resolution level index.</li>
     * </ul>
     */
    private Coord maxNumPrec[][][];
    
    /** 
     * Array containing the increment step
     *
     * <ul>
     * <li> 1st dim : tile index</li>
     * <li> 2nd dim : component index</li>
     * <li> 3rd dim : resolution level</li>
     * </ul>
     */
    private Coord incArray[][][];
    
    /** 
     * Array containing the maximum increment step
     *
     * <ul>
     * <li> 1st dim : tile index</li>
     * <li> 2nd dim : component index</li>
     * </ul>
     * */
    private Coord incArrayMax[][];
    
    /** 
     * Array used to store the start/end of tile horizontal and vertical
     * coordinates at each resolution level
     *
     * <ul>
     * <li> 1st dim : tile index</li>
     * <li> 2nd dim : component index</li>
     * <li> 3rd dim : resolution level</li>
     * <li> 4th dim : 0, start of tile, 1, end of tile</li>
     * </ul>
     * */
    private Coord sot_eotArray[][][][];
    
    /** 
     * Array used to store the start/end of tile horizontal and vertical
     * coordinates at the highest resolution level using the smallest
     * increments
     * 
     * <ul>
     * <li> 1st dim : tile index</li>
     * <li> 2nd dim : component index</li>
     * <li> 3rd dim : 0, start of tile, 1, end of tile</li>
     * </ul>
     * */
    private Coord sot_eotArrayMax[][][]; 
    
    /** 
     * The maximum number of resolution level for each component and each tile
     * */
    int mrl[][];    
    
    /** 
     * Keeps the first and the last subband index in the associated resolution
     * level for each tile and each component
     * */
    private int[][][][] subRange;     
    
    /** Whether or not the current packet is writable */
    private boolean packetWritable;
    
    /** 
     * Backup of the subband tree for each tile and component as they are used
     * quite often.
     * */
    private SubbandAn savedSbTree[][];
    
    /**
     * Creates a new packet header encoder, using the information from the
     * 'infoSrc' object. The information used is the number of components,
     * number of tiles, subband decomposition, etc.
     *
     * <P>Note that this constructor visits all the tiles in the 'infoSrc'
     * object. The 'infoSrc' object is left at the original tile (i.e. the
     * current tile before calling this constructor), but any side effects of
     * visiting the tiles is not reverted.
     *
     * @param infoSrc The source of information to construct the
     * object.
     *     
     * @param encSpec The parameters for the encoding
     *
     * @param maxNumPrec Maximum number of precinct in each tile, component
     * and resolution level.
     *
     * @param pl ParameterList instance that holds command line options
     * */
    public PktEncoder(CodedCBlkDataSrcEnc infoSrc, EncoderSpecs encSpec,
                      Coord[][][] maxNumPrec, ParameterList pl) {
        SubbandAn sb,sb2;
            
        Coord orTCoord;  // Original tile X and Y
        Coord tmpCoord = null;
        int numcb;       // Number of code-blocks
        Vector cblks = null;

        // Check parameters
        pl.checkList(OPT_PREFIX,pl.toNameArray(pinfo));

        // Parse the Psop option i.e. whether or not start of packet
        // (SOP) markers should be used.
        String sopopt = pl.getParameter("Psop");
        if (sopopt == null) {
            throw new IllegalArgumentException("Missing option 'Psop'");
        }

        // Parse the Peph option i.e. whether or not end of packet
        // headers (EPH) markers should be used.
        String ephopt = pl.getParameter("Peph");
        if (ephopt == null) {
            throw new IllegalArgumentException("Missing option 'Peph'");
        }

        this.infoSrc = infoSrc;
        this.encSpec = encSpec;
        this.maxNumPrec = maxNumPrec;
        
        // Save current tile indices
        orTCoord = infoSrc.getTile(null);

        // Get number of components and tiles
        int ncomp = infoSrc.getNumComps();
        int ntiles = infoSrc.getNumTiles();

        // Do initial allocation
        ttIncl = new TagTreeEncoder[ntiles][ncomp][][][];
        ttMaxBP = new TagTreeEncoder[ntiles][ncomp][][][];
        lblock = new int[ntiles][ncomp][][][];
        prevtIdxs = new int[ntiles][ncomp][][][];

        cbArrayI = new CBlkCoordInfo[ntiles][ncomp][][][];
        precArrayI = new PrecCoordInfo[ntiles][ncomp][][][];
        incArray    = new Coord[ntiles][ncomp][];
        incArrayMax = new Coord[ntiles][ncomp];
        sot_eotArray    = new Coord[ntiles][ncomp][][];
        sot_eotArrayMax = new Coord[ntiles][ncomp][2];
        mrl = new int[ntiles][ncomp];
        subRange = new int[ntiles][ncomp][][];
        savedSbTree = new SubbandAn[ntiles][ncomp];

        // Goto first tile
        infoSrc.setTile(0,0);

        // Finish allocation
        for(int t=0; t<ntiles; t++){ // Loop on tiles
            for(int c=0; c<ncomp; c++){ // Loop on components
                // Get number of resolution levels
                savedSbTree[t][c] = infoSrc.getSubbandTree(t,c);
                sb = savedSbTree[t][c];
                mrl[t][c] = sb.resLvl;
                
                subRange[t][c] = findSubInResLvl(c,t);
                
                incArray[t][c]  = new Coord[mrl[t][c]+1];
                sot_eotArray[t][c] =new Coord[mrl[t][c]+1][2];
                
                // Create and initialise the increment arrays
                buildIncArrays(c,t);
                // Initialise the start/end of tile arrays
                buildSotEotArrays(c,t);

                lblock[t][c] = new int[mrl[t][c]+1][][];
                ttIncl[t][c] = new TagTreeEncoder[mrl[t][c]+1][][];
                ttMaxBP[t][c] = new TagTreeEncoder[mrl[t][c]+1][][];
                prevtIdxs[t][c] = new int[mrl[t][c]+1][][];
                
                cbArrayI[t][c] = new CBlkCoordInfo[mrl[t][c]+1][][];
                precArrayI[t][c] = new PrecCoordInfo[mrl[t][c]+1][][];
                
                for(int r=mrl[t][c]; r>=0; r--){ // Loop on resolution levels
                    
                    int maxPrec = 
                        maxNumPrec[t][c][r].x * maxNumPrec[t][c][r].y;
                
                    // Allocate for subbands
                    ttIncl[t][c][r] = 
                        new TagTreeEncoder[subRange[t][c][r][1]+1][maxPrec];
                    ttMaxBP[t][c][r] = 
                        new TagTreeEncoder[subRange[t][c][r][1]+1][maxPrec];
                    prevtIdxs[t][c][r] = new int[subRange[t][c][r][1]+1][];
                    lblock[t][c][r] = new int[subRange[t][c][r][1]+1][];
                    cbArrayI[t][c][r] = 
                        new CBlkCoordInfo[subRange[t][c][r][1]+1][];
                    precArrayI[t][c][r] = 
                        new PrecCoordInfo[subRange[t][c][r][1]+1][];
                    
                    // Initialize code-blocks and precincts information in
                    // this resolution level
                    buildCblkPrec(t,c,r);
                    
                    for(int subIdx=subRange[t][c][r][0];
                        subIdx <= subRange[t][c][r][1]; subIdx ++){
                            // Loop on subbands
                        sb2 = (SubbandAn)sb.getSubbandByIdx(r,subIdx);
                        tmpCoord = infoSrc.getNumCodeBlocks(sb2,tmpCoord);
                        numcb = tmpCoord.x*tmpCoord.y;
                    
                        lblock[t][c][r][subIdx] = new int[numcb];
                        ArrayUtil.intArraySet(lblock[t][c][r][subIdx],
                                              INIT_LBLOCK);

                        prevtIdxs[t][c][r][subIdx] = new int[numcb];
                        ArrayUtil.intArraySet(prevtIdxs[t][c][r][subIdx],-1);
                        
                        // For each precinct, get the number of code-blocks in
                        // the packet and create the tag trees
                        for(int pIdx=0 ; pIdx<maxPrec ; pIdx++){
                            
                            cblks = getCBlkInPrec(t,c,r,subIdx,pIdx,cblks);
                            
                            // the first element contains the number of
                            // code-blocks in the precinct in each direction
                            tmpCoord = (Coord)cblks.elementAt(0);
                            
                            ttIncl[t][c][r][subIdx][pIdx] =
                                new TagTreeEncoder(tmpCoord.y,tmpCoord.x);
                            ttMaxBP[t][c][r][subIdx][pIdx] =
                                new TagTreeEncoder(tmpCoord.y,tmpCoord.x);
                        }
                    }
                }
            }
            // Goto next tile
            if (t<ntiles-1) { // not at last tile
                infoSrc.nextTile();
            }
        }
        // Reset tile to original one
        infoSrc.setTile(orTCoord.x,orTCoord.y);
    }

    /**
     * Encodes a packet and returns the buffer containing the encoded packet
     * header. The code-blocks appear in a 3D array of CBlkRateDistStats,
     * 'cbs'. The first index is the tile index in lexicographical order, the
     * second index is the subband index (as defined in the Subband class),
     * and the third index is the code-block index (whithin the subband tile)
     * in lexicographical order as well. The indexes of the new truncation
     * points for each code-block are specified by the 3D array of int
     * 'tIndx'. The indices of this array are the same as for cbs. The
     * truncation point indices in 'tIndx' are the indices of the elements of
     * the 'truncIdxs' array, of the CBlkRateDistStats class, that give the
     * real truncation points. If a truncation point index is negative it
     * means that the code-block has not been included in any layer yet. If
     * the truncation point is less than or equal to the highest truncation
     * point used in previous layers then the code-block is not included in
     * the packet. Otherwise, if larger, the code-block is included in the
     * packet. The body of the packet can be obtained with the
     * getLastBodyBuf() and getLastBodyLen() methods.
     *
     * <P>Layers must be coded in increasing order, in consecutive manner, for
     * each tile, component and resolution level (e.g., layer 1, then layer 2,
     * etc.). For different tile, component and/or resolution level no
     * particular order must be followed.
     *
     * @param ly The layer index (starts at 1).
     *
     * @param c The component index.
     *
     * @param r The resolution level
     *
     * @param t Index of the current tile
     *
     * @param cbs The 3D array of coded code-blocks.
     *
     * @param tIndx The truncation point indices for each code-block.
     *
     * @param hbuf The header buffer. If null a new BitOutputBuffer is created
     * and returned. This buffer is reset before anything is written to it.
     *
     * @param bbuf The body buffer. If null a new one is created. If not large
     * enough a new one is created.
     *
     * @param pIdx The precinct index
     *
     * @return The buffer containing the packet header.
     * */
    public BitOutputBuffer encodePacket(int ly, int c, int r, int t,
                                        CBlkRateDistStats cbs[][],
                                        int tIndx[][], BitOutputBuffer hbuf,
                                        byte bbuf[], int pIdx) {
        int b,by,bx,s,i,maxi;
        int ncb,ncbx,ncby;
        int thmax;
        int newtp;
        int cblen;
        int prednbits,nbits,deltabits;
        TagTreeEncoder cur_ttIncl,cur_ttMaxBP;
        int cur_prevtIdxs[]; // last encoded truncation points
        CBlkRateDistStats cur_cbs[];
        int cur_tIndx[]; // truncation points to encode
        int minsb,maxsb;
        Vector cbv = null;
        Coord cbCoord = null;
	boolean precinctPartitionUsed = infoSrc.precinctPartitionUsed(c,t);
        int firstCB_horIdx  = -1;
        int firstCB_vertIdx = -1;
        boolean pktPresentWritten = false;
        boolean precIdxExist = false;
        SubbandAn sb = null;
        roiInPkt = false;
        roiLen = 0;
        
        maxsb = subRange[t][c][r][1]+1;
        minsb = subRange[t][c][r][0];

        // First, we check if packet is empty (i.e precinct 'p' is empty
        // in all subbands)
        boolean isPktEmpty = true;
        
        for (s=minsb; s<maxsb; s++) {
            if( precArrayI[t][c][r][s] == null ){
                // The precinct is empty in the subband
                continue;
            } 
            else{
                cbv = getCBlkInPrec(t,c,r,s,pIdx,cbv);
                if(cbv.size()==1) // Precinct is not empty but there is no
                    // code-block inside (Is it possible ?)
                    continue;
                else {
                    // The precinct is not empty in at least one subband ->
                    // stop
                    isPktEmpty = false;
                    break;
                }
            }
        }
        
        if(isPktEmpty){
            packetWritable = true;
            
            if(hbuf == null){
                hbuf = new BitOutputBuffer();
            }
            else{
                hbuf.reset();
            }
            if (bbuf == null) {
                lbbuf = bbuf = new byte[1];
            }
            hbuf.writeBit(0);
            lblen = 0;
            
            return hbuf;
        } 
                
        if(hbuf == null){
            hbuf = new BitOutputBuffer();
        }
        else{
            hbuf.reset();
        }

        // Invalidate last body buffer
        lbbuf = null;
        lblen = 0;
		  
        // Signal that packet is present
        hbuf.writeBit(1);

        for(s=minsb; s<maxsb; s++){ // Loop on subbands

            // Do not try to write any code-block inclusion if the precinct
            // does not contain element of this subband
            if(precArrayI[t][c][r][s]==null){
                continue;
            }

            if(pIdx>=precArrayI[t][c][r][s].length){
                // If we arrive here, it means that there is no code-block for
                // this subband but the others subband might have some
                // code-blocks.
                if(precinctPartitionUsed){
                    continue;
                }
            }
            
            // If we arrive here, it means that the precinct exists in this
            // subband.
            precIdxExist = true;

            // Get in the vector cbv the code-blocks that "belong" to the
            // precinct whose index is 'pIdx'
            cbv = getCBlkInPrec(t,c,r,s,pIdx,cbv);

            cur_ttIncl = ttIncl[t][c][r][s][pIdx];
            cur_ttMaxBP = ttMaxBP[t][c][r][s][pIdx];
            cur_prevtIdxs = prevtIdxs[t][c][r][s];
            cur_cbs = cbs[s];
            cur_tIndx = tIndx[s];
            ncbx = cur_ttIncl.getWidth();
            ncby = cur_ttIncl.getHeight();
            
            // Set tag tree values for this subband 
            for( int numcb=1 ; numcb<cbv.size() ; numcb++ ){
                b = ((Integer)cbv.elementAt(numcb)).intValue();
                cbCoord = cbArrayI[t][c][r][s][b].idx;
                bx = cbCoord.x;
                by = cbCoord.y;
                
                // Save "offset" as tag trees are indexed from 0
                if ( numcb==1 ) {
                    firstCB_horIdx = bx;
                    firstCB_vertIdx = by;
                }
                
                if (cur_tIndx[b] > cur_prevtIdxs[b] &&
                    cur_prevtIdxs[b] < 0) { // First inclusion
                    // Set layer index
                    cur_ttIncl.setValue(by-firstCB_vertIdx,
                                        bx-firstCB_horIdx, ly-1);
                }
            }   
            
            if (ly == 1) { // First layer, need to set the skip of MSBP
                for ( int numcb=1 ; numcb<cbv.size() ; numcb++ ) {
                    b = ((Integer)cbv.elementAt(numcb)).intValue();
                    cbCoord = cbArrayI[t][c][r][s][b].idx;
                    bx = cbCoord.x;
                    by = cbCoord.y;
                
                    // Save "offset" as tag trees are indexed from 0
                    if ( numcb==1 ) {
                        firstCB_horIdx = bx;
                        firstCB_vertIdx = by;
                    }
                
                    // Set skip of most sig. bit-plane
                    cur_ttMaxBP.setValue(by-firstCB_vertIdx,
                                         bx-firstCB_horIdx,
                                         cur_cbs[b].skipMSBP);
                }
            }  
            
            // Now encode the information
            for(int numcb=1; numcb<cbv.size(); numcb++){
                b = ((Integer)cbv.elementAt(numcb)).intValue();
                cbCoord = cbArrayI[t][c][r][s][b].idx;
                bx = cbCoord.x;
                by = cbCoord.y;
                
                // Save "offset" as tag trees are indexed from 0
                if( numcb==1 ){
                    firstCB_horIdx = bx;
                    firstCB_vertIdx = by;
                }

                // 1) Inclusion information
                if (cur_tIndx[b] > cur_prevtIdxs[b]) {
                    // Block included in this precinct
                    if (cur_prevtIdxs[b] < 0) { // First inclusion
                        // Encode layer info
                        cur_ttIncl.encode(by-firstCB_vertIdx,
                                          bx-firstCB_horIdx,ly,hbuf);

                        // 2) Max bitdepth info. Encode value
                        thmax = cur_cbs[b].skipMSBP+1;
                        for (i=1; i<=thmax;i++) {
                            cur_ttMaxBP.encode(by-firstCB_vertIdx,
                                               bx-firstCB_horIdx,i,hbuf);
                        }

                        // Count body size for packet
                        lblen += cur_cbs[b].
                            truncRates[cur_cbs[b].truncIdxs[cur_tIndx[b]]];
                    }
                    else { // Already in previous layer
                        // Send "1" bit
                        hbuf.writeBit(1);
                        // Count body size for packet
                        lblen +=
                            cur_cbs[b].
                            truncRates[cur_cbs[b].truncIdxs[cur_tIndx[b]]] -
                            cur_cbs[b].
                            truncRates[cur_cbs[b].
                                      truncIdxs[cur_prevtIdxs[b]]];
                    }

                    // 3) Truncation point information
                    if (cur_prevtIdxs[b]<0) {
                        newtp = cur_cbs[b].truncIdxs[cur_tIndx[b]];
                    }
                    else {
                        newtp = cur_cbs[b].truncIdxs[cur_tIndx[b]]-
                            cur_cbs[b].truncIdxs[cur_prevtIdxs[b]]-1;
                    }

                    // Mix of switch and if is faster
                    switch (newtp) {
                    case 0:
                        hbuf.writeBit(0); // Send one "0" bit
                        break;
                    case 1:
                        hbuf.writeBits(2,2); // Send one "1" and one "0"
                        break;
                    case 2:
                    case 3:
                    case 4:
                            // Send two "1" bits followed by 2 bits
                            // representation of newtp-2
                        hbuf.writeBits((3<<2)|(newtp-2),4);
                        break;
                    default:
                        if (newtp <= 35) {
                            // Send four "1" bits followed by a five bits
                            // representation of newtp-5
                            hbuf.writeBits((15<<5)|(newtp-5),9);
                        }
                        else if (newtp <= 163) {
                            // Send nine "1" bits followed by a seven bits
                            // representation of newtp-36
                            hbuf.writeBits((511<<7)|(newtp-36),16);
                        }
                        else {
                            throw new
                                ArithmeticException("Maximum number "+
                                                    "of truncation "+
                                                    "points exceeded");
                        }
                    }
                }
                else { // Block not included in this layer
                    if (cur_prevtIdxs[b] >= 0) {
                        // Already in previous layer. Send "0" bit
                        hbuf.writeBit(0);
                    }
                    else { // Not in any previous layers
                        cur_ttIncl.encode(by-firstCB_vertIdx,
                                          bx-firstCB_horIdx,ly,hbuf);
                    }
                    // Go to the next one.
                    continue;
                }

                // Code-block length
                
                // We need to compute the maximum number of bits needed to
                // signal the length of each terminated segment and the final
                // truncation point.
                newtp = 1;
                maxi = cur_cbs[b].truncIdxs[cur_tIndx[b]];
                cblen = (cur_prevtIdxs[b]<0) ? 0 :
                    cur_cbs[b].truncRates[cur_cbs[b].
                                         truncIdxs[cur_prevtIdxs[b]]];
                
                // Loop on truncation points
                i = (cur_prevtIdxs[b]<0) ? 0 :
                    cur_cbs[b].truncIdxs[cur_prevtIdxs[b]]+1;
                int minbits = 0;
                for (; i<maxi; i++, newtp++) {
                    // If terminated truncation point calculate length
                    if (cur_cbs[b].isTermPass != null &&
                        cur_cbs[b].isTermPass[i]) {

                        // Calculate length
                        cblen = cur_cbs[b].truncRates[i] - cblen;

                        // Calculate number of needed bits
                        prednbits = lblock[t][c][r][s][b] +
                            MathUtil.log2(newtp);
                        minbits = ((cblen>0) ? MathUtil.log2(cblen) : 0)+1;

                        // Update Lblock increment if needed
                        for(int j=prednbits; j<minbits; j++) {
                            lblock[t][c][r][s][b]++;
                            hbuf.writeBit(1);
                        }
                        // Initialize for next length
                        newtp = 0;
                        cblen = cur_cbs[b].truncRates[i];
                    }
                }

                // Last truncation point length always sent

                // Calculate length
                cblen = cur_cbs[b].truncRates[i] - cblen;
                
                // Calculate number of bits
                prednbits = lblock[t][c][r][s][b] + MathUtil.log2(newtp);
                minbits = ((cblen>0) ? MathUtil.log2(cblen) : 0)+1;
                // Update Lblock increment if needed
                for(int j=prednbits; j<minbits; j++) {
                    lblock[t][c][r][s][b]++;
                    hbuf.writeBit(1);
                }
                
                // End of comma-code increment
                hbuf.writeBit(0);

                // There can be terminated several segments, send length info
                // for all terminated truncation points in addition to final
                // one
                newtp = 1;
                maxi = cur_cbs[b].truncIdxs[cur_tIndx[b]];
                cblen = (cur_prevtIdxs[b]<0) ? 0 :
                    cur_cbs[b].truncRates[cur_cbs[b].
                                         truncIdxs[cur_prevtIdxs[b]]];
                // Loop on truncation points and count the groups
                i = (cur_prevtIdxs[b]<0) ? 0 :
                    cur_cbs[b].truncIdxs[cur_prevtIdxs[b]]+1;
                for (; i<maxi; i++, newtp++) {
                    // If terminated truncation point, send length
                    if (cur_cbs[b].isTermPass != null &&
                        cur_cbs[b].isTermPass[i]) {

                        cblen = cur_cbs[b].truncRates[i] - cblen;
                        nbits = MathUtil.log2(newtp)+lblock[t][c][r][s][b];
                        hbuf.writeBits(cblen,nbits);

                        // Initialize for next length
                        newtp = 0;
                        cblen = cur_cbs[b].truncRates[i];
                    }
                }
                // Last truncation point length is always signalled
                // First calculate number of bits needed to signal
                // Calculate length
                cblen = cur_cbs[b].truncRates[i] - cblen;
                nbits = MathUtil.log2(newtp) + lblock[t][c][r][s][b];
                hbuf.writeBits(cblen,nbits);

            } // End loop on code-blocks
        } // End loop on subband
        
        // -> Copy the data to the body buffer
        
        // Ensure size for body data
        if (bbuf == null || bbuf.length < lblen){
            bbuf = new byte[lblen];
        }
        lbbuf = bbuf;
        lblen = 0;
            
        for (s=minsb; s<maxsb; s++) { // Loop on subbands
        
            // Get in the vector cblks the code-blocks that "belong" to the 
            // precinct whose index is 'p'
            cbv = getCBlkInPrec(t,c,r,s,pIdx,cbv);
            
            cur_prevtIdxs = prevtIdxs[t][c][r][s];
            cur_cbs = cbs[s];
            cur_tIndx = tIndx[s];
            ncb = cur_prevtIdxs.length;
            
            for ( int numcb=1 ; numcb<cbv.size() ; numcb++ ) {
                b = ((Integer)cbv.elementAt(numcb)).intValue();
                if (cur_tIndx[b] > cur_prevtIdxs[b]) {

                    // Block included in this precinct
                    // Copy data to body buffer and get code-size
                    if (cur_prevtIdxs[b] < 0) {
                        cblen = cur_cbs[b].
                            truncRates[cur_cbs[b].truncIdxs[cur_tIndx[b]]];
                        System.arraycopy(cur_cbs[b].data,0,
                                         lbbuf,lblen,cblen);
                    }
                    else {
                        cblen = cur_cbs[b].
                            truncRates[cur_cbs[b].truncIdxs[cur_tIndx[b]]] -
                            cur_cbs[b].
                            truncRates[cur_cbs[b].truncIdxs[cur_prevtIdxs[b]]];
                        System.
                            arraycopy(cur_cbs[b].data,
                                      cur_cbs[b].
                                      truncRates[cur_cbs[b].
                                                truncIdxs[cur_prevtIdxs[b]]],
                                      lbbuf,lblen,cblen);
                    }
                    lblen += cblen;

                    // Verify if this code-block contains new ROI information
                    if(cur_cbs[b].nROIcoeff!=0 && 
                       (cur_prevtIdxs[b]==-1 ||
                       cur_cbs[b].truncIdxs[cur_prevtIdxs[b]] <=
                       cur_cbs[b].nROIcp-1) ) {
                        roiInPkt = true;
                        roiLen = lblen;
                    } 

                    // Update truncation point
                    cur_prevtIdxs[b] = cur_tIndx[b];
                }
            }
        }

        if(!precIdxExist){ // No precinct found in this resolution level
            packetWritable = false;
            return hbuf;
        } else {
            packetWritable = true;
        }

        // Must never happen
	if(hbuf.getLength()==0){
            throw new Error("You have found a bug in PktEncoder, method:"+
                            " encodePacket");
	}

        return hbuf;
    }

    /**
     * Returns the buffer of the body of the last encoded packet. The length
     * of the body can be retrieved with the getLastBodyLen() method. The
     * length of the array returned by this method may be larger than the
     * actual body length.
     *
     * @return The buffer of body of the last encoded packet.
     *
     * @exception IllegalArgumentException If no packet has been coded since
     * last reset(), last restore(), or object creation.
     *
     * @see #getLastBodyLen
     * */
    public byte[] getLastBodyBuf() {
        if (lbbuf == null) {
            throw new IllegalArgumentException();
        }
        return lbbuf;
    }

    /**
     * Returns the length of the body of the last encoded packet, in
     * bytes. The body itself can be retrieved with the getLastBodyBuf()
     * method.
     *
     * @return The length of the body of last encoded packet, in bytes.
     *
     * @see #getLastBodyBuf
     * */
    public int getLastBodyLen() {
        return lblen;
    }

    /**
     * Saves the current state of this object. The last saved state
     * can be restored with the restore() method.
     *
     * @see #restore
     * */
    public void save() {
        int s,r,n,t,p;
        int maxsbi,minsbi;

        // Have we done any save yet?
        if (bak_lblock == null) {
            // Allocate backup buffers
            bak_lblock = new int[ttIncl.length][][][][];
            bak_prevtIdxs = new int[ttIncl.length][][][][];
            for (t=ttIncl.length-1; t>=0; t--) {
                bak_lblock[t] = new int[ttIncl[t].length][][][];
                bak_prevtIdxs[t] = new int[ttIncl[t].length][][][];
                for (n=ttIncl[t].length-1; n>=0; n--) {
                    bak_lblock[t][n] = new int[lblock[t][n].length][][];
                    bak_prevtIdxs[t][n] = new int[ttIncl[t][n].length][][];
                    for (r=lblock[t][n].length-1; r>=0; r--) {
                        bak_lblock[t][n][r] =
                            new int[lblock[t][n][r].length][];
                        bak_prevtIdxs[t][n][r] =
                            new int[ttIncl[t][n][r].length][];
                        maxsbi = ttIncl[t][n][r].length;
                        minsbi = maxsbi>>2; // minsbi = maxsbi/4;
                        for (s = minsbi; s<maxsbi; s++) {
                            bak_lblock[t][n][r][s] = 
                                new int[lblock[t][n][r][s].length];
                            bak_prevtIdxs[t][n][r][s] =
                                new int[prevtIdxs[t][n][r][s].length];
                        }
                    }
                }
            }
        }

        //-- Save the data

        // Use reference caches to minimize array access overhead
        TagTreeEncoder
            ttIncl_t_n[][][],
            ttMaxBP_t_n[][][],
            ttIncl_t_n_r[][],
            ttMaxBP_t_n_r[][];
        int
            lblock_t_n[][][],
            bak_lblock_t_n[][][],
            prevtIdxs_t_n_r[][],
            bak_prevtIdxs_t_n_r[][];
            
        // Loop on tiles
        for (t=ttIncl.length-1; t>=0; t--) {
            // Loop on components
            for (n=ttIncl[t].length-1; n>=0; n--) {
                // Initialize reference caches
                lblock_t_n = lblock[t][n];
                bak_lblock_t_n = bak_lblock[t][n];
                ttIncl_t_n = ttIncl[t][n];
                ttMaxBP_t_n = ttMaxBP[t][n];
                // Loop on resolution levels
                for (r=lblock_t_n.length-1; r>=0; r--) {
                    // Initialize reference caches
                    ttIncl_t_n_r = ttIncl_t_n[r];
                    ttMaxBP_t_n_r = ttMaxBP_t_n[r];
                    prevtIdxs_t_n_r = prevtIdxs[t][n][r];
                    bak_prevtIdxs_t_n_r = bak_prevtIdxs[t][n][r];

                    // Loop on subbands
                    maxsbi = ttIncl_t_n_r.length;
                    minsbi = maxsbi>>2; // minsbi = maxsbi/4;
                    for (s = minsbi; s<maxsbi; s++) {
                        // Save 'lblock'
                        System.arraycopy(lblock_t_n[r][s],0,
                                         bak_lblock_t_n[r][s],0,
                                         lblock_t_n[r][s].length);
                        // Save 'prevtIdxs'
                        System.arraycopy(prevtIdxs_t_n_r[s],0,
                                         bak_prevtIdxs_t_n_r[s],0,
                                         prevtIdxs_t_n_r[s].length);
                        if ( precArrayI[t][n][r][s] != null ) {
                            for (p=precArrayI[t][n][r][s].length-1 ; p>=0 ; p--)
                            {
                                if ( p<ttIncl_t_n_r[s].length ) {
                                    // Save tag trees
                                    ttIncl_t_n_r[s][p].save();
                                    ttMaxBP_t_n_r[s][p].save();
                                }
                            }
                        }
                    }
                }
            }
        }

        // Set the saved state
        saved = true;
    }

    /**
     * Restores the last saved state of this object. An
     * IllegalArgumentException is thrown if no state has been saved.
     *
     * @see #save
     * */
    public void restore() {
        int s,r,n,t,p;
        int maxsbi,minsbi;


        if (!saved) {
            throw new IllegalArgumentException();
        }

        // Invalidate last encoded body buffer
        lbbuf = null;

        //-- Restore tha data

        // Use reference caches to minimize array access overhead
        TagTreeEncoder
            ttIncl_t_n[][][],
            ttMaxBP_t_n[][][],
            ttIncl_t_n_r[][],
            ttMaxBP_t_n_r[][];
        int
            lblock_t_n[][][],
            bak_lblock_t_n[][][],
            prevtIdxs_t_n_r[][],
            bak_prevtIdxs_t_n_r[][];
            
        // Loop on tiles
        for (t=ttIncl.length-1; t>=0; t--) {
            // Loop on components
            for (n=ttIncl[t].length-1; n>=0; n--) {
                // Initialize reference caches
                lblock_t_n = lblock[t][n];
                bak_lblock_t_n = bak_lblock[t][n];
                ttIncl_t_n = ttIncl[t][n];
                ttMaxBP_t_n = ttMaxBP[t][n];
                // Loop on resolution levels
                for (r=lblock_t_n.length-1; r>=0; r--) {
                    // Initialize reference caches
                    ttIncl_t_n_r = ttIncl_t_n[r];
                    ttMaxBP_t_n_r = ttMaxBP_t_n[r];
                    prevtIdxs_t_n_r = prevtIdxs[t][n][r];
                    bak_prevtIdxs_t_n_r = bak_prevtIdxs[t][n][r];

                    // Loop on subbands
                    maxsbi = ttIncl_t_n_r.length;
                    minsbi = maxsbi>>2; // minsbi = maxsbi/4;
                    for (s = minsbi; s<maxsbi; s++) {
                        // Restore 'lblock'
                        System.arraycopy(bak_lblock_t_n[r][s],0,
                                         lblock_t_n[r][s],0,
                                         lblock_t_n[r][s].length);
                        // Restore 'prevtIdxs'
                        System.arraycopy(bak_prevtIdxs_t_n_r[s],0,
                                         prevtIdxs_t_n_r[s],0,
                                         prevtIdxs_t_n_r[s].length);
                        // Loop on precincts
                        if ( precArrayI[t][n][r][s] != null ) {
                            for(p=precArrayI[t][n][r][s].length-1 ; p>=0 ; p--) 
                            {
                                if ( p<ttIncl_t_n_r[s].length ) {
                                    // Restore tag trees
                                    ttIncl_t_n_r[s][p].restore();
                                    ttMaxBP_t_n_r[s][p].restore();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Resets the state of the object to the initial state, as if the object
     * was just created.
     * */
    public void reset() {
        int s,r,n,t,p;
        int maxsbi,minsbi;

        // Invalidate save
        saved = false;
        // Invalidate last encoded body buffer
        lbbuf = null;

        // Reinitialize each element in the arrays

        // Use reference caches to minimize array access overhead
        TagTreeEncoder
            ttIncl_t_n[][][],
            ttMaxBP_t_n[][][],
            ttIncl_t_n_r[][],
            ttMaxBP_t_n_r[][];
        int
            lblock_t_n[][][],
            prevtIdxs_t_n_r[][];
            
        // Loop on tiles
        for (t=ttIncl.length-1; t>=0; t--) {
            // Loop on components
            for (n=ttIncl[t].length-1; n>=0; n--) {
                // Initialize reference caches
                lblock_t_n = lblock[t][n];
                ttIncl_t_n = ttIncl[t][n];
                ttMaxBP_t_n = ttMaxBP[t][n];
                // Loop on resolution levels
                for (r=lblock_t_n.length-1; r>=0; r--) {
                    // Initialize reference caches
                    ttIncl_t_n_r = ttIncl_t_n[r];
                    ttMaxBP_t_n_r = ttMaxBP_t_n[r];
                    prevtIdxs_t_n_r = prevtIdxs[t][n][r];

                    // Loop on subbands
                    maxsbi = ttIncl_t_n_r.length;
                    minsbi = maxsbi>>2; // minsbi = maxsbi/4;
                    for (s = minsbi; s<maxsbi; s++) {
                        // Reset 'prevtIdxs'
                        ArrayUtil.intArraySet(prevtIdxs_t_n_r[s],-1);
                        
                        // Reset 'lblock'
                        ArrayUtil.intArraySet(lblock_t_n[r][s],
                                              INIT_LBLOCK);
                        // Loop on precincts
                        if ( precArrayI[t][n][r][s] != null ) {
                            for (p=precArrayI[t][n][r][s].length-1;p>=0;p--) {
                                if ( p<ttIncl_t_n_r[s].length ) {
                                    // Reset tag trees
                                    ttIncl_t_n_r[s][p].reset();
                                    ttMaxBP_t_n_r[s][p].reset();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Creates the arrays incArray and incArrayMax. The first array (incArray)
     * contains the increment step used for each component, each tile and each
     * resolution level. The second one (incArrayMax) contains the smallest
     * increment. This is used when looking for precinct inclusion.
     *
     * @param t the tile index
     *
     * @param c the component index
     * */
    private void buildIncArrays(int c, int t) {
        int ppx, ppy; // Precinct partition dimensions
        int l;        // Resolution level
        Coord inc, incMax;

        if ( incArrayMax[t][c]==null ) {
            // Allocate maximum increment array if it does not exist
            incArrayMax[t][c] = new Coord();
        }
        else {
            // Already allocated and filled
            return;
        }

        incMax = incArrayMax[t][c];

        for ( l=0 ; l<mrl[t][c]+1 ; l++ ) {

            // Get precinct partition dimensions
            ppx = infoSrc.getPPX(t, c, l);
            ppy = infoSrc.getPPY(t, c, l);

            if ( incArray[t][c][l]==null ) {
                // Allocate increment array for resolution level 'l' if it 
                // does not exist
                incArray[t][c][l] = new Coord();
            }
            inc = incArray[t][c][l];

            inc.x = infoSrc.getCompSubsX(c);
            inc.x <<= MathUtil.log2(ppx) + mrl[t][c] - l;
            if ( incMax.x==0 || inc.x<incMax.x ) {
                // Save smallest horizontal increment
                incMax.x = inc.x;
            }  

            inc.y = infoSrc.getCompSubsY(c);
            inc.y <<= MathUtil.log2(ppy) + mrl[t][c] - l;
            if ( incMax.y==0 || inc.y<incMax.y ) {
                // Save smallest vertical increment
                incMax.y = inc.y;
            }
        }   
    }
        
    /**
     * Creates the arrays sot_eotArray and sot_eotArrayMax. The first array
     * (sot_eotArray) contains the start/end of tile coordinates at each
     * resolution level and the second array (sot_eotArrayMax) contains the
     * start/end of tile coordinates for the highest resolution level using
     * the smallest increment step
     *
     * @param t the tile index
     *
     * @param c the component index
     * */
    private void buildSotEotArrays(int c, int t) {
        int l;
        Coord sot_eotMax[] = null;
        Coord sot_eot[] = null;
        
        // Create and initialise the maximum increment array
        // Allocate and initialise if elements do not exist
        if ( sot_eotArrayMax[t][c][0]==null ) {
            sot_eotArrayMax[t][c][0] = new Coord(); // start of tile
            sot_eotArrayMax[t][c][1] = new Coord(); // end of tile
            sot_eotMax = sot_eotArrayMax[t][c];
            
            // Calculate the start/enf of tile for the highest resolution and 
            // smallest increment step
            if (incArrayMax[t][c].x == 0) { 
                sot_eotMax[0].x      = 0;
                sot_eotMax[1].x      = 1;
                incArrayMax[t][c].x  = 1;
            }
            else {
                sot_eotMax[0].x = infoSrc.getULX(c); 
                sot_eotMax[1].x = sot_eotMax[0].x + infoSrc.getWidth();
                sot_eotMax[0].x &= ~(incArrayMax[t][c].x-1);
            }
            
            if (incArrayMax[t][c].y == 0) { 
                sot_eotMax[0].y      = 0;
                sot_eotMax[1].y      = 1;
                incArrayMax[t][c].y  = 1;
            }
            else {
                sot_eotMax[0].y = infoSrc.getULY(c); 
                sot_eotMax[1].y = sot_eotMax[0].y + infoSrc.getHeight();
                sot_eotMax[0].y &= ~(incArrayMax[t][c].y-1);
            }
        }
        
        // Create and initialise the increment array for each resolution level
        
        for ( l=0 ; l<mrl[t][c]+1 ; l++ ) {
            if ( sot_eotArray[t][c][l][0]==null ) {
                sot_eotArray[t][c][l][0] = new Coord(); // start of tile
                sot_eotArray[t][c][l][1] = new Coord(); // end of tile
            }
            else {
                // Already allocated and filled
                continue;
            }
            sot_eot = sot_eotArray[t][c][l];
            
            // Calculate the start/enf of tile for the resolution l and 
            // associated increment
            if (incArray[t][c][l].x == 0) { 
                sot_eot[0].x      = 0;
                sot_eot[1].x      = 1;
                incArray[t][c][l].x  = 1;
            }
            else {
                sot_eot[0].x = infoSrc.getULX(c); 
                sot_eot[1].x = sot_eot[0].x + infoSrc.getWidth();
                sot_eot[0].x &= ~(incArray[t][c][l].x-1); 
            }
            
            if (incArray[t][c][l].y == 0) { 
                sot_eot[0].y      = 0;
                sot_eot[1].y      = 1;
                incArray[t][c][l].y  = 1;
            }
            else {
                sot_eot[0].y = infoSrc.getULY(c); 
                sot_eot[1].y = sot_eot[0].y + infoSrc.getHeight();
                sot_eot[0].y &= ~(incArray[t][c][l].y-1);
            }
        }
    }
    
    /**
     * Builds the lists containing the ulx, uly, width, height and indexes of
     * the code-blocks and of the precincts for each tile, component,
     * resolution level and subband.  First, we compute the projected anchor
     * point for the code-block partition.  Then, the array containing the
     * code-blocks coordinates is built. Finally, the array containing the
     * precincts coordinates is built.
     *
     * @param t the tile index
     *
     * @param c the component index
     *
     * @param ppx the precinct partition width
     *
     * @param ppy the precinct partition height
     *
     * @param resLvl The resolution level
     * */
    private void buildCblkPrec(int t, int c, int resLvl) {
            
        int cn, cm, count, nrcb, tmp;
        int n, m; // horizontal and vertical indexes of the current code-block
        int apox, apoy; // projected anchor point for the code-block partition
        Coord ncblks = null;  // Number of code-blocks
        CBlkCoordInfo cbinf = null;
        SubbandAn sbtree, sb;
        int maxsbi, minsbi;
        int precIdxA[];
        
        // Get subband tree
        sbtree = savedSbTree[t][c];
        
        precIdxA = new int[subRange[t][c][resLvl][1]+1];

        // loop on each subband
        for(int subIdx=subRange[t][c][resLvl][0];
            subIdx <= subRange[t][c][resLvl][1]; subIdx ++){
            
            sb = (SubbandAn)sbtree.getSubbandByIdx(resLvl,subIdx);

            // Compute the projected anchor point for the code-block partition
            apox = infoSrc.getPartitionULX();
            apoy = infoSrc.getPartitionULY();

            SubbandAn sb2;
            switch (sb.gOrient) {
            case Subband.WT_ORIENT_LL:
                // No need to project since all low-pass => nothing to do
                break;
            case Subband.WT_ORIENT_HL:
                // There is at least a high-pass step on the horizontal
                // decomposition => project to 0
                apox = 0;
                // We need to find out if there has been a high-pass step on
                // the vertical decomposition
                sb2 = sb;
                do {
                    if (sb2.orientation == Subband.WT_ORIENT_HH ||
                        sb2.orientation == Subband.WT_ORIENT_LH) {
                        // Vertical high-pass step => project to 0 and done
                        apoy = 0;
                        break;
                    }
                    if (sb2.gOrient == Subband.WT_ORIENT_LL) {
                        // Only low-pass steps left, no need to continue 
                        // checking
                        break;
                    }
                    sb2 = (SubbandAn)sb2.getParent();
                } while (sb2 != null);
                break;
            case Subband.WT_ORIENT_LH:
                // We need to find out if there has been a high-pass step on
                // the horizontal decomposition
                sb2 = sb;
                do {
                    if (sb2.orientation == Subband.WT_ORIENT_HH ||
                        sb2.orientation == Subband.WT_ORIENT_HL) {
                        // Horizontal high-pass step => project to 0 and done
                        apox = 0;
                        break;
                    }
                    if (sb2.gOrient == Subband.WT_ORIENT_LL) {
                        // Only low-pass steps left, no need to continue
                        // checking
                        break;
                    }
                    sb2 = (SubbandAn)sb2.getParent();
                } while (sb2 != null);
                // There is at least a high-pass step on the vertical
                // decomposition => project to 0
                apoy = 0;
                break;
            case Subband.WT_ORIENT_HH:
                // There is at least a high-pass step on the horiz. and
                // vertical decomposition => project to 0
                apox = 0;
                apoy = 0;
                break;
            default:
                throw new Error("Internal JJ2000 error");
            } // end switch
        
            // Get number of code-blocks in current subband
            ncblks = infoSrc.getNumCodeBlocks(sb,ncblks);
            nrcb = ncblks.x*ncblks.y;

            if ( nrcb>0 ) {
                // If there is at least one code-block

                if ( cbArrayI[t][c][sb.resLvl][sb.sbandIdx]==null  ) {
                    cbArrayI[t][c][sb.resLvl][sb.sbandIdx] = 
                        new CBlkCoordInfo[nrcb];
                }

                // Deal with code-blocks first
                count = 0;
                n = -1;
                m = 0;
                do {
                    // Goto next code-block
                    n++;
                    if (n == ncblks.x) { 
                        // Got to end of this row of code-blocks
                        n = 0;
                        m++;
                    }

                    cbArrayI[t][c][sb.resLvl][sb.sbandIdx][count] = 
                        new CBlkCoordInfo();
                    cbArrayI[t][c][sb.resLvl][sb.sbandIdx][count].idx = 
                        new Coord(n,m);
                    cbinf = cbArrayI[t][c][sb.resLvl][sb.sbandIdx][count];

                    // Indexes of first code-block overlapping with the
                    // subband
                    cn = (sb.ulcx-apox+sb.nomCBlkW)/sb.nomCBlkW-1;
                    cm = (sb.ulcy-apoy+sb.nomCBlkH)/sb.nomCBlkH-1;

                    // ULX
                    if (n == 0) { 
                        // Left-most code-block, starts where subband starts
                        cbinf.ulx = sb.ulx;
                    }
                    else {
                        // Calculate starting canvas coordinate and convert to
                        // subband coords
                        cbinf.ulx = (cn+n)*sb.nomCBlkW - (sb.ulcx-apox)+ 
                            sb.ulx;
                    }

                    // ULY
                    if (m == 0) { 
                        // Bottom-most code-block, starts where subband starts
                        cbinf.uly = sb.uly;
                    }
                    else {
                        cbinf.uly = (cm+m)*sb.nomCBlkH - (sb.ulcy-apoy) 
                                    + sb.uly;
                    }

                    // Width
                    if (n < ncblks.x-1) {
                        // Calculate where next code-block starts => width
                        cbinf.w = (cn+n+1)*sb.nomCBlkW - (sb.ulcx-apox) + 
                            sb.ulx - cbinf.ulx;
                    }
                    else { // Right-most code-block, ends where subband ends
                        cbinf.w = sb.ulx+sb.w - cbinf.ulx;
                    }

                    // Height
                    if (m < ncblks.y-1) {
                        // Calculate where next code-block starts => height
                        cbinf.h = (cm+m+1)*sb.nomCBlkH - (sb.ulcy-apoy) + 
                            sb.uly - cbinf.uly;
                    }
                    else { // Bottom-most code-block, ends where subband ends
                        cbinf.h = sb.uly+sb.h- cbinf.uly;
                    }
                    
                    count++;
                    if ( count == nrcb ) {
                        // we have dealt with all code-blocks
                        break;
                    }
                } while (true); 
            } // nrcb>0
        } // Loop on subbands
    
        // -------------------------------------------------
        //                Now deal with precincts
        // -------------------------------------------------
        
        int nl, nrpp, ppx, ppy;
        int x_inc, y_inc;
        int Px=0;
        int Py=0;
        int precIdx = -1;
        int hIdx, vIdx;
        boolean incrPrecIdx = false;
        
        x_inc = incArray[t][c][resLvl].x;
        y_inc = incArray[t][c][resLvl].y;
        
        // Get the maximum number of precincts for the resolution level
        Coord numPrecC = maxNumPrec[t][c][resLvl];
        nrpp = numPrecC.x * numPrecC.y;
            
        for(int subIdx=subRange[t][c][resLvl][0];
            subIdx <= subRange[t][c][resLvl][1]; subIdx ++){
            if ( nrpp>0 ) {
                //If there is at least one precinct
                precArrayI[t][c][resLvl][subIdx] = new PrecCoordInfo[nrpp];
            }
        }
        
        for(int yr=sot_eotArrayMax[t][c][0].y;yr<sot_eotArrayMax[t][c][1].y; 
            yr+=incArrayMax[t][c].y){
            
            for(int xr=sot_eotArrayMax[t][c][0].x;
                xr<sot_eotArrayMax[t][c][1].x;

                xr+=incArrayMax[t][c].x){

                // Loop on highest resolution grid using the smallest
                // increment
                if( ( (xr==sot_eotArrayMax[t][c][0].x) || ((xr)%x_inc==0) ) &&
                    ( (yr==sot_eotArrayMax[t][c][0].y) || ((yr)%y_inc==0) ) ) {
                    // Precinct exists in the resolution level
                    
                    incrPrecIdx = false;
    
                    for(int subIdx=subRange[t][c][resLvl][0];
                        subIdx <= subRange[t][c][resLvl][1]; subIdx ++){
                        
                        sb = (SubbandAn)sbtree.getSubbandByIdx(resLvl,subIdx);
                        
                        // If precinct is empty, skip this subband
                        if(maxNumPrec[t][c][resLvl].x * 
                           maxNumPrec[t][c][resLvl].y == 0) {
                            continue;
                        }

                        if( !infoSrc.precinctPartitionUsed(c, t) ){

                            // If the precinct partition is not used, create
                            // a precinct that contains all the code-blocks of
                            // the subband
                            precArrayI[t][c][resLvl][subIdx][0]
                                = new PrecCoordInfo(0,0,sb.w,sb.h,xr,yr);
                        }
                        else {
                            // Precinct partition is used

                            // Get precinct partition sizes
                            ppx = infoSrc.getPPX(t,c,resLvl);
                            ppy = infoSrc.getPPY(t,c,resLvl);
                            
                            precIdx = precIdxA[subIdx];
                            if ( precIdx<0 || precIdx>nrpp-1 ) {
                                // Wrong precinct index
                                continue;
                            }

                            // Calculate horizontal coordinate within subband 
                            // (including missing image)
                            if ((xr==sot_eotArrayMax[t][c][0].x) && 
                                (sot_eotArrayMax[t][c][0].x !=
                                (sot_eotArray[t][c][resLvl][0].x >>
                                (mrl[t][c]-resLvl)))){
                                if (resLvl==0){ 
                                    Px=(int)(sot_eotArray[t][c][resLvl][0].x/
                                            Math.pow(2,mrl[t][c]-resLvl)-
                                            sb.ulcx);
                                }
                                else {
                                    // If resolution level is not 0, then
                                    // divide precincts' size by 2 - Not
                                    // documented in CD
                                    Px=(int)(sot_eotArray[t][c][resLvl][0].x/
                                            Math.pow(2,mrl[t][c]-resLvl+1)-
                                            sb.ulcx);
                                    ppx=ppx>>1;
                                }
                            }
                            else {          
                                if (resLvl==0){ 
                                    Px=(int)(xr/Math.pow(2,mrl[t][c]-resLvl)-
                                            sb.ulcx);
                                }
                                else {
                                    // If resolution level is not 0, then
                                    // divide precincts' size by 2 - Not
                                    // documented in CD
                                    Px=(int)(xr/Math.pow(2,mrl[t][c]-resLvl+1)-
                                            sb.ulcx);
                                    ppx=ppx>>1;
                                }
                            }

                            // Calculate vertical coordinate within subband
                            // (including missing image)
                            if((yr==sot_eotArrayMax[t][c][0].y) && 
                                (sot_eotArrayMax[t][c][0].y !=
                                (sot_eotArray[t][c][resLvl][0].y >>
                                (mrl[t][c]-resLvl)))){
                                if (resLvl==0){ 
                                Py=(int)(sot_eotArray[t][c][resLvl][0].y/
                                        Math.pow(2,mrl[t][c]-resLvl)-sb.ulcy);
                                }
                                else {
                                    // If resolution level is not 0, then
                                    // divide precincts' size by 2 - Not
                                    // documented in CD
                                    Py=(int)(sot_eotArray[t][c][resLvl][0].y/
                                            Math.pow(2,mrl[t][c]-resLvl+1)-
                                            sb.ulcy);
                                    ppy=ppy>>1;
                                }
                            }
                            else{
                                if (resLvl==0){ 
                                    Py=(int)(yr/Math.pow(2,mrl[t][c]-resLvl)-
                                            sb.ulcy);
                                }
                                else {
                                    // If resolution level is not 0, then
                                    // divide precincts' size by 2 - Not
                                    // documented in CD
                                    Py=(int)(yr/Math.pow(2,mrl[t][c]-resLvl+1)-
                                            sb.ulcy);
                                    ppy=ppy>>1;
                                }
                            }

                            if( ((Px+ppx)<=0) || ((Py+ppy)<=0) 
                                 || (Px>=sb.w) || (Py>=sb.h) ) {
                                // Precinct in missing image or after the
                                // subband
                                continue;
                            }
                            else {
                                // Create the precinct
                                precArrayI[t][c][resLvl][subIdx][precIdx]
                                     = new PrecCoordInfo(
                                         (Px<0) ? 0 : Px,
                                         (Py<0) ? 0 : Py,
                                         (Px+ppx>=sb.w) ? sb.w : Px+ppx,
                                         (Py+ppy>=sb.h) ? sb.h : Py+ppy,
                                         xr, yr
                                         );
                                incrPrecIdx = true;
                            }
                        } // End precinct partition
                    } // End loop on subbands  

                    // Precinct partition is used and at least one precinct
                    // has been created for the current resolution level so we
                    // increment the precinct index for each subband in the
                    // resolution level
                    if( infoSrc.precinctPartitionUsed(c, t) && incrPrecIdx){
                        for(int s=subRange[t][c][resLvl][0] ; 
                            s<subRange[t][c][resLvl][1]+1 ; s++ ){
                            precIdxA[s]++;
                        }
                    }
    
                } // test precinct
            } // End loop on horizontal coordinate
        } // End loop on vertical coordinate
    }
    
    /**
     * Returns the code-blocks contained in the precinct which index is
     * precIdx for the tile 't', the component 'c', the resolution level 'r'
     * and the subband index 'sbIdx'. The result is returned in a Coord object
     * which is allocated if it is null. The returned object is a Vector
     * object whose first element is a Coord object containing the number of
     * code-blocks in each direction and then, cblks contains all the
     * code-blocks indexes.
     *
     * @param t the tile index
     *
     * @param c the component index
     *
     * @param r the resolution level
     *
     * @param sbIdx the subband index
     * 
     * @param precIdx the precinct index
     *
     * @param cblks the Vector object in which to return the code-blocks
     * indexes
     *
     * @return the code-blocks contained in the precinct as the first Coord
     * object stored in the Vector and then the code-blocks indexes
     * */
    public Vector getCBlkInPrec(int t, int c, int r, int sbIdx, 
            int precIdx, Vector cblks) {
            
        int pulx, puly, pw, ph;
        int cbIdx, culx, culy, cw, ch;
        int maxulx, maxuly;
        Coord numCB = new Coord();
        CBlkCoordInfo cbI;
        PrecCoordInfo pktI;
        SubbandAn curSub = null;
        
        curSub = (SubbandAn)savedSbTree[t][c].getSubbandByIdx(r,sbIdx);
        
        // Allocated cblks if it is null
        if ( cblks==null ) {
            cblks = new Vector();
        }
        else {
            // If reused, empty Vector object
            cblks.removeAllElements();
        }
        
        if( curSub.h==0 || curSub.w==0 || precArrayI[t][c][r][sbIdx]==null || 
            precIdx>=precArrayI[t][c][r][sbIdx].length){
            cblks.addElement(new Coord(0,0));
            return cblks;
        }
        
        pktI = precArrayI[t][c][r][sbIdx][precIdx];
        if ( pktI==null ) {
            // This precinct does not exist in this subband
            cblks.addElement(new Coord(0,0));
            return cblks;
        }
    
        if ( cbArrayI[t][c][r][sbIdx]==null ) {
            cblks.addElement(new Coord(0,0));
            return cblks;
        }
        
        pulx = pktI.ulx;
        puly = pktI.uly;
        pw = pktI.w;
        ph = pktI.h;
        numCB.x = numCB.y = 0;
        maxulx = maxuly = -1;
            
        for ( cbIdx=0 ; cbIdx<cbArrayI[t][c][r][sbIdx].length ; cbIdx++ ) {
            
            cbI = cbArrayI[t][c][r][sbIdx][cbIdx];
            
            culx = cbI.ulx - curSub.ulx;
            culy = cbI.uly - curSub.uly;
            cw = cbI.w;
            ch = cbI.h;
            
            if(infoSrc.precinctPartitionUsed(c, t)){
                // Precinct partition is used
                if ( (culx>=pulx) && ((culx+cw)<=(pw)) && 
                     (culy>=puly) && ((culy+ch)<=(ph)) ) {
                    // Code block is in precinct 
                    if ( culx>maxulx ) {
                        numCB.x++;
                        maxulx = culx;
                    }

                    if ( culy>maxuly ) {
                        numCB.y++;
                        maxuly = culy;
                    }
                    cblks.addElement(new Integer(cbIdx));
                }
            }
            else {
                // Precinct partition is not used i.e. all the code-blocks are
                // in the precinct
                if ( culx>maxulx ) {
                    numCB.x++;
                    maxulx = culx;
                }
                if ( culy>maxuly ) {
                    numCB.y++;
                    maxuly = culy;
                }
                cblks.addElement(new Integer(cbIdx));
            }
        }
        // Insert the number of code-blocks contained in a Coord object at the
        // begining of the vector
        cblks.insertElementAt(numCB, 0);
        return cblks;
    }
    
    /**
     * Finds the number of subbands in each resolution level according to the
     * decomposition tree for the specified tile and component. Since JPEG
     * 2000 part I only supports dyadic decomposition, there is one subbands
     * in resolution level 0 and 3 in each other (numbered from 1 to 3).
     *
     * @param c The component
     *
     * @param t the tile
     *
     * @return Minimun and maximum subband identifier for each resolution
     * level (First index= resolution level, second index = 0(minimum) or
     * 1(maximum))
     * */
    private int[][] findSubInResLvl(int c, int t){
        int[][] subRange = new int[mrl[t][c]+1][2];
        
        // Dyadic decomposition
        for(int i=mrl[t][c]; i>0; i--){
            subRange[i][0] = 1;
            subRange[i][1] = 3;
        }
        subRange[0][0] = 0;
        subRange[0][1] = 0;

        return subRange;
    }
    
    /**
     * Returns the 'sot_eotArray' for the specified component, tile and
     * resolution level. This method is used by the rate allocator.
     *
     * @param c The component
     *
     * @param t The tile
     *
     * @param r The resolution level
     * */
    public Coord[] getSotEotArray(int t, int c, int r){
        return sot_eotArray[t][c][r];
    }
    
    /**
     * Returns the 'sot_eotArrayMax' for the specified component and tile.
     * This method is used by the rate allocator.
     *
     * @param c The component
     *
     * @param t The tile
     * */
    public Coord[] getSotEotArrayMax(int t, int c) {
        return sot_eotArrayMax[t][c];
    }
    
    /**
     * Returns the 'incArray' for the specified component, tile and resolution
     * level. This method is used by the rate allocator.
     *
     * @param c The component
     *
     * @param t The tile
     *
     * @param r The resolution level
     * */
    public Coord getIncArray(int t, int c, int r){
        return incArray[t][c][r];
    }
    
    /**
     * Returns the 'incArrayMax' for the specified component and tile.  This
     * method is used by the rate allocator.
     *
     * @param c The component
     *
     * @param t The tile
     * */
    public Coord getIncArrayMax(int t, int c) {
        return incArrayMax[t][c];
    }
    
    /** 
     * Returns the maximum resolution level for all tile-components. This
     * method is used by the rate allocator.
     * */
    public int[][] getMRL(){
        return mrl;
    }
    
    /** 
     * Returns true if the current packet is writable i.e. should be written.
     * Returns false otherwise.
     * */
    public boolean isPacketWritable() {
        return packetWritable;
    }

    /** 
     * Tells if there was ROI information in the last written packet 
     * */
    public boolean isROIinPkt(){
        return roiInPkt;
    }

    /** Gives the length to read in current packet body to get all ROI
     * information */
    public int getROILen(){
        return roiLen;
    }

    /**
     * Returns the parameters that are used in this class and implementing
     * classes. It returns a 2D String array. Each of the 1D arrays is for a
     * different option, and they have 3 elements. The first element is the
     * option name, the second one is the synopsis, the third one is a long
     * description of what the parameter is and the fourth is its default
     * value. The synopsis or description may be 'null', in which case it is
     * assumed that there is no synopsis or description of the option,
     * respectively. Null may be returned if no options are supported.
     *
     * @return the options name, their synopsis and their explanation, 
     * or null if no options are supported.
     * */
    public static String[][] getParameterInfo() {
        return pinfo;
    }
}
