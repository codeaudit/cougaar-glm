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

import java.util.Date;

public interface UIAllocationResult {
   
  /** The value of the allocation result.  The aspect types determine
    which of these are actually used; unused values are zero or null.
    @return  - the results for each aspect type
   */

  Date getStartTime();

  Date getEndTime();
  
  double getQuantity();

  double getInterval();

  double getTotalShipments();

  UITypedQuantityAspectValue[] getTypedQuantities();

  double getPOD();

  Date getPODDate();

  /** The confidence rating of this result.
      @return double - The confidence rating of this result. 
  */

  double getConfidenceRating();

  
  /** Represents whether or not the allocation was a success.
      @return success - true if allocation was a success
   */

  boolean isSuccess();

}

