// $Id: BigRegister.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: BigRegister.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.2  1999/06/30 22:44:36  edwin
// synchronized classes are evil
//
// Revision 1.1.1.1  1997/11/20 22:05:46  hopwood
// + Moved BigRegister and TrinomialLFSR here from the cryptix.util package.
//
// Revision 1.1.1  1997/11/20  David Hopwood
// + Renamed equals to isSameValue.
// + Moved to cryptix.util.math package.
//
// Revision 1.1  1997/11/07 05:53:26  raif
// *** empty log message ***
//
// Revision 0.1.3  1997/10/01 09:04:24  raif
// *** empty log message ***
//
// Revision 0.1.1  1997/09/25  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */

package cryptix.util.math;

import cryptix.util.core.ArrayUtil;

import java.io.Serializable;
import java.security.SecureRandom;

/**
 * Utility class to manage a large bit-register of a given size as a
 * <b>mutable</b> object.
 * <p>
 * The bits are indexed from <i>0</i> (rightmost) to <i><code>size</code>
 * - 1</i>, (leftmost) where <code>size</code> is this register's
 * designated (at instantiation time) bit capacity.
 * <p>
 * <b>Copyright</b> &copy; 1995-1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 */
public class BigRegister
implements Cloneable, Serializable
{
// Constants and variables
//.....................................................................

    /** Maximum allowed number of bits in a <code>BigRegister</code> object. */
    public static final int MAXIMUM_SIZE = 4096;

    // Number of bits in the binary representaion of x, 0 <= x <= 127
    private static final byte[] log2x = {
        0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
        5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7};

    // index of highest set bit in a 4-bit value
    private static final byte[] high = {
        0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3};

    // index of lowest set bit in a 4-bit value
    private static final byte[] low = {
        0, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0};

    private static final String[] binaryDigits = {
        "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111",
        "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};

    private static final String m_1 = "size < 2";
    private static final String m_2 = "size > MAXIMUM_SIZE";

    private static final SecureRandom prng = new SecureRandom();
    private byte[] bits;
    private int size;

    private static final long serialVersionUID = 2535877383275048954L;


// Constructors
//.....................................................................

    /**
     * Instantiate a <code>BigRegister</code> of a given <code>size</code>
     * with all its bits set to zeroes.
     *
     * @param  size  Number of meaningful bits in <code>this</code> object.
     * @exception  IllegalArgumentException  If the argument is less than
     *         2 or greater than the maximum allowed value.
     * @see    #MAXIMUM_SIZE
     */
    public BigRegister (int size) {
        if (size < 2) throw new IllegalArgumentException(m_1);
        if (size > MAXIMUM_SIZE) throw new IllegalArgumentException(m_2);
        this.size = size;
        bits = new byte[(size + 7) / 8];    // multiple of 8
    }

    // Constructor to implement cloneability
    private BigRegister (BigRegister r) {
        this.size = r.size;
        this.bits = (byte[]) (r.bits.clone());
    }


// Cloneable method implementation
//...........................................................................

    /** Return a reference to a duplicate of <code>this</code>. */
    public synchronized Object clone() { return new BigRegister(this); }


// Bit logical operators
//.....................................................................

    /**
     * Compute <code>this &amp;= source</code>.
     *
     * @exception  IllegalArgumentException  If the argument is of
     *     different <code>size</code> than <code>this</code>.
     */
    public synchronized void and (BigRegister source) {
        if (size != source.size) throw new IllegalArgumentException();
        for (int i = 0; i < bits.length; i++)
            bits[i] &= source.bits[i];
    }

    /**
     * Compute <code>this &amp;= ~source</code>.
     *
     * @exception  IllegalArgumentException  If the argument is of
     *     different <code>size</code> than <code>this</code>.
     */
    public synchronized void andNot (BigRegister source) {
        if (size != source.size) throw new IllegalArgumentException();
        for (int i = 0; i < bits.length; i++)
            bits[i] &= ~source.bits[i];
    }

    /**
     * Compute <code>this |= source</code>.
     *
     * @exception  IllegalArgumentException  If the argument is of
     *     different <code>size</code> than <code>this</code>.
     */
    public synchronized void or (BigRegister source) {
        if (size != source.size) throw new IllegalArgumentException();
        for (int i = 0; i < bits.length; i++)
            bits[i] |= source.bits[i];
        pad();
    }

    /**
     * Compute <code>this = ~this</code>.
     */
    public synchronized void not () {
        for (int i = 0; i < bits.length; i++)
            bits[i] = (byte) ~bits[i];
        pad();
    }

    /**
     * Compute <code>this ^= source</code>.
     *
     * @exception  IllegalArgumentException  If the argument is of
     *     different <code>size</code> than <code>this</code>.
     */
    public synchronized void xor (BigRegister source) {
        if (size != source.size) throw new IllegalArgumentException();
        for (int i = 0; i < bits.length; i++)
            bits[i] ^= source.bits[i];
        pad();
    }


// Shift operators
//.....................................................................

    /**
     * Execute a left shift of <code>this BigRegister</code>'s contents
     * by a given number of bit positions. If the number is negative, a
     * right shift is executed.
     *
     * @param  n  Number of bit positions to shift by. If this value
     *         is negative then a shift in the opposite direction is
     *         executed.
     */
    public synchronized void shiftLeft (int n) {
        if (n == 0) return;
        if (n < 0) {
            shiftRight(-n);
            return;
        }
        if (n >= size) {
            reset();
            return;
        }
        int start = lowestSetBit();
        if (start == -1) return;                // all zeroes
        if (start >= size - n) {                // all 1s will disappear
            reset();
            return;
        }
        start = n / 8;
        int offset = n % 8;
        int length = bits.length;
        byte[] result = new byte[length];

        if (offset == 0)
            System.arraycopy(bits, 0, result, start, length - start);
        else {
            int offsetBar = 8 - offset;
            for (int i = start, j = 0; i < length; i++, j++)
                result[i] = (byte) (
                    bits[j] << offset |
                    (j == 0 ? 0 : (bits[j - 1] & 0xFF) >>> offsetBar)
                );
        }
        bits = result;
        pad();
    }

    /**
     * Execute a right shift of <code>this BigRegister</code>'s contents
     * by a given number of bit positions. If the number is negative, a
     * left shift is executed.
     *
     * @param  n  Number of bit positions to shift by. If this value
     *         is negative then a shift in the opposite direction is
     *         executed.
     */
    public synchronized void shiftRight (int n) {
        if (n == 0) return;
        if (n < 0) {
            shiftLeft(-n);
            return;
        }
        if (n >= size) {
            reset();
            return;
        }
        int start = highestSetBit();            // index of last bit to move
        if (start < 0) return;                    // all zeroes anyway
        if (start < n) {                        // all 1s will go
            reset();
            return;
        }
        start = n / 8;
        int offset = n % 8;
        int length = bits.length;
        byte[] result = new byte[length];

        if (offset == 0)
            System.arraycopy(bits, start, result, 0, length - start);
        else
            for (int i = 0, j = start; i < length && j < length; i++, j++)
                result[i] = (byte) (
                    ((j == length - 1 ? 0 : bits[j + 1] << 8) | (bits[j] & 0xFF)) >>> offset
                );
        bits = result;
        pad();
    }


// Rotation operators
//.....................................................................

    /**
     * Circular left shift over the <code>size</code> of <code>this</code>
     * register.
     * <p>
     * Effectively compute <code>this = this << n | this >> (size - n)</code>.
     * <p>
     * If the number of positions to rotate by is negative, then a
     * right instead of left rotation is executed.
     */
    public synchronized void rotateLeft (int n) {
        n = n % size;
        if (n == 0) return;
        if (n < 0)
            rotateRight(-n);
        else {
            BigRegister same = (BigRegister) clone();
            same.shiftRight(size - n);
            shiftLeft(n);
            or(same);
        }
    }

    /**
     * Circular right shift over the <code>size</code> of <code>this</code>
     * register.
     * <p>
     * Effectively compute <code>this = this >> n | this << (size - n)</code>.
     * <p>
     * If the number of positions to rotate by is negative, then a
     * left instead of right rotation is executed.
     */
    public synchronized void rotateRight (int n) {
        n = n % size;
        if (n == 0) return;
        if (n < 0)
            rotateLeft(-n);
        else {
            BigRegister same = (BigRegister) clone();
            same.shiftLeft(size - n);
            shiftRight(n);
            or(same);
        }
    }

    /** Invert the bit order of the current contents of <code>this</code>. */
    public synchronized void invertOrder () {
        byte[] result = new byte[bits.length];
        for (int i = 0, j = size - 1; i < size; i++, j--)
            if (testBit(i)) result[j / 8] |= 1 << (j % 8);
        bits = result;
    }


// Test and comparison operators
//.....................................................................

    /**
     * Return true if the designated bit is set or false otherwise.
     *
     * @param  n  Index of the bit to test.
     * @return true if the designated bit is set or false otherwise.
     */
    public synchronized boolean testBit (int n) {
        if (n < 0 || n > size) throw new IllegalArgumentException();
        return (bits[n / 8] & (1 << (n % 8))) != 0;
    }

    /**
     * Return true if the parameters of the BigRegister <i>x</i>
     * (<code>size</code> and <code>bits</code>) are equal to this one;
     * false otherwise.
     * <p>
     * NOTE: the <code>equals</code> method is not used, because this is
     * a mutable object (see the requirements for equals in the Java Language
     * Spec).
     *
     * @param  x  BigRegister to test for equality.
     * @return true iff x has equal <code>size</code> and contents.
     */
    public synchronized boolean isSameValue (BigRegister x) {
        if (x.size != size) return false;
        return ArrayUtil.areEqual(bits, x.bits);
    }

    /**
     * Compare <code>this BigRegister</code>'s contents to that of the
     * argument, returning -1, 0 or 1 for less than, equal to, or greater
     * than comparison result.
     *
     * @param  x  A <code>BigRegister</code> object to compare to.
     * @return -1, 0, +1 If the contents of <code>this</code> object are
     *         respectively less than, equal to, or greater than those
     *         of the argument.
     */
    public synchronized int compareTo (BigRegister x) {
        if (size > x.size) return 1;
        if (size < x.size) return -1;
        return ArrayUtil.compared(bits, x.bits, true);
    }


// Setters and getters
//.....................................................................

    /**
     * Set the bit at the designated position to 1.
     *
     * @param  n  The bit position to alter.
     * @exception  IllegalArgumentException  If the argument would
     *         cause an <code>ArrayOutOfBOundsException</code> while
     *         accessing the bits array.
     */
    public synchronized void setBit (int n) {
        if (n < 0 || n > size) throw new IllegalArgumentException();
        bits[n / 8] |= 1 << (n % 8);
    }

    /**
     * Set <code>count</code> bits starting at offset <code>n</code>
     * to a given <code>value</code>.
     *
     * @param  n      The index of the first bit to set.
     * @param  count  Number of bits to set.
     * @param  value  New bits value, right justified in a <code>long</code>.
     * @exception  IllegalArgumentException  If any of the arguments would
     *         cause an <code>ArrayOutOfBOundsException</code> while
     *         accessing the bits array, or <code>count</code> is &lt; 1 or
     *         &gt; 64.
     */
    public synchronized void setBits (int n, int count, long value) {
        if (n < 0 || n > size || count < 1 || count > 64 || n + count > size)
            throw new IllegalArgumentException();
        for (int i = 0, j = n; i < count; i++, j++) {
            if ((value & 0x01) == 1) bits[j / 8] |= 1 << (j % 8); // setBit(j);
            value >>>= 1;
        }
    }

    /**
     * Set the bit at the designated position to 0; ie. clear it.
     *
     * @param  n  The bit position to alter.
     * @exception  IllegalArgumentException  If the argument would
     *         cause an <code>ArrayOutOfBOundsException</code> while
     *         accessing the bits array.
     */
    public synchronized void clearBit (int n) {
        if (n < 0 || n > size) throw new IllegalArgumentException();
        bits[n / 8] &= ~(1 << (n % 8));
    }

    /**
     * Flip the value of the bit at the designated position.
     *
     * @param  n  The bit position to alter.
     * @exception  IllegalArgumentException  If the argument would
     *         cause an <code>ArrayOutOfBOundsException</code> while
     *         accessing the bits array.
     */
    public synchronized void flipBit (int n) {
        if (n < 0 || n > size) throw new IllegalArgumentException();
        bits[n / 8] ^= 1 << (n % 8);
    }

    /**
     * Return 1 or 0 if the designated bit was set or cleared respectively.
     *
     * @param  n  The index of the bit to retrieve.
     * @exception  IllegalArgumentException  If the argument would
     *         cause an <code>ArrayOutOfBOundsException</code> while
     *         accessing the bits array.
     */
    public synchronized int getBit (int n) {
        if (n < 0 || n > size) throw new IllegalArgumentException();
        return ((bits[n / 8] & 0xFF) >> (n % 8)) & 0x01;
    }

    /**
     * Return <code>count</code> bits starting at offset <code>n</code>
     * framed in a <code>long</code>, right justified and left padded
     * with binary zeroes.
     *
     * @param  n      The index of the first bit to retrieve.
     * @param  count  Number of bits to retrieve.
     * @return Right justified <code>count</code> bits starting from bit
     *         index <code>n</code> in a java <code>long</code>.
     * @exception  IllegalArgumentException  If any of the arguments
     *         would cause an <code>ArrayOutOfBOundsException</code> while
     *         accessing the bits array, or <code>count</code> is &lt; 1 or
     *         &gt; 64.
     */
    public synchronized long getBits (int n, int count) {
        if (n < 0 || n > size || count < 1 || count > 64 || n + count > size)
            throw new IllegalArgumentException();
        long result = 0L;
        for (int i = 0, j = n + count - 1; i < count; i++, j--)
            result = result << 1 | (((bits[j / 8] & 0xFF) >> (j % 8)) & 0x01);
        return result;
    }

    /**
     * Return the rightmost byte value in <code>this BigRegister</code>.
     *
     * @return The rightmost byte value in <code>this</code>.
     */
    public synchronized int byteValue () { return bits[0] & 0xFF; }

    /**
     * Return the rightmost 32-bit value in <code>this BigRegister</code>
     * as an <code>int</code>.
     *
     * @return The rightmost 32-bit value in <code>this</code> as an
     *         <code>int</code>.
     */
    public synchronized int intValue () {
        int offset = 0;
        int result = bits[offset++] & 0xFF;
        try {
            result |= (bits[offset++] & 0xFF) <<  8 |
                      (bits[offset++] & 0xFF) << 16 |
                      (bits[offset  ] & 0xFF) << 24;
        } catch (ArrayIndexOutOfBoundsException e) {} // when bits.length < 4
        return result;
    }

    /**
     * Return the rightmost 64-bit value in <code>this BigRegister</code>
     * as a <code>long</code>.
     *
     * @return The rightmost 64-bit value in <code>this</code> as a <code>
     *         long</code>.
     */
    public synchronized long longValue () {
        int offset = 0;
        long result = bits[offset++] & 0xFFL;
        try {
            result |= (bits[offset++] & 0xFFL) <<  8 |
                      (bits[offset++] & 0xFFL) << 16 |
                      (bits[offset++] & 0xFFL) << 24 |
                      (bits[offset++] & 0xFFL) << 32 |
                      (bits[offset++] & 0xFFL) << 40 |
                      (bits[offset++] & 0xFFL) << 48 |
                      (bits[offset  ] & 0xFFL) << 56;
        } catch (ArrayIndexOutOfBoundsException e) {} // when bits.length < 8
        return result;
    }

    /**
     * Return a <code>BigRegister</code>, of the same <code>size</code>
     * as <code>this</code> set to the specified value.
     *
     * @return A <code>BigRegister</code>, of the same <code>size</code>
     *         as <code>this</code> set to the specified value.
     */
    public synchronized BigRegister valueOf (long n) {
        BigRegister result = new BigRegister(size);
        int limit = Math.min(8, bits.length);
        for (int i = 0; i < limit; i++)
            result.bits[i] = (byte) (n >>> (8 * i));
        result.pad();
        return result;
    }

    /** Reset to zeroes all <code>this BigRegister</code>'s bits. */
    public synchronized void reset () { ArrayUtil.clear(bits); }

    /**
     * Fill <code>this BigRegister</code> object with random data
     * generated from the default source.
     */
    public synchronized void atRandom () { atRandom(prng); }

    /**
     * Fill <code>this BigRegister</code> object with random data
     * generated from a designated source.
     */
    public synchronized void atRandom (SecureRandom source) {
        source.nextBytes(bits);
        pad();
    }

    /**
     * Copy the argument's value into <code>this</code>.
     *
     * @exception  IllegalArgumentException  If the argument is of
     *     different <code>size</code> than <code>this</code>.
     */
    public synchronized void load (BigRegister source) {
        if (size != source.size) throw new IllegalArgumentException();
        System.arraycopy(source.bits, 0, bits, 0, bits.length);
    }

    /**
     * Copy the bit values from a byte array into <code>this</code>.
     * Byte array order is assumed to have its Least Significant Byte
     * (LSB) at index position 0. This format mirrors that of the output
     * returned by the <code>toByteArray()</code> method.
     * <p>
     * Bits unprovided for in the <code>source</code> array are cleared.
     * It is a more tolerant way of initialising a register than that
     * obtained by invoking the same method with a <code>BigRegister</code>
     * argument.
     *
     * @param  source  The source bits organised in a byte array with
     *         their LSB at index 0.
     * @exception  IllegalArgumentException  If the argument is of
     *         greater <code>size</code> than <code>this</code>.
     * @see    #toByteArray
     */
    public synchronized void load (byte[] source) {
        int length = source.length;
        int limit = bits.length;
        if (length > limit) throw new IllegalArgumentException();
        System.arraycopy(source, 0, bits, 0, length);
        if (length < limit)    // clear the rest
            ArrayUtil.clear(bits, length, limit - length);
        pad();
    }

    /**
     * Return a copy of <code>this BigRegister</code>'s contents in a
     * byte array with the LSB at index position 0. This format is
     * compatible with the <code>load([B)</code> method of this class.
     *
     * @return The bits of <code>this</code> in a byte array with the
     *         LSB at index position 0.
     */
    public synchronized byte[] toByteArray () { return (byte[]) (bits.clone()); }
    

// Visualisation and introspection methods
//.....................................................................

    /**
     * Return the <code>size</code> of <code>this</code> object as
     * specified at its instantiation time.
     *
     * @return The <code>size</code> of <code>this</code> object
     *         as specified at its instantiation time.
     */
    public synchronized int getSize () { return size; }

    /**
     * Return the number of bits set (to 1) in <code>this</code>.
     *
     * @return The number of set bits in <code>this</code>.
     */
    public synchronized int countSetBits () {
        int count = 0;
        int limit = bits.length;
        for (int i = 0, j; i < limit; i++) {
            j = bits[i];
            count += j < 0 ? 8 : log2x[j & 0xFF];
        }
        return count;
    }

    /**
     * Return the index of the leftmost non-zero bit in <code>this</code>.
     *
     * @return Index of the leftmost non-zero bit in <code>this</code>, or
     *         -1 if all bits are zeroes.
     */
    public synchronized int highestSetBit () {
        int i = bits.length - 1;
        while (i > 0 && bits[i] == 0) i--;
        if (bits[i] == 0) return -1;
        int b = (bits[i] >>> 4) & 0x0F;
        int j = 4;
        if (b == 0) {
            b = bits[i] & 0x0F;
            j -= 4;
        }
        j += high[b];
        return i * 8 + j;
    }

    /**
     * Return the index of the rightmost non-zero bit in <code>this</code>.
     *
     * @return Index of the rightmost non-zero bit in <code>this</code>, or
     *         -1 if all bits are zeroes.
     */
    public synchronized int lowestSetBit () {
        int i = 0;
        int limit = bits.length;
        while (i < limit && bits[i] == 0) i++;
        if (i == limit) return -1;
        int b = bits[i] & 0x0F;
        int j = 0;
        if (b == 0) {
            b = (bits[i] >>> 4) & 0x0F;
            j += 4;
        }
        j += low[b];
        return i * 8 + j;
    }

    /**
     * Return a formatted <code>String</code> representation of the binary
     * contents of <code>this</code>.
     *
     * @return A formatted string representation of the binary contents
     *         of <code>this</code>.
     */
    public synchronized String toString () {
        StringBuffer sb = new StringBuffer(8 * bits.length + 64);
        sb.append("Binary dump of a BigRegister [").append(size).append("-bit]...\n");
        sb.append("Byte #:|........|........|........|........|........|........|........|........|\n");

        int k = bits.length;    // number of bytes
        int i, b;
        int first = k-- % 8;    // number of bytes on 1st line
        String s;

        if (first != 0) {
            s = "      " + String.valueOf(bits.length);
            sb.append(s.substring(s.length() - 6)).append(':');
            for (i = 0; i < 8 - first; i++) sb.append("         ");
            for (i = 0; i < first; i++) {
                b = bits[k--] & 0xFF;
                sb.append(' ').
                append(binaryDigits[(b >>> 4) & 0x0F]).
                append(binaryDigits[b & 0x0F]);
            }
            sb.append('\n');
        }
        int lines = (k + 1) / 8;    // number of remaining 8-byte lines
        int j;
        for (i = 0; i < lines; i++) {
            s = "      " + String.valueOf(8 * (lines - i));
            sb.append(s.substring(s.length() - 6)).append(':');
            for (j = 0; j < 8; j++) {
                b = bits[k--] & 0xFF;
                sb.append(' ').
                append(binaryDigits[(b >>> 4) & 0x0F]).
                append(binaryDigits[b & 0x0F]);
            }
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }
    

// house-keeping
//.....................................................................

    // ensure that bits to the left of 'size-1' are all zeroes
    private synchronized void pad () {
        int n = 8 - (size % 8);
        if (n != 8) bits[bits.length - 1] &= 0xFF >>> n;
    }
}
