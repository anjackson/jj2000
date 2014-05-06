// $Id: PKCS5.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: PKCS5.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.7  2000/08/17 11:40:59  edwin
// java.* -> xjava.*
//
// Revision 1.6  1997/12/09 05:15:48  raif
// *** empty log message ***
//
// 1997.12.09 --RSN
// + added some documentation.
//
// Revision 1.5  1997/12/07 07:29:12  hopwood
// + Replaced reference to blockSize with 8, for better compatibility with
//   Sun's JCE.
//
// Revision 1.4  1997/11/20 19:42:48  hopwood
// + cryptix.util.* name changes.
//
// Revision 0.1.0.0  1997/11/17  D. Hopwood
// + New version based on PKCS7.java.
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
 * described in section 6.2 of RSA Data Security, Inc.'s PKCS #5
 * standard:
 * <p>
 * <pre>
 *    ...
 * </pre>
 * <p>
 * An IllegalBlockSizeException is thrown (by the Cipher class) if
 * the block size is not 8 bytes.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> <cite>PKCS#5: ... An RSA
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
public class PKCS5
extends xjava.security.PaddingScheme
{
    /** Creates a PKCS5 object. */
    public PKCS5() { super("PKCS#5"); }

    /**
     * Pads a given array of bytes. The padding is written to the same buffer
     * that is used for input (<i>in</i>). When this method returns, the padded
     * block will be stored at <code>in[offset+length..offset+7]</code>.
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
     * For PKCS#5, the padding bytes all have value
     * <code>8 - (length % 8)</code>. Hence to find the number
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
            if (in[n] > 8)
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
     * For PKCS#5 padding, the only valid <i>size</i> is 8 bytes.
     */
    protected boolean engineIsValidBlockSize(int size) {
        return size == 8;
    }
}
