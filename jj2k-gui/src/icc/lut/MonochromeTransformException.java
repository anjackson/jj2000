/*****************************************************************************
 *
 * $Id: MonochromeTransformException.java,v 1.1.1.1 2002/08/02 09:47:04 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 *****************************************************************************/

package icc.lut;

/**
 * Exception thrown by MonochromeTransformTosRGB.
 * 
 * @see		jj2000.j2k.icc.lut.MonochromeTransformTosRGB
 * @version	1.0
 * @author	Bruce A. Kern
 */

public class MonochromeTransformException extends Exception {

    /**
     * Contruct with message
     *   @param msg returned by getMessage()
     */
    MonochromeTransformException (String msg) {
        super (msg); }

    /**
     * Empty constructor
     */
    MonochromeTransformException () {
    }
    
    /* end class MonochromeTransformException */ }




