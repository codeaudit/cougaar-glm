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

import org.cougaar.domain.planning.ldm.plan.AllocationResult;

/** A tree node for a penalty value.
  */

public class UIPenaltyValueNode extends UITreeNode {
  //PenaltyValue penaltyValue;
	AllocationResult allocationresult;
  String prefix;

  /** Create a tree node for the PenaltyValue.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the PenaltyValue which has no children.
  @param penaltyValue penalty value for which to create tree node
   */

  public UIPenaltyValueNode(AllocationResult allocationresult, String prefix) {
    super(allocationresult);
    this.allocationresult = allocationresult;
    this.prefix = prefix;
  }

  /** The PenaltyValue is a leaf.
    @return true
   */
  public boolean isLeaf() {
    return true;
  }

  /** This shouldn't be called because this is a leaf.
   */

  public void loadChildren() {
    System.out.println("UIPenaltyValueNode:loadChildren called, but PenaltyValue is a leaf.");
  }

  /** Return representation of a PenaltyValue in a tree.
    @return PenaltyValue: numeric value
   */

  public String toString() {
    //EPD 2/10/99 WAS HERE
    // Move to using entire pv array - for now use to String which gets all defined
    // values in the array and prints them.  If all values in the array are undefined
    // to String returns "UNDEFINED"
    //return prefix + (penaltyValue.toString());
    String failure;
    if (allocationresult == null) {
      failure = "";
    } else if (allocationresult.isSuccess()) {
      failure = "success ";
    } else {
      failure = "failed ";
    }
    return prefix + failure + allocationresult;
  }
  
}
