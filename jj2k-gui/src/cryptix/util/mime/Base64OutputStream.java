// $Id: Base64OutputStream.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: Base64OutputStream.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/12/22 03:28:42  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 1.7.1  1997/12/22  hopwood
// + Made sure that the close() method flushes and closes the underlying
//   stream.
//
// Revision 1.7  1997/12/03 01:15:01  raif
// + fixed a trivial bug in the close() method.
//
// Revision 1.6  1997/11/23 06:51:40  hopwood
// + Fixed typo.
//
// Revision 1.5  1997/11/23 03:20:02  hopwood
// + Committed changes below.
//
// Revision 1.4.1  1997/11/22  hopwood
// + If a Checksum is passed to the constructor, make sure it is reset.
//
// Revision 1.4  1997/11/21 06:10:05  hopwood
// + Fixed minor errors.
//
// Revision 1.3  1997/11/21 04:32:19  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/11/20  David Hopwood
// + Made the checksum handling more generic, by allowing any subclass of
//   java.util.zip.Checksum.
// + os variable removed - it is unnecessary because calling super.write
//   will write to the underlying stream.
// + Added synchronized where necessary.
//
// Revision 1.2  1997/11/04 19:33:30  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:55  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.1.1  1997/08/02  David Hopwood
// + Moved this class to the cryptix.mime package.
// + Output CR LF (instead of LF) for line endings, according to MIME
//   conventions.
// + Cosmetic changes.
//
// Revision 0.1.1.0  1997/06/0?  Raif Naffah
// + Start of history.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.mime;

import cryptix.util.checksum.PRZ24;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

/**
 * This class implements a BASE64 Character stream decoder as specified
 * in RFC1521, part of the MIME specification as published by the Internet
 * Engineering Task Force (IETF).
 * <p>
 * <b>Copyright</b> &copy; 1995-1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public class Base64OutputStream
extends FilterOutputStream
{
// Variables and constants
//...........................................................................

    /** Accumulated checksum, if requested, null if no checksum is used. */
    private Checksum crc;

    /** Number of checksum bytes to append. */
    private int crcLength;

    /** Internal data buffer. */
    private byte[] inBuf;

    /** Count of bytes in internal data buffer. */
    private int inOff;

    /** Number of bytes output so far on line. */
    private int lineLength;

    private static final int MAX_LINE_LENGTH = 64;
    private static final char[] BASE64_CHARSET = {
    //   0   1   2   3   4   5   6   7
        'A','B','C','D','E','F','G','H', // 0
        'I','J','K','L','M','N','O','P', // 1
        'Q','R','S','T','U','V','W','X', // 2
        'Y','Z','a','b','c','d','e','f', // 3
        'g','h','i','j','k','l','m','n', // 4
        'o','p','q','r','s','t','u','v', // 5
        'w','x','y','z','0','1','2','3', // 6
        '4','5','6','7','8','9','+','/'  // 7
    };
    private static final char PADDING = '=';


// Constructors
//...........................................................................

    /**
     * If check is true then append a PGP-style checksum immediately
     * after the base64 portion.
     *
     * @see cryptix.util.checksum.PRZ24
     */
    public Base64OutputStream (OutputStream os, boolean check) {
        super(os);
        init(check ? new PRZ24() : null, 3);
    }

    /** Creates a Base64OutputStream with no checksum. */
    public Base64OutputStream (OutputStream os) {
        super(os);
        init(null, 0);
    }

    /**
     * Creates a Base64OutputStream that uses the given <i>checksum</i>.
     * The checksum will be <i>length</i> bytes long, where
     * 0 &lt;= <i>length</i> &lt;= 8.
     */
    public Base64OutputStream (OutputStream os, Checksum checksum, int length) {
        super(os);
        checksum.reset();
        init(checksum, length);
    }


// FilterOutputStream overridden methods
//............................................................................

    public synchronized void write (int b)
    throws IOException {
        inBuf[inOff++] = (byte)b;
        if (crc != null) crc.update(b);
        if (inOff == 3) writeQuadruplet();
    }

    public synchronized void write (byte[] b, int offset, int length)
    throws IOException {
        for (int i = 0; i < length; i++) this.write(b[offset++]);
    }

    public synchronized void close ()
    throws IOException {
        writePadding();                 // process remaining chars in buffer
        // force line break unless current line is empty
        if (lineLength != 0) writeln();
        if (crc != null) {              // if should add a CRC then do it now
            long cks = crc.getValue();
            // write a padding byte, followed by the checksum bytes
            super.write(PADDING);
            crc = null;
//            for (int i = crcLength - 1; i >= 0; i++)
            for (int i = crcLength - 1; i >= 0; i--)
                this.write((int) (cks >>> (i * 8)) & 0xFF);
            writePadding();
            writeln();
        }
        super.flush();
        super.close();
    }


// Own methods
//...........................................................................

    private void init(Checksum checksum, int length) {
        if (length < 0 || length > 8)
            throw new IllegalArgumentException("length < 0 || length > 8");
        lineLength = inOff = 0;
        inBuf = new byte[3];
        crc = checksum;
        crcLength = length;
    }

    private void writePadding() throws IOException {
        if (inOff != 0) {
            for (int i = inOff; i < 3; i++) inBuf[i] = 0;
            writeQuadruplet();
        }
    }

    private void writeQuadruplet() throws IOException {
        char c = BASE64_CHARSET[(inBuf[0] & 0xFF) >> 2];
        super.write(c);
        c = BASE64_CHARSET[(inBuf[0] & 0x03) << 4 | (inBuf[1] & 0xFF) >> 4];
        super.write(c);
        c = inOff > 1 ? 
            BASE64_CHARSET[(inBuf[1] & 0x0F) << 2 | (inBuf[2] & 0xCF) >> 6] :
            PADDING;
        super.write(c);
        c = inOff > 2 ?
            BASE64_CHARSET[inBuf[2] & 0x3F] :
            PADDING;
        super.write(c);
        inOff = 0;
        lineLength += 4;
        if (lineLength >= MAX_LINE_LENGTH) writeln();
    }

    private void writeln ()
    throws IOException {
        super.write('\r');
        super.write('\n');
        lineLength = 0;
    }
}
