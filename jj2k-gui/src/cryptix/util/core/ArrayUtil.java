// $Id: ArrayUtil.java,v 1.1.1.1 2002/08/27 12:33:06 grosbois Exp $
//
// $Log: ArrayUtil.java,v $
// Revision 1.1.1.1  2002/08/27 12:33:06  grosbois
// Add cryptix 3.2
//
// Revision 1.6  1998/01/11 08:19:36  raif
// *** empty log message ***
//
// Revision 1.5.1  1997/12/28  raif
// + cosmetics.
//
// Revision 1.5  1997/12/11 10:05:40  ianb
// Raif fixed it!
//
// Revision 1.4  1997/12/10 12:34:39  ianb
// Tried to fix isText() - still not working properly.
//
// Revision 1.3  1997/12/10 12:11:57  ianb
// Added isText() method
//
// 1997.12.09 --Ian Brown
// + added isText() method.
//
// Revision 1.2  1997/12/07 07:33:34  hopwood
// + Reduce size of zeroes array.
//
// Revision 1.1.1.1  1997/11/20 21:04:51  hopwood
// + Moved these classes here from cryptix.core.util.*.
//
// Revision 1.1.1  1997/11/20  David Hopwood
// + Added areEqual(int[], int[]).
//
// Revision 1.1  1997/11/05 16:48:02  raif
// *** empty log message ***
//
// Revision 0.1.4  1997/10/01 09:04:24  raif
// *** empty log message ***
//
// Revision 0.1.3  1997/09/29 13:19:10  raif
// *** empty log message ***
//
// Revision 0.1.0.1  1997/09/03  R. Naffah
// + Added compared([B,[B,Z).
//
// Revision 0.1.0.0  1997/07/21  David Hopwood
// + Initial version (based on Raif Naffah's subrosa.util.Util).
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 *
 * Derived partly from code Copyright (c) 1996 Type & Graphics Pty
 * Limited.  All rights reserved.
 */

package cryptix.util.core;

/**
 * Static methods for converting between arrays of various types, for clearing
 * all or part of a byte array, and for comparing two byte arrays.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @since   Cryptix 2.2.2
 * @author  Raif Naffah
 * @author  David Hopwood
 * @author  Ian Brown
 */
public class ArrayUtil
{
    private ArrayUtil() {} // static methods only

    private static final int ZEROES_LEN = 500; // adjust for memory/speed tradeoff
    private static byte[] zeroes = new byte[ZEROES_LEN];

    /**
     * Clears a byte array to all-zeroes.
     */
    public static void clear (byte[] buf) { clear(buf, 0, buf.length); }

    /**
     * Clears <i>length</i> bytes of a byte array to zeroes, starting at
     * <i>offset</i>.
     */
    public static void clear (byte[] buf, int offset, int length) {
        if (length <= ZEROES_LEN)
            System.arraycopy(zeroes, 0, buf, offset, length);
        else {
            System.arraycopy(zeroes, 0, buf, offset, ZEROES_LEN);
            int halflength = length / 2;
            for (int i = ZEROES_LEN; i < length; i += i) {
                System.arraycopy(buf, offset, buf, offset+i,
                    (i <= halflength) ? i : length-i);
            }
        }
    }

    /**
     * Returns an int built from two shorts.
     *
     * @param s0    the least significant short
     * @param s1    the most significant short
     */
    public static int toInt(short s0, short s1) { return (s0 & 0xFFFF) | (s1 << 16); }

    /**
     * Returns a short built from two bytes.
     *
     * @param b0    the least significant byte
     * @param b1    the most significant byte
     */
    public static short toShort(byte b0, byte b1) { return (short) (b0 & 0xFF | b1 << 8); }

    /**
     * Returns a 4-byte array built from an int. The int's MSB is first
     * (big-endian order).
     */
    public static byte[] toBytes(int n) {
        byte[] buf = new byte[4];
            
        for (int i = 3; i >= 0; i--) {
            buf[i] = (byte) (n & 0xFF);
            n >>>= 8;
        }
        return buf;
    }

