/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

/** An object passed to UITreeNode (a dynamically expandable
  tree node) must extend the UITreeNode class and override these methods:
  isLeaf: true if it has no children;
  loadChildren: inserts children into tree;
  toString: for rendering itself
  */

public class UIPlanDetailsNode extends UITreeNode implements UISubscriber {
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

  public UIPlanDetailsNode(UIPlugIn uiPlugIn, String planName, 
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
      UIObjectNode node = new UIObjectNode(planElement, "");
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





