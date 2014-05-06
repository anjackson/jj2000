// $Id: Debug.java,v 1.1.1.1 2002/08/27 12:33:06 grosbois Exp $
//
// $Log: Debug.java,v $
// Revision 1.1.1.1  2002/08/27 12:33:06  grosbois
// Add cryptix 3.2
//
// Revision 1.3  1998/02/22 04:20:56  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1998/02/14  hopwood
// + Added GLOBAL_TRACE field.
//
// Revision 1.2  1998/01/11 08:19:36  raif
// *** empty log message ***
//
// Revision 1.1.2  1997/12/28  raif
// + added Tracing property parsing and access.
//   see isTraceable().
//
// Revision 1.1.1.1  1997/11/20 21:04:51  hopwood
// + Moved these classes here from cryptix.core.util.*.
//
// Revision 0.1.0.4  1997/11/16  David Hopwood
// + Removed getInternalLevel.
// + Changed names for all debugging properties to start with "Debug.Level".
//
// Revision 0.1.0.3  1997/11/13  David Hopwood
// + Fixed package name, and CryptixProperties import.
//
// Revision 0.1.0.2  1997/09/18  David Hopwood
// + Added GLOBAL_DEBUG_SLOW compile-time constant.
//
// Revision 0.1.0.1  1997/09/06  David Hopwood
// + Removed "Debug" from method names (e.g. getDebugLevel => getLevel).
//
// Revision 0.1.0.0  1997/08/29  David Hopwood
// + Original version, based on cryptix.core.Cryptix 1.0.9.
//
// $Endlog$
/*
 * Copyright (c) 1997, 1998 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.core;

import cryptix.CryptixProperties;

import java.io.PrintWriter;

/**
 * This class provides methods for determining where debugging output should be
 * sent, and what level of debugging is enabled for specific classes and
 * algorithms. Debugging levels are set in the <samp>Cryptix.properties</samp>
 * file.
 * <p>
 * <b>Copyright</b> &copy; 1997, 1998
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public class Debug
{
    private Debug() {} // static methods only


// Constants
//...........................................................................

    /**
     * Whether to compile the library with tracing support or not.
     * <p>
     * After changing this field, all classes should be recompiled in order
     * to make sure that the change has taken effect.
     */
    public static final boolean GLOBAL_TRACE = true;

    /**
     * Whether to compile the library with a standard level of debugging
     * support.
     * <p>
     * After changing this field, all classes should be recompiled
     * in order to make sure that the change has taken effect.
     */
    public static final boolean GLOBAL_DEBUG = true;

    /**
     * Whether to compile the library with additional debugging support that
     * is likely to slow it down.
     * <p>
     * After changing this field, all classes should be recompiled
     * in order to make sure that the change has taken effect.
     */
    public static final boolean GLOBAL_DEBUG_SLOW = false;

    /**
     * The PrintWriter to which debugging output is to be sent.
     */
    private static final PrintWriter err = new PrintWriter(System.err, true);


// Own methods
//...........................................................................

    /**
     * Return true if tracing is requested for a given class.<p>
     *
     * User indicates this by setting the tracing <code>boolean</code>
     * property for <i>label</i> in the <code>Cryptix.properties</code>
     * file. The property's key is "<code>Trace.<i>label</i></code>".<p>
     *
     * @param label  The name of a class.
     * @return True iff a boolean true value is set for a property with
     *      the key <code>Trace.<i>label</i></code>.
     */
    public static boolean isTraceable (String label) {
        String s = CryptixProperties.getProperty("Trace." + label);
        if (s == null) return false;
        return new Boolean(s).booleanValue();
    }

    /**
     * Returns the debug level for <i>label</i>. This is normally
     * given by the numeric provider property
     * "<code>Debug.Level.<i>label</i></code>".
     * <p>
     * If this property is not set, "<code>Debug.Level.*</code>" is
     * searched next. If neither property is set, or if the first property
     * found is not a valid decimal integer, then this method returns 0.
     */
    public static int getLevel(String label) {
        String s = CryptixProperties.getProperty("Debug.Level." + label);
        if (s == null) {
            s = CryptixProperties.getProperty("Debug.Level.*");
            if (s == null)
                return 0;
        }
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }

    /**
     * Returns the maximum of the debug levels for <i>label1</i> and <i>label2</i>.
     */
    public static int getLevel(String label1, String label2) {
        int m = getLevel(label1);
        int n = getLevel(label2);
        return m > n ? m : n;
    }

    /**
     * Returns the PrintWriter to which debugging output is to be sent.
     */
    public static PrintWriter getOutput() {
        return err;
    }
}
