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

package org.cougaar.glm.ldm.plan;

import org.cougaar.planning.ldm.plan.*;

/**
 * A LaborSchedule is a special class of Schedule that actually contains 2
 * Schedule objects.  For now, these schedule objects should be a schedule
 * containing QuantityScheduleElements and a schedule containing
 * RateScheduleElements.  This interface extends the Schedule interface.
 * This class may become more generic after the 99 demo.
 **/
 
public interface LaborSchedule extends Schedule {
   
  /** 
   * Return the Schedule containing the QuantityScheduleElements
   * NOTE: this is a copy of the underlying Schedule!
   * @return Schedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.QuantityScheduleElement
   **/
  Schedule getQuantitySchedule();
   
  /**
   * Return the Schedule containing the RateScheduleElements
   * NOTE: this is a copy of the underlying Schedule!
   * @return Schedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.RateScheduleElement
   **/
  Schedule getRateSchedule();
   
  /**
   * Replace the QtySchedule with a new one.
   * Note that this method completely removes any current QtySchedule and
   * its elements and replaces it with the Schedule passed in the argument.
   * @param aQuantitySchedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.QuantityScheduleElement
   */
  void setQuantitySchedule(Schedule aQuantitySchedule);
   
  /**
   * Replace the RateSchedule with a new one.
   * Note that this method completely removes any current RateSchedule and
   * its elements and replaces it with the Schedule passed in the argument.
   * @param aRateSchedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.RateScheduleElement
   */
  void setRateSchedule(Schedule aRateSchedule);
   
}