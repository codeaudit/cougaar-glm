/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/view/Attic/JTreeTable.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
   Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
   Reserved.
  
   This material has been developed pursuant to the BBN/RTI "ALPINE"
   Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
   Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

   @author Sundar Narasimhan, Daniel Bromberg
*/

/*
 * Loosely based on Sun code which came with this notice.
 *
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */
    

package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Font;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Vector;
import java.util.Iterator;

import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.SwingQueue;
import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;



import org.cougaar.domain.mlm.ui.tpfdd.producer.LogPlanProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ThreadedProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ItineraryProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ClusterCache;
import org.cougaar.domain.mlm.ui.tpfdd.producer.UnitHierarchy;
import org.cougaar.domain.mlm.ui.tpfdd.producer.PSPClientConfig;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;


/**
 * This example shows how to create a simple JTreeTable component, 
 * by using a JTree as a renderer (and editor) for the cells in a 
 * particular column in the JTable.  
 *
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */

public class JTreeTable extends JTable implements ActionListener, SwingUpdate
{
    private TPFDDShell parentView;
    private ClientPlanElementProvider parentProvider;
    private ClusterCache clusterCache;

    protected TreeTableCellRenderer tree;
    protected TreeTableCellRenderer filtered;
    protected ScheduleCellRenderer  scheduleCellRenderer;
    protected Runnable fireChangedRunnable;

    JPopupMenu carrierPopup;    
    JCheckBoxMenuItem  miNonTransport;

    TreeTableModel originalModel;
    TreeTableModel currentModel;
    TreeTableModelAdapter ttModelAdapter;

    private JMenuItem TPFDDItem;
    private JMenuItem TPFDDAndLogPlanItem;
    private JMenuItem filterItem;
    private ClusterQuery clusterQuery;

    public void resetColumns()
    {
	// set column widths
	TableColumn column = null;
	column = getColumnModel().getColumn(0);	
	column.setPreferredWidth(200);
	column = getColumnModel().getColumn(1);	
	column.setPreferredWidth(100);
	column = getColumnModel().getColumn(2);	
	column.setPreferredWidth(90);
	column = getColumnModel().getColumn(3);	
	column.setPreferredWidth(90);
	column = getColumnModel().getColumn(4);
	column.setPreferredWidth(360);
    }

    public JTreeTable(TreeTableModel treeTableModel, ClusterCache clusterCache,
		      ClientPlanElementProvider parentProvider, TPFDDShell parentView, Font myFont)
    {
	super();
	setFont(myFont);
	setRowHeight(getFontMetrics(myFont).getHeight());
	this.clusterCache = clusterCache;
	this.parentProvider = parentProvider;
	this.parentView = parentView;
	originalModel = treeTableModel;
	currentModel = originalModel;

	// Create the tree. It will be used as a renderer and editor. 
	tree = new TreeTableCellRenderer(treeTableModel, this, myFont); 

	// Install a tableModel representing the visible rows in the tree. 
	super.setModel(ttModelAdapter = new TreeTableModelAdapter(treeTableModel, tree)); 

	// Force the JTable and JTree to share their row selection models. 
	tree.setSelectionModel(new DefaultTreeSelectionModel() { 
		// Extend the implementation of the constructor, as if: 
		/* public this() */ {
	    setSelectionModel(listSelectionModel); 
		} 
	    }); 
	// Make the tree and table row heights the same.
	tree.setRowHeight(getRowHeight());

	// Install the tree editor renderer and editor. 
	setDefaultRenderer(TreeTableModel.class, tree); 
	setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());  
	
	// Install the schedule cell renderer
	scheduleCellRenderer = new ScheduleCellRenderer();
	setDefaultRenderer(TaskNode.class, scheduleCellRenderer); 

	setShowGrid(false);
	setIntercellSpacing(new Dimension(0, 0)); 	        

	// set column widths
	resetColumns();

	MouseAdapter rowAdapter = new MouseAdapter()
	    {
		private MouseEvent saveEvent = null;
		private int TPFDDflags = MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK;
		
		public void mousePressed(MouseEvent e)
		{
		    // Debug.out("JTT:MTT:MA:mP PRESS " + e.getPoint());
 		    saveEvent = e;
		}

		public void mouseReleased(MouseEvent e)
		{
		    // Debug.out("JTT:MTT:MA:mR RELEASE " + e.getPoint());
		    if ( saveEvent != null
			 && e.getX() >= 0 && e.getX() <= e.getComponent().getWidth()
			 && e.getY() >= 0 && e.getY() <= e.getComponent().getHeight()
			 && Math.abs(e.getX() - saveEvent.getX()) < 25 // cells are much wider than tall
			 && Math.abs(e.getY() - saveEvent.getY()) < 10 )
			clicked(e);
		}

		private void clicked(MouseEvent e)
		{
		    saveEvent = null;
		    int row = rowAtPoint(e.getPoint());
		    int column = columnAtPoint(e.getPoint());
		    Object o = getValueAt(row, 4);
		    if ( !(o instanceof TaskNode) ) {
			OutputHandler.out("JTT:JTT:MA Warning: table was rearranged. Don't know where to get task.");
			return;
		    }
		    TaskNode node = (TaskNode)o;
		    if ( (e.getModifiers() & TPFDDflags) != 0 && column >= 0 && column < 4 ) {
			String carrierType, cargoType;
			if ( node.getSourceType() == TaskNode.CARRIER_TYPE ) {
			    carrierType = node.getCarrierType();
			    cargoType = "All cargo";
			}
			else {
			    carrierType = "All carriers";
			    cargoType = node.getDirectObjectType();
			}
			getTPFDDAndLogPlanItem().setText("New TPFDD and Log Plan: " + node.getUnitName()
							 + "/" + carrierType + "/" + cargoType);
			getTPFDDItem().setText("New TPFDD: " + node.getUnitName() + "/" + carrierType
					       + "/" + cargoType);
			getTPFDDItem().setName(node.getUUID());
			getcarrierPopup().show(e.getComponent(), e.getX(), e.getY());
		    }
		    else if ( column == 4
			      && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0
			      && e.getClickCount() == 2 ) {
			ScrollInfoDialog dialog = new ScrollInfoDialog(node.toXML(true));
		    }
		}
	    };
	addMouseListener(rowAdapter);

