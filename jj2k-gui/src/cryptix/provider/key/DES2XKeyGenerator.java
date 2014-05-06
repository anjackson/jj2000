// $Id: DES2XKeyGenerator.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: DES2XKeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.2  2000/08/17 11:40:55  edwin
// java.* -> xjava.*
//
// Revision 1.1  1998/05/27 22:05:23  kettler
// + Added DES2X another variant of using DES with 56+3*64 bit keys
//
//
// $Endlog$
/*
 * Copyright (c) 1998 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.key;

/**
 * A key generator for DES2X with one DES key and three XOR key.
 * <p>
 * <b>Copyright</b> &copy; 1998
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Sascha Kettler
 * @author  David Hopwood
 * @author  Raif Naffah
 * @since   Cryptix 3.0.4
 */
public class DES2XKeyGenerator
extends DESKeyGenerator
{
    public DES2XKeyGenerator() {
        super("DES2X", 32);
    }

    /**
     * Returns true iff the first 8 bytes of <i>key</i> represent a weak
     * DES key.
     */
    protected boolean isWeak(byte[] key) {
        if (isWeak(key, 0))
            return true;

        return false;
    }
}
