/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.util.Vector;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.PlanElement;

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

