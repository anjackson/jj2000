// $Id: TestUnixCrypt.java,v 1.1.1.1 2002/08/27 12:32:14 grosbois Exp $
//
// $Log: TestUnixCrypt.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:14  grosbois
// Add cryptix 3.2
//
// Revision 1.1  1998/02/04 01:50:03  hopwood
// + Added to CVS.
//
// Revision 0.1.0  1998/02/03  hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997, 1998 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.test;

import cryptix.tools.UnixCrypt;
import cryptix.util.test.BaseTest;

/**
 * This class tests the <samp><a href=cryptix.tools.UnixCrypt.html>
 * cryptix.tools.UnixCrypt</a></samp> class (it may also fail if the DES
 * implementation is incorrect).
 * <p>
 * <b>Copyright</b> &copy; 1997, 1998
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 */
public class TestUnixCrypt
extends BaseTest
{

// Test methods
//................................................................................

    public static void main (String[] args) {
        new TestUnixCrypt().commandline(args);
    }

    /**
     * Test that is run by distribution to make sure everything is OK!
     * <p>
     * This C test program will confirm (note that some systems don't implement
     * straight crypt(3)).
     * <pre>
     *    #include <unistd.h>
     *    main()
     *    {
     *        const char *key = "CryptixRulez";
     *        const char salt[] = {'o','k'};
     *        printf("crypt(%s, %s) = %s\n",
     *               key, salt, crypt(key, salt));
     *    }
     * </pre>
     */
    protected void engineTest() throws Exception {
        setExpectedPasses(1);

        String original = "CryptixRulez";
        String salt = "OK";
        String solution = "OKDvOv8WCyJBI";

        UnixCrypt jc = new UnixCrypt(salt);
        String crypted = jc.crypt(original);
        out.println("original = \"" + original + "\", salt = \"" + salt +
            "\", solution = \"" + solution + "\",\n crypted = \"" + crypted + "\"");

        passIf(solution.equals(crypted), "UnixCrypt");
    }
}
