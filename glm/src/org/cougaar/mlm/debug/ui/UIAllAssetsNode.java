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
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.util.UnaryPredicate;

/** An object passed to UITreeNode (a dynamically expandable
  tree node) must extend the UITreeNode class and override these methods:
  isLeaf: true if it has no children;
  loadChildren: inserts children into tree;
  toString: for rendering itself
  */

public class UIAllAssetsNode extends UITreeNode implements UISubscriber {
  private UIPlugin uiPlugin;
  private String planName;
  private int treeIndex = 0;
  private boolean childrenLoaded = false;

  /** Creates a tree node for a plan by calling the UITreeNode constructor.
    @param uiPlugin this user interface plug in
    @param planName name of plan to display
    @param clusterId cluster from which to obtain plan
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIAllAssetsNode(UIPlugin uiPlugin, String planName, 
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

  /** Get this cluster's assets. */

  private static UnaryPredicate assetPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return ( o instanceof Asset );
      }
    };
  }

  /** Get all assets, properties and attributes.
   */

  public synchronized void loadChildren() {
    if (!childrenLoaded) {
      uiPlugin.subscribe(this, assetPredicate());
      childrenLoaded = true;
    }
  }

  /** UISubscriber interface.
    Notified when an asset is added or removed or changed;
    update the tree.
    */

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    boolean reload = false;
    Enumeration added = container.getAddedList();
    Enumeration removed = container.getRemovedList();
    Enumeration changed = container.getChangedList();
    while (added.hasMoreElements()) {
      Asset asset = (Asset)added.nextElement();
      if (asset instanceof AggregateAsset) {
	AggregateAsset aggregateAsset = (AggregateAsset)asset;
	asset = aggregateAsset.getAsset();
	if (asset == null)
	  continue;
      }
      UIAssetAttributeNode node = new UIAssetAttributeNode(asset);
      if (node != null) {
	//System.out.println("Added: " + asset.toString() + " at " +
	//		   getChildCount());
	treeModel.insertNodeInto(node, this, getChildCount());
	reload = true;
      }
    }
    if (reload) treeModel.reload();
    while (removed.hasMoreElements()) {
      Asset asset = (Asset)removed.nextElement();
      //System.out.println("Removed: " + asset.toString());
      removeObjectFromTree(asset);
    }
    while (changed.hasMoreElements()) {
      Asset asset = (Asset)changed.nextElement();
      DefaultMutableTreeNode node = findUserObject(asset);
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
