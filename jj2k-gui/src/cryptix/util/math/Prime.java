// $Id: Prime.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: Prime.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.4  1997/12/15 02:55:44  hopwood
// + Committed changes below.
//
// Revision 1.3.1  1997/12/15  hopwood
// + Made prime bitmap more space-efficient (by a factor of 16 in most VMs).
// + Renamed variables in the two getSmallFactors methods for consistency.
// + Moved some more of the ElGamal parameter generation code into the
//   getElGamal method of this class. Allow any of PLAIN, STRONG or GERMAIN
//   primes to be generated.
//
// Revision 1.3  1997/12/14 23:31:56  raif
// *** empty log message ***
//
// Revision 1.2.1  1997/12/15  R. Naffah
// + added isGermain() method to test if a number is a
//   Sophie Germain prime.
// + documentation.
//
// Revision 1.2  1997/12/14 17:45:26  hopwood
// + Committed changes below.
//
// Revision 1.1.1  1997/12/14  hopwood
// + Cosmetics.
// + Use cryptix.util.core.Debug for debugging.
// + Fixed bug in getSmallFactors (BigInteger p_1, int certainty,
//   BigInteger r) where r, not t was being tested for primality.
//   Unfortunately this makes parameter generation slower.
// + Added compile-time option, USE_GORDON, to determine whether Gordon's
//   algorithm will be used.
// + Added an isProbablePrimeFast method, which uses trial division on
//   primes up to SMALL_PRIME_THRESHOLD before doing Miller-Rabin.
//
// Revision 1.1  1997/12/13 22:51:15  raif
// + Added GORDON algorithm implementation for building strong
//   primes. ElGamal parameter generation is now faster.
// + Added method to find Germain primes. Although these primes
//   are not said to offer more security with large bit-length
//   primes, it's good to have a method for building them.
// + Added new method for finding all small factors of a value
//   when an already known large prime factor is known. Useful
//   for ElGamal parameter generation when the prime is built using
//   GORDON algorithm.
// + Added [HAC] reference.
// + Original version based on prime integer routines scattered
//   in RSA and ElGamal classes.
//
// $Endlog$
/*
 * Copyright (c) 1995-97 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */

package cryptix.util.math;

import cryptix.CryptixException;
import cryptix.util.core.Debug;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Random;
import java.util.Vector;

/**
 * A utility class to handle different algorithms for large prime
 * number generation, factorisation and tests.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a name="HAC">[HAC]</a>
 *        A. J. Menezes, P. C. van Oorschot, S. A. Vanstone,
 *        <a href="http://www.dms.auburn.edu/hac/">
 *        <cite>Handbook of Applied Cryptography</cite></a>
 *        CRC Press 1997, pp 145-154.
 *        <p>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 19.6 ElGamal," and "Section 11.3 Number Theory" (heading
 *        "Generators," pages 253-254),
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996
 *        <p>
 *   <li> S. C. Pohlig and M. E. Hellman, "An Improved Algorithm for Computing
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
 * @author  Raif S. Naffah
 * @author  David Hopwood
 */
public final class Prime
{
// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel = DEBUG ? Debug.getLevel("Prime") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("Prime: " + s); }
    private static void progress(String s) { err.print(s); err.flush(); }


// Constants and vars
//...........................................................................

    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private static final BigInteger TWO = BigInteger.valueOf(2L);

    public static final int PLAIN = 0;
    public static final int STRONG = 1;
    public static final int GERMAIN = 2;


// Constructor
//...........................................................................

    /** No instantiation possible. All methods are static. */
    private Prime () {}


// Prime generation methods
//...........................................................................

    /**
     * Returns a Gordon <b>strong</b> probable-prime with an approximate
     * specified <i>bitlength</i>, that is prime with a probability exceeding
     * 1 - (1/2)<sup><i>certainty</i></sup>.
     * <p>
     * A prime is said to be <b>strong</b> iff integers <i>r</i>, <i>s</i>,
     * and <i>t</i> exist such that the following three conditions are
     * satisfied:
     * <ol>
     *   <li> <i>p</i> - 1 has a large prime factor, denoted <i>r</i>,
     *   <li> <i>p</i> + 1 has a large prime factor, denoted <i>s</i>, and
     *   <li> <i>r</i> - 1 has a large prime factor, denoted <i>t</i>.
     * </ol>
     * <p>
     * GORDON's algorithm is described in [HAC] p.150 as follows:
     * <ol>
     *   <li> generate 2 random primes <i>s</i> and <i>t</i> of roughly
     *        equal bit-length.
     *   <li> select an integer i0. Find the first prime in the
     *        sequence <i>2it + 1</i>, for <i>i = i0, i0+1, i0+2,...</i>
     *        Denote this prime by <i>r = 2it + 1</i>.
     *   <li> compute <i>p0 = 2(s<sup>(r-2)</sup> mod r)s - 1</i> --See errata
     *        on [HAC] web site.
     *   <li> select an integer j0. Find the first prime in the sequence
     *        <i>p0 + 2jrs</i>, for <i>j = j0, j0 + 1, j0 + 2, ...</i>
     *        Denote this prime by <i>p = p0 + 2jrs</i>.
     *   <li> return <i>p</i>.
     * </ol>
     *
     * @param  bitlength    An approximate number of bits that the returned
     *                      prime integer must have.
     * @param  certainty    A measure of the probability that the returned
     *                      integer is a prime. The Miller-Rabin test used
     *                      ensures that the returned value is a prime with a
     *                      probability that exceeds 1 - (1/2)<sup><i>certainty</i></sup>.
     * @param  random       A source of randomness for the bits to use in
     *                      building the prime.
     * @return An array whose elements are respectively p, r, s and t.
     */
    public static BigInteger[]
    getGordon (int bitlength, int certainty, Random random) {
        // 1. generate 2 random primes s and t of roughly equal bitlength
        BigInteger s = new BigInteger(bitlength / 2, certainty, random);
        BigInteger t = new BigInteger(bitlength / 2, certainty, random);
            
        // 2. select an integer i0. Find the first prime in the sequence
        //    2it + 1, for i = i0, i0+1, i0+2,... Denote this prime by
        //    r = 2it + 1
        BigInteger t2 = t.multiply(TWO);
        BigInteger r = t2.add(ONE);

if (DEBUG && debuglevel >= 3) debug("Generating a strong prime (GORDON algorithm)...");
if (DEBUG && debuglevel >= 4) progress("<r>");

        while (!isProbablePrimeFast(r, certainty)) {
            r = r.add(t2);
if (DEBUG && debuglevel >= 5) progress(".");
        }
            
        // 3. compute p0 = 2(s**(r-2) mod r)s - 1.
        // See errata on [HAC] web site.
        BigInteger p0 =
            s.modPow(r.subtract(TWO), r).multiply(s).multiply(TWO).
            subtract(ONE);

        // 4. select an integer j0. Find the first prime in the sequence
        //    p0 + 2jrs, for j = j0, j0+1, j0+2,... Denote this prime by
        //    p = p0 + 2jrs
        BigInteger rs2 = r.multiply(s).multiply(TWO);
        BigInteger p = p0.add(rs2);

if (DEBUG && debuglevel >= 4) progress("<p>");
        while (!isProbablePrimeFast(p, certainty)) {
            p = p.add(rs2);
if (DEBUG && debuglevel >= 5) progress(".");
        }
if (DEBUG && debuglevel >= 4) err.println();

        BigInteger[] result = {p, r, s, t};
        return result;
    }

    /**
     * Returns a Germain (Sophie) probable-prime with an approximate
     * specified <i>bitlength</i>, that is prime with a probability exceeding
     * 1 - (1/2)<sup><i>certainty</i></sup>.
     * <p>
     * An integer <i>p</i> is a GERMAIN prime iff it is a prime, and
     * <i>p</i> = 2<i>q</i> + 1 where <i>q</i> is also a prime.
     *
     * @param  bitlength    An approximate number of bits that the returned
     *                      prime integer must have.
     * @param  certainty    A measure of the probability that the returned
     *                      integer is a prime. The Miller-Rabin test used
     *                      ensures that the returned value is a prime with a
     *                      probability that exceeds 1 - (1/2)<sup><i>certainty</i></sup>.
     * @param  random       A source of randomness for the bits to use in
     *                      building the prime.
     * @return A Germain prime: a prime of the form 2q + 1 where q is also a prime.
     */
    public static BigInteger
    getGermain (int bitlength, int certainty, Random random) {
        BigInteger q, p;

if (DEBUG && debuglevel >= 3) debug("Generating a GERMAIN prime...");
if (DEBUG && debuglevel >= 4) progress("<p>");

        while (true) {
            q = new BigInteger(bitlength, certainty, random);
if (DEBUG && debuglevel >= 5) progress(".");
            p = TWO.multiply(q).add(ONE);
            if (isProbablePrimeFast(p, certainty)) break;
        }
if (DEBUG && debuglevel >= 4) err.println();

        return p;
    }

