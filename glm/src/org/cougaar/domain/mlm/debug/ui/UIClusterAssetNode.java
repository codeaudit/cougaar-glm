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