	fireChangedRunnable = new Runnable()
	    {
		public void run() {
		    ((AbstractTableModel)getModel()).fireTableDataChanged();
		}
	    };
    }

    private JMenuItem getTPFDDItem()
    {
	if ( TPFDDItem == null ) {
	    try {
		TPFDDItem = new JMenuItem();
		TPFDDItem.addActionListener(this);
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return TPFDDItem;
    }

    private JMenuItem getTPFDDAndLogPlanItem()
    {
	if ( TPFDDAndLogPlanItem == null ) {
	    try {
		TPFDDAndLogPlanItem = new JMenuItem();
		TPFDDAndLogPlanItem.addActionListener(this);
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return TPFDDAndLogPlanItem;
    }

    private JMenuItem getfilterItem()
    {
	if ( filterItem == null ) {
	    try {
		filterItem = new JMenuItem();
		filterItem.setText("Select filter...");
		filterItem.setName("filterItem");
		filterItem.addActionListener(this);
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return filterItem;
    }

    private JPopupMenu getcarrierPopup()
    {
	if ( carrierPopup == null ) {
	    try {
		carrierPopup = new JPopupMenu();
		carrierPopup.add(getTPFDDItem());
		carrierPopup.add(getTPFDDAndLogPlanItem());
		carrierPopup.add(getfilterItem());
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return carrierPopup;
    }
	    

    private ClusterQuery getclusterQuery()
    {
	if ( clusterQuery == null ) {
	    try {
		clusterQuery = new ClusterQuery(parentProvider);
		clusterQuery.addActionListener(this);
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return clusterQuery;
    }

    private void handleException(Exception exception)
    {
	OutputHandler.out(ExceptionTools.toString("JTT:hE", exception));
    }

    public void actionPerformed(ActionEvent ae)
    {
	final ActionEvent e = ae;
	final Object source = e.getSource();
	final TaskNode node = (TaskNode)(parentProvider.warmRead(getTPFDDItem().getName()));
	final String unitName = node.getUnitName();

	Debug.out("JTT:aP command " + e.getActionCommand());
	if ( source == getfilterItem() ) {
	    getclusterQuery().getunitPanel().setUnitName(unitName);
	    getclusterQuery().setVisible(true);
	}
	else if ( source == getTPFDDAndLogPlanItem()
		  || source == getTPFDDItem()
		  || source == clusterQuery.getsendQueryButton() ) {
	    Runnable newViewRunnable = new Runnable()
		{
		    public void run()
		    {
			QueryData query;

			if ( source == getTPFDDItem() || source == getTPFDDAndLogPlanItem() ) {
			    query = new QueryData();
			    String[] unitNames = { node.getUnitName() };
			    query.setUnitNames(unitNames);
			    int sourceType = node.getSourceType();
			    
			    if ( sourceType == TaskNode.CARRIER_TYPE ) {
				String[] carrierTypes = { node.getCarrierType() };
				query.setCarrierTypes(carrierTypes);
				query.setCarrierIncluded(true);
			    }
			    else if ( sourceType == TaskNode.CARGO_TYPE ) {
				String[] cargoTypes = { node.getDirectObjectType() };
				query.setCargoTypes(cargoTypes);
				query.setCargoIncluded(true);
			    }
			    int nodeTypes[] = { TaskNode.ITINERARY, TaskNode.ITINERARY_LEG };
			    query.setNodeTypes(nodeTypes);
			    parentView.newView(true, source == getTPFDDAndLogPlanItem(), query, clusterCache, true);
			}
			else {
			    getclusterQuery().setVisible(false);
			    query = getclusterQuery().getLastQuery();
			    parentView.newView(true, false, query, clusterCache, query.isOwnWindow());
			}
		    }
		};
	    SwingQueue.invokeLater(newViewRunnable);
	}
	else
	    OutputHandler.out("VAS:aP Error: unknown bean source: " + source);

    }
    
    protected String getClassName(Object o)
    {
	String classString = o.getClass().getName();
	int dotIndex = classString.lastIndexOf(".");
	return classString.substring(dotIndex+1);
    }

    public TreeTableModel getTreeTableModel()
    {
	return currentModel;
    }

    public Runnable getFireChangedRunnable()
    {
	return fireChangedRunnable;
    }

    /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
     * paint the editor. The UI currently uses different techniques to 
     * paint the renderers and editors and overriding setBounds() below 
     * is not the right thing to do for an editor. Returning -1 for the 
     * editing row in this case, ensures the editor is never painted. 
     */
    public int getEditingRow()
    {
	return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;  
    }

    // 
    // The editor used to interact with tree nodes, a JTree.  
    //
    public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor
    {
	public Component getTableCellEditorComponent(JTable table, Object value,
						     boolean isSelected, int r, int c)
	{
	    return tree;
	}
    }
}
