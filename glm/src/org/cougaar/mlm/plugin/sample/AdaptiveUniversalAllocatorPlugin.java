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

import java.util.*;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.core.adaptivity.OperatingModeImpl;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.naming.Glob;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.AllocationResultHelper;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.OperatingModeService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

// Simple plugin that says 'yes' to any task fed to it
// Optionally, if arguments are given, will only allocate tasks with given verbs
public class AdaptiveUniversalAllocatorPlugin extends SimplePlugin {
    private static class Filter {
        public Verb verb;
        public Glob glob;
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

    /** The current speed OperatingMode **/
    private static int sv = 0;
    public static final String SPEED_KNOB_NAME =
        "AdaptiveUniversalAllocatorPlugin.SPEED";
    private static Double[] counts = {
        new Double(  1),
        new Double(  2),
        new Double(  4),
        new Double(  8),
        new Double( 16),
        new Double( 32),
        new Double( 64),
        new Double(128),
        new Double(256),
        new Double(512),
    };

    private static OMCRangeList values = new OMCRangeList(counts);

    private OperatingModeService operatingModeService;

    private LoggingService logger;

    private boolean allServicesAcquired = false;

    private Alarm timer;

    OperatingModeImpl speedOM = null;

    Double currentMode = (Double) values.getEffectiveValue();

    /**
     * Create a single dummy asset to which to allocate all
     * appropriate tasks
     **/
    private Asset sink_asset = null;

    public void load() {
        super.load();
    }

    public void unload() {
        ServiceBroker sb = getDelegate().getServiceBroker();
        if (operatingModeService != null) {
            OperatingMode speedOM = operatingModeService.getOperatingModeByName(SPEED_KNOB_NAME);
            if (speedOM != null) {
                publishRemove(speedOM);
            }
            sb.releaseService(this, OperatingModeService.class, operatingModeService);
        }
        if (logger != null) {
            sb.releaseService(this, LoggingService.class, logger);
        }
        super.unload();
    }

    private boolean acquireServices() {
        ServiceBroker sb = getDelegate().getServiceBroker();
        if (logger == null) {
            logger = (LoggingService) sb.getService(this, LoggingService.class, null);
            if (logger == null) return false;
        }
        if (operatingModeService == null) {
            operatingModeService =
                (OperatingModeService) sb.getService(this, OperatingModeService.class, null);
            if (operatingModeService == null) return false;
        }
        allServicesAcquired = true;
        return true;
    }

    public void setupSubscriptions() {
        if (acquireServices()) {
            reallyStart();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        timer = wakeAfterRealTime(1000);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void reallyStart() {
	allTasks = (IncrementalSubscription) subscribe(allTasksPredicate);
        parseParams();
        Collection operatingModes = getBlackboardService().query(new UnaryPredicate() {
            public boolean execute(Object o) {
                if (o instanceof OperatingMode) {
                    OperatingMode om = (OperatingMode) o;
                    if (om.getName().equals(SPEED_KNOB_NAME)) {
                        return true;
                    }
                }
                return false;
            }
        });
        if (operatingModes.size() > 0) {
            speedOM = (OperatingModeImpl) operatingModes.iterator().next();
        } else {
            speedOM = new OperatingModeImpl(SPEED_KNOB_NAME, values);
            publishAdd(speedOM);
        }
    }

    public void execute() {
        if (timer != null) cancelTimer();
        if (!allServicesAcquired) {
            if (acquireServices()) {
                reallyStart();
            } else {
                startTimer();
                return;
            }
        }
        addTasks(allTasks.getAddedList());
        changeTasks(allTasks.getChangedList());
        removeTasks(allTasks.getRemovedList());
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
                filter.verb = Verb.getVerb(verbPattern.substring(0, slashPos));
                filter.glob = Glob.parse(verbPattern.substring(slashPos + 1));
            } else {
                filter.verb = Verb.getVerb(verbPattern);
                filter.glob = null;
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
            System.out.println("AdaptiveUniversalAllocatorPlugin adding " + filter);
            assetName.append('_');
            assetName.append(filter.verb);
        }
	sink_asset = theLDMF.createPrototype("AbstractAsset", assetName.substring(0));
	publishAdd(sink_asset);
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
        if (filter.glob == null) return true;
        if (filter.glob.match(task.toString())) {
//              System.out.println("Match " + task.toString());
            return true;
        } else {
//              System.out.println("No match " + task.toString());
            return false;
        }
    }

    private void wasteTime() {
        if (currentMode.intValue() > 3) {
            sv += currentMode.intValue();
        } else {
            sv -= 1;
        }
    }

    private void delay() {
        Double newMode = ((Double) speedOM.getValue());
        if (!newMode.equals(currentMode)) {
            logger.debug("New mode is " + newMode);
            currentMode = newMode;
        }
        int loopCount = currentMode.intValue() * 8000;
        for (int i = 0; i < loopCount; i++) {
            wasteTime();
        }
    }

    private void addTasks(Enumeration e) {
	while (e.hasMoreElements()) {
            Task task = (Task)e.nextElement();

            if (!isInterestingTask(task))
                continue;
            delay();
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
        
        delay();
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

            delay();
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
	return helper.getAllocationResult(1.0);
    }
}
