/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

import java.util.*;

import org.cougaar.domain.glm.asset.TransportationRoute;

/**
 * <code>UITaskItineraryElementOrg</code>. 
 * Tasks allocated to Organizations ("tree node" allocations
 * to cluster+task which can be further queried to eventually find
 * UITaskItineraryElementCarrier instances)
 */

public class UITaskItineraryElementOrg
    extends UITaskItineraryElement
    implements org.cougaar.util.SelfPrinter, java.io.Serializable, java.lang.Cloneable
{

  public UITaskItineraryElementOrg() { super(); }

  public UITaskItineraryElement copy() {
    try {
      return (UITaskItineraryElement)super.clone();
    } catch (java.lang.CloneNotSupportedException noClone) {
      throw new RuntimeException("NEVER! "+noClone);
    }
  }

  //#############################################################
  // START ATTRIBUTES
  // USE ACCESSORS (SEE NEXT SECTION)

  private String allocatedClusterName;
  private String allocatedTaskID;

  // END ATTRIBUTES
  //############################################################

  //#############################################################
  // START ACCESSORS

  public String getAllocatedClusterName() { return allocatedClusterName; }
  public void setAllocatedClusterName(String s) {
    allocatedClusterName = s;
  }

  public String getAllocatedTaskID() { return allocatedTaskID; }
  public void setAllocatedTaskID(String s) {
    allocatedTaskID = s;
  }

  //END ACCESSORS
  //############################################################

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(allocatedClusterName, "AllocatedClusterName");
    pr.print(allocatedTaskID, "AllocatedTaskID");
  }

  public String toString() {
    return org.cougaar.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 2907874570513185246L;

}
