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

  double getReadiness();

  /** The confidence rating of this result.
      @return double - The confidence rating of this result. 
  */

  double getConfidenceRating();

  
  /** Represents whether or not the allocation was a success.
      @return success - true if allocation was a success
   */

  boolean isSuccess();

}

