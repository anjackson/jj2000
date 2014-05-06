// $Id: DES_EDE3KeyGenerator.java,v 1.1.1.1 2002/08/27 12:32:11 grosbois Exp $
//
// $Log: DES_EDE3KeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.2  2000/08/17 11:40:55  edwin
// java.* -> xjava.*
//
// Revision 1.1  1997/12/14 17:52:08  hopwood
// + Renamed from DES3KeyGenerator.
//
// Revision 0.1.1  1997/11/14  hopwood
// + Renamed this class to DES_EDE3KeyGenerator.
//
// Revision 0.1.0.4  1997/08/21  David Hopwood
// + Changed to consider cases where two or more keys are equal
//   as a weak key.
//
// Revision 0.1.0.3  1997/08/14  David Hopwood
// + Renamed this class to DES3KeyGenerator (from TripleDESKeyGenerator).
//   This is a more accurate name, because some variants of Triple DES
//   use only 2 independent DES keys.
// + Changed isWeak from public to protected.
//
// Revision 0.1.0.2  1997/08/01  David Hopwood
// + Added public isWeak(byte[] key) method.
// + Removed engineGenerateKey method, since the one inherited from
//   DESKeyGenerator will now work correctly.
// + Changed the algorithm name to "DES-EDE3" (which is a sensible
//   default algorithm to use with Triple DES keys, even though it
//   isn't the only one).
//
// Revision 0.1.0.1  1997/07/22  R. Naffah
// + Renamed keyBytesFromSeed to engineGenerateKey.
//
// Revision 0.1.0.0  1997/??/??  David Hopwood
// + Original version (Cryptix 2.2.0a)
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.key;

/**
 * A key generator for Triple DES with 3 independent DES keys.
 * <p>
 * A total of 24 bytes are generated, with a parity bit as the LSB of each
 * byte (i.e. there are 2^168 possible keys). The keys are encoded in the
 * order in which they are used for encryption. A Triple DES key is considered
 * weak if any of its constituent keys are weak, or if two or more of those
 * keys are equal, ignoring parity.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif Naffah
 * @since   Cryptix 2.2.0a, 2.2.2
 */
public class DES_EDE3KeyGenerator
extends DESKeyGenerator
{
    public DES_EDE3KeyGenerator() {
        super("DES-EDE3", 24);
    }

    /**
     * Returns true iff the first 24 bytes of <i>key</i> represent a weak
     * or semi-weak Triple DES key.
     */
    protected boolean isWeak(byte[] key) {
        if (isWeak(key, 0) || isWeak(key, 8) || isWeak(key, 16))
            return true;

        long k1 = ((long) key[ 0] & 0xFEL) << 56 |
                  ((long) key[ 1] & 0xFEL) << 48 |
                  ((long) key[ 2] & 0xFEL) << 40 |
                  ((long) key[ 3] & 0xFEL) << 32 |
                  ((long) key[ 4] & 0xFEL) << 24 |
                  ((long) key[ 5] & 0xFEL) << 16 |
                  ((long) key[ 6] & 0xFEL) <<  8 |
                  ((long) key[ 7] & 0xFEL);
        long k2 = ((long) key[ 8] & 0xFEL) << 56 |
                  ((long) key[ 9] & 0xFEL) << 48 |
                  ((long) key[10] & 0xFEL) << 40 |
                  ((long) key[11] & 0xFEL) << 32 |
                  ((long) key[12] & 0xFEL) << 24 |
                  ((long) key[13] & 0xFEL) << 16 |
                  ((long) key[14] & 0xFEL) <<  8 |
                  ((long) key[15] & 0xFEL);
        long k3 = ((long) key[16] & 0xFEL) << 56 |
                  ((long) key[17] & 0xFEL) << 48 |
                  ((long) key[18] & 0xFEL) << 40 |
                  ((long) key[19] & 0xFEL) << 32 |
                  ((long) key[20] & 0xFEL) << 24 |
                  ((long) key[21] & 0xFEL) << 16 |
                  ((long) key[22] & 0xFEL) <<  8 |
                  ((long) key[23] & 0xFEL);

        return k1 == k2 || k2 == k3 || k1 == k3;
    }
}
