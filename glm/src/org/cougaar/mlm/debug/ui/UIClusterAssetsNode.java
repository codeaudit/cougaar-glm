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
import org.cougaar.glm.ldm.asset.Organization;
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
