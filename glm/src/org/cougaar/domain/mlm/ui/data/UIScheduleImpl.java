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

import java.util.*;

import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;

public class UIScheduleImpl implements UISchedule {
  Schedule schedule;

  public UIScheduleImpl(Schedule schedule) {
    this.schedule = schedule;
  }

  /** Get an array of all of the schedule elements of this schedule
   * @return UIScheduleElement[] - schedule elements
   */
  public UIScheduleElement[] getUIScheduleElements() {
    Enumeration scheduleElements = schedule.getAllScheduleElements();
    ArrayList tmp = new ArrayList();
    while (scheduleElements.hasMoreElements())
      tmp.add(new UIScheduleElementImpl((ScheduleElement)scheduleElements.nextElement()));
    UIScheduleElement[] UIS = new UIScheduleElement[tmp.size()];
    return (UIScheduleElement[])tmp.toArray(UIS);
  }

  public UIScheduleElement getUIScheduleElement(int i) {
    UIScheduleElement[] UIS = getUIScheduleElements();
    if (i < UIS.length)
      return UIS[i];
    else
      return null;
  }
        
  /**
   * Return the start time of the first schedule element of the schedule.
   * For simple schedules, returns the start time from the single schedule element.
   @return long - start time
   **/
  public long getStartTime() {
    return schedule.getStartTime();
  }

  /**
   * Return the end time of the last schedule element of the schedule.
   * For simple schedules, returns the end time from the single schedule element.
   @return long - end time
   **/
  public long getEndTime() {
    return schedule.getEndTime();
  }
} 
