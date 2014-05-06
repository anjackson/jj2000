// $Id: RawRSAPublicKey.java,v 1.1.1.1 2002/08/27 12:32:13 grosbois Exp $
//
// $Log: RawRSAPublicKey.java,v $
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
// + Made to extend BaseRSAPublicKey.
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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import xjava.security.interfaces.CryptixRSAPublicKey;

/**
 * A class representing a RAW-encoded RSA public key.
 * <p>
 * <a name="encoding">The encoding consists of the following, in order:</a>
 * <ol>
 *   <li> the public exponent <i>e</i>,
 *   <li> the modulus <i>n</i>.
 * </ol>
 * <p>
 * Each integer is represented as follows:
 * <pre>
 *    Byte    Length
 *    offset  (bytes)   Meaning
 *    0       2         The length in bits of this BigInteger (MSB first);
 *    2       variable  The BigInteger's magnitude with no leading zeroes,
 *                      again MSB first.
 * </pre>
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
public class RawRSAPublicKey
extends BaseRSAPublicKey
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "RawRSAPublicKey") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("RawRSAPublicKey: " + s); }


// Constants
//...........................................................................

    /** Fermat prime F4. */
    private static final BigInteger F4 = BigInteger.valueOf(0x10001L);


// Constructors
//...........................................................................

    /**
     * Constructs a raw RSA public key given the public modulus <i>n</i>,
     * and the public exponent <i>e</i>.
     *
     * @param  n  the public modulus
     * @param  e  the public exponent
     */
    public RawRSAPublicKey(BigInteger n, BigInteger e) {
        setRsaParams(n, e);
    }

    /**
     * Constructs a raw RSA public key given the public modulus and using
     * the Fermat prime F4 (value 0x10001) as the exponent.
     *
     * @param  n  the public modulus
     */
    public RawRSAPublicKey (BigInteger n) {
        this(n, F4);
    }

    /**
     * Constructs a raw RSA public key from data read from an InputStream,
     * encoded as described <a href="#encoding">above</a>.
     *
     * @param  is  the input stream from which data is parsed.
     * @exception IOException if any I/O error occurs.
     * @see #getEncoded
     */
    public RawRSAPublicKey (InputStream is)
    throws IOException {
        BigInteger e = BI.fromStream(is);
        BigInteger n = BI.fromStream(is);

        setRsaParams(n, e);
    }


// Own methods
//...........................................................................

    /**
     * Returns the encoding format name, for this class always "RAW".
     *
     * @return the encoding format name for this key, "RAW".
     */
    public String getFormat() { return "RAW"; }

    /**
     * Returns an encoding of the key as a byte array, as described
     * <a href="#encoding">above</a>.
     * 
     * @return the encoded byte array, or null if an error occurred.
     */
    public byte[] getEncoded() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        try {
            BI.toStream(getExponent(), bos);
            BI.toStream(getModulus(), bos);
            bos.flush();
            bos.close();
            return baos.toByteArray();
        }
        catch (IOException e) { return null; }
    }
}
