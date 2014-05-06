// This file is currently unlocked (change this line if you lock the file)
//
// $Log: RC4KeyGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:11  grosbois
// Add cryptix 3.2
//
// Revision 1.2  1997/11/20 19:33:07  hopwood
// + Committed changes for RC4 weak keys.
//
// Revision 1.1.1.1.1  1997/11/10  David Hopwood
// + Implemented checking for some weak keys (see reference). There may be
//   other weak keys.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/08/15  David Hopwood
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
 * A variable-length key generator for RC4.
 * <p>
 * This implementation allows keys to have a length of between 40 and
 * 1024 bits. The default length is 128 bits.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> Andrew Roos &lt;andrewr@vironix.co.za&gt; (Vironix Software Laboratories),
 *        <cite>A Class of Weak Keys in the RC4 Stream Cipher</cite>,
 *        Preliminary draft posted to sci.crypt, 4th November 1997.
 * </ol>
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
public class RC4KeyGenerator extends RawKeyGenerator {
    public RC4KeyGenerator() {
        super("RC4", 40 / 8, 128 / 8, 1024 / 8);
    }

    /**
     * Returns true iff <i>key</i> is a weak RC4 key, as described in Andrew
     * Roos' paper.
     */
    protected boolean isWeak(byte[] key) {
        return key.length < 2 || (key[0] + key[1]) % 256 == 0;
    }
}
