/* 
 * CVS identifier:
 * 
 * $Id:
 * 
 * Class:                   InvWTHelper
 * 
 * Description:             Provides general implementation of some methods of 
 *                          the InvTransform abstract class.
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
 * 
 * 
 * 
 */


package jj2000.j2k.wavelet.synthesis;

import jj2000.j2k.wavelet.*;
/**
 * This abstract class provides default implementations of some of methods
 * that are useful for inverse wavelet transforms. The implementations are
 * general and can suit most inverse DWT implementation.
 *
 * */
public class InvWTHelper {

    /** The source of wavelet transform coefficients for the transform for
     * which this object is intended to be the helper. */
    InvWTData src;
    
    /**
     * The reversible flag for each component in each tile. The first index is
     * the tile index, the second one is the component index. The
     * reversibility of the components for each tile are calculated on a as
     * needed basis.
     * */
    boolean reversible[][];
    
    /**
     * Instantiates a new 'InvWTHelper' object to work with the specified
     * source of wavelet coefficients.
     *
     * @param src The source of wavelet coefficients for the wavelet transform 
     * to "help".
     *
     *
     * */
    public InvWTHelper(InvWTData src) {
        this.src = src;
        reversible = new boolean[src.getNumTiles()][];
    }
    
    /**
     * Returns the reversibility of the current subband. It computes
     * iteratively the reversibility of the child subbands. For each
     * subband it tests the reversibility of the horizontal and
     * vertical synthesis filters used to reconstruct this subband.
     *
     * @param subband The current subband.
     *
     * @return true if all the  filters used to reconstruct the current 
     * subband are reversible
     *
     *
     * */
    private boolean isSubbandReversible(Subband subband) {
        if(subband.isNode) {
            // It's reversible if the filters to obtain the 4 subbands
            // are reversible and the ones for this one are reversible 
            // too.
            return
                isSubbandReversible(subband.getLL()) &&
                isSubbandReversible(subband.getHL()) &&
                isSubbandReversible(subband.getLH()) &&
                isSubbandReversible(subband.getHH()) &&
                ((SubbandSyn)subband).hFilter.isReversible() && 
                ((SubbandSyn)subband).vFilter.isReversible();
        }
        else {
            // Leaf subband. Reversibility of data depends on source, so say
            // it's true
            return true;
        } 
    }
    
    /**
     * Returns the reversibility of the wavelet transform for the
     * specified tile-component. A wavelet transform is reversible
     * when it is suitable for lossless and lossy-to-lossless
     * compression.
     *
     * @param t The index of the tile.
     *
     * @param c The index of the component.
     *
     * @return true is the wavelet transform is reversible, false if not.
     *
     *
     * */
    public boolean isReversible(int t,int c) {
        if (reversible[t] == null) {
            // Reversibility not yet calculated for this tile
            reversible[t] = new boolean[src.getNumComps()];
            for (int i=reversible.length-1; i>=0 ; i--) {
                reversible[t][i] =
                    isSubbandReversible(src.getSubbandTree(t,i));
            }
        }
        return reversible[t][c];
    }
    
}
