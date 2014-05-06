// This file is currently unlocked (change this line if you lock the file)
//
// $Log: SAFERKeyGenerator.java,v $
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
// + Start of history.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.key;

/**
 * A key generator for SAFER.
 * <p>
 * SAFER keys have a length of either 64 or 128 bits. The default is
 * 128 bits.
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
public class SAFERKeyGenerator extends RawKeyGenerator {
    public SAFERKeyGenerator() {
        super("SAFER", 64 / 8, 128 / 8, 128 / 8);
    }

    public boolean isValidKeyLength(int length) {
        return length == 64 / 8 ||
               length == 128 / 8;
    }
}
