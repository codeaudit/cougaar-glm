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

import org.cougaar.planning.ldm.plan.AllocationResult;

/** A tree node for a penalty value.
  */
public class UIPenaltyValueNode extends UITreeNode {
  //PenaltyValue penaltyValue;
  AllocationResult allocationresult;
  String prefix;

  /** Create a tree node for the PenaltyValue.
   * Overrides the UITreeNode loadChildren, toString and isLeaf
   * methods to dynamically display the PenaltyValue which has no children.
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
