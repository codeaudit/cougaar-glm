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

package org.cougaar.mlm.plugin.sample;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.service.LoggingService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectRate;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.AllocationResultHelper;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

// Simple plugin that says 'yes' to any task fed to it
// Optionally, if arguments are given, will only allocate tasks with given verbs
public class UniversalAllocatorPlugin extends SimplePlugin {
    private static class Filter {
        public Verb verb;
        public Pattern regex;
        public Schedule schedule; // The schedule of failing periods (if any)
        public String toString() {
            if (schedule == null) return verb.toString();
            StringBuffer buf = new StringBuffer();
            buf.append(verb);
            for (ListIterator i = schedule.listIterator();i.hasNext();) {
                ScheduleElement el = (ScheduleElement) i.next();
                buf.append(';');
                if (el.getStartTime() > TimeSpan.MIN_VALUE) {
                    buf.append(el.getStartDate());
                }
                buf.append("..");
                if (el.getEndTime() < TimeSpan.MAX_VALUE) {
                    buf.append(el.getEndDate());
                }
            }
            return buf.toString();
        }
        public boolean mayFail() {
            return schedule != null && !schedule.isEmpty();
        }
    }
    private IncrementalSubscription allTasks;
    private UnaryPredicate allTasksPredicate = new UnaryPredicate() {
	public boolean execute(Object o) {
            if (o instanceof Task) {
                if (verbMap.isEmpty()) return true;
                if (verbMap.get(((Task) o).getVerb()) != null) {
                    return true;
                }
            }
            return false;
        }
    };

    /** Map a Verb to a Filter **/
    private Map verbMap = new HashMap();

    private LoggingService logger;

    /**
     * Create a single dummy asset to which to allocate all
     * appropriate tasks
     **/
    private Asset sink_asset = null;
    
    public void setupSubscriptions() {
        logger = (LoggingService)
            getDelegate().getServiceBroker()
            .getService(this, LoggingService.class, null);
        logger = LoggingServiceWithPrefix
            .add(logger, getAgentIdentifier().toString() + ": ");
	allTasks = (IncrementalSubscription)subscribe(allTasksPredicate);
        parseParams();
    }

    /**
     * Parameters are of the form:
     *    [-]<verb>{;[<startdate>]..[<enddate>]}*
     * ...optional minus, verb, and zero or more date ranges. The
     * start or end of a date range may be omitted signifying min or
     * max respectively. The initial minus signifies that the default
     * is to fail the allocations. The date ranges specify exceptions
     * to the default. So, for example:
     *   Supply;9/7/2005..10/3/2005
     * would fail in the time period from September 7, 2005 to October
     * 3, 2005.
     **/
    private void parseParams() {
        StringBuffer assetName = new StringBuffer();

        assetName.append("UniversalSink");
	for (Enumeration e = getParameters().elements();e.hasMoreElements();) {
	    String param = (String) e.nextElement();
            Filter filter = new Filter();
            ScheduleElement el;
            Schedule schedule = new ScheduleImpl();
            boolean defaultIsFailure = param.startsWith("-");
            if (defaultIsFailure) param = param.substring(1);
            StringTokenizer tokens = new StringTokenizer(param, ";");
            String verbPattern = tokens.nextToken();
            int slashPos = verbPattern.indexOf('/');
            if (slashPos >= 0) {
                filter.verb = Verb.get(verbPattern.substring(0, slashPos));
                filter.regex = Pattern.compile(verbPattern.substring(slashPos + 1));
            } else {
                filter.verb = Verb.get(verbPattern);
                filter.regex = null;
            }
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                String sub;
                int dotdot = token.indexOf("..");
                long from = TimeSpan.MIN_VALUE;
                long to = TimeSpan.MAX_VALUE;
                if (dotdot < 0) {
                    from = Date.parse(token);
                } else {
                    sub = token.substring(0, dotdot);
                    if (sub.length() > 0) {
                        from = Date.parse(sub);
                    }
                    sub = token.substring(dotdot + 2);
                    if (sub.length() > 0) {
                        to = Date.parse(sub);
                    }
                }
                el = new ScheduleElementImpl(from, to);
                schedule.add(el);
            }
            if (defaultIsFailure) {
                /* We built a schedule of exceptions to failure (a
                   success schedule). It must be converted to a
                   failure schedule. */
                long startTime = TimeSpan.MIN_VALUE;
                filter.schedule = new ScheduleImpl();
                for (ListIterator i = schedule.listIterator(); i.hasNext(); ) {
                    ScheduleElement el2 = (ScheduleElement) i.next();
                    el = new ScheduleElementImpl(startTime, el2.getStartTime());
                    filter.schedule.add(el);
                    startTime = el2.getEndTime();
                }
                el = new ScheduleElementImpl(startTime, TimeSpan.MAX_VALUE);
                filter.schedule.add(el);
            } else {
                /* We build a schedule of exceptions to success (a
                   failure schedule) and that is exactly what we need */
                filter.schedule = schedule;
            }
            verbMap.put(filter.verb, filter);
//             System.out.println("UniversalAllocatorPlugin adding " + filter);
            assetName.append('_');
            assetName.append(filter.verb);
        }

