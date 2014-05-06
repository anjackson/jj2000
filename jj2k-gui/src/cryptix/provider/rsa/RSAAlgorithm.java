// $Id: RSAAlgorithm.java,v 1.1.1.1 2002/08/27 12:32:13 grosbois Exp $
//
// $Log: RSAAlgorithm.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:13  grosbois
// Add cryptix 3.2
//
// Revision 1.7  1998/01/03 22:57:18  raif
// *** empty log message ***
//
// Revision 1.6.1  1998/01/04  raif
// + modified the rsa(6) method to first check if u = (1/q) mod p; if it
//   isn't exchange p and q before using the CRT algorithm. This is needed
//   for rsa factors not generated/set by cryptix.provider.rsa classes; eg.
//   PGP applications.
//
// Revision 1.6  1997/12/10 06:38:40  raif
// *** empty log message ***
//
// 1997.12.10 --RSN:
// + made it public final and made its methods public.
//
// Revision 1.5  1997/11/23 03:09:18  hopwood
// + Mostly documentation changes.
//
// Revision 1.4  1997/11/20 19:46:57  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.1  1997/11/16  David Hopwood
// + Minor documentation changes.
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
// Revision 0.1.0.1  1997/08/23  David Hopwood
// + Now u = q^-1 (mod p), not p^-1 (mod q). Apart from PGP, this is
//   more commonly used (e.g. see the P1363 draft).
// + Made the documentation consistent with u = q^-1 mod p.
//
// Revision 0.1.0.0  1997/08/20  David Hopwood
// + Original version, based on Raif Naffah's RSAWithMdSignature.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.rsa;

import cryptix.util.core.Debug;

import java.io.PrintWriter;
import java.math.BigInteger;

