// $Id: Install.java,v 1.1.1.1 2002/08/27 12:32:09 grosbois Exp $
//
// $Log: Install.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:09  grosbois
// Add cryptix 3.2
//
// Revision 1.9  1998/03/11 00:30:51  zox
// Cleaning up mess with good/bad exit values...
//
// Revision 1.8  1998/01/28 05:59:49  hopwood
// + Fixes for OS/2 (not tested on OS/2 yet).
//
// Revision 1.7  1998/01/10 04:56:29  hopwood
// + This class now has no dependencies on any other Cryptix classes.
//
// Revision 1.6  1998/01/10 04:52:57  hopwood
// + Fixed silly error in 1.5.
//
// Revision 1.5  1998/01/10 04:49:02  hopwood
// + Committed changes below.
//
// Revision 1.4.1  1997/12/31  hopwood
// + Return an error code depending on whether the provider was installed.
// + Changed constants to instance variables.
//
// Revision 1.4  1998/01/08 16:43:25  iang
// + Added exit(1) so that installation scripts will stop if provider is not
//   installed, and therefore later tests don't blow up needlessly.
//
// Revision 1.3  1997/11/20 19:24:48  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.2.1  1997/11/15  David Hopwood
// + Sorted out trivial conflicts between my version and Raif's.
//
// Revision 1.2  1997/11/10 07:31:32  raif
// + PROVIDER_CLASS is now cryptix.provider.Cryptix.
// + Removed reference to core.Cryptix.
// + Use CryptixProperties.
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.6  1997/08/30  David Hopwood
// + Changed PROVIDER_CLASS to (?)
//
// Revision 0.1.0.5  1997/08/14  David Hopwood
// + Changed PROVIDER_CLASS to "cryptix.Cryptix".
//
// Revision 0.1.0.4  1997/07/26  David Hopwood
// + Renamed PROVIDER to PRODUCT_NAME, to be consistent with the
//   PropertySupport class.
//
// Revision 0.1.0.3  1997/07/21  David Hopwood
// + Instead of searching for the highest provider number and adding one,
//   search for the first unused provider number. Print a warning if the
//   number one higher than that already exists. This strategy fits better
//   with how Sun's implementation of the Security class looks for providers.
//
// Revision 0.1.0.2  1997/07/12  David Hopwood
// + Cosmetic changes.
//
// Revision 0.1.0.1  1997/07/10  R. Naffah
// + Modified to cater for revision 0.1.0.1 of provider class.
// + Changed the constants value to same.
//
// Revision 0.1.0.0  1997/??/??  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.Enumeration;

