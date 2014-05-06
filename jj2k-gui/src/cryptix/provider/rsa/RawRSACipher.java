// $Id: RawRSACipher.java,v 1.1.1.1 2002/08/27 12:32:13 grosbois Exp $
//
// $Log: RawRSACipher.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:13  grosbois
// Add cryptix 3.2
//
// Revision 1.11  2000/08/18 02:32:23  edwin
// Oops, wrong check ;)
// (surprisingly enough the tests didn;t catch this)
//
// Revision 1.10  2000/08/17 11:41:00  edwin
// java.* -> xjava.*
//
// Revision 1.9  2000/08/16 17:51:43  edwin
// Throw a little more sensible exception when decryption fails and the decrypted
// data is one byte longer than it should.
//
// The code here is a little braindamaged, because it assumes that the first
// byte of the plaintext is always zero. If decryption fails due to a wrong key,
// this is often not the case, which results in a very weird exception.
//
// We still throw the original weird ArrayIndexOutOfBoundsException for
// compatibility, because that is what the old code throwed, however it does
// have a more descriptive message now.
//
// All of this will be documented in the to be written RSA faq, which details
// all peculiarities of the RSA code in cryptix3.
//
// Revision 1.8  1999/07/12 20:34:21  edwin
// renaming java.security.interfaces.RSAPrivateKey and RSAPublicKey to CryptixRSAPrivateKey and CryptixRSAPublicKey. This is one more step to JDK1.2 compatibility.
//
// Revision 1.7  1997/12/23 23:47:51  raif
// *** empty log message ***
//
// Revision 1.6.1  1997/12/24  raif
// + fixed the self-test. now runs OK.
// + rewrote the engineUpdate().
// + added enginePlaintextBlockSize(), engineCiphertextBlockSize()
//   and crippled engineBlockSize(). The rationale is that with
//   RSA, the encryption/decryption results usually do not match
//   their inputs in term of bit/byte-length.
//   This fixes the blockSize bug.
//
// Revision 1.6  1997/12/22 03:16:33  hopwood
// + RawRSACipher framing bug fixed.
// + BaseRSAKeyPairGenerator test #2 fixed.
//
// Revision 1.5  1997/11/23 03:09:18  hopwood
// + Mostly documentation changes.
//
// Revision 1.4  1997/11/20 19:46:57  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.1  1997/11/18  David Hopwood
// + Use BI.dumpString to print parameters in test method.
// + Fixed bug in test method (wrong value for e was being displayed).
//
// Revision 1.3  1997/11/05 16:48:03  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/04 19:33:31  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.1.1  1997/08/27  David Hopwood
// + Misc. fixes.
//
// Revision 0.1.1.0  1997/08/23  David Hopwood
// + Changed to use the RSA interfaces from java.security.*, rather
//   than requiring a particular key class.
// + Plaintext and ciphertext block sizes are now equal; we throw a
//   CryptixException if a BigInteger to be encrypted is out of range.
// + Use the RSA implementation from the RSAAlgorithm class.
// + Use new debugging conventions.
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
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Hex;
import cryptix.util.core.BI;

import java.io.PrintWriter;
import java.math.BigInteger;
import xjava.security.Cipher;
import xjava.security.AsymmetricCipher;
import java.security.Key;
import java.security.KeyException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import xjava.security.interfaces.RSAKey;
import xjava.security.interfaces.CryptixRSAPublicKey;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.RSAFactors;

import java.security.SecureRandom;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * The raw RSA encryption algorithm, without any block framing.
 * <p>
 * If the number of bits in the modulus is <i>bitlength</i>, the plaintext and
 * ciphertext block sizes will both be <code>(int) ((bitlength+7)/8)</code>.
 * When in ENCRYPT mode, if the value of any plaintext block, considered as a
 * BigInteger with Big-Endian byte order, is greater than or equal to the
 * modulus, a CryptixException will be thrown.
 * <p>
 * This class is designed to allow any input to the RSA encryption algorithm,
 * in order to facilitate implementation of standards which use a block framing
 * format not otherwise supported by Cryptix. Note that using raw RSA directly on
 * application data is potentially insecure; wherever possible a higher level
 * algorithm such as "RSA/PKCS#1" should be used.
 * <p>
 * (Note: RSA/PKCS#1 is not implemented in this version of Cryptix.)
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 19.3 RSA,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996.
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public class RawRSACipher
extends Cipher
implements AsymmetricCipher, Cloneable
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "RawRSACipher") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("RawRSACipher: " + s); }


