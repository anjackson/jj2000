// $Id: OFB.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: OFB.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.6  2000/08/17 11:40:58  edwin
// java.* -> xjava.*
//
// Revision 1.5  1997/11/21 05:45:11  hopwood
// + Missed a name change.
//
// Revision 1.4  1997/11/20 19:39:32  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.1  1997/11/16  David Hopwood
// + Removed commented-out code (since we will be using FeedbackMode
//   permanently now).
// + Fixed imports.
// + Added check that IV is set in engineUpdate.
// + Removed reset(byte[] iv) method.
// + Removed (ivStart == null) case in cryptByte, since we now require
//   the IV to always be set.
//
// Revision 1.3  1997/11/07 05:53:25  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/05 08:01:56  raif
// + Added initialisation of currentByte in engineInitEncrypt/Decrypt
//   to force proper handling of the iv in successive uses of the Mode
//   instance object without the need to re-setInitializationVector().
// + Call cipher.update instead of cipher.crypt.
// + Added Aug/97 fixes.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
/*
 * Copyright (c) 1995-1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.mode;

import cryptix.CryptixException;

import xjava.security.Cipher;
import java.security.Key;
import java.security.KeyException;
import java.security.InvalidParameterException;
import xjava.security.IllegalBlockSizeException;

/**
 * Implements a byte-oriented stream cipher using n-bit OFB with an
 * n-bit-sized block cipher.
 * <p>
 * The full block size of the supplied cipher is used for the
 * Output Feedback Mode. The bytes supplied are processed and
 * returned immediately.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 9.8 Output Feedback Mode," and "Section 9.11 Choosing a Cipher Mode,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996
 *        <p>
 *   <li> <code>sci.crypt</code> FAQ, "Part 5: Product Ciphers,"
 *        <a href="ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05">
 *        ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05</a>
 *        <p>
 *   <li> National Bureau of Standards (now NIST),
 *        "DES Modes of Operation,"
 *        <cite>NBS FIPS PUB 81</cite>,
 *        U.S. Department of Commerce, December 1980
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @author Raif S. Naffah
 * @since  Cryptix 2.2.2
 */
public class OFB
extends FeedbackMode
{

// Constructors
//...........................................................................

    /**
     * Constructs an OFB cipher, assuming that the IV will be provided
     * via <code>setInitializationVector</code>.
     * <p>
     * The IV value must be unique during the lifetime of the key.
     * <strong>If it is not unique, and an attacker has access to a different
     * message encrypted under the same IV and key, all of the plaintext can
     * normally be recovered.</strong>
     */
    public OFB() {
        super(true, false, "Cryptix");  // implements its own buffering
    }

    /**
     * Constructs an OFB cipher, assuming that the IV will be provided
     * via <code>setInitializationVector</code>.
     * See the previous constructor for more details.
     *
     * @param  cipher   the cipher object to use in OFB mode.
     * @exception NullPointerException if cipher == null
     * @exception IllegalBlockSizeException if cipher.getPlaintextBlockSize() !=
     *                  cipher.getCiphertextBlockSize()
     */
    public OFB(Cipher cipher) {
        this();
        engineSetCipher(cipher);
    }

    /**
     * Constructs a OFB cipher, using an initialization vector
     * provided in the constructor.
     * <p>
     * The IV value must be unique during the lifetime of the key.
     * <strong>If it is not unique, and an attacker has access to a different
     * message encrypted under the same IV and key, all of the plaintext can
     * normally be recovered.</strong>
     *
     * @param  cipher   the block cipher to use
     * @param  iv       the initial value for the shift register (IV)
     * @exception NullPointerException if cipher == null
     */
    public OFB(Cipher cipher, byte[] iv) {
        this(cipher);
        setInitializationVector(iv);
    }


// FeedbackMode and Mode methods
//...........................................................................

// Implementations from FeedbackMode are sufficient.


// Cipher methods
//...........................................................................

    protected int engineBlockSize() { return 1; }

    protected void engineInitEncrypt(Key newkey)
    throws KeyException {
        cipher.initEncrypt(newkey);
        if (ivStart != null) System.arraycopy(ivStart, 0, ivBlock, 0, length);
        currentByte = length;               // to force crypting the iv
    }

    protected void engineInitDecrypt(Key newkey)
    throws KeyException {
        cipher.initEncrypt(newkey);
        if (ivStart != null) System.arraycopy(ivStart, 0, ivBlock, 0, length);
        currentByte = length;               // to force crypting the iv
    }

    /**
     * <b>SPI</b>: This is the main engine method for updating data.
     * It may be called with any size of input.
     * <p>
     * <code>in</code> and <code>out</code> may be the same array, and the
     * input and output regions may overlap.
     *
     * @param in        the input data.
     * @param inOffset  the offset into <code>in</code> specifying where
     *                  the data starts.
     * @param inLen     the length of the subarray.
     * @param out       the output array.
     * @param outOffset the offset indicating where to start writing into
     *                  the <code>out</code> array.
     * @return the number of bytes written.
     */
    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out,
                                int outOffset) {
        if (ivBlock == null)
            throw new InvalidParameterException("OFB: IV is not set");
        if (getState() == UNINITIALIZED)
            throw new CryptixException("OFB: Cipher not initialized");

        if (in == out && outOffset > inOffset &&
            outOffset < (long)inOffset+inLen) {
            // The input array would be overwritten, so copy it.
            byte[] newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        for (int i = 0; i < inLen; i++)
            out[i + outOffset] = cryptByte(in[i + inOffset]);

        return inLen;
    }


// Own methods
//...........................................................................

    /**
     * Encrypts or decrypts a byte in OFB mode (encryption and decryption
     * use the same algorithm).
     */
    private byte cryptByte(byte b) {
        if (currentByte >= length) {
            currentByte = 0;
//            cipher.crypt(ivBlock, 0, length, ivBlock, 0);
            cipher.update(ivBlock, 0, length, ivBlock, 0);
        }
        b ^= ivBlock[currentByte];
        currentByte++;
        return b;
    }
}
