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

package org.cougaar.mlm.plugin.sample;

import javax.swing.*;
import javax.swing.table.*;
import java.text.NumberFormat;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Comparator;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.StateModelException;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Alert;
import org.cougaar.planning.ldm.plan.NewAlert;
import org.cougaar.planning.ldm.plan.NewAlertParameter;
import org.cougaar.planning.ldm.plan.AlertParameter;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.plugin.PluginAdapter;
import org.cougaar.util.UnaryPredicate;

/**
 * The AllocationsAssessorPlugin publishes an Alert for each
 * failed allocations in it's collection
 *
 * @author       ALPINE <alpine-software@bbn.com>
 *
 */

public class AlternateAllocationAssessorPlugin extends SimplePlugin
{
  /** frame displaying messages **/
  static JFrame frame;

  Comparator taskComparator = new Comparator() {
    public int compare(Object l, Object r) {
      Allocation la = (Allocation) l;
      Allocation ra = (Allocation) r;
      return la.getTask().getUID().compareTo(ra.getTask().getUID());
    }
  };

  private class MyTreeSet extends TreeSet {
    public MyTreeSet(Comparator comparator) {
      super(comparator);
    }

    public synchronized boolean add(Object o) {
      allocationArray = null;
      return super.add(o);
    }
    public synchronized boolean remove(Object o) {
      allocationArray = null;
      return super.remove(o);
    }
  }

  private Allocation[] allocationArray = null;

  private MyTreeSet allocations = new MyTreeSet(taskComparator);
  
  private Allocation[] getAllocationArray() {
    if (allocationArray == null) {
      allocationArray = (Allocation[]) allocations.toArray(new Allocation[allocations.size()]);
    }
    return allocationArray;
  }

  private NumberFormat confidenceFormat = NumberFormat.getPercentInstance();

  private class MyTableModel extends AbstractTableModel {
    public int getRowCount() {
      return allocations.size();
    }

    public int getColumnCount() {
      return 3;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0: return "Task ID";
      case 1: return "Status";
      case 2: return "Confidence";
      }
      return null;
    }

    public Object getValueAt(int row, int column) {
      synchronized (allocations) {
	Allocation[] ary = getAllocationArray();
	if (row >= ary.length) return null;
	Allocation a = ary[row];
	AllocationResult ar = a.getEstimatedResult();
	switch (column) {
	case 0:			// The task id
	  return a.getTask().getUID();
	case 1:			// The failed status
	  return ar.isSuccess() ? "Succeeded" : "Failed";
	case 2:			// The confidence rating
	  return confidenceFormat.format(ar.getConfidenceRating());
	}
	return "???";
      }
    }
  }

  /** Display Allocation Results  **/
  MyTableModel tableModel = new MyTableModel();
  JTable allocationList = new JTable(tableModel);
  JLabel allocCountLabel = new JLabel("   0 failed allocations");
  JScrollPane scrollPane = new JScrollPane(allocationList,
					   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  
  /** Subscription to hold collection of input tasks **/
  private IncrementalSubscription myalloc;

  /** Keep track of Allocations that come through.  **/
  private HashMap allocationAlerts = new HashMap();

  /** Look for allocations **/
  private static UnaryPredicate allocPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Allocation);
      }
    };
  }

  /** GUI to display failed allocation info **/
  private void createGUI() {
    frame = new JFrame("AlternateAllocationAssessorPlugin for " + getClusterIdentifier());
    frame.setLocation(0,160);
    Container panel = frame.getContentPane();
    panel.add(allocCountLabel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Overrides the setupSubscriptions() in the SimplePlugin.
   */
  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    myalloc = (IncrementalSubscription)subscribe(allocPred());
    createGUI();
  }

  private void updateGUI() {
    allocCountLabel.setText(allocationAlerts.size() + " failed allocations");
  }

  /** Creates and publishes alert for failed allocations **/
  public void createAlert(Allocation alloc){
    if (allocationAlerts.containsKey(alloc)) return;
    NewAlert alert = theLDMF.newAlert();
    NewAlertParameter []params = new NewAlertParameter[1];
    params[0] = theLDMF.newAlertParameter();
    params[0].setParameter(alloc);
    params[0].setDescription("Failed Allocation");
    alert.setAlertText("Allocation Failed for :" + alloc.toString());
    alert.setAlertParameters(params);
    publishAdd(alert);
    allocationAlerts.put(alloc, alert);
  }

  public void removeAlert(Allocation alloc) {
    Alert alert = (Alert) allocationAlerts.get(alloc);
    if (alert == null) return;	// Shouldn't happen
    publishRemove(alert);
    allocationAlerts.remove(alloc);
  }

  private void checkAllocations(Enumeration enum) {
    while (enum.hasMoreElements()) {
      Allocation alloc = (Allocation) enum.nextElement();
      AllocationResult ar = alloc.getEstimatedResult();
      if (ar != null) {
	if (!allocations.contains(alloc)) {
	  allocations.add(alloc);
	  tableModel.fireTableDataChanged();
	} else {
	  tableModel.fireTableDataChanged();
	}
	if (ar.isSuccess()) {
	  removeAlert(alloc);
	} else {
	  createAlert(alloc);
	}
      } else {
	removeAllocation(alloc);
      }
    }
  }

  private void removeAllocation(Allocation alloc) {
    removeAlert(alloc);
    if (allocations.contains(alloc)) {
      allocations.remove(alloc);
      tableModel.fireTableDataChanged();
    }
  }

  /* CCV2 execute method */
  /* This will be called every time a alloc matches the above predicate */
  /* Note: Failed Allocations only come through on the changed list. 
     Since Allocations are changed by other Plugins after we see them
     here, we need to keep track of the ones we've seen so we don't 
     act on them more than once.
   */
  public synchronized void execute() {
    checkAllocations(myalloc.getAddedList());
    checkAllocations(myalloc.getChangedList());
    Enumeration e = myalloc.getRemovedList();
    while (e.hasMoreElements()) {
      removeAllocation((Allocation) e.nextElement());
    }
  }
}
