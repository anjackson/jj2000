// $Id: Any_ElGamal_PKCS1Signature.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: Any_ElGamal_PKCS1Signature.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.4  2000/08/17 11:40:54  edwin
// java.* -> xjava.*
//
// Revision 1.3  1997/12/14 17:37:58  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/12/10  hopwood
// + initInternal was checking p before it was set.
// + Cosmetics.
//
// Revision 1.2  1997/12/10 01:17:46  raif
// + documentation.
// + use cryptix.util.core.BI to process BigIntegers while
//   signing and verifying signatures. Needed to eliminate size
//   uncertainties when converting BigInteger to/from byte arrays.
// + added tests for null objects in initInternal() and engineInitSign()
//   methods.
// + changed the AlgId semantics to align with RSA implementations.
//
// Revision 1.1  1997/12/07 06:37:27  hopwood
// + Major overhaul of ElGamal to match RSA.
//
// Revision 1.1.1.1.1  1997/11/22  hopwood
// + Renamed this class to Any_RSA_PKCS1Signature from
//   RawElGamalSignature.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.elgamal;

import cryptix.CryptixException;
import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Random;
import java.security.Signature;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import xjava.security.interfaces.ElGamalPublicKey;
import xjava.security.interfaces.ElGamalPrivateKey;

/**
 * An abstract class to digest a message and sign/verify the resulting
 * hash value, using any JCA MessageDigest algorithm with the ElGamal
 * digital signature scheme, and formatting and padding conventions
 * based on PKCS#1.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 19.6 ElGamal,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996.
 *        <p>
 *   <li> PKCS#1
 *        An RSA Laboratories Technical Note<br>
 *        Version 1.5<br>
 *        Revised November 1, 1993<br>
 *        An "RSA Data Security, Inc. Public-Key Cryptography Standard (PKCS)"
 * </ol>
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  Cryptix 2.2.2
 *
 * @see java.security.interfaces.ElGamalKey
 * @see ElGamalCipher
 * @see java.security.Signature
 */
