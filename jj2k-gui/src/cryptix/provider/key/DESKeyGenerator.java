// This file is currently unlocked (change this line if you lock the file)
//
// $Log: DESKeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.2  2000/08/17 11:40:55  edwin
// java.* -> xjava.*
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.4  1997/08/14  David Hopwood
// + Changed isWeak from public to protected.
//
// Revision 0.1.0.3  1997/08/04  David Hopwood
// + Changed uses of InvalidKeyFormatException to InvalidKeyException.
//
// Revision 0.1.0.2  1997/08/01  David Hopwood
// + Added a public isWeak(byte[] key) method.
// + Simplified implementation of engineGenerateKey by taking into
//   account RawKeyGenerator changes.
// + Added more comments.
//
// Revision 0.1.0.1  1997/07/22  R. Naffah
// + Renamed keyBytesFromSeed to engineGenerateKey.
//
// Revision 0.1.0.0  1997/?/0?  David Hopwood
// + Original version (Cryptix 2.2.0a)
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.key;

import xjava.security.WeakKeyException;
import java.security.InvalidKeyException;

/**
 * A key generator for (single) DES.
 * <p>
 * DES keys have a fixed length of 8 bytes, with a parity bit as the LSB of each
 * byte (i.e. there are 2^56 possible keys).
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif Naffah
 * @since   Cryptix 2.2.0a, 2.2.2
 */
public class DESKeyGenerator
extends RawKeyGenerator
{
    public DESKeyGenerator() {
        super("DES", 8);
    }

    /**
     * Constructor for use by subclasses that need to specify a different seed
     * length (e.g. for Triple DES).
     */
    protected DESKeyGenerator(String algorithm, int seedlength) {
        super(algorithm, seedlength);
    }

    protected byte[] engineGenerateKey (byte[] seed)
    throws WeakKeyException, InvalidKeyException {
        seed = super.engineGenerateKey(seed);
        setParity(seed);
        return seed;
    }

    /**
     * Sets parity bits for the given input array. The least significant bit
     * of each byte is changed to ensure that the byte has odd parity.
     */
    protected void setParity(byte[] array) {
        int b;
        for (int i = 0; i < array.length; i++) {
            b = array[i];
            array[i] = (byte)((b & 0xFE) |
                              (((b >> 1) ^
                                (b >> 2) ^
                                (b >> 3) ^
                                (b >> 4) ^
                                (b >> 5) ^
                                (b >> 6) ^
                                (b >> 7)) & 0x01));
        }
    }

    /**
     * Returns true iff the first 8 bytes of <i>key</i> represent a weak
     * or semi-weak single DES key.
     */
    protected boolean isWeak(byte[] key) {
        return isWeak(key, 0);
    }

    /**
     * Returns true iff the bytes at key[offset..offset+7] represent a weak
     * or semi-weak single DES key. It can be called either before or after
     * setting parity bits.
     * <p>
     * (This checks for the 16 weak and semi-weak keys as given by Schneier,
     * <cite>Applied Cryptography 2nd ed.</cite>, tables 12.11 and 12.12. It
     * does not check for the possibly-weak keys in table 12.13.)
     */
    protected boolean isWeak(byte[] key, int offset) {
        int a = (key[offset  ] & 0xFE) << 8 | (key[offset+1] & 0xFE);
        int b = (key[offset+2] & 0xFE) << 8 | (key[offset+3] & 0xFE);
        int c = (key[offset+4] & 0xFE) << 8 | (key[offset+5] & 0xFE);
        int d = (key[offset+6] & 0xFE) << 8 | (key[offset+7] & 0xFE);

        return (a == 0x0000 || a == 0xFEFE) &&
               (b == 0x0000 || b == 0xFEFE) &&
               (c == 0x0000 || c == 0xFEFE) &&
               (d == 0x0000 || d == 0xFEFE);
    }
}
