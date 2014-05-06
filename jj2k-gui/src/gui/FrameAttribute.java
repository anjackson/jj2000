package gui;

import jj2000.j2k.image.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/** Class to hold frame attributes (title, scroll pane, panel, ...) */
public class FrameAttribute {
    /** Number of components */
    public int nc = 0;

    /** Raw file containing displayed data */
    public File srcFile = null;

    /** Image offset and dimension in canvas */
    public Rectangle imgCo = null;

    /** Panel where the image is drawn */
    public JJImgPanel imgPan = null;

    /** Frame title */
    public String title = null;

    /** Extra information about the image */
    public String info = null;

    /** Scroll pane containing the image */
    public JScrollPane scrollPane = null;

    /** Construct the object with empty Objects */
    public FrameAttribute() {}
    
    /** Whether or not this a temporary file or not (if yes this file
     * has to be removed when closing the frame or the application) */
    public boolean isTemporaryFile = false;

    /** Construct the object with specified Objects */
    public FrameAttribute(int nc, File srcFile, Rectangle imgCo,
			  JJImgPanel imgPan, String title, String info,
			  JScrollPane scrollPane,boolean isTemporaryFile) {
	this.nc = nc;
	this.srcFile = srcFile;
	this.imgCo = imgCo;
	this.imgPan = imgPan;
	this.title = title;
	this.info = info;
	this.scrollPane = scrollPane;
	this.isTemporaryFile = isTemporaryFile;
    }

    public String toString() {
	return "nc="+nc+"\nsrcFile="+srcFile+"\nimgCo="+imgCo+"\ntitle="+
	    title+", isTemporaryFile="+isTemporaryFile+"\ninfo="+info;
    }
}
