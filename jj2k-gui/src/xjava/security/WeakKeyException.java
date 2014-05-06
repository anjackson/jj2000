/*
// $Log: WeakKeyException.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:31  grosbois
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

import java.security.KeyException;

/**
 * WeakKeyException is thrown when a weak key would have been generated
 * (e.g. by a KeyGenerator).
 * <p>
 * <strong><a href="../guide/ijce/JCEDifferences.html">This class
 * is not supported in JavaSoft's version of JCE.</a></strong>
 */
public class WeakKeyException extends KeyException {
    /**
     * Constructs a WeakKeyException without any detail message.
     */
    public WeakKeyException() {
        super();
    }

    /**
     * Constructs a WeakKeyException with the specified
     * detail message. A detail message is a String that describes
     * this particular exception.
     *
     * @param msg     the detail message.
     */
    public WeakKeyException(String msg) {
        super(msg);
    }
}
