// $Id: DosFilter.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: DosFilter.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.1.1.1  1997/11/20 21:06:50  hopwood
// + Moved these classes here from cryptix.io.*.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.2  1997/08/06  David Hopwood
// + Renamed set to setMask.
// + Added getMask method, and shortcut (String) constructor.
//
// Revision 0.1.0.1  1997/08/02  David Hopwood
// + Changed debugging to be similar to other classes.
// + Added more documentation.
//
// Revision 0.1.0.0  1997/06/0?  Raif Naffah
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.io;

import cryptix.util.core.Debug;

import java.io.PrintWriter;
import java.io.File;
import java.io.FilenameFilter;

/**
 * A java.io.FilenameFilter class that filters directory contents according
 * to traditional DOS wildcard conventions; i.e. the filter is split into
 * two parts: a filename and an extension. In each part '*' replaces any
 * number of characters, and '?' replaces any one character.
 * <p>
 * Note: this is slightly different to the Windows 95/NT conventions, where
 * filename and extension are not treated separately. E.g. "*" in 95/NT
 * matches all files, whereas for this class "*.*" would have to be used.
 * <p>
 *
 * <b>Copyright</b> &copy; 1995-1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
public class DosFilter
implements FilenameFilter
{

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = Debug.GLOBAL_DEBUG;
    private static int debuglevel = DEBUG ? Debug.getLevel("DosFilter") : 0;
    private static final PrintWriter err = DEBUG ? Debug.getOutput() : null;
    private static void debug(String s) { err.println("DosFilter: " + s); }


// Variables & constants
//...........................................................................

    private char[] nameMask;    // the filename mask
    private char[] extMask;     // the file extension mask


// Constructors
//...........................................................................

    /**
     * Constructs a DosFilter that matches all files.
     */
    public DosFilter() { reset(); }

    /**
     * Constructs a DosFilter for files that match <i>mask</i>.
     *
     * @param  mask  a string that may contain '*' and '?' characters.
     */
    public DosFilter(String mask) { setMask(mask); }


// FilenameFilter interface implementation
//...........................................................................

    /**
     * FilenameFilter interface implementation to handle ambiguous filename
     * selection in a given directory.
     * <p>
     * Tests if a specified file should be included in a file list of a given
     * directory.
     *
     * @param  dir   the directory in which the File object name was found.
     * @param  name  the name of the File object.
     * @return true if the name is that of a File object that should be
     *     included in the file list; false otherwise. Directories
     *     are always accepted, disregarding the set filter mask.
     */
    public boolean accept(File dir, String name) {
        // if the name is that of a directory accept it
        if (new File(dir, name).isDirectory())
            return true;

        // otherwise see if it matches the filter selection mask
if (DEBUG && debuglevel >= 3) debug("filtering " + dir.getAbsolutePath() + File.separator + name);

        // divide into name and extension substrings
        char[] nameChars = null, extChars = null;

        int n = name.indexOf(".");
        if (n == -1) {                // name with no extension
            if (extMask != null) {    // yet user wants to filter with some extension
if (DEBUG && debuglevel >= 4) debug(name + " FAILED 1\n");
                return false;
            } else {
                nameChars = name.toCharArray();
            }
        } else {
            nameChars = name.substring(0, n).toCharArray();
            extChars = name.substring(n + 1).toCharArray();
        }
        int i, j;                                   // index into the mask string
        boolean advance = false;
        for (i = 0, j = 0; nameMask != null && i < nameChars.length;) {
            char c = nameMask[j];
            if (c == '*') {                         // goto end of name and mask
                i = nameChars.length;
                j = nameMask.length;
            } else if (c != '?' && c != nameChars[i]) {
if (DEBUG && debuglevel >= 4) debug(name + " FAILED 2\n");
                return false;
            } else {                                // go on
                i++;
                j++;
                if (j == nameMask.length)           // end of name mask
                    if (i < nameChars.length) {     // name shorter than mask
if (DEBUG && debuglevel >= 4) debug(name + " FAILED 3\n");
                        return false;
                    }
            }
        }
        // now filter the extension
        advance = false;
        for (i = 0, j = 0; extMask != null && i < extChars.length;) {
            char c = extMask[j];
            if (c == '*') {                         // that's it
if (DEBUG && debuglevel >= 4) debug(name + " OK 1\n");
                return true;
            } else if (c != '?' && c != extChars[i]) {
if (DEBUG && debuglevel >= 4) debug(name + " FAILED 4\n");
                return false;
            } else {                                // go on
                i++;
                j++;
                if (j == extMask.length)            // end of extension mask
                    if (i < extChars.length) {      // extension shorter than mask
if (DEBUG && debuglevel >= 4) debug(name + " FAILED 5\n");
                        return false;
                    }
            }
        }
if (DEBUG && debuglevel >= 4) debug(name + " OK 2\n");
        return true;
    }


// Own methods
//...........................................................................

    /**
     * Resets the mask so that all files will be matched.
     */
    public void reset() { nameMask = extMask = null; }

    /**
     * Sets the mask this filter will be using from now on.
     *
     * @param  mask  a string that may contain '*' and '?' characters.
     */
    public void setMask (String mask) {
        // don't allow masks to start with '.'
        if (mask.startsWith("."))
            mask = "*" + mask;
        // nor shouldn't end with '.'
        if (mask.endsWith("."))
            mask += "*";

        int n = mask.indexOf(".");
        if (n == -1) {                        // files with no extensions required
            // anything after an '*' in a filter is ignored
            n = mask.indexOf("*");
            nameMask = (n == -1) ?
                mask.toCharArray() :
                mask.substring(0, n + 1).toCharArray();
        } else {
            String s = mask.substring(0, n);
            // anything after an '*' in a name filter is ignored
            n = s.indexOf("*");
            nameMask = (n == -1) ?
                s.toCharArray() :
                s.substring(0, n + 1).toCharArray();
            s = mask.substring(n + 2);
            // anything after an '*' in an extension filter is ignored
            n = s.indexOf("*");
            extMask = (n == -1) ?
                s.toCharArray() :
                s.substring(0, n + 1).toCharArray();
        }
if (DEBUG && debuglevel >= 3) debug("set filter file name: \"" + nameMask + "\"");
if (DEBUG && debuglevel >= 3) debug("           file ext.: \"" + extMask + "\"");
    }

    /**
     * Gets the current value of the mask for this filter.
     */
    public String getMask() {
        return
            (nameMask != null ? new String(nameMask) : "*") +
            "." +
            (extMask != null ? new String(extMask) : "*");
    }

    public String toString() {
        return getMask();
    }
}
