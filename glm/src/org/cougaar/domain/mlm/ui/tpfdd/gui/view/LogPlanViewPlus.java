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


import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import org.cougaar.domain.mlm.ui.tpfdd.gui.component.TPFDDColor;

import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.SwingQueue;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;

import org.cougaar.domain.mlm.ui.tpfdd.gui.model.ItemPoolModelListener;

import org.cougaar.domain.mlm.ui.tpfdd.producer.ClusterCache;


public class LogPlanViewPlus extends JPanel implements ItemPoolModelListener
{
    private JTreeTablePlus treeTable;
    private TreeTableCellRendererPlus tree;
    private TaskModel model;

    public LogPlanViewPlus(ClientPlanElementProvider provider, ClusterCache clusterCache, TestNewView parentView,
		       int fontSize)
    {
	model = new TaskModel(provider);
	Font font = new Font("SansSerif", Font.PLAIN, fontSize);
	treeTable = new JTreeTablePlus(model, clusterCache, provider, parentView, font);
	treeTable.setShowGrid(true);
	treeTable.setForeground(Color.white);
	treeTable.setBackground(Color.gray);
	treeTable.setSelectionForeground(Color.white);
	treeTable.setSelectionBackground(TPFDDColor.TPFDDBurgundy);

	treeTable.getTableHeader().setForeground(Color.white);
	treeTable.getTableHeader().setBackground(TPFDDColor.TPFDDBlue);
	ToolTipManager.sharedInstance().registerComponent(treeTable);

	TableCellRenderer renderer = treeTable.getDefaultRenderer(TreeTableModel.class);
	tree = (TreeTableCellRendererPlus)renderer;
	tree.setForeground(Color.white);
	tree.setBackground(Color.gray);
	tree.setRootVisible(false);
	tree.setShowsRootHandles(true);
	setBackground(Color.gray);
	setLayout(new BorderLayout());
	JScrollPane pane = new JScrollPane(treeTable);
	pane.setBackground(Color.gray);
	add(pane, BorderLayout.CENTER);
	setVisible(true);
    }

    // This all appears a bit redundant but parts may get specialized
    // and/or optimized in the future.  For example JTree does have
    // specific methods for adding vs. deleting vs. changing but
    // they're buggy now (as I'm told.) -DB

    // Consumer portion of ItemPoolModelListener interface
    public void fireAddition(Object item)
    {
	fireItemAdded(item);
    }

    public void fireDeletion(Object item)
    {
	fireItemDeleted(item);
    }

    public void fireChange(Object item)
    {
	// Debug.out("LPV:fC " + item);
	fireItemValueChanged(item);
    }

    // Remainder of ItemPoolModelListener interface
    public void fireItemAdded(Object item)
    {
      //Debug.out("LPV:fIA " + item);
	handleChange(item);
    }

    public void fireItemDeleted(Object item)
    {
      //Debug.out("LPV:fID " + item);
	handleChange(item);
    }

    public void fireItemWithIndexDeleted(Object item, int index)
    {
      //Debug.out("LPV:fIWID " + item + " " + index);
	if ( item == null || !(item instanceof TaskNode) )
	    return;

	TaskNode node = (TaskNode)item;

	final TaskModel taskModel = (TaskModel)treeTable.getTreeTableModel();
	Object parent = node.getParent_();
	final Object path[] = taskModel.getPathToNode(parent);

	final int indices[] = new int[1];
	indices[0] = index;

	final Object children[] = { null };
	
	/* Debug.out("LPV:fIWID changed index is " + indices[0]);
	for ( int i = 0; i < path.length; i++ )
	Debug.out("LPV:fIWID " + i + ": " + path[i]); */
	Runnable fireTreeChangedRunnable = new Runnable() {
	    public void run() {
	      // this should be attm
	      // path is array containing root node, indices is integer array one element
	      // value is index of deleted node, fourth is single element which is node 18
	      // for deletes the indexes represent 
	      taskModel.fireTreeStructureChanged(taskModel, path, indices, children);
	    }
	  };

	SwingQueue.invokeLater(treeTable.getFireChangedRunnable());
	SwingQueue.invokeLater(fireTreeChangedRunnable);
    }

    public void fireItemValueChanged(Object item)
    {
	handleChange(item);
    }

    public void handleChange(Object itemVar)
    {
	final Object item = itemVar;

	if ( item == null || !(item instanceof TaskNode) )
	    return;

	TaskNode node = (TaskNode)item;

	final TaskModel taskModel = (TaskModel)treeTable.getTreeTableModel();
	Object parent = node.getParent_();
	final Object path[] = taskModel.getPathToNode(parent);

	final int indices[] = new int[1];
	indices[0] = taskModel.getIndexOfChild(parent, node);

	final Object children[] = { node };
	
	// Debug.out("LPV:hC changed index is " + indices[0]);
	// for ( int i = 0; i < path.length; i++ )
        // Debug.out("LPV:hC " + i + ": " + path[i]);
	Runnable fireTreeChangedRunnable = new Runnable() {
	    public void run() {
	      // this should be attm
	      // path is array containing root node, indices is integer array one element
	      // value is index of deleted node, fourth is single element which is node 18
	      // for deletes the indexes represent 
	      //Debug.out("LPV:hC item " + item + " fTSC: tM " + taskModel + " path " + path +
	      //"indices " + indices + " children " + children);
	      taskModel.fireTreeStructureChanged(taskModel, path, indices, children);
	    }
	  };

	SwingQueue.invokeLater(treeTable.getFireChangedRunnable());
	SwingQueue.invokeLater(fireTreeChangedRunnable);
    }

    public void firingComplete()
    {
	Runnable firingCompleteRunnable = new Runnable()
	    {
		public void run()
		{
		    treeTable.invalidate();
		    treeTable.repaint();
		    invalidate();
		    repaint();
		}
	    };
	SwingQueue.invokeLater(firingCompleteRunnable);
    }
}
