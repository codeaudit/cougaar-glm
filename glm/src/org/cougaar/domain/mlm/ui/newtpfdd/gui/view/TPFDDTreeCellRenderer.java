/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/view/Attic/TPFDDTreeCellRenderer.java,v 1.2 2001-02-23 01:02:18 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  Based on Sun's example SampleTreeCellRenderer.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;


import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;


import org.cougaar.domain.mlm.ui.newtpfdd.gui.component.TPFDDColor;


public class TPFDDTreeCellRenderer extends JLabel implements TreeCellRenderer
{
    static boolean initialized = false;

    /** Font used if the string to be displayed isn't a font. */
    static protected Font             defaultFont;
    static protected final Color SelectedBackgroundColor = 
      TPFDDColor.TPFDDBurgundy;
    /** Whether or not the item that was last configured is selected. */
    protected boolean            selected;

    public TPFDDTreeCellRenderer(Font myFont)
    {
	setFont(myFont);
    }

    /**
     * This is messaged from JTree whenever it needs to get the size
     * of the component or it wants to draw it.
     * This attempts to set the font based on value, which will be
     * a TreeNode.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
						  boolean selected, boolean expanded,
						  boolean leaf, int row,
						  boolean hasFocus)
    {
	Font font;

	if ( value instanceof Node ) {
	    setText(((Node)value).getDisplayName());
	    /* Tooltips used by the tree. */
	    setToolTipText(((Node)value).getUUID());
	}

	if ( hasFocus )
	    setForeground(Color.cyan);
	else
	    setForeground(Color.white);

	// setFont(defaultFont);

	/* Update the selected flag for the next paint. */
	this.selected = selected;

	return this;
    }

    /**
     * paint is subclassed to draw the background correctly.  JLabel
     * currently does not allow backgrounds other than white, and it
     * will also fill behind the icon.  Something that isn't desirable.
     */
    public void paint(Graphics g)
    {
	Color bColor;

	if ( selected )
	    bColor = SelectedBackgroundColor;
	else if ( getParent() != null )
	    /* Pick background color up from parent (which will come from
	       the JTree we're contained in). */
 	    bColor = getParent().getBackground();
	else
 	   bColor = getBackground();
	g.setColor(bColor);
	g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
	super.paint(g);
    }
}
