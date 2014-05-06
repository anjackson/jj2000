// $Id: RawRSAPrivateKey.java,v 1.1.1.1 2002/08/27 12:32:13 grosbois Exp $
//
// $Log: RawRSAPrivateKey.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:13  grosbois
// Add cryptix 3.2
//
// Revision 1.7  2000/08/17 11:41:00  edwin
// java.* -> xjava.*
//
// Revision 1.6  1999/07/12 20:34:21  edwin
// renaming java.security.interfaces.RSAPrivateKey and RSAPublicKey to CryptixRSAPrivateKey and CryptixRSAPublicKey. This is one more step to JDK1.2 compatibility.
//
// Revision 1.5  1997/12/23 18:19:50  raif
// *** empty log message ***
//
// Revision 1.4.1  1997/12/24  raif
// + resurrected the Constructor(n, d). PKCS#1 document
//   implies that p and q can be constructed if n and e
//   are known (ed = 1 mop (p-1)(q-1); n = pq) which is
//   not the case here. Even if and when we do this, we
//   should keep this constructor and call Cosntructor(d,p,q)
//   from within.
//
// Revision 1.4  1997/11/23 03:09:18  hopwood
// + Mostly documentation changes.
//
// Revision 1.3.1  1997/11/18  David Hopwood
// + Swapped order of n and d in (commented out) use of setRsaParams.
//
// Revision 1.3  1997/11/20 19:46:57  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.2  1997/11/04 19:33:31  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/08/27  David Hopwood
// + Now u = q^-1 (mod p), not p^-1 (mod q). Apart from PGP, this is
//   more commonly used (e.g. see the P1363 draft).
// + Made to extend BaseRSAPrivateKey.
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
import xjava.security.interfaces.CryptixRSAPrivateKey;

/**
 * A class representing a RAW-encoded RSA private key.
 * <p>
 * <a name="encoding">The encoding consists of the following, in order:</a>
 * <ol>
 *   <li> the private exponent <i>d</i>,
 *   <li> the first factor of the modulus, <i>p</i>,
 *   <li> the second factor of the modulus, <i>q</i>,
 *   <li> the coefficient <i>q</i>^-1 mod <i>p</i>.
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
public class RawRSAPrivateKey
extends BaseRSAPrivateKey
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final int debuglevel =
        DEBUG ? Debug.getLevel("RSA", "RawRSAPrivateKey") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("RawRSAPrivateKey: " + s); }


// Constructors
//...........................................................................

// PKCS#1 section 7.2 says that it is possible to reconstruct p and q from
// n and d, "according to a result by Miller [Mil76]":
//
//   G.L. Miller. Riemann's hypothesis and tests for
//   primality. Journal of Computer and Systems
//   Sciences, 13(3):300-307, 1976.
//
// Until we look this up, omit the following constructor.
    /**
     * Constructs a raw RSA private key given the private exponent, and
     * the public modulus <i>n</i>. This should be used only when the
     * factors of <i>n</i> are unknown.
     *
     * @param  d  the private exponent
     * @param  n  the public modulus
     */
    public RawRSAPrivateKey (BigInteger n, BigInteger d) {
        setRsaParams(n, d);
    }

    /**
     * Constructs a raw RSA private key given the private exponent, and
     * the two factors used to generate the public modulus <i>n</i>.
     * The factors may be in any order.
     *
     * @param  d  the private exponent
     * @param  p  the first factor of the public modulus
     * @param  q  the second factor of the public modulus
     */
    public RawRSAPrivateKey(BigInteger d, BigInteger p, BigInteger q) {
        setRsaParams(d, p, q, null);
    }

    /**
     * Constructs a raw RSA private key given the private exponent, and
     * the two factors used to generate the public modulus <i>n</i>.
     * The factors <em>must</em> be given in the correct order, such
     * that <i>u</i> = <i>q</i>^-1 mod <i>p</i>.
     *
     * @param  d  the private exponent
     * @param  p  the first factor of the public modulus
     * @param  q  the second factor of the public modulus
     * @param  u  the multiplicative inverse of q modulo p
     */
    public RawRSAPrivateKey(BigInteger d, BigInteger p, BigInteger q,
                            BigInteger u) {
        setRsaParams(d, p, q, u);
    }

    /**
     * Constructs a raw RSA private key from data read from an InputStream,
     * encoded as described <a href="#encoding">above</a>.
     *
     * @param  is  the input stream from which data is parsed.
     * @exception IOException if any I/O error occurs.
     * @see #getEncoded
     */
    public RawRSAPrivateKey (InputStream is)
    throws IOException {
        BigInteger d = BI.fromStream(is);
        BigInteger p = BI.fromStream(is);
        BigInteger q = BI.fromStream(is);
        BigInteger u = BI.fromStream(is);

        setRsaParams(d, p, q, u);
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
            BI.toStream(getP(), bos);
            BI.toStream(getQ(), bos);
            BI.toStream(getInverseOfQModP(), bos);
            bos.flush();
            bos.close();
            return baos.toByteArray();
        }
        catch (IOException e) { return null; }
    }
}
