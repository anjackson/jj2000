// $Id: Any_RSA_PKCS1Signature.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: Any_RSA_PKCS1Signature.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.9  2000/08/17 11:40:59  edwin
// java.* -> xjava.*
//
// Revision 1.8  2000/07/02 21:32:25  edwin
// Improving interoperability with other implementations that not
// always put a leading zero in a signature. Fix for bug #14
// Submitted-by: Eric Rescorla <ekr@rtfm.com>
//
// Revision 1.7  1999/07/12 20:34:21  edwin
// renaming java.security.interfaces.RSAPrivateKey and RSAPublicKey to CryptixRSAPrivateKey and CryptixRSAPublicKey. This is one more step to JDK1.2 compatibility.
//
// Revision 1.6  1998/03/20 06:13:28  hopwood
// + Committed changes below.
//
// Revision 1.5.1  1998/03/20  hopwood
// + 'Recipient' in exception detail messages changed to 'Signer'.
//
// Revision 1.5  1998/03/19 20:07:54  raif
// *** empty log message ***
//
// Revision 1.4.1  1998/03/20  raif
// + added documentation and test/assertion code for cases when the PKCS#1
//   frame [to be] built by the makePKCS1() method is larger than the public
//   key modulus --corrections prompted by tests/results reported by
//   Istofani Api Diany <diany94@PUSPA.CS.UI.AC.ID>
//
// Revision 1.4  1997/12/08 10:02:06  raif
// *** empty log message ***
//
// 1997.12.08 --RSN
// + modified the semantics of the makePKCS1() method by moving 2
//   bytes of the ASN.1 data into the subclasses.
// + documentation changes.
//
// Revision 1.3  1997/12/07 07:31:50  hopwood
// + I've forgotten what the change was - something minor.
//
// Revision 1.2  1997/11/23 03:09:18  hopwood
// + Mostly documentation changes.
//
// Revision 1.1.1  1997/11/23  hopwood
// + engineGetParameter now throws InvalidParameterException.
// + Merge with Ra&iuml;f's code:
//   - Added debugging display of actual and computed values if/when
//     engineVerify() fails. Fixed security bug in Ra&iuml;f's version: now it
//     only does this for debuglevel >= 7.
//   - Added timing computation. Will print if debuglevel >= 7.
//
// Revision 1.1  1997/11/23 02:24:59  hopwood
// + Changed the naming convention for signature classes.
//   "PKCS1" is used instead of "PEM", because PKCS#1 is a signature
//   (and encryption) formatting standard, whereas PEM is a complete secure
//   mail standard.
//
// Revision 1.3.1  1997/11/22  hopwood
// + Renamed this class to Any_RSA_PKCS1Signature from
//   RSA_PEMSignatureWithDigest.
//
// Revision 1.3  1997/11/20 19:46:57  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.2.1  1997/11/18  David Hopwood
// + Restored use of RSAFactors interface.
//
// Revision 1.2  1997/11/05 16:48:03  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.4  1997/09/27  David Hopwood
// + Misc. fixes.
//
// Revision 0.1.0.3  1997/08/23  David Hopwood
// + Moved the RSA implementation to a separate RSAAlgorithm class.
// + Use new debugging conventions.
//
// Revision 0.1.0.2  1997/08/18  David Hopwood
// + Changed to use the RSA interfaces from java.security.*, rather
//   than requiring a particular key class.
// + Handle the case where the factors p and q are not known, for
//   RSA encryption (signing).
// + Now u = q^-1 (mod p), not p^-1 (mod q). Apart from PGP, this is
//   more commonly used (e.g. see the P1363 draft).
//
// Revision 0.1.0.1  1997/07/30  David Hopwood
// + Renamed this class to RSA_PEMSignatureWithDigest
//   (from RSAWithMdSignature).
// + Changed to extend java.security.SignatureWithDigest.
// + No longer abstract.
//
// Revision 0.1.0.0  1997/07/24  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */
package cryptix.provider.rsa;

import cryptix.util.core.Debug;
import cryptix.CryptixException;
import cryptix.util.core.BI;
import cryptix.util.core.Hex;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import xjava.security.interfaces.CryptixRSAPublicKey;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.RSAFactors;

