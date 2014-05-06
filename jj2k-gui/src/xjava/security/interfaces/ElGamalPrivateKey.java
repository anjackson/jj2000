/*
// $Log: ElGamalPrivateKey.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:31  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:28  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
*/

package xjava.security.interfaces;

import java.math.BigInteger;
import java.security.PrivateKey;

/**
 * The interface to an ElGamal private key.
 * <p>
 * <b>References</b>
 * <p>
 * <blockquote>
 *    <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *    "Section 19.6 ElGamal,"
 *    <cite>Applied Cryptography</cite>,
 *    Wiley 2nd Ed, 1996.
 * </blockquote>
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  IJCE 1.0.1
 * @see ElGamalKey
 * @see java.security.Cipher
 * @see java.security.Signature
 */
public interface ElGamalPrivateKey extends ElGamalKey, PrivateKey {
    /**
     * Returns the value of <i>x</i> (the private key).
     *
     * @return x as a java.math.BigInteger
     */
    BigInteger getX();
}
