/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.util.UnaryPredicate;

/** An object passed to UITreeNode (a dynamically expandable
  tree node) must extend the UITreeNode class and override these methods:
  isLeaf: true if it has no children;
  loadChildren: inserts children into tree;
  toString: for rendering itself
  */

public class UIPlanDetailsNode extends UITreeNode implements UISubscriber {
  private UIPlugin uiPlugin;
  private String planName;
  private boolean childrenLoaded = false;

  /** Creates a tree node for a plan by calling the UITreeNode constructor.
    Listens on the plan elements collection (plan elements are the children
    nodes of the plan) to dynamically change the tree as the plan elements
    change.
    @param uiPlugin this user interface plug in
    @param planName name of plan to display
    @param clusterId cluster from which to obtain plan
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIPlanDetailsNode(UIPlugin uiPlugin, String planName, 
		    MessageAddress clusterId) throws UINoPlanException {
    super();
    this.uiPlugin = uiPlugin;
    this.planName = planName;
    super.setUserObject(uiPlugin.getPlan(planName)); 
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
      uiPlugin.subscribe(this, planElementPredicate());
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