/**
 * An abstract class to digest a message and sign/verify the resulting
 * hash value, using any JCA MessageDigest algorithm with the RSA
 * digital signature scheme, and the formatting and padding conventions
 * defined by PKCS#1. These conventions are compatible with PEM (RFC-1423).
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 19.3 RSA,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996.
 *        <p>
 *   <li> PKCS#1 [need reference]
 *        <p>
 *   <li> RFC-1423 [need reference]
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Ra&iuml;f S. Naffah
 * @since   Cryptix 2.2.2
 */
public abstract class Any_RSA_PKCS1Signature
extends Signature
{
// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "Any_RSA_PKCS1Signature") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("Any_RSA_PKCS1Signature: " + s); }


// Constants and variables
//...........................................................................

    private static final int POSITIVE = 1;
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE =  BigInteger.valueOf(1L);

    private BigInteger n;
    private BigInteger exp; // e if state is ENCRYPT, d if DECRYPT.
    private BigInteger p;   // null if the factors of n are unknown.
    private BigInteger q;
    private BigInteger u;

    private MessageDigest md;


// Constructor
//...........................................................................

    /**
     * Constructor for an Any_RSA_PKCS1Signature.
     *
     * @param  mdAlgorithm  the standard JCA algorithm name of the message
     *                      digest to be used.
     */
    protected Any_RSA_PKCS1Signature (String mdAlgorithm) {
        super(mdAlgorithm + "/RSA/PKCS#1");
        try { md = MessageDigest.getInstance(mdAlgorithm); }
        catch (Exception e) {
            throw new CryptixException(getAlgorithm() +
                ": Unable to instantiate the " + mdAlgorithm +
                " MessageDigest\n" + e);
        }
    }


// Signature abstract methods implementation
//...........................................................................

    /**
     * <b>SPI:</b> Initializes this signature object for signing, using the
     * given private key.
     *
     * @param  key  the private key to be used to generate signatures.
     * @exception InvalidKeyException
     *      If the key class does not implement
     *      java.security.interfaces.RSAPrivateKey or
     *      If the size of the minimal PKCS#1 frame generated by the
     *      engineSign() method will be larger than the public key modulus.
     */
    protected void engineInitSign (PrivateKey key) throws InvalidKeyException {
        if (!(key instanceof CryptixRSAPrivateKey))
            throw new InvalidKeyException(getAlgorithm() +
                ": Not an RSA private key");

        CryptixRSAPrivateKey rsa = (CryptixRSAPrivateKey) key;
        n = rsa.getModulus();
        exp = rsa.getExponent();
        
        if (key instanceof RSAFactors) {
            RSAFactors factors = (RSAFactors) key;
            p = factors.getP();
            q = factors.getQ();
            u = factors.getInverseOfQModP();
        }
        md.reset();

        // result will have as many bytes as the public modulus n
        int mdl = md.digest().length;
        int length = (n.bitLength() + 7) / 8;
        int aidl = getAlgorithmEncoding().length;
        int padLen = length - 3 - aidl - mdl;
        if (padLen < 0) throw new
            InvalidKeyException("Signer's public key modulus too short.");
    }

    /**
     * <b>SPI:</b> Initializes this signature object for verification, using
     * the given public key.
     *
     * @param  key  the public key this signature is assumed to have
     *              been generated with.
     * @exception InvalidKeyException
     *      If the key class does not implement java.security.interfaces.RSAPrivateKey
     *      or
     *      If the size of the minimal PKCS#1 frame generated by the
     *      engineSign() method will be larger than the public key modulus.
     */
    protected void engineInitVerify (PublicKey key) throws InvalidKeyException {
        if (!(key instanceof CryptixRSAPublicKey))
            throw new InvalidKeyException(getAlgorithm() +
                ": Not an RSA public key");

        CryptixRSAPublicKey rsa = (CryptixRSAPublicKey) key;
        n = rsa.getModulus();
        exp = rsa.getExponent();

        // It is unusual for an RSAPublicKey class to implement RSAFactors,
        // because knowing p and q implies knowing the private key. Still,
        // occasionally it is useful to verify a signature that you created,
        // and this can be used to make that more efficient.
        if (key instanceof RSAFactors) {
            RSAFactors factors = (RSAFactors) key;
            p = factors.getP();
            q = factors.getQ();
            u = factors.getInverseOfQModP();
        }
        md.reset();

        // result will have as many bytes as the public modulus n
        int mdl = md.digest().length;
        int length = (n.bitLength() + 7) / 8;
        int aidl = getAlgorithmEncoding().length;
        int padLen = length - 3 - aidl - mdl;
        if (padLen < 0) throw new
            InvalidKeyException("Signer's public key modulus too short.");
    }

    /**
     * Updates the data to be signed or verified, using one byte.
     *
     * @param  b  the byte to use for the update process.
     * @exception SignatureException if the engine is not initialised properly.
     */ 
    protected void engineUpdate (byte b)
    throws SignatureException {
        if (state != VERIFY && state != SIGN)
            throw new SignatureException(getAlgorithm() + ": Not initialized");

        md.update(b);
    }

    /**
     * Updates the data to be signed or verified, using the specified
     * sub-array of bytes, starting at the specified offset.
     *
     * @param  in      the array of bytes.
     * @param  offset  the offset to start from in <i>in</i>.
     * @param  length  the number of bytes to use, starting at <i>offset</i>.
     * @exception SignatureException if the engine is not initialised properly.
     */          
    protected void engineUpdate (byte[] in, int offset, int length)
    throws SignatureException {
        if (state != VERIFY && state != SIGN)
            throw new SignatureException(getAlgorithm() + ": Not initialized");

        md.update(in, offset, length);
    }

    /**
     * Terminates the update process and returns the signature bytes of
     * all the data signed so far.
     * <p>
     * <b>NOTES:</b> Sun's documentation talks about the bytes returned
     * being X.509-encoded. For this RSA/PKCS#1 implementation, they
     * conform to PKCS#1 section 10. Practically, the return value will
     * be formed by concatenating a leading <i>NULL</i> byte, a block type
     * <i>BT</i>, a padding block <i>PS</i>, another <i>NULL</i>byte, and
     * finally a data block <i>D</i>;
     * ie:
     * <pre>
     *     return = 0x00 || BT || PS || 0x00 || D.
     * </pre>
     * For signing, <i>PKCS#1 block type 01</i> encryption-block formatting
     * scheme is employed. The block type <i>BT</i> is a single byte valued
     * 0x01 and the padding block <i>PS</i> is enough 0xFF bytes to make the
     * length of the complete RSA Multi Precision Integer equal to the length
     * of the public modulus. The data block <i>D</i> consists of the MIC --
     * Message Integrity Check, or message digest value-- and the MIC
     * algorithm ASN.1 encoded identifier. The formal syntax in ASN.1
     * notation is:
     * <pre>
     *   SEQUENCE {
     *     digestAlgorithm  AlgorithmIdentifier,
     *     digest           OCTET STRING
     *   }
     *
     *   AlgorithmIdentifier ::= SEQUENCE {
     *     algorithm        OBJECT IDENTIFIER,
     *     parameters       ANY DEFINED BY algorithm OPTIONAL
     *   }
     * </pre>
     *
     * @return  the signature bytes of the signing operation's result.
     * @exception  SignatureException
     *     if the engine is not initialised properly.
     */
    protected byte[] engineSign()
    throws SignatureException {
        if (state != SIGN) throw new SignatureException(
            getAlgorithm() + ": Not initialized for signing");

        long t = 0;
if (DEBUG && debuglevel >= 7) t = System.currentTimeMillis();

        BigInteger pkcs = makePKCS1();
        BigInteger result = RSAAlgorithm.rsa(pkcs, n, exp, p, q, u);

if (DEBUG && debuglevel >= 7) {
    t = System.currentTimeMillis() - t;
    debug(" ...engineSign() completed in "+t+" ms.");
}
        return result.toByteArray();
    }

    /**
     * Terminates the update process and verifies that the passed signature
     * equals that of a generated one based on the updated data so far.
     * <p>
     * <b>NOTES:</b> Sun's documentation talks about the bytes received
     * being X.509-encoded. For this RSA/PKCS#1 implementation, the bytes
     * received are assumed to conform to PKCS#1 section 10, or have
     * been generated by a previous invocation of the <code>engineSign</code>
     * method.
     *
     * @param  signature    the signature bytes to be verified.
     * @return true if the signature was verified successfully, false
     *                      otherwise.
     * @exception SignatureException if the engine is not initialised
     *                      properly, the received signature data is improperly
     *                      encoded or of the wrong type, etc.
     */
    protected boolean engineVerify(byte[] signature)
    throws SignatureException {
        if (state != VERIFY) throw new SignatureException(
            getAlgorithm() + ": Not initialized for verification");

        long t = 0;
if (DEBUG && debuglevel >= 7) t = System.currentTimeMillis();

        // if we generated it, it's a positive BI
        BigInteger M = new BigInteger(1,signature);
        BigInteger computed = RSAAlgorithm.rsa(M, n, exp, p, q, u);
        BigInteger actual = makePKCS1();
        boolean ok = computed.equals(actual);

if (DEBUG && debuglevel >= 7) {
    t = System.currentTimeMillis() - t;
    debug(" ...engineVerify() completed in "+t+" ms.");
    if (!ok) {
        debug("   Computed: " + Hex.dumpString(computed.toByteArray()));
        debug("     Actual: " + Hex.dumpString(actual.toByteArray()));
    }
}
        return ok;
    }

    protected void engineSetParameter(String param, Object value)
    throws InvalidParameterException {
        throw new InvalidParameterException(getAlgorithm() + ": " + param);
    }

    protected Object engineGetParameter(String param)
    throws InvalidParameterException {
        throw new InvalidParameterException(getAlgorithm() + ": " + param);
    }


// Own methods
//...........................................................................

    /**
     * Returns a byte array consisting of a padded message digest value,
     * previously computed. This packet will be RSA-encrypted with the
     * private key of this object to act as an authentication for whatever
     * was digested.
     * <p>
     * As described in the <i>engineSign()</i> method above, the return
     * array will consist of:
     * <pre>
     *    MSB                                                            LSB
     *    00  01   FF-1 ... FF-n  00   AID-1 ... AID-n   04 LL MD-1 ... MD-n
     *      | BT |----- PS -----|    |-- AlgorithmId --|------ digest ------|
     * </pre>
     * <p>
     * The <i>AID<i> bytes form the <i>AlgorithmIdentifier</i> token.
     * The OCTET STRING tag is <i>04</i> and <i>LL</i> is the length byte
     * (the number of bytes in the message digest proper, i.e. <i>n</i>).
     * <p>
     * Bytes <i>MD-1</i> to <i>MD-n</i> are the message digest value
     * of the material updated so far, thus completing the <i>digest</i>
     * token in the SEQUENCE described in <i>engineSign()</i> above.
     *
     * @return the result of the updating process framed in a PKCS#1
     *         type 01 block structure as a BigInteger.
     * @exception SignatureException If the length of the minimal PKCS#1 frame
     *      generated by this method will be longer than the public key modulus.
     */
    private BigInteger makePKCS1() throws SignatureException {
        byte[] theMD = md.digest();                           // stop hashing
        int mdl = theMD.length;

        // result has as many bytes as the public modulus
        int length = (n.bitLength() + 7) / 8;
        byte[] r = new byte[length];

        r[1] = 0x01;                          // PKCS#1 encryption block type

        byte[] aid = getAlgorithmEncoding();             // get the AID bytes
        int aidl = aid.length;

//        int padLen = length - 3 - aidl - mdl - 2;        // put padding bytes
        int padLen = length - 3 - aidl - mdl;            // put padding bytes
        if (padLen < 0)
            throw new SignatureException("Signer's public key modulus too short.");

        for (int i = 0; i < padLen;) r[2 + i++] = (byte) 0xFF;

        System.arraycopy(aid, 0, r, padLen + 3, aidl);   // copy the AID bytes
        // the next 2 bytes are now part of the subclass
//        r[length - mdl - 2] = 0x04;              // tag it as an OCTET STRING
//        r[length - mdl - 1] = (byte) mdl;           // and say how long it is
        System.arraycopy(theMD, 0, r, length - mdl, mdl); // now the md per se

if (DEBUG && debuglevel >= 4) debug("PKCS#1 frame = " + Hex.dumpString(r));

        return new BigInteger(r);        // always positive because r[0] is 0
    }

    /**
     * Returns the ASN.1 bytes of the <i>AlgorithmIdentifier</i> token described
     * in <code>engineSign()</code> method above.
     *
     * @return the <i>AlgorithmIdentifier</i> bytes.
     */
    protected abstract byte[] getAlgorithmEncoding();
}
