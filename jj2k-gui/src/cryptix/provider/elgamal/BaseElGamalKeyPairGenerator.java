// $Id: BaseElGamalKeyPairGenerator.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: BaseElGamalKeyPairGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.6  2000/08/17 11:40:54  edwin
// java.* -> xjava.*
//
// Revision 1.5  2000/08/16 17:55:25  edwin
// Remove try blocks for exceptions that cannot be thrown.
//
// Spotted-by: jikes +P
//
// Revision 1.4  1997/12/15 02:50:09  hopwood
// + Committed changes below.
//
// Revision 1.3.1  1997/12/15  hopwood
// + Follow changes in cryptix.util.math.Prime.
//
// Revision 1.3  1997/12/14 17:37:58  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/12/14  hopwood
// + Cosmetics.
// + Use cryptix.util.core.Debug for debugging.
// + Made the default ElGamal parameter set configurable in the properties
//   file.
//
// Revision 1.2  1997/12/13 22:53:16  raif
// + Moved the arithmetic functions to cryptix.util.math.Prime.
// + Use the new class.
// + Added alternative in parameter generation to use GORDON-built
//   strong primes. Execution is now faster.
//
// Revision 1.1  1997/12/07 06:37:26  hopwood
// + Major overhaul of ElGamal to match RSA.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
/*
 * Copyright (c) 1995-97 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */

package cryptix.provider.elgamal;

import cryptix.CryptixException;
import cryptix.CryptixProperties;
import cryptix.util.math.Prime;
import cryptix.util.core.Debug;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.InvalidParameterException;
import xjava.security.interfaces.ElGamalPrivateKey;
import xjava.security.interfaces.ElGamalPublicKey;
import xjava.security.interfaces.ElGamalParams;
import xjava.security.interfaces.ElGamalKeyPairGenerator;

/**
 * A class capable of generating ElGamal key pairs. The generator is first
 * initialized, then used to generate one or more key pairs.
 * <p>
 * Users wishing to indicate the prime or base, and to generate a key 
 * pair suitable for use with the ElGamal signature or encryption algorithms
 * typically
 * <ol>
 *   <li> Get a key pair generator for the ElGamal algorithms by calling the 
 *        KeyPairGenerator <code>getInstance</code> method with "ElGamal" 
 *        as its argument.<p> 
 *   <li> Initialize the generator by casting the result to an
 *        ElGamalKeyPairGenerator and calling one of the
 *        <code>initialize</code> methods.<p>
 *   <li> Generate one or more key pairs by calling the
 *        <code>generateKeyPair</code> method, as often as desired.
 * </ol> 
 * <p>
 * The algorithm used to generate ElGamal keys is as follows:
 * <ol>
 *   <li> Generate a random probable-prime, <i>p</i>, of the desired length
 *        (using java.math.BigInteger.generatePrime).
 *   <li> Find the small prime factors of <i>p-1</i> by trial division.
 *   <li> Divide </i>p-1</i> by all its small prime factors, and check that
 *        the result is probably-prime.
 *   <li> Pick a random <i>g</i>, with one less bit than <i>p</i>.
 *   <li> Repeat step 3 until <i>g</i> is a generator mod <i>p</i>
 *        (using the test given in Schneier section 11.3, and noting
 *        that we know the factors of <i>p-1</i> from steps 2 and 3).
 *   <li> Pick a random <i>x</i>, also with one less bit than <i>p</i>.
 *   <li> Calculate <i>y = g^x</i> mod <i>p</i>.
 * </ol>
 * <p>
 * If <i>p</i> and <i>g</i> are specified in advance then only the last two
 * steps are needed.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 19.6 ElGamal," and "Section 11.3 Number Theory" (heading
 *        "Generators," pages 253-254),
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996
 *        <p>
 *   <li> S.C. Pohlig and M.E. Hellman, "An Improved Algorithm for Computing
 *        Logarithms in GF(p) and Its Cryptographic Significance,"
 *        <cite>IEEE Transactions on Information Theory</cite>,
 *        v. 24 n. 1, Jan 1978, pages 106-111.
 *        <p>
 *   <li> IEEE P1363 draft standard,
 *        <a href="http://stdsbbs.ieee.org/groups/1363/index.html">
 *        http://stdsbbs.ieee.org/groups/1363/index.html</a>
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  Cryptix 2.2.2
 * @see java.security.KeyPairGenerator
 */
public class BaseElGamalKeyPairGenerator
extends KeyPairGenerator
implements ElGamalKeyPairGenerator
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("ElGamal", "BaseElGamalKeyPairGenerator") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("BaseElGamalKeyPairGenerator: " + s); }
    private static void progress(String s) { err.print(s); err.flush(); }


