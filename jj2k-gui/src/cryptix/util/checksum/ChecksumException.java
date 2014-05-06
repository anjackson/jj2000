// $Id: ChecksumException.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: ChecksumException.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/11/20 21:32:00  hopwood
// + Moved ChecksumException and PRZ24 here from the cryptix.mime package.
//
// Revision 0.1.0.0  1997/??/??  R. Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.checksum;

import java.io.IOException;

/**
 * A Java class to handle checksum exceptions.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 */
public class ChecksumException
extends IOException
{
    public ChecksumException () { super(); }
    public ChecksumException (String description) { super(description); }
}

