/*****************************************************************************
 *
 * $Id: ICCProfileNotFoundException.java,v 1.1.1.1 2002/08/02 09:47:03 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 *****************************************************************************/

package icc;

/**
 * This exception is thrown when an image contains no icc profile.
 * is incorrect.
 * 
 * @see		jj2000.j2k.icc.ICCProfile
 * @version	1.0
 * @author	Bruce A. Kern
 */

public class ICCProfileNotFoundException extends ICCProfileException {

    /**
     * Contruct with message
     *   @param msg returned by getMessage()
     */
    ICCProfileNotFoundException (String msg) {
        super (msg); }


    /**
     * Empty constructor
     */
    ICCProfileNotFoundException () {
        super ("no icc profile in image"); }
    
    /* end class ICCProfileNotFoundException */ }




