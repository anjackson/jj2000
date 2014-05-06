// $Id: IJCE_Java10Support.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: IJCE_Java10Support.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.3  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.2  1997/12/14 17:58:50  hopwood
// + Committed changes below.
//
// Revision 1.1.1.1.1  1997/12/12  hopwood
// + Remove use of .class because guavac doesn't like it (apparently it isn't
//   LALR(1)).
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.1  1997/08/25  David Hopwood
// + Renamed to IJCE_Java10Support (from IJCE_Java102Support).
//
// Revision 0.1.0.0  1997/08/12  David Hopwood
// + Start of history.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

/**
 * Reimplements methods supported only in Java 1.1. Remove this class when we
 * do a 1.1-only release.
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   IJCE 1.0.1
 */
class IJCE_Java10Support {
    private IJCE_Java10Support() {} // static methods only

    private static Class Object_class = new Object().getClass();

    /**
     * Reimplements <code>target.isAssignableFrom(cl)</code>, from class
     * <samp>Class</samp>.
     *
     * @return true if <i>target</i> is assignable from <i>cl</i>.
     */
    public static boolean isAssignableFrom(Class target, Class cl) {
        if (target.isInterface()) {
/* don't need this case
            if (target == Cloneable.class && cl.getName().charAt(0) == '[') {
                // If cl is an array type then it has Cloneable as an implicit superinterface.
                return true;
            }
*/
            return interfaceIsAssignableFrom(target, cl);
        } else if (target.getName().charAt(0) == '[') {
            return arrayIsAssignableFrom(target, cl);
        } else {
            if (cl.isInterface()) {
                // Optimization: a class is never assignable from an interface.
                return false;
            }
            return classIsAssignableFrom(target, cl);
        }
    }

    // Note: this is not used for the case where target is Cloneable and cl is an
    // array type.
    private static boolean interfaceIsAssignableFrom(Class target, Class cl) {
        if (target == cl) {
            // Any interface is assignable from itself.
            return true;
        }
        if (cl == Object_class) {
            // No interface is assignable from Object.
            return false;
        }
        // target is assignable from cl if it is assignable from any of cl's
        // direct superinterfaces.
        Class[] interfaces = cl.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaceIsAssignableFrom(target, interfaces[i]))
                return true;
        }
        // Otherwise, target is assignable from cl iff it is assignable from cl's
        // direct superclass.
        Class superclass = cl.getSuperclass();
        if (superclass == null) {
            return false;
        }
        return interfaceIsAssignableFrom(target, superclass);
    }

    private static boolean classIsAssignableFrom(Class target, Class cl) {
        if (target == cl) {
            // Any class is assignable from itself.
            return true;
        }
        if (cl == Object_class) {
            // No class other than Object is assignable from Object.
            return false;
        }
        // Otherwise, target is assignable from cl iff it is assignable from cl's
        // direct superclass, or cl is an interface and target is Object.
        Class superclass = cl.getSuperclass();
        if (superclass == null) {
            return cl.isInterface() && target == Object_class;
        }
        return classIsAssignableFrom(target, superclass);
    }

    private static boolean arrayIsAssignableFrom(Class target, Class cl) {
        return false;
    }
/* don't need this case
        // An array type can only be assignable from another array type with an
        // equal or greater nesting level.
        String targetName = target.getName();
        String clName = cl.getName();
        int depth = 0;
        do {
            if (clName.charAt(depth) != '[')
                return false;
        } while (targetName.charAt(++depth) == '[');

        if (targetName.charAt(depth) != 'L') {
            // If the target element type is primitive, target is assignable from
            // cl iff their encoded names are equal. (This works even when
            // ClassLoaders are involved).
            return targetName.equals(clName);
        }
        // Sanity check: targetName must end in ';'.
        if (targetName.charAt(targetName.length()-1) != ';')
            throw new InternalError("Sanity check failed: targetName = \"" + targetName + "\", which does not end in ';'");

        try {
            Class targetElement = loadClass(target.getClassLoader(),
                targetName.substring(depth+1, targetName.length()-1));
            if (clName.charAt(depth) == '[') {
                // If cl's nesting depth is greater, the target element type must be
                // Object or Cloneable.
                return targetElement == Object.class ||
                       targetElement == Cloneable.class;
            }

            // If the depth of array nesting is equal, target is assignable from cl
            // iff its element type is assignable from cl's element type.
            if (clName.charAt(depth) != 'L') {
                // Since targetElement is known to be non-primitive, the above condition
                // cannot be true if cl's element type is primitive.
                return false;
            }
            // Sanity check: clName must end in ';'.
            if (clName.charAt(clName.length()-1) != ';')
                throw new InternalError("Sanity check failed: clName = \"" + clName + "\", which does not end in ';'");

            // At this both both element types must be non-primitive.
            Class clElement = loadClass(cl.getClassLoader(),
                clName.substring(depth+1, clName.length()-1));
            return isAssignableFrom(targetElement, clElement);
        } catch (ClassNotFoundException e) {
            throw new InternalError(e.toString());
        }
    }

    private static Class loadClass(ClassLoader loader, String name)
    throws ClassNotFoundException {
        if (loader == null) {
            return Class.forName(name);
        } else {
            return loader.loadClass(name);
        }
    }

    //** Self-test. *
    public static void main(String[] args) throws ClassNotFoundException {
        test(Key.class, PrivateKey.class, true);
        test(java.util.Dictionary.class, java.util.Hashtable.class, true);
        test(Object.class, Object[].class, true);
        test(Object[].class, Object[][].class, true);
        test(Cloneable[].class, Exception[][].class, true);
        test(Cloneable.class, Object[].class, true);
        test(Exception[].class, Exception[][].class, false);
        test(Key[].class, java.util.Hashtable[].class, false);
        test(int[].class, short[].class, false);
        test(int[].class, int[].class, true);
//      test(void.class, boolean.class, false);
//      test(Object.class, boolean.class, false);
    }

    private static void test(Class target, Class cl, boolean expected) {
        boolean result = isAssignableFrom(target, cl);
        System.out.println("isAssignableFrom(<" + target + ">, <" + cl + ">) = " + result);
        if (result != expected) System.out.println("  Result should have been " + expected + "!");
    }
*/
}
