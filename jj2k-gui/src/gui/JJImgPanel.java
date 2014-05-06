package gui;

import javax.swing.*;
import java.awt.*;

/** Class to display Image instance in a JPanel object */
public class JJImgPanel extends JPanel {
    /** Reference to the main frame's desktop */
    private Main desktop;

    /** Reference to Image instance to draw in the panel */
    private Image image;
    /** Image position and dimension in the canvas */
    private Rectangle rect;
    /** The number of components in the image */
    private int nc;

    /** Whether or not a shape has to be drawn */
    private boolean drawing = false;
    /** No shape */
    private final static int NONE = 0;
    /** Rectangular ROI */
    private final static int RECT_ROI = 1;
    /** Circular ROI */
    private final static int CIRC_ROI = 2;
    /** Tile partition */
    private final static int TILE_PARTITION = 3;
    /** Shape type */
    private int shapeType = NONE;

    /** Shape coordinates and dimension */
    private int x,y,w,h;
    
    /** Displayed image's zooming factor */
    private double zf = 1;

    /** Current view */
    private Rectangle currentView = null;

    /** Class constructor. It also determines the initial zooming factor
     * such that all the image appears in the desktop */
    public JJImgPanel(Image image,Rectangle rect,int nc,Main desktop) {
        this.image = image;
        this.rect = rect;
        this.nc = nc;
        this.desktop = desktop;

        zf = 1;
        int dw = desktop.getWidth();
        int dh = desktop.getHeight();
        while(rect.width/zf>0.8*dw || rect.height/zf>0.8*dh) {
            zf *= 2;
        }

        setPreferredSize(new Dimension((int)(rect.width/zf),
				       (int)(rect.height/zf)));
    }

    /** Returns the number of components in the image */
    public int getNumComps() {
        return nc;
    }

    /** Return the image's width beside any zooming */
    public int getOrigWidth() {
        return rect.width;
    }

    /** Return the image's height beside any zooming */
    public int getOrigHeight() {
        return rect.height;
    }

    /** Returns the current view */
    public Rectangle getCurrentView() {
	return currentView;
    }

    /** Image horizontal offset in canvas */
    public int getOffX() {
	return rect.x;
    }

    /** Image vertical offset in canvas */
    public int getOffY() {
	return rect.y;
    }
    
    /** Set whether or not a ROI's shape has to be drawn */
    public void enableDrawing(boolean state) {
        drawing = state;
	if(state==false) {
	    shapeType = NONE;
	}
        repaint();
    }

    /** Specify the tile partition */
    public void setTilePartition(Rectangle tilePart) {
	shapeType = TILE_PARTITION;
	this.x = tilePart.x;
	this.y = tilePart.y;
	this.w = tilePart.width;
	this.h = tilePart.height;
	repaint();
    }

    /** 
     * Specify the coordinates and the dimension of the rectangular ROI to be
     * drawn 
     * */
    public void drawRect(int x,int y,int w,int h) {
	shapeType = RECT_ROI;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        repaint();
    }
    
    /** Specify the coordinates and the dimension of the circular ROI to be
     * drawn */
    public void drawOval(int x,int y,int w,int h) {
        shapeType = CIRC_ROI;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        repaint();
    }

    /** Specify a new image offset in canvas */
    public void setOffset(int x,int y) {
	rect.x = x;
	rect.y = y;
        setPreferredSize(new Dimension((int)((rect.width+x)/zf),
				       (int)((rect.height+y)/zf)));
	revalidate();
 	repaint();
    }

    /** Zoom in the image drawn in panel */
    public void zoomIn() {
        zf = zf*0.5;
        setPreferredSize(new Dimension((int)(rect.width/zf),
				       (int)(rect.height/zf)));
        revalidate();
        repaint();
    }

    /** Zoom out of the image drawn in panel */
    public void zoomOut() {
        zf = zf*2;
        setPreferredSize(new Dimension((int)(rect.width/zf),
				       (int)(rect.height/zf)));
        revalidate();
        repaint();
    }

    /** Display the image at its full resolution */
    public void zoomFull() {
        zf = 1;
        setPreferredSize(new Dimension((int)(rect.width/zf),
				       (int)(rect.height/zf)));
        revalidate();
        repaint();
    }

    /** Returns the zoom factor used to display the image */
    public double getZoomFactor() {
        return zf;
    }

    /** Paint the image and the specified shapes in the panel */
    public void paintComponent(Graphics g) {
	currentView = g.getClipBounds();
        g.drawImage(image,(int)(rect.x/zf),(int)(rect.y/zf),
		    (int)(rect.width/zf),(int)(rect.height/zf),desktop);
        if(drawing) {
            g.setColor(Color.white);
	    switch(shapeType) {
	    case RECT_ROI:
                g.drawRect(x,y,w,h);
		break;
	    case CIRC_ROI:
                g.drawOval(x,y,w,h);
		break;
	    case TILE_PARTITION:
		int totWidth = (int)((rect.width+rect.x)/zf);
		int totHeight = (int)((rect.height+rect.y)/zf);
		// Horizontal lines
		for(int i=y; i<totHeight; i+=h) {
		    g.drawLine(x,i,totWidth,i);
		}
		// Vertical lines
		for(int j=x; j<totWidth; j+=w) {
		    g.drawLine(j,y,j,totHeight);
		}
		break;
	    }
        }
    }
}
