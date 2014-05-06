// $Id: DefaultElGamalParameterSet.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: DefaultElGamalParameterSet.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.4  1997/12/15 02:50:09  hopwood
// + Committed changes below.
//
// Revision 1.3.1  1997/12/15  hopwood
// + Added parameters for 512 and 768 bits.
//
// Revision 1.3  1997/12/14 17:37:58  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/12/14  hopwood
// + Temporary fix to match changes in GenericElGamalParameterSet.
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

/**
 * A default set of ElGamal parameters for Cryptix. These parameters may change
 * in future versions of Cryptix, and longer primes will be added.
 * <p>
 * The best methods for computing discrete logarithms in GF(p) have an expensive
 * pre-computation stage, but once the pre-computation has been done, individual
 * logarithms can be calculated quickly. In order to make sure that a particular
 * set of parameters does not become too much of a target because it is used in
 * more than one application, you may wish to generate your own set of parameters.
 * <p>
 * Most of this source file was generated automatically using:
 * <blockquote>
 *    <code>java cryptix.examples.CreateElGamalParameterSet</code><br>
 *    <code> &nbsp;&nbsp; cryptix.security.elgamal.DefaultElGamalParameterSet 384 512 768 1024 1536</code><br>
 *    <code> &nbsp;&nbsp; &gt; DefaultElGamalParameterSet.java</code>
 * </blockquote>
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
 * <b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  Cryptix 2.2.2
 * @see cryptix.examples.CreateElGamalParameterSet
 * @see cryptix.provider.elgamal.GenericElGamalParameterSet
 * @see cryptix.provider.elgamal.ConcreteElGamalKeyPairGenerator
 */
public class DefaultElGamalParameterSet extends GenericElGamalParameterSet {
    public DefaultElGamalParameterSet() {
        super(primeLengths, precomputed);
    }

    private static final int[] primeLengths = {
        512, 768, //1024, 1280, 1536, 
    };

    private static final String[][] precomputed = {
      // 512 bits
      { // p =
        "48be6b5f8d2a96c39a7bb1047dae6d0796cd3c9b3cc875758e1ad86da82af35e" +
        "56059756fdce765d2ef38e0670397bb5243e8f101c6c7f13b2d70217d7550649" +
        "801",
        // g =
        "7ddd0ba5d8861f8425f26cd65790852fe68a664461603574ec32288d8dc5680e" +
        "069e18c9a9d0d8395d0e0c2fa623124b7024c5f5c077f30782af7016298decf3" +
        "00c",
      },
      // 768 bits
      { // p =
        "76a59d6204e58995115b833dae2f4baefd1a8f3ae914d7c4e2ca4227c90e07c4" +
        "5e8532d20f0dcbfbb3a31a00baace24ae5afb940c4603cf8841e6a9018913761" +
        "442aa2dd7c9b48dc4e89bdaefc9169c7167c9db41c733531b6610ed696a8e382" +
        "91a5",
        // g =
        "667503f758ace0e04a29b7b660452e56cbb564cb22828a68fdfe8af1cfd90242" +
        "d444ee3b236a6d1e47a1def82f5082598891c2ce031e4c1ac6883349c2945903" +
        "2cb57754c6eca99474de8371a04d9dba6ccffc7fa55bc3b04b75c0f6ad742836" +
        "f9d1",
      },
    };
}
