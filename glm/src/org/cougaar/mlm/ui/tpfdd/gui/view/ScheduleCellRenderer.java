/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/view/Attic/ScheduleCellRenderer.java,v 1.1 2001-12-27 22:44:26 bdepass Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Sundar Narasimhan, Daniel Bromberg
*/


package org.cougaar.mlm.ui.tpfdd.gui.view;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.util.Date;

import org.cougaar.mlm.ui.tpfdd.gui.component.TPFDDColor;

import org.cougaar.mlm.ui.tpfdd.util.Debug;
import org.cougaar.mlm.ui.tpfdd.util.OutputHandler;


public class ScheduleCellRenderer extends JLabel implements TableCellRenderer
{
    long earliest, latest, start, end, estStart, estEnd, minEnd, maxEnd, minStart;
    ScheduleElement elem;
    static protected Font defaultFont;
    FontMetrics defaultFontMetrics = null;
    int fontHeight = 0;
    int fontDescent = 0;
    long overrun = 0;
    boolean texttoleft = false;
    int xPoints[] = new int[4];
    int yPoints[] = new int[4];
    boolean isSelected;
    boolean isTransport;

    private static int EDGEWIDTH = 75;

    static
    {
	try {
	    defaultFont = new Font("SansSerif", 0, 9);
	} catch (RuntimeException e) {
	    System.err.println(e);
	}
    }

    void drawDownTriangle(Graphics g, int x, int y, Color c)
    {
	Color save = g.getColor();
	g.setColor(c);
	xPoints[0] = x - 3; xPoints[1] = x + 3; xPoints[2] = x;
	yPoints[0] = y - 3; yPoints[1] = y - 3; yPoints[2] = y + 2;
	g.fillPolygon(xPoints, yPoints, 3);
	g.setColor(save);
    }

    void drawUpTriangle(Graphics g, int x, int y, Color c)
    {
	Color save = g.getColor();
	g.setColor(c);
	xPoints[0] = x - 3; xPoints[1] = x + 3; xPoints[2] = x;
	yPoints[0] = y + 3; yPoints[1] = y + 3; yPoints[2] = y - 2;
	g.fillPolygon(xPoints, yPoints, 3);
	g.setColor(save);
    }

    void drawDiamond(Graphics g, int x, int y, Color c)
    {
	Color save = g.getColor();
	g.setColor(c);
	xPoints[0] = x; xPoints[1] = x + 4; xPoints[2] = x; xPoints[3] = x - 4;
	yPoints[0] = y - 4; yPoints[1] = y; yPoints[2] = y + 4; yPoints[3] = y;
	g.fillPolygon(xPoints, yPoints, 4);
	g.setColor(save);
    }

    public void drawCenteredString(Graphics g, String s, int x, int y, Color c)
    {
	Color cs = g.getColor();
	int w = defaultFontMetrics.stringWidth(s);
	if ( x - (w / 2) - 2 >= 0 ) {
	    g.setColor(c);
	    g.drawString(s, x - (w / 2), y + (fontHeight / 2 - fontDescent));
	}
	g.setColor(cs);
    }
    
