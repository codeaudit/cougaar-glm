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

package org.cougaar.mlm.ui.data;

import java.util.*;

import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;

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
