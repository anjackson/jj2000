// $Id: BaseRSAKeyPairGenerator.java,v 1.1.1.1 2002/08/27 12:32:13 grosbois Exp $
//
// $Log: BaseRSAKeyPairGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:13  grosbois
// Add cryptix 3.2
//
// Revision 1.9  2000/08/17 11:40:59  edwin
// java.* -> xjava.*
//
// Revision 1.8  1999/07/12 20:34:21  edwin
// renaming java.security.interfaces.RSAPrivateKey and RSAPublicKey to CryptixRSAPrivateKey and CryptixRSAPublicKey. This is one more step to JDK1.2 compatibility.
//
// Revision 1.7  1997/12/22 03:16:33  hopwood
// + RawRSACipher framing bug fixed.
// + BaseRSAKeyPairGenerator test #2 fixed.
//
// Revision 1.6  1997/11/23 03:09:18  hopwood
// + Mostly documentation changes.
//
// Revision 1.5  1997/11/22 07:05:40  raif
// + Added timing computation. Will display when Debug.Level.this >= 7.
// + Cosmetics.
//
// Revision 1.4  1997/11/20 19:46:57  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.2  1997/11/20  David Hopwood
// + Fix minor compilation bugs in 1.3.1.
//
// Revision 1.3.1  1997/11/16  David Hopwood
// + Call RSAAlgorithm instead of duplicating the RSA implementation for
//   testing.
// + Added makeKeyPair method, which can be overridden by subclasses.
// + Minor documentation and debugging changes.
//
// Revision 1.3  1997/11/05 16:48:02  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/04 19:33:31  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.0  1997/07/23  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */

package cryptix.provider.rsa;

import cryptix.util.core.Debug;
import cryptix.util.core.BI;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import xjava.security.interfaces.RSAKeyPairGenerator;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

/**
 * A class capable of generating RSA key pairs. The generator is first
 * initialized, then used to generate one or more RSA key pairs.
 * <p>
 * Users wishing to indicate the public exponent, and to generate a
 * key pair suitable for use with the RSA algorithm typically:
 * <ol>
 *   <li> Get a key pair generator for the RSA algorithm by calling
 *        the KeyPairGenerator <i>getInstance</i> method with "RSA" as
 *        its argument.
 *   <li> Initialize the generator by casting the result to an
 *        RSAKeyPairGenerator and calling the <i>initialize</i> method
 *        from this RSAKeyPairGenerator interface.
 *   <li> Generate one or more key pairs by calling the
 *        <i>generateKeyPair</i> method from the KeyPairGenerator class,
 *        as often as desired.
 * </ol>
 * <p>
 * Note: To use this generator in your configuration, make sure that
 * the following property is set in the <samp>Cryptix.properties</samp>
 * file (located in the <i>cryptix-lib</i> directory):
 * <pre>
 *     KeyPairGenerator.RSA = cryptix.provider.rsa.BaseRSAKeyPairGenerator
 * </pre>
 * <p>
 * The algorithm used to generate RSA keys is that described in [1],
 * adapted for our case where <i>e</i> is known in advance:
 * <ol>
 *   <li> Generate two large random and distinct primes <i>p</i> and
 *        <i>q</i>, each roughly the same size.
 *   <li> Compute <i>phi = (p - 1)(q - 1)</i>.
 *   <li> If <i>gcd(e, phi) != 1</i>, go to step 1.
 *   <li> Compute <i>n = pq</i>.
 *   <li> Use the extended Euclidean algorithm to compute the unique
 *        integer <i>d, 1 < d < phi</i>, such that <i>ed = 1 mod phi</i>.
 * </ol>
 * <p>
 * For the prime number generation, we use java.math.BigInteger class
 * methods and constructors which rely (as of JDK 1.1 and up to the time
 * of this writing) on <A HREF="mailto:colin@nyx.cs.du.edu">Colin Plumb</A>'s
 * <i>BigNum multi-precision integer math library</i>. It is not clear
 * though what part of this library is called (by the <code>plumbGeneratePrime</code>
 * native method) for the actual probable prime generation.
 * <p>
 * The BigInteger class also uses the Miller-Rabin probabilistic primality
 * test, also known as <i>strong pseudo prime test</i> as described in
 * FIPS-186, with a user supplied <i>certainty</i> factor, referred to in
 * the source as <code>isProbablePrime</code>. In this implementation we provide
 * a default value of <b>80</b> for this parameter. In future revisions we
 * will refine the computations to set this parameter, depending on the
 * strength of the desired prime, using a function to compute an upperbound
 * limit on the Miller-Rabin test error probability.
 * <p>
 * References:
 * <ol>
 *   <li> A. J. Menezes, P. C. van Oorschot, S. A. Vanstone,
 *        <cite>Handbook of Applied Cryptography</cite>,
 *        CRC Press 1997, pp 286-291.
 *        <p>
 *   <li> Bruce Schneier,
 *        "Section 19.3 RSA,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons 1996.
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @see     java.security.KeyPairGenerator
 */
public class BaseRSAKeyPairGenerator
extends KeyPairGenerator
implements RSAKeyPairGenerator
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "BaseRSAKeyPairGenerator") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("BaseRSAKeyPairGenerator: " + s); }


