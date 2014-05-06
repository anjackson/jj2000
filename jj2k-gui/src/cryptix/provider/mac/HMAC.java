// $Id: HMAC.java,v 1.1.1.1 2002/08/27 12:32:11 grosbois Exp $
//
// $Log: HMAC.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.4  2000/08/17 11:40:56  edwin
// java.* -> xjava.*
//
// Revision 1.3  1998/01/28 05:58:10  hopwood
// + Documentation changes.
//
// Revision 1.2  1998/01/12 04:17:51  hopwood
// + HMAC rewrite.
//
// Revision 1.1.1.1.1  1998/01/12  hopwood
// + Rewritten as a MessageDigest.
//
// Revision 1.1.1.1  1998/01/04 03:27:05  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0  1998/01/03  hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.mac;

import cryptix.CryptixException;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import cryptix.util.core.ArrayUtil;

import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.Security;
import xjava.security.Parameterized;
import java.security.InvalidParameterException;

/**
 * A class to implement the HMAC message authentication code, as described in
 * RFC 2104.
 * <p>
 * The definition of HMAC-X for a given message digest algorithm X depends
 * on that algorithm's internal block size, which is passed into the constructor
 * when this class is used directly. Normally, this class will be obtained
 * indirectly via the JCA API, e.g. <code>MessageDigest.getInstance("HMAC-SHA-1")</code>
 * if SHA-1 is to be used as the digest algorithm.
 * <p>
 * The key can be set as a byte array using the "key" parameter. It is <em>not</em>
 * reset after the MAC has been returned (this enables successive MACs using the
 * same key to be calculated efficiently). For example:
 * <pre>
 *    import java.security.Parameterized;
 * <br>
 *    MessageDigest hmac = MessageDigest.getInstance("HMAC-SHA-1");
 *    byte[] key = ..., input1 = ..., input2 = ...;
 *    ((Parameterized) mac).setParameter("key", key);
 *    byte[] mac1 = hmac.digest(input1);
 *    byte[] mac2 = hmac.digest(input2);
 * </pre>
 * <p>
 * Parameters other than "key" are passed through to the MessageDigest object,
 * but this can only be done when the key is not set. An explicit call to
 * <code>reset()</code> will 'unset' the key.
 * <p>
 * This implementation does not support truncation of the output MAC. If
 * truncation is desired, the caller should use only the relevant initial bits
 * of the output array.
 * <p>
 * Note that although this class implements <samp>Cloneable</samp>, its
 * <code>clone()</code> method will throw a <samp>CloneNotSupportedException</samp>
 * if the underlying MessageDigest cannot be cloned. This should not be the
 * case for any of the Cryptix-supported digest algorithms.
 * <p>
 * HMAC is similar, but <em>not</em> identical to the MAC used by Secure Sockets
 * Layer version 3. The JCA standard algorithm name of the variant used in SSL v3
 * is "SSL3MAC-X", for a message digest name X.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> RFC 2104, <cite>HMAC: Keyed-Hashing for Message Authentication</cite>
 *   <li> RFC 2202, <cite>Test Cases for HMAC-MD5 and HMAC-SHA-1</cite>
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 3.0.3
 */
public class HMAC
extends MessageDigest
implements Parameterized, Cloneable
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel = DEBUG ? Debug.getLevel("HMAC") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("HMAC: " + s); }


// Constants and variables
//...........................................................................

    private MessageDigest md;
    // precomputation not implemented yet.
    // private MessageDigest precompInner;
    // private MessageDigest precompOuter;
    private int blockSize;
    private int length;
    private byte[] key; // defaults to null


// Constructors
//...........................................................................

    /**
     * Constructs an HMAC object for the given MD algorithm name and block
     * size.
     * <p>
     * Do not rely on being able to use this constructor to obtain a subclass
     * of MessageDigest; when Cryptix is changed to use the Java 1.2 provider
     * architecture, it will be a subclass of MessageDigestSpi instead.
     *
     * @param  mdAlgorithm  the standard JCA algorithm name of the message
     *                      digest to be used.
     * @param  mdBlockSize  the internal block size of the digest algorithm.
     */
    public HMAC(String mdAlgorithm, int mdBlockSize) {
        super("HMAC-" + mdAlgorithm);
        try { md = MessageDigest.getInstance(mdAlgorithm, "Cryptix"); }
        catch (Exception e) {
            try { md = MessageDigest.getInstance(mdAlgorithm); }
            catch (Exception e2) {
                throw new CryptixException(getAlgorithm() +
                ": Unable to instantiate the " + mdAlgorithm +
                " MessageDigest\n" + e2);
            }
        }

        blockSize = mdBlockSize;
        length = md.digest().length;
        // in Java 1.2, use "length = md.getDigestLength();"
        key = new byte[mdBlockSize];
    }

    /** This constructor is here to implement cloneability of this class. */
    private HMAC(HMAC mac) throws CloneNotSupportedException {
        super(mac.getAlgorithm());
        md = (MessageDigest) (mac.md.clone());
        blockSize = mac.blockSize;
        length = mac.length;
        byte[] _key = mac.key;
        key = (_key == null) ? null : (byte[]) (_key.clone());
    }


// Cloneable method implementation
//...........................................................................

    /**
     * Returns a copy of this HMAC object.
     *
     * @throws CloneNotSupportedException if the underlying MessageDigest is not
     *                                    cloneable.
     */
    public Object clone() throws CloneNotSupportedException {
        return new HMAC(this);
    }


