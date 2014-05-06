// $Id: BaseRSAPublicKey.java,v 1.1.1.1 2002/08/27 12:32:13 grosbois Exp $
//
// $Log: BaseRSAPublicKey.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:13  grosbois
// Add cryptix 3.2
//
// Revision 1.6  2000/08/17 11:41:00  edwin
// java.* -> xjava.*
//
// Revision 1.5  1999/07/12 20:34:21  edwin
// renaming java.security.interfaces.RSAPrivateKey and RSAPublicKey to CryptixRSAPrivateKey and CryptixRSAPublicKey. This is one more step to JDK1.2 compatibility.
//
// Revision 1.4  1997/11/20 19:46:57  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.1  1997/11/18  David Hopwood
// + Swapped order of n and e in setRsaParams.
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
// Revision 0.1.0.1  1997/08/27  David Hopwood
// + Misc. fixes.
//
// Revision 0.1.0.0  1997/07/23  R. Naffah
// + Original version
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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import xjava.security.interfaces.CryptixRSAPublicKey;

/**
 * An abstract class representing an RSA public key.
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
public abstract class BaseRSAPublicKey
implements CryptixRSAPublicKey
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "BaseRSAPublicKey") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("BaseRSAPublicKey: " + s); }


// Variables
//...........................................................................

    /**
     * Public modulus. It is the product of the two <i>p</i> and <i>q</i>
     * factors but for a public key we usually do not have access to them.
     */
    private BigInteger n;

    /** Public exponent. */
    private BigInteger e;


// Constructor
//...........................................................................

    /**
     * Constructs an RSA private key, without setting the parameters.
     * Subclasses should call one of the setRsaParams methods in each of
     * their constructors.
     */
    protected BaseRSAPublicKey() {}


// RSAKey interface methods implementation
//...........................................................................

    /**
     * Returns the public modulus <i>n</i>.
     *
     * @return the public modulus <i>n</i>.
     */
    public BigInteger getModulus() { return n; }

    /**
     * Returns the public exponent <i>e</i>.
     *
     * @return the public exponent <i>e</i>.
     */
    public BigInteger getExponent() { return e; }


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
     * Sets the RSA parameters <i>n</i> and <i>e</i>.
     *
     * @exception NullPointerException if n == null || e == null
     */
    protected void setRsaParams(BigInteger n, BigInteger e) {
        if (n == null) throw new NullPointerException("n == null");
        if (e == null) throw new NullPointerException("e == null");

        this.n = n;
        this.e = e;
    }

    /**
     * Returns a string representation of this key.
     *
     * @return a string representation of this key.
     */
    public String toString () {
        return "<----- RSAPublicKey:\n" +
               "         n: " + BI.dumpString(n) +
               "         e: " + BI.dumpString(e) +
               "----->\n";
    }
}
