/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 1999-2004 BBNT Solutions, LLC
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
package org.cougaar.glm.plugins.projection;

import org.cougaar.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.glm.ldm.plan.PlanScheduleElementType;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.Rate;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.planning.ldm.plan.ScheduleType;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

public abstract class ConsumerSpec implements Serializable {
  protected Object consumer_;
  protected String resourceType_;
  protected Schedule mergedSchedule_ = null;
  private Map multipliers_ = null;
  private Logger logger = Logging.getLogger(ConsumerSpec.class);

  public ConsumerSpec(Object consumer, String type, Map multipliers) {
    consumer_ = consumer;
    resourceType_ = type;
    multipliers_ = multipliers;
  }

  public ConsumerSpec(Object consumer, String type) {
    consumer_ = consumer;
    resourceType_ = type;
  }

  /**
   * The merged schedule drives the consumer spec.
   * It is only calculated once unless reset() is called.
   */
  public void reset() {
    mergedSchedule_ = null;
  }

  // what is consumed
  public abstract Enumeration getConsumed();

  // when is it consumed
  public abstract Vector getParameterSchedules();

  /**
   * at what rate is it consumed
   * @return -1 if rate is not defined such as when there is
   *         no org activity
   */
  public abstract Rate getRate(Asset resource, Vector params);

  private static final Double ONE = new Double(1.0);

  public Double getMultiplier(Asset consumed) {
    return getMultiplier(consumed.getTypeIdentificationPG().getTypeIdentification());
  }

  public Double getMultiplier(String consumedId) {
    if (multipliers_ == null)
      return ONE;
    Double mult = (Double) multipliers_.get(consumedId);
    if (mult == null)
      return ONE;
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
   * Here is what this method does (Ray Tomlinson). Merges a number
   * of parameter schedules into a combined schedule. In the
   * combined schedule, the "parameter" of each element is a Vector
   * of the parameters from each of the input schedules. For
   * elements not covered by the input schedule, the corresponding
   * Vector element is null. The output schedule elements correspond
   * to the intersections of the elements or inter-element gaps of
   * the input schedules. No schedule element is generated for time
   * spans where none of the input schedules has an element.
   * Conversely, all elements of the merged schedule have at least
   * one non-null parameter.
   * There is an assumption that the elements of the input schedules
   * and the output schedule are non-overlapping.
   */
  public Schedule getMergedSchedule() {
    if (mergedSchedule_ == null) {
      // sets scheds to Enumerations of schedule elements
      // sets intervals to the initial schedule element within each schedule
      Vector scheds = getParameterSchedules();
      int num_params = scheds.size();
      ObjectScheduleElement[] intervals = new ObjectScheduleElement[num_params];
      Enumeration[] enums = new Enumeration[num_params];
      if (logger.isDebugEnabled()) {
        logger.debug(num_params + " num params");
      }
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

      Vector result_sched = new Vector();
      long end = TimeSpan.MIN_VALUE;
      while (end != TimeSpan.MAX_VALUE) {
        Vector params = new Vector(num_params);
        params.setSize(num_params);
        boolean haveParams = false;
        end = TimeSpan.MAX_VALUE;
        for (int ii = 0; ii < num_params; ii++) {
          params.set(ii, null);// Presume no element for schedule(ii)
          // check if interval good
          ose = intervals[ii];
          if (ose != null) {
            if (ose.getEndTime() <= start) {
              // This has already been covered; Step to next
              if (!enums[ii].hasMoreElements()) {
                // ran off end of schedule(ii)
                intervals[ii] = null;
                continue;
              }
              ose = (ObjectScheduleElement) enums[ii].nextElement();
              intervals[ii] = ose;
            }
            if (ose.getStartTime() > start) {
              // ose is _not_ part of this result
              // element, it's later (there is a gap)
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
            haveParams = true;
          }
        }
        if (haveParams) {
          result_sched.add(new ObjectScheduleElement(start, end, params));
        }
        start = end;
      }
      mergedSchedule_ = newObjectSchedule(result_sched.elements());
      if (logger.isDebugEnabled()) {
        logger.debug("ConsumerSpec created mergedSchedule " + result_sched.size());
      }
    }
    return mergedSchedule_;
  }

  // getExtras();

  /**
   * @param resource consumed resource
   * @return schedule quantity schedule of the daily rate of consumption
   *         of the given resource.
   */
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
      ose = (ObjectScheduleElement) schedule_elements.nextElement();
      rate = getRate(resource, (Vector) ose.getObject());
      if (rate == null)
        continue;
      // 	    rse = GLMFactory.newRateScheduleElement();
      rse = new ObjectScheduleElement();
      rse.setObject(rate);
      // 	    rse.setRate(rate);
      rse.setStartTime(ose.getStartTime());
      rse.setEndTime(ose.getEndTime());
      consumption_rate_schedule.addElement(rse);
    }
    return newObjectSchedule(consumption_rate_schedule.elements());
    // 	return GLMFactory.newRateSchedule(consumption_rate_schedule.elements());
  }

}