    /**
     * Returns a byte array built from a short array. Each short is broken
     * into 2 bytes with the short's MSB first (big-endian order).
     * <p>
     * If offset and length are omitted, the whole array is used.
     */
    public static byte[] toBytes(short[] array, int offset, int length) {
        byte[] buf = new byte[2 * length];
        int j = 0;

        for (int i = offset; i < offset + length; i++) {
            buf[j++] = (byte) ((array[i] >>> 8) & 0xFF);
            buf[j++] = (byte) (array[i] & 0xFF);
        }
        return buf;
    }
    
    public static byte[] toBytes(short[] array) { return toBytes(array, 0, array.length); }

    /**
     * Returns a short array built from a byte array. Each 2 bytes form
     * a short with the first byte as the short's MSB (big-endian order).
     * <p>
     * If offset and length are omitted, the whole array is used.
     */
    public static short[] toShorts(byte[] array, int offset, int length) {
        short[] buf = new short[length / 2];
        int j = 0;

        for (int i = offset; i < offset + length - 1; i += 2)
            buf[j++] = (short) (((array[i] & 0xFF) << 8) | (array[i + 1] & 0xFF));

        return buf;
    }

    public static short[] toShorts(byte[] array) { return toShorts(array, 0, array.length); }

    /**
     * Compares two byte arrays for equality.
     *
     * @return true if the arrays have identical contents
     */
    public static boolean areEqual(byte[] a, byte[] b) {
        int aLength = a.length;
        if (aLength != b.length) return false;

        for (int i = 0; i < aLength; i++)
            if (a[i] != b[i]) return false;

        return true;
    }

    /**
     * Compares two int arrays for equality.
     *
     * @return true if the arrays have identical contents
     */
    public static boolean areEqual(int[] a, int[] b) {
        int aLength = a.length;
        if (aLength != b.length) return false;

        for (int i = 0; i < aLength; i++)
            if (a[i] != b[i]) return false;

        return true;
    }

    /*
     * Compare two byte arrays returning -1, 0 or +1 if the first argument
     * is less than, equal to, or greater than the second one. Both arguments
     * are assumed to have the same order of byte significance.
     * <p>
     * When last argument is true, the comparison assumes the MSB (Most
     * Significant Byte) is at the highest index position, and when it's
     * false, the contrary; i.e. MSB at [0].
     *
     * @since Cryptix 2.2.2
     * @return  -1, 0 or 1 if a < b, a == b and a > b respectively.
     */
    public static int compared (byte[] a, byte[] b, boolean msbFirst) {
        int aLength = a.length;
        if (aLength < b.length) return -1;
        if (aLength > b.length) return 1;
        int b1, b2;
        if (msbFirst)
            for (int i = aLength - 1; i >= 0; i--) {
                b1 = a[i] & 0xFF;
                b2 = b[i] & 0xFF;
                if (b1 < b2) return -1;
                if (b1 > b2) return 1;
            }
        else
            for (int i = 0; i < aLength; i++) {
                b1 = a[i] & 0xFF;
                b2 = b[i] & 0xFF;
                if (b1 < b2) return -1;
                if (b1 > b2) return 1;
            }
        return 0;
    }

    /** @return true If the data in the byte array consists of just text. */
    public static boolean isText (byte[] buffer) {
        int len = buffer.length;
        if (len == 0) return false;
        for (int i = 0; i < len; i++) {
            int c = buffer[i] & 0xFF;
            if (c < '\u0020' || c > '\u007F')
                switch (c) {  // control chars that are allowed in text files
                    case '\u0007':  // BEL
                    case '\u0008':  // BS
                    case '\t':  // HT
                    case '\n':      // LF
                    case '\u000B':  // VT
                    case '\u000C':  // FF
                    case '\r':      // CR
                    case '\u001A':  // EOF
                    case '\u001B':  // ESC
                    case '\u009B':  // CSI
                        continue;
                    default: // anything else, isn't.
                        return false;
                }
        }
        return true;
    }
}