    /**
     * Generates a random probable-prime, <i>p</i>, of the given length, such that all
     * the factors of <i>p</i> - 1 are known.
     *
     * @param  bitlength    An approximate number of bits that the returned
     *                      prime integer must have.
     * @param  certainty    A measure of the probability that the returned
     *                      integer <i>p</i>, and the largest factor of <i>p</i> - 1
     *                      are primes. The Miller-Rabin test used ensures that
     *                      these values are prime with a probability that exceeds
     *                      1 - (1/2)<sup><i>certainty</i></sup>.
     * @param  random       A source of randomness for the bits to use in
     *                      building the prime.
     * @param  prime_type   what type of prime to build: PLAIN, STRONG or GERMAIN.
     * @return An array of two Objects: the first being the found prime itself,
     *         say <i>p</i>, and the second Object is an array of the known distinct
     *         prime factors of the value (<i>p</i> - 1).
     */
    public static Object[]
    getElGamal (int bitlength, int certainty, Random random, int prime_type) {
        BigInteger p = null;
        BigInteger[] q = null;

        switch (prime_type) {
          case PLAIN:
            // alternative-1: replicate what we had before
            while (q == null) {
                p = new BigInteger(bitlength, certainty, random);
                q = getSmallFactors(p.subtract(ONE), certainty);

                // if input has only small factors it is insecure (see Pohlig
                // and Hellman reference). Make sure that the last prime factor;
                // i.e. the largest, is at least half the length of the input.
                if (q != null && q[q.length - 1].bitLength() <= p.bitLength() / 2) {
if (DEBUG && debuglevel >= 5) debug("largest factor is too short");
                    q = null;
                }
            }
            break;

          case STRONG:
            // alternative-2: use GORDON-built strong primes
            BigInteger[] result;
            BigInteger r;
            while (q == null) {
                result = getGordon(bitlength, certainty, random);
                p = result[0];
                r = result[1];
                q = Prime.getSmallFactors(p.subtract(ONE), certainty, r);
            }
            break;

          case GERMAIN:
            // alternative-3: use GERMAIN primes
            p = getGermain(bitlength, certainty, random);
            q = new BigInteger[] { TWO, p.subtract(ONE).divide(TWO) };
            break;
        }

        Object[] result = {p, q};
        return result;
    }


// Factorisation methods
//...........................................................................

    /**
     * Returns a BigInteger array whose elements are the prime factors of a
     * designated BigInteger value, or null if the value could not easily be
     * factorised.
     *
     * @param  r            A BigInteger to factor.
     * @param  certainty    A measure of the probability that the largest returned
     *                      factor is a prime. The Miller-Rabin test used ensures
     *                      that this factor is a prime with a probability that
     *                      exceeds 1 - (1/2)<sup><i>certainty</i></sup>.
     * @return A BigInteger array whose elements are the distinct prime
     * factors of <i>p</i> when the latter can be written as:
     * <pre>
     *      S_1 * S_2 * ... * S_n * L
     * </pre>
     * Where S_i are small prime factors found in SMALL_PRIMES and L is a
     * large prime factor. Return null otherwise.
     */
    public static BigInteger[] getSmallFactors (BigInteger r, int certainty) {
        BigInteger[] result;
        BigInteger s;
        Vector factors = new Vector();

if (DEBUG && debuglevel >= 5) progress("factors = ");
        for (int i = 0; i < SMALL_PRIMES.length; i++) {
            s = SMALL_PRIMES[i];
            result = r.divideAndRemainder(s);
            if (result[1].equals(ZERO)) {
if (DEBUG && debuglevel >= 5) progress(s + ".");
                factors.addElement(s); // SMALL_PRIMES[i] is a factor.
                r = result[0]; // the quotient

                // it may be a factor more than once; divide out from r.
                while (true) {
                    result = r.divideAndRemainder(s);
                    if (!result[1].equals(ZERO)) break;
if (DEBUG && debuglevel >= 5) progress(s + ".");
                    r = result[0]; // the quotient
                }
            }
        }

        if (!r.equals(ONE)) {
if (DEBUG && debuglevel >= 5) progress("(" + r.bitLength() + "-bit ");
            if (!r.isProbablePrime(certainty)) { // check that r is prime.
if (DEBUG && debuglevel >= 5) err.println("composite)");
                return null;
            }
if (DEBUG && debuglevel >= 5) err.println("prime)");
            factors.addElement(r);
        } else {
if (DEBUG && debuglevel >= 5) err.println("1");
        }

        BigInteger[] z = new BigInteger[factors.size()];
        factors.copyInto(z);
        return z;
    }

    /**
     * Return a BigInteger array whose elements are the prime factors of a
     * designated BigInteger value, for which we already have one large prime
     * factor.
     * <p>
     * The returned array conatins all the distinct factors including the one
     * we gave on input. The returned array is not guaranteed to be in any
     * specific order.
     *
     * @param  r            A BigInteger to factor.
     * @param  certainty    A measure of the probability that the returned integers
     *                      are primes. The Miller-Rabin test used ensures that
     *                      each array element is a prime with a probability that
     *                      exceeds 1 - (1/2)<sup><i>certainty</i></sup>.
     * @param  q            A known prime factor of r.
     * @return If all the prime factors, except two (one of which is <i>q</i>), can
     *         be found in the list of pre-computed small primes the method returns an
     *         array whose elements are the distinct prime factors of <i>r</i>. On the
     *         other hand if not all the prime factors, except two, can be found in the
     *         list of pre-computed small primes the method returns null.
     */
    public static BigInteger[]
    getSmallFactors (BigInteger r, int certainty, BigInteger q) {
        BigInteger[] result = r.divideAndRemainder(q);
        if (!result[1].equals(ZERO)) throw new ArithmeticException(
            "q is not a factor of r");

        BigInteger t = result[0]; // the quotient
        while (true) {
            result = t.divideAndRemainder(q);
            if (!result[1].equals(ZERO)) break;
            t = result[0]; // the quotient
        }
        BigInteger s;
        Vector factors = new Vector();
if (DEBUG && debuglevel >= 5) progress("factors = (" + q.bitLength() + "-bit prime).");

        for (int i = 0; i < SMALL_PRIMES.length; i++) {
            s = SMALL_PRIMES[i];
            result = t.divideAndRemainder(s);
            if (result[1].equals(ZERO)) {
if (DEBUG && debuglevel >= 5) progress(s + ".");
                factors.addElement(s); // SMALL_PRIMES[i] is a factor.
                t = result[0]; // the quotient
                // it may be a factor more than once; reduce t.
                while (true) {
                    result = t.divideAndRemainder(s);
                    if (!result[1].equals(ZERO)) break;
if (DEBUG && debuglevel >= 5) progress(s + ".");
                    t = result[0]; // the quotient
                }
            }
        }

        if (!t.equals(ONE)) {
if (DEBUG && debuglevel >= 5) progress("(" + t.bitLength() + "-bit ");
            if (!t.isProbablePrime(certainty)) { // check that t is prime.
if (DEBUG && debuglevel >= 5) err.println("composite)");
                return null;
            }
if (DEBUG && debuglevel >= 5) err.println("prime)");
            factors.addElement(t);
        } else {
if (DEBUG && debuglevel >= 5) err.println("1");
        }

        factors.addElement(q); // the one we started with
        BigInteger[] z = new BigInteger[factors.size()];
        factors.copyInto(z);
        return z;
    }


// Number quality/characteristics methods
//...........................................................................

    /**
     * @return true iff <i>p</i> is a probable prime and (p-1)/2 is also
     * a probable prime.
     */
    public static boolean isGermain (BigInteger p, int certainty) {
        if (isProbablePrimeFast(p, certainty)) return false;
        BigInteger[] result = p.subtract(ONE).divideAndRemainder(TWO);
        if (!result[1].equals(ZERO)) return false;
        return isProbablePrimeFast(result[0], certainty);
    }

    /**
     * @return true iff <i>g</i> is a generator mod <i>p</i>. <i>z</i> is an
     * array containing <i>(p-1)/q</i>, for each unique prime factor <i>q</i>
     * of <i>p-1</i>.
     */
    public static boolean isGeneratorModP(BigInteger g, BigInteger p,
                                          BigInteger[] z) {
        for (int i = 0; i < z.length; i++)
            if (g.modPow(z[i], p).equals(ONE)) return false;
        return true;
    }

    /**
     * Implements a faster (on average) primality check than
     * <code>BigInteger.isProbablePrime(r, certainty)</code>.
     */
    public static boolean isProbablePrimeFast(BigInteger r, int certainty) {
        int x = r.mod(PRIME_BITMAP_MOD).intValue() >> 1;
        if ((PRIME_BITMAP[x >> 3] & (1 << (x & 0x07))) != 0)
            return false;

        BigInteger q = r.mod(SMALL_PRIME_MOD);
        for (int i = 0; i < SMALL_PRIME_THRESHOLD; i++) {
            if (q.mod(SMALL_PRIMES[i]).equals(ZERO)) return false;
        }
        return r.isProbablePrime(certainty);
    }


// static methods and constants
//...........................................................................
    
