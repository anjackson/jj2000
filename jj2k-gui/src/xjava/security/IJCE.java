// $Id: IJCE.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: IJCE.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.5  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.4  1999/07/13 18:08:27  edwin
// Fixed references to methods in java.security.Security that were protected in JDK1.1, but private in JDK1.2.
// It works now on both 1.1 and 1.2
//
// Revision 1.3  1997/12/14 17:58:49  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/12/14  hopwood
// + Minor comment change.
// + Version is now 1.1.
//
// Revision 1.2  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 1.1.1.1.1  1997/11/17  David Hopwood
// + Renamed getInternalDebugLevel to getDebugLevel (and changed the
//   property name it looks for).
// + Added getReleaseDate method.
// + Calculate release date from the CVS date tag.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.6  1997/08/21  David Hopwood
// + Added getDebugOutput method, for redirecting debugging
//   messages (currently this always returns System.err).
// + Added SNAPSHOT_DATE to the version string, so that different
//   snapshots of the same release can be distinguished.
//
// Revision 0.1.0.5  1997/08/17  David Hopwood
// + If an algorithm class is not found, we now search the remaining
//   providers rather than aborting immediately. This makes it easier
//   to distribute subsets of a security provider, without having to
//   change its properties, or change the order of providers in the
//   java.security file.
//   Unfortunately it is less efficient than the normal mechanism
//   (in cases where NoSuchAlgorithmException would previously have
//   been thrown), because to implement it we have to call
//   Security.getProviders() and search each of them in order :-(.
//   Caching implementation classes would produce incorrect results
//   when a provider is deleted.
// + Replaced getImplementationClassName with two getImplementationClass
//   methods.
//
// Revision 0.1.0.4  1997/08/12  David Hopwood
// + Changed debugging to use properties file.
// + Types are now configured in the IJCE.properties file, rather than
//   hardwiring the corresponding class as "java.security." + type.
// + Renamed getImpl methods to getImplementation, and made them public
//   (with a different argument order).
// + Renamed getEngineClassName to getImplementationClassName, and made
//   it public.
//
// Revision 0.1.0.3  1997/08/02  David Hopwood
// + Added getInternalDebugLevel method, for debugging non-algorithm
//   classes.
// + Added code to print the version, statically configured providers,
//   and library path in main.
//
// Revision 0.1.0.2  1997/07/17  David Hopwood
// + Accepted changes made in 1.0.1 as permanent by removing commented-out code.
// + static {} block to initialize IJCE_SecuritySupport is no longer needed.
// + Renamed getSandboxedProvider[s] to getProvider[s]Internal. This follows
//   naming conventions used elsewhere.
//
// Revision 0.1.0.1  1997/07/10  R. Naffah
// + Moved most of the static code to the IJCE_SecuritySupport class.
//   That code is now executed by invoking the IJCE_SecuritySupport.init()
//   method.
// + ensureLoaded() is not needed anymore.
// + Got rid of the static initialisation of netscape.security-related static
//   vars (getProvidersTarget and privMgr) and replaced them with ad-hoc
//   initialisation in methods where used.
// + Enclosed netscape.security calls within try/catch.
//
// Revision 0.1.0.0  1997/07/??  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import netscape.security.ForbiddenTargetException;
import netscape.security.PrivilegeManager;
import netscape.security.Target;

