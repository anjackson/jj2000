// $Id: IllegalBlockSizeException.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: IllegalBlockSizeException.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.3  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.2  1997/12/09 05:17:28  raif
// *** empty log message ***
//
// 1997.12.09 --RSN
// + documentation.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/08/09  David Hopwood
// + Added (int blockSize, int dataSize, String message) constructor.
// + Deprecated blockSize and dataSize as public fields.
//
// Revision 0.1.0.0  1997/?/0?  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

/**
 * This exception is thrown when an incorrect block size is processed
 * through a cipher.
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
 */
public class IllegalBlockSizeException extends RuntimeException {
    /**
     * The block size of the cipher.
     * @deprecated Use getBlockSize().
     */
    public int blockSize;

    /**
     * The size of the data passed to the cipher.
     * @deprecated Use getDataSize().
     */
    public int dataSize;

    /**
     * Gets the block size of the cipher (or 0 if this was not set).
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Gets the size of the data passed to the cipher (or 0 if this
     * was not set).
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This method
     * is not supported in JavaSoft's version of JCE.</a></strong>
     */
    public int getDataSize() {
        return dataSize;
    }

    /**
     * Constructs an IllegalBlockSizeException with the specified
     * detail message. A detail message is a String that describes
     * this particular exception.
     *
     * @param  message      the detail message.
     */
    public IllegalBlockSizeException(String message) {
        super(message);
    }

    /**
     * Constructs an IllegalBlockSizeException with the specified
     * block size and illegal data size.
     * <p>
     * The detail message is set to <code>"blockSize = " +
     * blockSize + ", dataSize = " + dataSize</code>.
     *
     * @param  blockSize    the block size of the cipher.
     * @param  dataSize     the illegal size of the data passed to the cipher.
     */
    public IllegalBlockSizeException(int blockSize, int dataSize) {
        super("blockSize = " + blockSize + ", dataSize = " + dataSize);
        this.blockSize = blockSize;
        this.dataSize = dataSize;
    }

    /**
     * Constructs an IllegalBlockSizeException with the specified
     * block size, illegal data size, and detail message.
     * <p>
     * <strong><a href="../guide/ijce/JCEDifferences.html">This constructor
     * is not supported in JavaSoft's version of JCE.</a></strong>
     *
     * @param  blockSize    the block size of the cipher.
     * @param  dataSize     the illegal size of the data passed to the cipher.
     * @param  message      the detail message.
     */
    public IllegalBlockSizeException(int blockSize, int dataSize,
                                     String message) {
        super(message);
        this.blockSize = blockSize;
        this.dataSize = dataSize;
    }
}