public abstract class Any_ElGamal_PKCS1Signature
extends Signature
{

// Debugging methods and vars.
//............................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("ElGamal", "Any_ElGamal_PKCS1Signature") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("Any_ElGamal_PKCS1Signature: " + s); }


// Constants and variables
//............................................................................

    /**
     * The minimum length of <i>p</i> in bits.
     */
    private static final int MIN_BITLENGTH = 256;

    private static final int POSITIVE = 1;
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private static final BigInteger ONE = BigInteger.valueOf(1);

    private BigInteger p;
    private BigInteger g;
    private BigInteger x; // null if the state is ENCRYPT
    private BigInteger y;

    private int primeLen;
    private Random rng;

    private MessageDigest md;


// Constructor
//............................................................................

    /**
     * Constructor for an Any_ElGamal_PKCS1Signature.
     *
     * @param  mdAlgorithm  the standard JCA algorithm name of the message
     *                      digest to be used.
     */
    protected Any_ElGamal_PKCS1Signature (String mdAlgorithm) {
        super(mdAlgorithm + "/ElGamal/PKCS#1");
        try { md = MessageDigest.getInstance(mdAlgorithm); }
        catch (Exception e) {
            throw new CryptixException(getAlgorithm() +
                ": Unable to instantiate the " + mdAlgorithm +
                " MessageDigest\n" + e);
        }
    }


// Signature abstract methods implementation
//............................................................................

    /**
     * <b>SPI</b>: Initializes the cipher for signing, using the
     * given private key. The key object must implement
     * java.security.interfaces.ElGamalPrivateKey.
     * <p>
     * The input to this algorithm will be padded on the left with random
     * bits, up to the size of a block, before signing.
     *
     * @param key   the private key
     * @exception InvalidKeyException if !(key instanceof
     *          java.security.interfaces.ElGamalPrivateKey)
     */
    protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
        if (!(key instanceof ElGamalPrivateKey))
            throw new InvalidKeyException("ElGamal: signing key does not implement java.security.interfaces.ElGamalPrivateKey");

        ElGamalPrivateKey privateKey = (ElGamalPrivateKey) key;
        BigInteger newX = privateKey.getX();
        if (newX == null) throw new InvalidKeyException("ElGamal: getX() == null");

        initInternal(privateKey.getP(), privateKey.getG(),
                     newX, privateKey.getY());
        if (rng == null)
            rng = new SecureRandom();
    }

    /**
     * <b>SPI</b>: Initializes the cipher for verification, using the
     * given public key. The key object must implement
     * java.security.interfaces.ElGamalPublicKey.
     *
     * @param key   the public key
     * @exception InvalidKeyException if !(key instanceof
     *          java.security.interfaces.ElGamalPublicKey)
     */
    protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
        if (!(key instanceof ElGamalPublicKey))
            throw new InvalidKeyException("ElGamal: verification key does not implement java.security.interfaces.ElGamalPublicKey");

        ElGamalPublicKey publicKey = (ElGamalPublicKey) key;
        initInternal(publicKey.getP(), publicKey.getG(),
                     null, publicKey.getY());
        if (rng == null)
            rng = new SecureRandom();
    }

    private void initInternal(BigInteger newP, BigInteger newG,
                              BigInteger newX, BigInteger newY)
            throws InvalidKeyException{
        if (newP == null) throw new InvalidKeyException("ElGamal: getP() == null");
        if (newG == null) throw new InvalidKeyException("ElGamal: getG() == null");
        if (newY == null) throw new InvalidKeyException("ElGamal: getY() == null");
        if (newP.bitLength() < MIN_BITLENGTH)
            throw new InvalidKeyException("ElGamal: getP().bitLength() < " + MIN_BITLENGTH);

        if (newP.compareTo(ONE) <= 0)
            throw new InvalidKeyException("ElGamal: getP() < 2");

        p = newP;
        g = newG;
        x = newX;
        y = newY;

        primeLen = (p.bitLength() + 7) / 8;
        md.reset();
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
     * being X.509-encoded. For this ElGamal/PKCS#1 implementation, they
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
     * length of the complete Multi Precision Integer equal to the length
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
     * @return the bytes of the signing operation's result.
     * @exception SignatureException if the engine is not initialised properly.
     */
    protected byte[] engineSign()
    throws SignatureException {
        if (state != SIGN) throw new SignatureException(
            getAlgorithm() + ": Not initialized for signing");

        BigInteger pkcs = makePKCS1();

        // sign the padded message.
        BigInteger[] ab = new BigInteger[2];
        ElGamalAlgorithm.sign(pkcs, ab, p, g, x, rng);

/*
        // DER-encode the result.
        ASN1Value sig = new ASN1Sequence(new ASN1Value[] {
                            new ASN1Integer(ab[0]),
                            new ASN1Integer(ab[1]),
                        });
        return sig.derEncode();
*/
//        byte[] aBytes = ab[0].toByteArray();
//        byte[] bBytes = ab[1].toByteArray();
//        byte[] signature = new byte[primeLen*2];
//        System.arraycopy(aBytes, 0, signature, primeLen-aBytes.length, aBytes.length);
//        System.arraycopy(bBytes, 0, signature, signature.length-bBytes.length, bBytes.length);
//        return signature;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BI.toStream(ab[0], baos);
            BI.toStream(ab[1], baos);
        } catch (IOException e) {
            throw new SignatureException("BI.toStream() failed");
        }
        return baos.toByteArray();
    }

    /**
     * Terminates the update process and verifies that the passed signature
     * equals that of a generated one based on the updated data so far.
     * <p>
     * <b>NOTES:</b> Sun's documentation talks about the bytes received
     * being X.509-encoded. For this ElGamal/PKCS#1 implementation, the bytes
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

        BigInteger pkcs = makePKCS1();

/*
        // BER-decode the signature.
        ASN1Value value = null;
        try {
            value = new BERDecoder().decode(signature);
            ASN1Sequence seq = (ASN1Sequence) value;
            BigInteger a = ((ASN1Integer) (seq.elementAt(0))).BigIntegerValue();
            BigInteger b = ((ASN1Integer) (seq.elementAt(1))).BigIntegerValue();

            return ElGamalAlgorithm.verify(pkcs, a, b, p, g, y);
        } catch (ASN1FormatException e) {
            throw new InvalidKeyException("ElGamal: ASN.1 signature could not be decoded.");
        } catch (ClassCastException e) {
            throw new InvalidKeyException("ElGamal: ASN.1 signature has unexpected format.");
        }
*/
//        if (signature.length != primeLen*2)
//            return false;

