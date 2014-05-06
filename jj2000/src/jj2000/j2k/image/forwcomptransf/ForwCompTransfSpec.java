/* 
 * CVS identifier:
 * 
 * $Id: ForwCompTransfSpec.java,v 1.5 2001/02/27 19:15:11 grosbois Exp $
 * 
 * Class:                   ForwCompTransfSpec
 * 
 * Description:             Component Transformation specification for encoder
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
 *  */
package jj2000.j2k.image.forwcomptransf;

import jj2000.j2k.wavelet.analysis.*;
import jj2000.j2k.wavelet.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.*;

import java.util.*;

/**
 * This class extends CompTransfSpec class in order to hold encoder specific
 * aspects of CompTransfSpec.
 *
 * @see CompTransfSpec
 * */
public class ForwCompTransfSpec extends CompTransfSpec {
    /**
     * Constructs a new 'ForwCompTransfSpec' for the specified number of
     * components and tiles and the arguments of <tt>optName</tt>
     * option. This constructor is called by the encoder. It also
     * checks that the arguments belongs to the recognized arguments
     * list.
     *
     * <P>This constructor chose the component transformation type
     * depending on the wavelet filters : RCT with w5x3 filter and ICT
     * with w9x7 filter. Note: All filters must use the same data
     * type.
     *
     * @param nt The number of tiles
     *
     * @param nc The number of components
     *
     * @param type the type of the specification module i.e. tile specific,
     * component specific or both.
     *
     * @param pl The ParameterList
     *
     * @param wfs The wavelet filter specifications
     * */
    public ForwCompTransfSpec(int nt, int nc, byte type, AnWTFilterSpec wfs,
                              ParameterList pl){
        super(nt,nc,type);

	String param = pl.getParameter("Mct");

	if(param==null){
            if(nc==3) {
                AnWTFilter[][] anfilt;
                int[] filtType = new int[nComp];
                for(int c=0;c<nComp; c++){
                    anfilt = (AnWTFilter[][])wfs.getCompDef(c);
                    filtType[c] = anfilt[0][0].getFilterType();
                }
                // Check that all filters are the same one
                boolean reject = false;
                for(int c=1; c<nComp;c++){
                    if(filtType[c]!=filtType[0])
                        reject = true;
                }
                if(reject)
                    param = "off";
                else
                    param = "on";
            }
            else 
                param = "off";

            if(param.equals("off")){
                setDefault("none");
            }
            else if(param.equals("on")){
                if(nc<3)
                    throw new IllegalArgumentException("Cannot use component"+
                                                       " transformation with "+
                                                       "less than 3 "+
                                                       "components"+
                                                       " within the image");

                // Chose component transformation depending on wavelet
                // filters used
                int filtType = getFilterType(-1,wfs);
                
                switch(filtType){
                case FilterTypes.W5X3:
                    setDefault("rct");
                    return;
                case FilterTypes.W9X7:
                    setDefault("ict");
                    return;
                default:
                    throw new IllegalArgumentException("Default filter is "+
                                                       "not JPEG 2000 part"+
                                                       " I compliant");
                }
            }
            else{
                throw new IllegalArgumentException("Default parameter of "+
                                                   "option Mct not"+
                                                   " recognized: "+param);
            }
	}

	// Parse argument
	StringTokenizer stk = new StringTokenizer(param);
	String word; // current word
	byte curSpecType = SPEC_DEF; // Specification type of the
	// current parameter
	boolean[] tileSpec = null; // Tiles concerned by the
        // specification
        Boolean value;
	
	while(stk.hasMoreTokens()){
	    word = stk.nextToken();
            
	    switch(word.charAt(0)){
	    case 't': // Tiles specification
 		tileSpec = parseIdx(word,nTiles);
		if(curSpecType==SPEC_COMP_DEF){
		    curSpecType = SPEC_TILE_COMP;
		}
		else{
		    curSpecType = SPEC_TILE_DEF;
		}
 		break;
	    case 'c': // Components specification
                throw new IllegalArgumentException("Component specific "+
                                                   " parameters"+
                                                   " not allowed with "+
                                                   "-Mct option");
            default:
                if(word.equals("off")){
                    if(curSpecType==SPEC_DEF){
                        setDefault("none");
                    }
                    else if(curSpecType==SPEC_TILE_DEF){
                        for(int i=tileSpec.length-1; i>=0; i--)
                            if(tileSpec[i]){
                                setTileDef(i,"none");
                            }
                    }   
                }
                else if(word.equals("on")){
                    if(nc<3)
                        throw new 
                            IllegalArgumentException("Cannot use component"+
                                                     " transformation with"+
                                                     " less than 3 "+
                                                     "components"+
                                                     " within the image");

                    // Get default filter type
                    int filtType = getFilterType(-1,wfs);

                    switch(filtType){
                    case FilterTypes.W5X3:
                        if(curSpecType==SPEC_DEF){
                            setDefault("rct");
                        }
                        else if(curSpecType==SPEC_TILE_DEF){
                            for(int i=tileSpec.length-1; i>=0; i--)
                                if(tileSpec[i]){
                                    setTileDef(i,"rct");
                                }
                        }
                        break;
                    case FilterTypes.W9X7:
                        if(curSpecType==SPEC_DEF){
                            setDefault("ict");
                        }
                        else if(curSpecType==SPEC_TILE_DEF){
                            for(int i=tileSpec.length-1; i>=0; i--)
                                if(tileSpec[i]){
                                    setTileDef(i,"ict");
                                }
                        }
                        break;
                    default:
                        throw new 
                            IllegalArgumentException("Default filter is "+
                                                     "not JPEG 2000 part"+
                                                     " I compliant");
                    }
                }
                else{
                    throw new IllegalArgumentException("Default parameter of "+
                                                       "option Mct not"+
                                                       " recognized: "+param);
                }

		// Re-initialize
		curSpecType = SPEC_DEF;
		tileSpec = null;
		break;
	    }
	}

        // Check that default value has been specified
        if(getDefault()==null){
            int ndefspec = 0;
            for(int t=nt-1; t>=0; t--){
                for(int c=nc-1; c>=0 ; c--){
                    if(specValType[t][c] == SPEC_DEF){
                        ndefspec++;
                    }
                }
            }
            
            // If some tile-component have received no specification, it takes
            // the default value defined in ParameterList
            if(ndefspec!=0){
                param = pl.getDefaultParameterList().getParameter("Mct");

                if(param==null){
                    if(nc==3) {
                        AnWTFilter[][] anfilt;
                        int[] filtType = new int[nComp];
                        for(int c=0;c<nComp; c++){
                            anfilt = (AnWTFilter[][])wfs.getCompDef(c);
                            filtType[c] = anfilt[0][0].getFilterType();
                        }
                        // Check that all filters are the same one
                        boolean reject = false;
                        for(int c=1; c<nComp;c++){
                            if(filtType[c]!=filtType[0])
                                reject = true;
                        }
                        if(reject)
                            param = "off";
                        else
                            param = "on";
                    }
                    else 
                        param = "off";
                    
                    if(param.equals("off")){
                        setDefault("none");
                    }
                    else if(param.equals("on")){
                        if(nc<3)
                            throw new 
                                IllegalArgumentException("Cannot use "+
                                                         "component"+
                                                         " transformation"+
                                                         " with "+
                                                         "less than 3 "+
                                                         "components"+
                                                         " within the image");

                        // Chose component transformation depending on wavelet
                        // filters used
                        int filtType = getFilterType(-1,wfs);
                        
                        switch(filtType){
                        case FilterTypes.W5X3:
                            setDefault("rct");
                            return;
                        case FilterTypes.W9X7:
                            setDefault("ict");
                            return;
                        default:
                            throw new 
                                IllegalArgumentException("Default filter is "+
                                                         "not JPEG 2000 part"+
                                                         " I compliant");
                        }
                    }
                    else{
                        throw new 
                            IllegalArgumentException("Default parameter of "+
                                                     "option Mct not"+
                                                     " recognized: "+param);
                    }
                } else if(param.equals("off")){
                    setDefault("none");
                }
                else if(param.equals("on")){
                    if(nc<3)
                        throw new IllegalArgumentException("Cannot use "+
                                                           "component"+
                                                           " transformation "+
                                                           "with "+
                                                           "less than 3 "+
                                                           "components"+
                                                           " within the "+
                                                           "image");

                    int filterType = getFilterType(-1,wfs);
                
                    switch(filterType){
                    case FilterTypes.W5X3:
                        setDefault("rct");
                        return;
                    case FilterTypes.W9X7:
                        setDefault("ict");
                        return;
                    default:
                        throw new IllegalArgumentException("Default filter"+
                                                           " is "+
                                                           "not JPEG 2000"+
                                                           " part"+
                                                           " I compliant");
                    }
                }
                else{
                    throw new IllegalArgumentException("Default parameter of "+
                                                       "option Mct not"+
                                                       " recognized: "+param);
                }
            }
            else{
                // All tile-component have been specified, takes the first
                // tile-component value as default.
                setDefault(getTileCompVal(0,0));
                switch(specValType[0][0]){
                case SPEC_TILE_DEF:
                    for(int c=nc-1; c>=0; c--){
                        if(specValType[0][c]==SPEC_TILE_DEF)
                            specValType[0][c] = SPEC_DEF;
                    }
                    tileDef[0] = null;
                    break;
                case SPEC_COMP_DEF:
                    for(int t=nt-1; t>=0; t--){
                        if(specValType[t][0]==SPEC_COMP_DEF)
                            specValType[t][0] = SPEC_DEF;
                    }
                    compDef[0] = null;
                    break;
                case SPEC_TILE_COMP:
                    specValType[0][0] = SPEC_DEF;
                    tileCompVal.put("t0c0",null);
                    break;
                }
            }
	}
        
        // Check validity of component transformation of each tile compared to
        // the filter used.
        for(int t=nt-1; t>=0; t--){

            if(((String)getTileDef(t)).equals("none")){
                // No comp. transf is used. No check is needed
                continue;
            }
            else if(((String)getTileDef(t)).equals("rct")){
                // Tile is using Reversible component transform
                int filterType = getFilterType(t,wfs);
                switch(filterType){
                case FilterTypes.W5X3: // OK
                    break;
                case FilterTypes.W9X7: // Must use ICT
                    if(isTileSpecified(t)){
                        // User has requested RCT -> Error
                        throw new IllegalArgumentException("Cannot use RCT "+
                                                           "with 9x7 filter "+
                                                           "in tile "+t);
                    }
                    else{ // Specify ICT for this tile
                        setTileDef(t,"ict");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Default filter is "+
                                                       "not JPEG 2000 part"+
                                                       " I compliant");
                }
            }
            else{ // ICT
                int filterType = getFilterType(t,wfs);
                switch(filterType){
                case FilterTypes.W5X3: // Must use RCT
                    if(isTileSpecified(t)){
                        // User has requested ICT -> Error
                        throw new IllegalArgumentException("Cannot use ICT "+
                                                           "with filter 5x3 "+
                                                           "in tile "+t);
                    }
                    else{
                        setTileDef(t,"rct");
                    }
                    break;
                case FilterTypes.W9X7: // OK
                    break;
                default:
                    throw new IllegalArgumentException("Default filter is "+
                                                       "not JPEG 2000 part"+
                                                       " I compliant");
                    
                }
            }
        }
    }

    /** Get the filter type common to all component of a given tile. If the
     * tile index is -1, it searches common filter type of default
     * specifications.
     *
     * @param t The tile index
     *
     * @param wfs The analysis filters specifications 
     *
     * @return The filter type common to all the components 
     *
     */
    private int getFilterType(int t, AnWTFilterSpec wfs){
        AnWTFilter[][] anfilt;
        int[] filtType = new int[nComp];
        for(int c=0;c<nComp; c++){
            if(t==-1)
                anfilt = (AnWTFilter[][])wfs.getCompDef(c);
            else
                anfilt = (AnWTFilter[][])wfs.getTileCompVal(t,c);
            filtType[c] = anfilt[0][0].getFilterType();
        }
        
        // Check that all filters are the same one
        boolean reject = false;
        for(int c=1; c<nComp;c++){
            if(filtType[c]!=filtType[0])
                reject = true;
        }
        if(reject){
            throw new IllegalArgumentException("Can not use component"+
                                               " transformation when "+
                                               "components do not use "+
                                               "the same filters");
        }
        return filtType[0];
    }
}
