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

import org.cougaar.planning.ldm.asset.Asset;

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


