/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

import org.cougaar.domain.planning.ldm.plan.*;

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
   * @see org.cougaar.domain.planning.ldm.plan.Schedule
   * @see org.cougaar.domain.glm.plan.QuantityScheduleElement
   **/
  Schedule getQuantitySchedule();
   
  /**
   * Return the Schedule containing the RateScheduleElements
   * NOTE: this is a copy of the underlying Schedule!
   * @return Schedule
   * @see org.cougaar.domain.planning.ldm.plan.Schedule
   * @see org.cougaar.domain.glm.plan.RateScheduleElement
   **/
  Schedule getRateSchedule();
   
  /**
   * Replace the QtySchedule with a new one.
   * Note that this method completely removes any current QtySchedule and
   * its elements and replaces it with the Schedule passed in the argument.
   * @param aQuantitySchedule
   * @see org.cougaar.domain.planning.ldm.plan.Schedule
   * @see org.cougaar.domain.glm.plan.QuantityScheduleElement
   */
  void setQuantitySchedule(Schedule aQuantitySchedule);
   
  /**
   * Replace the RateSchedule with a new one.
   * Note that this method completely removes any current RateSchedule and
   * its elements and replaces it with the Schedule passed in the argument.
   * @param aRateSchedule
   * @see org.cougaar.domain.planning.ldm.plan.Schedule
   * @see org.cougaar.domain.glm.plan.RateScheduleElement
   */
  void setRateSchedule(Schedule aRateSchedule);
   
}
