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

package org.cougaar.lib.util;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.util.TimeSpan;

public class UTILSchedule {

  public boolean scheduleAvailable(Schedule sched, Date start, Date end) {
    Enumeration x = sched.getAllScheduleElements();
    Date target = start;
    ScheduleElement se;
    while (x.hasMoreElements()) {
      se = (ScheduleElement)x.nextElement();
      if (target.after(se.getStartDate()) || target.equals(se.getStartDate())) {
	target = se.getEndDate();
      } else return false;
    }
    if (target.after(end) || target.equals(end)) return true;
    else return false;
  }

  public Schedule andAvail(Schedule x, Schedule y, PlanningFactory ldmf) {
    //    logger.debug("X: "+x);
    //    logger.debug("Y: "+y);
    Vector newElem = new Vector();
    Enumeration a = x.getAllScheduleElements();
    Enumeration b = y.getAllScheduleElements();
    ScheduleElement m = null;
    ScheduleElement n = null;
    while (a.hasMoreElements() || b.hasMoreElements()) {
      if (m == null) m = (ScheduleElement)a.nextElement();
      if (n == null) n = (ScheduleElement)b.nextElement();
      Date latestStart;
      Date earliestEnd;
      if (m.getStartDate().before(n.getStartDate())) latestStart = n.getStartDate(); else latestStart = m.getStartDate();
      if (m.getEndDate().before(n.getEndDate())) earliestEnd = m.getEndDate(); else earliestEnd = n.getEndDate();
      if (latestStart.before(earliestEnd)) {
	newElem.addElement((ScheduleElement)new ScheduleElementImpl(latestStart,earliestEnd));
      }
      if (a.hasMoreElements() && m.getEndDate().before(n.getEndDate())) m = (ScheduleElement)a.nextElement();
      else if (b.hasMoreElements() && n.getEndDate().before(m.getEndDate())) n = (ScheduleElement)b.nextElement();      
    }
    return ldmf.newSchedule(newElem.elements());
  }

  public Schedule timeInverse(Schedule x, PlanningFactory ldmf) {
    Vector newElem = new Vector();
    Date point = new Date(0);
    Enumeration enum = x.getAllScheduleElements();
    // As I understand it this is getting a enum from an ordered set and is therefore ordered
    while (enum.hasMoreElements()) {
      ScheduleElement se = (ScheduleElement)enum.nextElement();
      if (point.before(se.getStartDate())) {
	newElem.addElement((ScheduleElement)new ScheduleElementImpl(point,se.getStartDate()));
      }
      point = se.getEndDate();
    }
    if (!point.equals(new Date(TimeSpan.MAX_VALUE))) {
      newElem.addElement((ScheduleElement)new ScheduleElementImpl(point,new Date(TimeSpan.MAX_VALUE)));
    }
    if (newElem.size() == 0) 
      return x;
    else
      return ldmf.newSchedule(newElem.elements());
  }

  public void fill(Schedule x) {
    ScheduleElement y = (ScheduleElement)new ScheduleElementImpl(new Date(0), new Date(TimeSpan.MAX_VALUE));
    ((ScheduleImpl)x).setScheduleElement(y);

  }


}
