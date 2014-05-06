// This file is currently unlocked (change this line if you lock the file)
//
// $Log: SPEEDKeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/08/02  David Hopwood
// + Changed to allow variable-length keys.
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
 * A variable-length key generator for SPEED.
 * <p>
 * SPEED keys have a length of between 48 and 256 bits, in steps of
 * 16 bits (i.e. keys always have an even number of bytes). The default
 * length is 128 bits.
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public class SPEEDKeyGenerator extends RawKeyGenerator {
    public SPEEDKeyGenerator() {
        super("SPEED", 48 / 8, 128 / 8, 256 / 8);
    }

    public boolean isValidKeyLength(int length) {
        return length >= 48 / 8 &&
               length <= 256 / 8 &&
               length % 2 == 0;
    }
}
