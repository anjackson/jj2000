// $Id: CBC.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: CBC.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.7  2000/08/17 11:40:57  edwin
// java.* -> xjava.*
//
// Revision 1.6  1998/04/02 01:42:01  raif
// *** empty log message ***
//
// Revision 1.5.1  1998/04/02  raif
// + added Gertjan van Oosten <gertjan@West.NL> bug correction
//   in the engineUpdate/DECRYPT method/case.
// + removed dead code.
// + cosmetics.
//
// Revision 1.5  1997/12/09 05:14:36  raif
// *** empty log message ***
//
// Revision 1.4  1997/11/20 19:39:31  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.1  1997/11/16  David Hopwood
// + Removed commented-out code (since we will be using FeedbackMode
//   permanently now).
// + Fixed imports.
// + FIXME: engineUpdate incorrectly assumes only one block of input.
//
// Revision 1.3  1997/11/07 05:53:25  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/05 08:01:56  raif
// + Renamed temp to xorBlock.
// + Call cipher.update instead of cipher.crypt.
// + Fixed the decrypt bug.
// + Added all the fixes that date back from Aug/97.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
/*
 * Copyright (c) 1997, 1998 Systemics Ltd on behalf of
 * the Cryptix Development Team.  All rights reserved.
 */
package cryptix.provider.mode;

import cryptix.CryptixException;

import xjava.security.Cipher;
import java.security.Key;
import java.security.KeyException;
import java.security.InvalidParameterException;
import xjava.security.IllegalBlockSizeException;

/**
 * Implements a block cipher in CBC mode. The block size is the same as
 * that of the underlying cipher.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 9.3 Cipher Block Chaining Mode," and "Section 9.11 Choosing a Cipher Mode,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996.<p>
 *   <li> <samp>sci.crypt</samp> FAQ, "Part 5: Product Ciphers,"
 *        <a href="ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05">
 *        ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05</a>.<p>
 *   <li> National Bureau of Standards (now NIST),
 *        "DES Modes of Operation,"
 *        <cite>NBS FIPS PUB 81</cite>,
 *        U.S. Department of Commerce, December 1980.</ol><p>
 *
 * <b>Copyright</b> &copy; 1997, 1998
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.<p>
 *
 * <b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @author Raif S. Naffah
 * @since  Cryptix 2.2.2
 */
public class CBC
extends FeedbackMode
{
// Variables
//...........................................................................

    /**
     * Temporary buffer to hold input bytes when there's not enough of
     * them to cipher.
     */
    protected byte[] xorBlock;


// Constructors
//...........................................................................

    /**
     * Constructs a CBC cipher, assuming that the IV will be provided
     * via <code>setInitializationVector</code>.
     * <p>
     * The IV value should be random, but unlike
     * <a href=cryptix.provider.mode.CFB.html>CFB</a> mode, it is not
     * absolutely required to be unique.
     */
    public CBC() { super(false, false, "Cryptix"); }

    /**
     * Constructs a CBC cipher, assuming that the IV will be provided
     * via <code>setInitializationVector</code>.
     * See the previous constructor for more details.
     *
     * @param  cipher   the cipher object to use in CBC mode.
     * @exception NullPointerException if cipher == null
     * @exception IllegalBlockSizeException if cipher.getPlaintextBlockSize() !=
     *                  cipher.getCiphertextBlockSize()
     */
    public CBC (Cipher cipher) {
        this();
        engineSetCipher(cipher);
    }

    /**
     * Constructs a CBC cipher, using an initialization vector
     * provided in the constructor.
     * <p>
     * For CBC mode the IV value should be random, but unlike
     * <a href=cryptix.security.CFB.html>CFB</a> mode, it is not
     * absolutely required to be unique.
     *
     * @param  cipher   the block cipher to use
     * @param  iv       the initial value for the shift register (IV)
     * @exception NullPointerException if cipher == null
     */
    public CBC (Cipher cipher, byte[] iv) {
        this(cipher);
        setInitializationVector(iv);
    }


// FeedbackMode and Mode methods
//...........................................................................

    protected void engineSetCipher (Cipher cipher) {
        super.engineSetCipher(cipher);
        xorBlock = new byte[length];
    }


// Cipher methods
//...........................................................................

    protected int engineBlockSize() { return length; }

    protected void engineInitEncrypt (Key newkey) throws KeyException {
        cipher.initEncrypt(newkey);
        if (ivStart != null)
            System.arraycopy(ivStart, 0, ivBlock, 0, length);
    }

    protected void engineInitDecrypt (Key newkey) throws KeyException {
        cipher.initDecrypt(newkey);
        if (ivStart != null)
            System.arraycopy(ivStart, 0, ivBlock, 0, length);
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
    protected int engineUpdate (byte[] in, int inOffset, int inLen,
                                byte[] out, int outOffset) {
        if (ivBlock == null)
            throw new InvalidParameterException("CBC: IV is not set");

        switch (getState()) {
            case ENCRYPT:
                for (int i = 0; i < inLen; )
                    ivBlock[i++] ^= in[inOffset++];
                cipher.update(ivBlock, 0, inLen, ivBlock, 0);
                System.arraycopy(ivBlock, 0, out, outOffset, inLen);
                break;

            case DECRYPT:
                byte[] ivTmp = new byte[inLen];
                System.arraycopy(in, inOffset, ivTmp, 0, inLen);
                cipher.update(in, inOffset, inLen, xorBlock, 0);
                for (int i = 0; i < inLen; i++)
                    out[i+outOffset] = (byte) (xorBlock[i] ^ ivBlock[i]);
                System.arraycopy(ivTmp, 0, ivBlock, 0, inLen);
                ivTmp = null;
                break;

            default:
                throw new CryptixException("CBC: Cipher not initialized");
        }
        return inLen;
    }
}
