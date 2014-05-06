// This file is currently unlocked (change this line if you lock the file)
//
// $Log: IDEAKeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.2  1997/09/09  David Hopwood
// + Added check for weak keys (see reference).
//
// Revision 0.1.0.1  1997/08/02  David Hopwood
// + Cosmetic changes.
//
// Revision 0.1.0.0  1997/08/02  David Hopwood
// + Start of history (Cryptix 2.2.0a).
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.key;

/**
 * A key generator for IDEA.
 * <p>
 * IDEA keys have a fixed length of 128 bits.
 * <p>
 * <b>References:</b>
 * <ol>
 *    <li>
 * </ol>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 2.2.0a, 2.2.2
 */
public class IDEAKeyGenerator extends RawKeyGenerator {
    public IDEAKeyGenerator() {
        super("IDEA", 128 / 8);
    }

    /**
     * Returns true iff the byte array <i>key</i> represents a
     * weak IDEA key.
     * <p>
     * IDEA has two non-overlapping classes of weak keys (bit numbering
     * is from left to right, e.g. 0 denotes the most significant bit of
     * the first byte):
     * <ul>
     *   <li> Keys with zeros in bit positions 0-25, 29-71, and 75-110
     *        (inclusive) and any value in bits 26-28, 72-74, and 111-127.
     *        There are 2^23 weak keys in this class.
     *        <p>
     *   <li> Keys with zeros in bit positions 0-25, 41-71, 84-98, and
     *        123-127 and any value in bit positions 26-40, 72-83, and
     *        99-122.  There are 2^51 weak keys in this class.
     * </ul>
     *
     * @param key   the byte array containing user key data.
     */
    public boolean isWeak (byte[] key) {
        // keys with any 1 in bits 0-25, 41-71, or 84-98 are OK.
        if (key[0] != 0 || key[1] != 0 || key[2] != 0 || (key[3] & 0xC0) != 0 ||
            (key[5] & 0x7F) != 0 || key[6] != 0 || key[7] != 0 || key[8] != 0 ||
            (key[10] & 0x0F) != 0 || key[11] != 0 || (key[12] & 0xE0) != 0)
            return false;

        // keys additionally with all 0s in bits 29-71 and 75-110 are weak.
        if ((key[3] & 0x07) == 0 && key[4] == 0 && key[5] == 0 && /* 6, 7, 8 done */
            (key[9] & 0x1F) == 0 && key[10] == 0 && /* 11 done */ key[12] == 0 &&
            (key[13] & 0xFE) == 0)
            return true;

        // keys additionally with all 0s in bits 123-127 are weak.
        if ((key[15] & 0x1F) == 0)
            return true;

        // otherwise OK.
        return false;
    }
}
