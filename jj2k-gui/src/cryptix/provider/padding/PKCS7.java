// $Id: PKCS7.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: PKCS7.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.6  2000/08/17 11:40:59  edwin
// java.* -> xjava.*
//
// Revision 1.5  1997/11/20 19:42:48  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.4.1  1997/11/16  David Hopwood
// + Fixed return type of enginePad.
//
// Revision 1.4  1997/11/07 21:58:36  iang
// + Didn't compile!   ':' ==> ';' and added }
//
// Revision 1.3  1997/11/07 14:32:47  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.3  1997/08/27  David Hopwood
// + Throw CryptixException instead of IllegalBlockSizeException when
//   the input block is invalid.
// + Misc. fixes.
//
// Revision 0.1.0.2  1997/08/07  David Hopwood
// + Changed the algorithm name to "PKCS#7", from "PKCS#5x",
//   because this scheme is described in PKCS #7.
// + Renamed this class to PKCS7 (from PKCS5x).
//
// Revision 0.1.0.1  1997/07/20  R. Naffah
// + Use new PaddingScheme.
//
// Revision 0.1.0.0  1997/07/??  D. Hopwood
// + Original version derived from code Copyright (c) 1997,
//   Type & Graphics Pty Limited. All rights reserved.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.padding;

import cryptix.CryptixException;

/**
 * A class for padding cipher data according to the following scheme,
 * described in section 10.3 of RSA Data Security, Inc.'s PKCS #7
 * standard:
 * <p>
 * <pre>
 *    Some content-encryption algorithms assume the
 *    input length is a multiple of k octets, where k >
 *    1, and let the application define a method for
 *    handling inputs whose lengths are not a multiple
 *    of k octets. For such algorithms, the method shall
 *    be to pad the input at the trailing end with k -
 *    (l mod k) octets all having value k - (l mod k),
 *    where l is the length of the input. In other
 *    words, the input is padded at the trailing end
 *    with one of the following strings:
 * <br>
 *             01 -- if l mod k = k-1
 *            02 02 -- if l mod k = k-2
 *                        .
 *                        .
 *                        .
 *          k k ... k k -- if l mod k = 0
 * <br>
 *    The padding can be removed unambiguously since all
 *    input is padded and no padding string is a suffix
 *    of another. This padding method is well-defined if
 *    and only if k < 256; methods for larger k are an
 *    open issue for further study.
 * </pre>
 * <p>
 * An IllegalBlockSizeException is thrown (by the Cipher class) if
 * the block size is greater than 255 bytes.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <cite>PKCS#7: Cryptographic Message Syntax Standard. An RSA
 *        Laboratories Technical Note</cite>;
 *        Version 1.5; Revised November 1, 1993.
 * </ol>
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
public class PKCS7
extends xjava.security.PaddingScheme
{
    /** Creates a PKCS7 object. */
    public PKCS7() { super("PKCS#7"); }

    /**
     * Pads a given array of bytes. The padding is written to the same buffer
     * that is used for input (<i>in</i>). When this method returns, the padded
     * block will be stored at <code>in[offset+length..offset+blocksize-1]</code>.
     *
     * @param in        the buffer containing the incomplete block.
     * @param offset    the offset into the <code>in</code> buffer of the
     *                  first byte in the group of bytes to be padded.
     * @param length    the number of bytes from the <code>in</code> buffer,
     *                  starting at <code>offset</code>, that need to be padded.
     */
    protected int enginePad(byte[] in, int offset, int length) {
        int padLen = padLength(length);
        byte padChar = (byte) padLen;
        int j = offset + length;
        for (int i = 0; i < padLen; i++)
            in[j++] = padChar;

        return padLen;
    }

    /**
     * Given the specified subarray of bytes that includes padding bytes, returns
     * the index indicating where padding starts.
     * <p>
     * For PKCS#7, the padding bytes all have value
     * <code>blockSize - (length % blockSize)</code>. Hence to find the number
     * of added bytes, it's enough to consider the last byte value of the
     * padded message.
     *
     * @param  in       the buffer containing the bytes.
     * @param  offset   the offset into the <i>in</i> buffer of the
     *                  first byte in the block.
     * @param  length   the length of the block in bytes.
     * @return the index into the <i>in</i> buffer indicating
     *                  where the padding starts.
     * @exception CryptixException if the number of padding bytes is invalid.
     */
    protected int engineUnpad(byte[] in, int offset, int length) {
        int n = offset + length - 1;
        if (n >= 0) {
            if (in[n] > blockSize)
                throw new CryptixException(getAlgorithm() +
                    ": Invalid number of padding bytes");
            else
                return offset + length - (in[offset + length - 1] & 0xFF) ;
        } else
            return 0;
    }

    /**
     * <b>SPI</b>: Returns true if <i>size</i> is a valid block size (in
     * bytes) for this algorithm.
     * <p>
     * For PKCS#7 padding, values of <i>size</i> between 1 and 255 bytes
     * inclusive are valid. 
     */
    protected boolean engineIsValidBlockSize(int size) {
        return size > 0 && size < 256;
    }
}