// RSA constants and variables
//...........................................................................

    /** true if temporary arrays should be wiped immediately. */
    private static final boolean WIPE = true;

    private static final int POSITIVE = 1;
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE =  BigInteger.valueOf(1L);

    private BigInteger n;
    private BigInteger exp; // e if state is ENCRYPT, d if DECRYPT.
    private BigInteger p;   // null if the factors of n are unknown.
    private BigInteger q;
    private BigInteger u;

    private int blockSize; // defaults to 0
    private byte[] temp;


// Constructor
//............................................................................

    /**
     * Constructs a RawRSA cipher object, in the UNINITIALIZED state.
     * This calls the Cipher constructor with <i>implBuffering</i> false,
     * <i>implPadding</i> false and the provider set to "Cryptix".
     */
    public RawRSACipher() {
        super(false, false, "Cryptix");
    }


// JCE methods
//............................................................................

    /**
     * <b>SPI:</b> Initializes this cipher object for encryption, using the
     * given public key.
     *
     * @param  key  the public key to be used for encryption.
     * @exception InvalidKeyException if the key class does not implement
     *              java.security.interfaces.RSAPublicKey.
     */
    protected void engineInitEncrypt(Key key)
    throws InvalidKeyException {
        if (!(key instanceof CryptixRSAPublicKey))
            throw new InvalidKeyException(getAlgorithm() +
                ": Not an RSA public key");

        CryptixRSAPublicKey rsa = (CryptixRSAPublicKey) key;
        n = rsa.getModulus();
        exp = rsa.getExponent();

        if (key instanceof RSAFactors) {
            RSAFactors factors = (RSAFactors) key;
            p = factors.getP();
            q = factors.getQ();
            u = factors.getInverseOfQModP();
        }
        initInternal();
    }

    /**
     * <b>SPI:</b> Initializes this cipher object for decryption, using the
     * given private key.
     *
     * @param  key  the private key to be used for decryption.
     * @exception InvalidKeyException if the key class does not implement
     *              java.security.interfaces.RSAPrivateKey.
     */
    protected void engineInitDecrypt(Key key)
    throws InvalidKeyException {
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
        initInternal();
    }

    private void initInternal() {
//        blockSize = (n.bitLength()+7)/8;
        blockSize = BI.getMagnitude(n).length;
        temp = new byte[blockSize];
    }

    /**
     * <b>SPI</b>: Return the block size, in bytes. For RawRSA this is the number
     * of bytes needed to represent the modulus, <i>n</i>.
     *
     * @return the block size in bytes.
     * @exception CryptixException if the cipher object is uninitialized.
     */
