/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/view/Attic/UnitPanel.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


import java.util.Vector;
import java.util.Iterator;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JPanel;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

import javax.swing.event.TreeSelectionListener;

import java.awt.Color;
import java.awt.BorderLayout;

import org.cougaar.domain.mlm.ui.tpfdd.gui.component.TPFDDColor;

import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.tpfdd.producer.UnitHierarchy;


public class UnitPanel extends JPanel
{
    private UnitHierarchy units;
    private JScrollPane scroll;
    private JTree tree;
    private TreeCellRenderer renderer;
    private String selectedCluster;
    private TreeSelectionListener parent;

    public String getSelectedCluster()
    {
	return selectedCluster;
    }

    private UnitHierarchy getunits()
    {
	if ( units == null ) {
	    try {
		units = new UnitHierarchy();
	    }
	    catch ( RuntimeException e ) {
		handleException(e);
	    }
	}
	return units;
    }

    private JScrollPane getscroll()
    {
	if ( scroll == null ) {
	    try {
		scroll = new JScrollPane(gettree());
		scroll.setBackground(Color.gray);
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return scroll;
    }

    private JTree gettree()
    {
	if ( tree == null ) {
	    try {
		tree = new JTree(getunits());
		getunits().setTree(tree);
		tree.expandRow(0);
		tree.setSelectionRow(0);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setForeground(Color.white);
		tree.setBackground(Color.gray);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(getRenderer());
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return tree;
    }

    private TreeCellRenderer getRenderer()
    {
	DefaultTreeCellRenderer internal;

	if ( renderer == null ) {
	    try {
		internal = new DefaultTreeCellRenderer();
		internal.setLeafIcon(null);
		internal.setOpenIcon(null);
		internal.setClosedIcon(null);
		internal.setBackground(Color.gray);
		internal.setBackgroundSelectionColor(TPFDDColor.TPFDDBurgundy);
		internal.setBackgroundNonSelectionColor(Color.gray);
		renderer = internal;
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return renderer;
    }

    public UnitPanel(TreeSelectionListener parent)
    {
	super();
	this.parent = parent;
	setName("unitPanel");
	setLayout(new BorderLayout());
	add(getscroll(), BorderLayout.CENTER);
	getunits().expandAll();
	gettree().addTreeSelectionListener(parent);
    }
	    
    private void handleException(Exception e)
    {
	OutputHandler.out(ExceptionTools.toString("UD:hE", e));
    }

    public void selectAllChildrenOfSelected()
    {
	gettree().removeTreeSelectionListener(parent);
	String[] selectedNames = getUnitNames();
	if ( selectedNames == null ) {
	    gettree().addTreeSelectionListener(parent);
	    return;
	}
	for ( int i = 0; i < selectedNames.length; i++ ) {
	    Vector deepChildren = getunits().getDeepCommandees(selectedNames[i]);
	    for ( Iterator dCi = deepChildren.iterator(); dCi.hasNext(); ) {
		String child = (String)(dCi.next());
		TreePath childPath = getunits().getPathToNode(child);
		gettree().addSelectionPath(childPath);
	    }
	}
	parent.valueChanged(null);
	gettree().addTreeSelectionListener(parent);
    }

    public String[] getUnitNames()
    {
	String[] selectedUnits = null;
	int[] rows = gettree().getSelectionRows();
	if ( rows != null && rows.length > 0 ) {
	    selectedUnits = new String[rows.length];
	    for ( int i = 0; i < rows.length; i++ ) {
		TreePath path = gettree().getPathForRow(rows[i]);
		selectedUnits[i] = (String)(path.getLastPathComponent());
	    }
	}
	return selectedUnits;
    }

    public void setUnitName(String name)
    {
	Debug.out("UP:sUN name " + name + " path " + getunits().getPathToNode(name));
	gettree().setSelectionPath(getunits().getPathToNode(name));
    }
}
