package gui;

import jj2000.j2k.encoder.*;
import jj2000.j2k.util.*;
import jj2000.j2k.*;

import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;

/** 
 * Implementation of MsgLogger for outputting JJ2000 messages in dialog boxes
 * */
class JJPopup implements MsgLogger {
    /** Reference to the main frame */
    private JDesktopPane desktop;

    /** Buffer for information messages */
    private String infoBuffer = "";

    /** Buffer for warning messages */
    private String warningBuffer = "";

    /** Class constructor */
    public JJPopup(JDesktopPane desktop) {
	this.desktop = desktop;
    }

    /** Reinitialize info and warning buffers */
    public void reset() {
        infoBuffer = "";
        warningBuffer = "";
    }

    /** Flush the content of each buffer in corresponding dialog boxes */
    public void flush() {
	if(infoBuffer!="") {
            JTextArea ta = new JTextArea(infoBuffer,10,40);
            ta.setEditable(false);
            ta.setBackground(Color.lightGray);
	    JOptionPane.
                showInternalMessageDialog(desktop,new JScrollPane(ta),
                                          "info",
                                          JOptionPane.INFORMATION_MESSAGE);
	}
	if(warningBuffer!="") {
            JTextArea ta2 = new JTextArea(warningBuffer,10,40);
            ta2.setEditable(false);
            ta2.setBackground(Color.lightGray);
	    JOptionPane.
                showInternalMessageDialog(desktop,new JScrollPane(ta2),
                                          "Warning",
                                          JOptionPane.WARNING_MESSAGE);
	}
	infoBuffer = "";
	warningBuffer = "";
    }

    /** Appends information message to the relevant buffer */
    public void println(String str, int flind, int ind) {
	infoBuffer += str+"\n";
    }

    /** Appends message to the relevant buffer */
    public void printmsg(int sev, String msg) {
	switch(sev) {
	case MsgLogger.INFO:
            infoBuffer += msg+"\n";
	    break;
	case MsgLogger.WARNING:
            warningBuffer += msg+"\n";
	    break;
	case MsgLogger.ERROR:
	    JOptionPane.showInternalMessageDialog(desktop,msg,"Error",
						  JOptionPane.ERROR_MESSAGE);
	    break;
	default:
	    JOptionPane.showInternalMessageDialog(desktop,"Severity "+sev+
						  " not valid.","Warning",
						  JOptionPane.WARNING_MESSAGE);
	    throw new IllegalArgumentException("Severity "+sev+" not valid."); 
	}
    }
}
