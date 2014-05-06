// $Id: Mode.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: Mode.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.3  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.2  1997/11/29 04:45:13  hopwood
// + Committed changes below.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.4  1997/10/26  David Hopwood
// + Added getInstance methods.
//
// Revision 0.1.0.3  1997/08/08  David Hopwood
// + Moved most of the implementation methods/variables introduced
//   in 1.0.1 to cryptix.provider.mode.FeedbackMode (i.e. they are no
//   longer part of IJCE).
// + This class no longer extends FeedbackCipher.
// + Changed the default implementation of engineSet/GetParameter to
//   forward to the cipher object.
//
// Revision 0.1.0.2  1997/08/02  David Hopwood
// + Fixed documentation to take into account the changes in 1.0.1.
//
// Revision 0.1.0.1  1997/07/13  Raif Naffah
// + Changes to allow leaner implementations of modes.
//
// Revision 0.1.0.0  1997/07/??  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;


/**
 * This class is used to provide the functionality of an encryption
 * mode, such as CBC, CFB, or OFB.
 * <p>
 * Modes are implemented as Ciphers with an additional engine method,
 * <code>engineSetCipher</code>, that is called once to set the
 * underlying cipher algorithm. The Mode class stores this cipher
 * in the protected field <code>cipher</code>. Subclasses are expected
 * to use this to implement their own
 * <code>engineInitEncrypt</code>, <code>engineInitDecrypt</code> and
 * <code>engineUpdate</code> methods. 
 * <p>
 * <strong><a href="../guide/ijce/JCEDifferences.html">This class
 * is not supported in JavaSoft's version of JCE.</a></strong>
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   IJCE 1.0.1
 *
 * @see java.security.Cipher
 */
public abstract class Mode
extends Cipher
{

// Variables
//.....................................................................

    /**
     * A reference to the cipher currently operating in this mode.
     */
    protected Cipher cipher;


// Constructor
//.....................................................................

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
    protected Mode(boolean implBuffering, boolean implPadding, String provider) {
        super(implBuffering, implPadding, provider);
    }


// JCE methods
//.....................................................................

    /**
     * Generates a Mode object that implements the algorithm
     * requested, as available in the environment.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#Mode">
     * <cite>International JCE Standard Algorithm Names</a> for a list
     * of Mode algorithm names.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @return the new Mode object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available in the environment.
     */
    // Note: this should really return Mode, but javac complains that
    // the return type is different from Cipher.getInstance(String).
    // (I think this is a bug in javac, or a specification bug - this
    // method does not _override_ Cipher.getInstance(String), because
    // it is static.)
    //
    public static Cipher getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        return (Cipher) (IJCE.getImplementation(algorithm, "Mode"));
    }

    /**
     * Generates a Mode object implementing the specified
     * algorithm, as supplied from the specified provider, if such an
     * algorithm is available from the provider.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#Mode">
     * <cite>International JCE Standard Algorithm Names</a> for a list
     * of Mode algorithm names.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @param  provider     the string name of the provider.
     * @return the new KeyGenerator object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available from the provider.
     * @exception NoSuchProviderException if the provider is not
     *                      available in the environment.
     */
    // Note: this should really return Mode (see previous method).
    //
    public static Cipher getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {
        return (Cipher) (IJCE.getImplementation(algorithm, provider, "Mode"));
    }

    /**
     * Gets the standard names of all Modes implemented by a
     * provider.
     */
    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "Mode");
    }

    /**
     * Gets the standard names of all Modes implemented by any
     * installed provider. Algorithm names are not duplicated if
     * they are supported by more than one provider.
     * The built-in mode "ECB" is included.
     */
    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("Mode");
    }

    public String toString() {
        return "Mode [" + getProvider() + " " +
            getAlgorithm() + "/" + getMode() + "/" + getPadding() + "]";
    }


// SPI methods
//.....................................................................

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
        if (cipher == null) throw new NullPointerException("cipher == null");
        this.cipher = cipher;
    }

    /**
     * <b>SPI</b>: Sets the specified algorithm parameter to the specified
     * value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. The mode
     * implementation should first check whether it recognizes the
     * parameter name, and if not, call
     * <code>super.engineSetParameter(param, value)</code>.
     * <p>
     * A parameter may be any settable parameter for the algorithm, such
     * as block size, a source of random bits for IV generation (if
     * appropriate), or an indication of whether or not to perform a
     * specific but optional computation. A uniform algorithm-specific
     * naming scheme for each parameter is desirable but left unspecified
     * at this time.
     * <p>
     * The default implementation forwards the call to the underlying
     * cipher.
     *
     * @param  param    the string name of the parameter. 
     * @param  value    the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this cipher implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set (for example because the cipher is in the
     *                  wrong state).
     * @exception InvalidParameterTypeException if value is the wrong type
     *                  for this parameter.
     */
    protected void engineSetParameter(String param, Object value)
    throws NoSuchParameterException, InvalidParameterException,
           InvalidParameterTypeException {
        cipher.setParameter(param, value);
    }

    /**
     * <b>SPI</b>: Gets the value of the specified algorithm parameter.
     * <p>
     * This method supplies a general-purpose mechanism through which
     * it is possible to get the various parameters of this object. The
     * mode implementation should first check whether it recognizes the
     * parameter name, and if not, return
     * <code>super.engineGetParameter(param)</code>.
     * <p>
     * A parameter may be any settable parameter for the algorithm, such
     * as block size, a source of random bits for IV generation (if
     * appropriate), or an indication of whether or not to perform a
     * specific but optional computation. A uniform algorithm-specific
     * naming scheme for each parameter is desirable but left unspecified
     * at this time.
     * <p>
     * The default implementation forwards the call to the underlying
     * cipher.
     *
     * @param  param    the string name of the parameter. 
     * @return the object that represents the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this cipher implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    protected Object engineGetParameter(String param)
    throws NoSuchParameterException, InvalidParameterException {
        return cipher.getParameter(param);
    }
}