// Constants and vars
//...........................................................................

    /**
     * Use probable-primes with no more than a 2^-80 chance of being composite.
     */
    private static final int CONFIDENCE = 80;

    /**
     * Setting this to false prevents pre-computed parameters from being
     * used (i.e. new parameters will always be generated).
     */
    private static final boolean USE_PRECOMPUTED = true;

    /**
     * True if small values of <i>g</i>, chosen from the
     * <code>efficientBases</code> array will be used.
     * <p>
     * Currently there is no advantage in setting this to true, since
     * java.math.BigInteger#modPow is not optimized for small <i>g</i>.
     */
    private static final boolean USE_SMALL_G = false;

    /**
     * The type of primes that are to be generated for parameters:
     * Prime.PLAIN, Prime.STRONG, or Prime.GERMAIN.
     */
    private static final int PRIME_TYPE = Prime.STRONG;

    /**
     * The mimimum acceptable length in bits of the prime, <i>p</i>.
     * (If you change this other than temporarily for debugging, make sure
     * the documentation is also changed.)
     */
    private static final int MIN_PRIME_LEN = 256;

    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);

    private static GenericElGamalParameterSet defaultParamSet;

    static {
        try {
            String classname = CryptixProperties.getProperty(
                "Alg.DefaultParameterSet.ElGamal");
            if (classname != null)
                defaultParamSet = (GenericElGamalParameterSet)
                    (Class.forName(classname).newInstance());
        } catch (Exception e) {
if (DEBUG && debuglevel >= 1) debug("exception while instantiating default parameter set: " + e);
        }
        if (defaultParamSet == null)
            defaultParamSet = new DefaultElGamalParameterSet();
    }

    protected BigInteger p;
    protected BigInteger g;
    protected SecureRandom source;


