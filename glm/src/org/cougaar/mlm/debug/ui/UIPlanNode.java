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

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

/** An object passed to UITreeNode (a dynamically expandable
  tree node) must extend the UITreeNode class and override these methods:
  isLeaf: true if it has no children;
  loadChildren: inserts children into tree;
  toString: for rendering itself
  */

public class UIPlanNode extends UITreeNode implements UISubscriber {
  private UIPlugIn uiPlugIn;
  private String planName;
  private boolean childrenLoaded = false;

  /** Creates a tree node for a plan by calling the UITreeNode constructor.
    Listens on the plan elements collection (plan elements are the children
    nodes of the plan) to dynamically change the tree as the plan elements
    change.
    @param uiPlugIn this user interface plug in
    @param planName name of plan to display
    @param clusterId cluster from which to obtain plan
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIPlanNode(UIPlugIn uiPlugIn, String planName, 
		    ClusterIdentifier clusterId) throws UINoPlanException {
    super();
    this.uiPlugIn = uiPlugIn;
    this.planName = planName;
    super.setUserObject(uiPlugIn.getPlan(planName)); 
  }

  /** The plan is never a leaf.
    @return false
   */

  public boolean isLeaf() {
    return false;
  }

  /** Get this cluster's plan elements. */

  private static UnaryPredicate planElementPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return ( o instanceof PlanElement );
      }
    };
  }

  /** Get the plan elements (children of the plan node) for this plan.
    Don't do this at node creation time, because the tree model
    that is used in subscriptionChanged won't be set.
   */

  public void loadChildren() {
    if (!childrenLoaded) {
      uiPlugIn.subscribe(this, planElementPredicate());
      childrenLoaded = true;
    }
  }

  /** UISubscriber interface.
    Notified when a plan element is added or removed or changed;
    update the tree.
    */

  public void subscriptionChanged(IncrementalSubscription container) {
    boolean reload = false;
    Enumeration added = container.getAddedList();
    Enumeration removed = container.getRemovedList();
    Enumeration changed = container.getChangedList();
    while (added.hasMoreElements()) {
      PlanElement planElement = (PlanElement)added.nextElement();
      UIPlanElementNode node = new UIPlanElementNode(planElement);
      if (node != null) {
	treeModel.insertNodeInto(node, this, getChildCount());
	reload = true;
      }
    }
    if (reload) treeModel.reload();
    while (removed.hasMoreElements()) {
      PlanElement planElement = (PlanElement)removed.nextElement();
      removeObjectFromTree(planElement);
    }
    // if a plan element changed, remove the node and re-add it
    // and redisplay the tree, as we don't know 
    // what in the plan element changed
    while (changed.hasMoreElements()) {
      PlanElement planElement = (PlanElement)changed.nextElement();
      DefaultMutableTreeNode node = findUserObject(planElement);
      if (node != null) {
	treeModel.removeNodeFromParent(node);
	treeModel.insertNodeInto(node, this, getChildCount());
      } else
	System.out.println("Warning: could not find changed node.");
    }
  }

  /** Display the plan name in the tree.
    @return the plan name
   */

  public String toString() {
    return planName;
  }


}





