// $Id: Cryptix.java,v 1.1.1.1 2002/08/27 12:32:09 grosbois Exp $
//
// $Log: Cryptix.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:09  grosbois
// Add cryptix 3.2
//
// Revision 1.7  1999/07/21 02:39:30  edwin
// Removing workaround as it reveals another bug in the JDK.
//
// Revision 1.6  1999/07/13 18:25:25  edwin
// Workaround to prevent a buggy JDK from unloading java.security.Security which in turn unloads the Cryptix provider
// This is a fix to: http://130.89.235.121/root/cryptix/old/cryptix/FAQ.html#bj_unloading
//
// Revision 1.5  1997/11/20 19:24:48  hopwood
// + cryptix.util.* name changes.
//
// Revision 1.4.1  1997/11/15  David Hopwood
// + Sorted out trivial conflicts between my version and Raif's.
//
// Revision 1.4  1997/11/10 07:31:32  raif
// *** empty log message ***
//
// Revision 1.3  1997/11/05 08:01:56  raif
// *** empty log message ***
//
// Revision 1.2  1997/11/04 19:33:30  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:56  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.0  1997/08/29  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.provider;

import cryptix.CryptixProperties;

import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.security.Provider;

/**
 * This class acts as a security provider for the Java Cryptography
 * Architecture. The mapping between cipher names and classes is stored
 * in the <samp>Cryptix.properties</samp> file (see
 * <a href=cryptix.CryptixProperties.html><samp>CryptixProperties</samp></a>).
 * <p>
 * SECURITY: In JavaSoft's version of JCE, Provider indirectly extends
 * Hashtable, which means that it would normally be possible to call
 * the <code>put</code>, <code>remove</code> or <code>clear</code>
 * methods to change properties without any security check (also the
 * <code>load</code> method from Properties). This is arguably a design
 * bug in JCA and/or the Properties class.
 * <p>
 * This class works around the potential security problem by not using
 * the Hashtable superclass. Instead it delegates to
 * <samp>CryptixProperties</samp>.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Jill Baker
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
public class Cryptix
extends Provider
{

// Constants
//...........................................................................

    static final long serialVersionUID = 2535048358772783954L;


// Constructor
//...........................................................................

    /** Constructs a Cryptix security provider object. */
    public Cryptix() {
        super("Cryptix", getVersionAsDouble(),         // name, version
            "<html>\n" +                                        // info
            "<head><title>" +
                CryptixProperties.getVersionString() +
            "</title></head>\n" +
            "<body>\n" +
                CryptixProperties.getHtmlInfo() +
            "</body>\n" +
            "</html>\n");
    }


// Own methods
//...........................................................................

    /**
     * Returns the Cryptix version number as a double. Note that Cryptix
     * version numbers are not normally written as decimals, but as triples
     * of integers. However, we need a double to pass to the Provider
     * constructor.
     */
    private static double getVersionAsDouble() {
        return CryptixProperties.getMajorVersion() +
            (double) CryptixProperties.getMinorVersion() / 100 +
            (double) CryptixProperties.getIntermediateVersion() / 10000;
    }

    public String toString() {
        return CryptixProperties.getVersionString();
    }


// Methods defined in java.util.Properties (excluding load, which is
// deliberately not supported).
//...........................................................................

    public void save(OutputStream os, String comment) {
        CryptixProperties.save(os, comment);
    }
    public String getProperty(String key) {
        return CryptixProperties.getProperty(key);
    }
    public String getProperty(String key, String defaultValue) {
        return CryptixProperties.getProperty(key, defaultValue);
    }
    public Enumeration propertyNames() {
        return CryptixProperties.propertyNames();
    }
    public void list(PrintStream out) {
        CryptixProperties.list(out);
    }
    public void list(PrintWriter out) {
        CryptixProperties.list(out);
    }
}
