/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.debug.ui;

import org.cougaar.domain.planning.ldm.asset.Asset;

import java.util.Enumeration;

/** A tree node for an asset.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the asset which has no children.
  */

public class UIAssetNode extends UITreeNode {
  Asset asset;
  String prefix;

  /** Create a tree node for the Asset.
    @param asset the asset for which to create a tree node
   */

  public UIAssetNode(Asset asset, String prefix) {
    super(asset);
    this.asset = asset;
    this.prefix = prefix;
  }

  /** The Asset is a leaf.
    @return true
   */
  public boolean isLeaf() {
    return true;
  }

  /** Prints an error message inidicating this
    shouldn't be called because this is a leaf.
   */

  public void loadChildren() {
    System.out.println("UIAssetNode:loadChildren called, but Asset is a leaf.");
  }

  /** Return the representation of a Asset in a tree.
    This is the asset name, and 
    for physical objects, the serial number, if specified, 
    and the capabilities.
    @return asset name followed by serial number (optional) and capabilities
   */

  public String toString() {
    return prefix + UIAsset.getDescription(asset);
  }

}


