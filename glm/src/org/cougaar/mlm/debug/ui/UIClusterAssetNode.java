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

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;

/** A tree node for a Cluster Asset.
  Overrides the UITreeNode loadChildren, toString and isLeaf.
  */

public class UIClusterAssetNode extends UITreeNode {
  Organization org;

  /** Create a tree node for the cluster asset.
    @param org for which to create the node
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


