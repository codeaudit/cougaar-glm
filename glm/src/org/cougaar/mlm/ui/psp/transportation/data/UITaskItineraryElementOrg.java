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
 
package org.cougaar.mlm.ui.psp.transportation.data;

import java.util.*;

import org.cougaar.glm.ldm.asset.TransportationRoute;

/**
 * <code>UITaskItineraryElementOrg</code>. 
 * Tasks allocated to Organizations ("tree node" allocations
 * to cluster+task which can be further queried to eventually find
 * UITaskItineraryElementCarrier instances)
 */

public class UITaskItineraryElementOrg
    extends UITaskItineraryElement
    implements org.cougaar.core.util.SelfPrinter, java.io.Serializable, java.lang.Cloneable
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

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(allocatedClusterName, "AllocatedClusterName");
    pr.print(allocatedTaskID, "AllocatedTaskID");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 2907874570513185246L;

}