//    protected int engineBlockSize() {
//        if (blockSize == 0) throw new CryptixException(getAlgorithm() +
//            ": Block size is not valid until key is set");
//        return blockSize;
//    }
    
    protected int enginePlaintextBlockSize () {
        if (blockSize == 0) throw new CryptixException(getAlgorithm() +
            ": Block size is not valid until key is set");
        return blockSize - 1;
    }
    
    protected int engineCiphertextBlockSize() {
        if (blockSize == 0) throw new CryptixException(getAlgorithm() +
            ": Block size is not valid until key is set");
        return blockSize;
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen,
                               byte[] out, int outOffset) {
/*
if (DEBUG && debuglevel >= 7) debug("in.length = " + in.length + ", out.length = " + out.length + ", blockSize = " + blockSize);

        for (int i = 0; i <= inLen-blockSize; i += blockSize) {
if (DEBUG && debuglevel >= 8) debug("i = " + i);
            System.arraycopy(in, inOffset+i, temp, 0, blockSize);
            BigInteger I = new BigInteger(POSITIVE, temp);

            if (I.compareTo(n) >= 0) throw new CryptixException(getAlgorithm() +
                ": Input block value is out of range (>= modulus)");
            BigInteger O = RSAAlgorithm.rsa(I, n, exp, p, q, u);

            byte[] buffer = O.toByteArray();
            int displacement = blockSize-buffer.length;

            if (displacement > 0) {
                ArrayUtil.clear(out, outOffset+i, displacement);
                System.arraycopy(buffer, 0, out, outOffset+i+displacement, buffer.length);
            } else {
                System.arraycopy(buffer, -displacement, out, outOffset+i, blockSize);
            }
            if (WIPE) ArrayUtil.clear(buffer);
        }
        if (WIPE) ArrayUtil.clear(temp);

        return blockSize;
*/
        if (inLen < 0) throw new IllegalArgumentException("inLen < 0");
        int inBlockSize = getState() == ENCRYPT ?
            enginePlaintextBlockSize() : engineCiphertextBlockSize();
        int outBlockSize = getState() == ENCRYPT ?
            engineCiphertextBlockSize() : enginePlaintextBlockSize();
        int blockCount = inLen / inBlockSize;

        for (int i = 0; i < blockCount; i++) {
            ArrayUtil.clear(temp);
            System.arraycopy(in, inOffset, temp, temp.length - inBlockSize, inBlockSize);
            BigInteger I = new BigInteger(POSITIVE, temp);

            if (I.compareTo(n) >= 0) throw new CryptixException(getAlgorithm() +
                ": Input block value is out of range (>= modulus)");
            BigInteger O = RSAAlgorithm.rsa(I, n, exp, p, q, u);

            byte[] buffer = BI.getMagnitude(O);

            if (buffer.length > outBlockSize) {
                throw new ArrayIndexOutOfBoundsException("Decryption failed, "+
                                                         "wrong key?");
            }
            ArrayUtil.clear(temp);
            System.arraycopy(buffer, 0, temp, outBlockSize - buffer.length, buffer.length);
            System.arraycopy(temp, 0, out, outOffset, outBlockSize);

            inOffset += inBlockSize;
            outOffset += outBlockSize;
        }
        return blockCount * outBlockSize;
    }


// Test methods
//...........................................................................
//
// Don't expand this code please without thinking about it,
// much better to write a separate program.
//

    /**
     * Entry point for very basic <code>self_test</code>.
     */
    public static final void main(String[] args) {
        try {
            self_test(new PrintWriter(System.out, true));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void self_test(PrintWriter out)
    throws Exception {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA", "Cryptix");
        SecureRandom random = new SecureRandom();

        long start = System.currentTimeMillis();
        keygen.initialize(1024, random);
        KeyPair keypair = keygen.generateKeyPair();
        long duration = System.currentTimeMillis() - start;

        out.println("Keygen: " + (float)duration/1000 + " seconds");

        RawRSACipher raw = new RawRSACipher();
        raw.test(out, keypair, random);
    }

    private void test(PrintWriter out, KeyPair keypair, SecureRandom random)
    throws KeyException {
        CryptixRSAPrivateKey privateKey = (CryptixRSAPrivateKey) (keypair.getPrivate());
        CryptixRSAPublicKey publicKey = (CryptixRSAPublicKey) (keypair.getPublic());

//        byte[] M = new byte[blockSize()];
//        random.nextBytes(M);

        long start = System.currentTimeMillis();
        initEncrypt(publicKey);
        BigInteger e = exp;
        
        byte[] M = new byte[getPlaintextBlockSize()];
        random.nextBytes(M);
        
        byte[] C = crypt(M);
        long midpoint = System.currentTimeMillis();
        initDecrypt(privateKey);
        byte[] Mdash = crypt(C);
        long end = System.currentTimeMillis();

        out.println("         n = " + BI.dumpString(n));
        out.println("         e = " + BI.dumpString(e));
        out.println("         d = " + BI.dumpString(exp));
        out.println("         p = " + BI.dumpString(p));
        out.println("         q = " + BI.dumpString(q));
        out.println("q^-1 mod p = " + BI.dumpString(u));
        out.println(" plaintext = " + Hex.toString(M) + "\n");
        out.println("ciphertext = " + Hex.toString(C) + "\n");

        if (!ArrayUtil.areEqual(M, Mdash)) {
            out.println("DECRYPTION FAILED!\n");
            out.println("  computed = " + Hex.toString(Mdash) + "\n");
        }

        out.println("Encrypt: " + ((float) (midpoint - start) / 1000) + " seconds");
        out.println("Decrypt: " + ((float) (end - midpoint) / 1000) + " seconds");
    }
}
