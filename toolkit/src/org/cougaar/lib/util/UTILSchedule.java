/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.util;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ScheduleImpl;
import org.cougaar.domain.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.util.TimeSpan;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class UTILSchedule {

  public static boolean scheduleAvailable(Schedule sched, Date start, Date end) {
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

  public static Schedule andAvail(Schedule x, Schedule y, RootFactory ldmf) {
    //    System.out.println("X: "+x);
    //    System.out.println("Y: "+y);
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

  public static Schedule timeInverse(Schedule x, RootFactory ldmf) {
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

  public static void fill(Schedule x) {
    ScheduleElement y = (ScheduleElement)new ScheduleElementImpl(new Date(0), new Date(TimeSpan.MAX_VALUE));
    ((ScheduleImpl)x).setScheduleElement(y);

  }


}