// Constructor
//...........................................................................

    public BaseElGamalKeyPairGenerator() { super("ElGamal"); }

    /**
     * Initialises the key pair generator using the prime and base from
     * the specified ElGamalParams object.
     *
     * @param params    the parameters to use to generate the keys.
     * @param random    the random bit source to use to generate 
     *                  key bits.
     * @exception NullPointerException if params == null || random == null
     * @exception InvalidParameterException if the parameters passed are
     *                  invalid.
     */
    public void initialize(ElGamalParams params, SecureRandom random)
        throws InvalidParameterException {
        initialize(params.getP(), params.getG(), random);
    }

    /**
     * Initialises the key pair generator using the specified prime
     * (</i>p</i>) and base (<i>g</i>). The difficulty of cracking ElGamal
     * by solving the discrete logarithm problem is dependent on the length
     * of the prime.
     * <p>
     * An InvalidParameterException will be thrown if <code>base >= prime</code>.
     *
     * @param prime     the prime to be used, as a java.math.BigInteger
     * @param base      the base to be used, as a java.math.BigInteger
     * @param random    the random bit source to use to generate 
     *                  key bits.
     * @exception NullPointerException if prime == null || base == null ||
     *                  random == null
     * @exception InvalidParameterException if the parameters passed are
     *                  invalid.
     */
    public void initialize(BigInteger prime, BigInteger base, SecureRandom random)
        throws InvalidParameterException {
        if (prime == null) throw new NullPointerException("prime == null");
        if (base == null) throw new NullPointerException("base == null");
        if (random == null) throw new NullPointerException("random == null");
        if (base.compareTo(prime) >= 0)
            throw new InvalidParameterException("base >= prime");

        p = prime;
        g = base;
        source = random;
    }

    /**
     * Initialises the key pair generator for a given prime length,
     * without parameters.
     *
     * @param primeLen  the prime length, in bits. Valid lengths are any
     *                  integer >= 256.
     * @param random    the random bit source to use to generate
     *                  key bits.
     * @exception InvalidParameterException if the prime length is less
     *                  than 256.
     */
    public void initialize(int primeLen, SecureRandom random) {
        ElGamalParams params = null;
        if (USE_PRECOMPUTED && defaultParamSet != null)
            params = defaultParamSet.getParameters(primeLen);

        if (params == null)
            params = generateParams(primeLen, random);

        p = params.getP();
        g = params.getG();
        source = random;
    }

    /**
     * Initialises the key pair generator for a given prime length,
     * without parameters.
     * <p>
     * If <i>genParams</i> is true, this method will generate new
     * p and g parameters. If it is false, the method will use precomputed
     * parameters for the prime length requested. If there are no
     * precomputed parameters for that prime length, an exception will be
     * thrown. It is guaranteed that there will always be default
     * parameters for a prime length of 512 bits.
     * <p>
     * [Future versions will probably also support 1024, 1536, 2048, 3072,
     * and 4096 bits.]
     *
     * @param primeLen  the prime length, in bits. Valid lengths are any
     *                  integer >= 256.
     * @param random    the random bit source to use to generate
     *                  key bits.
     * @param genParams whether to generate new parameters for the prime
     *                  length requested.
     * @exception InvalidParameterException if the prime length is less
     *                  than 256, or if genParams is false and there are not
     *                  precomputed parameters for the prime length requested.
     */
    public void initialize(int primeLen, boolean genParams, SecureRandom random)
        throws InvalidParameterException {
        if (primeLen < MIN_PRIME_LEN) throw new InvalidParameterException(
            "ElGamal: prime length " + primeLen + " is too short (< " + MIN_PRIME_LEN + ")");

        ElGamalParams params;
        if (!USE_PRECOMPUTED || genParams || defaultParamSet == null) {
            params = generateParams(primeLen, random);
        } else {
            params = defaultParamSet.getParameters(primeLen);
            if (params == null) throw new InvalidParameterException(
                "ElGamal: no pre-computed parameters for prime length " + primeLen);
        }
        p = params.getP();
        g = params.getG();
        source = random;
    }

    /**
     * Generates a key pair.
     */
    public KeyPair generateKeyPair() {
        if (p == null) throw new CryptixException(
            "ElGamal: key pair generator not initialized");

        int length = p.bitLength()-1;
        BigInteger x = new BigInteger(length, source).setBit(length);

        ElGamalPrivateKey privateKey =
            new BaseElGamalPrivateKey(p, g, x);
        ElGamalPublicKey publicKey =
            new BaseElGamalPublicKey(p, g, privateKey.getY());
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Generates new parameters, <i>p</i> and <i>g</i>. This method
     * does not change the parameters currently being used by
     * <code>generateKeyPair</code>.
     *
     * @param primeLen  the prime length, in bits. Valid lengths are any
     *                  integer >= 256.
     * @param random    the random bit source to use to generate the parameters.
     * @exception InvalidParameterException if the prime length is less
     *                  than 256.
     */
    public ElGamalParams generateParams(int primeLen, SecureRandom random)
    throws InvalidParameterException {
        if (primeLen < MIN_PRIME_LEN) throw new InvalidParameterException(
            "ElGamal: prime length " + primeLen + " is too short (< " + MIN_PRIME_LEN + ")");

        Object[] result = Prime.getElGamal(primeLen, CONFIDENCE, random, PRIME_TYPE);
        BigInteger newP = (BigInteger) result[0];
        BigInteger[] q = (BigInteger[]) result[1];
        BigInteger newG = findG(newP, q, random);
        return new BaseElGamalParams(newP, newG);
    }

    /**
     * Returns a generator mod <i>p</i>. <i>q</i> is an array containing all
     * the prime factors of <i>p-1</i>.
     * <p>
     * This algorithm is based on Schneier page 254.
     */
    private static BigInteger findG(BigInteger p, BigInteger[] q,
                                    SecureRandom random) {
        BigInteger p_minus_1 = p.subtract(ONE);
        BigInteger[] z = new BigInteger[q.length];
        BigInteger g;

        // z_i = (p-1)/q_i for each factor q_i
        for (int i = 0; i < q.length; i++) {
            z[i] = p_minus_1.divide(q[i]);
        }
if (DEBUG && debuglevel >= 5) progress("g =");
        if (USE_SMALL_G) {
            for (int i = 0; i < efficientBases.length; i++) {
                g = efficientBases[i];
if (DEBUG && debuglevel >= 5) progress(" " + g + "?");
                if (Prime.isGeneratorModP(g, p, z)) {
if (DEBUG && debuglevel >= 4) err.println(" OK");
                    return g;
                }
            }
        }
        int length = p.bitLength()-1;
        while (true) {
if (DEBUG && debuglevel >= 5) progress(" ?");
            g = new BigInteger(length, random).setBit(length);
            if (Prime.isGeneratorModP(g, p, z)) {
if (DEBUG && debuglevel >= 4) err.println(" OK");
                return g;
            }
        }
    }

    private static BigInteger[] efficientBases;

    static {
        if (USE_SMALL_G) {
            int[] eb = { 2, 3, 17, 257, 65537, };
            efficientBases = new BigInteger[eb.length];
            for (int i = 0; i < eb.length; i++)
                efficientBases[i] = BigInteger.valueOf(eb[i]);
        }
    }
}