// SPI methods
//...........................................................................

    /**
     * Resets this object disregarding any temporary data present at the
     * time of the invocation of this call.
     */
    protected void engineReset() {
        md.reset();
        key = null;
    }

    /** Continues an HMAC digest using the input byte. */
    protected void engineUpdate(byte input) {
        if (key == null)
            throw new IllegalStateException(getAlgorithm() + ": Key has not been set");

        md.update(input);
    }

    /**
     * Hashes a byte array from a given offset for a specified length.
     *
     * @param  input    byte array from which data is to be hashed.
     * @param  offset   start index of bytes to hash in input.
     * @param  len      number of bytes to hash.
     */
    protected void engineUpdate(byte[] input, int offset, int len) {
        if (key == null)
            throw new IllegalStateException(getAlgorithm() + ": Key has not been set");

        md.update(input, offset, len);
    }

    /**
     * Calculates the final MAC.
     * <p>
     * RFC 2104 describes HMAC as follows:
     * <p>
     * The definition of HMAC requires a cryptographic hash function, which
     * we denote by H, and a secret key K. We assume H to be a cryptographic
     * hash function where data is hashed by iterating a basic compression
     * function on blocks of data.   We denote by B the byte-length of such
     * blocks (B=64 for all the above mentioned examples of hash functions),
     * and by L the byte-length of hash outputs (L=16 for MD5, L=20 for
     * SHA-1).  The authentication key K can be of any length up to B, the
     * block length of the hash function.  Applications that use keys longer
     * than B bytes will first hash the key using H and then use the
     * resultant L byte string as the actual key to HMAC. In any case the
     * minimal recommended length for K is L bytes (as the hash output
     * length). See section 3 for more information on keys.
     * <p>
     * We define two fixed and different strings <i>ipad</i> and <i>opad</i>
     * as follows (the 'i' and 'o' are mnemonics for inner and outer):
     * <blockquote>
     *    <i>ipad</i> = the byte 0x36 repeated B times <br>
     *    <i>opad</i> = the byte 0x5C repeated B times.
     * </blockquote>
     * <p>
     * To compute HMAC over the data 'text' we perform
     * <blockquote>
     *    H(K XOR <i>opad</i>, H(K XOR <i>ipad</i>, text))
     * </blockquote>
     * <p>
     * Namely,
     * <ol>
     *   <li> append zeros to the end of K to create a B byte string
     *        (e.g., if K is of length 20 bytes and B=64, then K will be
     *        appended with 44 zero bytes 0x00)
     *   <li> XOR (bitwise exclusive-OR) the B byte string computed in step
     *        (1) with <i>ipad</i>
     *   <li> append the stream of data 'text' to the B byte string resulting
     *        from step (2)
     *   <li> apply H to the stream generated in step (3)
     *   <li> XOR (bitwise exclusive-OR) the B byte string computed in
     *        step (1) with <i>opad</i>
     *   <li> append the H result from step (4) to the B byte string
     *        resulting from step (5)
     *   <li> apply H to the stream generated in step (6) and output
     *        the result
     * </ol>
     */
    protected byte[] engineDigest() {
if (DEBUG && debuglevel >= 7) debug("engineDigest()");

        if (key == null)
            throw new IllegalStateException(getAlgorithm() + ": Key has not been set");

        byte[] innerhash = md.digest();

        byte[] xorblock = new byte[blockSize];
        for (int i = 0; i < key.length; i++)
            xorblock[i] = (byte) (0x5C ^ key[i]);

        for (int i = key.length; i < blockSize; i++)
            xorblock[i] = (byte) 0x5C;

        md.update(xorblock);
        byte[] result = md.digest(innerhash);

if (DEBUG && debuglevel >= 7) debug("... = <" + Hex.toString(result) + ">");
        return result;
    }

    /** <b>SPI</b>: Returns the digest length in bytes. */
    protected int engineGetDigestLength() { return length; }

    public void setParameter(String param, Object value)
    throws InvalidParameterException {
        engineSetParameter(param, value);
    }

    public Object getParameter(String param)
    throws InvalidParameterException {
        return engineGetParameter(param);
    }

    protected void engineSetParameter(String param, Object value)
    throws InvalidParameterException {
        try {
            if (param.equalsIgnoreCase("key")) {
                setKey((byte[]) value);
                return;
            }

            // don't allow setting hash parameters after the key has been initialised
            // (allowing this would inhibit planned optimisations).
            if (key != null)
                throw new InvalidParameterException(getAlgorithm() +
                    ": Can't set parameter after key has been initialised");

            ((Parameterized) md).setParameter(param, value);
        } catch (Exception e) { throw new InvalidParameterException(e.getMessage()); }
    }

    protected Object engineGetParameter(String param)
    throws InvalidParameterException {
        try {
            return ((Parameterized) md).getParameter(param);
        } catch (Exception e) { throw new InvalidParameterException(e.getMessage()); }
    }


// Own methods
//...........................................................................

    /**
     * Sets the key value. If the MessageDigest is cloneable, it is possible to
     * do precomputations for the inner and outer digests (but this is not
     * implemented yet).
     *
     * @param  userkey  the bytes of the key.
     */
    private void setKey(byte[] userkey) {
if (DEBUG && debuglevel >= 7) debug("setKey(<" + Hex.toString(userkey) + ">)");

        if (key != null)
            md.reset(); // junk incomplete input

        if (userkey.length > blockSize)
            key = md.digest(userkey);
        else
            key = (byte[]) (userkey.clone());

        byte[] xorblock = new byte[blockSize];
        for (int i = 0; i < key.length; i++)
            xorblock[i] = (byte) (0x36 ^ key[i]);

        for (int i = key.length; i < blockSize; i++)
            xorblock[i] = (byte) 0x36;

        md.update(xorblock);
    }
}
