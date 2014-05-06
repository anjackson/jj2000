// $Id: BaseRSAPrivateKey.java,v 1.1.1.1 2002/08/27 12:32:13 grosbois Exp $
//
// $Log: BaseRSAPrivateKey.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:13  grosbois
// Add cryptix 3.2
//
// Revision 1.7  2000/08/17 11:41:00  edwin
// java.* -> xjava.*
//
// Revision 1.6  1999/07/12 20:34:21  edwin
// renaming java.security.interfaces.RSAPrivateKey and RSAPublicKey to CryptixRSAPrivateKey and CryptixRSAPublicKey. This is one more step to JDK1.2 compatibility.
//
// Revision 1.5  1997/11/23 03:09:18  hopwood
// + Mostly documentation changes.
//
// Revision 1.4.1  1997/11/22  hopwood
// + Swapped order of n and d parameters to setRsaParams, to be consistent
//   with BaseRSAPublicKey.
//
// Revision 1.4  1997/11/20 19:46:57  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3  1997/11/05 08:01:56  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/04 19:33:31  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.2  1997/08/27  David Hopwood
// + Misc. fixes.
//
// Revision 0.1.0.1  1997/08/23  David Hopwood
// + Now u = q^-1 (mod p), not p^-1 (mod q). Apart from PGP, this is
//   more commonly used (e.g. see the P1363 draft).
// + Added check that uq = 1 (mod p). If this check fails, a debugging
//   message is printed, and the given value of u is ignored. This is
//   worthwhile because uq (mod p) is much faster to calculate than
//   q^-1 (mod p), and it makes the code robust against errors where
//   p and q are incorrectly swapped.
//
// Revision 0.1.0.0  1997/07/23  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.rsa;

import cryptix.util.core.Debug;
import cryptix.util.core.BI;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.RSAFactors;

/**
 * An abstract class representing an RSA private key.
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
public abstract class BaseRSAPrivateKey
implements CryptixRSAPrivateKey, RSAFactors
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "BaseRSAPrivateKey") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("BaseRSAPrivateKey: " + s); }


// Variables
//...........................................................................

    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE =  BigInteger.valueOf(1L);

    /**
     * Public decryption modulus. It is the product of the two <i>p</i>
     * and <i>q</i> factors.
     */
    private BigInteger n;

    /**
     * Private encryption exponent. Traditionally referred to as <i>d</i>.
     */
    private BigInteger d;

    /**
     * The first factor of the public modulus <i>n</i> traditionally
     * referred to as <i>p</i>.
     */
    private BigInteger p;

    /**
     * The second factor of the public modulus <i>n</i> traditionally
     * referred to as <i>q</i>.
     */
    private BigInteger q;

    /**
     * The result of <i>q</i>^-1 (mod <i>p</i>), called the 'multiplicative
     * inverse' and traditionally referred to as <i>u</i>. This is used in
     * modular exponentiation operations using the Chinese Remainder
     * Theorem (CRT).
     */
    private BigInteger u;


// Constructor
//...........................................................................

    /**
     * Constructs an RSA private key, without setting the parameters.
     * Subclasses should call one of the setRsaParams methods in each of
     * their constructors.
     */
    protected BaseRSAPrivateKey() {}


// RSAKey interface methods implementation
//...........................................................................

    /**
     * Return the public modulus <i>n</i>: the product of both <i>p</i>
     * and <i>q</i>.
     *
     * @return the public modulus <i>n</i>: the product of both <i>p</i>
     *         and <i>q</i>.
     */
    public BigInteger getModulus() { return n; }

    /**
     * Return the private exponent <i>d</i>.
     *
     * @return the private exponent <i>d</i>.
     */
    public BigInteger getExponent() { return d; }


// RSAFactors interface methods implementation
//...........................................................................

    /**
     * Returns <i>p</i>, the first factor of the public modulus.
     *
     * @return the first factor <i>p</i>
     */
    public BigInteger getP() { return p; }

    /**
     * Return <i>q</i>, the second factor of the public modulus.
     *
     * @return the second factor <i>q</i>
     */
    public BigInteger getQ() { return q; }

    /**
     * Returns the multiplicative inverse of <i>q</i> modulo <i>p</i>. The
     * values <i>p</i> and <i>q</i> are those returned by the <i>getP()</i>
     * and <i>getQ()</i> methods respectively.
     *
     * @return the multiplicative inverse of <i>q</i> modulo <i>p</i>.
     */
    public BigInteger getInverseOfQModP() { return u; }


// Key interface methods implementation
//...........................................................................

    /**
     * Returns the name of the algorithm, for this class always "RSA".
     *
     * @return the name of the algorithm, "RSA".
     */
    public String getAlgorithm() { return "RSA"; }


// Own methods
//...........................................................................

    /**
     * Sets the RSA parameters <i>n</i> and <i>d</i>.
     *
     * @exception NullPointerException if n == null || d == null
     */
    protected void setRsaParams(BigInteger n, BigInteger d) {
        if (n == null) throw new NullPointerException("n == null");
        if (d == null) throw new NullPointerException("d == null");

        this.n = n;
        this.d = d;
    }

    /**
     * Sets the RSA parameters <i>d</i>, <i>p</i>, <i>q</i>, and <i>u</i>,
     * to allow fast execution of mathematical operations performed later
     * on during the life of this key. <i>u</i> may be null, in which case
     * it is calculated automatically.
     *
     * @exception NullPointerException if d == null || p == null || q == null
     * @exception InvalidParameterException if u must be calculated, and
     *              gcd(q, p) != 1
     */
    protected void setRsaParams(BigInteger d, BigInteger p, BigInteger q,
                                BigInteger u) {
        if (d == null) throw new NullPointerException("d == null");

        this.n = p.multiply(q);
        this.d = d;
        this.p = p;
        this.q = q;

        if (u != null && !u.multiply(q).mod(p).equals(ONE)) {
            if (DEBUG && debuglevel >= 1) debug("uq != 1 (mod p)");
            u = null;
        }
        if (u == null) {
            try {
                u = q.modInverse(p);
            } catch (ArithmeticException ae) {
                if (DEBUG && debuglevel >= 1) {
                    if (p.compareTo(ZERO) <= 0) debug("p <= 0");
                    if (p.equals(q)) debug("p == q");
                    if (!p.isProbablePrime(80)) debug("p is composite");
                    if (!q.isProbablePrime(80)) debug("q is composite");
                }
                throw new InvalidParameterException("gcd(q, p) != 1");
            }
        }
        this.u = u;
    }

    /**
     * Returns a string representation of this key. This may reveal
     * private information when debugging is enabled, and should be used
     * with care.
     *
     * @return a string representation of this key.
     */
    public String toString() {
        if (DEBUG && debuglevel >= 5) {
            return "<----- RSAPrivateKey:\n" +
                   "         d: " + BI.dumpString(d) +
                   "         p: " + BI.dumpString(p) +
                   "         q: " + BI.dumpString(q) +
                   "q^-1 mod p: " + BI.dumpString(u) +
                   "----->\n";
        } else {
            return "<BaseRSAPrivateKey>";
        }
    }
}