    public void paint(Graphics g)
    {
	// Debug.out("SCR:paint enter " + ((TaskNode)elem).getUUID());
	long range = latest - earliest;
	Dimension cellDim = getSize();

	g.setFont(defaultFont);
	if ( defaultFontMetrics == null ) {
	    defaultFontMetrics = g.getFontMetrics();
	    fontHeight = defaultFontMetrics.getHeight();
	    fontDescent = defaultFontMetrics.getDescent();
	}
	
	g.setColor(Color.black);
	g.fillRect(0, 0, cellDim.width - 1, cellDim.height - 1);

	if ( isSelected ) {
	    g.setColor(Color.yellow);
	    g.drawRect(2, 2, cellDim.width - 2, cellDim.height - 2);
	    g.setColor(Color.black);
	}

	if ( (start == 0 || end == 0)
	     && (estStart == 0 || estEnd == 0) ) {
	    String string;
	    if ( !isTransport ) {
		g.setColor(TPFDDColor.TPFDDGray);
		string = "No known tasks in unit";
	    }
	    else {
		g.setColor(TPFDDColor.TPFDDPink);
		string = "Missing schedule information";
	    }
	    g.fillRect(50, 2, cellDim.width - 100, cellDim.height - 5);
	    g.setColor(Color.white);
	    drawCenteredString(g, string, cellDim.width / 2, cellDim.height / 2, Color.white);
	    return;
	}
	int startLoc = 0, endLoc = 0;
	String startDateString = null, endDateString = null;
	String minStartString = null, minEndString = null, maxEndString = null;
	int width;
	boolean dataFound = false;

	if ( start != 0 && end != 0 ) {
	    // Debug.out("SCR:paint using start " + start + " end " + end);
	    dataFound = true;
	    //	    if ( end - start < (3600 * 1000 * 48) ) {
		startDateString = TaskNode.longDate(elem.getActualStart());
		endDateString = TaskNode.longDate(elem.getActualEnd());
		//	    }
		//	    else {
		//		startDateString = TaskNode.shortDate(elem.getActualStart());
		//		endDateString = TaskNode.shortDate(elem.getActualEnd());
		//	    }
	    startLoc = (int)((start - earliest) * (cellDim.width - 2*EDGEWIDTH) / range) + EDGEWIDTH;
	    endLoc = (int)((end - earliest) * (cellDim.width - 2*EDGEWIDTH) / range) + EDGEWIDTH;

	    // draw Actual Schedule Bar
	    switch( elem.getMode() ) {
		case TaskNode.MODE_GROUND:
		    g.setColor(TPFDDColor.TPFDDDullerYellow);
		    break;
		case TaskNode.MODE_SEA:
		    g.setColor(TPFDDColor.TPFDDGreen);
		    break;
		case TaskNode.MODE_AIR:
		    g.setColor(TPFDDColor.TPFDDBlue);
		    break;
	        case TaskNode.MODE_ITINERARY:
		    g.setColor(TPFDDColor.TPFDDBurgundy);
		    break;
	        case TaskNode.MODE_AGGREGATE:
		    g.setColor(TPFDDColor.TPFDDPurple);
		    break;
		case TaskNode.MODE_UNKNOWN:
		    g.setColor(Color.gray);
		    break;
		default:
		    g.setColor(Color.red);
		}

	    g.fillRect(startLoc, 2, endLoc - startLoc, cellDim.height - 5);
	    if ( elem.getMode() == TaskNode.MODE_ITINERARY && !elem.isContiguous() )
		drawCenteredString(g, "DISCONTIGUOUS", cellDim.width / 2, cellDim.height / 2, Color.yellow);
	}
	
	if ( estStart != 0 && estEnd != 0 ) {
	    int estStartLoc = (int)((estStart - earliest) * (cellDim.width - 100) / range) + 50;
	    int estEndLoc = (int)((estEnd - earliest) * (cellDim.width - 100) / range) + 50;
	    g.setColor(TPFDDColor.TPFDDGreen);

	    // draw Estimated Schedule Markers
	    g.fillRect(estStartLoc, 2, 3, cellDim.height - 5);
	    g.fillRect(estEndLoc - 3, 2, 3, cellDim.height - 5);

	    if ( !dataFound ) { // Just estimated, but better than no string
		dataFound = true;
		startDateString = TaskNode.longDate(elem.getEstimatedStart());
		endDateString = TaskNode.longDate(elem.getEstimatedEnd());
		startLoc = estStartLoc;
		endLoc = estEndLoc;
	    }
	}


	g.setColor(Color.white);
	// put start date if it fits
	width = defaultFontMetrics.stringWidth(startDateString);
	if ( startLoc - width - 4 >= 0 )
	    g.drawString(startDateString, startLoc - width - 2, cellDim.height / 2
			 + (fontHeight / 2 - fontDescent));
	// put end date if it fits
	width = defaultFontMetrics.stringWidth(endDateString);
	if ( endLoc + width + 4 < cellDim.width )
	    g.drawString(endDateString, endLoc + 2, cellDim.height / 2
			 + (fontHeight / 2 - fontDescent));

	if ( minStart != 0 ) {
	    int minStartLoc = (int)((minStart - earliest) * (cellDim.width - 100) / range) + 50;
	    if ( start >= minStart )
		/* drawDiamond(g, minStartLoc, (cellDim.height / 2) - 1, Color.cyan) */;
	    else
		drawDiamond(g, minStartLoc, (cellDim.height / 2) - 1, Color.red);

	    minStartString = TaskNode.longDate(minStart);
	    width = defaultFontMetrics.stringWidth(minStartString);
	    // don't draw the string if it will overlap w/ the left edge
	    if ( minStartLoc - width - 4 > startLoc ) {
		g.setColor(Color.red);
		g.drawString(minStartString,
			     minStartLoc - width - 4, cellDim.height / 2 + (fontHeight / 2 - fontDescent));
	    }
	}

	if ( minEnd != 0 ) {
	    int minEndLoc = (int)((minEnd - earliest) * (cellDim.width - 100) / range) + 50;
	    if ( end >= minEnd )
		/* drawUpTriangle(g, minEndLoc, (cellDim.height / 2) - 1, Color.cyan) */;
	    else
		drawUpTriangle(g, minEndLoc, (cellDim.height / 2) - 1, Color.red);
	}

	if ( maxEnd != 0 ) {
	    int maxEndLoc = (int)((maxEnd - earliest) * (cellDim.width - 100) / range) + 50;
	    if ( end <= maxEnd )
		/* drawDownTriangle(g, maxEndLoc, (cellDim.height / 2) + 1, Color.cyan) */;
	    else
		drawDownTriangle(g, maxEndLoc, (cellDim.height / 2) + 1, Color.red);
	}
    }

