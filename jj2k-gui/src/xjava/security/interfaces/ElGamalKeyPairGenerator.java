/*
// $Log: ElGamalKeyPairGenerator.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:31  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:28  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:58  hopwood
// + Imported to CVS (tagged as 'start').
//
// $Endlog$
*/

package xjava.security.interfaces;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.InvalidParameterException;

/**
 * An interface to an object capable of generating ElGamal key pairs.  The
 * generator is first initialized, then used to generate one or more
 * key pairs.
 * <p>
 * Users wishing to indicate the prime or base, and to generate a key 
 * pair suitable for use with the ElGamal signature or encryption algorithms
 * typically
 * <ol>
 *   <li> Get a key pair generator for the ElGamal algorithms by calling the 
 *        KeyPairGenerator <code>getInstance</code> method with "ElGamal" 
 *        as its argument.<p> 
 *   <li> Initialize the generator by casting the result to an 
 *        ElGamalKeyPairGenerator and calling one of the <code>initialize</code> 
 *        methods from this ElGamalKeyPairGenerator interface.<p>
 *   <li> Generate one or more key pairs by calling the 
 *        <code>generateKeyPair</code> method from the KeyPairGenerator 
 *        class, as often as desired.
 * </ol> 
 * <p>
 * Note: it is not always necessary to do algorithm-specific
 * initialization for an ElGamal key pair generator. That is, it is not always
 * necessary to call one of the <code>initialize</code> methods in this
 * interface.
 * Algorithm-independent initialization using the <code>initialize</code>
 * method in the KeyPairGenerator interface is all that is needed when you 
 * accept defaults for algorithm-specific parameters.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  IJCE 1.0.1
 * @see java.security.KeyPairGenerator
 */
public interface ElGamalKeyPairGenerator {
    /**
     * Initializes the key pair generator using the prime and base from
     * the specified ElGamalParams object.
     *
     * @param params    the parameters to use to generate the keys.
     * @param random    the random bit source to use to generate 
     *          key bits.
     * @exception InvalidParameterException if the parameters passed are
     *          invalid.
     */
    void initialize(ElGamalParams params, SecureRandom random)
        throws InvalidParameterException;

    /**
     * Initializes the key pair generator using the specified prime and
     * base. The difficulty of cracking ElGamal by solving the discrete
     * logarithm problem is dependent on the length of the prime. An
     * InvalidParameterException will be thrown if the base is not less
     * than the prime.
     *
     * @param prime     the prime to be used, as a java.math.BigInteger
     * @param base      the base to be used, as a java.math.BigInteger
     * @param random    the random bit source to use to generate 
     *          key bits.
     * @exception InvalidParameterException if the parameters passed are
     *          invalid.
     */
    void initialize(BigInteger prime, BigInteger base, SecureRandom random)
        throws InvalidParameterException;

    /**
     * Initializes the key pair generator for a given prime length,
     * without parameters.
     * <p>
     * If <i>genParams</i> is true, this method will generate new
     * p and g parameters. If it is false, the method will use precomputed
     * parameters for the prime length requested. If there are no
     * precomputed parameters for that prime length, an exception will be
     * thrown. It is guaranteed that there will always be default
     * parameters for prime lengths of 513, 1025, 1537 and 2049 bits.
     *
     * @param primeLen  the prime length, in bits. Valid lengths are any
     *          integer >= 512.
     * @param random    the random bit source to use to generate
     *          key bits.
     * @param genParams whether to generate new parameters for the prime
     *          length requested.
     * @exception InvalidParameterException if the prime length is less
     *          than 512, or if genParams is false and there are not
     *          precomputed parameters for the prime length requested.
     */
    void initialize(int primeLen, boolean genParams, SecureRandom random)
        throws InvalidParameterException;

    /**
     * Generates new parameters, <i>p</i> and <i>g</i>. This method
     * does not change the parameters currently being used by
     * <code>generateKeyPair</code>.
     *
     * @param primeLen  the prime length, in bits. Valid lengths are any
     *          integer >= 512.
     * @param random    the random bit source to use to generate the parameters.
     * @exception InvalidParameterException if the prime length is less
     *          than 512.
     */
    ElGamalParams generateParams(int primeLen, SecureRandom random)
        throws InvalidParameterException;
}
