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

public interface UIQuantitySchedule
{
                
  /**
   * Return the start time from a schedule element.
   @return long - start time
   **/
  long getStartTime();

  /**
   * Return the end time from a schedule element.
   @return long - end time
   **/
  long getEndTime();

  /**
   * Return the quantity from a schedule element.
   @return double - quantity
   **/

  double getQuantity();
} 
