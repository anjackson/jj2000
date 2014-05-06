// $Id: RawElGamalCipher.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: RawElGamalCipher.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.3  2000/08/17 11:40:54  edwin
// java.* -> xjava.*
//
// Revision 1.2  1997/12/07 06:37:26  hopwood
// + Major overhaul of ElGamal to match RSA.
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
import cryptix.util.core.ArrayUtil;

import java.math.BigInteger;
import java.util.Random;
import java.security.SecureRandom;
import xjava.security.Cipher;
import xjava.security.AsymmetricCipher;
import java.security.Key;
import java.security.KeyException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import xjava.security.IllegalBlockSizeException;
import xjava.security.interfaces.ElGamalPublicKey;
import xjava.security.interfaces.ElGamalPrivateKey;

// needed only for test code
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * The raw ElGamal encryption algorithm.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> Bruce Schneier,
 *        "Section 19.6 ElGamal,"
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
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public class RawElGamalCipher
extends Cipher
implements AsymmetricCipher, Cloneable {

    private static final int POSITIVE = 1;
    private static final BigInteger ONE = BigInteger.valueOf(1);

    private BigInteger p;
    private BigInteger p_minus_1;
    private BigInteger g;
    private BigInteger x; // null if the state is ENCRYPT
    private BigInteger y;

    private int primeLen;
    private Random rng;

    /**
     * Constructor for a RawElGamalCipher.
     */
    public RawElGamalCipher() {
        super(false, true, "Cryptix");
    }

    /**
     * <b>SPI</b>: Initializes the cipher for encryption, using the
     * given public key. The key object must implement
     * java.security.interfaces.ElGamalPublicKey.
     *
     * @param key   the public key
     * @exception InvalidKeyException if !(key instanceof
     *          java.security.interfaces.ElGamalPublicKey), or the
     *          key is otherwise invalid
     */
    protected void engineInitEncrypt(Key key) throws KeyException {
        if (!(key instanceof ElGamalPublicKey))
            throw new InvalidKeyException("ElGamal: encryption key does not " +
                "implement java.security.interfaces.ElGamalPublicKey");

        ElGamalPublicKey elgamalKey = (ElGamalPublicKey) key;
        initInternal(elgamalKey.getP(), elgamalKey.getG(),
                     null, elgamalKey.getY());
        if (rng == null)
            rng = new SecureRandom();
    }

    /**
     * <b>SPI</b>: Initializes the cipher for decryption, using the
     * given private key. The key object must implement
     * java.security.interfaces.ElGamalPrivateKey.
     *
     * @param key   the private key
     * @exception InvalidKeyException if !(key instanceof
     *          java.security.interfaces.ElGamalPrivateKey), or the
     *          key is otherwise invalid
     */
    protected void engineInitDecrypt(Key key) throws KeyException {
        if (!(key instanceof ElGamalPrivateKey))
            throw new InvalidKeyException("ElGamal: decryption key does not " +
                "implement java.security.interfaces.ElGamalPrivateKey");

        ElGamalPrivateKey elgamalKey = (ElGamalPrivateKey) key;
        BigInteger newX = elgamalKey.getX();
        if (newX == null) throw new InvalidKeyException("ElGamal: getX() == null");

        initInternal(elgamalKey.getP(), elgamalKey.getG(),
                     newX, elgamalKey.getY());
    }

    private void initInternal(BigInteger newP, BigInteger newG,
                              BigInteger newX, BigInteger newY)
            throws InvalidKeyException {
        if (newP == null) throw new InvalidKeyException("ElGamal: getP() == null");
        if (newG == null) throw new InvalidKeyException("ElGamal: getG() == null");
        if (newY == null) throw new InvalidKeyException("ElGamal: getY() == null");

        p = newP;
        g = newG;
        x = newX;
        y = newY;

        primeLen = (p.bitLength() - 1) / 8;
    }

    /**
     * <b>SPI</b>: Return the plaintext block size, in bytes. For ElGamal this
     * is the number of bytes needed for a bit string one bit shorter than the
     * prime, <i>p</i>.
     * <p>
     * If the key has not been set, this method throws CryptixException (a subclass
     * of <a href=java.security.ProviderException.html>ProviderException</a>).
     *
     * @return the plaintext block size
     */
    protected int enginePlaintextBlockSize() {
        if (primeLen == 0) throw new CryptixException(
            "ElGamal: plaintext block size is not valid until key is set");
        return primeLen;
    }

    /**
     * <b>SPI</b>: Return the ciphertext block size, in bytes. For ElGamal this
     * is <strong>double</strong> the number of bytes needed to represent <i>p-1</i>.
     * <p>
     * If the key has not been set, this method throws CryptixException (a subclass
     * of <a href=java.security.ProviderException.html>ProviderException</a>).
     *
     * @return the ciphertext block size
     */
    protected int engineCiphertextBlockSize() {
        if (primeLen == 0) throw new CryptixException(
            "ElGamal: ciphertext block size is not valid until key is set");
        return primeLen*2;
    }

    /**
     * <b>SPI</b>: Set an algorithm-specific parameter.
     * <p>
     * ElGamal has one algorithm-specific parameter called "random", of type
     * java.util.Random, which specifies the source of random bits used to
     * generate the <i>k</i> values needed for encryption. If this parameter
     * is not set when <code>initKey</code> is called, the result of
     * <code>new SecureRandom()</code> will be used.
     * <p>
     * You can set the "random" parameter using the following code:
     * <pre>
     *   try {
     *       elgamal.setParameter("random", existingSecureRandom);
     *   } catch (InvalidParameterException e) { /* ignore &#42;/ }
     *   elgamal.initEncrypt(publicKey);
     * </pre>
     * <p>
     * This is not useful if the cipher will only be used for decryption.
     *
     * @param param the string identifier of the parameter.
     * @param value the parameter value.
     * @exception InvalidParameterException if (!(param.equals("random") &&
     *          value instanceof java.util.Random))
     */
    protected void engineSetParameter(String param, Object value) {
        if (param.equals("random")) {
            if (!(value instanceof Random)) throw new InvalidParameterException(
                "value must be an instance of java.util.Random");

            rng = (Random)value;
            return;
        }
        throw new InvalidParameterException(param);
    }

    /** <b>SPI</b>: Return an algorithm-specific parameter.
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

    /**
     * <b>SPI</b>: DOCUMENT ME
     */
    protected int engineUpdate(byte[] in, int inOffset, int inLen,
                               byte[] out, int outOffset) {
        if (inLen <= 0) return 0;

        if (getState() == ENCRYPT) {
            if (inLen != primeLen) throw new IllegalBlockSizeException(
                "inLen = " + inLen + ", plaintext block size = " + primeLen);

            byte[] plaintext = new byte[primeLen];
            System.arraycopy(in, inOffset, plaintext, 0, primeLen);
            BigInteger[] ab = new BigInteger[2];

            BigInteger M = new BigInteger(POSITIVE, plaintext);
            ElGamalAlgorithm.encrypt(M, ab, p, g, y, rng);

            byte[] aBytes = ab[0].toByteArray();
            byte[] bBytes = ab[1].toByteArray();
            ArrayUtil.clear(out, outOffset, primeLen*2);
            System.arraycopy(aBytes, 0, out, outOffset + primeLen - aBytes.length,
                aBytes.length);
            System.arraycopy(bBytes, 0, out, outOffset + primeLen*2 - bBytes.length,
                bBytes.length);

            // don't leave plaintext hanging about indefinitely.
            ArrayUtil.clear(plaintext);
            return primeLen*2;
        } else {
            // getState() == DECRYPT
            if (inLen != primeLen*2) throw new IllegalBlockSizeException(
                "inLen = " + inLen + ", ciphertext block size = " + primeLen*2);

            byte[] ciphertext = new byte[primeLen];
            System.arraycopy(in, inOffset, ciphertext, 0, primeLen);
            BigInteger a = new BigInteger(POSITIVE, ciphertext);
            System.arraycopy(in, inOffset + primeLen, ciphertext, 0, primeLen);
            BigInteger b = new BigInteger(POSITIVE, ciphertext);

            BigInteger M = ElGamalAlgorithm.decrypt(a, b, p, g, x);

            byte[] plaintext = M.toByteArray();
            ArrayUtil.clear(out, outOffset, primeLen - plaintext.length);
            System.arraycopy(plaintext, 0, out, outOffset + primeLen - plaintext.length,
                plaintext.length);
            return primeLen;
        }
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
        try { self_test(new PrintWriter(System.out, true)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static void self_test(PrintWriter out)
    throws KeyException {
        KeyPairGenerator keygen = new BaseElGamalKeyPairGenerator();
        SecureRandom random = new SecureRandom();

        long start = System.currentTimeMillis();
        keygen.initialize(385, random);
        KeyPair keypair = keygen.generateKeyPair();
        long duration = System.currentTimeMillis() - start;

        out.println("Keygen: " + (float)duration/1000 + " seconds");

        RawElGamalCipher raw = new RawElGamalCipher();
        raw.test(out, keypair, random);
    }

    private void test(PrintWriter out, KeyPair keypair, SecureRandom random)
    throws KeyException {
        ElGamalPrivateKey privateKey = (ElGamalPrivateKey) (keypair.getPrivate());
        ElGamalPublicKey publicKey = (ElGamalPublicKey) (keypair.getPublic());

        BigInteger M = new BigInteger(privateKey.getP().bitLength() - 1, random);
        rng = random;

        long start = System.currentTimeMillis();
        initEncrypt(publicKey);
        BigInteger[] ab = new BigInteger[2];
        ElGamalAlgorithm.encrypt(M, ab, p, g, y, rng);
        long midpoint = System.currentTimeMillis();
        initDecrypt(privateKey);
        BigInteger Mdash = ElGamalAlgorithm.decrypt(ab[0], ab[1], p, g, x);
        long end = System.currentTimeMillis();

        out.println("p = " + p);
        out.println("g = " + g);
        out.println("x = " + x);
        out.println("y = " + y);
        out.println("M = " + M);
        out.println("a = " + ab[0]);
        out.println("b = " + ab[1]);

        if (!(M.equals(Mdash))) {
            out.println("DECRYPTION FAILED!");
            out.println("M' = " + Mdash);
        }

        out.println("Encrypt: " + ((float) (midpoint - start) / 1000) + " seconds");
        out.println("Decrypt: " + ((float) (end - midpoint) / 1000) + " seconds");
    }
}
