/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/view/Attic/TaskGanttChart.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Jason Leatherman, Sundar Narasimhan, Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.gui.view;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.Date;

import java.awt.*;
import java.awt.geom.*;

import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.SwingQueue;

import org.cougaar.domain.mlm.ui.tpfdd.gui.component.TPFDDColor;

import org.cougaar.domain.mlm.ui.tpfdd.gui.component.GanttChart;
import org.cougaar.domain.mlm.ui.tpfdd.gui.component.LozengeRow;
import org.cougaar.domain.mlm.ui.tpfdd.gui.component.Lozenge;
import org.cougaar.domain.mlm.ui.tpfdd.gui.component.LozengeLabel;
import org.cougaar.domain.mlm.ui.tpfdd.gui.component.RowLabel;

import org.cougaar.domain.mlm.ui.tpfdd.producer.PlanElementProvider;
import org.cougaar.domain.mlm.ui.tpfdd.producer.TreeMap;


public class TaskGanttChart extends GanttChart
{
    public static final int DAYLEN = 1000 * 3600 * 24;
    private long last;
    private PlanElementProvider provider;
    protected static final int DECORATOR_CIRCLE_ANCHOR_LEFT = 0;
    protected static final int DECORATOR_CIRCLE_ANCHOR_RIGHT = 1;
    protected static final int DECORATOR_TRIANGLE_ANCHOR_LEFT = 2;
    protected static final int DECORATOR_TRIANGLE_ANCHOR_RIGHT = 3;
    
    // Set up decorator values
    static {
	DEC_RELATIVE_SIZE = 0.2f;
	
	decShapes = new Shape[4];
	decShapes[0] = new Ellipse2D.Float(-5.0f, -5.0f, 10.0f, 10.0f);
	decShapes[1] = new Ellipse2D.Float(-5.0f, -5.0f, 10.0f, 10.0f);
	GeneralPath tri = new GeneralPath();
	tri.moveTo(-5.0f, 10.0f);
	tri.lineTo(5.0f, 10.0f);
	tri.lineTo(0.0f, 0.0f);
	tri.lineTo(-5.0f, 10.0f);
	decShapes[2] = tri;
	decShapes[3] = tri;

	decYRelHeights = new float[4];
	decYRelHeights[0] = 0.5f;
	decYRelHeights[1] = 0.5f;
	decYRelHeights[2] = 0.5f;
	decYRelHeights[3] = 0.5f;

	decAttachLeft = new boolean[4];
	decAttachLeft[0] = true;
	decAttachLeft[1] = false;
	decAttachLeft[2] = true;
	decAttachLeft[3] = false;
    }
    
    public TaskGanttChart(PlanElementProvider provider)
    {
	super();
	this.provider = provider;
    }
    
    public void fitToView()
    {
	Runnable fitToViewRunnable = new Runnable()
	    {
		public void run()
		{
		    setVirtualXSize(provider.getMaxTaskEnd() - provider.getMinTaskStart() + 2 * TaskGanttChart.DAYLEN);
		    setVirtualXLocation(provider.getMinTaskStart() - TaskGanttChart.DAYLEN);
		}
	    };
	SwingQueue.invokeLater(fitToViewRunnable);
    }
    
    // ** GanttChart Overrides **
    public void firingComplete()
    {
	fitToView();
	super.firingComplete();
    }

    public Object readItem(int row)
    {
	// Debug.out("TaskGanttChart:rI enter " + row);
	TaskNode rootTask = (TaskNode)provider.getChild(provider.getRoot(), row);
	return rootTask;
    }

    protected LozengeRow makeLozengeRow(Object o)
    {
	if ( o == null || !(o instanceof TaskNode) ) {
	    OutputHandler.out("TaskGanttChart:mLR Error: received junk: " + o);
	    return null;
	}
	
	TaskNode node = (TaskNode)o;
	if ( !node.isRoot_() ) {
	    Debug.out("TaskGanttChart:mLR Non root node " + node + " received, ignoring.");
	    return null;
	}
	LozengeRow lozRow;
	if ( node.isStructural() )
	    lozRow = makeLozengeRowByTaskFlattening(node, 0);
	else
	    lozRow = makeLozengeRowByTaskFlattening(node, -1);
	return lozRow;
    }