    private static BigInteger[] SMALL_PRIMES;

    private static byte[] PRIME_BITMAP;
    private static final int PRIME_BITMAP_SIZE = 30030; // 2*3*5*7*11*13
    private static final BigInteger PRIME_BITMAP_MOD = BigInteger.valueOf(PRIME_BITMAP_SIZE);

    private static final int SMALL_PRIME_THRESHOLD = 100;
    private static BigInteger SMALL_PRIME_MOD;

    static {
        // This array was generated by "java cryptix.examples.math.ListPrimes 50000".
        int[] sp = {
                2,     3,     5,     7,    11,    13,    17,    19,    23,    29,
               31,    37,    41,    43,    47,    53,    59,    61,    67,    71,
               73,    79,    83,    89,    97,   101,   103,   107,   109,   113,
              127,   131,   137,   139,   149,   151,   157,   163,   167,   173,
              179,   181,   191,   193,   197,   199,   211,   223,   227,   229,
              233,   239,   241,   251,   257,   263,   269,   271,   277,   281,
              283,   293,   307,   311,   313,   317,   331,   337,   347,   349,
              353,   359,   367,   373,   379,   383,   389,   397,   401,   409,
              419,   421,   431,   433,   439,   443,   449,   457,   461,   463,
              467,   479,   487,   491,   499,   503,   509,   521,   523,   541,
              547,   557,   563,   569,   571,   577,   587,   593,   599,   601,
              607,   613,   617,   619,   631,   641,   643,   647,   653,   659,
              661,   673,   677,   683,   691,   701,   709,   719,   727,   733,
              739,   743,   751,   757,   761,   769,   773,   787,   797,   809,
              811,   821,   823,   827,   829,   839,   853,   857,   859,   863,
              877,   881,   883,   887,   907,   911,   919,   929,   937,   941,
              947,   953,   967,   971,   977,   983,   991,   997,  1009,  1013,
             1019,  1021,  1031,  1033,  1039,  1049,  1051,  1061,  1063,  1069,
             1087,  1091,  1093,  1097,  1103,  1109,  1117,  1123,  1129,  1151,
             1153,  1163,  1171,  1181,  1187,  1193,  1201,  1213,  1217,  1223,
             1229,  1231,  1237,  1249,  1259,  1277,  1279,  1283,  1289,  1291,
             1297,  1301,  1303,  1307,  1319,  1321,  1327,  1361,  1367,  1373,
             1381,  1399,  1409,  1423,  1427,  1429,  1433,  1439,  1447,  1451,
             1453,  1459,  1471,  1481,  1483,  1487,  1489,  1493,  1499,  1511,
             1523,  1531,  1543,  1549,  1553,  1559,  1567,  1571,  1579,  1583,
             1597,  1601,  1607,  1609,  1613,  1619,  1621,  1627,  1637,  1657,
             1663,  1667,  1669,  1693,  1697,  1699,  1709,  1721,  1723,  1733,
             1741,  1747,  1753,  1759,  1777,  1783,  1787,  1789,  1801,  1811,
             1823,  1831,  1847,  1861,  1867,  1871,  1873,  1877,  1879,  1889,
             1901,  1907,  1913,  1931,  1933,  1949,  1951,  1973,  1979,  1987,
             1993,  1997,  1999,  2003,  2011,  2017,  2027,  2029,  2039,  2053,
             2063,  2069,  2081,  2083,  2087,  2089,  2099,  2111,  2113,  2129,
             2131,  2137,  2141,  2143,  2153,  2161,  2179,  2203,  2207,  2213,
             2221,  2237,  2239,  2243,  2251,  2267,  2269,  2273,  2281,  2287,
             2293,  2297,  2309,  2311,  2333,  2339,  2341,  2347,  2351,  2357,
             2371,  2377,  2381,  2383,  2389,  2393,  2399,  2411,  2417,  2423,
             2437,  2441,  2447,  2459,  2467,  2473,  2477,  2503,  2521,  2531,
             2539,  2543,  2549,  2551,  2557,  2579,  2591,  2593,  2609,  2617,
             2621,  2633,  2647,  2657,  2659,  2663,  2671,  2677,  2683,  2687,
             2689,  2693,  2699,  2707,  2711,  2713,  2719,  2729,  2731,  2741,
             2749,  2753,  2767,  2777,  2789,  2791,  2797,  2801,  2803,  2819,
             2833,  2837,  2843,  2851,  2857,  2861,  2879,  2887,  2897,  2903,
             2909,  2917,  2927,  2939,  2953,  2957,  2963,  2969,  2971,  2999,
             3001,  3011,  3019,  3023,  3037,  3041,  3049,  3061,  3067,  3079,
             3083,  3089,  3109,  3119,  3121,  3137,  3163,  3167,  3169,  3181,
             3187,  3191,  3203,  3209,  3217,  3221,  3229,  3251,  3253,  3257,
             3259,  3271,  3299,  3301,  3307,  3313,  3319,  3323,  3329,  3331,
             3343,  3347,  3359,  3361,  3371,  3373,  3389,  3391,  3407,  3413,
             3433,  3449,  3457,  3461,  3463,  3467,  3469,  3491,  3499,  3511,
             3517,  3527,  3529,  3533,  3539,  3541,  3547,  3557,  3559,  3571,
             3581,  3583,  3593,  3607,  3613,  3617,  3623,  3631,  3637,  3643,
             3659,  3671,  3673,  3677,  3691,  3697,  3701,  3709,  3719,  3727,
             3733,  3739,  3761,  3767,  3769,  3779,  3793,  3797,  3803,  3821,
             3823,  3833,  3847,  3851,  3853,  3863,  3877,  3881,  3889,  3907,
             3911,  3917,  3919,  3923,  3929,  3931,  3943,  3947,  3967,  3989,
             4001,  4003,  4007,  4013,  4019,  4021,  4027,  4049,  4051,  4057,
             4073,  4079,  4091,  4093,  4099,  4111,  4127,  4129,  4133,  4139,
             4153,  4157,  4159,  4177,  4201,  4211,  4217,  4219,  4229,  4231,
             4241,  4243,  4253,  4259,  4261,  4271,  4273,  4283,  4289,  4297,
             4327,  4337,  4339,  4349,  4357,  4363,  4373,  4391,  4397,  4409,
             4421,  4423,  4441,  4447,  4451,  4457,  4463,  4481,  4483,  4493,
             4507,  4513,  4517,  4519,  4523,  4547,  4549,  4561,  4567,  4583,
             4591,  4597,  4603,  4621,  4637,  4639,  4643,  4649,  4651,  4657,
             4663,  4673,  4679,  4691,  4703,  4721,  4723,  4729,  4733,  4751,
             4759,  4783,  4787,  4789,  4793,  4799,  4801,  4813,  4817,  4831,
             4861,  4871,  4877,  4889,  4903,  4909,  4919,  4931,  4933,  4937,
             4943,  4951,  4957,  4967,  4969,  4973,  4987,  4993,  4999,  5003,
             5009,  5011,  5021,  5023,  5039,  5051,  5059,  5077,  5081,  5087,
             5099,  5101,  5107,  5113,  5119,  5147,  5153,  5167,  5171,  5179,
             5189,  5197,  5209,  5227,  5231,  5233,  5237,  5261,  5273,  5279,
             5281,  5297,  5303,  5309,  5323,  5333,  5347,  5351,  5381,  5387,
             5393,  5399,  5407,  5413,  5417,  5419,  5431,  5437,  5441,  5443,
             5449,  5471,  5477,  5479,  5483,  5501,  5503,  5507,  5519,  5521,
             5527,  5531,  5557,  5563,  5569,  5573,  5581,  5591,  5623,  5639,
             5641,  5647,  5651,  5653,  5657,  5659,  5669,  5683,  5689,  5693,
             5701,  5711,  5717,  5737,  5741,  5743,  5749,  5779,  5783,  5791,
             5801,  5807,  5813,  5821,  5827,  5839,  5843,  5849,  5851,  5857,
             5861,  5867,  5869,  5879,  5881,  5897,  5903,  5923,  5927,  5939,
             5953,  5981,  5987,  6007,  6011,  6029,  6037,  6043,  6047,  6053,
             6067,  6073,  6079,  6089,  6091,  6101,  6113,  6121,  6131,  6133,
             6143,  6151,  6163,  6173,  6197,  6199,  6203,  6211,  6217,  6221,
             6229,  6247,  6257,  6263,  6269,  6271,  6277,  6287,  6299,  6301,
             6311,  6317,  6323,  6329,  6337,  6343,  6353,  6359,  6361,  6367,
             6373,  6379,  6389,  6397,  6421,  6427,  6449,  6451,  6469,  6473,
             6481,  6491,  6521,  6529,  6547,  6551,  6553,  6563,  6569,  6571,
             6577,  6581,  6599,  6607,  6619,  6637,  6653,  6659,  6661,  6673,
             6679,  6689,  6691,  6701,  6703,  6709,  6719,  6733,  6737,  6761,
             6763,  6779,  6781,  6791,  6793,  6803,  6823,  6827,  6829,  6833,
             6841,  6857,  6863,  6869,  6871,  6883,  6899,  6907,  6911,  6917,
             6947,  6949,  6959,  6961,  6967,  6971,  6977,  6983,  6991,  6997,
             7001,  7013,  7019,  7027,  7039,  7043,  7057,  7069,  7079,  7103,
             7109,  7121,  7127,  7129,  7151,  7159,  7177,  7187,  7193,  7207,
             7211,  7213,  7219,  7229,  7237,  7243,  7247,  7253,  7283,  7297,
             7307,  7309,  7321,  7331,  7333,  7349,  7351,  7369,  7393,  7411,
             7417,  7433,  7451,  7457,  7459,  7477,  7481,  7487,  7489,  7499,
             7507,  7517,  7523,  7529,  7537,  7541,  7547,  7549,  7559,  7561,
             7573,  7577,  7583,  7589,  7591,  7603,  7607,  7621,  7639,  7643,
             7649,  7669,  7673,  7681,  7687,  7691,  7699,  7703,  7717,  7723,
             7727,  7741,  7753,  7757,  7759,  7789,  7793,  7817,  7823,  7829,
             7841,  7853,  7867,  7873,  7877,  7879,  7883,  7901,  7907,  7919,
             7927,  7933,  7937,  7949,  7951,  7963,  7993,  8009,  8011,  8017,
             8039,  8053,  8059,  8069,  8081,  8087,  8089,  8093,  8101,  8111,
             8117,  8123,  8147,  8161,  8167,  8171,  8179,  8191,  8209,  8219,
             8221,  8231,  8233,  8237,  8243,  8263,  8269,  8273,  8287,  8291,
             8293,  8297,  8311,  8317,  8329,  8353,  8363,  8369,  8377,  8387,
             8389,  8419,  8423,  8429,  8431,  8443,  8447,  8461,  8467,  8501,
             8513,  8521,  8527,  8537,  8539,  8543,  8563,  8573,  8581,  8597,
             8599,  8609,  8623,  8627,  8629,  8641,  8647,  8663,  8669,  8677,
             8681,  8689,  8693,  8699,  8707,  8713,  8719,  8731,  8737,  8741,
             8747,  8753,  8761,  8779,  8783,  8803,  8807,  8819,  8821,  8831,
             8837,  8839,  8849,  8861,  8863,  8867,  8887,  8893,  8923,  8929,
             8933,  8941,  8951,  8963,  8969,  8971,  8999,  9001,  9007,  9011,
             9013,  9029,  9041,  9043,  9049,  9059,  9067,  9091,  9103,  9109,
             9127,  9133,  9137,  9151,  9157,  9161,  9173,  9181,  9187,  9199,
             9203,  9209,  9221,  9227,  9239,  9241,  9257,  9277,  9281,  9283,
             9293,  9311,  9319,  9323,  9337,  9341,  9343,  9349,  9371,  9377,
             9391,  9397,  9403,  9413,  9419,  9421,  9431,  9433,  9437,  9439,
             9461,  9463,  9467,  9473,  9479,  9491,  9497,  9511,  9521,  9533,
             9539,  9547,  9551,  9587,  9601,  9613,  9619,  9623,  9629,  9631,
             9643,  9649,  9661,  9677,  9679,  9689,  9697,  9719,  9721,  9733,
             9739,  9743,  9749,  9767,  9769,  9781,  9787,  9791,  9803,  9811,
             9817,  9829,  9833,  9839,  9851,  9857,  9859,  9871,  9883,  9887,
             9901,  9907,  9923,  9929,  9931,  9941,  9949,  9967,  9973, 10007,
            10009, 10037, 10039, 10061, 10067, 10069, 10079, 10091, 10093, 10099,
            10103, 10111, 10133, 10139, 10141, 10151, 10159, 10163, 10169, 10177,
            10181, 10193, 10211, 10223, 10243, 10247, 10253, 10259, 10267, 10271,
            10273, 10289, 10301, 10303, 10313, 10321, 10331, 10333, 10337, 10343,
            10357, 10369, 10391, 10399, 10427, 10429, 10433, 10453, 10457, 10459,
            10463, 10477, 10487, 10499, 10501, 10513, 10529, 10531, 10559, 10567,
            10589, 10597, 10601, 10607, 10613, 10627, 10631, 10639, 10651, 10657,
            10663, 10667, 10687, 10691, 10709, 10711, 10723, 10729, 10733, 10739,
            10753, 10771, 10781, 10789, 10799, 10831, 10837, 10847, 10853, 10859,
            10861, 10867, 10883, 10889, 10891, 10903, 10909, 10937, 10939, 10949,
            10957, 10973, 10979, 10987, 10993, 11003, 11027, 11047, 11057, 11059,
            11069, 11071, 11083, 11087, 11093, 11113, 11117, 11119, 11131, 11149,
            11159, 11161, 11171, 11173, 11177, 11197, 11213, 11239, 11243, 11251,
            11257, 11261, 11273, 11279, 11287, 11299, 11311, 11317, 11321, 11329,
            11351, 11353, 11369, 11383, 11393, 11399, 11411, 11423, 11437, 11443,
            11447, 11467, 11471, 11483, 11489, 11491, 11497, 11503, 11519, 11527,
            11549, 11551, 11579, 11587, 11593, 11597, 11617, 11621, 11633, 11657,
            11677, 11681, 11689, 11699, 11701, 11717, 11719, 11731, 11743, 11777,
            11779, 11783, 11789, 11801, 11807, 11813, 11821, 11827, 11831, 11833,
            11839, 11863, 11867, 11887, 11897, 11903, 11909, 11923, 11927, 11933,
            11939, 11941, 11953, 11959, 11969, 11971, 11981, 11987, 12007, 12011,
            12037, 12041, 12043, 12049, 12071, 12073, 12097, 12101, 12107, 12109,
            12113, 12119, 12143, 12149, 12157, 12161, 12163, 12197, 12203, 12211,
            12227, 12239, 12241, 12251, 12253, 12263, 12269, 12277, 12281, 12289,
            12301, 12323, 12329, 12343, 12347, 12373, 12377, 12379, 12391, 12401,
            12409, 12413, 12421, 12433, 12437, 12451, 12457, 12473, 12479, 12487,
            12491, 12497, 12503, 12511, 12517, 12527, 12539, 12541, 12547, 12553,
            12569, 12577, 12583, 12589, 12601, 12611, 12613, 12619, 12637, 12641,
            12647, 12653, 12659, 12671, 12689, 12697, 12703, 12713, 12721, 12739,
            12743, 12757, 12763, 12781, 12791, 12799, 12809, 12821, 12823, 12829,
            12841, 12853, 12889, 12893, 12899, 12907, 12911, 12917, 12919, 12923,
            12941, 12953, 12959, 12967, 12973, 12979, 12983, 13001, 13003, 13007,
            13009, 13033, 13037, 13043, 13049, 13063, 13093, 13099, 13103, 13109,
            13121, 13127, 13147, 13151, 13159, 13163, 13171, 13177, 13183, 13187,
            13217, 13219, 13229, 13241, 13249, 13259, 13267, 13291, 13297, 13309,
            13313, 13327, 13331, 13337, 13339, 13367, 13381, 13397, 13399, 13411,
            13417, 13421, 13441, 13451, 13457, 13463, 13469, 13477, 13487, 13499,
            13513, 13523, 13537, 13553, 13567, 13577, 13591, 13597, 13613, 13619,
            13627, 13633, 13649, 13669, 13679, 13681, 13687, 13691, 13693, 13697,
            13709, 13711, 13721, 13723, 13729, 13751, 13757, 13759, 13763, 13781,
            13789, 13799, 13807, 13829, 13831, 13841, 13859, 13873, 13877, 13879,
            13883, 13901, 13903, 13907, 13913, 13921, 13931, 13933, 13963, 13967,
            13997, 13999, 14009, 14011, 14029, 14033, 14051, 14057, 14071, 14081,
            14083, 14087, 14107, 14143, 14149, 14153, 14159, 14173, 14177, 14197,
            14207, 14221, 14243, 14249, 14251, 14281, 14293, 14303, 14321, 14323,
            14327, 14341, 14347, 14369, 14387, 14389, 14401, 14407, 14411, 14419,
            14423, 14431, 14437, 14447, 14449, 14461, 14479, 14489, 14503, 14519,
            14533, 14537, 14543, 14549, 14551, 14557, 14561, 14563, 14591, 14593,
            14621, 14627, 14629, 14633, 14639, 14653, 14657, 14669, 14683, 14699,
            14713, 14717, 14723, 14731, 14737, 14741, 14747, 14753, 14759, 14767,
            14771, 14779, 14783, 14797, 14813, 14821, 14827, 14831, 14843, 14851,
            14867, 14869, 14879, 14887, 14891, 14897, 14923, 14929, 14939, 14947,
            14951, 14957, 14969, 14983, 15013, 15017, 15031, 15053, 15061, 15073,
            15077, 15083, 15091, 15101, 15107, 15121, 15131, 15137, 15139, 15149,
            15161, 15173, 15187, 15193, 15199, 15217, 15227, 15233, 15241, 15259,
            15263, 15269, 15271, 15277, 15287, 15289, 15299, 15307, 15313, 15319,
            15329, 15331, 15349, 15359, 15361, 15373, 15377, 15383, 15391, 15401,
            15413, 15427, 15439, 15443, 15451, 15461, 15467, 15473, 15493, 15497,
            15511, 15527, 15541, 15551, 15559, 15569, 15581, 15583, 15601, 15607,
            15619, 15629, 15641, 15643, 15647, 15649, 15661, 15667, 15671, 15679,
            15683, 15727, 15731, 15733, 15737, 15739, 15749, 15761, 15767, 15773,
            15787, 15791, 15797, 15803, 15809, 15817, 15823, 15859, 15877, 15881,
            15887, 15889, 15901, 15907, 15913, 15919, 15923, 15937, 15959, 15971,
            15973, 15991, 16001, 16007, 16033, 16057, 16061, 16063, 16067, 16069,
            16073, 16087, 16091, 16097, 16103, 16111, 16127, 16139, 16141, 16183,
            16187, 16189, 16193, 16217, 16223, 16229, 16231, 16249, 16253, 16267,
            16273, 16301, 16319, 16333, 16339, 16349, 16361, 16363, 16369, 16381,
            16411, 16417, 16421, 16427, 16433, 16447, 16451, 16453, 16477, 16481,
            16487, 16493, 16519, 16529, 16547, 16553, 16561, 16567, 16573, 16603,
            16607, 16619, 16631, 16633, 16649, 16651, 16657, 16661, 16673, 16691,
            16693, 16699, 16703, 16729, 16741, 16747, 16759, 16763, 16787, 16811,
            16823, 16829, 16831, 16843, 16871, 16879, 16883, 16889, 16901, 16903,
            16921, 16927, 16931, 16937, 16943, 16963, 16979, 16981, 16987, 16993,
            17011, 17021, 17027, 17029, 17033, 17041, 17047, 17053, 17077, 17093,
            17099, 17107, 17117, 17123, 17137, 17159, 17167, 17183, 17189, 17191,
            17203, 17207, 17209, 17231, 17239, 17257, 17291, 17293, 17299, 17317,
            17321, 17327, 17333, 17341, 17351, 17359, 17377, 17383, 17387, 17389,
            17393, 17401, 17417, 17419, 17431, 17443, 17449, 17467, 17471, 17477,
            17483, 17489, 17491, 17497, 17509, 17519, 17539, 17551, 17569, 17573,
            17579, 17581, 17597, 17599, 17609, 17623, 17627, 17657, 17659, 17669,
            17681, 17683, 17707, 17713, 17729, 17737, 17747, 17749, 17761, 17783,
            17789, 17791, 17807, 17827, 17837, 17839, 17851, 17863, 17881, 17891,
            17903, 17909, 17911, 17921, 17923, 17929, 17939, 17957, 17959, 17971,
            17977, 17981, 17987, 17989, 18013, 18041, 18043, 18047, 18049, 18059,
            18061, 18077, 18089, 18097, 18119, 18121, 18127, 18131, 18133, 18143,
            18149, 18169, 18181, 18191, 18199, 18211, 18217, 18223, 18229, 18233,
            18251, 18253, 18257, 18269, 18287, 18289, 18301, 18307, 18311, 18313,
            18329, 18341, 18353, 18367, 18371, 18379, 18397, 18401, 18413, 18427,
            18433, 18439, 18443, 18451, 18457, 18461, 18481, 18493, 18503, 18517,
            18521, 18523, 18539, 18541, 18553, 18583, 18587, 18593, 18617, 18637,
            18661, 18671, 18679, 18691, 18701, 18713, 18719, 18731, 18743, 18749,
            18757, 18773, 18787, 18793, 18797, 18803, 18839, 18859, 18869, 18899,
            18911, 18913, 18917, 18919, 18947, 18959, 18973, 18979, 19001, 19009,
            19013, 19031, 19037, 19051, 19069, 19073, 19079, 19081, 19087, 19121,
            19139, 19141, 19157, 19163, 19181, 19183, 19207, 19211, 19213, 19219,
            19231, 19237, 19249, 19259, 19267, 19273, 19289, 19301, 19309, 19319,
            19333, 19373, 19379, 19381, 19387, 19391, 19403, 19417, 19421, 19423,
            19427, 19429, 19433, 19441, 19447, 19457, 19463, 19469, 19471, 19477,
            19483, 19489, 19501, 19507, 19531, 19541, 19543, 19553, 19559, 19571,
            19577, 19583, 19597, 19603, 19609, 19661, 19681, 19687, 19697, 19699,
            19709, 19717, 19727, 19739, 19751, 19753, 19759, 19763, 19777, 19793,
            19801, 19813, 19819, 19841, 19843, 19853, 19861, 19867, 19889, 19891,
            19913, 19919, 19927, 19937, 19949, 19961, 19963, 19973, 19979, 19991,
            19993, 19997, 20011, 20021, 20023, 20029, 20047, 20051, 20063, 20071,
            20089, 20101, 20107, 20113, 20117, 20123, 20129, 20143, 20147, 20149,
            20161, 20173, 20177, 20183, 20201, 20219, 20231, 20233, 20249, 20261,
            20269, 20287, 20297, 20323, 20327, 20333, 20341, 20347, 20353, 20357,
            20359, 20369, 20389, 20393, 20399, 20407, 20411, 20431, 20441, 20443,
            20477, 20479, 20483, 20507, 20509, 20521, 20533, 20543, 20549, 20551,
            20563, 20593, 20599, 20611, 20627, 20639, 20641, 20663, 20681, 20693,
            20707, 20717, 20719, 20731, 20743, 20747, 20749, 20753, 20759, 20771,
            20773, 20789, 20807, 20809, 20849, 20857, 20873, 20879, 20887, 20897,
            20899, 20903, 20921, 20929, 20939, 20947, 20959, 20963, 20981, 20983,
            21001, 21011, 21013, 21017, 21019, 21023, 21031, 21059, 21061, 21067,
            21089, 21101, 21107, 21121, 21139, 21143, 21149, 21157, 21163, 21169,
            21179, 21187, 21191, 21193, 21211, 21221, 21227, 21247, 21269, 21277,
            21283, 21313, 21317, 21319, 21323, 21341, 21347, 21377, 21379, 21383,
            21391, 21397, 21401, 21407, 21419, 21433, 21467, 21481, 21487, 21491,
            21493, 21499, 21503, 21517, 21521, 21523, 21529, 21557, 21559, 21563,
            21569, 21577, 21587, 21589, 21599, 21601, 21611, 21613, 21617, 21647,
            21649, 21661, 21673, 21683, 21701, 21713, 21727, 21737, 21739, 21751,
            21757, 21767, 21773, 21787, 21799, 21803, 21817, 21821, 21839, 21841,
            21851, 21859, 21863, 21871, 21881, 21893, 21911, 21929, 21937, 21943,
            21961, 21977, 21991, 21997, 22003, 22013, 22027, 22031, 22037, 22039,
            22051, 22063, 22067, 22073, 22079, 22091, 22093, 22109, 22111, 22123,
            22129, 22133, 22147, 22153, 22157, 22159, 22171, 22189, 22193, 22229,
            22247, 22259, 22271, 22273, 22277, 22279, 22283, 22291, 22303, 22307,
            22343, 22349, 22367, 22369, 22381, 22391, 22397, 22409, 22433, 22441,
            22447, 22453, 22469, 22481, 22483, 22501, 22511, 22531, 22541, 22543,
            22549, 22567, 22571, 22573, 22613, 22619, 22621, 22637, 22639, 22643,
            22651, 22669, 22679, 22691, 22697, 22699, 22709, 22717, 22721, 22727,
            22739, 22741, 22751, 22769, 22777, 22783, 22787, 22807, 22811, 22817,
            22853, 22859, 22861, 22871, 22877, 22901, 22907, 22921, 22937, 22943,
            22961, 22963, 22973, 22993, 23003, 23011, 23017, 23021, 23027, 23029,
            23039, 23041, 23053, 23057, 23059, 23063, 23071, 23081, 23087, 23099,
            23117, 23131, 23143, 23159, 23167, 23173, 23189, 23197, 23201, 23203,
            23209, 23227, 23251, 23269, 23279, 23291, 23293, 23297, 23311, 23321,
            23327, 23333, 23339, 23357, 23369, 23371, 23399, 23417, 23431, 23447,
            23459, 23473, 23497, 23509, 23531, 23537, 23539, 23549, 23557, 23561,
            23563, 23567, 23581, 23593, 23599, 23603, 23609, 23623, 23627, 23629,
            23633, 23663, 23669, 23671, 23677, 23687, 23689, 23719, 23741, 23743,
            23747, 23753, 23761, 23767, 23773, 23789, 23801, 23813, 23819, 23827,
            23831, 23833, 23857, 23869, 23873, 23879, 23887, 23893, 23899, 23909,
            23911, 23917, 23929, 23957, 23971, 23977, 23981, 23993, 24001, 24007,
            24019, 24023, 24029, 24043, 24049, 24061, 24071, 24077, 24083, 24091,
            24097, 24103, 24107, 24109, 24113, 24121, 24133, 24137, 24151, 24169,
            24179, 24181, 24197, 24203, 24223, 24229, 24239, 24247, 24251, 24281,
            24317, 24329, 24337, 24359, 24371, 24373, 24379, 24391, 24407, 24413,
            24419, 24421, 24439, 24443, 24469, 24473, 24481, 24499, 24509, 24517,
            24527, 24533, 24547, 24551, 24571, 24593, 24611, 24623, 24631, 24659,
            24671, 24677, 24683, 24691, 24697, 24709, 24733, 24749, 24763, 24767,
            24781, 24793, 24799, 24809, 24821, 24841, 24847, 24851, 24859, 24877,
            24889, 24907, 24917, 24919, 24923, 24943, 24953, 24967, 24971, 24977,
            24979, 24989, 25013, 25031, 25033, 25037, 25057, 25073, 25087, 25097,
            25111, 25117, 25121, 25127, 25147, 25153, 25163, 25169, 25171, 25183,
            25189, 25219, 25229, 25237, 25243, 25247, 25253, 25261, 25301, 25303,
            25307, 25309, 25321, 25339, 25343, 25349, 25357, 25367, 25373, 25391,
            25409, 25411, 25423, 25439, 25447, 25453, 25457, 25463, 25469, 25471,
            25523, 25537, 25541, 25561, 25577, 25579, 25583, 25589, 25601, 25603,
            25609, 25621, 25633, 25639, 25643, 25657, 25667, 25673, 25679, 25693,
            25703, 25717, 25733, 25741, 25747, 25759, 25763, 25771, 25793, 25799,
            25801, 25819, 25841, 25847, 25849, 25867, 25873, 25889, 25903, 25913,
            25919, 25931, 25933, 25939, 25943, 25951, 25969, 25981, 25997, 25999,
            26003, 26017, 26021, 26029, 26041, 26053, 26083, 26099, 26107, 26111,
            26113, 26119, 26141, 26153, 26161, 26171, 26177, 26183, 26189, 26203,
            26209, 26227, 26237, 26249, 26251, 26261, 26263, 26267, 26293, 26297,
            26309, 26317, 26321, 26339, 26347, 26357, 26371, 26387, 26393, 26399,
            26407, 26417, 26423, 26431, 26437, 26449, 26459, 26479, 26489, 26497,
            26501, 26513, 26539, 26557, 26561, 26573, 26591, 26597, 26627, 26633,
            26641, 26647, 26669, 26681, 26683, 26687, 26693, 26699, 26701, 26711,
            26713, 26717, 26723, 26729, 26731, 26737, 26759, 26777, 26783, 26801,
            26813, 26821, 26833, 26839, 26849, 26861, 26863, 26879, 26881, 26891,
            26893, 26903, 26921, 26927, 26947, 26951, 26953, 26959, 26981, 26987,
            26993, 27011, 27017, 27031, 27043, 27059, 27061, 27067, 27073, 27077,
            27091, 27103, 27107, 27109, 27127, 27143, 27179, 27191, 27197, 27211,
            27239, 27241, 27253, 27259, 27271, 27277, 27281, 27283, 27299, 27329,
            27337, 27361, 27367, 27397, 27407, 27409, 27427, 27431, 27437, 27449,
            27457, 27479, 27481, 27487, 27509, 27527, 27529, 27539, 27541, 27551,
            27581, 27583, 27611, 27617, 27631, 27647, 27653, 27673, 27689, 27691,
            27697, 27701, 27733, 27737, 27739, 27743, 27749, 27751, 27763, 27767,
            27773, 27779, 27791, 27793, 27799, 27803, 27809, 27817, 27823, 27827,
            27847, 27851, 27883, 27893, 27901, 27917, 27919, 27941, 27943, 27947,
            27953, 27961, 27967, 27983, 27997, 28001, 28019, 28027, 28031, 28051,
            28057, 28069, 28081, 28087, 28097, 28099, 28109, 28111, 28123, 28151,
            28163, 28181, 28183, 28201, 28211, 28219, 28229, 28277, 28279, 28283,
            28289, 28297, 28307, 28309, 28319, 28349, 28351, 28387, 28393, 28403,
            28409, 28411, 28429, 28433, 28439, 28447, 28463, 28477, 28493, 28499,
            28513, 28517, 28537, 28541, 28547, 28549, 28559, 28571, 28573, 28579,
            28591, 28597, 28603, 28607, 28619, 28621, 28627, 28631, 28643, 28649,
            28657, 28661, 28663, 28669, 28687, 28697, 28703, 28711, 28723, 28729,
            28751, 28753, 28759, 28771, 28789, 28793, 28807, 28813, 28817, 28837,
            28843, 28859, 28867, 28871, 28879, 28901, 28909, 28921, 28927, 28933,
            28949, 28961, 28979, 29009, 29017, 29021, 29023, 29027, 29033, 29059,
            29063, 29077, 29101, 29123, 29129, 29131, 29137, 29147, 29153, 29167,
            29173, 29179, 29191, 29201, 29207, 29209, 29221, 29231, 29243, 29251,
            29269, 29287, 29297, 29303, 29311, 29327, 29333, 29339, 29347, 29363,
            29383, 29387, 29389, 29399, 29401, 29411, 29423, 29429, 29437, 29443,
            29453, 29473, 29483, 29501, 29527, 29531, 29537, 29567, 29569, 29573,
            29581, 29587, 29599, 29611, 29629, 29633, 29641, 29663, 29669, 29671,
            29683, 29717, 29723, 29741, 29753, 29759, 29761, 29789, 29803, 29819,
            29833, 29837, 29851, 29863, 29867, 29873, 29879, 29881, 29917, 29921,
            29927, 29947, 29959, 29983, 29989, 30011, 30013, 30029, 30047, 30059,
            30071, 30089, 30091, 30097, 30103, 30109, 30113, 30119, 30133, 30137,
            30139, 30161, 30169, 30181, 30187, 30197, 30203, 30211, 30223, 30241,
            30253, 30259, 30269, 30271, 30293, 30307, 30313, 30319, 30323, 30341,
            30347, 30367, 30389, 30391, 30403, 30427, 30431, 30449, 30467, 30469,
            30491, 30493, 30497, 30509, 30517, 30529, 30539, 30553, 30557, 30559,
            30577, 30593, 30631, 30637, 30643, 30649, 30661, 30671, 30677, 30689,
            30697, 30703, 30707, 30713, 30727, 30757, 30763, 30773, 30781, 30803,
            30809, 30817, 30829, 30839, 30841, 30851, 30853, 30859, 30869, 30871,
            30881, 30893, 30911, 30931, 30937, 30941, 30949, 30971, 30977, 30983,
            31013, 31019, 31033, 31039, 31051, 31063, 31069, 31079, 31081, 31091,
            31121, 31123, 31139, 31147, 31151, 31153, 31159, 31177, 31181, 31183,
            31189, 31193, 31219, 31223, 31231, 31237, 31247, 31249, 31253, 31259,
            31267, 31271, 31277, 31307, 31319, 31321, 31327, 31333, 31337, 31357,
            31379, 31387, 31391, 31393, 31397, 31469, 31477, 31481, 31489, 31511,
            31513, 31517, 31531, 31541, 31543, 31547, 31567, 31573, 31583, 31601,
            31607, 31627, 31643, 31649, 31657, 31663, 31667, 31687, 31699, 31721,
            31723, 31727, 31729, 31741, 31751, 31769, 31771, 31793, 31799, 31817,
            31847, 31849, 31859, 31873, 31883, 31891, 31907, 31957, 31963, 31973,
            31981, 31991, 32003, 32009, 32027, 32029, 32051, 32057, 32059, 32063,
            32069, 32077, 32083, 32089, 32099, 32117, 32119, 32141, 32143, 32159,
            32173, 32183, 32189, 32191, 32203, 32213, 32233, 32237, 32251, 32257,
            32261, 32297, 32299, 32303, 32309, 32321, 32323, 32327, 32341, 32353,
            32359, 32363, 32369, 32371, 32377, 32381, 32401, 32411, 32413, 32423,
            32429, 32441, 32443, 32467, 32479, 32491, 32497, 32503, 32507, 32531,
            32533, 32537, 32561, 32563, 32569, 32573, 32579, 32587, 32603, 32609,
            32611, 32621, 32633, 32647, 32653, 32687, 32693, 32707, 32713, 32717,
            32719, 32749, 32771, 32779, 32783, 32789, 32797, 32801, 32803, 32831,
            32833, 32839, 32843, 32869, 32887, 32909, 32911, 32917, 32933, 32939,
            32941, 32957, 32969, 32971, 32983, 32987, 32993, 32999, 33013, 33023,
            33029, 33037, 33049, 33053, 33071, 33073, 33083, 33091, 33107, 33113,
            33119, 33149, 33151, 33161, 33179, 33181, 33191, 33199, 33203, 33211,
            33223, 33247, 33287, 33289, 33301, 33311, 33317, 33329, 33331, 33343,
            33347, 33349, 33353, 33359, 33377, 33391, 33403, 33409, 33413, 33427,
            33457, 33461, 33469, 33479, 33487, 33493, 33503, 33521, 33529, 33533,
            33547, 33563, 33569, 33577, 33581, 33587, 33589, 33599, 33601, 33613,
            33617, 33619, 33623, 33629, 33637, 33641, 33647, 33679, 33703, 33713,
            33721, 33739, 33749, 33751, 33757, 33767, 33769, 33773, 33791, 33797,
            33809, 33811, 33827, 33829, 33851, 33857, 33863, 33871, 33889, 33893,
            33911, 33923, 33931, 33937, 33941, 33961, 33967, 33997, 34019, 34031,
            34033, 34039, 34057, 34061, 34123, 34127, 34129, 34141, 34147, 34157,
            34159, 34171, 34183, 34211, 34213, 34217, 34231, 34253, 34259, 34261,
            34267, 34273, 34283, 34297, 34301, 34303, 34313, 34319, 34327, 34337,
            34351, 34361, 34367, 34369, 34381, 34403, 34421, 34429, 34439, 34457,
            34469, 34471, 34483, 34487, 34499, 34501, 34511, 34513, 34519, 34537,
            34543, 34549, 34583, 34589, 34591, 34603, 34607, 34613, 34631, 34649,
            34651, 34667, 34673, 34679, 34687, 34693, 34703, 34721, 34729, 34739,
            34747, 34757, 34759, 34763, 34781, 34807, 34819, 34841, 34843, 34847,
            34849, 34871, 34877, 34883, 34897, 34913, 34919, 34939, 34949, 34961,
            34963, 34981, 35023, 35027, 35051, 35053, 35059, 35069, 35081, 35083,
            35089, 35099, 35107, 35111, 35117, 35129, 35141, 35149, 35153, 35159,
            35171, 35201, 35221, 35227, 35251, 35257, 35267, 35279, 35281, 35291,
            35311, 35317, 35323, 35327, 35339, 35353, 35363, 35381, 35393, 35401,
            35407, 35419, 35423, 35437, 35447, 35449, 35461, 35491, 35507, 35509,
            35521, 35527, 35531, 35533, 35537, 35543, 35569, 35573, 35591, 35593,
            35597, 35603, 35617, 35671, 35677, 35729, 35731, 35747, 35753, 35759,
            35771, 35797, 35801, 35803, 35809, 35831, 35837, 35839, 35851, 35863,
            35869, 35879, 35897, 35899, 35911, 35923, 35933, 35951, 35963, 35969,
            35977, 35983, 35993, 35999, 36007, 36011, 36013, 36017, 36037, 36061,
            36067, 36073, 36083, 36097, 36107, 36109, 36131, 36137, 36151, 36161,
            36187, 36191, 36209, 36217, 36229, 36241, 36251, 36263, 36269, 36277,
            36293, 36299, 36307, 36313, 36319, 36341, 36343, 36353, 36373, 36383,
            36389, 36433, 36451, 36457, 36467, 36469, 36473, 36479, 36493, 36497,
            36523, 36527, 36529, 36541, 36551, 36559, 36563, 36571, 36583, 36587,
            36599, 36607, 36629, 36637, 36643, 36653, 36671, 36677, 36683, 36691,
            36697, 36709, 36713, 36721, 36739, 36749, 36761, 36767, 36779, 36781,
            36787, 36791, 36793, 36809, 36821, 36833, 36847, 36857, 36871, 36877,
            36887, 36899, 36901, 36913, 36919, 36923, 36929, 36931, 36943, 36947,
            36973, 36979, 36997, 37003, 37013, 37019, 37021, 37039, 37049, 37057,
            37061, 37087, 37097, 37117, 37123, 37139, 37159, 37171, 37181, 37189,
            37199, 37201, 37217, 37223, 37243, 37253, 37273, 37277, 37307, 37309,
            37313, 37321, 37337, 37339, 37357, 37361, 37363, 37369, 37379, 37397,
            37409, 37423, 37441, 37447, 37463, 37483, 37489, 37493, 37501, 37507,
            37511, 37517, 37529, 37537, 37547, 37549, 37561, 37567, 37571, 37573,
            37579, 37589, 37591, 37607, 37619, 37633, 37643, 37649, 37657, 37663,
            37691, 37693, 37699, 37717, 37747, 37781, 37783, 37799, 37811, 37813,
            37831, 37847, 37853, 37861, 37871, 37879, 37889, 37897, 37907, 37951,
            37957, 37963, 37967, 37987, 37991, 37993, 37997, 38011, 38039, 38047,
            38053, 38069, 38083, 38113, 38119, 38149, 38153, 38167, 38177, 38183,
            38189, 38197, 38201, 38219, 38231, 38237, 38239, 38261, 38273, 38281,
            38287, 38299, 38303, 38317, 38321, 38327, 38329, 38333, 38351, 38371,
            38377, 38393, 38431, 38447, 38449, 38453, 38459, 38461, 38501, 38543,
            38557, 38561, 38567, 38569, 38593, 38603, 38609, 38611, 38629, 38639,
            38651, 38653, 38669, 38671, 38677, 38693, 38699, 38707, 38711, 38713,
            38723, 38729, 38737, 38747, 38749, 38767, 38783, 38791, 38803, 38821,
            38833, 38839, 38851, 38861, 38867, 38873, 38891, 38903, 38917, 38921,
            38923, 38933, 38953, 38959, 38971, 38977, 38993, 39019, 39023, 39041,
            39043, 39047, 39079, 39089, 39097, 39103, 39107, 39113, 39119, 39133,
            39139, 39157, 39161, 39163, 39181, 39191, 39199, 39209, 39217, 39227,
            39229, 39233, 39239, 39241, 39251, 39293, 39301, 39313, 39317, 39323,
            39341, 39343, 39359, 39367, 39371, 39373, 39383, 39397, 39409, 39419,
            39439, 39443, 39451, 39461, 39499, 39503, 39509, 39511, 39521, 39541,
            39551, 39563, 39569, 39581, 39607, 39619, 39623, 39631, 39659, 39667,
            39671, 39679, 39703, 39709, 39719, 39727, 39733, 39749, 39761, 39769,
            39779, 39791, 39799, 39821, 39827, 39829, 39839, 39841, 39847, 39857,
            39863, 39869, 39877, 39883, 39887, 39901, 39929, 39937, 39953, 39971,
            39979, 39983, 39989, 40009, 40013, 40031, 40037, 40039, 40063, 40087,
            40093, 40099, 40111, 40123, 40127, 40129, 40151, 40153, 40163, 40169,
            40177, 40189, 40193, 40213, 40231, 40237, 40241, 40253, 40277, 40283,
            40289, 40343, 40351, 40357, 40361, 40387, 40423, 40427, 40429, 40433,
            40459, 40471, 40483, 40487, 40493, 40499, 40507, 40519, 40529, 40531,
            40543, 40559, 40577, 40583, 40591, 40597, 40609, 40627, 40637, 40639,
            40693, 40697, 40699, 40709, 40739, 40751, 40759, 40763, 40771, 40787,
            40801, 40813, 40819, 40823, 40829, 40841, 40847, 40849, 40853, 40867,
            40879, 40883, 40897, 40903, 40927, 40933, 40939, 40949, 40961, 40973,
            40993, 41011, 41017, 41023, 41039, 41047, 41051, 41057, 41077, 41081,
            41113, 41117, 41131, 41141, 41143, 41149, 41161, 41177, 41179, 41183,
            41189, 41201, 41203, 41213, 41221, 41227, 41231, 41233, 41243, 41257,
            41263, 41269, 41281, 41299, 41333, 41341, 41351, 41357, 41381, 41387,
            41389, 41399, 41411, 41413, 41443, 41453, 41467, 41479, 41491, 41507,
            41513, 41519, 41521, 41539, 41543, 41549, 41579, 41593, 41597, 41603,
            41609, 41611, 41617, 41621, 41627, 41641, 41647, 41651, 41659, 41669,
            41681, 41687, 41719, 41729, 41737, 41759, 41761, 41771, 41777, 41801,
            41809, 41813, 41843, 41849, 41851, 41863, 41879, 41887, 41893, 41897,
            41903, 41911, 41927, 41941, 41947, 41953, 41957, 41959, 41969, 41981,
            41983, 41999, 42013, 42017, 42019, 42023, 42043, 42061, 42071, 42073,
            42083, 42089, 42101, 42131, 42139, 42157, 42169, 42179, 42181, 42187,
            42193, 42197, 42209, 42221, 42223, 42227, 42239, 42257, 42281, 42283,
            42293, 42299, 42307, 42323, 42331, 42337, 42349, 42359, 42373, 42379,
            42391, 42397, 42403, 42407, 42409, 42433, 42437, 42443, 42451, 42457,
            42461, 42463, 42467, 42473, 42487, 42491, 42499, 42509, 42533, 42557,
            42569, 42571, 42577, 42589, 42611, 42641, 42643, 42649, 42667, 42677,
            42683, 42689, 42697, 42701, 42703, 42709, 42719, 42727, 42737, 42743,
            42751, 42767, 42773, 42787, 42793, 42797, 42821, 42829, 42839, 42841,
            42853, 42859, 42863, 42899, 42901, 42923, 42929, 42937, 42943, 42953,
            42961, 42967, 42979, 42989, 43003, 43013, 43019, 43037, 43049, 43051,
            43063, 43067, 43093, 43103, 43117, 43133, 43151, 43159, 43177, 43189,
            43201, 43207, 43223, 43237, 43261, 43271, 43283, 43291, 43313, 43319,
            43321, 43331, 43391, 43397, 43399, 43403, 43411, 43427, 43441, 43451,
            43457, 43481, 43487, 43499, 43517, 43541, 43543, 43573, 43577, 43579,
            43591, 43597, 43607, 43609, 43613, 43627, 43633, 43649, 43651, 43661,
            43669, 43691, 43711, 43717, 43721, 43753, 43759, 43777, 43781, 43783,
            43787, 43789, 43793, 43801, 43853, 43867, 43889, 43891, 43913, 43933,
            43943, 43951, 43961, 43963, 43969, 43973, 43987, 43991, 43997, 44017,
            44021, 44027, 44029, 44041, 44053, 44059, 44071, 44087, 44089, 44101,
            44111, 44119, 44123, 44129, 44131, 44159, 44171, 44179, 44189, 44201,
            44203, 44207, 44221, 44249, 44257, 44263, 44267, 44269, 44273, 44279,
            44281, 44293, 44351, 44357, 44371, 44381, 44383, 44389, 44417, 44449,
            44453, 44483, 44491, 44497, 44501, 44507, 44519, 44531, 44533, 44537,
            44543, 44549, 44563, 44579, 44587, 44617, 44621, 44623, 44633, 44641,
            44647, 44651, 44657, 44683, 44687, 44699, 44701, 44711, 44729, 44741,
            44753, 44771, 44773, 44777, 44789, 44797, 44809, 44819, 44839, 44843,
            44851, 44867, 44879, 44887, 44893, 44909, 44917, 44927, 44939, 44953,
            44959, 44963, 44971, 44983, 44987, 45007, 45013, 45053, 45061, 45077,
            45083, 45119, 45121, 45127, 45131, 45137, 45139, 45161, 45179, 45181,
            45191, 45197, 45233, 45247, 45259, 45263, 45281, 45289, 45293, 45307,
            45317, 45319, 45329, 45337, 45341, 45343, 45361, 45377, 45389, 45403,
            45413, 45427, 45433, 45439, 45481, 45491, 45497, 45503, 45523, 45533,
            45541, 45553, 45557, 45569, 45587, 45589, 45599, 45613, 45631, 45641,
            45659, 45667, 45673, 45677, 45691, 45697, 45707, 45737, 45751, 45757,
            45763, 45767, 45779, 45817, 45821, 45823, 45827, 45833, 45841, 45853,
            45863, 45869, 45887, 45893, 45943, 45949, 45953, 45959, 45971, 45979,
            45989, 46021, 46027, 46049, 46051, 46061, 46073, 46091, 46093, 46099,
            46103, 46133, 46141, 46147, 46153, 46171, 46181, 46183, 46187, 46199,
            46219, 46229, 46237, 46261, 46271, 46273, 46279, 46301, 46307, 46309,
            46327, 46337, 46349, 46351, 46381, 46399, 46411, 46439, 46441, 46447,
            46451, 46457, 46471, 46477, 46489, 46499, 46507, 46511, 46523, 46549,
            46559, 46567, 46573, 46589, 46591, 46601, 46619, 46633, 46639, 46643,
            46649, 46663, 46679, 46681, 46687, 46691, 46703, 46723, 46727, 46747,
            46751, 46757, 46769, 46771, 46807, 46811, 46817, 46819, 46829, 46831,
            46853, 46861, 46867, 46877, 46889, 46901, 46919, 46933, 46957, 46993,
            46997, 47017, 47041, 47051, 47057, 47059, 47087, 47093, 47111, 47119,
            47123, 47129, 47137, 47143, 47147, 47149, 47161, 47189, 47207, 47221,
            47237, 47251, 47269, 47279, 47287, 47293, 47297, 47303, 47309, 47317,
            47339, 47351, 47353, 47363, 47381, 47387, 47389, 47407, 47417, 47419,
            47431, 47441, 47459, 47491, 47497, 47501, 47507, 47513, 47521, 47527,
            47533, 47543, 47563, 47569, 47581, 47591, 47599, 47609, 47623, 47629,
            47639, 47653, 47657, 47659, 47681, 47699, 47701, 47711, 47713, 47717,
            47737, 47741, 47743, 47777, 47779, 47791, 47797, 47807, 47809, 47819,
            47837, 47843, 47857, 47869, 47881, 47903, 47911, 47917, 47933, 47939,
            47947, 47951, 47963, 47969, 47977, 47981, 48017, 48023, 48029, 48049,
            48073, 48079, 48091, 48109, 48119, 48121, 48131, 48157, 48163, 48179,
            48187, 48193, 48197, 48221, 48239, 48247, 48259, 48271, 48281, 48299,
            48311, 48313, 48337, 48341, 48353, 48371, 48383, 48397, 48407, 48409,
            48413, 48437, 48449, 48463, 48473, 48479, 48481, 48487, 48491, 48497,
            48523, 48527, 48533, 48539, 48541, 48563, 48571, 48589, 48593, 48611,
            48619, 48623, 48647, 48649, 48661, 48673, 48677, 48679, 48731, 48733,
            48751, 48757, 48761, 48767, 48779, 48781, 48787, 48799, 48809, 48817,
            48821, 48823, 48847, 48857, 48859, 48869, 48871, 48883, 48889, 48907,
            48947, 48953, 48973, 48989, 48991, 49003, 49009, 49019, 49031, 49033,
            49037, 49043, 49057, 49069, 49081, 49103, 49109, 49117, 49121, 49123,
            49139, 49157, 49169, 49171, 49177, 49193, 49199, 49201, 49207, 49211,
            49223, 49253, 49261, 49277, 49279, 49297, 49307, 49331, 49333, 49339,
            49363, 49367, 49369, 49391, 49393, 49409, 49411, 49417, 49429, 49433,
            49451, 49459, 49463, 49477, 49481, 49499, 49523, 49529, 49531, 49537,
            49547, 49549, 49559, 49597, 49603, 49613, 49627, 49633, 49639, 49663,
            49667, 49669, 49681, 49697, 49711, 49727, 49739, 49741, 49747, 49757,
            49783, 49787, 49789, 49801, 49807, 49811, 49823, 49831, 49843, 49853,
            49871, 49877, 49891, 49919, 49921, 49927, 49937, 49939, 49943, 49957,
            49991, 49993, 49999,
        };
        try {
            SMALL_PRIMES = new BigInteger[sp.length];
            for (int i = 0; i < sp.length; i++)
                SMALL_PRIMES[i] = BigInteger.valueOf(sp[i]);

            PRIME_BITMAP = new byte[(PRIME_BITMAP_SIZE+15)/16];
            for (int i = 1; i <= 5; i++) {
                int p = sp[i]; // 3, 5, 7, 11 or 13
                for (int j = p; j < PRIME_BITMAP_SIZE; j += p+p) {
                    int x = j >> 1;
                    PRIME_BITMAP[x >> 3] |= (1 << (x & 0x07));
                }
            }

            BigInteger x = ONE;
            for (int i = 0; i < SMALL_PRIME_THRESHOLD; i++)
                x = x.multiply(SMALL_PRIMES[i]);

            SMALL_PRIME_MOD = x;
if (DEBUG && debuglevel >= 8) debug("SMALL_PRIME_MOD = " + x.toString(16));

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
