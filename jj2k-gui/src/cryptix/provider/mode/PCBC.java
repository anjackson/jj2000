// $Id: PCBC.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: PCBC.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.6  2000/08/17 11:40:58  edwin
// java.* -> xjava.*
//
// Revision 1.5  1997/12/07 07:27:50  hopwood
// + Committed changes below.
//
// Revision 1.4.1  1997/12/04  hopwood
// + Removed commented-out code.
// + Added import for cryptix.util.core.ArrayUtil.
//
// Revision 1.4  1997/11/29 17:01:29  raif
// + replaced temp* with xorBlock for consistency with other modes;
// + fixed the engineUpdate() method code;
// + added test data in cryptix.util.test.IDEA.mtest for use
//   with Maker and tried it. Works OK.
//
// Revision 1.3  1997/11/20 19:39:32  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.3.1  1997/11/16  David Hopwood
// + Removed commented-out code (since we will be using FeedbackMode
//   permanently now).
// + Fixed imports.
// + Added check that IV is set in engineUpdate.
// + Removed reset(byte[] iv) method.
// + Removed (ivStart == null) case in engineUpdate, since we now require
//   the IV to always be set.
// + FIXME: engineUpdate incorrectly assumes only one block of input.
// + This class is currently untested.
//
// Revision 1.2  1997/11/05 08:01:56  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.mode;

import cryptix.CryptixException;
import cryptix.util.core.ArrayUtil;

import xjava.security.Cipher;
import java.security.Key;
import xjava.security.FeedbackCipher;
import xjava.security.SymmetricCipher;
import java.security.KeyException;
import java.security.InvalidParameterException;
import xjava.security.IllegalBlockSizeException;

/**
 * Implements a block cipher in PCBC mode. The block size is the same as
 * that of the underlying cipher.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 9.10 Other Block-Cipher Modes,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996
 *        <p>
 *   <li> <samp>sci.crypt</samp> FAQ, "Part 5: Product Ciphers,"
 *        <a href="ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05">
 *        ftp://ftp.rtfm.mit.edu/pub/usenet/news.answers/cryptography-faq/part05</a>
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
public class PCBC
extends FeedbackMode
{

// Variables
//...........................................................................

    /**
     * Temporary buffer to hold input bytes when there's not enough of
     * them to cipher.
     */
    private byte[] xorBlock;


// Constructors
//...........................................................................

    /**
     * Constructs a PCBC cipher, assuming that the IV will be provided
     * via <code>setInitializationVector</code>.
     * <p>
     * The IV value should be random, but unlike
     * <a href=cryptix.provider.mode.CFB.html>CFB</a> mode, it is not
     * absolutely required to be unique.
     */
    public PCBC() {
        super(false, false, "Cryptix");
    }

    /**
     * Constructs a PCBC cipher, assuming that the IV will be provided
     * via <code>setInitializationVector</code>.
     * See the previous constructor for more details.
     *
     * @exception NullPointerException if cipher == null
     * @exception IllegalBlockSizeException if cipher.getPlaintextBlockSize() !=
     *            cipher.getCiphertextBlockSize()
     */
    public PCBC(Cipher cipher) {
        this();
        engineSetCipher(cipher);
    }

    /**
     * Constructs a PCBC cipher, using an initialization vector
     * provided in the constructor.
     * <p>
     * The IV value must be unique during the lifetime of the key.
     * <strong>If it is not unique, at least the first block
     * of the plaintext can be recovered.</strong>
     *
     * @param  cipher   the block cipher to use
     * @param  iv       the initial value for the shift register (IV)
     * @exception NullPointerException if cipher == null
     */
    public PCBC(Cipher cipher, byte[] iv) {
        this(cipher);
        setInitializationVector(iv);
    }


// FeedbackMode and Mode methods
//...........................................................................

    protected void engineSetCipher(Cipher cipher) {
        super.engineSetCipher(cipher);
        xorBlock = new byte[length];
    }


// Cipher methods
//...........................................................................

    protected int engineBlockSize() { return length; }

    protected void engineInitEncrypt(Key newkey)
    throws KeyException {
        cipher.initEncrypt(newkey);
        if (ivStart != null) System.arraycopy(ivStart, 0, ivBlock, 0, length);
        ArrayUtil.clear(xorBlock);
    }

    protected void engineInitDecrypt(Key newkey)
    throws KeyException {
        cipher.initDecrypt(newkey);
        if (ivStart != null) System.arraycopy(ivStart, 0, ivBlock, 0, length);
        ArrayUtil.clear(xorBlock);
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
            throw new InvalidParameterException("PCBC: IV is not set");
        if (inLen <= 0) return 0;

        switch (getState()) {
            case ENCRYPT:
//                System.arraycopy(in, inOffset, temp, 0, length);
//                for (int i = 0; i < length; i++) ivBlock[i] ^= temp[i];
//                cipher.update(ivBlock, 0, length, ivBlock, 0);
//                System.arraycopy(ivBlock, 0, out, outOffset, length);
//                for (int i = 0; i < length; i++) ivBlock[i] ^= temp[i];
//                System.arraycopy(ivBlock, 0, out, outOffset, length);

                for (int i = 0; i < length; i++) {
                    ivBlock[i] ^= in[inOffset + i];
                    xorBlock[i] ^= ivBlock[i];
                }
                cipher.update(xorBlock, 0, length, ivBlock, 0);
                System.arraycopy(ivBlock, 0, out, outOffset, length);
                System.arraycopy(in, inOffset, xorBlock, 0, length);
                break;

            case DECRYPT:
//                System.arraycopy(in, inOffset, temp, 0, length);
//                cipher.update(temp, 0, length, temp2, 0);
//                for (int i = 0; i < length; i++) {
//                    out[i+outOffset] = (byte) (temp2[i] ^ ivBlock[i]);
//                    ivBlock[i] = (byte) (temp[i] ^ temp2[i]);
//                }

                cipher.update(in, inOffset, length, xorBlock, 0);
                for (int i = 0; i < length; i++) {
                    out[outOffset + i] = (byte) (ivBlock[i] ^ xorBlock[i]);
                    ivBlock[i] = (byte)(in[inOffset + i] ^ out[outOffset + i]);
                }
                break;

            default:
                throw new CryptixException("PCBC: Cipher not initialized");
        }
        return length;
    }
}
