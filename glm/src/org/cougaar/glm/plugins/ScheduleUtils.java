/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBNT Solutions LLC,
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 2000 by
 *             BBNT Solutions LLC,
 *             all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleUtilities;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.core.plugin.PluginDelegate;

import java.util.*;

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.asset.NewScheduledContentPG;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;
import org.cougaar.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.ldm.plan.PlanScheduleType;
import org.cougaar.glm.debug.GLMDebug;

/** Provide convenience methods for creating objects. */
public class ScheduleUtils {

    protected static final long MSEC_PER_DAY =  86400000;

    public static Vector convertEnumToVector(Enumeration e) {
	Vector v = new Vector();
	while (e.hasMoreElements()) {
	    v.addElement(e.nextElement());
	}
	return v;
    }


    public static ScheduledContentPG createScheduledContentPG( Asset a, Schedule s)
    {
	NewScheduledContentPG scp = PropertyGroupFactory.newScheduledContentPG();
	scp.setAsset(a);
	scp.setSchedule(s);
	return scp;
    }

    // truncates time span
    // note amount can be positive or negative.
    public static Schedule adjustSchedule(Schedule sched, long start, long end, int amount, long time_incr)
    {
	return adjustSchedule(sched, truncTime(start, time_incr), 
			      truncTime(end+1, time_incr)-1, amount);
    }

    // note amount can be positive or negative.
    public static Schedule adjustSchedule(Schedule sched, long start, long end, int amount)
    {
	Schedule simple_sched = buildSimpleQuantitySchedule(amount, start, end);
	return ScheduleUtilities.addSchedules(sched, simple_sched);
    }

    public static Schedule buildSimpleQuantitySchedule(double qty, 
						       long start, long end) {
	QuantityScheduleElement nqse = buildQuantityScheduleElement(qty, 
								    start, 
								    end);
	Vector sched_el = new Vector();
	sched_el.addElement(nqse);

	return GLMFactory.newQuantitySchedule(sched_el.elements(), 
					      PlanScheduleType.TOTAL_INVENTORY);
    }

    public static Schedule buildSimpleQuantitySchedule(int qty, long start, 
						       long end, long time_incr) {
	return  buildSimpleQuantitySchedule(qty, truncTime(start, time_incr),
					    truncTime(end+1, time_incr)-1);
    }

    public static long truncTime(long time, long time_incr) {
	return (time/time_incr)*time_incr;
    }

    public static QuantityScheduleElement buildQuantityScheduleElement(double qty, long start, long end)
    {
	NewQuantityScheduleElement e = GLMFactory.newQuantityScheduleElement();
	e.setQuantity(qty);
	e.setStartTime(start);
	e.setEndTime(end);
	return e;
    }

    public static QuantityScheduleElement buildQuantityScheduleElement(double qty, long start, long end, long time_incr)
    {
	return buildQuantityScheduleElement(qty, truncTime(start, time_incr), 
					    truncTime(end+1, time_incr)-1);
    }


    // note amount can be positive or negative.
    // adjust quantity from start until end of schedule
    // if start is after end of schedule, 
    // append new element from start to start + a day
    public static Schedule adjustSchedule(Schedule sched, long start, int amount)
    {
	long end = sched.getEndTime();
	if (end > start) end = start+MSEC_PER_DAY;
	return adjustSchedule(sched, start, end, amount);
    }

    public static boolean isOffendingSchedule(Schedule sched) {
	long start = sched.getStartTime() -1;
	QuantityScheduleElement qse;
	Enumeration elements = sched.getAllScheduleElements();
	while (elements.hasMoreElements()){
	    qse = (QuantityScheduleElement)elements.nextElement();
	    if (qse.getStartTime() < start) {
		return true;
	    }
	    start = qse.getStartTime();
	}
	return false;
    }

    public static ScheduleElement getElementWithTime(Schedule s, long time) {
	ScheduleElement se = null;
	if (s != null) {
	    Collection c = s.getScheduleElementsWithTime(time);
	    if (!c.isEmpty()) {
		se = (ScheduleElement)c.iterator().next();
	    }
	}
	return se;
    }
		       
}