/**
 * A class that calculates the RSA algorithm. A single method is
 * used for encryption, decryption, signing and verification:
 * <ul>
 *   <li> for encryption and verification, the public exponent, <i>e</i>,
 *        should be given.
 *   <li> for decryption and signing, the private exponent, <i>d</i>,
 *        should be given.
 * </ul>
 * <p>
 * The purpose of having this as a separate class is to avoid duplication
 * between the RSA Cipher and Signature implementations.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> Donald E. Knuth,
 *        <cite>The Art of Computer Programming</cite>,
 *        ISBN 0-201-03822-6 (v.2) pages 270-274.
 *        <p>
 *   <li> <cite>ANS X9.31, Appendix B</cite>.
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
public final class RSAAlgorithm
{
    private RSAAlgorithm() {} // static methods only

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "RSAAlgorithm") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("RSAAlgorithm: " + s); }


// Constants
//...........................................................................

    private static final BigInteger ONE = BigInteger.valueOf(1L);


// Own methods
//...........................................................................

    /**
     * Computes the RSA algorithm. If <i>p</i> is null, straightforward
     * modular exponentiation is used.
     * <p>
     * Otherwise, this method uses the Chinese Remainder Theorem (CRT) to
     * compute the result given the known factorisation of the public
     * modulus <i>n</i> into two relatively prime factors <i>p</i> and <i>q</i>.
     * The arithmetic behind this method is detailed in [1] and [2].
     * <p>
     * The comments that follow, which are edited from the PGP
     * <samp>mpilib.c</samp> file <em>with p and q reversed</em>, make
     * the practical algorithmic implementation clearer:
     * <p>
     * <blockquote>
     *     Y = X**d (mod n) = X**d (mod pq)
     * </blockquote>
     * <p>
     * We form this by evaluating:
     * <blockquote>
     *     p2 = plain**d (mod p) and <br>
     *     q2 = plain**d (mod q)
     * </blockquote>
     * and then combining the two by the CRT.
     * <p>
     * Two optimisations of this are possible. First, we reduce X modulo p
     * and q before starting, since:
     * <blockquote>
     *    x**a (mod b) = (x (mod b))**a (mod b)
     * </blockquote>
     * <p>
     * Second, since we know the factorisation of p and q (trivially derived
     * from the factorisation of n = pq), and input is relatively prime to
     * both p and q, we can use Euler's theorem:
     * <blockquote>
     *     X**phi(m) = 1 (mod m),
     * </blockquote>
     * to throw away multiples of phi(p) or phi(q) in d. Letting
     * <blockquote>
     *     ep = d (mod phi(p)) and <br>
     *     eq = d (mod phi(q))
     * </blockquote>
     * then combining these two speedups, we only need to evaluate:
     * <blockquote>
     *     p2 = (X mod p)**ep (mod p) and <br>
     *     q2 = (X mod q)**eq (mod q).
     * </blockquote>
     * <p>
     * Now we need to apply the CRT. Starting with:
     * <blockquote>
     *     Y = p2 (mod p) and <br>
     *     Y = q2 (mod q)
     * </blockquote>
     * we can say that:
     * <blockquote>
     *     Y = q2 + kq
     * </blockquote>
     * and if we assume that:
     * <blockquote>
     *     0 <= q2 < q, then <br>
     *     0 <= Y < pq for some 0 <= k < p
     * </blockquote>
     * <p>
     * Since we want:
     * <blockquote>
     *     Y = p2 (mod p),
     * </blockquote>
     * then
     * <blockquote>
     *     kq = (p2 - q2) (mod q)
     * <blockquote>
     * <p>
     * Since p and q are relatively prime, q has a multiplicative inverse
     * u mod p. In other words, uq = 1 (mod p).
     * <p>
     * Multiplying by u on both sides gives:
     * <blockquote>
     *     k = u * (p2 - q2) (mod p)
     * </blockquote>
     * <p>
     * Once we have k, evaluating kq + q2 is trivial, and that gives
     * us the result.
     *
     * @param  X    the BigInteger to be used as input.
     * @param  n    the public modulus.
     * @param  exp  the exponent (e for encryption and verification,
     *              d for decryption and signing).
     * @param  p    the first factor of the public modulus.
     * @param  q    the second factor of the public modulus.
     * @param  u    the multiplicative inverse of q modulo p.
     * @return the result of the computation.
     */
    public static BigInteger rsa(BigInteger X, BigInteger n, BigInteger exp,
                          BigInteger p, BigInteger q, BigInteger u) {
        if (p != null) {
            //
            // Factors are known.
            // First check if u = (1/q) mod p; if not exchange p and q
            // before using CRT. This is needed for factors not generated/set
            // by cryptix.provider.rsa classes; eg. PGP applications.
            //
            if (! u.equals(q.modInverse(p))) {
                BigInteger t = q;
                q = p;
                p = t;
            }
            //
            // Factors are known and usable by our CRT code.
            //
//            // Was "X.mod(p).modPow(...)", but BigInteger.modPow already
//            // calculates X.mod(p).
//            BigInteger p2 = X.modPow(exp.mod(p.subtract(ONE)), p);
//            BigInteger q2 = X.modPow(exp.mod(q.subtract(ONE)), q);
            BigInteger p2 = X.mod(p).modPow(exp.mod(p.subtract(ONE)), p);
            BigInteger q2 = X.mod(q).modPow(exp.mod(q.subtract(ONE)), q);

            // "if (p2.compareTo(q2) == 0) return q2;" removed because it
            // is redundant.
            if (p2.equals(q2)) return q2;
            BigInteger k = (p2.subtract(q2).mod(p)).multiply(u).mod(p);
            return q.multiply(k).add(q2);
        } else {
            // Slower method.
            return X.modPow(exp, n);
        }
    }

    /**
     * Computes the RSA algorithm, without using the Chinese Remainder
     * Theorem.
     *
     * @param  X    the BigInteger to be used as input.
     * @param  n    the public modulus.
     * @param  exp  the exponent (e for encryption and verification,
     *              d for decryption and signing).
     * @return the result of the computation.
     */
    public static BigInteger rsa(BigInteger X, BigInteger n, BigInteger exp) {
        return X.modPow(exp, n);
    }
}
