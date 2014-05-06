/*
// $Log: CryptixRSAPublicKey.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:31  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:28  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1  1999/07/12 20:35:35  edwin
// renaming java.security.interfaces.RSAPrivateKey and RSAPublicKey to CryptixRSAPrivateKey and CryptixRSAPublicKey. This is one more step to JDK1.2 compatibility.
//
// Revision 1.1.1.1  1997/11/03 22:36:58  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
*/

package xjava.security.interfaces;

import java.security.PublicKey;

/**
 * The interface to an RSA public key.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  IJCE 1.0
 * @see java.security.Cipher
 * @see java.security.Signature
 */
public interface CryptixRSAPublicKey extends RSAKey, PublicKey {
}
