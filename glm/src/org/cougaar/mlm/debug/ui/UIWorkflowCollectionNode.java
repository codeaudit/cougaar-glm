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


package org.cougaar.mlm.debug.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

/** A tree for an allocatable workflow collection.
  */

public class UIWorkflowCollectionNode extends UITreeNode implements UISubscriber {
  private UIPlugin uiPlugin;
  private String planName;
  private boolean childrenLoaded = false;

  /** Creates a tree for a workflow collection in the specified cluster
    and for the specified plan by calling the UITreeNode constructor.
    @param uiPlugin this user interface plug in
    @param planName name of plan for which to display workflows
    @param clusterId cluster from which to obtain workflows
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIWorkflowCollectionNode(UIPlugin uiPlugin, String planName,
		  MessageAddress clusterId) throws UINoPlanException {
    super();
    this.uiPlugin = uiPlugin;
    this.planName = planName;
    super.setUserObject(uiPlugin.getPlan(planName));
  }

  /** Has leaves which are the Workflows.
    @return false
   */

  public boolean isLeaf() {
    return false;
  }

  /** Get the Workflows.
   */

  private static UnaryPredicate workflowPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return (o instanceof Expansion);
      }
    };
  }

  public synchronized void loadChildren() {
    if (!childrenLoaded) {
      uiPlugin.subscribe(this, workflowPredicate());
      childrenLoaded = true;
    }
  }

  /** Display the plan name in the tree.
    @return plan name
   */

  public String toString() {
    return planName;
  }

  /** UISubscriber interface.
    Actually returns dispositions which are expansions.
    Only pay attention to the added list.
    */

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    //System.out.println("Container changed");
    boolean reload = false;
    Enumeration added = container.getAddedList();
    while (added.hasMoreElements()) {
      Expansion expansion = (Expansion)added.nextElement();
      UIWorkflowNode node = new UIWorkflowNode(expansion.getWorkflow());
      if (node != null) {
	//System.out.println("Added: " + workflow.toString() + " at " +
	//		   getChildCount());
	treeModel.insertNodeInto(node, this, getChildCount());
	reload = true;
      }
    }
    if (reload) treeModel.reload();
  }

}