	final String aName = assetName.substring(0);

	// See if an AbstractAsset with the appropriate TypeID already exists if rehydrating
	if (didRehydrate()) {
	  Collection sinks = query(new UnaryPredicate() {
	      public boolean execute(Object o) {
		if (o instanceof AbstractAsset) {
		  Asset a = (Asset)o;
		  // Must be a prototype with appropriate name
		  // the assetName is the TypeId and the classname of the Asset is AbstractAsset
		  TypeIdentificationPG tip = a.getTypeIdentificationPG();
		  if (tip != null && aName.equals(tip.getTypeIdentification()))
		    return true;
		}
		return false;
	      }
	    });
	  if (sinks != null && ! sinks.isEmpty()) {
	    Iterator iter = sinks.iterator();
	    if (iter.hasNext())
	      sink_asset = (Asset) iter.next();
	  }
	} // end didRehydrate test

	if (sink_asset == null) {
	  if (logger.isDebugEnabled())
	    logger.debug(getAgentIdentifier() + " creating new AbAsset " + aName);
	  sink_asset = theLDMF.createPrototype("AbstractAsset", aName);
	  publishAdd(sink_asset);
	} else {
	  if (logger.isDebugEnabled())
	    logger.debug(getAgentIdentifier() + " found old AbAsset " + aName);
	}
    }

    /**
     * Is this a task we're interested in? Either we didn't specify a
     * verb, or the task has a verb among those specified
     **/
    private boolean isInterestingTask(Task task) 
    {
        if (verbMap.size() == 0) return true;
        Filter filter = (Filter) verbMap.get(task.getVerb());
        if (filter == null) return false;
        if (filter.regex == null) return true;
        String input = task.toString();
        Matcher m = filter.regex.matcher(input);
        if (m.matches()) {
//              System.out.println("Match " + input);
            return true;
        } else {
//              System.out.println("No match " + input);
            return false;
        }
    }

    public void execute() 
    {
      //	System.out.println("In UniversalAllocatorPlugin.execute");

        addTasks(allTasks.getAddedList());
        changeTasks(allTasks.getChangedList());
        removeTasks(allTasks.getRemovedList());
    }

    private void addTasks(Enumeration e) {
	while (e.hasMoreElements()) {
            Task task = (Task)e.nextElement();

            if (!isInterestingTask(task))
                continue;
            print("   add", task);
            AllocationResult ar = computeAllocationResult(task);

            // Allocate task to sink_asset
            Allocation allocation = 
                theLDMF.createAllocation(theLDMF.getRealityPlan(),
                                         task,
                                         sink_asset,
                                         ar,
                                         Role.BOGUS);
            publishAdd(allocation);
        }
    }

    private void changeTasks(Enumeration e) {
      // NEEDS TO BE FIXED!!
      // all task changes are not necessarily allocationresult changes
      // allocation result updates should be done as a result of a subscription
      // for this plugins planelements!!!!
      while (e.hasMoreElements()) {
        Task task = (Task)e.nextElement();
        
        if (!isInterestingTask(task))
          continue;
        
        print("change", task);
        AllocationResult ar = computeAllocationResult(task);
        Allocation allocation = (Allocation) task.getPlanElement();
        if (allocation != null) {
          AllocationResult estAR = allocation.getEstimatedResult();
          if (estAR != null) {
            if (!ar.isEqual(estAR)) {
              allocation.setEstimatedResult(ar);
              publishChange(allocation);
            }
          }
        }
      }
    }

    private void removeTasks(Enumeration e) {
	while (e.hasMoreElements()) {
            Task task = (Task)e.nextElement();

            if (!isInterestingTask(task))
                continue;

            print("remove", task);
        }
    }

    private void print(String m, Task task) {
//          System.out.println("UA " + m + ": " + (TaskUtils.isProjection(task) ? TaskUtils.projectionDesc(task) : TaskUtils.taskDesc(task)));
    }

    /**
     * Compute an allocation result for this task. We use an
     * AllocationResultHelper to do most of the work. Our job is to
     * enumerate the schedule failure time periods and ask the helper
     * to indicate a failure (zero value) for the applicable part of
     * those failure time intervals.
     **/
    private AllocationResult computeAllocationResult(Task task) 
    {
        Verb verb = task.getVerb();
        Filter filter = (Filter) verbMap.get(verb);
        AllocationResultHelper helper = new AllocationResultHelper(task, null);
        boolean isSupply = verb.equals(Constants.Verb.SUPPLY);
        boolean isProjectSupply = verb.equals(Constants.Verb.PROJECTSUPPLY);
        if (filter != null && filter.schedule != null) { // There are some failure time periods
            for (ListIterator i = filter.schedule.listIterator(); i.hasNext(); ) {
                ScheduleElement el = (ScheduleElement) i.next();
                if (isSupply) {
                    helper.setFailed(AspectType.QUANTITY, el.getStartTime(), el.getEndTime());
                    continue;
                }
                if (isProjectSupply) {
                    helper.setFailed(AlpineAspectType.DEMANDRATE, el.getStartTime(), el.getEndTime());
                    continue;
                }
                // Don't know how to fail anything else.
            }
        }
//          if (false) {            // This code delivers everything a day early
//              if (isSupply && !helper.isChanged()) {
//                  long endTime = helper.getPhase(0).getEndTime() - TimeUtils.MSEC_PER_DAY;
//                  AspectValue av = new TimeAspectValue(AspectType.END_TIME, endTime);
//                  helper.setAspect(av);
//              }
//          }
	AllocationResult ret = helper.getAllocationResult(1.0);
        if (ret.isSuccess() || filter.mayFail()) return ret;
        if (logger.isWarnEnabled()) {
            AspectValue[] avs = ret.getAspectValueResults();
            boolean found = false;
            for (int i = 0; i < avs.length; i++) {
                AspectValue av = avs[i];
                int aspectType = av.getAspectType();
                Preference pref = task.getPreference(aspectType);
                ScoringFunction sf = pref.getScoringFunction();
                double thisScore = sf.getScore(av);
                AspectValue best = pref.getScoringFunction().getBest().getAspectValue();
                if (thisScore >= ScoringFunction.HIGH_THRESHOLD) {
                    String avRateClass = null;
                    String bestRateClass = null;
                    if (best instanceof AspectRate) {
                        ((AspectRate) best).getRateValue().getClass().getName();
                    }
                    if (av instanceof AspectRate) {
                        ((AspectRate) av).getRateValue().getClass().getName();
                    }
                    logger.warn("Unexpected failure in computeAllocationResult:"
                                + " task=" + task
                                + " AspectType=" + aspectType
                                + " AspectValue=" + av
                                + " scoringFunction=" + sf
                                + " best=" + best
                                + " score=" + thisScore
                                + " avRateClass=" + avRateClass
                                + " bestRateClass=" + bestRateClass);
                    found = true;
                }
            }
            if (!found) {
                logger.warn("Failure in computeAllocationResult with not apparent reason");
            }
        }
        return helper.getAllocationResult(1.0, true);
    }
}
