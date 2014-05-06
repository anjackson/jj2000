// $Id: IJCE_SecuritySupport.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: IJCE_SecuritySupport.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.6  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.5  2000/04/25 16:17:35  edwin
// The fix to IJCE_Properties.java broke this. However as this did not
// work in the first place, I'm commenting out just enough to not let
// it crash the rest.
//
// Revision 1.4  1997/12/14 17:58:50  hopwood
// + Committed changes below.
//
// Revision 1.3.1  1997/12/12  hopwood
// + Remove use of .class because guavac doesn't like it (apparently it isn't
//   LALR(1)).
//
// Revision 1.3  1997/11/29 04:45:13  hopwood
// + Committed changes below.
//
// Revision 1.2  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.7  1997/08/12  David Hopwood
// + HTML help file for security targets is now stored in the IJCE
//   library directory.
//
// Revision 0.1.0.6  1997/08/11  David Hopwood
// + Changed debugging to use properties file.
//
// Revision 0.1.0.5  1997/08/06  David Hopwood
// + Fixed some bugs where a NoClassDefFoundError was being thrown and
//   ignored incorrectly.
//
// Revision 0.1.0.4  1997/07/31  David Hopwood
// + Fixed two errors where debug(...) had not been changed to
//   IJCE.debug(...).
//
// Revision 0.1.0.3  1997/07/26  David Hopwood
// + Renamed IJCE_Properties to IJCE_Properties.
//
// Revision 0.1.0.2  1997/07/17  David Hopwood
// + Changed init() back to a static initializer, and removed the birthday
//   field. The IJCE class no longer has a static initializer, so there are
//   no inter-dependency problems.
// + Changed back to using IJCE.debug. If debugging is centralized then it
//   is easier to change how it is done in future.
//
// Revision 0.1.0.1  1997/07/11  R. Naffah
// + Moved privMgr (static var) initialisation into init() method.
// + Use birthday (boolean static) to tell if loaded for first time to
//   eliminate inter-dependancy on IJCE class <clinit> code.
// + Moved IJCE class static{} code into init() method. Now IJCE ensures
//   that this class is loaded by calling the init() method, instead of
//   this ensuring that IJCE is loaded.
// + Enclosed netscape.security calls within try/catch.
// + Defined a local debug() method to totally dissociate this class
//   from the IJCE one.
//
// Revision 0.1.0.0  1997/07/0?  D. Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import xjava.lang.IJCE_ClassLoaderDepth; // ugly hack
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Hashtable;

import netscape.security.ForbiddenTargetException;
import netscape.security.PrivilegeManager;
import netscape.security.Target;
import netscape.security.UserDialogHelper;
import netscape.security.UserTarget;

/**
 * This class is used to make any security checks needed by IJCE (for example
 * when adding and removing security providers). It is needed to work around
 * some nasty compatibility problems that would otherwise arise from supporting
 * Java 1.0.2, Java 1.1 and Netscape simultaneously.
 * <p>
 *
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @author  Raif S. Naffah
 * @since   Cryptix 2.2.2
 */
class IJCE_SecuritySupport
{
    private IJCE_SecuritySupport() {} // static methods only

// Debugging methods and vars.
//...........................................................................

    private static final boolean DEBUG = true;
    private static int debuglevel =
        DEBUG ? IJCE.getDebugLevel("IJCE_SecuritySupport") : 0;
    private static PrintWriter err = DEBUG ? IJCE.getDebugOutput() : null;
    private static void debug(String s) { err.println("IJCE_SecuritySupport: " + s); }


//...........................................................................

    private static final String TARGET_HELP_FILENAME = "TargetHelp.html";

    private static String targetHelpURL;
    private static PrivilegeManager privMgr;
    private static Hashtable targets = new Hashtable();

    static {
        //try {
            targetHelpURL = // IJCE_Properties.getLibraryPath() +
                TARGET_HELP_FILENAME;
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        try {
            registerTargets();
        } catch (Throwable t) {
            t.printStackTrace();
            IJCE.reportBug("Unexpected exception in IJCE_SecuritySupport.registerTargets()");
        }
    }

