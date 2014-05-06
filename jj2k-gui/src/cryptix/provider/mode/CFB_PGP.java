/* $Id: CFB_PGP.java,v 1.1.1.1 2002/08/27 12:32:12 grosbois Exp $
 *
 * Copyright (c) 1995-1999 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.mode;

import xjava.security.Cipher;
import xjava.security.IllegalBlockSizeException;

/**
 * <b>Use of this feedback mode is deprecated, use it for compatibility only!
 * </b><p>
 * This class implements PGP's (i.e. Zimmerman's) non-standard CFB mode.
 * (For the standard method, see the Applied Cryptography reference below).
 * It replaces <samp>cryptix.pgp.CFB</samp> in version 2.2, and
 * <samp>cryptix.pgp.PGP_CFB</samp> in version Cryptix 2.2.0a.
 * <p>
 * The differences between this and standard CFB are that:
 * <ul>
 *   <li> The IV passed to the underlying CFB class is always zero.
 *        An additional IV should be included as the first block of
 *        the input stream (when encrypting), and this additional IV
 *        should be unique.
 *   <li> Before each encryption or decryption operation (i.e. each
 *        call to <code>update</code> or <code>crypt</code>), the CFB
 *        shift register is encrypted, regardless of the current position
 *        within a block.
 * </ul>
 * <p>
 * Note that it is unusual for the output of a cipher to depend on
 * the exact boundaries between data passed to each encryption/decryption
 * call. Normally, the lengths of data passed to each call do not matter
 * as long as they make up the correct input when concatenated together -
 * but this class is an exception.
 * <p>
 * As a result, using this mode with CipherInputStream and CipherOutputStream
 * <em>may</em> produce unexpected output, and is not recommended.
 * <p>
 * <b>References</b>
 * <ol>
 *   <li> <a href="mailto:schneier@counterpane.com">Bruce Schneier</a>,
 *        "Section 9.6 Cipher Feedback Mode,"
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author Systemics Ltd
 * @author Raif Naffah
 * @author David Hopwood
 * @since  Cryptix 2.2.2
 */
public class CFB_PGP
extends CFB
{
    /**
     * Constructs a CFB-PGP cipher, assuming that the IV will be provided
     * as the first <code>getInitializationVectorLength()</code>
     * bytes of the stream (it will be visible as cleartext at the start
     * of the output stream).
     * <p>
     * The IV must be unique during the lifetime of the key.
     * <strong>If it is not unique, at least the first block
     * of the plaintext can be recovered.</strong>
     */
    public CFB_PGP() {
        super();
        setInitializationVector(new byte[getInitializationVectorLength()]);
    }

    /**
     * Constructs a CFB-PGP cipher, assuming that the IV will be provided
     * as the first <code>getInitializationVectorLength()</code>
     * bytes of the stream. See the previous constructor for more details.
     *
     * @exception NullPointerException if cipher == null
     * @exception IllegalBlockSizeException if cipher.getPlaintextBlockSize() !=
     *            cipher.getCiphertextBlockSize()
     */
    public CFB_PGP(Cipher cipher) {
        super(cipher);
        setInitializationVector(new byte[getInitializationVectorLength()]);
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen,
                               byte[] out, int outOffset) {
        next_block();
        return super.engineUpdate(in, inOffset, inLen, out, outOffset);
    }
}
