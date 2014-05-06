package gui;

import java.awt.*;
import javax.swing.*;

public class Rule extends JComponent {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SIZE = 25;

    public int orientation;
    private int increment = 100;
    
    /** Reference to the component around which is drawn this rule */
    private JJImgPanel imgPan = null;

    /** Image dimension and offset in canvas */
    private Rectangle imgCo = null;

    /** JScrollPane instance */
    private JScrollPane scrollPane;

    /** Class constructor */
    public Rule(int o, JJImgPanel imgPan, Rectangle imgCo, 
		JScrollPane scrollPane) {
	this.imgPan = imgPan;
	this.scrollPane = scrollPane;
	this.imgCo = imgCo;
        orientation = o;
    }

    /** Set the preferred height */
    public void setPreferredHeight(int ph) {
        setPreferredSize(new Dimension(SIZE,ph));
    }

    /** Set the preferred width */
    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(pw,SIZE));
    }

    /** Paint the ruler */
    public void paintComponent(Graphics g) {
	double zf = imgPan.getZoomFactor();
        Rectangle ruleRect = g.getClipBounds();
	Rectangle scrollRect = scrollPane.getViewport().getViewRect();
	imgPan.repaint();
	Rectangle viewRect = imgPan.getCurrentView();

        // Do the ruler labels in a small font that's black.
        g.setFont(new Font("SansSerif",Font.PLAIN,10)); 
        g.setColor(Color.black);

        // Some vars we need.
        int end = 0;
        int start = 0;
        String text = null;
    
        // Use clipping bounds to calculate first tick and last tick location.
        if (orientation == HORIZONTAL) { // Horizontal rule
            start = (scrollRect.x / increment) * increment;
            end = ( (scrollRect.x+scrollRect.width) / increment) * increment;
	    while(start<scrollRect.x) start += increment;
        } else { // Vertical rule
            start = (scrollRect.y / increment) * increment;
            end = ( (scrollRect.y+scrollRect.height) / increment) * increment;
	    while(start<scrollRect.y) start += increment;
        }

        // Make a special case of 0 to display the number within the rule
	int tickLength = 5;
        // ticks and labels
        for (int i=start; i<=end; i+=increment) {
	    text = Integer.toString((int)(i*zf));

	    if (orientation == HORIZONTAL) { // Horizontal rule
		g.drawLine(i,SIZE-1,i,SIZE-tickLength-1);
		if (text != null) {
		    g.drawString(text,i+2,15);
		}
	    } else { // Vertical rule
		g.drawLine(SIZE,i,SIZE-tickLength-1,i);
		if (text != null)
		    g.drawString(text,5,i+10);
	    }
        }
    }
}
