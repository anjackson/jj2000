// $Id: CryptixException.java,v 1.1.1.1 2002/08/27 12:32:09 grosbois Exp $
//
// $Log: CryptixException.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:09  grosbois
// Add cryptix 3.2
//
// Revision 1.1  1997/11/20 21:01:06  hopwood
// + Moved CryptixException and CryptixProperties here from the cryptix.core
//   package.
//
// Revision 1.2  1997/11/08 14:37:53  raif
// *** empty log message ***
//
// Revision 1.1  1997/11/05  R. Naffah
// + Made it extend java.security.ProviderException;
//
// Revision 0.1.0.1  1997/08/05  David Hopwood
// + Moved this class here from cryptix.security package.
// + Made the constructor public.
//
// Revision 0.1.0.0  1997/08/05  David Hopwood
// + Start of history (Cryptix 2.2.0a).
//
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team. All rights reserved.
 */

package cryptix;

/**
 * This class is for any unexpected exception in the crypto library.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public class CryptixException
extends java.security.ProviderException
{
    /** @param reason  the reason why the exception was thrown. */
    public CryptixException(String reason) { super(reason); }       
}
