/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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
 */

package org.cougaar.mlm.plugin.sample;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.core.adaptivity.OperatingModeImpl;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.OperatingModeService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.AllocationResultHelper;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

// Simple plugin that says 'yes' to any task fed to it
// Optionally, if arguments are given, will only allocate tasks with given verbs
public class AdaptiveUniversalAllocatorPlugin extends SimplePlugin {
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