    protected LozengeRow makeLabelRow(Object o)
    {
	if ( o == null || !(o instanceof TaskNode) ) {
	    OutputHandler.out("TaskGanttChart:mLabR Error: Received junk: " + o);
	    return null;
	}
	    
	TaskNode node = (TaskNode)o;
	if ( !node.isRoot_() )
	    return null;
	String s = node.getUnitName() + ":" + node.getDisplayName();

	myLabelPanel.setVirtualXLocation(0);
	myLabelPanel.setVirtualXSize(2 * DAYLEN);
	LozengeLabel lozLabel = new LozengeLabel(s);
	Lozenge loz = new Lozenge();
	loz.setForeground(Color.black);
	loz.setLeftTipType(Lozenge.SQUARE_TIP);
	loz.setRightTipType(Lozenge.SQUARE_TIP);
	loz.setLozengeVirtualXLocation(0);
	loz.setLozengeVirtualXSize(2 * DAYLEN);
	loz.setLozengeDescription(node.getLongName());
	loz.addLozengeLabel(lozLabel);
	LozengeRow lozRow = new LozengeRow();
	lozRow.setVirtualXLocation(0);
	lozRow.setVirtualXSize(2 * DAYLEN);
	lozRow.addLozenge(loz);
	return lozRow;
    }

