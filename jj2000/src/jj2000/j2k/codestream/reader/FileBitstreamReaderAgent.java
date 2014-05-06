/*
 * CVS identifier:
 *
 * $Id: FileBitstreamReaderAgent.java,v 1.44 2001/03/02 10:09:13 grosbois Exp $
 *
 * Class:                   FileBitstreamReaderAgent
 *
 * Description:             Retrieve code-blocks codewords in the bit stream
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
import jj2000.j2k.codestream.*;
import jj2000.j2k.decoder.*;
import jj2000.j2k.entropy.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.io.*;
import jj2000.j2k.*;

import java.util.*;
import java.io.*;

/**
 * This class reads the bit stream (with the help of HeaderDecoder for tile
 * headers and PktDecoder for packets header and body) and retrives location
 * of all code-block's codewords.
 *
 * <P>Note: All tile-parts headers are read by the constructor whereas packets
 * are processed when decoding related tile (when setTile method is called).
 *
 * <P>In parsing mode, the reader simulates a virtual layer-resolution
 * progressive bit stream with the same truncation points in each code-block,
 * whereas in truncation mode, only the first bytes are taken into account (it
 * behaves like if it is a real truncated codestream).
 *
 * @see HeaderDecoder
 * @see PktDecoder
 * */
public class FileBitstreamReaderAgent extends BitstreamReaderAgent 
    implements Markers, ProgressionType, StdEntropyCoderOptions{

    /** Reference to the PktDecoder instance */
    public PktDecoder pktDec;
    
    /** Reference to the ParameterList instance */
    private ParameterList pl;

    /** The RandomAccessIO where to get data from */
    private RandomAccessIO in;

    /** The number of tiles in the image */
    private int nt;

    /** Offset of the first packet in each tile-part in each tile */
    private int[][] firstPackOff;

    /** Number of bytes allocated to each tile. In parsing mode, this number
     * is related to the tile length in the codestream whereas in truncation
     * mode all the rate is affected to the first tiles. */
    private int[] nBytes;

    /** Length of each tile-part (written in Psot) */
    private int[][] tilePartLen;

    /** Total length of each tile */
    private int[] totTileLen;

    /** Total length of tiles' header */
    private int[] totTileHeadLen;

    /** First tile part header length*/
    private int firstTilePartHeadLen;

    /** Total length of all tile parts in all tiles */
    private double totAllTileLen;

    /** Length of main header */
    private int mainHeadLen;

    /** Length of all tile-part headers */
    private int[][] tilePartHeadLen;

    /** Length of each packet head found in the tile */
    private Vector pktHL;

    /** True if truncation mode is used. False if parsing mode */
    private boolean isTruncMode;

    /** The number of tile-parts that remain to read */
    private int remainingTileParts;

    /** The number of tile-parts read so far for each tile */
    private int[] tilePartsRead;

    /** Thetotal  number of tile-parts read so far */
    private int totTilePartsRead=0;

    /** The number of tile-parts in each tile */
    private int[] tileParts;

    /** The current tile part being used */
    private int curTilePart;

    /** The number of the tile-part in the codestream */
    private int[][] tilePartNum;

    /** Array containing info. for all the code-blocks:<br>
     * - 1st dim: component index.<br>
     * - 2nd dim: resolution level index.<br>
     * - 3rd dim: subband index.<br>
     * - 4th/5th dim: code-block index (vert. and horiz.).<br>
     */
    private CBlkInfo[][][][][] cbI;

    /** 
     * Reads all tiles headers and keep offset of their first packet. Finally
     * the rate allocation method is called.
     *
     * @param hd HeaderDecoder of the codestream.
     *
     * @param ehs The input stream where to read bit-stream.
     *
     * @param decSpec The decoder specifications 
     *
     * @param pl The ParameterList instance created from the command-line
     * arguments.
     *
     * @see #allocateRate
     * */
    public FileBitstreamReaderAgent(HeaderDecoder hd,RandomAccessIO ehs,
                                    DecoderSpecs decSpec,ParameterList pl)
    throws IOException {
        super(hd,decSpec);
        
        this.pl = pl;
        String strInfo = "Codestream elements information in bytes "+
            "(offset, total length, header length):\n\n";
        
        // Get decoding rate
        boolean rateInBytes;
        boolean parsing = pl.getBooleanParameter("parsing");
        try{
            trate = pl.getFloatParameter("rate");
        }
        catch (NumberFormatException e) {
            throw new Error("Invalid value in 'rate' option: "+
                  pl.getParameter("rate"));
        }
        catch (IllegalArgumentException e) {
            throw new Error("'rate' option is missing");
        }
        
        try{
            tnbytes = pl.getIntParameter("nbytes");
        }
        catch (NumberFormatException e) {
            throw new Error("Invalid value in 'nbytes' option: "+
                  pl.getParameter("nbytes"));
        }
        catch (IllegalArgumentException e) {
            throw new Error("'nbytes' option is missing");
        }
        
        // Check that '-rate' and '-nbytes' are not used at the same time
        ParameterList defaults = pl.getDefaultParameterList();
        if(tnbytes != defaults.getFloatParameter("nbytes"))
            rateInBytes = true;
        else
            rateInBytes = false;

        if(rateInBytes && trate != defaults.getIntParameter("rate")) {
            throw new Error("Cannot use '-rate' and '-nbytes' options at the "+
                  "same time.");
        }

        if(rateInBytes){
            trate = tnbytes*8f/hd.getImgWidth()/hd.getImgHeight();
        }
        else {
            tnbytes = (int)(trate*hd.getImgWidth()*hd.getImgHeight())/8;
        }
        isTruncMode = !pl.getBooleanParameter("parsing");

        // initializations
        nt = ntX * ntY;
        in = ehs;
        pktDec = new PktDecoder(decSpec,hd,ehs,this,isTruncMode);

        tileParts = new int[nt];
        totTileLen = new int[nt];
	tilePartLen = new int[nt][];
        tilePartNum = new int[nt][];
        firstPackOff = new int[nt][];
        tilePartsRead = new int[nt];
        totTileHeadLen = new int[nt];
	tilePartHeadLen = new int[nt][];
	nBytes = new int[nt];
        hd.nTileParts = new int[nt];


	this.isTruncMode = isTruncMode;
        int t=0, pos, tp=0, tptot=0;

	// Keeps main header's length, takes file format into account
        int cdstreamStart = hd.initPos; // Codestream offset in the file
	mainHeadLen = in.getPos() - cdstreamStart;
        anbytes = mainHeadLen;
        strInfo += "Main header length    : "+cdstreamStart+", "+mainHeadLen+
            ", "+mainHeadLen+"\n";

        // If cannot even read the first tile-part
        if(anbytes > tnbytes){
            throw new Error("Requested bitrate is too small.");
        }

        // Read all tile-part headers from all tiles.
	int tilePartStart;
        boolean rateReached = false;
        int mdl;
        totAllTileLen = 0;
        remainingTileParts = nt; // at least as many tile-parts as tiles
        try{
            while(remainingTileParts != 0){
                tilePartStart = in.getPos();
                // Read tile-part header
		try{
                    t = readTilePartHeader();
                    tp = tilePartsRead[t];
		}
		catch(EOFException e){
		    firstPackOff[t][tp]=in.length();
		    throw e;
		}

                pos = in.getPos();

                // In truncation mode, if target decoding rate is reached in
                // tile-part header, skips the tile-part and stop reading
                if(isTruncMode){
                    if( (pos-cdstreamStart) > tnbytes ){
                        firstPackOff[t][tp]=in.length();
                        rateReached = true;
                        break;
                    }
                }
                
                // Set tile part position and header length
                firstPackOff[t][tp] = pos;
                tilePartHeadLen[t][tp] = (pos-tilePartStart);

                strInfo += "Tile-part "+tp+" of tile "+t+" : "+tilePartStart
                    +", "+tilePartLen[t][tp]+", "+tilePartHeadLen[t][tp]+"\n";

                // Update length counters
                totTileLen[t] += tilePartLen[t][tp];
                totTileHeadLen[t] += tilePartHeadLen[t][tp];
                totAllTileLen += tilePartLen[t][tp];
                if(isTruncMode){
                    if(anbytes+tilePartLen[t][tp] > tnbytes) {
                        anbytes += tilePartHeadLen[t][tp];
                        rateReached = true;
                        nBytes[t] += (tnbytes-anbytes);
                        break;
                    }
                    else {
                        anbytes += tilePartHeadLen[t][tp];
                        nBytes[t] += (tilePartLen[t][tp]-
                                      tilePartHeadLen[t][tp]);
                    }
                }
                else {
                    if(anbytes+tilePartHeadLen[t][tp] > tnbytes) {
                        break;
                    }
                    else {
                        anbytes += tilePartHeadLen[t][tp];
                    }
                }
                
                // If this is first tile-part, remember header length
                if(tptot == 0)
                    firstTilePartHeadLen = tilePartHeadLen[t][tp];
                
                // Go to the beginning of next tile part
                tilePartsRead[t]++;
                in.seek(tilePartStart+tilePartLen[t][tp]);
                remainingTileParts--;
                tptot++;
            }
        } catch(EOFException e) {
            if(pl.getBooleanParameter("cdstr_info"))
               FacilityManager.getMsgLogger().
                   printmsg(MsgLogger.INFO,strInfo); 
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.WARNING,"Codestream truncated in tile "+t);

            // Bit-rate allocation
            if(!isTruncMode)
                allocateRate();

            // Update 'res' value once all tile-part headers are read
            if(pl.getParameter("res") == null){
                res = decSpec.dls.getMin();
            }
            else{
                try {
                    res = pl.getIntParameter("res");
                    if(res<0){
                        throw new
                            IllegalArgumentException("Specified negative "+
                                                     "resolution level "+
                                                     "index: "+res);
                    }
                }
                catch(NumberFormatException f){
                    throw new 
                        IllegalArgumentException("Invalid resolution level "+
                                                 "index ('-res' option) " +
                                                 pl.getParameter("res"));
                }
            }
            
            // Verify reduction in resolution level
            mdl = decSpec.dls.getMin();
            if(res>mdl){
                FacilityManager.getMsgLogger().
                    printmsg(MsgLogger.WARNING,
                             "Specified resolution level ("+res+") is larger"+
                             " than the maximum value. Setting it to "+
                             mdl +" (maximum value)");
                res = mdl;
            }

            return;
	}
        remainingTileParts = 0;
	
        // Update 'res' value once all tile-part headers are read
        if(pl.getParameter("res") == null){
            res = decSpec.dls.getMin();
        }
        else{
            try {
                res = pl.getIntParameter("res");
                if(res<0){
                    throw new
                        IllegalArgumentException("Specified negative "+
                                                 "resolution level index: "+
                                                 res);
                }
            }
            catch(NumberFormatException e){
                throw new 
                    IllegalArgumentException("Invalid resolution level "+
                                             "index ('-res' option) " +
                                             pl.getParameter("res"));
            }
        }
        
        // Verify reduction in resolution level
        mdl = decSpec.dls.getMin();
        if(res>mdl){
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,
                         "Specified resolution level ("+res+") is larger"+
                         " than the maximum possible. Setting it to "+
                         mdl +" (maximum possible)");
            res = mdl;
        }

        if(pl.getBooleanParameter("cdstr_info"))
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo); 

	// Check presence of EOC marker is decoding rate not reached
	try{
	    if(!rateReached && in.readShort() != EOC){
		FacilityManager.getMsgLogger().
		    printmsg(MsgLogger.WARNING,"EOC marker not found. "+
			     "Codestream is corrupted.");
	    }
	}
	catch(EOFException e){
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.WARNING,"EOC marker is missing");
	}

	// Bit-rate allocation
        if(!isTruncMode)
            allocateRate();
        else{
            // Take EOC into account if rate is not reached
            if(in.getPos()>=tnbytes)
                anbytes += 2;
        }
    }

    /** 
     * Allocates output bit-rate for each tile in parsing mode: The allocator
     * simulates the truncation of a virtual layer-resolution progressive
     * codestream.
     * */
    private void allocateRate() throws IOException {
	int stopOff = tnbytes;

	// In parsing mode, the bitrate is allocated related to each tile's
	// length in the bit stream

        // EOC marker's length 
        anbytes += 2;
            
        // If there are too few bytes to read the tile part headers throw an
        // error
        if(anbytes > stopOff){
            throw new Error("Requested bitrate is too small for parsing");
        }
        
        // Calculate bitrate for each tile
        int rem = stopOff-anbytes;
        int totnByte = rem;
        for(int t=nt-1; t>0; t--){
            rem -= nBytes[t]=(int)(totnByte*(totTileLen[t]/totAllTileLen));
        }
        nBytes[0] = rem;
    }

    /** 
     * Reads SOT marker segment of the tile-part header and calls related
     * methods of the HeaderDecoder to read other markers segments. The
     * tile-part header is entirely read when a SOD marker is encountered.
     *
     * @return The tile number of the tile part that was read
     * */
    private int readTilePartHeader() throws IOException{

        int tile, psot, tilePart;
        // SOT marker
        if(in.readShort() != SOT){ // check for SOT flag
             throw new CorruptedCodestreamException("SOT tag not found "+
                                                    "in tile-part start");
        }

        // Lsot (shall equals 10)
        int lsot = in.readUnsignedShort();
        if(lsot != 10)
            throw new CorruptedCodestreamException("Wrong length for "+
                                                   "SOT marker segment: "+
                                                   lsot); 

        // Isot
        tile = in.readUnsignedShort();
        if(tile>65534){
            throw new CorruptedCodestreamException("Tile index too high in "+
                                                   "tile-part.");
        }

        // Psot
        psot = in.readInt();
        if(psot<0) {
            throw new NotImplementedError("Tile length larger "+
					  "than maximum supported");
        }
        // TPsot
        tilePart = in.read();
        if( tilePart!=tilePartsRead[tile] || tilePart<0 || tilePart>254 )
            throw new CorruptedCodestreamException("Out of order tile-part");
	// TNsot
	int nrOfTileParts=in.read();
 
        if(tilePart == 0){
            remainingTileParts += nrOfTileParts- 1;
            tileParts[tile] = nrOfTileParts;
            tilePartLen[tile] = new int[nrOfTileParts];
            tilePartNum[tile] = new int[nrOfTileParts];
            firstPackOff[tile] = new int[nrOfTileParts];
            tilePartHeadLen[tile] = new int[nrOfTileParts];
        }

        // Other markers
        hd.resetHeaderMarkers();
        hd.nTileParts[tile] = nrOfTileParts;
	// Decode and store the tile-part header (i.e. until a SOD marker is
	// found)
        do {
	    hd.extractTilePartMarkSeg(in.readShort(),in,tile);
        } while((hd.markersFound & hd.SOD_FOUND)==0);

	// Read each marker segment previously found
	hd.readFoundTilePartMarkSeg(tile);

        tilePartLen[tile][tilePart]=psot;

        tilePartNum[tile][tilePart]=totTilePartsRead;
        totTilePartsRead++;

        // add to list of which tile each successive tile part belongs.
        // This list is needed if there are PPM markers used
        hd.setTileOfTileParts(tile);

        return tile;
    }

    /** 
     * Reads packets of the current tile according to the
     * layer-resolution-component-position progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution
     *
     * @param lye Index of the last layer
     *
     * @param ress Index of the first resolution level
     *
     * @param rese Index of the last resolution level
     *
     * @param comps Index of the first component
     *
     * @param compe Index of the last component
     *
     * @return True if rate has been reached
     * */
    private boolean readLyResCompPos(int[][] lys,int lye,int ress,int rese,
				     int comps,int compe,
                                     int curTilePart) 
	throws IOException{
        boolean pph=false;
	int nPrec; // Number of precincts
	int start; // Offset of current packet's head
	Vector[] cblks;
	Coord co;
	CBlkInfo cb;
	int t = getTileIdx(); // Current tile index
	boolean status=false; // True if decoding rate is reached when
        int lastByte = in.getPos()+tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];

	int minlys; // minimum layer start index of each component and
	// resolution
	minlys = 10000;
	for(int c=comps; c<compe; c++){ //loop on components
	    for(int r=ress; r<rese; r++) {//loop on resolution levels
		if(lys[c]!=null && r<lys[c].length && lys[c][r]<minlys)
		    minlys = lys[c][r];
	    }
	}

        int hlen,plen;
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;

        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()){
            pph = true;
        }

        // Loop on layers
        for(int l=minlys; l<lye; l++){
            // Loop on resolution levels
            for(int r=ress; r<rese; r++){
                // Loop on components
                for(int c=comps; c<compe; c++){

		    // Check if resolution level is defined for this component
		    if(r>mdl[c]) continue;

		    if(l<lys[c][r]) continue;
		    
		    nPrec = pktDec.getNumPrecinct(c,r);
                    // Loop on precints
		    for(int p=0;p<nPrec; p++){
			start = in.getPos();

                        // If packed packet headers are used, there is no need
                        // to check that there are bytes enough to read header
                        if(pph)
                            pktDec.readPktHead(l,r,c,p,cbI[c][r],nBytes);

                        // If we are about to read outside of tile-part,
                        // skip to next tile-part
                        if(start>lastByte && 
                           curTilePart < firstPackOff[t].length-1){
                            curTilePart++;
                            in.seek(firstPackOff[t][curTilePart]);
                            lastByte = in.getPos()+
                                tilePartLen[t][curTilePart]-1-
                                tilePartHeadLen[t][curTilePart]; 
                        }

                        // Read SOP marker segment if necessary
                        status = pktDec.readSOPMarker(nBytes,p,c,r);

			if(status){
                            if(pl.getBooleanParameter("cdstr_info"))
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
			    return true;
                        }

                        if(!pph)
                            status = 
                                pktDec.readPktHead(l,r,c,p,cbI[c][r],
                                                   nBytes);
			if(status) {
                            if(pl.getBooleanParameter("cdstr_info"))
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
			    return true;
                        }

			// Store packet's head length
                        hlen = in.getPos()-start;
			pktHL.addElement(new Integer(hlen));

			// Reads packet's body
			status = pktDec.readPktBody(l,r,c,p,cbI[c][r],nBytes);
                        plen = in.getPos()-start;
                        strInfo+= " Pkt l="+l+",r="+r+",c="+c+": "+start+
                            ", "+plen+", "+hlen+"\n";

			if(status) {
                            if(pl.getBooleanParameter("cdstr_info"))
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
			    return true;
                        }
		    } // End lopp on precincts
                } // End loop on components
            } // End loop on resolution levels 
        } // End loop on layers

        if(pl.getBooleanParameter("cdstr_info"))
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
	return false; // Decoding rate was not reached
    }
    
    /** 
     * Reads packets of the current tile according to the
     * resolution-layer-component-position progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution
     *
     * @param lye Index of the last layer
     *
     * @param ress Index of the first resolution level
     *
     * @param rese Index of the last resolution level
     *
     * @param comps Index of the first component
     *
     * @param compe Index of the last component
     *
     * @return True if rate has been reached
     * */
    private boolean readResLyCompPos(int lys[][],int lye,int ress,int rese,
				     int comps,int compe,
                                     int curTilePart) 
        throws IOException{
        boolean pph=false;
	int nPrec;
	int start;
	int t = getTileIdx(); // Current tile index
	boolean status=false; // True if decoding rate is reached when
        int lastByte = in.getPos()+tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];
	int minlys; // minimum layer start index of each component
        int hlen,plen;
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;

        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()){
            pph = true;
        }

        // Loop on resolution levels
        for(int r=ress; r<rese; r++){
            if(lys==null) continue;

	    minlys = 100000;
	    for(int c=comps; c<compe; c++) {
		if(lys[c]!=null && r<lys[c].length && lys[c][r]<minlys)
		    minlys = lys[c][r];
            }

            // Loop on layers
            for(int l=minlys; l<lye; l++){
                // Loop on components
                for(int c=comps; c<compe; c++){
		    // Check if resolution level is defined for this component
		    if(r>mdl[c]) continue;

		    if(l<lys[c][r]) continue;

		    nPrec = pktDec.getNumPrecinct(c,r);

                    // Loop on precints
		    for(int p=0; p<nPrec; p++){
			start = in.getPos();

                        // If packed packet headers are used, there is no need
                        // to check that there are bytes enough to read header
                        if(pph){
                            status = 
                                pktDec.readPktHead(l,r,c,p,cbI[c][r],nBytes);
                        }
                        
                        // If we are about to read outside of tile-part,
                        // skip to next tile-part  
                        if(start>lastByte && 
                           curTilePart < firstPackOff[t].length-1){
                            curTilePart++;
                            in.seek(firstPackOff[t][curTilePart]);
                            lastByte = in.getPos()+
                                tilePartLen[t][curTilePart]-1-
                                tilePartHeadLen[t][curTilePart]; 
                        }
                        
                        // Read SOP marker segment if necessary
                        status = pktDec.readSOPMarker(nBytes,p,c,r);

			if(status){
                            if(pl.getBooleanParameter("cdstr_info"))
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
			    return true;
                        }

                        if(!pph)
                            status = 
                                pktDec.readPktHead(l,r,c,p,cbI[c][r],nBytes);

			if(status){
                            if(pl.getBooleanParameter("cdstr_info"))
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
			    return true;
                        }

			// Store packet head length
                        hlen = in.getPos()-start;
			pktHL.addElement(new Integer(hlen));

                        status = pktDec.readPktBody(l,r,c,p,cbI[c][r],nBytes);
                        
                        plen = in.getPos()-start;
                        strInfo+= " Pkt l="+l+",r="+r+",c="+c+": "+start+
                            ", "+plen+", "+hlen+"\n";
			if(status) {
                            if(pl.getBooleanParameter("cdstr_info"))
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
			    return true;
                        }
                    } // End loop on precincts
                } // End loop on components
            } // End loop on layers
        } // End loop on resolution levels 

        if(pl.getBooleanParameter("cdstr_info"))
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
	return false; // Decoding rate was not reached
    }
    
    /** 
     * Reads packets of the current tile according to the
     * resolution-position-component-layer progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution
     *
     * @param lye Index of the last layer
     *
     * @param ress Index of the first resolution level
     *
     * @param rese Index of the last resolution level
     *
     * @param comps Index of the first component
     *
     * @param compe Index of the last component
     *
     * @return True if rate has been reached
     * */
    private boolean readResPosCompLy(int[][] lys,int lye,int ress,int rese,
				     int comps,int compe,
                                     int curTilePart) 
        throws IOException{

        boolean pph=false;
	int t = getTileIdx(); // Current tile index
	boolean status=false; // True if decoding rate is reached when
        int lastByte = in.getPos()+tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];

	int start;
	Coord xys[],xyInc;
	int x0,y0,x1,y1,xInc,yInc,x0_rl,y0_rl,xInc_rl,yInc_rl;
        int hlen,plen;
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;

        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()){
            pph = true;
        }

	// Coord[] xys = pktDec.getSotEotArrayMax(c);
	xys = pktDec.getSotEotArrayMax(0);
	x0 = xys[0].x;
	y0 = xys[0].y;
	x1 = xys[1].x;
	y1 = xys[1].y;
	
	// Coord xyInc = pkDec.getIncArrayMax(c);
	xyInc = pktDec.getIncArrayMax(0);
	xInc = xyInc.x;
	yInc = xyInc.y;

	// precinct index
	int[][][] precIdx = new int[compe][rese][lye];

  	// Loop on resolution levels
  	for(int r=ress; r<rese; r++){
  	    // Loop on precints
	    for(int y=y0; y<y1; y+=yInc)
		for(int x=x0; x<x1; x+=xInc){

		    // Loop on components
		    for(int c=comps; c<compe; c++){
			// Check if resolution level is defined for this
			// component
			if(r>mdl[c]) continue;

			xyInc = pktDec.getIncArray(c,r);
			xInc_rl = xyInc.x;
			yInc_rl = xyInc.y;
			xys = pktDec.getSotEotArray(c,r);
			x0_rl = xys[0].x;
			y0_rl = xys[0].y;
			
			// Test on precinct
			if( ( (x==x0) || (x%xInc_rl==0) ) &&
			    ( (y==y0) || (y%yInc_rl==0) ) ){
			    
			    // Loop on layers
			    for(int l=lys[c][r]; l<lye; l++){
				start = in.getPos();

                                // If packed packet headers are used, there 
                                // is no need to check that there are bytes 
                                // enough to read header
				if(pph) 
                                    status = pktDec.
                                        readPktHead(l,r,c,precIdx[c][r][l],
                                                    cbI[c][r],nBytes);


				// If we are about to read outside of
				// tile-part, skip to next tile-part
                                if(start>lastByte && 
                                   curTilePart < firstPackOff[t].length-1){
                                    curTilePart++;
				    in.seek(firstPackOff[t][curTilePart]);
				    lastByte = in.getPos()+
					tilePartLen[t][curTilePart]-1-
					tilePartHeadLen[t][curTilePart]; 
 				}

                                // Read SOP marker segment if necessary
                                status = pktDec.
                                    readSOPMarker(nBytes,precIdx[c][r][l],c,r);
                                
                                if(status){
                                    if(pl.getBooleanParameter("cdstr_info"))
                                        FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                    return true;
                                }

				if(!pph) 
                                    status = pktDec.
                                        readPktHead(l,r,c,precIdx[c][r][l],
                                                    cbI[c][r],nBytes);
				if(status) {
                                    if(pl.getBooleanParameter("cdstr_info"))
                                        FacilityManager.getMsgLogger().
                                            printmsg(MsgLogger.INFO,strInfo);
				    return true;
                                }
				
				// Store packet head length
                                hlen = in.getPos()-start;
				pktHL.addElement(new Integer(hlen));
				
				status = 
				    pktDec.readPktBody(l,r,c,precIdx[c][r][l],
                                                       cbI[c][r],nBytes);
                                plen = in.getPos()-start;
                                strInfo+= " Pkt l="+l+",r="+r+",c="+c+": "+
                                    start+", "+plen+", "+hlen+"\n";
				if(status){
                                    if(pl.getBooleanParameter("cdstr_info"))
                                        FacilityManager.getMsgLogger().
                                            printmsg(MsgLogger.INFO,strInfo);
				    return true;
                                }
				precIdx[c][r][l]++;
			    } // End loop on layers
			} // End test on precincts
		    } // End loop on components
		} // End loop on precincts
  	} // End loop on resolution levels 

        if(pl.getBooleanParameter("cdstr_info"))
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
	return false; // Decoding rate was not reached
    }
    
    /** 
     * Reads packets of the current tile according to the
     * position-component-resolution-layer progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution
     *
     * @param lye Index of the last layer
     *
     * @param ress Index of the first resolution level
     *
     * @param rese Index of the last resolution level
     *
     * @param comps Index of the first component
     *
     * @param compe Index of the last component
     *
     * @return True if rate has been reached
     * */
    private boolean readPosCompResLy(int[][] lys,int lye,int ress,int rese,
				     int comps,int compe,
                                     int curTilePart) 
        throws IOException{
        boolean pph=false;
	int t = getTileIdx(); // Current tile index
	boolean status=false; // True if decoding rate is reached when
        int lastByte = in.getPos()+tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];

	int start;
	Coord xys[],xyInc;
	int x0,y0,x1,y1,xInc,yInc,x0_rl,y0_rl,xInc_rl,yInc_rl;
        int hlen,plen;
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;

        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()){
            pph = true;
        }

	// Coord[] xys = pktDec.getSotEotArrayMax(c);
	xys = pktDec.getSotEotArrayMax(0);
	x0 = xys[0].x;
	y0 = xys[0].y;
	x1 = xys[1].x;
	y1 = xys[1].y;
	
	// Coord xyInc = pkDec.getIncArrayMax(c);
	xyInc = pktDec.getIncArrayMax(0);
	xInc = xyInc.x;
	yInc = xyInc.y;

	// precinct index
	int[][][] precIdx = new int[compe][rese][lye];

  	// Loop on precincts
	for(int y=y0; y<y1; y+=yInc)
	    for(int x=x0; x<x1; x+=xInc){
  	    // Loop on components
  	    for(int c=comps; c<compe; c++){
  		// Loop on resolution levels
  		for(int r=ress; r<rese; r++){
		    // Check if resolution level is defined for this
		    // component
		    if(r>mdl[c]) continue;

		    xyInc = pktDec.getIncArray(c,r);
		    xInc_rl = xyInc.x;
		    yInc_rl = xyInc.y;
		    xys = pktDec.getSotEotArray(c,r);
		    x0_rl = xys[0].x;
		    y0_rl = xys[0].y;

		    // Test on precinct
		    if( ( (x==x0) || (x%xInc_rl==0) ) &&
			( (y==y0) || (y%yInc_rl==0) ) ){

			// Loop on layers
			for(int l=lys[c][r]; l<lye; l++){
			    start = in.getPos();

                            // If packed packet headers are used, there 
                            // is no need to check that there are bytes 
                            // enough to read header
                            if(pph)
                                status = pktDec.
                                    readPktHead(l,r,c,precIdx[c][r][l],
                                                cbI[c][r],nBytes);

			    // If we are about to read outside of tile-part,
			    // skip to next tile-part
                            if(start>lastByte && 
                               curTilePart < firstPackOff[t].length-1){
                                curTilePart++;
				in.seek(firstPackOff[t][curTilePart]);
				lastByte = in.getPos()+
				    tilePartLen[t][curTilePart]-1-
				    tilePartHeadLen[t][curTilePart]; 
			    }

                            // Read SOP marker segment if necessary
                            status = pktDec.
                                readSOPMarker(nBytes,precIdx[c][r][l],c,r);

                            if(status){
                                if(pl.getBooleanParameter("cdstr_info"))
                                  FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
                                return true;
                            }

                            if(!pph)
                                status = pktDec.
                                    readPktHead(l,r,c,precIdx[c][r][l],
                                                cbI[c][r],nBytes);
			    if(status) {
                                if(pl.getBooleanParameter("cdstr_info"))
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
				return true;
                            }
			    
			    // Store packet head length
                            hlen = in.getPos()-start;
			    pktHL.addElement(new Integer(hlen));
			    
			    status = 
				pktDec.readPktBody(l,r,c,precIdx[c][r][l],
                                                   cbI[c][r],nBytes);
                            plen = in.getPos()-start;
                            strInfo+= " Pkt l="+l+",r="+r+",c="+c+": "+
                                start+", "+plen+", "+hlen+"\n";
			    if(status){
                                if(pl.getBooleanParameter("cdstr_info"))
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                return true;
                            }
			    precIdx[c][r][l]++;
			} // End loop on layers
		    } // End test on precinct
  		} // End loop on resolution levels 
  	    } // End loop on components
 	} // End loop on precincts

        if(pl.getBooleanParameter("cdstr_info"))
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
	return false; // Decoding rate was not reached
    }
    
    /** 
     * Reads packets of the current tile according to the
     * component-position-resolution-layer progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution
     *
     * @param lye Index of the last layer
     *
     * @param ress Index of the first resolution level
     *
     * @param rese Index of the last resolution level
     *
     * @param comps Index of the first component
     *
     * @param compe Index of the last component
     *
     * @return True if rate has been reached
     * */
    private boolean readCompPosResLy(int lys[][],int lye,int ress,int rese,
				     int comps,int compe,
                                     int curTilePart) 
        throws IOException{
        boolean pph=false;
	int t = getTileIdx(); // Current tile index
	boolean status=false; // True if decoding rate is reached when
        int lastByte = in.getPos()+tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];

	int start;
	Coord xys[],xyInc;
	int x0,y0,x1,y1,xInc,yInc,x0_rl,y0_rl,xInc_rl,yInc_rl;
        int hlen,plen;
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;

        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()){
            pph = true;
        }

	// precinct index
	int[][][] precIdx = new int[compe][rese][lye];

	for(int c=comps; c<compe; c++){// Loop on components
	    
	    // Loop on precints
	    xys = pktDec.getSotEotArrayMax(c);
	    x0 = xys[0].x;
	    y0 = xys[0].y;
	    x1 = xys[1].x;
	    y1 = xys[1].y;
	    
	    xyInc = pktDec.getIncArrayMax(c);
	    xInc = xyInc.x;
	    yInc = xyInc.y;
	    
	    // Loop on precincts
	    for(int y=y0; y<y1; y+=yInc)
		for(int x=x0; x<x1; x+=xInc){
		    
		    // Loop on resolution levels
		    for(int r=ress; r<rese; r++){
			// Check if resolution level is defined for this
			// component
			if(r>mdl[c]) continue;

			xyInc = pktDec.getIncArray(c,r);
			xInc_rl = xyInc.x;
			yInc_rl = xyInc.y;
			xys = pktDec.getSotEotArray(c,r);
			x0_rl = xys[0].x;
			y0_rl = xys[0].y;

			// Test on precinct
			if( ( (x==x0) || (x%xInc_rl==0) ) &&
			    ( (y==y0) || (y%yInc_rl==0) ) ){
			    
			    // Loop on layers
			    for(int l=lys[c][r]; l<lye; l++){
				start = in.getPos();

                                // If packed packet headers are used, there 
                                // is no need to check that there are bytes 
                                // enough to read header
                                if(pph)
                                    status = 
                                        pktDec.
                                        readPktHead(l,r,c,precIdx[c][r][l],
                                                    cbI[c][r],nBytes);


				// If we are about to read outside of
				// tile-part, skip to next tile-part
                                if(start>lastByte && 
                                   curTilePart < firstPackOff[t].length-1){
				    curTilePart++;
				    in.seek(firstPackOff[t][curTilePart]);
				    lastByte = in.getPos()+
					tilePartLen[t][curTilePart]-1-
					tilePartHeadLen[t][curTilePart]; 
				}

                                // Read SOP marker segment if necessary
                                status = pktDec.
                                    readSOPMarker(nBytes,precIdx[c][r][l],c,r);

                                if(status){
                                    if(pl.getBooleanParameter("cdstr_info"))
                                      FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                    return true;
                                }

                                if(!pph)
                                    status = 
                                        pktDec.
                                        readPktHead(l,r,c,precIdx[c][r][l],
                                                    cbI[c][r],nBytes);
				if(status){
                                    if(pl.getBooleanParameter("cdstr_info"))
                                        FacilityManager.getMsgLogger().
                                            printmsg(MsgLogger.INFO,strInfo);
				    return true;
				}
			    
				// Store packet head length
                                hlen = in.getPos()-start;
				pktHL.addElement(new Integer(hlen));
			    
				status = 
				    pktDec.readPktBody(l,r,c,precIdx[c][r][l],
                                                       cbI[c][r],nBytes);
                                plen = in.getPos()-start;
                                strInfo+= " Pkt l="+l+",r="+r+",c="+c+": "+
                                    start+", "+plen+", "+hlen+"\n";
				if(status){
                                    if(pl.getBooleanParameter("cdstr_info"))
                                        FacilityManager.getMsgLogger().
                                            printmsg(MsgLogger.INFO,strInfo);
				    return true;
				}
				precIdx[c][r][l]++;
			    } // End loop on layers
			} // End test on precincts
		    } // End loop on resolution levels 
		} // End loop on precincts
	} // End loop on components

        if(pl.getBooleanParameter("cdstr_info"))
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
	return false; // Decoding rate was not reached
    }

    /** 
     * Finish initialization of members for specified tile, reads packets head
     * of each tile and keeps location of each code-block's codewords. The
     * last 2 tasks are done by calling specific methods of PktDecoder.
     *
     * <P>Then, if a parsing output rate is defined, it keeps information of
     * first layers only. This operation simulates a creation of a
     * layer-resolution-component progressive bit-stream which will be next
     * truncated and decoded.
     *
     * @param t Tile index
     *
     * @see PktDecoder
     * */
    private void initTile(int t) throws IOException{
	pktHL = new Vector();

        // Number of layers
        int nl = ((Integer)decSpec.nls.getTileDef(t)).intValue();

        // If packed packet headers was used, get the packet headers for this
        // tile
        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()){
            // Get packed headers as separate input stream
            ByteArrayInputStream pphbais=hd.getPackedPktHead(t);
            
            // Restarts PacketDecoder instance
            cbI = pktDec.restart(nc,mdl,nl,cbI,true,pphbais);
        }
        else{
            // Restarts PacketDecoder instance
            cbI = pktDec.restart(nc,mdl,nl,cbI,false,null);
        }

        // Reads packets of the tile according to the progression order
        int[][] pocSpec = ((int[][])decSpec.pcs.getTileDef(t));
        int nChg = (pocSpec==null) ?  0 : pocSpec.length;

	// Create an array containing information about changes (progression
	// order type, layers index start, layer index end, resolution level
	// start, resolution level end, component index start, component index
	// end). There is one row per progresion order
	int[][] change = new int[nChg+1][6];
	int idx = 0; // Index of the current progression order
	
	// First progression order is given by the COD marker.
	change[0][0] = ((Integer)decSpec.pos.getTileDef(t)).intValue();
	change[0][1] = 0; // layer start

	for(idx=0; idx<nChg; idx++){
	    change[idx][1] = pocSpec[idx][2];// layer end
	    change[idx][2] = pocSpec[idx][0]; // res. lev. start
	    change[idx][3] = pocSpec[idx][3]; // res. lev. end
	    change[idx][4] = pocSpec[idx][1]; // Comp. index start
	    change[idx][5] = pocSpec[idx][4]; // Comp. index end
	    
	    // Next progression order type 
	    change[idx+1][0] = pocSpec[idx][5];
	}
	change[idx][1] = nl; // Layer index end
	change[idx][2] = 0; // resolution level start
	change[idx][3] = decSpec.dls.getMaxInTile(t)+1; // res. level end
	change[idx][4] = 0; // Component index start
	change[idx][5] = nc; // Component index end

        // Seek to the first packet in the first tile-part
	try{
            // If in truncation mode, the first tile-part may be beyond the
            // target decoding rate. In this case, the offset of the first
            // packet is not defined.
            if(isTruncMode && firstPackOff==null || firstPackOff[t]==null)
                return;
            in.seek(firstPackOff[t][0]);
	}
	catch(EOFException e){
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.WARNING,"Codestream truncated in tile "+t);
            return;
	}

        curTilePart = 0;

        // Start and end indexes for layers, resolution levels and components.
        int lye,ress,rese,comps,compe;
	boolean status = false;
	int nb = nBytes[t];
	int[][] lys = new int[nc][];
	for(int c=0; c<nc; c++){
	    lys[c] = new int[((Integer)decSpec.dls.getTileCompVal(t,c)).
			    intValue()+1];
	}
	    
	try{
            for(int chg=0; chg<=nChg; chg++){
                lye = change[chg][1];
                ress = change[chg][2];
                rese = change[chg][3];
                comps = change[chg][4];
                compe = change[chg][5];
                
                switch(change[chg][0]){
                case LY_RES_COMP_POS_PROG:
                    status = readLyResCompPos(lys,lye,ress,rese,comps,compe,
                                              curTilePart);
                    break;
                case RES_LY_COMP_POS_PROG:
                    status = readResLyCompPos(lys,lye,ress,rese,comps,compe,
                                              curTilePart);
                    break;
                case RES_POS_COMP_LY_PROG:
                    status = readResPosCompLy(lys,lye,ress,rese,comps,compe,
                                              curTilePart);
                    break;
                case POS_COMP_RES_LY_PROG:
                    status = readPosCompResLy(lys,lye,ress,rese,comps,compe,
                                              curTilePart);
                    break;
                case COMP_POS_RES_LY_PROG:
                    status = readCompPosResLy(lys,lye,ress,rese,comps,compe,
                                              curTilePart);
                    break;
                default:
                    throw new IllegalArgumentException("Not recognized "+
                                                       "progression type");
                }
                
		// Update next first layer index 
		for(int c=comps; c<compe; c++) {
                    if(c>=lys.length) continue;
		    for(int r=ress; r<rese; r++) {
                        if(r>=lys[c].length) continue;
			lys[c][r] = lye;
                    }
                }

                if(status)
                    break;
            }
        }
	catch(EOFException e){
            // Should never happen. Truncated codestream are normally found in
            // FileBitstreamReaderAgent constructor
            throw e;
	}
	
	// In truncation mode, update the number of read bytes
	if(isTruncMode){
            anbytes += nb-nBytes[t];

	    // If truncation rate is reached
	    if(status)
                nBytes[t] = 0;
	}
       	// In parsing mode, if there is not enough rate to entirely read the
       	// tile. Then, parses the bit stream so as to create a virtual
       	// layer-resolution-component progressive bit stream that will be
       	// truncated and decoded afterwards.
        else if(nBytes[t]<(totTileLen[t]-totTileHeadLen[t])){
            CBlkInfo cb;

            // Systematicaly reject all remaining code-blocks if one
            // code-block, at least, is refused.
            boolean reject; 
            // Stop reading any data from the bit stream
            boolean stopCount = false;
            // Length of each packet's head (in an array)
            int[] pktHeadLen = new int[pktHL.size()];
            for(int i=pktHL.size()-1;i>=0;i--)
                pktHeadLen[i] = ((Integer)pktHL.elementAt(i)).intValue();
            
            // Parse each code-block, layer per layer until nBytes[t] is
            // reached
            reject = false;
            for(int l=0; l<nl; l++){ // layers
                if(cbI==null) continue;
                int nc = cbI.length;
                
                int mres = 0;
                for(int c=0; c<nc; c++){
                    if(cbI[c]!=null && cbI[c].length>mres)
                        mres = cbI[c].length;
                }
                for(int r=0; r<mres; r++){ // resolutions
                        
                    int msub = 0;
                    for(int c=0; c<nc; c++){
                        if(cbI[c]!=null && cbI[c][r]!=null 
                           && cbI[c][r].length>msub)
                            msub = cbI[c][r].length;
                    }
                    for(int s=0; s<msub; s++){ // subbands
                        // Only LL subband resolution level 0
                        if(r==0 && s!=0)
                            continue;
                        // No LL subband in resolution level > 0
                        else if(r!=0 && s==0)
                            continue;

                        int mnby=0;
                        for(int c=0; c<nc; c++){
                            if(cbI[c]!=null && cbI[c][r]!=null && 
                               cbI[c][r][s]!=null &&
                               cbI[c][r][s].length>mnby)
                                mnby = cbI[c][r][s].length;
                        }
                        for(int m=0; m<mnby; m++){

                            int mnbx = 0;
                            for(int c=0; c<nc; c++){
                                if(cbI[c]!=null && cbI[c][r]!=null && 
                                   cbI[c][r][s]!=null && cbI[c][r][s][m]!=null
                                   && cbI[c][r][s][m].length>mnbx)
                                    mnbx = cbI[c][r][s][m].length;
                            }
                            for(int n=0; n<mnbx; n++){
                                
                                for(int c=0; c<nc; c++){
                                    
                                    if(cbI[c]==null || cbI[c][r]==null || 
                                       cbI[c][r][s]==null || 
                                       cbI[c][r][s][m]==null ||
                                       cbI[c][r][s][m][n]==null )
                                        continue;
                                    cb = cbI[c][r][s][m][n];
                                    
                                    // If no code-block has been refused until
                                    // now
                                    if(!reject){
                                        // Rate is to low to allow reading of
                                        // packet's head
                                        if(nBytes[t]<pktHeadLen[cb.pktIdx[l]]){
                                            // Stop parsing
                                            stopCount = true;
                                            // Reject all next
                                            // code-blocks
                                            reject=true;
                                        }
                                        // Rate is enough to read packet's
                                        // head
                                        else{
                                            // If parsing was not stopped
                                            if(!stopCount){
                                                // Takes into account packet's
                                                // head length
                                                nBytes[t] -= 
                                                    pktHeadLen[cb.pktIdx[l]];
                                                anbytes += 
                                                    pktHeadLen[cb.pktIdx[l]];
                                                // Set packet's head length to
                                                // 0, so that it won't be
                                                // taken into account next
                                                // time
                                                pktHeadLen[cb.pktIdx[l]]=0;
                                            }
                                        }
                                    }
                                    // Code-block has no data in this layer
                                    if(cb.len[l]==0){
                                        continue;
                                    }
                                    
                                    // Accepts code-block if length is enough,
                                    // if this code-block was not refused in a
                                    // previous layer and if no code-block was
                                    // refused in current component
                                    if(cb.len[l]<nBytes[t] 
                                       && !reject){
                                        nBytes[t] -= cb.len[l];
                                        anbytes += cb.len[l];
                                    }
                                    // Refuses code-block
                                    else{
                                        // Forgets code-block's data
                                        cb.len[l]=cb.off[l]=cb.ntp[l]= 0;
                                        // Refuses all other code-block in
                                        // current and next component
                                        reject=true;
                                    }
                                    
                                } // End loop on components
                            } // End loop on horiz. code-blocks
                        } // End loop on vert. code-blocks
                    } // End loop on subbands
                } // End loop on resolutions
            } // End loop on layers
        }
        // No parsing for this tile, adds tile's body to the total
        // number of read bytes.
        else{
            anbytes += totTileLen[t]-totTileHeadLen[t];
            if(t<getNumTiles()-1){
                nBytes[t+1] += nBytes[t]-(totTileLen[t]-totTileHeadLen[t]);
            }
        }
    }



    /**
     * Changes the current tile, given the new indexes. An
     * IllegalArgumentException is thrown if the indexes do not
     * correspond to a valid tile.
     *
     * @param x The horizontal indexes the tile.
     *
     * @param y The vertical indexes of the new tile.
     * */
    public void setTile(int x,int y){

        int i;          // counter
        int ctox,ctoy;  // new current tile origin in the reference grid
        // Check validity of tile indexes
        if (x<0 || y<0 || x>=ntX || y>=ntY) {
            throw new IllegalArgumentException();
        }
        int t = (y*ntX+x);

        // Set the new current tile
        ctX = x;
        ctY = y;
        // Calculate tile relative points
        ctox = (x == 0) ? ax : px+x*ntW;
        ctoy = (y == 0) ? ay : py+y*ntH;
        for (i = nc-1; i >= 0; i--) {
            culx[i] = (ctox+hd.getCompSubsX(i)-1)/hd.getCompSubsX(i);
            culy[i] = (ctoy+hd.getCompSubsY(i)-1)/hd.getCompSubsY(i);
            offX[i] = (px+x*ntW+hd.getCompSubsX(i)-1)/hd.getCompSubsX(i);
            offY[i] = (py+y*ntH+hd.getCompSubsY(i)-1)/hd.getCompSubsY(i);
        }
        
        // Initialize subband tree and number of resolution levels
        subbTrees = new SubbandSyn[nc];
        mdl = new int[nc];
        derived = new boolean[nc];
        params = new StdDequantizerParams[nc];
        gb = new int[nc];

        for(int c=0; c<nc; c++){
            derived[c] = decSpec.qts.isDerived(t,c);
            params[c] = 
                (StdDequantizerParams)decSpec.qsss.getTileCompVal(t,c);
            gb[c] = ((Integer)decSpec.gbs.getTileCompVal(t,c)).intValue();
            mdl[c] = ((Integer)decSpec.dls.getTileCompVal(t,c)).intValue();

            subbTrees[c] =
                new SubbandSyn(getCompWidth(c,mdl[c]),
                               getCompHeight(c,mdl[c]),
                               getULX(c,mdl[c]),getULY(c,mdl[c]),mdl[c],
                               decSpec.wfs.getHFilters(t,c),
                               decSpec.wfs.getVFilters(t,c));
            initSubbandsFields(c,subbTrees[c]);
        }

        
        // Read tile's packets
        try{
            initTile(t);
        }
        catch(IOException e){
            e.printStackTrace();
            throw new Error("IO Error when reading tile "+x+" x "+y);
        }
    }

    /**
     * Advances to the next tile, in standard scan-line order (by rows
     * then columns). An NoNextElementException is thrown if the
     * current tile is the last one (i.e. there is no next tile).
     * */
    public void nextTile(){
        if (ctX == ntX-1 && ctY == ntY-1) { // Already at last tile
            throw new NoNextElementException();
        }
        else if (ctX < ntX-1) { // If not at end of current tile line
            setTile(ctX+1,ctY);
        }
        else { // Go to first tile at next line
            setTile(0,ctY+1);
        }
    }

    /**
     * Returns the specified coded code-block, for the specified
     * component, in the current tile. The first layer to return is
     * indicated by 'fl'. The number of layers that is returned depends
     * on 'nl' and the amount of data available.
     *
     * <P>The argument 'fl' is to be used by subsequent calls to this
     * method for the same code-block. In this way supplamental data
     * can be retrieved at a later time. The fact that data from more
     * than one layer can be returned means that several packets from
     * the same code-block, of the same component, and the same tile,
     * have been concatenated.
     *
     * <P>The returned compressed code-block can have its progressive
     * attribute set. If this attribute is set it means that more data 
     * can be obtained by subsequent calls to this method (subject to
     * transmission delays, etc). If the progressive attribute is not
     * set it means that the returned data is all the data that can be 
     * obtained for the specified subblock.
     *
     * <P>The compressed code-block is uniquely specified by the
     * current tile, the component (identified by 'c'), the subband
     * (indentified by 'sb') and the code-bock vertical
     * and horizontal indexes 'n' and 'm'.
     *
     * <P>The 'ulx' and 'uly' members of the returned 'DecLyrdCBlk'
     * object contain the coordinates of the top-left corner of the block,
     * with respect to the tile, not the subband.
     *
     * @param c The index of the component, from 0 to N-1.
     *
     * @param m The vertical index of the code-block to return, in the
     * specified subband.
     *
     * @param n The horizontal index of the code-block to return, in the
     * specified subband.
     *
     * @param sb The subband in whic the requested code-block is.
     *
     * @param fl The first layer to return.
     *
     * @param nl The number of layers to return, if negative all
     * available layers are returned, starting at 'fl'.
     *
     * @param ccb If not null this object is used to return the
     * compressed code-block. If null a new object is created and
     * returned. If the data array in ccb is not null then it can be
     * reused to return the compressed data.
     *
     * @return The compressed code-block, with a certain number of
     * layers determined by the available data and 'nl'.
     * */
    public DecLyrdCBlk getCodeBlock(int c, int m, int n,
                                    SubbandSyn sb, int fl, int nl,
                                    DecLyrdCBlk ccb){

        int t = getTileIdx();
        CBlkInfo rcb; // requested code-block
        int r = sb.resLvl;  // Resolution level
        int s = sb.sbandIdx; // Sub-band index
        int tpidx;
        int passtype;

        // Number of layers
        int numLayers = ((Integer)decSpec.nls.getTileDef(t)).intValue();
        int options =
            ((Integer)decSpec.ecopts.getTileCompVal(t,c)).intValue();
        if(nl<0){
            nl = numLayers-fl+1;
        }
        
        // Check validity of all the arguments
        try{
            rcb = cbI[c][r][s][m][n];

            if(fl < 1 || fl > numLayers || fl+nl-1 > numLayers){
                throw new IllegalArgumentException();
            }
        }
        catch(ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException("Block (t:"+t+", c:"+
                                               c+", r:"+r+", s:"+s+", "+m+"x"+
                                               +n+") not found in bit stream");
        }
        catch(NullPointerException e){
            throw new IllegalArgumentException("Block (t:"+t+", c:"+
                                               c+", r:"+r+", s:"+s+", "+m+"x"
                                               +n+") not found in bit stream");
        }
        
        // Create DecLyrdCBlk object if necessary
        if(ccb == null){
            ccb = new DecLyrdCBlk();
        }
        ccb.m = m;
        ccb.n = n;
        ccb.nl = 0;
        ccb.dl = 0;
        ccb.nTrunc = 0;

        if(rcb==null){
            // This code-block was skipped when reading. Returns no data
            ccb.skipMSBP = 0;
            ccb.prog = false;
	    ccb.w = ccb.h = ccb.ulx = ccb.uly = 0;
            return ccb;
        }

        // ccb initialization
        ccb.skipMSBP = rcb.msbSkipped;
        ccb.ulx = rcb.ulx;
        ccb.uly = rcb.uly;
        ccb.w = rcb.w;
        ccb.h = rcb.h;
        ccb.ftpIdx = 0;
        
        // Search for index of first truncation point (first layer where
        // length of data is not zero)
        int l=0;
        while( (l<rcb.len.length) && (rcb.len[l]==0)){
            ccb.ftpIdx += rcb.ntp[l];
            l++;
        }

        // Calculate total length, number of included layer and number of
        // truncation points
        for(l=fl-1; l<fl+nl-1; l++){
            ccb.nl++;
            ccb.dl += rcb.len[l];
            ccb.nTrunc += rcb.ntp[l];
        }

        // Calculate number of terminated segments
        int nts;
        if((options & OPT_REG_TERM) != 0){
            // Regular termination in use One segment per pass
            // (i.e. truncation point)
            nts = ccb.nTrunc-ccb.ftpIdx;
        }
        else if((options & OPT_BYPASS) != 0){
            // Lazy coding in use, but no regular termination: 1 segment upto
            // the end of the last pass of the 4th most significant bit-plane,
            // and, in each following bit-plane, one segment upto the end of
            // the 2nd pass and one upto the end of the 3rd pass.

            if(ccb.nTrunc <= FIRST_BYPASS_PASS_IDX){
                nts = 1;
            }
            else{
                nts = 1;
                // And one for each terminated pass
                for (tpidx = ccb.ftpIdx; tpidx < ccb.nTrunc; tpidx++) {
                    if (tpidx >= FIRST_BYPASS_PASS_IDX-1) {
                        passtype =
                            (tpidx+NUM_EMPTY_PASSES_IN_MS_BP)%NUM_PASSES;
                        if (passtype == 1 || passtype == 2) {
                            // lazy pass just before MQ pass or
                            // MQ pass just before lazy pass =>
                            // terminated
                            nts++;
                        }
                    }
                }
            }
        }
        else {
            // Nothing special in use, just one terminated segment
            nts = 1;
        }

        // ccb.data creation
        if(ccb.data == null || ccb.data.length<ccb.dl) {
            ccb.data = new byte[ccb.dl];
        }

        // ccb.tsLengths creation
        if (nts > 1 && (ccb.tsLengths == null || ccb.tsLengths.length < nts)) {
            ccb.tsLengths = new int[nts];
        }
        else if (nts > 1 &&
                 (options & (OPT_BYPASS|OPT_REG_TERM)) == OPT_BYPASS) {
            ArrayUtil.intArraySet(ccb.tsLengths,0);
        }
        
        // Fill ccb with compressed data
        int dataIdx = -1;
        tpidx = ccb.ftpIdx;
        int ctp = ccb.ftpIdx; // Cumulative number of truncation
        // point for the current layer layer
        int tsidx=0;
        int j;

        for(l=fl-1; l<fl+nl-1; l++){
            ctp += rcb.ntp[l];
            // No data in this layer
            if(rcb.len[l]==0)
                continue;

            // Read data
            // NOTE: we should never get an EOFException here since all
            // data is checked to be within the file.
            try{
                in.seek(rcb.off[l]);
                in.readFully(ccb.data,dataIdx+1,rcb.len[l]);
                dataIdx += rcb.len[l];
            }
            catch (IOException e) {
                JJ2KExceptionHandler.handleException(e);
            }
            
            // Get the terminated segment lengths, if any
            if (nts == 1) continue;
            if((options & OPT_REG_TERM) != 0){
                // Regular termination => each pass is terminated
                for(j=0; tpidx<ctp; j++,tpidx++){
                    if(rcb.segLen[l]!=null){
                        ccb.tsLengths[tsidx++] = rcb.segLen[l][j];
                    }
                    else{ // Only one terminated segment in packet
                        ccb.tsLengths[tsidx++] = rcb.len[l];
                    }
                }
            }
            else{
                // Lazy coding without regular termination
                for(j=0; tpidx<ctp; tpidx++){
                    if( tpidx >= FIRST_BYPASS_PASS_IDX-1 ){
                        passtype =
                            (tpidx+NUM_EMPTY_PASSES_IN_MS_BP)%NUM_PASSES;
                        if( passtype!=0 ){
                            // lazy pass just before MQ pass or MQ
                            // pass just before lazy pass =>
                            // terminated
                            if(rcb.segLen[l]!=null){
                                ccb.tsLengths[tsidx++] += rcb.segLen[l][j++];
                                rcb.len[l] -= rcb.segLen[l][j-1];
                            }
                            else { // Only one terminated segment in packet
                                ccb.tsLengths[tsidx++] += rcb.len[l];
                                rcb.len[l] = 0;
                            }
                        }

                    }
                }

                // Last length in packet always in (either terminated segment
                // or contribution to terminated segment)
                if( rcb.segLen[l]!=null && j<rcb.segLen[l].length ){
                    ccb.tsLengths[tsidx] += rcb.segLen[l][j];
                    rcb.len[l] -= rcb.segLen[l][j];
                }
                else { // Only one terminated segment in packet
                    if(tsidx<nts){
                        ccb.tsLengths[tsidx] += rcb.len[l];
                        rcb.len[l] = 0;
                    }
                }
            }
        }
       if( nts==1 && ccb.tsLengths!=null ) {
           ccb.tsLengths[0] = ccb.dl;
       }

       // Set the progressive flag
       int lastlayer = fl+nl-1;
       if(lastlayer<numLayers-1){
           for(l=lastlayer+1; l<numLayers; l++){
               // It remains data for this code-block in the bit stream
               if(rcb.len[l]!=0){
                   ccb.prog = true;
               }
           }
       }
       return ccb;
    }
}
