/*
 * CVS identifier:
 *
 * $Id: CmdLnDecoder.java,v 1.42 2001/07/17 12:27:32 grosbois Exp $
 *
 * Class:                   CmdLnDecoder
 *
 * Description:             The command line decoder
 *
 *
 *
 * COPYRIGHT:
 * 
 * This software module was originally developed by Rapha�l Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askel�f (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, F�lix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * 
 * Copyright (c) 1999/2000 JJ2000 Partners.
 * */
package jj2000.j2k.decoder;

import jj2000.j2k.util.*;

import java.io.*;

/**
 * This class runs the JJ2000 decoder from the command line interface. It
 * parses command-line arguments in order to fill a ParameterList object. Then
 * this one is provided to a Decoder object.
 * */
public class CmdLnDecoder{

    /** The parameter list (with modules arguments) */
    private ParameterList pl;

    /** The default parameter list (with modules arguments) */
    private ParameterList defpl;

    /** The current Decoder object */
    private Decoder dec;

    /**
     * The starting point of the program. It calls the constructor with the
     * command line options in a String array.
     *
     * @param argv The command line parameters
     * */
    public static void main(String argv[]) {
        if (argv.length == 0) {
            FacilityManager.getMsgLogger()
                .println("CmdLnDecoder: JJ2000's JPEG 2000 Decoder\n"+
                         "    use jj2000.j2k.decoder.CmdLnDecoder -u "+
                         "to get help\n",0,0);
            System.exit(1);
        }

        new CmdLnDecoder(argv);
    }

    /**
     * Instantiates a command line decoder object, width the 'argv' command
     * line arguments. It also initializes the default parameters. If the
     * argument list is empty an IllegalArgumentException is thrown. If an
     * error occurs while parsing the arguments error messages are written to
     * stderr and the run exit code is set to non-zero, see getExitCode()
     *
     * @exception IllegalArgumentException If 'argv' is empty
     *
     * @see Decoder#getExitCode
     * */
    public CmdLnDecoder(String argv[]) {
        // Initialize default parameters
        defpl = new ParameterList();
	String[][] param = Decoder.getAllParameters();

	for (int i=param.length-1; i>=0; i--) {
	    if(param[i][3]!=null)
		defpl.put(param[i][0],param[i][3]);
        }

        // Create parameter list using defaults
        pl = new ParameterList(defpl);

        if (argv.length == 0 ) {
            throw new IllegalArgumentException("No arguments!");
        }

        // Parse arguments from argv
        try {
            pl.parseArgs(argv);
        }
        catch (StringFormatException e) {
            System.err.println("An error occured while parsing the "+
			       "arguments:\n"+e.getMessage());
            return;
        }
        // Parse the arguments from some file?
        if (pl.getParameter("pfile") != null) {
            // Load parameters from file
            InputStream is;
            try {
                is = new FileInputStream(pl.getParameter("pfile"));
                is = new BufferedInputStream(is);
                pl.load(is);
            }
            catch (FileNotFoundException e) {
                System.err.println("Could not load the argument file " +
				   pl.getParameter("pfile"));
                return;
            }
            catch (IOException e) {
                System.err.println("An error ocurred while reading from "+
				   "the argument "+"file "+ 
				   pl.getParameter("pfile"));
                return;
            }
            try {
                is.close();
            }
            catch (IOException e) {
                System.out.println("[WARNING]: Could not close the argument"+
				   " file after reading");
            }
            // Now reparse command line arguments so that they override file
            // arguments
            try {
                pl.parseArgs(argv);
            }
            catch (StringFormatException e) {
                System.err.println("An error occured while re-parsing the "+
				   "arguments:\n"+e.getMessage());
                return;
            }
        }

	// Instantiate the Decoder object
        dec = new Decoder(pl);
        if (dec.getExitCode() != 0) { // An error ocurred
            System.exit(dec.getExitCode());
        }

        // Run the decoder
        try {
            dec.run();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            if (dec.getExitCode() != 0) {
                System.exit(dec.getExitCode());
            }
        }
    }
}
