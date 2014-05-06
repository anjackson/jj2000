// $Id: FeedbackMode.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: FeedbackMode.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.5  2000/08/17 11:40:58  edwin
// java.* -> xjava.*
//
// Revision 1.4  1997/11/20 19:39:32  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3  1997/11/07 05:53:25  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/05 08:01:56  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/08/28  David Hopwood
// + Made this class implement SymmetricCipher.
//
// Revision 0.1.0.0  1997/08/08  David Hopwood
// + Original version, split from java.security.Mode.
// + Removed setInitializationVector() with no arguments (zero IVs
//   are not a good idea in general).
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.mode;

import xjava.security.Cipher;
import xjava.security.FeedbackCipher;
import java.security.InvalidParameterException;
import xjava.security.Mode;
import xjava.security.SymmetricCipher;

/**
 * <samp>FeedbackMode</samp> is used to provide the functionality of an
 * encryption mode, such as CBC, CFB, or OFB, that works as a feedback
 * cipher, where the size of the IV and feedback buffer are equal to the
 * cipher's block size.
 * <p>
 * It is internal to Cryptix, and not intended to be used directly by
 * applications.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
abstract class FeedbackMode
extends Mode
implements FeedbackCipher, SymmetricCipher
{

// Variables
//...........................................................................

    /** Value of the current feedback register/queue/buffer. */
    protected byte[] ivBlock;

    /** Value of the IV at initialisation phase as supplied by user. */
    protected byte[] ivStart;

    /** Index into the ivBlock. */
    protected int currentByte;

    /** Size of the feedback register/queue/buffer. */
    protected int length;


// Constructor
//...........................................................................
    
    /**
     * Constructor for a Mode. This constructor is only for use 
     * by subclasses, which should pass the correct arguments to convey 
     * their behaviour to the superclass.  Applications typically do
     * not use Mode classes directly; they should call one of the 
     * <code><a href="java.security.Cipher.html#getInstance">Cipher.getInstance</a></code>
     * factory methods instead.
     *
     * @param  implBuffering    if true, this argument indicates that data
     *                          will be passed from update/crypt to
     *                          engineUpdate/engineCrypt without modification.
     * @param  implPadding      if true, this argument indicates that the
     *                          implementation can perform padding, and that
     *                          the engineCrypt method will be called when
     *                          padding is required.
     * @param  provider         the name of the provider of the underlying
     *                          cryptographic engine.
     * @exception NullPointerException if provider == null
     */
    protected FeedbackMode(boolean implBuffering, boolean implPadding, String provider) {
        super(implBuffering, implPadding, provider);
    }


// Own methods
//...........................................................................

    /**
     * <b>SPI</b>: Sets the underlying cipher.
     * <p>
     * For example, to create an IDEA cipher in CBC mode, the cipher
     * for "IDEA" would be passed to the mode for "CBC" using
     * this method. It is called once, immediately after the mode
     * object is constructed.
     * <p>
     * Subclasses that override this method (to do initialization that
     * depends on the cipher being set) should call
     * <code>super.engineSetCipher(cipher)</code> first.
     *
     * @param  cipher   the underlying cipher object
     * @exception NullPointerException if cipher == null
     */
    protected void engineSetCipher(Cipher cipher) {
        super.engineSetCipher(cipher);
        length = cipher.blockSize();
        ivStart = null;
        ivBlock = new byte[length];
        currentByte = 0;
    }


// FeedbackCipher default implementations
//...........................................................................

    /**
     * Sets the initialization vector.
     * <p>
     * Note: in JavaSoft's version of JCE, this method may only be
     * called when the cipher is in the <code>UNINITIALIZED</code> state.
     * In IJCE that is relaxed to also allow it to be called after
     * <code>initEncrypt</code>/<code>initDecrypt</code>, but before the
     * first call to <code>update</code> or <code>crypt</code>, provided
     * that the IV is not set twice.
     *
     * @param  iv   the initialization vector.
     * @exception InvalidParameterException if the initialization vector
     *              is of the wrong length or has already been set.
     */
    public void setInitializationVector(byte[] iv)
    throws InvalidParameterException {
        if (ivStart != null)
            throw new InvalidParameterException(
                getMode() + ": Initialization vector is already set");

        if (iv.length != length)
            throw new InvalidParameterException(
                getMode() + ": Initialization vector length = " + iv.length +
                ", should be " + length);

        ivStart = (byte[]) (iv.clone());
        ivBlock = (byte[]) (ivStart.clone());
        currentByte = length;
    }

    /**
     * Gets a copy of the starting initialization vector. It will
     * return null if the initialization vector has not been set.
     *
     * @return a copy of the initialization vector for this cipher object.
     */
    public byte[] getInitializationVector() {
        return (ivStart == null) ? null : (byte[]) (ivStart.clone());
    }

    /**
     * Returns the size of the initialization vector expected by
     * <code>setInitializationVector</code>. For this class, that is
     * the block size of the underlying cipher.
     *
     * @return the required size of the argument to
     *         <code>setInitializationVector</code>.
     */
    public int getInitializationVectorLength() { return length; }
}
