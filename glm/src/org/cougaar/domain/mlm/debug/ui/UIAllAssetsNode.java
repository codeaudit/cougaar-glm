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

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

/** An object passed to UITreeNode (a dynamically expandable
  tree node) must extend the UITreeNode class and override these methods:
  isLeaf: true if it has no children;
  loadChildren: inserts children into tree;
  toString: for rendering itself
  */

public class UIAllAssetsNode extends UITreeNode implements UISubscriber {
  private UIPlugIn uiPlugIn;
  private String planName;
  private int treeIndex = 0;
  private boolean childrenLoaded = false;

  /** Creates a tree node for a plan by calling the UITreeNode constructor.
    @param uiPlugIn this user interface plug in
    @param planName name of plan to display
    @param clusterId cluster from which to obtain plan
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIAllAssetsNode(UIPlugIn uiPlugIn, String planName, 
			 ClusterIdentifier clusterId) throws UINoPlanException {
    super();
    this.uiPlugIn = uiPlugIn;
    this.planName = planName;
    super.setUserObject(uiPlugIn.getPlan(planName));
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
      uiPlugIn.subscribe(this, assetPredicate());
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
