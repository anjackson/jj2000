// $Id: SHA1_ElGamal_PKCS1Signature.java,v 1.1.1.1 2002/08/27 12:32:10 grosbois Exp $
//
// $Log: SHA1_ElGamal_PKCS1Signature.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:10  grosbois
// Add cryptix 3.2
//
// Revision 1.2  1997/12/10 01:17:46  raif
// *** empty log message ***
//
// 1997.12.10 --RSN
// + modified the semantics of the AlgId bytes.
//
// Revision 1.1  1997/12/07 06:37:26  hopwood
// + Major overhaul of ElGamal to match RSA.
//
// Revision 0.1.0.0  1997/12/04  David Hopwood
// + Original version (based on SHA1_ElGamal_PKCS1Signature).
//
// $Endlog$
/*
 * Copyright (c) 1995-97 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider.elgamal;

/**
 * A class to digest a message with SHA-1, and sign/verify the
 * resulting hash using the ElGamal digital signature scheme, with PKCS#1
 * block padding.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public class SHA1_ElGamal_PKCS1Signature
extends Any_ElGamal_PKCS1Signature
{

// Constants and variables
//............................................................................
    
    private static final byte[] SHA1_ASN_DATA = {
        0x30, 0x21,                                // SEQUENCE 33
          0x30, 0x09,                                // SEQUENCE 9
            0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A,  // OID {1.3.14.3.2.26}
            0x05, 0x00,                                // NULL
          0x04, 0x14                                 // OCTET STRING 20
    };


// Constructor
//............................................................................

    public SHA1_ElGamal_PKCS1Signature () { super("SHA-1"); }


// Any_ElGamal_PKCS1Signature abstract method implementation
//............................................................................

    protected byte[] getAlgorithmEncoding () { return SHA1_ASN_DATA; }
}