    public Component getTableCellRendererComponent(JTable table,
						   Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row, int column)
    {
	//	Debug.out("SCR:gTCRC enter " + ((TaskNode)value).getUUID() + "isTransport: "
	//		  + ((TaskNode)value).isTransport());
	setForeground(Color.white);
	if ( isSelected )
	    setBackground(table.getSelectionBackground());
	else
	    setBackground(table.getBackground());
       
	isSelected = isSelected;
	TaskNode taskNode = (TaskNode)value;

	isTransport = taskNode.isTransport();

	// Get min/max dates.
	TableModel tableModel = table.getModel();
	if ( !(tableModel instanceof TreeTableModelAdapter) ) {
	    OutputHandler.out("Unknown model!" + tableModel);
	    return this;
	}

	TreeTableModelAdapter treeTableModelAdapter = (TreeTableModelAdapter)tableModel;
	TreeTableModel treeTableModel = treeTableModelAdapter.getTreeTableModel();
	if ( !(treeTableModel instanceof TaskModel) ) {
	    OutputHandler.out("Unknown model!" + treeTableModel);
	    return this;
	}
	
	TaskModel taskModel = (TaskModel)treeTableModel;
	elem = taskNode;
	earliest = taskModel.getProvider().getMinTaskStart();
	latest = taskModel.getProvider().getMaxTaskEnd();
	start = taskNode.getActualStart();
	end = taskNode.getActualEnd();
	// Debug.out("SCR:gTCRC UUID: " + taskNode.getUUID() + " start: " + start + " end: " + end
	//	     + " earliest " + earliest + " latest " + latest);
	estStart = taskNode.getEstimatedStart();
	estEnd = taskNode.getEstimatedEnd();
	minStart = taskNode.getMinStart();
	minEnd = taskNode.getMinEnd();
	maxEnd = taskNode.getMaxEnd();
	// Debug.out("SCR:gTCRC leave " + ((TaskNode)value).getUUID());
	return this;
    }
}