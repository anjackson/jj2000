// $Id: BaseElGamalPrivateKey.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: BaseElGamalPrivateKey.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.2  2000/08/17 11:40:54  edwin
// java.* -> xjava.*
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
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.elgamal;

import java.math.BigInteger;
import xjava.security.interfaces.ElGamalPrivateKey;
import xjava.security.interfaces.ElGamalParams;

/**
 * A class representing an ElGamal private key. This can also act as
 * an ElGamal public key. It is called BaseElGamalPrivateKey to
 * distinguish it from the interface ElGamalPrivateKey, without having to
 * use fully-qualified names.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 19.6 ElGamal,"
 *        <cite>Applied Cryptography, 2nd Edition</cite>,
 *        John Wiley &amp; Sons, 1996.
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
 * @see ElGamalKey
 * @see java.security.Cipher
 * @see java.security.Signature
 */
public class BaseElGamalPrivateKey
extends BaseElGamalPublicKey
implements ElGamalPrivateKey {

    protected BigInteger x;

    /**
     * Constructs a BaseElGamalPrivateKey with the specified prime
     * <i>p</i>, base <i>g</i>, private value <i>x</i>, and pre-calculated
     * public value <i>y = g<super>x</super></i> mod <i>p</i>.
     *
     * @param p the prime as a java.math.BigInteger
     * @param g the base as a java.math.BigInteger
     * @param x the value of x as a java.math.BigInteger
     * @param y the value of y as a java.math.BigInteger
     * @exception NullPointerException if p == null || g == null ||
     *          x == null || y == null
     */
    public BaseElGamalPrivateKey(BigInteger p, BigInteger g,
                                 BigInteger x, BigInteger y) {
        super(p, g, y);
        if (x == null) throw new NullPointerException("x == null");
        this.x = x;
    }

    /**
     * Constructs a BaseElGamalPrivateKey with the specified prime
     * <i>p</i>, base <i>g</i>, and private value <i>x</i>.
     * <p>
     * The public value <i>y = g<super>x</super></i> mod <i>p</i> will be
     * calculated.
     *
     * @param p the prime as a java.math.BigInteger
     * @param g the base as a java.math.BigInteger
     * @param x the value of x as a java.math.BigInteger
     * @exception NullPointerException if p == null || g == null || x == null
     */
    public BaseElGamalPrivateKey(BigInteger p, BigInteger g,
                                 BigInteger x) {
        this(p, g, x, g.modPow(x, p));
    }

    /**
     * Constructs a BaseElGamalPublicKey with a prime and base taken
     * from an object implementing java.security.interfaces.ElGamalParams,
     * and the specified private value <i>x</i>.
     * <p>
     * The public value <i>y = g<super>x</super></i> mod <i>p</i> will be
     * calculated.
     *
     * @param params  the parameters for this key
     * @param x       the value of x as a java.math.BigInteger
     * @exception NullPointerException if params == null || x == null
     *
     * @see ElGamalParams
     */
    protected BaseElGamalPrivateKey(ElGamalParams params, BigInteger x) {
        this(params.getP(), params.getG(), x);
    }

    /**
     * Returns the value of <i>x</i> (the private key).
     *
     * @return x as a java.math.BigInteger
     */
    public BigInteger getX() { return x; }
}
