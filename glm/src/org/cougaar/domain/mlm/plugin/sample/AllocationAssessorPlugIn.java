/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.sample;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.text.NumberFormat;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.Alert;
import org.cougaar.domain.planning.ldm.plan.NewAlert;
import org.cougaar.domain.planning.ldm.plan.NewAlertParameter;
import org.cougaar.domain.planning.ldm.plan.AlertParameter;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.PlugInAdapter;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.domain.mlm.plugin.UICoordinator;

/**
 * The AllocationsAssessorPlugIn publishes an Alert for each
 * failed allocations in it's collection
 *
 * @author       ALPINE <alpine-software@bbn.com>
 * @version      $Id: AllocationAssessorPlugIn.java,v 1.1 2000-12-15 20:17:45 mthome Exp $
 */

public class AllocationAssessorPlugIn extends SimplePlugIn
{
  private static final boolean BRIEF = false; // True for task id only

  /** frame displaying messages **/
  private JFrame frame;

  static NumberFormat confidenceFormat = NumberFormat.getPercentInstance();

  /** Display Allocation Results  **/
  JTextArea failedTaskIds = new JTextArea(6, 30);
  JLabel allocCountLabel = new JLabel("   0 failed allocations");
  JScrollPane failedTaskPane = new JScrollPane(failedTaskIds,
					       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  
  /** Display Allocation Results  **/
  JTextArea succeededTaskIds = new JTextArea(6, 30);
  JLabel succeededCountLabel = new JLabel("   0 successful allocations");
  JScrollPane succeededTaskPane = new JScrollPane(succeededTaskIds,
						  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  
  /** Subscription to hold collection of input tasks **/
  private IncrementalSubscription myalloc;

  /** Keep track of Allocations that come through.  **/
  private HashMap allocationAlerts = new HashMap();

  private HashSet allocationSuccesses = new HashSet();

  /** Look for failed allocations **/
  private static UnaryPredicate allocPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Allocation);
      }
    };
  }

  /** GUI to display failed allocation info **/
  private void createGUI() {
    frame = new JFrame("AllocationAssessorPlugIn for " + getClusterIdentifier());
    Container panel = frame.getContentPane();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(allocCountLabel, gbc);
    gbc.gridy = 2;
    panel.add(succeededCountLabel, gbc);
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1.0;
    panel.add(failedTaskPane, gbc);
    gbc.gridy = 3;
    panel.add(succeededTaskPane, gbc);
    frame.pack();
    UICoordinator.setBounds(frame);
    frame.setVisible(true);
  }

  /**
   * Overrides the setupSubscriptions() in the SimplePlugIn.
   */
  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    myalloc = (IncrementalSubscription)subscribe(allocPred());
    createGUI();
  }

  private String getAllocationString(Allocation alloc) {
    String confidence = "";
    AllocationResult ar = alloc.getReportedResult();
    if (ar == null) {
      confidence = "<no result>" ;
    } else {
      confidence = confidenceFormat.format(ar.getConfidenceRating()) + " ";
    }
    if (BRIEF) {
      return confidence + alloc.getTask().getUID().toString();
    } else {
      return confidence + alloc.getTask().toString();
    }
  }

  private void updateGUI() {
    allocCountLabel.setText(allocationAlerts.size() + " failed allocations");
    TreeSet names = new TreeSet();
    for (Iterator iter = allocationAlerts.keySet().iterator(); iter.hasNext(); ) {
      names.add(getAllocationString((Allocation) iter.next()));
    }
    StringBuffer buf = new StringBuffer();
    for (Iterator iter = names.iterator(); iter.hasNext(); ) {
      buf.append(iter.next());
      buf.append("\n");
    }
    String newText = buf.substring(0);
    if (!failedTaskIds.equals(newText)) {
      failedTaskIds.setText(newText);
    }
    succeededCountLabel.setText(allocationSuccesses.size() + " successful allocations");
    names.clear();
    for (Iterator iter = allocationSuccesses.iterator(); iter.hasNext(); ) {
      names.add(getAllocationString((Allocation) iter.next()));
    }
    buf.setLength(0);
    for (Iterator iter = names.iterator(); iter.hasNext(); ) {
      buf.append(iter.next());
      buf.append("\n");
    }
    newText = buf.substring(0);
    if (!succeededTaskIds.getText().equals(newText)) {
      succeededTaskIds.setText(newText);
    }
  }

  /** Creates and publishes alert for failed allocations **/
  public void createAlert(Allocation alloc){
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

  private void addFailedDisposition(Allocation alloc) {
    if (allocationAlerts.containsKey(alloc)) return;
    createAlert(alloc);
  }

  private void removeFailedDisposition(Allocation alloc) {
    if (!allocationAlerts.containsKey(alloc)) return;
    removeAlert(alloc);
  }

  private void addSucceededAllocation(Allocation alloc) {
    if (allocationSuccesses.contains(alloc)) return;
    allocationSuccesses.add(alloc);
  }

  private void removeSucceededAllocation(Allocation alloc) {
    if (!allocationSuccesses.contains(alloc)) return;
    allocationSuccesses.remove(alloc);
  }

  private void removeAllocation(Allocation alloc) {
    removeFailedDisposition(alloc);
    removeSucceededAllocation(alloc);
  }

  private boolean checkAllocations(Enumeration allocations, boolean addOk) {
    boolean doUpdate = false;
    while (allocations.hasMoreElements()) {
      Allocation alloc = (Allocation) allocations.nextElement();
      if (!addOk) {
	if (!allocationAlerts.containsKey(alloc) &&
	    !allocationSuccesses.contains(alloc)) {
	  continue;		// Probably rescinded
	}
      }
      AllocationResult ar = alloc.getReportedResult();
      if (ar == null || ar.isSuccess()) {
	removeFailedDisposition(alloc);
	addSucceededAllocation(alloc);
      } else {
	removeSucceededAllocation(alloc);
	addFailedDisposition(alloc);
      }
      doUpdate = true;
    }
    return doUpdate;
  }

  private boolean removeAllocations(Enumeration allocations) {
    boolean doUpdate = false;
    while (allocations.hasMoreElements()) {
      Allocation alloc = (Allocation) allocations.nextElement();
      removeAllocation(alloc);
      doUpdate = true;
    }
    return doUpdate;
  }

  /* CCV2 execute method */
  /* This will be called every time a alloc matches the above predicate */
  /* Note: Failed Allocations only come through on the changed list. 
     Since Allocations are changed by other PlugIns after we see them
     here, we need to keep track of the ones we've seen so we don't 
     act on them more than once.
   */
  public synchronized void execute() {
    boolean doUpdate = false;
    doUpdate |= checkAllocations(myalloc.getAddedList(), true);
    doUpdate |= checkAllocations(myalloc.getChangedList(), false);
    doUpdate |= removeAllocations(myalloc.getRemovedList());
    if (doUpdate) updateGUI();
  }
}
