/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.ui.data;

import java.util.Vector;

import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class UIPlanElementImpl implements UIPlanElement, XMLUIPlanObject {
  PlanElement planElement;
  UUID planElementUUID;

  public UIPlanElementImpl(PlanElement planElement) {
    this.planElement = planElement;
    planElementUUID = new UUID(planElement.getUID().toString());
  }

  /** Get the name of the plan containing this plan element.
      @return String - the name of the Plan of this plan element.
   **/
  public String getPlanName() {
    return planElement.getPlan().getPlanName();
  }

  /** This returns the Task of the PlanElement. 
   * @return UUID - the uuid of the task associated with this plan elemenet
   **/
  
  public UUID getTask() {
    return new UUID(planElement.getTask().getUID().toString());
  }
  
  /** Returns the estimated allocation result that is related to performing
    * the Task.
    * @return UIAllocationResult - estimated allocation result
    **/

  public UIAllocationResult getEstimatedResult() {
    AllocationResult ar = planElement.getEstimatedResult();
    if (ar != null)
      return new UIAllocationResultImpl(ar);
    else
      return null;
  }

  /** Returns the reported allocation result.
    * @return UIAllocationResult - reported allocation result
    **/
  public UIAllocationResult getReportedResult() {
    AllocationResult ar = planElement.getReportedResult();
    if (ar != null)
      return new UIAllocationResultImpl(ar);
    else
      return null;
  }

  public UUID getUUID() {
    return planElementUUID;
  }
   
  // 
  // XMLPlanObject method for UI, other clients
  //
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }
}

