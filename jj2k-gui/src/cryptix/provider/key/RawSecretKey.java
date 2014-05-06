// This file is currently unlocked (change this line if you lock the file)
//
// $Log: RawSecretKey.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.2  2000/08/17 11:40:56  edwin
// java.* -> xjava.*
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.3  1997/08/21  David Hopwood
// + Added (String algorithm, byte[] data, int offset, int length)
//   constructor.
//
// Revision 0.1.0.2  1997/07/31  David Hopwood
// + Cosmetic changes.
//
// Revision 0.1.0.1  1997/07/22  R. Naffah
// + Moved the code to cryptix.provider.key.RawKey class and made
//   this a subclass. Now we can have different sorts of Raw-keys
//   with minimal implementations.
//
// Revision 0.1.0.0  1997/?/0?  David Hopwood
// + Original version (Cryptix 2.2.0a).
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.key;

import xjava.security.SecretKey;

/**
 * RawSecretKey implements a secret key in raw format. RawKeyGenerator
 * creates keys that are instances of this class.
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
public class RawSecretKey
extends RawKey
implements SecretKey
{

// Constructors
//............................................................................

    /**
     * Constructs a secret key with the specified algorithm and raw-encoded
     * data array.
     *
     * @param algorithm the name of the algorithm
     * @param data      the key's raw-encoded data
     * @exception NullPointerException if algorithm == null || data == null
     */
    public RawSecretKey (String algorithm, byte[] data) {
        super(algorithm, data);
    }

    /**
     * Constructs a secret key with the specified algorithm and raw-encoded
     * data subarray.
     *
     * @param algorithm the name of the algorithm
     * @param data      the key's raw-encoded data
     * @param offset    the offset of the encoding in <i>data</i>
     * @param length    the length of the encoding
     * @exception NullPointerException if algorithm == null || data == null
     */
    public RawSecretKey (String algorithm, byte[] data, int offset, int length) {
        super(algorithm, data, offset, length);
    }
}
