// This file is currently unlocked (change this line if you lock the file)
//
// $Log: IJCE_ClassLoaderDepth.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:29  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.2  2000/08/17 11:35:23  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.1.1.1  1997/11/03 22:36:58  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.0  1997/08/06  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.lang;

/**
 * This class is a gross hack. It allows IJCE to obtain the current class
 * loader depth (the number of stack frames after the last method in this
 * thread that was part of a class loaded by a ClassLoader).
 * <p>
 * Since <code>ClassLoader.classLoaderDepth()</code> is protected, and
 * since the JDK does not allow creating a security manager after one has
 * already been installed, this requires having code in the java.lang package.
 * <p>
 *
 * <b>Copyright</b> &copy; 1995-1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 *
 * <p><b>$Revision: 1.1.1.1 $</b>
 * @author David Hopwood
 * @since  IJCE 1.0.1
 */
public class IJCE_ClassLoaderDepth {
    private IJCE_ClassLoaderDepth() {} // static methods only

    /**
     * Gets the current classLoaderDepth (assuming there is a security manager
     * installed). This will be 1 if this method was called <em>directly</em>
     * by a class with a ClassLoader, 2 if there is one intervening stack
     * frame, etc.
     * <p>
     * If there is no ClassLoader in the call stack of the current thread,
     * -1 is returned. If there is no security manager installed, -2 is returned.
     * <p>
     * This should only be used to implement higher-level security primitives;
     * never directly to make security decisions.
     */
    public static int classLoaderDepth() {
        return -2;
    }
}
