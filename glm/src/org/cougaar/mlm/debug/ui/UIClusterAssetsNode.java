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
import org.cougaar.glm.ldm.asset.Organization;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

/** An object passed to UITreeNode (a dynamically expandable
  tree node) must extend the UITreeNode class and override these methods:
  isLeaf: true if it has no children;
  loadChildren: inserts children into tree;
  toString: for rendering itself
  */

public class UIClusterAssetsNode extends UITreeNode implements UISubscriber {
  private UIPlugin uiPlugin;
  private String planName;
  private boolean childrenLoaded = false;

  /** Creates a tree node for a plan by calling the UITreeNode constructor.
    @param uiPlugin this user interface plug in
    @param planName name of plan to display
    @param clusterId cluster from which to obtain plan
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIClusterAssetsNode(UIPlugin uiPlugin, String planName, 
			    MessageAddress clusterId) throws UINoPlanException {
    super();
    this.uiPlugin = uiPlugin;
    this.planName = planName;
    super.setUserObject(uiPlugin.getPlan(planName));
  }

  /** Not a leaf.
    @return false
   */

  public boolean isLeaf() {
    return false;
  }

  /** Get this cluster's cluster assets. */

  private static UnaryPredicate organizationPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return (o instanceof Organization);
      }
    };
  }

  /** Get the cluster assets for the plan.
   */

  public synchronized void loadChildren() {
    // new CCV2 method to obtain cluster assets
    if (!childrenLoaded) {
      uiPlugin.subscribe(this, organizationPredicate());
      childrenLoaded = true;
    }
  }

  /** Display the plan name in the tree.
    @return the plan name
   */

  public String toString() {
    return planName;
  }

  /** UISubscriber interface.
    Notified when a cluster asset is added or removed and
    update the tree.
    */

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    //System.out.println("Container changed");
    boolean reload = false;
    Enumeration added = container.getAddedList();
    Enumeration removed = container.getRemovedList();
    Enumeration changed = container.getChangedList();
    while (added.hasMoreElements()) {
      Organization organization = (Organization)added.nextElement();
      UIClusterAssetNode node = new UIClusterAssetNode(organization);
      if (node != null) {
	//System.out.println("Added: " + organization.toString() + " at " +
	//		   getChildCount());
	treeModel.insertNodeInto(node, this, getChildCount());
	reload = true;
      }
    }
    if (reload) treeModel.reload();
    while (removed.hasMoreElements()) {
      Organization organization = (Organization)removed.nextElement();
      //System.out.println("Removed: " + organization.toString());
      removeObjectFromTree(organization);
    }
    while (changed.hasMoreElements()) {
      Organization organization = (Organization)changed.nextElement();
      DefaultMutableTreeNode node = findUserObject(organization);
      if (node != null) {
	treeModel.removeNodeFromParent(node);
	treeModel.insertNodeInto(node, this, getChildCount());
      } else
	System.out.println("Warning: could not find changed node.");
    }
  }

}
