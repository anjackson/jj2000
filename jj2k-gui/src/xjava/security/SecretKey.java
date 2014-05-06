/*
// $Log: SecretKey.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:25  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
*/

package xjava.security;

import java.security.Key;

/**
 * A secret key.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @see Key
 * @see PrivateKey
 * @see PublicKey
 * @see Cipher
 */
public interface SecretKey extends Key {
}
