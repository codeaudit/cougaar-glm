/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins.projection;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ScheduleImpl;
import org.cougaar.domain.planning.ldm.plan.ScheduleType;
import org.cougaar.util.TimeSpan;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.cougaar.domain.glm.ldm.ALPFactory;
import org.cougaar.domain.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.domain.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.domain.glm.ldm.plan.PlanScheduleElementType;
import org.cougaar.domain.glm.ldm.plan.PlanScheduleType;
import org.cougaar.domain.glm.debug.*;

public abstract class ConsumerSpec implements Serializable {
    protected Object consumer_;
    protected String resourceType_;
    protected Schedule mergedSchedule_ = null;
    private Map multipliers_ = null;

    public ConsumerSpec(Object consumer, String type, Map multipliers) {
	consumer_ = consumer;
	resourceType_ = type;
        multipliers_ = multipliers;
    }

    public ConsumerSpec(Object consumer, String type) {
	consumer_ = consumer;
	resourceType_ = type;
    }

    // what is consumed
    public abstract Enumeration getConsumed();
    // when is it consumed
    public abstract Vector getParameterSchedules();
    /** at what rate is it consumed
     * @return -1 if rate is not defined such as when there is 
     *  no org activity
     */
    public abstract Rate getRate(Asset resource, Vector params);

    private static final Double ONE = new Double(1.0);

    protected Double getMultiplier(Asset consumed) {
      return getMultiplier(consumed.getTypeIdentificationPG().getTypeIdentification());
    }

    protected Double getMultiplier(String consumedId) {
        if (multipliers_ == null) return ONE;
        Double mult = (Double) multipliers_.get(consumedId);
        if (mult == null) return ONE;
        return mult;
    }

    public static Schedule newObjectSchedule(Enumeration elements) {
	ScheduleImpl s = new ScheduleImpl();
	s.setScheduleElementType(PlanScheduleElementType.OBJECT);
	s.setScheduleType(ScheduleType.OTHER);
	s.setScheduleElements(elements);
	return s;
    }

    public Object getConsumer() {
	return consumer_;
    }

    public String getConsumedType() {
	return resourceType_;
    }

    // for a particular resource, return the
    /**
     * Here is what this method does (Ray Tomlinson).
     * Merges a number of parameter schedules into a combined
     * schedule. In the combined schedule, the "parameter"  of each
     * element is a Vector of the parameters from each of the input
     * schedules. For elements not covered by the input schedule, the
     * corresponding Vector element is null.
     *
     * There is a basic assumption that the elements of the input
     * schedules and the output schedule are non-overlapping and
     * gap-free. This may not have been the intention, but the
     * original code made that assumption and so do my modifications.
     **/
    public Schedule getMergedSchedule() {
	if (mergedSchedule_ == null) {
	    // sets scheds to Enumerations of schedule elements
	    // sets intervals to the initial schedule element within each schedule
	    Vector scheds = getParameterSchedules();
	    int num_params = scheds.size();
	    ObjectScheduleElement[] intervals = new ObjectScheduleElement[num_params];
	    Enumeration[] enums = new Enumeration[num_params];
	    GLMDebug.DEBUG("ConsumerSpec", num_params + " num params");
            ObjectScheduleElement ose;
            long start = TimeSpan.MAX_VALUE;
	    for (int ii = 0; ii < num_params; ii++) {
                enums[ii] = ((Schedule) scheds.get(ii)).getAllScheduleElements();
                if (enums[ii].hasMoreElements()) {
                    ose = (ObjectScheduleElement) enums[ii].nextElement();
                    intervals[ii] = ose;
                    if (ose.getStartTime() < start) {
                        start = ose.getStartTime();
                    }
                } else {
                    intervals[ii] = null; // Empty schedule
                }
	    }

	    long end = TimeSpan.MAX_VALUE; 
	    Vector result_sched = new Vector();
	    boolean notdone = true;
	    while (notdone) {
		Vector params = new Vector(num_params);
		params.setSize(num_params);
		notdone = false;
		for (int ii = 0; ii < num_params; ii++) {
                    params.set(ii, null);// Presume no element for schedule(ii)
		    // check if interval good
		    ose = intervals[ii];
		    if (ose != null) {
                        if (ose.getEndTime() <= start) {
                            // This has already been covered, Step to next
                            if (!enums[ii].hasMoreElements()) {
				// ran off end of schedule(ii)
                                intervals[ii] = null;
                                continue;
                            }
                            ose = (ObjectScheduleElement)enums[ii].nextElement();
                            intervals[ii] = ose;
                        }
			if (ose.getStartTime() > start) {
			    // ose is _not_ part of this result (gap)
                            // element, it's later
			    if (ose.getStartTime() < end) {
                                // This result element ends not later
                                // than the start of this pending element
				end = ose.getStartTime();
			    }
                            continue;
			}
                        // search for earliest end time 
			if (ose.getEndTime() < end) {
			    end = ose.getEndTime();
			}
			// add current param to list
			params.set(ii, ose.getObject());
			notdone = true;
		    }
		}	    
		if (notdone) {
		    result_sched.add(new ObjectScheduleElement(start, end, params));
		    start = end;
		    end = TimeSpan.MAX_VALUE;
		}
	    }
	    mergedSchedule_ = newObjectSchedule(result_sched.elements());
	    GLMDebug.DEBUG("ConsumberSpec", "ConsumerSpec created mergedSchedule "+result_sched.size());
	}
	return mergedSchedule_;
    }

    // getExtras();

    /**
     * @param resource consumed resource
     * @param demand_spec demand spec for the consumer
     * @return schedule quantity schedule of the daily rate of consumption
     *          of the given resource.
     **/
    public Schedule buildConsumptionRateSchedule(Asset resource) {
	ObjectScheduleElement ose;
	Rate rate;
	// This needs to be changed to a RateScheduleElement once I get the OK from
	// the other plugin developers - AHF
//	RateScheduleElement rse;
 	ObjectScheduleElement rse;

	Vector consumption_rate_schedule = new Vector();
	Enumeration schedule_elements = getMergedSchedule().getAllScheduleElements();
	while (schedule_elements.hasMoreElements()) {
	    ose = (ObjectScheduleElement)schedule_elements.nextElement();
	    rate = getRate(resource, (Vector)ose.getObject());
	    if (rate == null)
		continue;
// 	    rse = ALPFactory.newRateScheduleElement();
	    rse = new ObjectScheduleElement();
	    rse.setObject(rate);
// 	    rse.setRate(rate);
	    rse.setStartTime(ose.getStartTime());
	    rse.setEndTime(ose.getEndTime());
	    consumption_rate_schedule.addElement(rse);
	}
	return newObjectSchedule(consumption_rate_schedule.elements());
// 	return ALPFactory.newRateSchedule(consumption_rate_schedule.elements());
    }

}
