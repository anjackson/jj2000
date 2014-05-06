// $Id: PaddingScheme.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: PaddingScheme.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.8  2000/08/17 11:35:25  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.7  1997/12/07 07:39:45  hopwood
// + Trivial changes.
//
// Revision 1.6  1997/12/01 03:37:27  hopwood
// + Committed changes below.
//
// Revision 1.5.1  1997/11/30  hopwood
// + Made output of toString more concise.
//
// Revision 1.5  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 1.4.1  1997/11/18  David Hopwood
// + Implement java.security.Parameterized.
//
// Revision 1.4  1997/11/07 14:32:47  raif
// *** empty log message ***
//
// Revision 1.3  1997/11/07 05:53:27  raif
// + Changes to unpad().
//
// Revision 1.2  1997/11/04 19:33:31  raif
// + Changed back the pad() method semantics to conform with Sun's JCE.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.5  1997/10/26  David Hopwood
// + Added getInstance methods.
// + Added getBlockSize() method.
// + Assume that padding always extends the plaintext to the next block
//   (therefore the enginePadLength method is not needed).
//   This greatly simplifies the treatment of padding in Cipher,
//   CipherInputStream, and CipherOutputStream.
// + Documented the above change.
//
// Revision 0.1.0.4  1997/08/17  David Hopwood
// + Various simple fixes (1.0.3 did not compile).
//
// Revision 0.1.0.3  1997/08/09  David Hopwood
// + Changed to extend the deprecated interface Padding.
//
// Revision 0.1.0.2  1997/08/07  David Hopwood
// + Deleted unnecessary methods.
//
// Revision 0.1.0.1  1997/07/20  R. Naffah
// + Added protected variable 'blockSize' to handle padding schemes
//   with unlimited/undefined blocking size.
// + Added setBlockSize() method with functionality similar to
//   setCipher() in Mode.
// + Changed the signatures of most of the methods to use this new
//   protected variable.
// + Added paddingScheme() method, deprecated it and made it call
//   the new getAlgorithm().
// + Added padLength() method and enginePadLength() method with a
//   default implementation.
//
// Revision 0.1.0.0  1997/07/??  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

/**
 * This class is extended by classes that provide a general-purpose
 * padding scheme, such as the ones described in PKCS #5 or in RFC 1423
 * (PEM).
 * <p>
 * For simplicity, an assumption is made that padding schemes always
 * extend the plaintext to the next block boundary. That is, the input
 * to the padding algorithm always has a length between 0 and
 * <code>blockSize</code>-1, and the output always has length
 * <code>blockSize</code>.
 * <p>
 * <strong><a href="../guide/ijce/JCEDifferences.html">This class
 * is not supported in JavaSoft's version of JCE.</a></strong>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   IJCE 1.0.1
 */
public abstract class PaddingScheme
extends IJCE_Traceable
implements Parameterized, Padding
{

// Variables
//...........................................................................

    private String algorithm;
    protected int blockSize;


// Constructor
//...........................................................................

    /**
     * Constructor for a PaddingScheme. This constructor is only for use 
     * by subclasses; applications cannot call it directly.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#PaddingScheme">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of PaddingScheme algorithm names.
     *
     * @param algorithm the standard string name of the algorithm.
     * @exception NullPointerException if algorithm == null
     */
    protected PaddingScheme(String algorithm) {
        super("PaddingScheme");
        if (algorithm == null)
            throw new NullPointerException("algorithm == null");
        this.algorithm = algorithm;
    }


// JCE methods
//...........................................................................

    /**
     * Generates a PaddingScheme object that implements the algorithm
     * requested, as available in the environment.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#PaddingScheme">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of PaddingScheme algorithm names.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @return the new PaddingScheme object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available in the environment.
     */
    public static PaddingScheme getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        return (PaddingScheme) (IJCE.getImplementation(algorithm,
            "PaddingScheme"));
    }

    /**
     * Generates a PaddingScheme object implementing the specified
     * algorithm, as supplied from the specified provider, if such an
     * algorithm is available from the provider.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#PaddingScheme">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of PaddingScheme algorithm names.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @param  provider     the string name of the provider.
     * @return the new KeyGenerator object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available from the provider.
     * @exception NoSuchProviderException if the provider is not
     *                      available in the environment.
     */
    public static PaddingScheme getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {
        return (PaddingScheme) (IJCE.getImplementation(algorithm, provider,
            "PaddingScheme"));
    }

    /**
     * Gets the standard names of all PaddingSchemes implemented by a
     * provider.
     */
    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "PaddingScheme");
    }

    /**
     * Gets the standard names of all PaddingSchemes implemented by any
     * installed provider. Algorithm names are not duplicated if
     * they are supported by more than one provider.
     * The built-in PaddingScheme "NONE" is included.
     */
    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("PaddingScheme");
    }

    /**
     * Returns the standard name of the padding scheme implemented.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#PaddingScheme">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of PaddingScheme algorithm names.
     *
     * @return the standard name of the padding scheme, such as "PKCS#5".
     */
    public final String getAlgorithm() { return algorithm; }

    /** Returns the block size for this padding scheme. */
    public final int getBlockSize() { return blockSize; }


