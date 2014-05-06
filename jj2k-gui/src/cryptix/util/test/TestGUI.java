// $Id: TestGUI.java,v 1.1.1.1 2002/08/27 12:32:16 grosbois Exp $
//
// $Log: TestGUI.java,v $
// Revision 1.1.1.1  2002/08/27 12:32:16  grosbois
// Add cryptix 3.2
//
// Revision 1.1  1998/02/22 04:18:30  hopwood
// + Committed changes below.
//
// Revision 0.1.2  1998/02/22  hopwood
// + Import TextAreaWriter from cryptix.util.gui package.
//
// Revision 0.1.1  1998/02/19  hopwood
// + Added getMinimumSize and getPreferredSize.
// + Override update() to try to prevent flickering.
//
// Revision 0.1.0  1998/02/15  hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1998 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package cryptix.util.test;

import cryptix.util.gui.TextAreaWriter;

import java.io.PrintWriter;
import java.awt.*;
import java.awt.event.*;

/**
 * A GUI panel for the test classes.
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
public class TestGUI
extends Panel
{

// Constants and fields
//...........................................................................

    private static final Dimension minimumSize = new Dimension(100, 100);
    private static final Dimension preferredSize = new Dimension(300, 300);

    private Frame frame;
    private BaseTest owner;
    private PrintWriter output;
    private TextArea textArea;
//    private Button startButton;
//    private boolean running; // defaults to false
//    private final Object LOCK = new Object();
    private boolean allowExit; // defaults to false;


// Constructors
//...........................................................................

    /**
     * Creates a TestGUI with the given owner.
     */
    public TestGUI(BaseTest owner) {
        this.owner = owner;
        init();
    }

    protected void init() {
        textArea = new TextArea();
        textArea.setEditable(false);
        Font font = Font.getFont("monospaced");
        if (font != null) textArea.setFont(font);
        output = new PrintWriter(new TextAreaWriter(textArea));

        setLayout(new GridLayout(1, 1));
        add(textArea);
        textArea.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                allowExit = true;
                TestGUI.this.notifyAll();
            }
        });
        textArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                allowExit = true;
                TestGUI.this.notifyAll();
            }
        });
/*
        startButton = new Button(" Start test ");
        add(startButton);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                synchronized (LOCK) {
                    if (!running) {
                        running = true;
                        startButton.setEnabled(false);
                        new Thread() {
                            public void run() {
                                try { owner.test(); }
                                catch (TestException e) { e.printStackTrace(output); }
                            }
                        }.start();
                        startButton.setEnabled(true);
                        running = false;
                    }
                }
            }
        });
*/
    }


// Drawing
//...........................................................................

    /** Override update to avoid flicker. */
    public void update(Graphics g) { paint(g); }

    public Dimension getMinimumSize() { return minimumSize; }

    public Dimension getPreferredSize() { return preferredSize; }


// Own methods
//...........................................................................

    public PrintWriter getOutput() { return output; }

    public synchronized void useAppFrame(boolean flag) {
        if (flag && frame == null) {
            frame = new Frame();
            frame.setSize(preferredSize);
            frame.setTitle(getName());
            frame.add(this);
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    System.exit(TestException.ABORTED_BY_USER);
                }
            });
        } else if (!flag && frame != null) {
            frame.dispose();
            frame = null;
        }
    }

    public synchronized void waitForExit() {
        while (!allowExit) {
            try { wait(); }
            catch (InterruptedException e) {}
        }
    }
}
