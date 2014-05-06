// $Id: TextAreaWriter.java,v 1.1.1.1 2002/08/27 12:32:15 grosbois Exp $
//
// $Log: TextAreaWriter.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:15  grosbois
// Add cryptix 3.2
//
// Revision 1.1  1998/02/22 04:12:26  hopwood
// + Added to CVS.
//
// Revision 0.1.1  1998/02/22  hopwood
// + Moved to new cryptix.util.gui package.
//
// Revision 0.1.0  1998/02/15  hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1998 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.gui;

import java.io.Writer;
import java.awt.TextArea;

/**
 * A class that extends Writer to write into a TextArea. Note that the
 * output will not be displayed (i.e. the TextArea repainted) until
 * <code>flush()</code> or <code>close()</code> is called.
 * <p>
 * <b>Copyright</b> &copy; 1998
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   Cryptix 3.0.4
 */
public class TextAreaWriter
extends Writer
{

// Constants and fields
//...........................................................................

    /** Time in milliseconds between repaints. */
    private static final long REPAINT_DELAY = 200;

    /** The TextArea to be written into. */
    private TextArea textArea;


// Constructors
//...........................................................................

    /**
     * Creates a TextAreaWriter for the given TextArea.
     */
    public TextAreaWriter(TextArea ta) {
        textArea = ta;
    }


// Writer methods
//...........................................................................

    public void write(char[] ca, int offset, int length) {
        append(new String(ca, offset, length));
    }

    public void write(String s) {
        append(s);
    }

    public void write(String s, int offset, int length) {
        append(s.substring(offset, length));
    }

    public void flush() {
        textArea.repaint();
//        textArea.repaint(REPAINT_DELAY);
    }

    public void close() {
        flush();
    }


// Own methods
//...........................................................................

    private void append(String s) {
        textArea.append(s);
    }
}
