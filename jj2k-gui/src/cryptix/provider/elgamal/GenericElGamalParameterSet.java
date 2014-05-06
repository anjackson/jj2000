// $Id: GenericElGamalParameterSet.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: GenericElGamalParameterSet.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.4  2000/08/17 11:40:54  edwin
// java.* -> xjava.*
//
// Revision 1.3  1997/12/14 17:37:58  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/12/14  hopwood
// + Changed to allow CreateElGamalParameterSet to output the class source
//   progressively as parameters are found.
//
// Revision 1.2  1997/12/07 06:37:26  hopwood
// + Major overhaul of ElGamal to match RSA.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.elgamal;

import java.math.BigInteger;
import java.security.InvalidParameterException;
import xjava.security.interfaces.ElGamalParams;

/**
 * This class represents a set of ElGamal parameters for various prime lengths.
 * <p>
 * The best methods for computing discrete logarithms in GF(p) have an expensive
 * pre-computation stage, but once the pre-computation has been done, individual
 * logarithms can be calculated quickly. In order to make sure that a particular
 * set of parameters does not become too much of a target because it is used in
 * more than one application, you may wish to generate your own set of parameters.
 * <p>
 * <b>References:</b>
 * <ol>
 *   <li> Bruce Schneier,
 *        "Section 11.3 Number Theory" (heading "Calculating Discrete Logarithms
 *        in a Finite Group," pages 262-263),
 *        <cite>Applied Cryptography, 2nd edition</cite>,
 *        John Wiley &amp; Sons, 1996.
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  Cryptix 2.2.2
 * @see cryptix.examples.CreateElGamalParameterSet
 * @see cryptix.security.elgamal.BaseElGamalKeyPairGenerator
 */
public class GenericElGamalParameterSet {
    private int[] primeLengths;
    private String[][] precomputed;

    /**
     * Subclasses should call this constructor to determine the parameters
     * that will be returned by <code>getParameters</code>.
     *
     * @param primeLengths  an array of bit lengths for each prime
     * @param precomputedP  an array of hex strings representing each prime
     * @param precomputedG  an array of hex strings representing each base
     * @exception IllegalArgumentException if the arrays are not all the
     *          same length.
     */
    protected GenericElGamalParameterSet(int[] primeLengths,
                                         String[][] precomputed) {
        if (precomputed.length != primeLengths.length)
            throw new IllegalArgumentException("array lengths do not match");

        this.primeLengths = primeLengths;
        this.precomputed = precomputed;
    }

    /**
     * If <i>primeLength</i> corresponds to one of the precomputed prime lengths,
     * this method returns a corresponding ElGamalParams object. Otherwise,
     * it returns null.
     */
    public ElGamalParams getParameters(int primeLength) {
        for (int i = 0; i < primeLengths.length; i++) {
            if (primeLength == primeLengths[i]) {
                return new BaseElGamalParams(
                    new BigInteger(precomputed[i][0], 16),
                    (precomputed[i][1] != null) ? new BigInteger(precomputed[i][1], 16)
                                                : null
               );
            }
        }
        return null;
    }

    /**
     * Throws an InvalidParameterException if any of the parameters are obviously
     * incorrect.
     */
    public void checkSane() throws InvalidParameterException {
        for (int i = 0; i < primeLengths.length; i++) {
            BigInteger p = new BigInteger(precomputed[i][0]);
//            if (p.bitLength() != primeLengths[i])
            if (p.bitLength() < primeLengths[i])
                throw new InvalidParameterException(p + " has incorrect bit length");
            BigInteger g = new BigInteger(precomputed[i][1]);
            if (!p.isProbablePrime(80))
                throw new InvalidParameterException(p + " is not prime");
            if (g.compareTo(p) >= 0)
                throw new InvalidParameterException("g >= p");
        }
    }
}
