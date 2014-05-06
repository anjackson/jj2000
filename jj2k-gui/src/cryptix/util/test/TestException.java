// $Id: TestException.java,v 1.1.1.1 2002/08/27 12:32:16 grosbois Exp $
//
// $Log: TestException.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:16  grosbois
// Add cryptix 3.2
//
// Revision 1.2  1998/02/22 04:18:30  hopwood
// + Committed changes below.
//
// Revision 1.1.1  1998/02/08  hopwood
// + Added FATAL_ERROR and COMPLETE_SUCCESS codes.
//
// Revision 1.1  1998/01/28 05:44:02  hopwood
// + Major update of test classes.
//
// Revision 0.1.2.1  1998/01/16  hopwood
// + Made this class, the constants, and the constructor public.
// + Moved to cryptix.util.test package.
//
// Revision 0.1.2  1997/12/16 08:52:08  iang
// + Added exit code 3 - no test written yet
//
// Revision 0.1.1  1997/12/15 02:40:54  hopwood
// + Committed changes below.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.test;

/**
 * This exception is thrown when a test fails. If the test is running directly from
 * the command line, the <code>getErrorCode()</code> method returns the error code
 * that should be passed to <code>System.exit</code>.
 * <p>
 * <b>Copyright</b> &copy; 1995-1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 3.0.2
 */
public class TestException extends Exception
{
    public static final int FATAL_ERROR = 0;
    public static final int COMPLETE_FAILURE = 1;
    public static final int ILLEGAL_ARGUMENTS = 2;
    public static final int NO_TESTS_AVAILABLE = 3; // some tests not written yet
    public static final int PARTIAL_FAILURE = 4;
    public static final int ABORTED_BY_USER = 5;
    // leave some room here
    public static final int COMPLETE_SUCCESS = 10;

    private int errorcode;

    /** Returns the error code that should be passed to <code>System.exit</code>. */
    public int getErrorCode() { return errorcode; }

    /**
     * Constructs a TestException with the specified detail message and error code.
     *
     * @param  reason   the reason why the exception was thrown.
     * @param  code     the error code.
     */
    public TestException(String reason, int code) {
        super(reason);
        errorcode = code;
    }
}
