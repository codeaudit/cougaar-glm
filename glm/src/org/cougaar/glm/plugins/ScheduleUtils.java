/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.asset.NewScheduledContentPG;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.glm.ldm.plan.PlanScheduleType;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleUtilities;

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
