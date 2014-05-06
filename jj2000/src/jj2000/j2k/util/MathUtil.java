/*
 * CVS identifier:
 *
 * $Id: MathUtil.java,v 1.12 2000/12/05 22:47:08 grosbois Exp $
 *
 * Class:                   MathUtil
 *
 * Description:             Utility mathematical methods
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


package jj2000.j2k.util;

/**
 * This class contains a collection of utility methods fro mathematical
 * operations. All methods are static.
 * */
public class MathUtil {

    /**
     * Method that calculates the floor of the log, base 2, 
     * of 'x'. The calculation is performed in integer arithmetic,
     * therefore, it is exact.
     *
     * @param x The value to calculate log2 on.
     *
     * @return floor(log(x)/log(2)), calculated in an exact way.
     * */
    public static int log2(int x) {
        int y,v;
        // No log of 0 or negative
        if (x <= 0) {
            throw new IllegalArgumentException(""+x+" <= 0");
        }
        // Calculate log2 (it's actually floor log2)
        v = x;
        y = -1;
        while (v>0) {
            v >>=1;
            y++;
        }
        return y;
    }
}