/**
 * The IJCE class provides an interface to features that were not present
 * in JavaSoft's initial version of JCE. Hopefully the getAlgorithms and/or
 * the enable/disableTracing methods will be incorporated into a later version
 * of the standard.
 * <p>
 * If you bundle this classfile, and the other classfiles in java.security.* that
 * have names starting "IJCE_" with your application, these methods should work
 * as documented even if linked against JavaSoft's implementation, so using them
 * does not result in any loss of compatibility.
 * <p>
 * This class also defines the version number of the IJCE library:
 * <ul>
 *   <li> The major version is intended to signal significant
 *        changes in compatibility or style.
 *   <li> The minor version is intended to signal small changes
 *        in compatibility and new, compatible additions.
 *   <li> The intermediate version is incremented for bug-fix and alpha
 *        releases.
 * </ul>
 * <p>
 * Version numbers should be written as a triple of integers, not as a decimal.
 * If the last number is omitted it is assumed to be 0, so for example, version
 * 1.10 is after version 1.2.
 * <p>
 * The IJCE version number corresponding to this documentation is 1.1.
 * (This is independent of the version numbers of any libraries that IJCE may be
 * distributed with, such as <a href="http://www.systemics.com/docs/cryptix">Cryptix</a>.)
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   IJCE 1.0.1
 */
public class IJCE
{
    private IJCE() {} // static methods only

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = true;
    private static final int debuglevel = DEBUG ? IJCE.getDebugLevel("IJCE") : 0;
    private static final PrintWriter err = new PrintWriter(System.err, true);


// Constants and variables
//...........................................................................

    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;
    private static final int INTER_VERSION = 0;

    /**
     * This field should be true if this is a snapshot release.
     */
    private static final boolean IS_SNAPSHOT = true;

    private static final String CVS_DATE = "$Date: 2002/08/27 11:49:30 $";

    private static Target getProvidersTarget;
    private static PrivilegeManager privMgr;
    private static Hashtable typeToClass = new Hashtable();


// Own methods
//...........................................................................

    /**
     * Gets the standard names of all algorithms of the given type
     * implemented by a provider.
     * <p>
     * Possible values for <i>type</i> include "Cipher", "Mode",
     * "PaddingScheme", "MessageDigest", "Signature", "KeyGenerator",
     * and "KeyPairGenerator". Other types can be configured in the
     * IJCE.properties file.
     */
    public static String[] getAlgorithms(Provider provider, String type) {
        if (getClassForType(type) == null)
            return new String[0];

        String typedot = type + ".";
        Vector algorithms = new Vector();
        Enumeration enum = provider.propertyNames();
        while (enum.hasMoreElements()) {
            String key = (String) (enum.nextElement());
            if (key.startsWith(typedot))
                algorithms.addElement(key.substring(typedot.length()));
        }
        String[] buf = new String[algorithms.size()];
        algorithms.copyInto(buf);
        return buf;
    }

    /**
     * Gets the standard names of all algorithms of the given type
     * implemented by any installed provider. Algorithm names are not
     * duplicated if they are supported by more than one provider
     * (but specialized cipher/mode implementations are treated as
     * distinct, for example "DES/CBC" is different from "DES").
     * <p>
     * Possible values for <i>type</i> include "Cipher", "Mode",
     * "PaddingScheme", "MessageDigest", "Signature", "KeyGenerator",
     * and "KeyPairGenerator". Other types can be configured in the
     * IJCE.properties file.
     * <p>
     * The built-in padding scheme "NONE", or the built-in mode "ECB"
     * are included if applicable.
     */
    public static String[] getAlgorithms(String type) {
        if (getClassForType(type) == null)
            return new String[0];

        String typedot = type + ".";
        // really need a set, but a Hashtable will do.
        Hashtable algorithms = new Hashtable();
        if (type.equals("PaddingScheme"))
            algorithms.put("NONE", "");
        else if (type.equals("Mode"))
            algorithms.put("ECB", "");

        Provider[] providers = getProvidersInternal();

        for (int i = 0; i < providers.length; i++) {
            Enumeration enum = providers[i].propertyNames();
            while (enum.hasMoreElements()) {
                String key = (String) (enum.nextElement());
                if (key.startsWith(typedot))
                    algorithms.put(key.substring(typedot.length()), "");
            }
        }
        String[] buf = new String[algorithms.size()];
        Enumeration enum = algorithms.keys();
        int n = 0;
        while (enum.hasMoreElements())
            buf[n++] = (String) (enum.nextElement());

        return buf;
    }

