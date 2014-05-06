// $Id: BaseTest.java,v 1.1.1.1 2002/08/27 12:32:16 grosbois Exp $
//
// $Log: BaseTest.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:16  grosbois
// Add cryptix 3.2
//
// Revision 1.5  1999/07/26 16:12:48  iang
// Now catches Throwable, as unknown class errors were sneaking through
//
// Revision 1.4  1999/07/25 22:56:26  iang
// added Throwable, something's slipping through....
//
// Revision 1.3  1998/02/22 04:18:30  hopwood
// + Committed changes below.
//
// Revision 1.2.1  1998/02/14  hopwood
// + Added support for running as an applet, and as a standalone GUI program
//   (needs some work still).
// + Output defaults to System.out.
// + Exit with code TestException.COMPLETE_SUCCESS if no exception is thrown.
// + Case is now not significant for the -verbose option.
//
// Revision 1.2  1998/02/01 05:11:49  hopwood
// + Committed changes below.
//
// Revision 1.1.1  1998/02/01  hopwood
// + Removed unnecessary line feeds in output.
// + Added isVerbose method.
//
// Revision 1.1  1998/01/28 05:44:02  hopwood
// + Major update of test classes.
//
// Revision 0.1.8.2  1998/01/28  hopwood
// + Added ability to skip tests.
//
// Revision 0.1.8.1  1998/01/13  hopwood
// + Output now goes to a StringWriter initially, and is only printed when
//   one or more tests fail. A -verbose option can be used to obtain the
//   previous behaviour.
// + Distinguish between failures (incorrect output) and errors (problems with
//   the testing, or unexpected exceptions).
// + Changed expectedPasses back to 0, since the expectedPasses == passes == 0
//   case now causes an error, which has the same effect.
// + Exceptions that are uncaught by the test class should cause an error.
// + Added more convenience methods.
// + Encapsulated variables properly.
// + Removed debugging cycle for the time being.
// + Documented methods and variables.
// + Moved to cryptix.util.test package.
//
// Revision 0.1.8  1998/01/13 13:51:05  iang
// + Made expectedPasses==1 so that pre-initialisation exceptions are caught
//   and given exit(1).  Problem arose with TestRSA doing preparation.
// + Also commented out the noisy bits of the not-yet-written
//   debug-ON cycle.  Should really be protected with a property (?) but the
//   'finally' part made that non-trivial as that did the exceptions.
//
// Revision 0.1.7  1998/01/12 04:10:39  hopwood
// + Made engineTest() protected.
// + Cosmetics.
//
// Revision 0.1.6.1  1998/01/12  hopwood
// + Made all methods protected. The reflection hack added in 1.5.1 is no
//   longer needed.
//
// Revision 0.1.6  1997/12/22 15:07:47  raif
// *** empty log message ***
//
// Revision 0.1.5.1  1997/12/23  raif
// + Use reflection API to invoke engineTest. Doing so
//   eliminates the need to modify existing code.
// + Other changes to allow use by Maker.
//
// Revision 0.1.5  1997/12/21 19:09:05  iang
// + Commented out the changes to passes variables, exit codes always fail=1.
//
// Revision 0.1.4  1997/12/15 02:40:54  hopwood
// + Committed changes below.
//
// Revision 0.1.3.1  1997/12/15  hopwood
// + Use IanG's conventions for return codes.
//
// Revision 0.1.3  1997/11/29 17:47:08  hopwood
// + Minor changes to test API.
// + Added better support for BaseTest subclasses in TestAll (doesn't work
//   yet).
//
// Revision 0.1.2  1997/11/29 05:12:22  hopwood
// + Changes to use new test API (BaseTest).
//
// Revision 0.1.1.1.1.2  1997/11/23  hopwood
// + Fleshed out the API a little more, added 'expectedPasses'.
//
// Revision 0.1.1.1.1.1  1997/11/21  hopwood
// + Added the beginnings of a test API (fail, pass, done, engineTest etc.)
//   At the moment these methods are all package-private, in case some tests
//   are insecure.
//
// Revision 0.1.1.1.1  1997/11/03 22:36:55  hopwood
// + Imported to CVS (tagged as 'start').
//
// Revision 0.1.0.0  1997/09/14  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997, 1998 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.applet.Applet;

/**
 * This abstract class acts as a base for <em>all</em> Cryptix test classes.
 * Its purpose is to provide a framework that will allow us to easily add new
 * testing features. Features that have already been added include:
 * <ul>
 *   <li> only printing complete output when one or more tests fail.
 * </ul>
 * <p>
 * Planned features include:
 * <ul>
 *   <li> testing as an applet,
 *   <li> testing as a standalone GUI program (with the -gui option),
 *   <li> repeating tests that have failed at a higher debugging level.
 * </ul>
 * <p>
 * <b>Copyright</b> &copy; 1997, 1998
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 2.2.2
 */
