// $Id: FileUtil.java,v 1.1.1.1 2002/08/27 12:33:06 grosbois Exp $
//
// $Log: FileUtil.java,v $
// Revision 1.1.1.1  2002/08/27 12:33:06  grosbois
// Add cryptix 3.2
//
// Revision 1.1  1998/01/27 07:20:00  raif
// *** empty log message ***
//
// Revision 1.0  1998/01/27  raif
// + original version.
//
// $Endlog$
/*
 * Copyright (c) 1997, 1998 Systemics Ltd on behalf of
 * the Cryptix Development Team. All rights reserved.
 */
package cryptix.util.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Static methods for processing File utilitarian tasks.<p>
 *
 * <b>Copyright</b> &copy; 1997, 1998
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.<p>
 *
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  Raif S. Naffah
 * @since   Cryptix 3.0.4
 */
public class FileUtil // implicit default (package-private) constructor
{
// Utility (static) methods
//...........................................................................

    /**
     * Load a Properties object from a file given its name. The search for
     * the specified file is carried out on all the files and directories
     * specified in the CLASSPATH environment variable (contents of the
     * java system property "java.class.path"). The search is also done on
     * the top level contents of ".zip" and ".jar" files. Finally the search
     * stops after the first successful completion of the loading process; ie.
     * after the first <name>.properties file was found and was successfully
     * used to load the specified properties.
     *
     * @param  properties  The java.util.Properties object to load.
     * @param  name        The name of the properties file. If it does not
     *                     end with the suffix ".properties" one will be
     *                     added.
     * @return True iff the properties object was loaded correctly
     */
    public static boolean loadProperties (Properties properties, String name) {
        if (! name.endsWith(".properties")) name += ".properties";
        StringTokenizer list = new
            StringTokenizer(System.getProperty("java.class.path"),
                            File.pathSeparator);
        while (list.hasMoreTokens())
            if (doFileOrDir(properties, name, new File(list.nextToken())))
                return true;
        return false;
    }

    static boolean doFileOrDir (Properties properties, String name, File f) {
        if (f.isDirectory()) {
            String[] list = f.list(new IdentityFilter(name));
            int n = list.length;
            for (int i = 0; i < n; i++)
                if (doFileOrDir(properties, name, new File(f, list[i])))
                    return true;
            return false;
        }
        if (! f.isFile()) return false;

        String it = f.getName();
        if (it.equals(name))
            try {
                BufferedInputStream in = new
                    BufferedInputStream(new FileInputStream(f));
                properties.load(in);
                in.close();
                return true;
            }
            catch (FileNotFoundException x1) {}
            catch (IOException x2) {}

        it = it.toUpperCase();
        if (! (it.endsWith(".ZIP") || it.endsWith(".JAR"))) return false;

        ZipInputStream zip;
        try { zip = new ZipInputStream(new FileInputStream(f)); }
        catch (FileNotFoundException x) { return false; }
        boolean result = false;
        ZipEntry ze;
        try {
            while ((ze = zip.getNextEntry()) != null) {
                if (ze.isDirectory()) continue;
                it = ze.getName();
                int n;
                if (it.endsWith(name)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
                    byte[] buffer = new byte[512];
                    while ((n = zip.read(buffer)) != -1) out.write(buffer, 0, n);
                    BufferedInputStream in = new BufferedInputStream(
                        new ByteArrayInputStream(out.toByteArray()));
                    properties.load(in);
                    in.close();
                    result = true;
                    break;
                }
            }
        }
        catch (IOException x1) {}
        finally {
            try { zip.close(); }
            catch (IOException x2) {}
        }
        return result;
    }

    /**
     * same as above given a .zip or .jar file object. The difference with
     * this method is that the search does not go deeper than the top level;
     * ie. no directory recursion is done.
     */
    static boolean doZip (Properties properties, String name, File f) {
        ZipInputStream zip;
        try { zip = new ZipInputStream(new FileInputStream(f)); }
        catch (FileNotFoundException x) { return false; }
        boolean result = false;
        ZipEntry ze;
        try {
            while ((ze = zip.getNextEntry()) != null) {
                if (ze.isDirectory()) continue;
                String it = ze.getName();
                int n;
                if (it.endsWith(name)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
                    byte[] buffer = new byte[512];
                    while ((n = zip.read(buffer)) != -1) out.write(buffer, 0, n);
                    BufferedInputStream in = new BufferedInputStream(
                        new ByteArrayInputStream(out.toByteArray()));
                    properties.load(in);
                    in.close();
                    result = true;
                    break;
                }
            }
        }
        catch (IOException x1) {}
        finally {
            try { zip.close(); }
            catch (IOException x2) {}
        }
        return result;
    }


    // (inner class)
    // An implementation of a FilenameFilter that checks for a given (by name)
    // file's exitence in a tree
    //.......................................................................

    static class IdentityFilter
    implements FilenameFilter
    {
        private String it;                         // the file name to look for
        public IdentityFilter (String name) { it = name; }       // constructor
        public boolean accept (File dir, String name) {    // the filter method
            File f = new File(dir, name);
            if (f.isDirectory()) return true;
            if (f.isFile() && name.equals(it)) return true;
            return false;
        }
    }
}
