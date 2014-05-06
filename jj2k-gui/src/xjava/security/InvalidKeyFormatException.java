/*
// $Log: InvalidKeyFormatException.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:24  edwin
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

/**
 * InvalidKeyFormatException is thrown when a key format is not recognized.
 * (Current valid formats for the "SUN" provider include "X.509", "PKCS#8",
 * "RAW-BigInteger", and "RAW-DER".)
 * <p>
 * This class is documented in JavaSoft's preview APIs, for introduction
 * in Java 1.2.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  IJCE 1.0.1
 */
public class InvalidKeyFormatException extends GeneralSecurityException {
    /**
     * Constructs an InvalidKeyFormatException without any detail message.
     */
    public InvalidKeyFormatException() {
        super();
    }

    /**
     * Constructs an InvalidKeyFormatException with the specified
     * detail message. A detail message is a String that describes
     * this particular exception.
     *
     * @param msg     the detail message.
     */
    public InvalidKeyFormatException(String msg) {
        super(msg);
    }
}
