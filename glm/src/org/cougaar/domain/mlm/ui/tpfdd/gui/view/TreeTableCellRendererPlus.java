/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;


import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;


public class TreeTableCellRendererPlus extends JTree implements TableCellRenderer 
{
    protected int visibleRow;
    JTreeTablePlus treeTable = null;

    public TreeTableCellRendererPlus(TreeModel model, JTreeTablePlus treeTab, Font myFont)
    { 
	super(model);
	expandRow(0);
	treeTable = treeTab;

	// set default properties
	setForeground(Color.white);
	setBackground(Color.gray);
	setRootVisible(false);
	setShowsRootHandles(true);
	
	setCellRenderer(new TPFDDTreeCellRenderer(myFont));

	// Check if this is the right way to do it.
	// I had to do this to make programmatic adds to trees to work.
	// Almost wasted a day before I figured out that nodes* weren't
	// really being handled -- tree structure changes seem to work
	// right.
	model.addTreeModelListener(new TreeModelListener() {
	    public void treeNodesChanged(TreeModelEvent e) { }
	    public void treeNodesInserted(TreeModelEvent e) { }
	    public void treeNodesRemoved(TreeModelEvent e) { }
	    public void treeStructureChanged(TreeModelEvent e) {
		TreePath p = new TreePath(e.getPath());
		int val = getRowForPath(p);
		setVisibleRowCount(val);
	    }
	});
    }

    public void setBounds(int x, int y, int w, int h)
    {
	if ( treeTable == null )
	    return;
	super.setBounds(x, 0, w, treeTable.getHeight());
    }

    public void setExpanded(TreePath path, boolean state)
    {
	super.setExpandedState(path, state);
    }

    public void paint(Graphics g)
    {
	g.translate(0, -visibleRow * getRowHeight());
	super.paint(g);
    }

    public Component getTableCellRendererComponent(JTable table,
						   Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row, int column)
    {
	if ( isSelected )
	    setBackground(table.getSelectionBackground());
	else
	    setBackground(table.getBackground());
       
	visibleRow = row;
	return this;
    }
}
