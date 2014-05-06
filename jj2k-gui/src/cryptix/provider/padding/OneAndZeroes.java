// $Id: OneAndZeroes.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
//
// $Log: OneAndZeroes.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:12  grosbois
// Add cryptix 3.2
//
// Revision 1.4  2000/08/17 11:40:59  edwin
// java.* -> xjava.*
//
// Revision 1.3  1997/11/20 19:42:48  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.2.1  1997/11/16  David Hopwood
// + Fixed return type of enginePad.
//
// Revision 1.2  1997/11/07 14:32:47  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.2  1997/08/06  David Hopwood
// + Added engineIsValidBlockSize method, which always returns
//   true.
//
// Revision 0.1.0.1  1997/08/02  David Hopwood
// + Changed the standard name of this scheme from "10...0" to
//   "OneAndZeroes".
// + Cosmetic changes.
//
// Revision 0.1.0.0  1997/07/20  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.padding;

/**
 * Class for padding cipher data with a binary-digit one, followed by
 * as many binary-digit zeroes as needed to fill this instance's
 * <code>blockSize</code>.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 * @see java.security.PaddingScheme
 */
public final class OneAndZeroes
extends xjava.security.PaddingScheme
{
    /** Creates a OneAndZeroes object. */
    public OneAndZeroes() { super("OneAndZeroes"); }

    /**
     * Pads a given array of bytes. The padding is written to the same
     * buffer that is used for input (<i>in</i>). When this method returns,
     * the padded block will be stored at <code>in[offset..offset+blockSize-1]</code>.
     *
     * @param  in       the buffer containing the incomplete block.
     * @param  offset   the offset into the <i>in</i> buffer of the
     *                  first byte in the group of bytes to be padded.
     * @param  length   the number of bytes from the <i>in</i> buffer,
     *                  starting at <i>offset</i>, that need to be padded.
     */
    protected int enginePad(byte[] in, int offset, int length) {
        int padLen = padLength(length);
        int j = offset + length;
        in[j++] = (byte) 0x80;
        for (int i = 1; i < padLen; i++)
            in[j++] = 0x00;

        return padLen;
    }

    /**
     * Given the specified subarray of bytes that includes padding bytes, returns
     * the index indicating where padding starts.
     *
     * @param in        the buffer containing the bytes.
     * @param offset    the offset into the <i>in</i> buffer of the
     *                  first byte in the block.
     * @param length    the number of bytes from the <i>in</i> buffer
     *                  to check, starting at <i>offset</i>.
     * @return the index into the <i>in</i> buffer indicating where the
     *                  padding starts.
     */
    protected int engineUnpad (byte[] in, int offset, int length) {
        int i = offset + length - 1;
        while (i > 0 && in[i] == 0) i--;
        return i < 0 ? 0 : i;
    }

    /**
     * <b>SPI</b>: Returns true if <i>size</i> is a valid block size (in
     * bytes) for this algorithm.
     * <p>
     * For OneAndZeroes padding, all sizes are valid.
     */
    protected boolean engineIsValidBlockSize(int size) { return true; }
}
