// $Id: BaseElGamalPublicKey.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: BaseElGamalPublicKey.java,v $
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
import xjava.security.interfaces.ElGamalPublicKey;
import xjava.security.interfaces.ElGamalParams;

/**
 * A class representing an ElGamal public key. This is also a superclass
 * for ElGamal private keys. It is called BaseElGamalPublicKey to
 * distinguish it from the interface ElGamalPublicKey, without having to
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
 * @see ElGamalParams
 * @see java.security.Key
 * @see java.security.Cipher
 * @see java.security.Signature
 */
public class BaseElGamalPublicKey implements ElGamalPublicKey {

    protected BigInteger p;
    protected BigInteger g;
    protected BigInteger y;

    /**
     * Constructs a BaseElGamalPublicKey with the specified prime <i>p</i>,
     * base <i>g</i>, and public value <i>y = g<super>x</super></i> mod <i>p</i>.
     *
     * @exception NullPointerException if p == null || g == null || y == null
     */
    public BaseElGamalPublicKey(BigInteger p, BigInteger g, BigInteger y) {
        if (p == null) throw new NullPointerException("p == null");
        if (g == null) throw new NullPointerException("g == null");
        if (y == null) throw new NullPointerException("y == null");
        this.p = p;
        this.g = g;
        this.y = y;
    }

    /**
     * Constructs a BaseElGamalPublicKey with a prime and base taken
     * from an object implementing java.security.interfaces.ElGamalParams,
     * and the specified public value <i>y = g<super>x</super></i> mod <i>p</i>.
     *
     * @exception NullPointerException if params == null || y == null
     */
    public BaseElGamalPublicKey(ElGamalParams params, BigInteger y) {
        this(params.getP(), params.getG(), y);
    }

    /**
     * Returns the prime, <i>p</i>.
     * 
     * @return the prime as a java.math.BigInteger
     */
    public BigInteger getP() { return p; }

    /**
     * Returns the base, <i>g</i>.
     * 
     * @return the base as a java.math.BigInteger 
     */
    public BigInteger getG() { return g; }

    /**
     * Returns the value of <i>y = g<super>x</super></i> mod <i>p</i>
     * (where <i>x</i> is the private value).
     *
     * @return y as a java.math.BigInteger
     */
    public BigInteger getY() { return y; }

    /**
     * Returns the name of the algorithm this key is intended for
     * ("ElGamal").
     */
    public String getAlgorithm() {
        return "ElGamal";
    }

    /**
     * Returns the encoding format name for this key. In the current
     * release this always returns null, because key encoding is not
     * yet supported.
     */
    public String getFormat() {
        return null;
    }

    /**
     * Returns an encoding of this key as a byte array. In the current
     * release this always returns null, because key encoding is not
     * yet supported.
     */
    public byte[] getEncoded() {
        return null;
    }
}