    /**
     * Enables tracing of calls to SPI methods for the algorithm object
     * <i>obj</i>. Output is sent to the PrintWriter <i>out</i>. This method
     * will return true if tracing was enabled successfully, or false if it
     * is not supported for this class.
     * <p>
     * Some trivial methods (such as those that get the block size) are not
     * traced.
     * <p>
     * This can produce a lot of Output, and it should only be used for
     * debugging, when the data being processed by the algorithm object is not
     * secret.
     * <p>
     * Tracing only works for classes whose implementations are being provided
     * by IJCE. If there is a non-IJCE implementation of the base class for
     * <i>obj</i> (Cipher, MessageDigest, etc.) earlier in the CLASSPATH, it
     * will probably not support tracing, and false will be returned.
     * <p>
     * Note that the IJCE library includes an implementation of
     * java.io.PrintWriter that will be used automatically when running
     * on Java 1.0.2.
     */
    public static boolean enableTracing(Object obj, PrintWriter out) {
        if (obj instanceof IJCE_Traceable) {
            ((IJCE_Traceable) obj).enableTracing(out);
            return true;
        }
        return false;
    }

    /**
     * Enables tracing of calls to SPI methods for the algorithm object
     * <i>obj</i>, with Output sent to the default location, given by
     * <code>getDebugOutput()</code>.
     */
    public static boolean enableTracing(Object obj) {
        return enableTracing(obj, err);
    }

    /**
     * Disables tracing of SPI methods for <i>obj</i>. Returns silently
     * if tracing was not enabled or is not supported for this object.
     */
    public static void disableTracing(Object obj) {
        if (obj instanceof IJCE_Traceable)
            ((IJCE_Traceable) obj).disableTracing();
    }

    /**
     * Expands the possible alias <i>algorithm</i> to a standard name. If
     * <i>algorithm</i> is not an alias, it is returned as-is. This method
     * does not check whether a corresponding algorithm implementation exists.
     * <p>
     * Possible values for <i>type</i> include "Cipher", "Mode",
     * "PaddingScheme", "MessageDigest", "Signature", "KeyGenerator",
     * and "KeyPairGenerator". Other types can be configured in the
     * IJCE.properties file.
     *
     * @param  algorithm    the possible alias
     * @param  type         the type of algorithm
     * @return the standard name
     */
    public static String getStandardName(String algorithm, String type) {
        String temptype = "Alias."+type;
        String standardName = Security.getAlgorithmProperty(algorithm, temptype);
        return (standardName != null) ? standardName : algorithm;
    }