    /**
     * Ensures that the security targets have been registered.
     */
    private static void registerTargets() {
        if (DEBUG && debuglevel >= 4) debug("Initializing...");

        // If the target registration does not complete, there is no security problem with
        // just returning, since that will leave the 'targets' Hashtable empty. Any future
        // security checks will fail with a ForbiddenTargetException when they try to call
        // findTarget.

        try {
            netscape.security.Principal myPrincipal = null;
            try {
                netscape.security.Principal[] principals = PrivilegeManager.getMyPrincipals();
                if (principals == null || principals.length == 0) {
                    err.println("Warning: invalid return value from PrivilegeManager.getMyPrincipals()\n" +
                                "Future security-related operations will probably fail.");
                    return;
                }
                myPrincipal = principals[0];
            } catch (NoClassDefFoundError e) {}

            if (DEBUG && debuglevel >= 5) debug("myPrincipal = " + myPrincipal);

            int lowRisk =                  UserDialogHelper.targetRiskLow();
            String lowRiskColour =         UserDialogHelper.targetRiskColorLow();
            //int mediumRisk =             UserDialogHelper.targetRiskMedium();
            //String mediumRiskColour =    UserDialogHelper.targetRiskColorMedium();
            int highRisk =                 UserDialogHelper.targetRiskHigh();
            String highRiskColour =        UserDialogHelper.targetRiskColorHigh();

            if (DEBUG && debuglevel >= 5) debug("registering security targets...");

            registerTarget(myPrincipal, "AddSecurityProvider", highRisk, highRiskColour);
            registerTarget(myPrincipal, "RemoveSecurityProvider", highRisk, highRiskColour);
            registerTarget(myPrincipal, "SecurityPropertyRead", lowRisk, lowRiskColour);
            registerTarget(myPrincipal, "SecurityPropertyWrite", highRisk, highRiskColour);

            boolean notFixed = true;
            try {
                notFixed = IJCE_Java10Support.isAssignableFrom(
                    Class.forName("java.util.Hashtable"),
                    Class.forName("java.security.Provider"));
            } catch (Exception e) {}

            if (notFixed) // Provider extends Hashtable bug not fixed.
                registerTarget(myPrincipal, "GetSecurityProviders", highRisk, highRiskColour);
            else
                registerTarget(myPrincipal, "GetSecurityProviders", lowRisk, lowRiskColour);

            if (DEBUG && debuglevel >= 3) debug("All security targets successfully registered.");
        } catch (Exception e) {
            IJCE.debug("Warning: Unable to register security target.");
            e.printStackTrace();
        } catch (NoClassDefFoundError e2) {
            if (DEBUG && debuglevel >= 1) e2.printStackTrace();
        }
    }

