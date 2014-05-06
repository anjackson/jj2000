// $Id: LOKI91KeyGenerator.java,v 1.1.1.1 2002/08/27 12:32:11 grosbois Exp $
//
// $Log: LOKI91KeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.2  2000/08/17 11:40:55  edwin
// java.* -> xjava.*
//
// Revision 1.1  1997/12/05 19:12:09  raif
// *** empty log message ***
//
// 1997.12.06: RSN
// + changed spelling to LOKI91.
// + cosmetics.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.2  1997/10/29  David Hopwood
// + Renamed class to Loki91KeyGenerator (from LOKI91KeyGenerator).
//   Loki is not an acronym.
//
// Revision 0.1.0.1  1997/08/02  David Hopwood
// + Added a public isWeak(byte[] key) method.
// + Moved list of weak keys to the isWeak comment.
// + Added (long) casts when converting the key bytes to a long.
//
// Revision 0.1.0.0  1997/07/0?  Raif Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.key;

/**
 * A key generator for LOKI91.
 * <p>
 * LOKI91 keys have a fixed length of 64 bits.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @author  David Hopwood
 * @since   Cryptix 2.2.0a, 2.2.2
 */
public class LOKI91KeyGenerator
extends RawKeyGenerator
{
    /** LOKI91 weak and semi-weak keys. */
    private static final long[] weakKeys = {

        // weak keys
        0x0000000000000000L,
        0xFFFFFFFFFFFFFFFFL,
        0x55555555AAAAAAAAL,
        0xAAAAAAAA55555555L,

        // semi-weak keys
        0x0000000055555555L, 0x00000000AAAAAAAAL, 0x00000000FFFFFFFFL,
        0x5555555500000000L, 0x5555555555555555L, 0x55555555FFFFFFFFL,
        0xAAAAAAAA00000000L, 0xAAAAAAAAAAAAAAAAL, 0xAAAAAAAAFFFFFFFFL,
        0xFFFFFFFF00000000L, 0xFFFFFFFF55555555L, 0xFFFFFFFFAAAAAAAAL
    };


    public LOKI91KeyGenerator() { super("LOKI91", 64 / 8); }


    /**
     * Returns true iff the byte array <i>key</i> represents a
     * weak or semi-weak LOKI91 key.
     * <p>
     * LOKI91 has four weak keys and twelve semi-weak keys. Here are
     * the lists of these keys (values given in hexadecimal form):
     * <p>
     * Weak Keys:<ul>
     *   <li> 00000000 00000000
     *   <li> FFFFFFFF FFFFFFFF
     *   <li> 55555555 AAAAAAAA
     *   <li> AAAAAAAA 55555555
     * </ul>
     * Semi-Weak Keys:<ul>
     *   <li> 00000000 55555555
     *   <li> AAAAAAAA 00000000
     *   <li> 00000000 AAAAAAAA
     *   <li> 55555555 00000000
     *   <li> 00000000 FFFFFFFF
     *   <li> FFFFFFFF 00000000
     *   <li> 55555555 55555555
     *   <li> AAAAAAAA AAAAAAAA
     *   <li> 55555555 FFFFFFFF
     *   <li> FFFFFFFF AAAAAAAA
     *   <li> AAAAAAAA FFFFFFFF
     *   <li> FFFFFFFF 55555555
     * </ul>
     *
     * @param key   the byte array containing user key data.
     */
    public boolean isWeak (byte[] key) {
        long a = ((long) key[0] & 0xFF) << 56 |
                 ((long) key[1] & 0xFF) << 48 |
                 ((long) key[2] & 0xFF) << 40 |
                 ((long) key[3] & 0xFF) << 32 |
                 ((long) key[4] & 0xFF) << 24 |
                 ((long) key[5] & 0xFF) << 16 |
                 ((long) key[6] & 0xFF) <<  8 |
                 ((long) key[7] & 0xFF);

        for (int i = 0; i < weakKeys.length; i++)
            if (weakKeys[i] == a) return true;
        return false;
    }
}
