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

import org.cougaar.planning.ldm.asset.Asset;

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