    /**
     * Returns an object configured to the specified type. All providers will
     * be searched in order of preference.
     * <p>
     * Possible values for <i>type</i> include "Cipher", "Mode",
     * "PaddingScheme", "MessageDigest", "Signature", "KeyGenerator",
     * and "KeyPairGenerator". Other types can be configured in the
     * IJCE.properties file.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @param  type         the type of algorithm.
     * @return the implementation object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available in the environment.
     */
    public static Object getImplementation(String algorithm, String type)
    throws NoSuchAlgorithmException {
        try {
            return IJCE.getImplementation(algorithm, null, type);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    /**
     * Returns an object configured to the specified type. Provider can be null,
     * in which case all providers will be searched in order of preference.
     * <p>
     * Possible values for <i>type</i> include "Cipher", "Mode",
     * "PaddingScheme", "MessageDigest", "Signature", "KeyGenerator",
     * and "KeyPairGenerator". Other types can be configured in the
     * IJCE.properties file.
     *
     * @param  algorithm    the standard name or an alias for the algorithm.
     * @param  provider     the string name of the provider.
     * @param  type         the type of algorithm.
     * @return the implementation object.
     * @exception NoSuchAlgorithmException if the algorithm is not
     *                      available from the provider.
     * @exception NoSuchProviderException if the provider is not
     *                      available in the environment.
     */
    public static Object getImplementation(String algorithm, String provider, String type)
    throws NoSuchAlgorithmException, NoSuchProviderException {
        Class cl = getImplementationClass(algorithm, provider, type);

        String error;
        try {
            return cl.newInstance();
        } catch (LinkageError e) {
            error = " could not be linked correctly.\n" + e;
        } catch (InstantiationException e) {
            error = " cannot be instantiated.\n" + e;
        } catch (IllegalAccessException e) {
            error = " cannot be accessed.\n" + e;
        }
        throw new NoSuchAlgorithmException("class configured for " + type + ": " +
            cl.getName() + error);
    }

    /**
     * Given an algorithm name (which may be an alias) and type, returns the
     * corresponding algorithm class from any provider.
     */
    public static Class
    getImplementationClass(String algorithm, String type)
    throws NoSuchAlgorithmException {
        try {
            return getImplementationClass(algorithm, null, type);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    /**
     * Given an algorithm name (which may be an alias), a provider name, and
     * a type, returns the corresponding algorithm class.
     */
    public static Class
    getImplementationClass(String algorithm, String provider, String type)
    throws NoSuchAlgorithmException, NoSuchProviderException {

        String standardName = getStandardName(algorithm, type);
        Class target = getClassForType(type);
        if (target == null)
            throw new NoSuchAlgorithmException(type + " is not a configured type");

        Class cl = getClassCandidate(standardName, provider, type);

        // replace this with target.isAssignableFrom(cl) for a 1.1-only release.
        if (IJCE_Java10Support.isAssignableFrom(target, cl))
            return cl;

        throw new NoSuchAlgorithmException("class configured for " + type +
            ": " + cl.getName() + " is not a subclass of " + target.getName());
    }

    private static Class
    getClassCandidate(String standardName, String provider, String type)
    throws NoSuchAlgorithmException, NoSuchProviderException {
        String property = type + "." + standardName;

        if (provider == null) {
            String classname;
            Class cl;

            // the first class matching this name does not exist, so search all
            // providers.
            Provider[] providers = getProvidersInternal();
            for (int i = 0; i < providers.length; i++) {
                classname = providers[i].getProperty(property);
                if (classname != null) {
                    try {
                        cl = findEngineClass(classname, type);
                        if (cl != null)
                            return cl;
                    } catch (NoSuchAlgorithmException e) {}
                }
            }
            throw new NoSuchAlgorithmException("algorithm " + standardName +
                " is not available.");
        }

        Provider providerObj = getProviderInternal(provider);
        if (providerObj == null)
            throw new NoSuchProviderException("provider " + provider +
                " is not available.");

        String classname = providerObj.getProperty(property);
        if (classname != null) {
            Class cl = findEngineClass(classname, type);
            if (cl != null)
                return cl;
        }
        throw new NoSuchAlgorithmException("algorithm " + standardName +
            " is not available from provider " + provider);
    }

    private static Class
    findEngineClass(String classname, String type)
    throws NoSuchAlgorithmException {
        String error;
        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodError e) {
            error = " does not have a zero-argument constructor.\n" + e;
        } catch (LinkageError e) {
            error = " could not be linked correctly.\n" + e;
        }
        throw new NoSuchAlgorithmException("class configured for " + type + ": " +
            classname + error);
    }
    
    /**
     * Returns a Target that can be passed to
     * <code>PrivilegeManager.enablePrivilege(...)</code>, in order to request
     * permission for an action that requires the user's trust.
     * <p>
     * Currently the following target names are recognized:
     * <ul>
     *   <li> AddSecurityProvider - add a new security provider to the system.
     *   <li> RemoveSecurityProvider - remove a security provider from the system.
     *   <li> SecurityPropertyRead - read the security properties.
     *   <li> SecurityPropertyWrite - change the value of any security property.
     *   <li> GetSecurityProviders - get a reference to any security Provider object.
     * </ul>
     *
     * @param name  the name of the target to be returned
     * @return the Target object
     * @exception ForbiddenTargetException if <i>name</i> is not recognized.
     */
    public static Target findTarget(String name)
    throws ForbiddenTargetException {
        return IJCE_SecuritySupport.findTarget(name);
    }

    /**
     * Reserved for future use, in case parameterized targets are needed. Currently
     * this always throws a ForbiddenTargetException.
     *
     * @param name  the name of the target to be returned
     * @param arg   a parameter object
     * @return the Target object
     * @exception ForbiddenTargetException if <i>name</i> is not recognized.
     */
    public static Target findTarget(String name, Object arg)
    throws ForbiddenTargetException {
        return IJCE_SecuritySupport.findTarget(name, arg);
    }

    /**
     * Returns the major version of this release of IJCE.
     */
    public static int getMajorVersion() { return MAJOR_VERSION; }

    /**
     * Returns the minor version of this release of IJCE.
     */
    public static int getMinorVersion() { return MINOR_VERSION; }

    /**
     * Returns the intermediate version of this release of IJCE.
     */
    public static int getIntermediateVersion() { return INTER_VERSION; }

    /**
     * Returns true iff this version of IJCE is at least the given
     * version.
     */
    public static boolean
    isVersionAtLeast(int major, int minor, int intermediate) {
        if (MAJOR_VERSION > major) return true;
        if (MAJOR_VERSION < major) return false;
        if (MINOR_VERSION > minor) return true;
        if (MINOR_VERSION < minor) return false;
        return INTER_VERSION >= intermediate;
    }

    /**
     * Returns the release date of this version of IJCE, as a string in
     * the form "yyyy/mm/dd".
     */
    public static String getReleaseDate() {
        try {
            return CVS_DATE.substring(7, 17);
        } catch (StringIndexOutOfBoundsException e) {
            return "unknown";
        }
    }

    /**
     * Returns a string describing this version of IJCE.
     */
    public static String getVersionString() {
        StringBuffer version = new StringBuffer("IJCE ")
            .append(MAJOR_VERSION).append(".").append(MINOR_VERSION);
        if (INTER_VERSION != 0)
            version.append(".").append(INTER_VERSION);
        if (IS_SNAPSHOT)
            version.append(" (").append(getReleaseDate()).append(" snapshot)");

        return version.toString();
    }

    /**
     * Returns true if IJCE is providing the implementations of the JCA classes
     * (MessageDigest, Signature, etc). This will be false if another version
     * of JCA (for example JavaSoft's) is installed earlier in the CLASSPATH.
     */
    public static boolean isProvidingJCA() {
        try {
            return IJCE_Java10Support.isAssignableFrom(
                Class.forName("java.security.IJCE_Traceable"),
                Class.forName("java.security.MessageDigest"));
        } catch (Exception e) { return false; }
    }

    /**
     * Returns true if IJCE is providing the implementations of the JCE classes
     * (Cipher, KeyGenerator, etc). This will be false if another version
     * of JCE (for example JavaSoft's) is installed earlier in the CLASSPATH.
     */
    public static boolean isProvidingJCE() {
        try {
            return IJCE_Java10Support.isAssignableFrom(
                Class.forName("java.security.IJCE_Traceable"),
                Class.forName("java.security.Cipher"));
        } catch (Exception e) { return false; }
    }


// Internal methods
//...........................................................................

    /**
     * Returns the algorithm superclass corresponding to <i>type</i>, or null
     * if there is no such class.
     * <p>
     * For example, normally <code>getClassForType("Cipher")</code> would
     * return <code>java.security.Cipher.class</code>.
     * Type names are configured in the IJCE.properties file.
     */
    private static Class getClassForType(String type) {
        Class cl = (Class) (typeToClass.get(type));
        if (cl != null)
            return cl;

        String classname = IJCE_Properties.getProperty("Type." + type);
        if (classname == null)
            return null;

        try {
            cl = Class.forName(classname);
        } catch (LinkageError e) {
            IJCE.debug("Error loading class for algorithm type " + type + ": " + e);
            return null;
        } catch (ClassNotFoundException e) {
            IJCE.debug("Error loading class for algorithm type " + type + ": " + e);
            return null;
        }
        typeToClass.put(type, cl);
        return cl;
    }

    /**
     * Helper method to get an array of all configured security Provider objects.
     * This method does not do a security check.
     *
     * @return an array of configured Provider objects.
     */
    private static Provider[] getProvidersInternal() {
        try {
            if (getProvidersTarget == null)
                getProvidersTarget = findTarget("GetSecurityProviders");
            if (privMgr == null)
                privMgr = PrivilegeManager.getPrivilegeManager();
          
            privMgr.enablePrivilege(getProvidersTarget);
        } catch (NoClassDefFoundError e) {}
    
        Provider[] providers = Security.getProviders();
        if (DEBUG && debuglevel >= 4) {
            for (int i = 0; i < providers.length; i++)
                debug("providers[" + i + "] = " + providers[i]);
        }

        try { privMgr.revertPrivilege(getProvidersTarget); }
        catch (NoClassDefFoundError e) {}

        return providers;
    }

    /**
     * Helper method to get a configured security Provider object by name.
     * This method does not do a security check.
     *
     * @return the Provider object for providerName.
     */
    private static Provider getProviderInternal(String providerName) {
        try {
            if (getProvidersTarget == null)
                getProvidersTarget = findTarget("GetSecurityProviders");
            if (privMgr == null)
                privMgr = PrivilegeManager.getPrivilegeManager();
          
            privMgr.enablePrivilege(getProvidersTarget);
        } catch (NoClassDefFoundError e) {}
    
        Provider provider = Security.getProvider(providerName);

        try { privMgr.revertPrivilege(getProvidersTarget); }
        catch (NoClassDefFoundError e) {}

        return provider;
    }

    /**
     * Prints a debugging message that may be significant to a developer.
     */
    static void debug(String s) { err.println(s); }

    /**
     * Prints an error message that may be significant to a user.
     */
    static void error(String s) { err.println(s); }

    /**
     * Prints a "can't happen" error, with a request to report this as a bug.
     * Always throws an InternalError.
     */
    static void reportBug(String s) {
        err.println(
            "\n" + s + "\n\n" +
            "Please report this as a bug to <david.hopwood@lmh.ox.ac.uk>, including\n" +
            "any other messages displayed on the console, and a description of what\n" +
            "appeared to cause the error.\n");
        throw new InternalError(s);
    }

    /**
     * Debugging method to list all providers.
     */
    static void listProviders() {
        Provider[] providers = getProvidersInternal();
        for (int i = 0; i < providers.length; i++)
            err.println("providers[" + i + "] = " + providers[i]);
    }

    /**
     * Returns the debug level for <i>label</i>. Its value is normally given
     * by the numeric provider property "<code>Debug.Level.<i>label</i></code>".
     * <p>
     * If this property is not set, "<code>Debug.Level.*</code>" is
     * searched next. If neither property is set, or if the first property
     * found is not a valid decimal integer, then this method returns 0.
     */
    static int getDebugLevel(String label) {
        String s = IJCE_Properties.getProperty("Debug.Level." + label);
        if (s == null) {
            s = IJCE_Properties.getProperty("Debug.Level.*");
            if (s == null)
                return 0;
        }
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }

    /**
     * Returns the PrintWriter that debugging Output is to be sent to.
     */
    static PrintWriter getDebugOutput() {
        return err;
    }


// Main
//...........................................................................

    /**
     * Prints the IJCE version string, a list of statically configured providers,
     * and the location of the library directory.
     */
    public static void main(String[] args) {
        System.out.println(getVersionString());
        System.out.println();
        listProviders();
        System.out.println();
        try {
            String libPath = IJCE_Properties.getLibraryPath();
            System.out.println("The library directory is");
            System.out.println("  " + libPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
