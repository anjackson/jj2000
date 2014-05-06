// $Log: Padding.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:25  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.0  1997/10/26  David Hopwood
// + Start of history.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
*/

package xjava.security;

/**
 * Padding is the interface defined by JavaSoft's JCE to be implemented
 * by classes that provide a general-purpose padding scheme. It is not
 * used in IJCE, for several reasons:
 * <ul>
 *   <li> Interfaces are more difficult to extend with new functionality
 *        than abstract classes (for an abstract class, new methods can
 *        be given default implementations that are defined in terms of
 *        existing methods).
 *        <p>
 *   <li> An abstract class can enforce that SPI methods are called
 *        with valid arguments; an interface cannot.
 *        <p>
 *   <li> The Padding interface does not support arbitrary block sizes. Each
 *        padding scheme must be defined for a specific block size.
 *        <p>
 *   <li> The documentation for Padding was not clear enough to re-implement
 *        accurately.
 *        <p>
 *   <li> The Security.getImpl method in JavaSoft's implementation of
 *        JCE does not work correctly for interfaces.
 * </ul>
 * <p>
 * This interface is included in IJCE only so that classes that use it
 * will not fail with an unrecoverable linking error. The abstract class
 * PaddingScheme should be used instead.
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   IJCE 1.0.0
 * @deprecated
 * @see java.security.PaddingScheme
 */
public interface Padding {
    /**
     * Pads a given array of bytes.  The padded bytes are written to the
     * same buffer that is used for input ("in").
     *
     * @param in  the buffer containing the bytes.
     * @param off the offset into the <code>in</code> buffer of the
     *          first byte in the group of bytes to be padded.
     * @param len the number of bytes from the <code>in</code> buffer,
     *          starting at <code>off</code>, that need to be padded.
     * @return the number of padding bytes written.
     *
     * @see java.security.Cipher#blockSize
     */
    int pad(byte in[], int off, int len);

    /**
     * Given the specified subarray of bytes that includes padding
     * bytes, returns the index indicating where padding starts.
     *
     * @param in  the buffer containing the bytes.
     * @param off the starting offset into the <code>in</code> buffer of
     *          the bytes to be checked to determine where padding starts.
     * @param len the number of bytes from the <code>in</code> buffer
     *          to check, starting at offset <code>off</code>.
     * @return the index into the <code>in</code> buffer indicating
     *          where the padding starts.
     */
    int unpad(byte in[], int off, int len);

    /**
     * Returns the absolute value of the increase in size that a
     * padding operation would cause on input data of a given length.
     *
     * @param len the length of the data which is to be padded.
     * @return the absolute value of the increase in size that a padding
     *          operation would cause on input data of the specified length.
     */
    int padLength(int len);

    /**
     * Returns the standard name of the padding scheme implemented.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#PaddingScheme">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of PaddingScheme algorithm names.
     *
     * @return the standard name of the padding scheme, such as "PKCS#5".
     */
    String paddingScheme();
}
