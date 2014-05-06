// $Id: BI.java,v 1.1.1.1 2002/08/27 12:33:06 grosbois Exp $
//
// $Log: BI.java,v $
// Revision 1.1.1.1  2002/08/27 12:33:06  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/11/20 21:04:51  hopwood
// + Moved these classes here from cryptix.core.util.*.
//
// Revision 1.1.1  1997/11/16  David Hopwood
// + Make dumpString return "null" when given a null BigInteger.
// + Formatting changes.
//
// Revision 1.1  1997/11/05 16:48:02  raif
// *** empty log message ***
//
// Revision 1.1  1997/10/27 23:07:22  raif
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */

package cryptix.util.core;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * Static methods for processing BigInteger utilitarian tasks.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @since   Cryptix 2.2.2
 * @author  Raif S. Naffah
 */
public class BI
{
    private BI () {} // static methods only


// Utility methods
//...........................................................................

    /**
     * Read a BigInteger from a stream in Big Endian format (MSB first)
     * in a manner compatible with <code>toStream</code>.
     *
     * @param  is   the input stream.
     * @return a positive BigInteger read from the input stream in Big
     *              Endian format (MSB first).
     * @exception IOException if an I/O error occurs.
     */
    public static BigInteger fromStream (InputStream is)
    throws IOException {
        int bits = is.read() << 8 | is.read();  // reads 2 bytes into an int
        byte[] b = new byte[(bits + 7) / 8];    // compute nbr of bytes to allocate
        is.read(b);                             // reads the array into memory
        return new BigInteger(1, b);            // instantiate a positive BI
    }

    /**
     * Write a BigInteger to a stream in Big Endian format (MSB first)
     * in a manner compatible with <i>fromStream</i>.
     *
     * @param  x   A BigInteger to write to the output stream.
     * @param  os  The output stream.
     * @exception  IOException  If an i/o error occurs.
     */
    public static void toStream (BigInteger x, OutputStream os)
    throws IOException {
        int bits = x.bitLength();   // and write it as 2 bytes, MSB first
        os.write(bits >>> 8);
        os.write(bits & 0xFF);
        os.write(getMagnitude(x));
    }

    /**
     * Return the magnitude bytes of a BigInteger with no leading 0's.
     *
     * @return the magnitude bytes of a BigInteger with no leading 0's.
     */
    public static byte[] getMagnitude (BigInteger x) {
        byte[] y = x.toByteArray();    // including leading 0(s).
        int i = 0;
        for ( ; y[i] == 0 && i < y.length - 1; i++)
            ;
        byte[] result = new byte[y.length - i];	// excluding leading 0(s).
        System.arraycopy(y, i, result, 0, y.length - i);
        return result;
    }

    /**
     * Dump a BigInteger as a string, in a format that is easy to read for
     * debugging purposes. The string <i>m</i> is prepended to the start of
     * each line.
     *
     * @param  x  the BigInteger to dump
     * @param  m  a string to be prepended to each line
     * @return a String containing the dump
     */
    public static String dumpString (BigInteger x, String m) {
        if (x == null) return "null";

        StringBuffer sb = new StringBuffer(256);
        sb.append(m).append("Multi-Precision Integer ")
          .append(x.bitLength()).append(" bits long...\n");

        sb.append(m).append("      sign: ");
        if (x.signum() == -1)
            sb.append("Negative\n");
        else
            sb.append("Positive\n");

        sb.append(m).append(" magnitude: ")
          .append(Hex.dumpString(getMagnitude(x))).append('\n');

        return sb.toString();
    }

    public static String dumpString (BigInteger x) { return dumpString(x, ""); }
}
