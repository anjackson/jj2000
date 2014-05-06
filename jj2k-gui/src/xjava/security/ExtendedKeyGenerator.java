// This file is currently unlocked (change this line if you lock the file)
//
// $Log: ExtendedKeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.0  1997/08/13  David Hopwood
// + Original version, based on the API of the Cryptix class
//   cryptix.provider.key.RawKeyGenerator.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;


import java.security.InvalidKeyException;
import java.security.SecureRandom;


/**
 * The KeyGenerator class in JavaSoft's original version of JCE
 * does not provide these essential features:
 * <ul>
 *   <li> ability to set key lengths, for algorithms that support
 *        more than one length.
 *   <li> ability to query which key lengths are supported.
 *   <li> creation of a key from an encoded byte array.
 *   <li> checking for weak keys.
 * </ul>
 * <p>
 * This interface provides a standard API for KeyGenerators that
 * implement the above features.
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
 */
public interface ExtendedKeyGenerator
{

// KeyGenerator methods
//............................................................................

    /**
     * Initializes the key generator.
     *
     * @param random    the source of randomness for this generator.
     */
    void initialize(SecureRandom random);

    /**
     * Generates a key. This method generates a new random key every
     * time it is called.
     * <p>
     * If a source of random bytes has not been set using one of the
     * <code>initialize</code> methods, <code>new SecureRandom()</code>
     * will be used.
     *
     * @return the new key.
     */
    SecretKey generateKey();


// Additional methods
//............................................................................

    /**
     * Initializes the key generator, and sets a specific key length
     * for use with algorithms that allow variable-length keys.
     * <p>
     * The <i>length</i> parameter only affects randomly generated
     * keys (i.e. the <code>generateKey()</code> method without
     * parameters).
     *
     * @param random    the source of randomness for this generator.
     * @param length    the desired key length in bytes.
     * @exception IllegalArgumentException if length is not valid for
     *          this algorithm.
     */
    void initialize(SecureRandom random, int length);

    /**
     * Generates a key from an encoded byte array. The format of the
     * secret key is "RAW". The contents of <i>data</i> will not be
     * modified.
     * <p>
     * The encoded key bytes may differ from <i>data</i> in order to
     * make sure that they represent a valid key. For example, if keys
     * for this algorithm conventionally include parity bits, those
     * bits will be set correctly. For most algorithms, <i>data</i> is
     * used unchanged.
     *
     * @param  data     user supplied raw-encoded data from which a secret
     *                  key will be generated.
     * @return the new key.
     * @exception NullPointerException if data == null
     * @exception WeakKeyException if <i>isWeakAllowed()</i> is false, and
     *                  <i>data</i> represents a weak key for this algorithm.
     * @exception InvalidKeyException if the length of <i>data</i> is not
     *                  valid for this algorithm.
     */
    SecretKey generateKey (byte[] data)
    throws WeakKeyException, InvalidKeyException;

    /**
     * Returns true if this object is allowed to generate weak and
     * semi-weak keys; false otherwise.
     */
    boolean isWeakAllowed();

    /**
     * Sets whether this object is allowed to generate weak and
     * semi-weak keys.
     *
     * @param allowWeak true if weak/semi-weak keys are allowed.
     */
    void setWeakAllowed(boolean allowWeak);

    /**
     * Returns the minimum key length for this algorithm.
     */
    int getMinimumKeyLength();

    /**
     * Returns the key length that will be used by
     * <code>generateKey()</code> to create new random keys. This is
     * either the default key length determined by the KeyGenerator
     * for this algorithm, or the length set using
     * <code>initialize(SecureRandom random, int length)</code>.
     */
    int getDefaultKeyLength();

    /**
     * Returns the maximum useful key length for this algorithm.
     */
    int getMaximumKeyLength();

    /**
     * Returns true iff <i>length</i> is a valid key length (in bytes)
     * for this algorithm.
     */
    boolean isValidKeyLength(int length);
}