    /**
     * Creates a LozengeRow using n for schedule data.
     * depth limits the extent of descent into n's children.
     * depth<0: no limit, use all descendants of n
     * depth=0: Use only n, no children
     * depth=1: Use n and its immediate children
     * ...
     **/
    public LozengeRow makeLozengeRowByTaskFlattening(TaskNode n, int depth)
    {
	// Debug.out("TaskGanttChart:mLRBTF " + n + " " + depth);
	int i;
	    
	// Collect data
	Vector tasks = expandToDepth(n, depth);
	// Debug.out("TaskGanttChart:mLRBTF tasks " + tasks);

	// Filter tasks with no schedule
	for ( i = 0; i < tasks.size(); i++ ) {
	    TaskNode t = (TaskNode)(tasks.elementAt(i));
	    if ( getStartValue(t) == 0 || getEndValue(t) == 0 ) {
		tasks.removeElementAt(i);
		i--;
	    }
	    // Remove tasks that have been marked as non trip
	    if (t.getTripTag() == TaskNode.NON_TRIP_TAG) {
		tasks.removeElementAt(i);
		i--;
	    }
	    /*if ( i > 6 ) {
		OutputHandler.out("TaskGanttChart:mLRBTF Warning: truncating oversized ("
				  + tasks.size() + ") node " + n + " at 7");
		break;
                }*/
	}

	Lozenge parentLeg = null;;
	Vector legs = new Vector();
	Vector waystations = new Vector();

	// Create legs
	LozengeRow lozrow = new LozengeRow(this);
	SortedTaskList taskleaves = new SortedTaskList();
	double heightOffset = 0.0;
	TaskNode t = null, oldt = null;
	for ( i = 0; i < tasks.size(); i++ ) {
	    oldt = t;
	    t = (TaskNode)(tasks.elementAt(i));
	    if ( i > 1 ) {
		long startVal = getStartValue(t);
		long oldEndVal = getEndValue(oldt);
		// forgiveness factor due to TOPS roundoff: push "pretty close" values to 1 hour apart
		if ( Math.abs(startVal - oldEndVal) < 3600 * 1000 )
		    startVal = oldEndVal + 3600 * 1000;
		if ( startVal < oldEndVal )
		    heightOffset += 0.1;
	    }

	    Lozenge loz = new Lozenge(this, t);
	    loz.setLeftTipType(Lozenge.SQUARE_TIP);
	    loz.setRightTipType(Lozenge.SQUARE_TIP);

	    loz.setLozengeVirtualXLocation(getStartValue(t));
	    loz.setLozengeVirtualXSize(getEndValue(t) - getStartValue(t));

	    loz.setRelativeHeight(0.3);
	    loz.setRelativeHeightOffset(heightOffset);
	    String lStr = TaskNode.longDate(getStartValue(t));
	    String rStr = TaskNode.longDate(getEndValue(t));

	    loz.setLozengeDescription(t.getCarrierName() + " " + t.getFromName() +"->"+ t.getToName()
				      + "[" + lStr + "-" + rStr + "]");

	    if ( t.hasChildren() && (((TaskNode)(t.getChild_(0))).isTransport()) ) {
		// if children don't cover all of their parent,
		//  warning color will show through
		loz.setForeground(Color.red);
		parentLeg = loz;
	    }
	    else {
		if ( t.getBestEnd() != 0 )
		    loz.addDecorator(DECORATOR_TRIANGLE_ANCHOR_RIGHT, t.getBestEnd());
		if ( t.getMinEnd() != 0 )
		    loz.addDecorator(DECORATOR_CIRCLE_ANCHOR_RIGHT, t.getMinEnd(), t.getActualEnd() < t.getMinEnd());
		if ( t.getMaxEnd() != 0 )
		    loz.addDecorator(DECORATOR_CIRCLE_ANCHOR_RIGHT, t.getMaxEnd(), t.getActualEnd() > t.getMaxEnd());
		if ( t.getMinStart() != 0 )
		    loz.addDecorator(DECORATOR_CIRCLE_ANCHOR_LEFT, t.getMinStart(),
				     t.getActualStart() < t.getMinStart());
		String longName = (t.getCarrierType().equals(t.getCarrierName()) ?
				   t.getCarrierType()
				   : t.getCarrierType() + ": " + t.getCarrierName());
		loz.addLozengeLabel(new LozengeLabel(longName, t.getCarrierType(), LozengeLabel.CENTER));
		switch( t.getMode() ) {
		case TaskNode.MODE_GROUND:
		    loz.setForeground(TPFDDColor.TPFDDDullerYellow);
		    break;
		case TaskNode.MODE_SEA:
		    loz.setForeground(TPFDDColor.TPFDDGreen);
		    // These are the only legs for which we know carrier names right now
		    break;
		case TaskNode.MODE_AIR:
		    loz.setForeground(TPFDDColor.TPFDDBlue);
		    break;
		case TaskNode.MODE_AGGREGATE:
		    loz.setForeground(TPFDDColor.TPFDDPurple);
		    break;
		case TaskNode.MODE_UNKNOWN:
		    loz.setForeground(Color.gray);
		    break;
		default:
		    loz.setForeground(Color.red);
		}
		legs.add(loz);
		taskleaves.add(t);		// keep track of final legs so we can compute way stations
	    }
	}
	
	// Render way stations
	if ( !taskleaves.isEmpty() ) {
	    Lozenge loz;
		
	    // Create origin
	    t = taskleaves.firstElement();
	    // Debug.out("TaskGanttChart:mLRTBF origin: " + t + " type: " + t.getSourceType());
	    loz = new Lozenge(this, t);
	    loz.setLeftTipType(Lozenge.POINTED_TIP);
	    loz.setRightTipType(Lozenge.SQUARE_TIP);
	    loz.setLozengeVirtualXLocation(getStartValue(t) - DAYLEN);
	    loz.setLozengeVirtualXSize(DAYLEN);
	    loz.setRelativeHeight(1.0);
	    loz.addLozengeLabel(new LozengeLabel(t.getFromName(), t.getFromCode(), LozengeLabel.CENTER));
	    String tilStr = TaskNode.longDate(getStartValue(t));

	    loz.setLozengeDescription(t.getFromName() + "(-- " + tilStr + "]");

	    loz.setForeground(TPFDDColor.TPFDDBurgundy);
	    waystations.add(loz);
	    // Create destination
	    t = taskleaves.lastElement();
	    // Debug.out("TaskGanttChart:mLRTBF destination: " + t + " type: " + t.getSourceType());
	    loz = new Lozenge(this, t);
	    loz.setLeftTipType(Lozenge.SQUARE_TIP);
	    loz.setRightTipType(Lozenge.POINTED_TIP);
	    loz.setLozengeVirtualXLocation(getEndValue(t));
	    loz.setLozengeVirtualXSize(DAYLEN);
	    loz.setRelativeHeight(1.0);
	    loz.addLozengeLabel(new LozengeLabel(t.getToName(), t.getToCode(), LozengeLabel.CENTER));
	    String sinceStr = TaskNode.longDate(getEndValue(t));
	    loz.setLozengeDescription(t.getToName() + "[" + sinceStr + "-)");
	    loz.setForeground(TPFDDColor.TPFDDBlueGreen);
	    waystations.add(loz);

	    // Create waypoints
	    Enumeration e = taskleaves.elements();
	    TaskNode prevLeg, nextLeg;
	    nextLeg = (TaskNode)(e.nextElement());
	    while ( e.hasMoreElements() ) {
		prevLeg = nextLeg;
		nextLeg = (TaskNode)(e.nextElement());

		// Skip ill-formed waypoints
		if ( getStartValue(nextLeg) < getEndValue(prevLeg) )
		    continue;
				
		loz = new Lozenge(this, t);
		loz.setLozengeVirtualXLocation(getEndValue(prevLeg));
		loz.setRelativeHeight(1.0);
		waystations.add(loz);

		if ( prevLeg.getToName().compareTo(nextLeg.getFromName()) == 0 ) {
		    loz.setLeftTipType(Lozenge.SQUARE_TIP);
		    loz.setRightTipType(Lozenge.SQUARE_TIP);
		    // if geolocs match
		    loz.setForeground(Color.orange);
		    loz.setLozengeVirtualXSize(getStartValue(nextLeg) - getEndValue(prevLeg));
		    loz.addLozengeLabel(new LozengeLabel(prevLeg.getToName(), prevLeg.getToCode(),
							 LozengeLabel.CENTER));
		    String lStr = TaskNode.longDate(getEndValue(prevLeg));
		    String rStr = TaskNode.longDate(getStartValue(nextLeg));
		    loz.setLozengeDescription(prevLeg.getToName() + " " + "[" + lStr + "-" + rStr + "]");
		}
		else {
		    loz.setLeftTipType(Lozenge.SQUARE_TIP);
		    loz.setRightTipType(Lozenge.BROKEN_TIP);
		    long halfWidth = (getStartValue(nextLeg) - getEndValue(prevLeg)) / 2;
		    loz.setForeground(Color.red);
		    loz.setLozengeVirtualXSize(halfWidth);
		    loz.addLozengeLabel(new LozengeLabel(prevLeg.getToName(), prevLeg.getToCode(),
							 LozengeLabel.RIGHT));
		    String lStr = TaskNode.longDate(getEndValue(prevLeg));
		    loz.setLozengeDescription(prevLeg.getToName() + " " + "[" + lStr + "-???]");

		    loz = new Lozenge(this, t);
		    loz.setForeground(Color.red);
		    loz.setLeftTipType(Lozenge.BROKEN_TIP);
		    loz.setRightTipType(Lozenge.SQUARE_TIP);
		    loz.setLozengeVirtualXLocation(getStartValue(nextLeg) - halfWidth);
		    loz.setLozengeVirtualXSize(halfWidth);
		    loz.setRelativeHeight(1.0);
		    loz.addLozengeLabel(new LozengeLabel(nextLeg.getFromName(), nextLeg.getFromCode(),
							 LozengeLabel.LEFT));
		    String rStr = TaskNode.longDate(getStartValue(nextLeg));
		    loz.setLozengeDescription(nextLeg.getFromName() + " " + " [???-" + rStr + "]");
		    waystations.add(loz);
		}
	    }
	}

	if ( parentLeg != null )
	    lozrow.addLozenge(parentLeg);
	for ( Iterator ileg = legs.iterator(); ileg.hasNext(); )
	    lozrow.addLozenge((Lozenge)(ileg.next()));
	for ( Iterator iway = waystations.iterator(); iway.hasNext(); )
	    lozrow.addLozenge((Lozenge)(iway.next()));
	
	return lozrow;
    }