//        byte[] temp = new byte[primeLen];
//        System.arraycopy(signature, 0, temp, 0, primeLen);
//        BigInteger a = new BigInteger(POSITIVE, temp);
//        System.arraycopy(signature, 0, temp, 0, primeLen);
//        BigInteger b = new BigInteger(POSITIVE, temp);
        ByteArrayInputStream bais = new ByteArrayInputStream(signature);
        BigInteger a, b;
        try {
            a = BI.fromStream(bais);
            b = BI.fromStream(bais);
        } catch (IOException e) {
            throw new SignatureException("BI.fromStream() failed");
        }
        return ElGamalAlgorithm.verify(pkcs, a, b, p, g, y);
    }

    /**
     * <b>SPI</b>: Sets an algorithm-specific parameter.
     * <p>
     * ElGamal has one algorithm-specific parameter called "random", of type
     * java.util.Random. It specifies the source of random bits used for
     * generating the <i>k</i> values needed for signing. If this parameter
     * is not set when <code>initSign</code> is called, the result of
     * <code>new SecureRandom()</code> will be used.
     * <p>
     * You can set the "random" parameter using the following code:
     * <pre>
     *   try {
     *       elgamal.setParameter("random", random_number_generator);
     *   } catch (InvalidParameterException e) { ... }
     * </pre>
     * <p>
     * This is not useful if the Signature object will only be used for
     * verification.
     *
     * @param param the string identifier of the parameter.
     * @param value the parameter value.
     * @exception InvalidParameterException if !(param.equals("random") &&
     *          value instanceof java.util.Random)
     */
    protected void engineSetParameter(String param, Object value) {
        if (param.equals("random")) {
            if (!(value instanceof Random)) throw new InvalidParameterException(
                "value must be an instance of java.util.Random");

            rng = (Random) value;
            return;
        }
        throw new InvalidParameterException(param);
    }

    /**
     * <b>SPI</b>: Returns an algorithm-specific parameter.
     * <p>
     * ElGamal has one algorithm-specific parameter called "random", as described
     * <a href="#engineSetParameter">above</a>. It is guaranteed to be a subclass
     * of java.util.Random. Calling this method with a <i>param</i> string
     * other than "random" will return null.
     *
     * @param param the string name of the parameter.
     * @return the object that represents the parameter value, or null if there
     *          is none.
     */
    protected Object engineGetParameter(String param) {
        if (param.equals("random")) return rng;
        return null;
    }


// Own methods
//............................................................................

    /**
     * Returns a byte array consisting of a padded message digest value,
     * previously computed. This packet will be ElGamal-signed with the
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
     */
    private BigInteger makePKCS1() {
        byte[] theMD = md.digest();                            // stop hashing
        int mdl = theMD.length;

        // result has as many bytes as the prime
        byte[] r = new byte[primeLen];

        r[1] = 0x01;                           // PKCS#1 encryption block type

        byte[] aid = getAlgorithmEncoding();              // get the AID bytes
        int aidl = aid.length;

//        int padLen = primeLen - 3 - aidl - mdl - 2;       // put padding bytes
        int padLen = primeLen - 3 - aidl - mdl;       // put padding bytes
        for (int i = 0; i < padLen; i++) r[2 + i] = (byte)0xFF;

        System.arraycopy(aid, 0, r, padLen + 3, aidl);   // copy the AID bytes
//        r[primeLen - mdl - 2] = 0x04;             // tag it as an OCTET STRING
//        r[primeLen - mdl - 1] = (byte) mdl;          // and say how long it is
        System.arraycopy(theMD, 0, r, primeLen - mdl, mdl);      // now the md

if (DEBUG && debuglevel >= 4) debug("PKCS#1 frame = " + Hex.dumpString(r));

        return new BigInteger(r);         // always positive because r[0] is 0
    }

    /**
     * Returns the ASN.1 bytes of the <i>AlgorithmIdentifier</i> token described
     * in <code>engineSign()</code> method above.
     *
     * @return the <i>AlgorithmIdentifier</i> bytes.
     */
    protected abstract byte[] getAlgorithmEncoding();
}