public abstract class BaseTest
extends Applet
{

// Constants and fields
//...........................................................................

    private static final String SEPARATOR =
        "\n===========================================================================";

    /** The PrintWriter to which output is sent by default. */
    private static PrintWriter defaultOutput = new PrintWriter(System.out, true);

    /** The PrintWriter to which immediate output is sent. */
    protected PrintWriter status;

    /** The PrintWriter to which delayed output is sent. */
    protected PrintWriter out;

    /** The name of this test. */
    private String name;

    /** A StringWriter that collects delayed output. */
    private StringWriter sw;

    /** The number of failures so far. */
    private int failures;

    /** The number of errors so far. */
    private int errors;

    /** The number of passes so far. */
    private int passes;

    /** The number of skipped tests so far. */
    private int skipped;

    /** The number of expected passes. */
    private int expectedPasses;

    /** true if there is an overall pass. */
    private boolean overallPass;

    /** true if output is to be printed even if all tests pass. */
    private boolean verbose;

    /** true if the program was run from the command line. */
    private boolean commandLine;


// Constructor
//...........................................................................

    /** Constructor for use by subclasses. */
    protected BaseTest() {
        name = this.getClass().getName();
        status = defaultOutput;
    }


// GUI and Applet support
//...........................................................................

    /**
     * The GUI implementation (in a separate class to avoid creating unwanted
     * dependencies in this one).
     */
    private TestGUI gui;

    /** Used when the test is running as an Applet. */
    public synchronized void init() {
        initGui();
        try { test(); }
        catch (TestException e) { e.printStackTrace(status); }
    }

    /** Initialises the GUI interface. */
    private void initGui() {
        gui = new TestGUI(this);
        add(gui);
        setOutput(gui.getOutput());
    }

    /** Sets whether a GUI interface is used. */
    public synchronized void setGuiEnabled(boolean flag) {
        if (flag && gui == null) {
            initGui();
            if (commandLine) gui.useAppFrame(true);
        } else if (!flag && gui != null) {
            if (commandLine) gui.useAppFrame(false);
            setOutput(defaultOutput);
            gui = null;
        }
    }

    /** Returns true iff a GUI interface is being used. */
    public boolean isGuiEnabled() {
        return gui != null;
    }

    /** Blocks until it is time to exit. */
    public synchronized void waitForExit() {
        if (gui != null) gui.waitForExit();
    }


// Command-line parsing
//...........................................................................

    /**
     * Used to run a test from the command line. Each test class' main method
     * should create an instance and call the <code>commandline</code>
     * method on it, passing the array of command line arguments. For example,
     * in class TestFoo:
     * <pre>
     *    public static void main(String[] args) {
     *        new TestFoo().commandline(args);
     *    }
     * </pre>
     */
    protected void commandline(String[] args) {
        commandline(args, 0);
    }

    /**
     * Used to run a test from the command line, skipping the first <i>offset</i>
     * arguments (which are assumed to have been processed by the subclass).
     */
    protected void commandline(String[] args, int offset) {
        commandLine = true;
        try {
            parseOptions(args, offset);
            test();
            waitForExit();
            System.exit(TestException.COMPLETE_SUCCESS);
        } catch (TestException e) {
            status.println(e.getMessage());
            waitForExit();
            System.exit(e.getErrorCode());
        }
    }

    /**
     * Parses a command-line option. If the option is not recognised,
     * <code>super.parseOption(option)</code> should be called.
     * <p>
     * Case should generally not be significant in option names.
     */
    protected void parseOption(String option) throws TestException {
        if (option.equalsIgnoreCase("-verbose"))
            setVerbose(true);
        else if (option.equalsIgnoreCase("-gui"))
            setGuiEnabled(true);
        else {
            System.err.println(describeUsage());
            throw new TestException("Unrecognised option: '" + option + "'",
                TestException.ILLEGAL_ARGUMENTS);
        }
    }

    /**
     * Processes command-line arguments from <i>args</i>, starting at index
     * <i>offset</i>.
     */
    public void parseOptions(String[] args, int offset) throws TestException {
        for (int i = offset; i < args.length; i++)
            parseOption(args[i]);
    }

    /** Describes the command-line usage of this class. */
    public String describeUsage() {
        return
            "Usage:\n" +
            "    java " + getClass().getName() + " [options...]\n" +
            describeOptions();
    }

    /** Describes the command-line options of this class. */
    public String describeOptions() {
        return
            "Options:\n" +
            "    -verbose: print output even if all tests pass.\n";
    }

    /**
     * Sets the name of this test (as shown to the user). The default is the name of
     * the class.
     */
    public void setName(String n) { name = n; }

    /** Gets the name of this test (as shown to the user). */
    public String getName() { return name; }

    /**
     * Sets the PrintWriter to which output is to be sent. In most cases this
     * does not need to be set by the test class; the <code>commandline</code>
     * method will do that automatically.
     */
    public void setOutput(PrintWriter pw) { status = pw; }

    /** Sets whether output is to be printed even if all tests pass. */
    public void setVerbose(boolean flag) { verbose = flag; }

    /** Returns true iff output is to be printed even if all tests pass. */
    public boolean isVerbose() { return verbose; }

    /**
     * Runs an instance of Maker. First use the methods setIncludeSource,
     * setResourcesOnly, setProvider, etc. to initialize any properties for which
     * the defaults are not as required.
     *
     * @param  options          an array of string options.
     * @param  name             a name for this test.
     * @exception cryptix.util.test.TestException if the test fails.
     * @since Cryptix 3.0.4
     */
    /**
     * Begins the test proper. This method is called automatically by
     * <code>commandline</code>, but it can also be called directly.
     */
    public void test() throws TestException {
        if (verbose) {
            sw = null;
            out = status;
        } else {
            sw = new StringWriter();
            out = new PrintWriter(sw);
        }
        try {
            failures = errors = passes = skipped = expectedPasses = 0;
            overallPass = false;

            status.print("Running tests for " + getName());
            if (verbose)
                status.println();
            else
                status.flush();
            engineTest();
        } catch (Throwable e) {
            error(e);
        } finally {
            String andSkipped = (skipped > 0) ? " and skipped tests" : "";

            if (passes + skipped < expectedPasses)
                error("Number of passes" + andSkipped + " is less than expected");
            else if (passes < 1)
                error("At least one pass is required");
            else if (0 < expectedPasses && expectedPasses < passes + skipped)
                error("Number of passes" + andSkipped + " is more than expected\n" +
                      "(therefore the expected number is wrong)");

            report();

            if (failures > 0 || errors > 0) {
                if (passes == 0)
                    throw new TestException(getName() + " failed completely",
                        TestException.COMPLETE_FAILURE);

                throw new TestException(getName() + " failed partially",
                    TestException.PARTIAL_FAILURE);
            }
            overallPass = true;
        }
    }

    /**
     * Reports a failure, with the given message.
     */
    protected void fail(String msg) {
        failures++;
        out.println("\nFailed: " + msg);
        if (sw != null) switchStream();
    }

    /**
     * Reports an error, with the given message.
     */
    protected void error(String msg) {
        errors++;
        out.println("\nError: " + msg);
        if (sw != null) switchStream();
    }

    /**
     * Reports a skipped test, with the given message.
     */
    protected void skip(String msg) {
        skipped++;
        out.println("\nTest skipped: " + msg);
        if (sw != null) switchStream();
    }

    private void switchStream() {
        out.flush();
        out = status;
        out.println();
        out.print(sw.getBuffer());
        out.flush();
        sw = null;
    }

    /**
     * Reports an error due to an unexpected exception.
     */
    protected void error(Exception e) {
        error("Exception Unexpected " + e.getClass().getName());
        e.printStackTrace(out);
    }

    protected void error(Throwable e) {
        error("Throwable Unexpected " + e.getClass().getName());
        e.printStackTrace(out);
    }

    /**
     * Reports a pass, with the given message.
     */
    protected void pass(String msg) {
        passes++;
        out.println("\nPassed: " + msg);
        if (sw != null) {
            status.print(".");
            status.flush();
        }
    }

    /**
     * Reports a pass if <i>pass</i> is true, or a failure if it is false.
     * In either case, the given message is used.
     */
    protected void passIf(boolean pass, String msg) {
        if (pass)
            pass(msg);
        else
            fail(msg);
    }

    /**
     * Sets the number of expected passes for this test class. This can be
     * called at any time by the <code>engineTest</code> method, but should
     * normally be called at the start of that method.
     */
    protected void setExpectedPasses(int n) {
        if (n < 0) throw new IllegalArgumentException("n < 0");
        expectedPasses = n;
    }

    /**
     * Forces a report of the number of passes, failures, errors, and expected
     * passes so far.
     */
    protected void report() {
        status.println(SEPARATOR);
        status.println("Number of passes:        " + passes);
        status.println("Number of failures:      " + failures);
        if (errors > 0)
            status.println("Number of errors:        " + errors);
        if (skipped > 0)
            status.println("Number of skipped tests: " + skipped);
        status.println("Expected passes:         " +
            (expectedPasses > 0 ? Integer.toString(expectedPasses) : "unknown"));
    }

    /** Returns the number of failures so far. */
    public int getFailures() { return failures; }

    /** Returns the number of errors so far. */
    public int getErrors() { return errors; }

    /** Returns the number of passes so far. */
    public int getPasses() { return passes; }

    /** Returns the number of skipped tests so far. */
    public int getSkipped() { return skipped; }

    /**
     * Returns the number of expected passes, or 0 if this has not yet been
     * set, or is unknown.
     */
    public int getExpectedPasses() { return expectedPasses; }

    /** Returns true iff all the tests have completed successfully. */
    public boolean isOverallPass() { return overallPass; }

    /**
     * This method should be overridden by test subclasses, to perform the
     * actual testing.
     */
    protected abstract void engineTest() throws Exception;
}