/**
 * Installs Cryptix in the java.security file:
 * <ol>
 *   <li> If there is no java.security file, make one, and exit.
 *        <p>
 *   <li> Find the first value of <i>n</i>, starting at 1, for which a line
 *        of the form
 *        <blockquote>
 *          <code>security.provider.<i>n</i>=<i>*</i></code>
 *        </blockquote>
 *        does <em>not</em> exist.
 *        <p>
 *   <li> If a property of the form
 *        <blockquote>
 *          <code>security.provider.<i>*</i>=cryptix.provider.Cryptix</code>
 *        </blockquote>
 *        was found during the previous step, exit.
 *        <p>
 *   <li> Add the following line to the end of the file:
 *        <blockquote>
 *          <code># Added by Cryptix-Java x.x.x installation program:</code><br>
 *          <code>security.provider.<i>n</i>=cryptix.provider.Cryptix</code>
 *        </blockquote>
 * </ol>
 * <p>
 * An error code is returned as follows (see the source for the values of these
 * constants):
 * <ul>
 *   <li> INSTALLED -         the provider was installed by this program.
 *   <li> ALREADY_INSTALLED - the provider was already installed.
 *   <li> NOT_INSTALLED -     the provider was not installed (for example because
 *                            the java.security file was not readable or writable).
 * </ul>
 * <p>
 * SECURITY: this class must be package-private, since untrusted code should not
 * be able to run it.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
final class Install
{
    private PrintWriter out;
    private String product_name;
    private String provider_class;
    private String version_string;
    private int errorcode;

    //Should cryptix.util.test.TestException be included for these exit values?
    // COMPLETE_SUCCESS = 10;
    // COMPLETE_FAILURE = 1;
    private static final int INSTALLED = 10;
    private static final int ALREADY_INSTALLED = 10; // for the time being
    private static final int NOT_INSTALLED = 1;

    /**
     * The entry point for this application. No arguments are needed.
     */
    public static void main(String[] args) {
        Install x = new Install(new PrintWriter(System.out, true),
            "Cryptix", "cryptix.provider.Cryptix", "Cryptix V3");

        try {
            x.run();
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println("Cryptix has not been installed.");
        }
        System.exit(x.getErrorCode());
    }

    Install(PrintWriter out, String product_name, String provider_class, String version_string) {
        this.out = out;
        this.product_name = product_name;
        this.provider_class = provider_class;
        this.version_string = version_string;
        this.errorcode = NOT_INSTALLED;
    }

    int getErrorCode() { return errorcode; }

    void run() {
        String javaHome = System.getProperty("java.home");
        out.println("Examining the Java installation at " + javaHome);
        out.println();

        String nl = System.getProperty("line.separator");

        Properties properties = new Properties();
        File securityDir = new File(javaHome,
                               "lib" + File.separator +
                               "security" + File.separator);
        File securityPropsFile = new File(securityDir, "java.security");

        try {
            try {
                BufferedInputStream bis = new BufferedInputStream(
                                          new FileInputStream(securityPropsFile));
                properties.load(bis);
                bis.close();
            } catch (FileNotFoundException e) {
                try {
                    // curse OS/2 and its incompatible JVM.
                    securityPropsFile = new File(securityDir, "JAVA.SEC");
                    BufferedInputStream bis = new BufferedInputStream(
                                              new FileInputStream(securityPropsFile));
                    properties.load(bis);
                    bis.close();
                } catch (FileNotFoundException e2) {
                    securityPropsFile = new File(securityDir, "java.security");
                    throw e;
                }
            }
        } catch (FileNotFoundException e) {
            try {
                if (securityDir.exists()) {
                    if (!securityDir.isDirectory()) {
                        out.println("The installation program needs to create the directory");
                        out.println("  " + securityDir.getPath() + File.separator);
                        out.println("but a file already exists with that name.");
                        throw new RuntimeException("Could not create directory.");
                    }
                } else {
                    securityDir.mkdirs();
                }
                DataOutputStream dos = new DataOutputStream(
                                       new BufferedOutputStream(
                                       new FileOutputStream(securityPropsFile)));
                dos.writeBytes(
                    "# " + version_string + " (do not edit or delete this line)" + nl +
                    "# This is the \"master security properties file\"." + nl +
                    "#" + nl +
                    "# The " + product_name + " installation program was unable to find an existing" + nl +
                    "# java.security file, so it created this one." + nl +
                    nl +
                    "security.provider.1=" + provider_class + nl
                );
                dos.close();
                errorcode = INSTALLED;

                out.println("The file " + securityPropsFile);
                out.println("has been created.");
                return;
            } catch (IOException e2) {
                errorcode = NOT_INSTALLED;

                out.println("The file " + securityPropsFile);
                out.println("could not be created because of an I/O exception.");
                throw new RuntimeException(e2.toString());
            }
        } catch (IOException e) {
            errorcode = NOT_INSTALLED;

            out.println("Failed to load the java.security file.");
            throw new RuntimeException(e.toString());
        } catch (SecurityException e) {
            errorcode = NOT_INSTALLED;

            out.println("Not allowed to load the java.security file.");
            throw new RuntimeException(e.toString());
        }

        int nextProvider = 1;
        for (; ; nextProvider++) {
            String value = properties.getProperty("security.provider." + nextProvider);

            if (value == null)
                break;
            if (value.equals(provider_class)) {
                errorcode = ALREADY_INSTALLED;

                out.println(product_name + " is already installed.");
                return;
            }
        }

        if (properties.getProperty("security.provider." + (nextProvider+1)) != null) {
            out.println("Warning: additional providers may have been added that were not previously");
            out.println("recognized, because a gap in the sequence of provider numbers has been filled.");
            out.println("You should edit the java.security file and check that it is correct.");
            out.println();
        }

        String linesToAdd = nl + "# Added by " + version_string + " installation program:" + nl +
            "security.provider." + nextProvider + "=" + provider_class + nl;

        try {
            RandomAccessFile file = new RandomAccessFile(securityPropsFile, "rw");
            long fileLen = securityPropsFile.length();
            file.seek(fileLen);
            file.writeBytes(linesToAdd);
            file.close();
            errorcode = INSTALLED;

            out.println("The following lines were added to");
            out.println("  " + securityPropsFile + ":");
            out.println(linesToAdd);
            out.println("To uninstall " + product_name + ", remove these lines manually.");
        } catch (IOException e) {
            errorcode = NOT_INSTALLED;

            out.println("The file " + securityPropsFile);
            out.println("could not be written to because of an I/O exception.");
            throw new RuntimeException(e.toString());
        } catch (SecurityException e) {
            errorcode = NOT_INSTALLED;

            out.println("The file " + securityPropsFile);
            out.println("could not be written to because of a security exception.");
            throw new RuntimeException(e.toString());
        }
    }
}
