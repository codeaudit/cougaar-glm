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

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;

/** A tree node for a Cluster Asset.
  Overrides the UITreeNode loadChildren, toString and isLeaf.
  */

public class UIClusterAssetNode extends UITreeNode {
  Organization org;

  /** Create a tree node for the cluster asset.
    @param ClusterAsset clusterAsset for which to create the node
   */
  public UIClusterAssetNode(Organization org) {
    super(org);
    this.org = org;
  }

  /** This is a leaf.
    @return true
   */
  public boolean isLeaf() {
    return true;
  }

  /** This shouldn't be called because this is a leaf.
   */

  public void loadChildren() {
    System.out.println("Warning: UIClusterAssetNode:loadChildren called, but this is a leaf.");
  }

  /** Return representation of a cluster asset in a tree.
    @return String s
   */

  public String toString() {
    String s = "";

    ItemIdentificationPG itemIdProp =  org.getItemIdentificationPG();
    if (itemIdProp != null)
      s = itemIdProp.getAlternateItemIdentification();
    
    return s + " " + org.getRelationshipSchedule().toString();
  }

}