    /**
     * Registers a new security target.
     */
    private static Target registerTarget(netscape.security.Principal myPrincipal,
                                         String name, int risk, String riskColour) {
        String description = IJCE_Properties.getProperty("UI.target." + name);
        String helpURL =
            targetHelpURL == null ? null : targetHelpURL + "#" + name;

        try {
            Target target = new UserTarget(name, myPrincipal, risk, riskColour,
                                           description, helpURL).registerTarget();
            if (DEBUG && debuglevel >= 6) debug("registering " + target);
            targets.put(name, target);
            return target;
        } catch (NoClassDefFoundError e) {
            if (DEBUG) e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a Target that can be passed to
     * <code>PrivilegeManager.enablePrivilege(...)</code>, in order to request
     * permission for an action that requires the user's trust.
     *
     * @param name  the name of the target to be returned
     * @return the Target object
     * @exception ForbiddenTargetException if <i>name</i> is not recognized.
     * @see java.security.IJCE#findTarget(java.lang.String)
     */
    static Target findTarget(String name) throws ForbiddenTargetException {
        Target t = (Target) (targets.get(name));
        if (t != null)
            return t;
        throw new ForbiddenTargetException(
            "There is no security target with name \"" + name + "\"");
    }

    /**
     * Reserved for future use, in case parameterized targets are needed. Currently
     * this always throws a ForbiddenTargetException.
     *
     * @param name  the name of the target to be returned
     * @param arg   a parameter object
     * @return the Target object
     * @exception ForbiddenTargetException if <i>name</i> is not recognized.
     * @see java.security.IJCE#findTarget(java.lang.String, java.lang.Object)
     */
    static Target findTarget(String name, Object arg) throws ForbiddenTargetException {
        throw new ForbiddenTargetException(
            "This version of IJCE has no parameterized security targets");
    }

    /**
     * On VMs that support Netscape's PrivilegeManager extensions, this checks
     * that the security privilege with name <i>targetname</i> has been enabled. On other
     * VMs, it is equivalent to <code>IJCE_SecuritySupport.checkSystemCaller(depth)</code>.
     * <p>
     * For example,
     * <blockquote>
     *    <code>IJCE_SecuritySupport.checkPrivilegeEnabled("AddSecurityProvider", 1)</code>
     * <blockquote>
     * <p>
     * On Netscape this will check that the "AddSecurityProvider" privilege has
     * been enabled. On other VMs it will check that either no security manager is
     * installed, or the <em>immediate</em> caller is a system class.
     */
    static void checkPrivilegeEnabled(String targetname, int depth) {
        checkPrivilegeEnabled(findTarget(targetname), depth+1);
    }

    /**
     * On VMs that support Netscape's PrivilegeManager extensions, this checks
     * that the privilege given by <i>target</i> has been enabled. On other
     * VMs, it is equivalent to <code>IJCE_SecuritySupport.checkSystemCaller(depth)</code>.
     * <p>
     * For example,
     * <blockquote>
     *     <code>IJCE_SecuritySupport.checkPrivilegeEnabled(target, 1)</code>
     * <blockquote>
     * <p>
     * On Netscape this will check that the <i>target</i> privilege has
     * been enabled. On other VMs it will check that either no security manager is
     * installed, or the <em>immediate</em> caller is a system class.
     */
    static void checkPrivilegeEnabled(Target target, int depth) {
        if (target == null) throw new NullPointerException("target == null");
        try {
            if (privMgr == null) privMgr = PrivilegeManager.getPrivilegeManager();
            privMgr.checkPrivilegeEnabled(target);
            return;
        } catch (NoClassDefFoundError e) {
        } catch (NoSuchMethodError e) {
        }
        checkSystemCaller(depth+1);
    }

    /**
     * On VMs that support Netscape's PrivilegeManager extensions, this checks
     * that the privilege given by <i>target</i>, with parameter <i>arg</i> has
     * been enabled. On other VMs, it is equivalent to
     * <code>IJCE_SecuritySupport.checkSystemCaller(depth)</code>.
     *
     * @see #checkPrivilegeEnabled(netscape.security.Target, int)
     */
    static void checkPrivilegeEnabled(Target target, Object arg, int depth) {
        if (target == null) throw new NullPointerException("target == null");
        try {
            if (privMgr == null) privMgr = PrivilegeManager.getPrivilegeManager();
            privMgr.checkPrivilegeEnabled(target, arg);
            return;
        } catch (NoClassDefFoundError e) {
        } catch (NoSuchMethodError e) {
        }
        checkSystemCaller(depth+1);
    }

    /**
     * If a SecurityManager is installed, this method checks that there is no class
     * loaded by a ClassLoader in the previous <i>depth</i> stack frames. If there
     * is no SecurityManager, it returns silently.
     * <p>
     * For example,
     * <blockquote>
     *    <code>IJCE_SecuritySupport.checkSystemCaller(1)</code>
     * <blockquote>
     * will check that either no security manager is installed, or the
     * <em>immediate</em> caller is a system class.
     */
    static void checkSystemCaller(int depth) {
        int cldepth = IJCE_ClassLoaderDepth.classLoaderDepth();
        if (cldepth < 0) return;
        if (cldepth <= depth) {
            IJCE.reportBug(
                "incorrect depth passed to IJCE_SecuritySupport.checkSystemCaller:\n" +
                "depth = " + depth + ", classLoaderDepth() = " + cldepth);
        }
        if (cldepth == depth+1) {
            throw new SecurityException(
                "this operation cannot be performed from a non-system class");
        }
    }
}
