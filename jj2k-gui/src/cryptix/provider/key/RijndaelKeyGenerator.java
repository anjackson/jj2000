/* $Id: RijndaelKeyGenerator.java,v 1.1.1.1 2002/08/27 12:32:11 grosbois Exp $
 *
 * This file is in the public domain.
 */
package cryptix.provider.key;


/**
 * A variable-length key generator for Rijndael.
 * 
 * <p>The possible values are 128, 192 and 256 bits (16, 24 and 32 bytes).</p>
 *
 * @version $Revision: 1.1.1.1 $
 * @author  Edwin Woudt <edwin@cryptix.org>
 * @since   Cryptix 3.1.3/3.2.0
 */

public class RijndaelKeyGenerator extends RawKeyGenerator {

    public RijndaelKeyGenerator() {
        super("Rijndael", 16, 16, 32);
    }
    
    public boolean isValidKeyLength(int length) {
        return ((length == 16) || (length == 24) || (length == 32));
    }

}
