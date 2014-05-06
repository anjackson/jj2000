// This file is currently unlocked (change this line if you lock the file)
//
// $Log: VariableLengthDigest.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.3  2000/08/17 11:35:25  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.2  1997/12/07 07:39:45  hopwood
// + Trivial changes.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.0  1997/08/25  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

/**
 * This interface defines the additional API for MessageDigest classes that
 * have a variable-length output.
 * <p>
 * The output length should be set before updating any input, because when
 * <code>setDigestLength</code> is called, the digest will be reset. The length
 * is always an exact number of bytes (i.e. a multiple of 8 bits).
 * <p>
 * <strong><a href="../guide/ijce/JCEDifferences.html">This interface
 * is not supported in JavaSoft's version of JCE.</a></strong>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   IJCE 1.0.1
 */
public interface VariableLengthDigest
{
    /**
     * Sets the length of the digest output in bytes, and resets the digest.
     *
     * @param  nbytes   the new length in bytes.
     */
    void setDigestLength(int nbytes);
}
