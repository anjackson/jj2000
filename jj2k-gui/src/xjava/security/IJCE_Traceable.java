// $Id: IJCE_Traceable.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: IJCE_Traceable.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.6  2000/08/17 11:35:24  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.5  1997/12/01 03:37:28  hopwood
// + Committed changes below.
//
// Revision 1.4  1997/11/29 04:45:13  hopwood
// + Committed changes below.
//
// Revision 1.3.1  1997/11/28  hopwood
// + Improved formatting for nested calls.
// + Distinguish between void methods and methods that will return an argument
//   (by adding traceVoidMethod).
//
// Revision 1.3  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1997/11/19  David Hopwood
// + Debug output now goes to IJCE.getDebugOutput().
//
// Revision 1.2  1997/11/07 05:53:27  raif
// *** empty log message ***
//
// Revision 1.1.1.1  1997/11/03 22:36:57  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.2  1997/08/12  David Hopwood
// + Changed IJCE_Properties to IJCE_Properties.
// + Trace output now goes to System.err, not System.out.
// + Cosmetic changes.
//
// Revision 0.1.0.1  1997/07/12  R. Naffah
// + Added initialisation code to setup traceable objects set in
//   IJCE.properties file.
//
// Revision 0.1.0.0  1997/07/0?  David Hopwood
// + Original version
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */
package xjava.security;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class is extended by Java Security classes that allow tracing of
 * calls to SPI methods. Tracing is useful when debugging an algorithm
 * implementation, for example.
 * <p>
 * These methods are not part of the public API; they are intended to be
 * called only from the algorithm classes in this package.
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
abstract class IJCE_Traceable
{

// Variables
//...........................................................................

    boolean tracing; // defaults to false
    private PrintWriter out;

    private static int indent; // defaults to 0
    private static boolean dangling; // defaults to false
    private static Hashtable traced = new Hashtable();


// Static initialization
//...........................................................................

    static {
        String obj;                         // an entry in the IJCE.properties file
        String want;                        // property value associated w/ obj
        String tracePrefix = "Trace.";      // IJCE properties we're interested in here
        int offset = tracePrefix.length();  // pos. of first char in obj after 'Trace.'

        PrintWriter err = IJCE.getDebugOutput();
        Enumeration names = IJCE_Properties.propertyNames();
        while (names.hasMoreElements()) {
            obj = (String) (names.nextElement());
            if (obj.startsWith(tracePrefix)) {
                want = IJCE_Properties.getProperty(obj);
                if (want != null && want.equalsIgnoreCase("true"))
                    traced.put(obj.substring(offset), err);
            }
        }
    }


// Constructor
//...........................................................................

    IJCE_Traceable(String type) {
        PrintWriter pw = (PrintWriter) (traced.get(this.getClass().getName()));
        if (pw == null) pw = (PrintWriter) (traced.get(type));
        if (pw != null) enableTracing(pw);
    }


// Own methods
//...........................................................................

    /**
     * Enables tracing of calls to SPI methods for this object. Output is
     * sent to the PrintWriter <i>out</i>.
     */
    void enableTracing(PrintWriter out) {
        if (out == null) throw new NullPointerException("out == null");
        this.out = out;
        tracing = true;
    }

    /**
     * Disables tracing of SPI methods for this object. Returns silently
     * if tracing was not enabled.
     */
    void disableTracing() {
        tracing = false;
        out = null;
    }

    /** Traces a void method call. */
    void traceVoidMethod(String s) {
        try {
            newline();
            out.println("<" + this + ">." + s);
            dangling = false;
        } catch (NullPointerException e) {}
    }

    /** Traces a method call that will return a result. */
    void traceMethod(String s) {
        try {
            newline();
            out.print("<" + this + ">." + s + " ");
            out.flush();
            dangling = true;
            indent++;
        } catch (NullPointerException e) {}
    }

    /** Traces the result of a method as a string. */
    void traceResult(String s) {
        try {
            if (!dangling) {
                for (int i = 1; i < indent; i++)
                    out.print("    ");
                out.print("... ");
            }
            out.println("= " + s);
            dangling = false;
            indent--;
        } catch (NullPointerException e) {}
    }

    /** Traces the result of a method as an integer. */
    void traceResult(int i) {
        traceResult(Integer.toString(i));
    }

    private void newline() {
        if (dangling)
            out.println("...");
        for (int i = 0; i < indent; i++)
            out.print("    ");
    }
}
