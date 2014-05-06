// $Id: BaseElGamalParams.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: BaseElGamalParams.java,v $
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
import xjava.security.interfaces.ElGamalParams;

/**
 * Class representing an ElGamal-specific set of key parameters, which defines
 * an ElGamal <em>key family</em>.
 * <p>
 * The same key parameters apply to both the signature and encryption
 * algorithms.
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
 * @see java.security.Key
 * @see java.security.Cipher
 * @see java.security.Signature
 */
public class BaseElGamalParams
implements ElGamalParams {

    protected BigInteger p;
    protected BigInteger g;

    /**
     * Construct an ElGamalParams object with the specified prime <i>p</i>,
     * and base <i>g</i>.
     */
    public BaseElGamalParams(BigInteger p, BigInteger g) {
        this.p = p;
        this.g = g;
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
}
