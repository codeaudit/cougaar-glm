/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.util.Date;
import java.util.Enumeration;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;

public class UIAllocationResultImpl implements UIAllocationResult {
  AllocationResult allocationResult;

  public UIAllocationResultImpl(AllocationResult allocationResult) {
    this.allocationResult = allocationResult;
  }

  /** The value of the allocation result.  The aspect types determine
    which of these are actually used; unused values are -1 (which mirrors
    the society behavior) or null.
    @return  - the results for each aspect type
   */

  private double getResult(int desiredAspectType) {
    if (allocationResult.isDefined(desiredAspectType))
      return allocationResult.getValue(desiredAspectType);
    else
      return -1;
  }

  private Date getDateResult(int desiredAspectType) {
    if (allocationResult.isDefined(desiredAspectType))
      return new Date((long)getResult(desiredAspectType));
    else
      return null;
  }

  public Date getStartTime() {
    return getDateResult(AspectType.START_TIME);
  }

  public Date getEndTime() {
    return getDateResult(AspectType.END_TIME);
  }

  public double getQuantity() {
    return getResult(AspectType.QUANTITY);
  }

  public double getInterval() {
    return getResult(AspectType.INTERVAL);
  }

  public double getTotalShipments() {
    return getResult(AspectType.TOTAL_SHIPMENTS);
  }

  public UITypedQuantityAspectValue[] getTypedQuantities() {
    //    if (allocationResult.isDefined(AspectType.TYPED_QUANTITY))
    // awaiting definition of method on allocationResult
    // AspectValue getAspectValue(int aspectType)
    // in AllocationResult
    return null;
  }

  public UITypedQuantityAspectValue getTypedQuantity(int i) {
    return null;
  }

  public double getPOD() {
    // should return new UILocation(allocationResult.getAspectValue(AspectType.POD));
    return getResult(AspectType.POD);
  }

  public Date getPODDate() {
    return getDateResult(AspectType.POD_DATE);
  }

  /** The confidence rating of this result.
      @return double - The confidence rating of this result. 
  */
   
  public double getConfidenceRating() {
    return allocationResult.getConfidenceRating();
  }
  
  /** Represents whether or not the allocation was a success.
      @return success - true if allocation was a success
   */

  public boolean isSuccess() {
    return allocationResult.isSuccess();
  }

}

