// $Id: PRZ24.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: PRZ24.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/11/20 21:32:00  hopwood
// + Moved ChecksumException and PRZ24 here from the cryptix.mime package.
//
// Revision 0.1.1.0  1997/??/??  R. Naffah
// + Modified to extend JDK 1.1 java.util.zip.Checksum;
//
// Revision 0.1.0.0  1997/??/??  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.checksum;

import java.util.zip.Checksum;

/**
 * Checksum subclass for calculating a 24-bit PGP-compatible CRC.
 * <p>
 * Notes on making a good 24-bit CRC: [FH]
 * <p>    
 * The primitive irreducible polynomial of degree 23 over GF(2), 040435651
 * (octal), comes from Appendix C of "Error Correcting Codes, 2nd edition"
 * by Peterson and Weldon, page 490. This polynomial was chosen for its
 * uniform density of ones and zeros, which has better error detection
 * properties than polynomials with a minimal number of nonzero terms.
 * Multiplying this primitive degree-23 polynomial by the polynomial x + 1
 * yields the additional property of detecting any odd number of bits in
 * error, which means it adds parity. This approach was recommended by Neal
 * Glover.
 * <p>
 * To multiply the polynomial 040435651 by (x + 1), shift it left 1 bit and
 * bitwise add (xor) the unshifted version back in. Dropping the unused
 * upper bit (bit 24) produces a CRC-24 generator bitmask of 041446373
 * octal, or 0x864CFB (hex).  
 * <p>
 * You can detect spurious leading zeros or framing errors in the message
 * by initializing the CRC accumulator to some agreed-upon nonzero "random-
 * like" value, but this is a bit nonstandard.
 * <p>
 * General notes:
 * <p>
 * These CRC functions are derived from code in chapter 19 of the book "C
 * Programmer's Guide to Serial Communications", by Joe Campbell.
 * Generalized to any CRC width by Philip Zimmermann.
 * <p>
 * Adapted from PGP, by Philip Zimmermann, source file <samp>armor.c</samp>.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  FH
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
public class PRZ24
implements Checksum
{

// Constants and variables
//...........................................................................
    
    /**
     * A CRC lookup table derived from the CRC polynomial. The table is used
     * later by the update methods to speed up computation.
     */
    private static final int[] table = new int[256];        // blank final
    
    private static final int
        CRC_BITS     = 24,
        CRC_HIGH_BIT = 1 << CRC_BITS,
//        CRC_MASK     = (1 << CRC_BITS) - 1,
        CRC_MASK     = CRC_HIGH_BIT - 1,
        PRZ_CRC      = 0x864CFB,    // PRZ's 24-bit CRC generator polynomial
        START_VALUE  = 0xB704CE;    // initial value for CRC accumulator

    /** This instance's accumulated CRC value. */
    private int prz24;


// Static code to build table[]
//...........................................................................

    static {
        int i, j, t;

        table[0] = 0;
        table[1] = PRZ_CRC;
        for (i = 1, j = 2; i < 128; i++) {
            t = table[i] << 1;
            if ((t & CRC_HIGH_BIT) != 0) {
                table[j++] = t ^ PRZ_CRC;
                table[j++] = t;
            } else {
                table[j++] = t;
                table[j++] = t ^ PRZ_CRC;
            }
        }
    }


// Constructor
//...........................................................................
    
    public PRZ24 () { reset(); }


// Checksum interface methods implementation
//...........................................................................

    /** Resets to start processing a new CRC. */
    public void reset () { prz24 = START_VALUE; }
    
    /**
     * Processes a buffer of bytes into the CRC accumulator.
     *
     * @param  buffer  buffer data.
     * @param  offset  offset into buffer.
     * @param  length  number of bytes to consider.
     */
    public void update (byte[] buffer, int offset, int length) {
        // make sure we don't exceed buffer's allocated size/length
        if ((length + offset) > buffer.length)
            length = buffer.length - offset;
        while (length-- > 0)
            update(buffer[offset++]);
    }

    /** Accumulates 1 byte into the CRC accumulator. */
    public void update (int n) {
        prz24 = ((prz24 << 8) ^ table[(prz24 >>> 16) ^ (n & 0xFF)]) & CRC_MASK;
    }

    /** Returns the value in the CRC accumulator. */
    public long getValue () { return (long) prz24 & 0xFFFFFFFFL; }
}
