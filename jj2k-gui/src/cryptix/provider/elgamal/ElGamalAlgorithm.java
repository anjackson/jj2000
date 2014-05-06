// $Id: ElGamalAlgorithm.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: ElGamalAlgorithm.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.2  1997/12/10 06:39:51  raif
// *** empty log message ***
//
// 1997.12.10 --RSN:
// + made it public final and its methods public.
//
// Revision 1.1  1997/12/07 06:37:26  hopwood
// + Major overhaul of ElGamal to match RSA.
//
// Revision 0.1.0  1997/11/22  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.elgamal;

import cryptix.CryptixException;
import cryptix.util.core.Debug;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Random;

/**
 * A class that calculates the ElGamal family of algorithms (encryption,
 * decryption, signing and verification).
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
public final class ElGamalAlgorithm
{
    private ElGamalAlgorithm() {} // static methods only

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("ElGamal", "ElGamalAlgorithm") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("ElGamalAlgorithm: " + s); }


// Constants
//...........................................................................

    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);


// Own methods
//...........................................................................

    /**
     * The encryption algorithm for ElGamal. The two parts of the ciphertext,
     * <i>a</i> and <i>b</i>, will be stored in <code>ab[0]</code> and
     * <code>ab[1]</code> respectively.
     *
     * @param  M    the plaintext.
     * @param  ab   a 2-element BigInteger array in which to store the ciphertext.
     * @param  p    the prime from the public key.
     * @param  g    the generator from the public key.
     * @param  y    the value of <i>y</i> from the public key.
     * @param  rng  a random number generator to be used for encryption.
     */
    public static void encrypt(BigInteger M, BigInteger[] ab,
                        BigInteger p, BigInteger g, BigInteger y,
                        Random rng) {
        BigInteger p_minus_1 = p.subtract(ONE);
        // choose a random k, relatively prime to p-1.
        BigInteger k;
        do {
            // values of k with the same number of bits as p won't be chosen, but
            // that shouldn't be a problem.
            k = new BigInteger(p.bitLength()-1, rng);
            if (!(k.testBit(0)))
                k = k.setBit(0); // make sure k is odd
        } while (!(k.gcd(p_minus_1).equals(ONE)));

        ab[0] /* a */ = g.modPow(k, p);
        ab[1] /* b */ = y.modPow(k, p).multiply(M).mod(p);
    }

    /**
     * The decryption algorithm for ElGamal. <i>a</i> and <i>b</i> are the
     * two parts of the ciphertext (using the same convention as given in
     * Schneier).
     *
     * @param  a    the first part of the ciphertext.
     * @param  b    the second part of the ciphertext.
     * @param  p    the prime from the private key.
     * @param  g    the generator from the private key.
     * @param  x    the value of <i>x</i> from the private key.
     * @return the plaintext.
     */
    public static BigInteger decrypt(BigInteger a, BigInteger b,
                              BigInteger p, BigInteger g, BigInteger x) {
        try {
            return b.multiply(a.modPow(x, p).modInverse(p)).mod(p);
        } catch (ArithmeticException e) {
            // Shouldn't happen, but might if the key was not generated properly,
            // or if initEncrypt or initDecrypt is called at the same time by
            // another thread.
            // SECURITY: can't include values of a, x and p in the detail message,
            // because x is secret, and a or p may be secret depending on the
            // application.
            throw new CryptixException("ElGamal: " + e.getClass().getName() +
                " while calculating a.modPow(x, p).modInverse(p) - maybe key was" +
                " not generated properly?");
        }
    }

    /**
     * The signature algorithm for ElGamal. <i>ab</i> should be a two-element
     * BigInteger array. The two parts of the signature, <i>a</i> and <i>b</i>,
     * will be stored in <code>ab[0]</code> and <code>ab[1]</code> respectively.
     *
     * @param  M    the value to be signed.
     * @param  ab   an array in which to store the result.
     * @param  p    the prime from the private key.
     * @param  g    the generator from the private key.
     * @param  x    the value of <i>x</i> from the private key.
     * @param  rng  a random number generator to be used for signing.
     * @return true iff the signature is valid.
     */
    public static void sign(BigInteger M, BigInteger[] ab,
                     BigInteger p, BigInteger g, BigInteger x,
                     Random rng) {
        BigInteger p_minus_1 = p.subtract(ONE);
        // choose a random k, relatively prime to p-1.
        BigInteger k;
        do {
            // values of k with the same number of bits as p won't be chosen, but
            // that shouldn't be a problem.
            k = new BigInteger(p.bitLength()-1, rng);
            if (!(k.testBit(0)))
                k = k.setBit(0); // make sure k is odd
        } while (!(k.gcd(p_minus_1).equals(ONE)));

        BigInteger a = g.modPow(k, p);
        ab[0] = a;

        // solve for b in the equation:
        //     M = (x.a + k.b) mod (p-1)
        // i.e.
        //     b = (inverse of k mod p-1).(M - x.a) mod (p-1)
        try {
            ab[1] /* b */ = k.modInverse(p_minus_1)
                             .multiply(M.subtract(x.multiply(a)).mod(p_minus_1))
                             .mod(p_minus_1);
        } catch (ArithmeticException e) {
            // Shouldn't happen (because modInverse is the only method that
            // throws ArithmeticException, k is relatively prime to p-1, and
            // p-1 > 0).
            //
            // SECURITY: can't include values of k and p-1 in the detail message,
            // because k is secret, and p may be secret depending on the application.

            throw new CryptixException(
                "ElGamal: ArithmeticException while calculating k.modInverse(p-1)");
        }
    }

    /**
     * The verification algorithm for ElGamal. Returns true iff the signature was
     * verified successfully.
     *
     * @param  M    the value that was signed.
     * @param  a    the first part of the signature.
     * @param  b    the second part of the signature.
     * @param  p    the prime from the public key.
     * @param  g    the generator from the public key.
     * @param  y    the value of <i>y</i> from the public key.
     * @return true iff the signature is valid.
     */
    public static boolean verify(BigInteger M, BigInteger a, BigInteger b,
                          BigInteger p, BigInteger g, BigInteger y) {
        BigInteger p_minus_1 = p.subtract(ONE);
        // sanity checks
        if (M.compareTo(ZERO) < 0 || M.compareTo(p_minus_1) >= 0 ||
            a.compareTo(ZERO) < 0 || a.compareTo(p_minus_1) >= 0 ||
            b.compareTo(ZERO) < 0 || b.compareTo(p_minus_1) >= 0)
            return false;

        // accept iff y^a.a^b = g^M (mod p)
        return y.modPow(a, p).multiply(a.modPow(b, p)).mod(p)
                .equals(g.modPow(M, p));
    }
}
