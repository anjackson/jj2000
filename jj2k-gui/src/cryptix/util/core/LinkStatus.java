// $Id: LinkStatus.java,v 1.1.1.1 2002/08/27 12:33:06 grosbois Exp $
//
// $Log: LinkStatus.java,v $
// Revision 1.1.1.1  2002/08/27 12:33:06  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/11/20 21:04:51  hopwood
// + Moved these classes here from cryptix.core.util.*.
//
// Revision 1.1  1997/11/05 16:48:02  raif
// *** empty log message ***
//
//
// 1.0.1        21 Jul 1997     David Hopwood
// + Added useNative, checkNative and setNative methods.
//
// 1.0.0        21 Jul 1997     David Hopwood
// + Start of history (Cryptix 2.2.0a).
//
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.core;

/**
 * A class providing information about the linking status of a
 * native library (whether it was loaded successfully, its required
 * and actual version numbers, etc.)
 * <p>
 * The status of a library used by a particular Cryptix class can
 * be found by calling the static method <code>getLinkStatus()</code>
 * on that class.
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * @author  David Hopwood
 * @version 1.0.1, 1997/07/21
 * @since   Cryptix 2.2.0a, 2.2.2
 */
public interface LinkStatus
{
    /** Returns the required major version number. */
    int getRequiredMajorVersion();

    /** Returns the required minor version number. */
    int getRequiredMinorVersion();

    /** Returns the library name. */
    String getLibraryName();

    /** Returns the actual major version number. */
    int getMajorVersion();

    /** Returns the actual minor version number. */
    int getMinorVersion();

    /**
     * Returns true if the library was loaded. It may or may not be the
     * correct version.
     */
    boolean isLibraryLoaded();

    /** Returns true if the library was loaded successfully. */
    boolean isLibraryCorrect();

    /** Returns true if native code is being used. */
    boolean useNative();

    /**
     * Returns an error string describing why the library failed to load,
     * or null if there was no error.
     *
     * @return the error string, or null if no error occured
     */
    String getLinkErrorString();

    /**
     * Checks that the native library is being used (i.e. it loaded successfully,
     * has valid version numbers, and has not been disabled).
     *
     * @exception UnsatisfiedLinkError if the library is not being used
     */
    void checkNative() throws UnsatisfiedLinkError;

    /**
     * Enables or disables the native code. By default, native code
     * is used whenever its library can be loaded correctly.
     * This method can be used to disable native linking (and re-enable
     * it) for a specific class.
     *
     * @param enable    true if native code should be used.
     */
    void setNative(boolean enable);
}