// Padding interface methods
//...........................................................................

    /**
     * Pads a given array of bytes. The padding is written to the same buffer
     * that is used for input (<i>in</i>). When this method returns, the padded
     * output will be stored at
     * <code>in[offset .. offset+length+padLength(length)-1]</code>.
     * <p>
     * The <i>in</i> array should be long enough to accomodate the padding.
     * <p>
     * The return value is the number of bytes written, <em>not</em> the total
     * length of the padded block.
     *
     * @param  in       the buffer containing the input.
     * @param  offset   the offset of the bytes to be padded.
     * @param  length   the number of bytes from the <i>in</i> buffer,
     *                  starting at <i>offset</i>, that need to be padded.
     * @return the number of padding bytes written to out.
     * @exception ArrayIndexOutOfBoundsException if offset < 0 || length < 0 ||
     *                  (long)offset + length + padLength(length) > in.length
     */
    public final int pad(byte[] in, int offset, int length) {
        if (offset < 0 || length < 0)
            throw new ArrayIndexOutOfBoundsException("offset < 0 || length < 0");
        int size = blockSize;
        int skip = length - (length % size);
        if ((long)offset + skip + size > in.length)
            throw new ArrayIndexOutOfBoundsException(
                "(long)offset + length + padLength(length) > in.length");
        offset += skip;
        length -= skip;
        if (tracing) traceMethod("enginePad(<" + in + ">, " + offset + ", " + length + ")");
        int result = enginePad(in, offset, length);
        if (tracing) traceResult(result);
        return result;
    }

    /**
     * Returns the increase in size that a padding operation would cause on
     * input data of a given length. This is always
     * <code>blockSize - (length % blockSize)</code>.
     *
     * @param  length   the length of the data to be padded.
     * @return the increase in size that a padding operation would cause on
     *                  input of the specified length.
     */
    public final int padLength(int length) {
        return blockSize - (length % blockSize);
    }

    /**
     * Given the specified subarray of bytes that includes padding bytes,
     * returns the index indicating where padding starts.
     * <p>
     * <i>length</i> must be at least <code>blockSize</code>.
     *
     * @param  in       the buffer containing the bytes.
     * @param  offset   the offset into the <i>in</i> buffer of the
     *                  first byte in the block.
     * @param  length   the total length in bytes of the blocks to be
     *                  unpadded.
     * @return the index into the <i>in</i> buffer indicating where the
     *                  padding starts.
     * @exception ArrayIndexOutOfBoundsException if offset < 0 || length < 0 ||
     *                  (long)offset + length > in.length
     * @exception IllegalBlockSizeException if length < blockSize
     */
    public final int unpad(byte[] in, int offset, int length) {
        if (length == 0) return 0;
        if (offset < 0 || length < 0 || (long)offset + length > in.length)
            throw new ArrayIndexOutOfBoundsException(
                "offset < 0 || length < 0 || (long)offset + length > in.length");
// this is taken care of in Cipher.updateInternal --RSN
//        int size = blockSize;
//        if (length < size)
//            throw new IllegalBlockSizeException("length < blockSize");
//        offset += length - size;
//        if (tracing) traceMethod("engineUnpad(<" + in + ">, " + offset + ", " + size + ")");
        if (tracing) traceMethod("engineUnpad(<" + in + ">, " + offset + ", " + length + ")");
//        int result = engineUnpad(in, offset, size);
        int result = engineUnpad(in, offset, length);
        if (tracing) traceResult(result);
        return result;
    }

    /**
     * Returns the standard name of the padding scheme implemented.
     * <p>
     * See <a href="../guide/ijce/Algorithms.html#PaddingScheme">
     * <cite>International JCE Standard Algorithm Names</cite></a> for a list
     * of PaddingScheme algorithm names.
     *
     * @return the standard name of the padding scheme.
     * @deprecated Use getAlgorithm() instead.
     */
    public final String paddingScheme() { return algorithm; }

    /**
     * Sets the specified algorithm parameter to the specified value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string identifier of the parameter.
     * @param  value    the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this padding scheme implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set.
     * @exception InvalidParameterTypeException if value is the wrong type
     *                  for this parameter.
     */
    public void setParameter(String param, Object value)
    throws NoSuchParameterException, InvalidParameterException,
           InvalidParameterTypeException {
        if (param == null) throw new NullPointerException("param == null");
        engineSetParameter(param, value);
    }

    /**
     * Gets the value of the specified algorithm parameter.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to get the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter.
     * @return the object that represents the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this padding scheme implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    public Object getParameter(String param)
    throws NoSuchParameterException, InvalidParameterException {
        if (param == null) throw new NullPointerException("param == null");
        return engineGetParameter(param);
    }

    /**
     * Returns a clone of this cipher.
     *
     * @exception CloneNotSupportedException if the cipher is not cloneable.
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }

    public String toString() {
        return "PaddingScheme [" + getAlgorithm() + "]";
    }


// SPI methods
//...........................................................................

    /**
     * Sets the <code>blockSize</code> variable for this instance.
     * <p>
     * Subclasses that override this method (to do initialization that
     * depends on <code>blockSize</code> being set) should call
     * <code>super.engineSetBlockSize(size)</code> first.
     *
     * @exception IllegalBlockSizeException if size < 1 || !engineIsValidBlockSize(size)
     */
    protected void engineSetBlockSize(int size) {
        if (size < 1 || !engineIsValidBlockSize(size))
            throw new IllegalBlockSizeException(getAlgorithm() + ": " +
                size + " is not a valid block size");
        blockSize = size;
    }

    /**
     * <b>SPI</b>: Pads a single incomplete block.
     * <p>
     * The padding is written to the same buffer that is used for input
     * (<i>in</i>). When this method returns, the padded block should be stored at
     * <code>in[offset .. offset+blockSize-1]</code>.
     * <p>
     * <i>in</i> will be long enough to accomodate the padding. <i>length</i> is
     * guaranteed to be in the range 0 .. <code>blockSize</code>-1.
     *
     * @param  in       the buffer containing the incomplete block.
     * @param  offset   the offset into the <i>in</i> buffer of the block.
     * @param  length   the number of bytes from the <i>in</i> buffer,
     *                  starting at <i>offset</i>, that need to be unpadded.
     * @return the number of padding bytes written.
     */
    protected abstract int enginePad(byte[] in, int offset, int length);

    /**
     * <b>SPI</b>: Given the specified subarray of bytes that includes
     * padding bytes, returns the index indicating where padding starts.
     * <p>
     * <i>length</i> is guaranteed to be a non-negative multiple of
     * <code>blockSize</code>.
     *
     * @param  in       the buffer containing the bytes.
     * @param  offset   the offset into the <i>in</i> buffer of the
     *                  first byte to be unpadded.
     * @param  length   the total length in bytes of the blocks to be
     *                  unpadded.
     * @return the index into the <i>in</i> buffer indicating
     *                  where the padding starts.
     */
    protected abstract int engineUnpad(byte[] in, int offset, int length);

    /**
     * <b>SPI</b>: Returns true if <i>size</i> is a valid block size (in
     * bytes) for this scheme.
     * <p>
     * The default implementation always returns true.
     */
    protected boolean engineIsValidBlockSize(int size) { return true; }

    /**
     * <b>SPI</b>: Sets the specified algorithm parameter to the specified
     * value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * The default implementation always throws a NoSuchParameterException.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter. 
     * @param  value    the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this padding scheme implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set.
     * @exception InvalidParameterTypeException if value is the wrong type
     *                  for this parameter.
     */
    protected void engineSetParameter(String param, Object value)
    throws NoSuchParameterException, InvalidParameterException,
           InvalidParameterTypeException {
        throw new NoSuchParameterException(getAlgorithm() + ": " + param);
    }

    /**
     * <b>SPI</b>: Gets the value of the specified algorithm parameter.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to get the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     * <p>
     * The default implementation always throws a NoSuchParameterException.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  param    the string name of the parameter. 
     * @return the object that represents the parameter value.
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this padding scheme implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    protected Object engineGetParameter(String param)
    throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(getAlgorithm() + ": " + param);
    }
}
