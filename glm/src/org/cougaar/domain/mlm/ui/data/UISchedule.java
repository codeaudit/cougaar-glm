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

public interface UISchedule
{
                
  /** Get an array of all of the schedule elements of this schedule
   * @return UIScheduleElement[] - schedule elements
   */
  UIScheduleElement[] getUIScheduleElements();
        
  /**
   * Return the start time of the first schedule element of the schedule.
   * For simple schedules, returns the start time from the single schedule element.
   @return long - start time
   **/
  long getStartTime();

  /**
   * Return the end time of the last schedule element of the schedule.
   * For simple schedules, returns the end time from the single schedule element.
   @return long - end 
   **/
  long getEndTime();
} 