    /**
     * Keeps a list of TaskNodes in chronologically sorted order.
     **/
    private class SortedTaskList
    {
	private Vector tasks = new Vector();

	/**
	 * Returns the index of the first task in the list whose
	 * starting time is greater than or equal to n.  (including
	 * the index beyond the end the list.)
	 **/
	public int findPlace(TaskNode n)
	{
	    int i;
	    for ( i = 0; i < tasks.size(); i++ )
		if ( getStartValue((TaskNode)tasks.elementAt(i)) >= getStartValue(n) )
		    break;
	    return i;
	}

	public void add(TaskNode n)
	{
	    int i = findPlace(n);
	    if ( i > tasks.size() - 1 )
		tasks.addElement(n);
	    else
		tasks.insertElementAt(n, i);
	}

	public Enumeration elements()
	{
	    return tasks.elements();
	}

	public boolean isEmpty()
	{
	    return tasks.isEmpty();
	}
	
	public int size()
	{
	    return tasks.size();
	}
	
	public TaskNode firstElement()
	{
	    return (TaskNode)(tasks.firstElement());
	}
	
	public TaskNode lastElement()
	{
	    return (TaskNode)(tasks.lastElement());
	}
	
	public TaskNode elementAt(int i)
	{
	    return (TaskNode)(tasks.elementAt(i));
	}
    }

    /**
     * "Expands" n by collecting its descendants.
     * depth = 0 results in just n; depth=1 results in
     * n and all its immediate children; and so on.
     **/
    private Vector expandToDepth(TaskNode node, int depth)
    {
      // System.out.println("expanding to " + depth + " " +
      //node.getDisplayName());

	Vector stack = new Vector();
	stack.addElement(node);
	int start = 0;	// index of first unexpanded element
	int end = 1;	// index after last unexpanded element

	int d = 0;
	while ( d < depth || depth < 0 ) {
	    for ( int i = start; i < end; i++ ) {
		TaskNode parent = (TaskNode)(stack.elementAt(i));
		TreeMap children = parent.getChildren_();
		if ( children != null )
		    for( int j = 0; j < children.size(); j++ ) {
			if ( children.get(j) == null ) {
			    OutputHandler.out("Null child # " + i + " of task " + parent.getUUID() + "!");
			    continue;
			}
			if ( ((TaskNode)children.get(j)).isTransport() )
			    stack.addElement(children.get(j));
		    }
	    }
	    start = end;
	    end = stack.size();
	    if ( start == end )
		break;
	    d++;
	}

	return stack;
    }

    private long getStartValue(TaskNode t)
    {
	if ( t.getActualStart() != 0 )
	    return t.getActualStart();
	return t.getEstimatedStart();
    }

    private long getEndValue(TaskNode t)
    {
	if ( t.getActualEnd() != 0 )
	    return t.getActualEnd();
	return t.getEstimatedEnd();
    }
}