// Constants and variables
//...........................................................................

    private int strength;
    private BigInteger e;
    private SecureRandom source;

    /**
     * Used to assert that the generated <i>p</i> and <i>q</i> factors
     * are primes with a probability that will exceed <i>1 - (1/2)**
     * CONFIDENCE</i>.
     * <p>
     * FIPS-186 (May 1994), APPENDIX 2. GENERATION OF PRIMES FOR THE DSA,
     * section 2.1. A PROBABILISTIC PRIMALITY TEST, describes the algorithm
     * used in BigInteger's <i>isProbablePrime()</i> method and suggests a
     * value not less than 50. The litterature [1] recommends a value of 80
     * which we use.
     */
    private static final int CONFIDENCE = 80;

    /** Fermat prime F4. */
    private static final BigInteger F4 = BigInteger.valueOf(0x10001L);
    
    private static final BigInteger ONE = BigInteger.valueOf(1L);

    /**
     * Default value of the strength (number of bits) of the public moduli
     * generated by this object.
     */
    private static final int DEFAULT_STRENGTH = 1024;


// Constructor
//...........................................................................

    public BaseRSAKeyPairGenerator() { super("RSA"); }


// RSAKeyPairGenerator interface method implementation
//...........................................................................

    /**
     * Initialise the key pair generator using the specified strength
     * (desired public modulus length in bits), public exponent, and a
     * source of random bits.
     *
     * @param  strength  desired number of bits in the public modulus
     *                   to be generated by this object. If null or
     *                   less than 2 then use the set DEFAULT_STRENGTH
     * @param  e         the encryption/decryption exponent. If null
     *                   then use Fermat's F4 prime.
     * @param  source    a cryptographically strong source of pseudo
     *                   random data. If null then use a default one.
     */
    public void initialize (int strength, BigInteger e, SecureRandom source) {
        this.e = (e == null) ? F4 : e;
        this.strength = (strength < 2) ? DEFAULT_STRENGTH : strength;
        this.source = (source == null) ? new SecureRandom() : source;
    }


// KeyPairGenerator abstract methods implementation
//...........................................................................

    /**
     * Initialise the RSA key pair generator for a given key strength
     * (its number of bits), using the Fermat prime F4 (0x10001) as the
     * public exponent.
     *
     * @param  strength  desired number of bits in the public modulus
     *                   to be generated by this object.
     * @param  source    a cryptographically strong source of pseudo
     *                   random data.
     */
    public void initialize (int strength, SecureRandom source) {
        initialize(strength, F4, source);
    }

    /**
     * Generate a new RSA key pair with the confidence that each of the
     * public modulus <i>n</i> factors <i>p</i> and <i>q</i> are primes
     * with a mathematical probability that will exceed <i>1 - (1/2)**
     * CONFIDENCE</i>.
     */
    public KeyPair generateKeyPair() {
        int k1 = strength / 2;
        int k2 = strength - k1;
        BigInteger p, q, n, phi, d;
        long t1 = 0;
if (DEBUG && debuglevel >= 7) t1 = System.currentTimeMillis();

        while (true) {
            try {
                while (true) {
//                    p = new BigInteger(k1, CONFIDENCE, source).setBit(k1-1).setBit(k1-2);
//                    q = new BigInteger(k2, CONFIDENCE, source).setBit(k2-1).setBit(k2-2);
                    p = new BigInteger(k1, CONFIDENCE, source);
                    q = new BigInteger(k2, CONFIDENCE, source);
                    n = p.multiply(q);
                    if (p.compareTo(q) != 0 && n.bitLength() == strength)
                        break;
                }
                phi = p.subtract(ONE).multiply(q.subtract(ONE));
                d = e.modInverse(phi);
                break;
            }
            catch (ArithmeticException ae) {}  // gcd(e * phi) != 1. Try again
        }

if (DEBUG && debuglevel >= 7) {
    t1 = System.currentTimeMillis() - t1;
    debug(" ...generateKeyPair() completed in "+t1+" ms.");
}

if (DEBUG && debuglevel >= 5) {
    try {
        // rsa-encrypt twice
        err.print("RSA parameters self test #1/2... ");
        BigInteger x = new BigInteger(k1, source);
        BigInteger y = RSAAlgorithm.rsa(x, n, e);
        BigInteger z = RSAAlgorithm.rsa(y, n, d);
        boolean yes = z.compareTo(x) == 0;
        err.println(yes ? "OK" : "Failed");
        if (!yes) throw new RuntimeException();
        // rsa-encrypt then -decrypt
        err.print("RSA parameters self test #2/2... ");
        BigInteger u = q.modInverse(p);
        z = RSAAlgorithm.rsa(y, n, d, p, q, u);
        yes = z.compareTo(x) == 0;
        err.println(yes ? "OK" : "Failed");
        if (!yes) throw new RuntimeException();
        err.println();
    }
    catch (Exception ex) {
        err.println("RSA parameters:");
        err.println("         n: " + BI.dumpString(n));
        err.println("         e: " + BI.dumpString(e));
        err.println("         d: " + BI.dumpString(d));
        err.println("         p: " + BI.dumpString(p));
        err.println("         q: " + BI.dumpString(q));
        err.println("q^-1 mod p: " + BI.dumpString(q.modInverse(p)));

        throw new RuntimeException(e.toString());
    }
}
        return makeKeyPair(n, e, d, p, q);
    }


// Own methods
//...........................................................................

    /** Makes an RSA key pair using the given parameters. */
    protected KeyPair makeKeyPair(BigInteger n, BigInteger e, BigInteger d,
                                  BigInteger p, BigInteger q) {
        CryptixRSAPublicKey pk = new RawRSAPublicKey(n, e);
        CryptixRSAPrivateKey sk = new RawRSAPrivateKey(d, p, q);

        return new KeyPair(pk, sk);
    }

    /**
     * Initialise the RSA key pair generator for key strength value of
     * 1024-bit, using the Fermat prime F4 (0x10001) as the encryption/
     * decryption exponent and a default SecureRandom source.
     */
    public void initialize () {
        initialize(DEFAULT_STRENGTH, F4, new SecureRandom());
    }
}
