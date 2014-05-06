// $Id: CFB.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: CFB.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.5  2000/08/17 11:40:58  edwin
// java.* -> xjava.*
//
// Revision 1.4  1997/11/20 19:39:32  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.1  1997/11/16  David Hopwood
// + Removed commented-out code (since we will be using FeedbackMode
//   permanently now).
// + Fixed imports.
// + Added check that IV is set in engineUpdate.
//
// Revision 1.3  1997/11/07 05:53:25  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/05 08:01:56  raif
// + Added constructor with (Cipher, IV).
// + Added initialisation of currentByte in engineInitEncrypt/Decrypt
//   to force proper handling of the iv in successive uses of the Mode
//   instance object without the need to re-setInitializationVector().
// + Call cipher.update instead of cipher.crypt.
// + Added fixes of Aug/97.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.3.1.0  1997/08/28  David Hopwood
// + Rewritten as a pure IJCE 1.0.1 Mode, and moved to cryptix.provider.mode.
//   Cryptix 2.2 compatibility is now handled by the separate
//   cryptix.security.CFB class.
//
// Revision 0.3.0.1  1997/08/06  David Hopwood
// + Renamed engineKeyLength to keyLength, and made it public but
//   deprecated.
// + Call superclass constructor with implBuffering = true,
//   implPadding = false, provider = "Cryptix".
//
// Revision 0.3.0.0  1997/??/??  David Hopwood
// + Cryptix 2.2.0a.
//
// Revision 0.2.5.2  1997/04/08  Systemics
// + Added doco.  Noted IV problem.  See CipherFeedback.
//
// Revision 0.2.5.1  1997/03/15  Jill Baker
// + Moved this file here from old namespace.
//
// Revision 0.2.5.0  1997/02/24  Systemics
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1995-1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.mode;

import cryptix.util.core.Debug;
import cryptix.CryptixException;

import java.io.PrintWriter;
import xjava.security.Cipher;
import java.security.Key;
import java.security.KeyException;
import java.security.InvalidParameterException;

/**
 * Implements a byte-oriented stream cipher using n-bit CFB with an
 * n-bit-sized block cipher.
 * <p>
 * The full block size of the supplied cipher is used for the
 * Cipher Feedback Mode. The bytes supplied are processed and
 * returned immediately.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 9.6 Cipher Feedback Mode," and "Section 9.11 Choosing a Cipher Mode,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996
 *        <p>
 *   <li> <samp>sci.crypt</samp> FAQ, "Part 5: Product Ciphers,"
 *        <a href="ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05">
 *        ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05</a>
 *        <p>
 *   <li> National Bureau of Standards (now NIST),
 *        "DES Modes of Operation,"
 *        <cite>NBS FIPS PUB 81</cite>,
 *        U.S. Department of Commerce, December 1980
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1995-1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
public class CFB
extends FeedbackMode
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static final boolean DEBUG_SLOW = Debug.GLOBAL_DEBUG_SLOW;
    private static final int debuglevel = DEBUG ? Debug.getLevel("CFB") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("CFB: " + s); }


// Variables
//...........................................................................

    private byte[] xorBlock;


// Constructors
//...........................................................................

    /**
     * Constructs a CFB mode object.
     * <p>
     * The IV is provided via <code>setInitializationVector</code>. This IV
     * must be unique during the lifetime of the key. <strong>If it is not
     * unique, at least the first block of the plaintext can be recovered.</strong>
     */
    public CFB() {
        super(true, false, "Cryptix");  // implements its own buffering
    }

    /**
     * Constructs a CFB cipher, assuming that the IV will be provided
     * via <code>setInitializationVector</code>.
     * See the previous constructor for more details.
     *
     * @param  cipher   the cipher object to use in CFB mode.
     * @exception NullPointerException if cipher == null
     */
    public CFB(Cipher cipher) {
        this();
        engineSetCipher(cipher);
    }

    /**
     * Constructs a CFB cipher, using an initialization vector
     * provided in the constructor.
     *
     * @param  cipher   the block cipher to use
     * @param  iv       the initial value for the shift register (IV)
     * @exception NullPointerException if cipher == null
     */
    public CFB(Cipher cipher, byte[] iv) {
        this(cipher);
        setInitializationVector(iv);
    }


// FeedbackMode and Mode methods
//...........................................................................

    protected void engineSetCipher(Cipher cipher) {
        super.engineSetCipher(cipher);
        xorBlock = new byte[length]; // all zeroes by default
    }


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
            throw new InvalidParameterException("CFB: IV is not set");

        if (in == out && outOffset > inOffset &&
            outOffset < (long)inOffset+inLen) {
            // The input array would be overwritten, so copy it.
            byte[] newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        switch (getState()) {
            case ENCRYPT:
                for (int i = 0; i < inLen; i++)
                    out[i + outOffset] = encryptByte(in[i + inOffset]);
                break;
            case DECRYPT:
                for (int i = 0; i < inLen; i++)
                    out[i + outOffset] = decryptByte(in[i + inOffset]);
                break;
            default:
                throw new CryptixException("CFB: Cipher not initialized");
        }
        return inLen;
    }


// Own methods
//...........................................................................

    /** Encrypts a byte in CFB mode. */
    private byte encryptByte(byte b) {
        if (currentByte >= length) {
            currentByte = 0;
            if (ivStart == null) {
                ivStart = new byte[length];
                System.arraycopy(ivBlock, 0, ivStart, 0, length);
            }
//            cipher.crypt(ivBlock, 0, length, xorBlock, 0);
            cipher.update(ivBlock, 0, length, xorBlock, 0);
        }
        b ^= xorBlock[currentByte];
        ivBlock[currentByte] = b;
        currentByte++;
        return b;
    }

    /** Decrypts a byte in CFB mode. */
    private byte decryptByte(byte b) {
        if (currentByte >= length) {
            currentByte = 0;
            if (ivStart == null) {
                ivStart = new byte[length];
                System.arraycopy(ivBlock, 0, ivStart, 0, length);
            }
//            cipher.crypt(ivBlock, 0, length, xorBlock, 0);
            cipher.update(ivBlock, 0, length, xorBlock, 0);
        }
        ivBlock[currentByte] = b;
        b ^= xorBlock[currentByte];
        currentByte++;
        return b;
    }

    /**
     * Rotates the IV left by currentByte bytes, to mimic the V2.2
     * behaviour. This is needed in order to implement the variant of CFB
     * used by PGP.
     *
     * @see cryptix.provider.mode.CFB_PGP
     */
    protected void next_block() {
        byte[] buf = new byte[length];
        System.arraycopy(ivBlock, currentByte, buf, 0, length-currentByte);
        System.arraycopy(ivBlock, 0, buf, length-currentByte, currentByte);
        ivBlock = buf;
        currentByte = length;
    }
}
