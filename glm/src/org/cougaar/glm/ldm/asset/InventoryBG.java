/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.ldm.asset;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.plugin.util.AllocationResultHelper;
import org.cougaar.glm.debug.*;
import org.cougaar.glm.execution.common.InventoryReport;
import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.glm.ldm.plan.PlanScheduleType;
import org.cougaar.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.plugins.*;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;

public class InventoryBG implements PGDelegate {

  public static final int PRIORITY_LEVELS = 10;
  public static final long CANONICAL_TIME_OFFSET = TimeUtils.MSEC_PER_DAY - 1;
  public static final double GOAL_LEVEL_BOOST_CAPACITY_FACTOR= 1.1;

  protected InventoryPG myPG_;
  // Each element in these Vectors represents a day.
  // Element zero of each Vector is today
  // Refill Tasks
  protected transient Vector dueIns_;
  protected transient Vector dueInTasks_;
  // Supply Tasks
  protected transient Vector dueOut_;
  // ProjectSupply Tasks
  protected transient double[] level_;
  protected transient int dirtyDay_;
  protected transient boolean needInitializeInventoryLevels;
  protected transient Vector dueInsByRequestDay_;
  // Was the BG properly initialized, if not, it is unusable
  protected boolean initialized_ = false;
  // today is the alp day right now
  private boolean isStartTimeSet = false;
  private int startDay_;
  protected int today_;
  protected int firstDayOfDemand_;
  private ProjectionWeight weight_;
  // Stuff for execution below
  protected ArrayList report_history;
  protected InventoryReport latest_report;
  protected InventoryReport oldest_report;
  protected Calendar calendar_ = Calendar.getInstance();

  // target level stuff
  protected double[] demandSchedule_=null;
  protected double goalLevelMultiplier_;
  protected double minReorderLevel_;
  protected double maxReorderLevel_;
  protected int    daysOfDemand_;
  protected int    daysForward_;
  protected int    daysBackward_;
    
  protected int    goalExceededCap=0;
  final static protected int GOAL_EXCEEDED_CAPACITY_DEBUG_THRESHOLD = 2;

  public InventoryBG(InventoryPG  pg) {
    myPG_ = pg;
    dueIns_ = new Vector();
    dueOut_ = new Vector();
    dueInsByRequestDay_ = new Vector();
    level_ = null;
    report_history = new ArrayList();
    latest_report = null;
    oldest_report = null;
  }

  public void setProjectionWeight(ProjectionWeight weight) {
    weight_ = weight;
  }

  // put in so that we can check whether a requisition will be counted
  public ProjectionWeight getProjectionWeight() {
    return weight_;
  }

  //Reinit transient variables from input serialization
  private void readObject(ObjectInputStream s)
    throws IOException,
	   ClassNotFoundException,
	   NotActiveException {
    s.defaultReadObject();
	
    dueIns_ = new Vector();
    dueOut_ = new Vector();
    dueInsByRequestDay_ = new Vector();
    level_ = null;
  }

  // GLK temp changed void to int
  public int resetInventory(Inventory inventory, long today) {
    needInitializeInventoryLevels = true;

    if (!isStartTimeSet) {// Only do this check once
      Scalar ignore = getScalar(0.0);// Insure we know how to manage this type
    }
    for (int i = 0, n = dueOut_.size(); i < n; i++) {
      ((DueOutList) dueOut_.elementAt(i)).clear();
    }
    for (int i = 0, n = dueIns_.size(); i < n; i++) {
      ((DueInList) dueIns_.elementAt(i)).clear();
    }
    dueInsByRequestDay_.clear();

    initializeTime(inventory, today);
    if (!inventory.hasScheduledContentPG()) {
      Asset proto = inventory.getPrototype();
      String id = proto.getTypeIdentificationPG().getNomenclature();
      GLMDebug.ERROR("InventoryBG", " initInventory - NO ScheduledContentPG on Inventory Object- "+id+ ", EXIT!");
      System.exit(1);
    }

    //     printDebug("Initializing inventory: "+inventoryDesc(inventory));
    //     printInventory(inventory,null);


    // GLK temp return
    return 0;
  }

  /**
     Set the startTime to the earliest of today or the oldest
     inventory report. In all cases, the start time is pushed to the
     end of the day in which it falls.
  **/
  private void initializeTime(Inventory inventory, long today) {
    try {
      if (!isStartTimeSet) { // Need to set start time
	long start_time = today;
	InventoryReport oldest = getOldestInventoryReport();
	if (oldest != null) {
	  start_time = Math.min(start_time, oldest.theReportDate);
	}
	startDay_ = (int) (start_time / TimeUtils.MSEC_PER_DAY);
	isStartTimeSet = true;
      }
      today_ = convertTimeToDay(today);
    } catch (RuntimeException re) {
      System.err.print(re + ": today=" + today + ", today_ = " + today_);
      throw re;
    }
  }

  public long getStartTime() {
    return convertDayToTime(0);
  }

  private int getStartDay() {
    //          if (!isStartTimeSet) throw new RuntimeException("startDay_ never set");
    return startDay_;
  }

  public int getImputedDayOfTime(long time) {
    return getImputedDay(convertTimeToDay(time));
  }

  public int getImputedDay(int day) {
    return day - getToday();
  }

  public int getFirstPlanningDay() {
    // The first planning day is the first day of demand unless
    // that day falls in the past, in which case the first
    // planning day is today.
    return Math.max(today_ + 1, firstDayOfDemand_);
  }


  /**
   * @return the day number of the current execution time.
   **/
  public int getToday() {
    return today_;
  }

  /**
   * Get the inventory level at a particular execution time
   **/
  public Scalar getLevel(long time) {
    return getLevel(convertTimeToDay(time));
  }

  /**
   * Get inventory level on a particular day relative to the start
   * time of the inventory. If the specified day is before the start
   * time of the inventory, the initial level is returned. If after
   * the maximum day, the level on that maximum day is returned.
   **/
  public Scalar getLevel(int day) {
    if ((day < 0) || (level_ == null)) {
      return myPG_.getInitialLevel();
    }
    if  (day >= level_.length) {
      day = level_.length-1;
    }
    if (day >= dirtyDay_) updateLevels(day);
    return getScalar(level_[day]);
  }

  private void updateDirtyDay(int day) {
    if (day > 0 && day < dirtyDay_) {
      dirtyDay_ = day;
    }
  }

  /**
   * Record all demand tasks as dueouts. As a side effect compute the firstDayOfDemand_
   **/
  public int withdrawFromInventory(Inventory inventory, MessageAddress clusterID) {
    Enumeration role_sched = inventory.getRoleSchedule().getRoleScheduleElements();
    long earliestDemand = Long.MAX_VALUE;
    while (role_sched.hasMoreElements()) {
      PlanElement pe = (PlanElement)role_sched.nextElement();
      Task task = pe.getTask();
      if (task.getVerb().equals(Constants.Verb.WITHDRAW)) {
	addDueOut(task);
	earliestDemand = Math.min(earliestDemand, TaskUtils.getEndTime(task));
      } else if (task.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) {
	addDueOutProjection(task);
	earliestDemand = Math.min(earliestDemand, TaskUtils.getStartTime(task));
      } else {
	System.err.println("What the .... Task added to role schedule "+TaskUtils.taskDesc(task));
      }
    }
    if (earliestDemand != Long.MAX_VALUE)
      firstDayOfDemand_ = convertTimeToDay(earliestDemand);
    return 0;
  }

  /**
   * Convert a time (long) into a day of this inventory that can be
   * used to index duein/out vectors, levels, etc.
   **/
  public int convertTimeToDay(long time) {
    int thisDay = (int) (time / TimeUtils.MSEC_PER_DAY);
    int startDay = getStartDay();
    return thisDay - startDay;
  }

  public long convertDayToTime(int day) {
    int startDay = getStartDay();
    return TimeUtils.MSEC_PER_DAY * (day + startDay) + CANONICAL_TIME_OFFSET;
  }

  public long getStartOfDay(int day) {
    int startDay = getStartDay();
    return TimeUtils.MSEC_PER_DAY * (day + startDay);
  }

  private long canonicalTime(long time) {
    return convertDayToTime(convertTimeToDay(time));
  }

  private abstract class DueIOList {
    List list = null; // Create on demand
    protected double actualTotal;
    protected double projectedTotal;
    protected int day;
    protected int imputedDay;

    protected DueIOList(int day) {
      this.day = day;
      setImputedDay();
    }

    private void setImputedDay() {
      imputedDay = day - today_;
    }

    public double getTotal() {
      return actualTotal + projectedTotal;
    }

    public double getActualTotal() {
      return actualTotal;
    }

    public double getProjectedTotal() {
      return projectedTotal;
    }

    public int size() {
      if (list == null) return 0;
      return list.size();
    }

    public void clear() {
      if (list != null) list.clear();
      if (actualTotal != 0.0 || projectedTotal != 0.0) {
	actualTotal = 0.0;
	projectedTotal = 0.0;
	updateDirtyDay(day);
      }
      setImputedDay();
    }

    public void add(DueIO d) {
      Task t = d.getTask();
      if (d.getFilled()) {
	adjustTotals(t, 1.0);
	updateDirtyDay(day);
      }
      if (list == null) list = new ArrayList(1);
      list.add(d);
    }

    public void remove(Task t) {
      for (int i = 0, n = list.size(); i < n; i++) {
	DueIO d = (DueIO) list.get(i);
	if (d.getTask() == t) {
	  if (d.getFilled()) {
	    adjustTotals(t, -1.0);
	    updateDirtyDay(day);
	  }
	  list.remove(d);
	  return;
	}
      }
    }

    protected abstract void adjustTotals(Task t, double w);
  }

  private class DueOutList extends DueIOList {
    public DueOutList(int day) {
      super(day);
    }

    public DueOut get(int ix) {
      return (DueOut) list.get(ix);
    }

    public List getWithdrawTasks() {
      if (list == null) return null;
      Iterator tasks = list.iterator();
      ArrayList actualsList = new ArrayList();
      Task t;
      while (tasks.hasNext()) {
	t = ((DueIO)tasks.next()).getTask();
	if (t.getVerb().equals(Constants.Verb.WITHDRAW) &&
	    (getWeightingFactor(t, imputedDay) > 0.0)) {
	  actualsList.add(t);
	}
      }
      return actualsList;
    }

    protected void adjustTotals(Task t, double w) {
      double wf = getWeightingFactor(t, imputedDay) * w;
      if (t.getVerb().equals(Constants.Verb.WITHDRAW)) {
	actualTotal += TaskUtils.getWithdrawQuantity(t) * wf;
      } else {
	projectedTotal += TaskUtils.getDailyQuantity(t) * wf;
      }
    }
  }

  private class DueInList extends DueIOList {
    public DueInList(int day) {
      super(day);
    }

    public DueIn get(int ix) {
      return (DueIn) list.get(ix);
    }

    public List getSupplyTasks() {
      if (list == null) return null;
      Iterator tasks = list.iterator();
      ArrayList actualsList = new ArrayList();
      Task t;
      while (tasks.hasNext()) {
	t = ((DueIO)tasks.next()).getTask();
	if (t.getVerb().equals(Constants.Verb.SUPPLY) &&
	    (getWeightingFactor(t, imputedDay) > 0.0)) {
	  actualsList.add(t);
	}
      }
      return actualsList;
    }

    protected void adjustTotals(Task t, double w) {
      double wf = getWeightingFactor(t, imputedDay);
      if (TaskUtils.isProjection(t)) {
	double q = TaskUtils.getDailyQuantity(t);
	if (q == 0.0 && w > 0.0) {
	  GLMDebug.ERROR("InventoryBG", null, "Added refill projection having 0.0 quantity " + TaskUtils.taskDesc(t));
	}
	projectedTotal += q * w * wf;
      } else {
	if (wf < 1.0){
	  GLMDebug.ERROR("InventoryBG", null,
			 "Inv Day: " + day
			 + " imputedDay: " + imputedDay
			 + " Zero weighted supply task: "+TaskUtils.taskDesc(t));
	}
	actualTotal += TaskUtils.getRefillQuantity(t) * w * wf;
      }
      //              System.out.println("adjustTotals " + TimeUtils.dateString(convertDayToTime(day)) + ": " + getTotal());
    }
  }

  private void addDueOut(DueOut dueOut) {
    int day = dueOut.getDay();
    while (day >= dueOut_.size()) {
      dueOut_.add(new DueOutList(dueOut_.size()));
    }
    DueOutList v = (DueOutList) dueOut_.get(day);
    v.add(dueOut);
  }

  private void addDueOut(Task request) {
    int day = convertTimeToDay(TaskUtils.getEndTime(request));
    if (day <  0) {
      day = 0;
      GLMDebug.ERROR("InventoryBG",
		     "addDueOut(), Request happens in the past.(Start Time "
		     + TimeUtils.dateString(getStartTime())
		     + ")"
		     + "Task time:"
		     + TimeUtils.dateString(TaskUtils.getEndTime(request))
		     + ", Task: "
		     + TaskUtils.taskDesc(request));
    }
    PlanElement pe = request.getPlanElement();
    // If the request has just been rescinded, the plan element will be null.
    // This task should not affect planning
    if (pe == null) return;
    boolean filled = true;
    AllocationResult ar = pe.getEstimatedResult();
    if (ar != null) filled = ar.isSuccess();
    addDueOut(new DueOut(request, filled, day));
  }

  // DRAT!!! Golden opportunity for refactoring.  Emergency demo solution
  // AHF -- Need to merge common code from addDueOut() and addDueOutProjeciton()
  // 
  private void addDueOutProjection(Task task) {
    PlanElement pe = task.getPlanElement();
    // If the task has just been rescinded, the plan element will be null.
    // This task should not affect planning
    if (pe == null) return;
    //          Rate r = TaskUtils.getRate(task);
    //          double rate = r.getValue(0);
    AllocationResultHelper helper = new AllocationResultHelper(task, task.getPlanElement());
    for (int i = 0, n = helper.getPhaseCount(); i < n; i++) {
      AllocationResultHelper.Phase phase =
	(AllocationResultHelper.Phase) helper.getPhase(i);
      double qty = phase.getAspectValue(AlpineAspectType.DEMANDRATE).getValue();
      boolean filled = qty > 0.0;
      int start = convertTimeToDay(phase.getStartTime());
      int end = convertTimeToDay(phase.getEndTime());
      if (start < 0) start = 0;
      for (int day = start; day < end; day++) {
	addDueOut(new DueOut(task, filled, day));
      }
    }
  }

  /**
   * Update the allocations results of all the allocations of
   * DueOuts for this inventory. Cycles through all the dueouts and
   * updates the allocation results to be success or fail on the day
   * of the dueout.
   * @return List of the changed Allocations
   **/
  public List updateDueOutAllocations() {
    List result = new ArrayList();
    int size = dueOut_.size();
    Map helpers = new HashMap();
    for (int day = 0; day < size; day++) {
      long startTime = getStartOfDay(day);
      long endTime = startTime + TimeUtils.MSEC_PER_DAY;
      DueOutList dueOuts = (DueOutList) dueOut_.get(day);
      for (int i = 0, n = dueOuts.size(); i < n; i++) {
	DueOut dueout = dueOuts.get(i);
	Task task = dueout.getTask();
	PlanElement pe = task.getPlanElement();
	if (pe == null) continue; // Probably being rescinded
	AllocationResultHelper helper =
	  (AllocationResultHelper) helpers.get(pe);
	if (helper == null) {
	  helper = new AllocationResultHelper(task, pe);
	  helpers.put(pe, helper);
	}
	if (dueout.getFilled() != dueout.getPreviouslyFilled()) {
	  if (dueout.getFilled()) {
	    if (TaskUtils.isProjection(task)) {
	      helper.setBest(AlpineAspectType.DEMANDRATE, startTime, endTime);
	    } else {
	      helper.setBest(AspectType.QUANTITY, startTime, endTime);
	    }
	  } else {
	    if (TaskUtils.isProjection(task)) {
	      helper.setFailed(AlpineAspectType.DEMANDRATE, startTime, endTime);
	    } else {
	      helper.setFailed(AspectType.QUANTITY, startTime, endTime);
	    }
	  }
	}
      }
    }
    for (Iterator i = helpers.keySet().iterator(); i.hasNext(); ) {
      PlanElement pe = (PlanElement) i.next();
      AllocationResultHelper helper = (AllocationResultHelper) helpers.get(pe);
      if (helper.isChanged()) {
	AllocationResult ar = helper.getAllocationResult(1.0);
	pe.setEstimatedResult(ar);
	result.add(pe);
      }
    }
    return result;
  }

  // Looking for the lowest priority dueout from today to the given day.
  public DueOut getLowestPriorityDueOutBeforeDay(int end) {
    DueOut lowest = null;
    // Also includes this day
    int low = Math.max(0, today_);
    int size = dueOut_.size() - 1;
    if (end > size) end = size;
    // Scan all dueouts for the specified days
    for (int i = end; i >= low; i--) {
      DueOutList demand = (DueOutList) dueOut_.get(i);
      for (int h = 0, n = demand.size(); h < n; h++) {
	DueOut d = demand.get(h);
	if (d.getFilled()) {
	  double weight = getWeightingFactor(d.getTask(), i - today_);
	  if (weight > 0.0) {
	    if (lowest == null || comparePriority(d, lowest) < 0) {
	      lowest = d;
	    }
	  }
	}
      }
    }
    return lowest;            // Nothing found
  }

  public void setDueOutFilled(DueOut dueOut, boolean newFilled) {
    dueOut.setFilled(newFilled);
    updateDirtyDay(dueOut.getDay());
  }

  /**
   * Compare two DueOuts for priority.
   * Returns priority(d1) - priority(d2).
   * Remember: priority 0 is high
   * Remember: previouslyFilled is high
   * Remember: 0.0 quantity is low
   * @return the difference in priority of the DueOuts which will be
   * negative if d1 has the lowest priority.
   **/
  private int comparePriority(DueOut d1, DueOut d2) {
    int diff;
    Task t1 = d1.getTask();
    Task t2 = d2.getTask();
    diff = TaskUtils.getNumericPriority(t1) - TaskUtils.getNumericPriority(t2);
    if (diff != 0) return diff;
    diff = (d1.getPreviouslyFilled() ? 1 : 0) - (d2.getPreviouslyFilled() ? 1 : 0);
    if (diff != 0) return diff;
    diff = d2.getDay() - d1.getDay();
    if (diff != 0) return diff;
    double q1 = getRequestedDailyQuantity(t1);
    double q2 = getRequestedDailyQuantity(t2);
    if (q1 != q2) return (q1 < q2) ? -1 : 1;
    return 0;
  }

  private double getRequestedDailyQuantity(Task task) {
    if (TaskUtils.isProjection(task)) {
      return TaskUtils.getDailyQuantity(task);
    } else {
      return TaskUtils.getQuantity(task);
    }
  }


  public void computeThresholdSchedule(int daysOfDemand,
				       int daysForward, 
				       int daysBackward,
				       double minReorderLevel,
				       double maxReorderLevel,
				       double goalLevelMultiplier) {

    minReorderLevel_ = minReorderLevel;
    maxReorderLevel_ = maxReorderLevel;
    goalLevelMultiplier_ = goalLevelMultiplier;

    daysOfDemand_ = daysOfDemand;
    daysForward_ = daysForward;
    daysBackward_ = daysBackward;
	
    computeDemandSchedule();
	
  }


  protected double getTotalValidDueOuts(int day) {
    double result = Double.NaN;
    if(isDueOutValid(day)) 
      result = getDueOutTotal(day);

    if(Double.isNaN(result)) 
      return 0.0;
    else
      return result;
  }
	
    

  protected void computeDemandSchedule() {
    computeDemandSchedule(daysOfDemand_, daysForward_, daysBackward_);
  }
    
  /**
   *  Compute a schedule of the NDaysOfDemand using and averaging
   *  window put the results in the a schedule.
   */


  protected void computeDemandSchedule(int daysOfDemand,
				       int daysForward, 
				       int daysBackward) {

    int days = level_.length;

    double[] newDSched = new double[days];
    int maxDay = dueOut_.size();
    int nDays=0;
    double demand=0.0;
    int totalPeriod = days+daysForward-1;

    ///GLMDebug.DEBUG("InventoryBG::computeDemandSchedule:MWD", "MWD TESTING Start");
    //if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG::computeDemandSchedule:MWD", "Days Forward:" + daysForward + " Days Backward: " + daysBackward + " Days of Demand:" + daysOfDemand + " Max Day: " + maxDay);


    for (int i=0; i< totalPeriod; i++) {

      //currDay is the day around which the window is built
      //i is really the leading edge of the window
      //start is the trailing end.
      int currDay=i-daysForward+1;
      int start=currDay-daysBackward;

	    
      double startDemand = 0.0;
      double endDemand = getTotalValidDueOuts(i);

      if(start >= 0) {
	startDemand = getTotalValidDueOuts(start-1);
      }

      //If the start of the window hasn't come onto the scene
      //just add another day of demand
      if(currDay <= daysBackward) {
	demand += endDemand;
	nDays++;
      }
      else {
	//nominally the new day of demand comes from the right
	//of the window, and the 1st day of window falls off the 
	//left
	demand = demand + (endDemand - startDemand);
      }

      if(currDay >= 0) {
	if(nDays==0) nDays=1;
	//moved daysOfDemand_ to getNDaysOfDemand
	newDSched[currDay] = demand/nDays;
      }
    }

    int len = days;
    if (demandSchedule_ != null) {
      int oldlen = demandSchedule_.length;
      for (int i=0; i < len; i++) {
	if (i < oldlen) {
	  double num = newDSched[i] - demandSchedule_[i];
	  double den = newDSched[i] + demandSchedule_[i];
	  if (den > 0.0) {
	    if (num < 0.0 || Math.abs(num)/den < 0.1) {
	      newDSched[i] = demandSchedule_[i];
	    }
	  }
	}
      }
    }
    demandSchedule_ = newDSched;
  }

  public double getGoalLevel(int day) {

    if(myPG_.getFillToCapacity()) {
      return getDouble(myPG_.getCapacity());
    } else {
      double goal_level= goalLevelMultiplier_ * getReorderLevel(day);
      double capacity = getDouble(myPG_.getCapacity());

      if (goal_level > capacity) {

	if(goalExceededCap == GOAL_EXCEEDED_CAPACITY_DEBUG_THRESHOLD) {
	  GLMDebug.ERROR("InventoryBG", "getGoalLevel()::WARNING the goal level is exceeding the capacity.   It is probable that you should up the capacity in the inv config file.");
	}

	if(goalExceededCap <= GOAL_EXCEEDED_CAPACITY_DEBUG_THRESHOLD) {
	  goalExceededCap++;
	}
      }

      return goal_level;
    }
  }


  public double getNDaysDemand(int day) {
    if (day >= demandSchedule_.length) {
      //	    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG","ARRGH Inventory array level is : " + level_.length);
      throw new IllegalArgumentException("Demand Schedule Index is wrong! index = " + day + " array is length " + demandSchedule_.length);
    }

    return (daysOfDemand_ * demandSchedule_[day]);
  }
	

  public double getReorderLevel(int day) {	

    double rl = getNDaysDemand(day);

    if(minReorderLevel_ > maxReorderLevel_) {
      throw new RuntimeException("Minimum Reorder Level is Greater than Max Reorder Level");
    }

    rl = Math.min(maxReorderLevel_,(Math.max(minReorderLevel_,rl)));

    return rl;
  }

  public int addPreviousRefillsToInventory(Task maintainInv) {
    int totalFilled=0;
    if (maintainInv == null) {
      return 0;
    }
    PlanElement pe = maintainInv.getPlanElement();
    if ((pe != null) && (pe instanceof Expansion)){
      Expansion expansion = (Expansion)pe;
      Workflow wf = expansion.getWorkflow();
      Enumeration tasks = wf.getTasks();
      while (tasks.hasMoreElements()) {
	Task refill = (Task) tasks.nextElement();
	totalFilled += addDueIn(refill);
      }
    }
    return totalFilled;
  }

  private void addDueIn(DueIn dueIn, int day) {
    if (day >= 0) {
      while (day >= dueIns_.size()) {
	dueIns_.add(new DueInList(dueIns_.size()));
      }
      DueInList v = (DueInList) dueIns_.get(day);
      v.add(dueIn);
    } else {
      throw new IllegalArgumentException("InventoryBG: addDueIn(), Refill Starts in the past (start time "
					 + TimeUtils.dateString(getStartTime())
					 + "). Task = "
					 + TaskUtils.taskDesc(dueIn.getTask()));
    }
  }

  public int addDueIn(Task refillTask) {
    if (TaskUtils.isProjection(refillTask)) {
      return addDueInProjection(refillTask);
    }
    else {
      return addDueInSupply(refillTask);
    }
  }

  private void addDueInByRequestDay(DueIn d) {
    int day = convertTimeToDay(TaskUtils.getEndTime(d.getTask()));
    while (day >= dueInsByRequestDay_.size()) {
      dueInsByRequestDay_.add(new Vector(1));
    }
    Vector v = (Vector) dueInsByRequestDay_.elementAt(day);
    v.addElement(d);
  }

  private void removeDueInByRequestDay(Task refillTask) {
    int day = convertTimeToDay(TaskUtils.getEndTime(refillTask));
    if (day < dueInsByRequestDay_.size()) {
      Vector v = (Vector) dueInsByRequestDay_.elementAt(day);
      for (Iterator i = v.iterator(); i.hasNext(); ) {
	DueIn d = (DueIn) i.next();
	if (d.getTask() == refillTask) {
	  i.remove();
	  return;
	}
      }
    }
  }

  private int addDueInSupply(Task refill) {
    int totalFilled = 0;
    boolean filled = true;
    double qty = TaskUtils.getRefillQuantity(refill);
    if (qty == 0.0) {
      filled = false;
    } else {
      filled = true;
      totalFilled++;
    }
    // Add Refill to daily schedule
    // All times set to Midnight of that day
    long refillTime = TaskUtils.getRefillTime(refill);
    int day = convertTimeToDay(refillTime);
    if (day >= 0) {
      DueIn d = new DueIn(refill, filled);
      addDueIn(d, day);
      addDueInByRequestDay(d);
    } else {
      GLMDebug.ERROR("InventoryBG", "addDueIn() on day " + day + ", refillTime=" + TimeUtils.dateString(refillTime));
    }
    return totalFilled;
  }

  private int addDueInProjection(Task task) {
    int totalFilled = 0;
    PlanElement pe = task.getPlanElement();
    if (pe != null) {
      AllocationResultHelper helper = new AllocationResultHelper(task, pe);
      for (int i = 0, n = helper.getPhaseCount(); i < n; i++) {
	AllocationResultHelper.Phase phase = helper.getPhase(i);
	AspectValue av = phase.getAspectValue(AlpineAspectType.DEMANDRATE);
	boolean filled = av.getValue() != 0.0;
	totalFilled += addDueInRange(task, phase.getStartTime(), phase.getEndTime(), filled);
      }
    } else {
      totalFilled += addDueInRange(task, TaskUtils.getStartTime(task), TaskUtils.getEndTime(task), true);
    }
    return totalFilled;
  }

  private int  addDueInRange(Task task, long startTime, long endTime, boolean filled) {
    int totalFilled = 0;
    int start = convertTimeToDay(startTime);
    int end = convertTimeToDay(endTime);
    if (start < 0) { start = 0; }
    for (int day = start; day < end; day++) {
      addDueIn(new DueIn(task, filled), day);
      if (filled) totalFilled++;
    }
    return totalFilled;
  }

  public int removeDueIn(Task refillTask) {
    // Remove Task from local structure
    int day = convertTimeToDay(TaskUtils.getEndTime(refillTask));
    try {
      DueInList v = (DueInList) dueIns_.get(day);
      v.remove(refillTask);
    } catch (ArrayIndexOutOfBoundsException exception) {
      GLMDebug.ERROR("InventoryBG", "removeDueIn, day "+day+": Refill not found. "+TaskUtils.taskDesc(refillTask));
    }
    removeDueInByRequestDay(refillTask);
    return 0;
  }

  public int getPlanningDays() {
    if (level_ == null) {
      determineInventoryLevels();
    }
    return level_.length;
  }

  public int determineInventoryLevels() {
    if (needInitializeInventoryLevels) {
      initializeInventoryLevels();
      needInitializeInventoryLevels = false;
    }
    return 0;
  }

  protected void initializeInventoryLevels() {
    int today = getToday();
    int size = today + 1;  // Always include tomorrow in the levels.
    double reported_levels[] = getTimeOrderedReportedLevels();
    if (dueIns_.size() > size) size = dueIns_.size();
    if (dueOut_.size() > size) size = dueOut_.size();
    if (reported_levels.length > size) size = reported_levels.length;

    if (level_ == null || level_.length < size) {
      double []replacementLevel = new double[size];

      //Don't lose info that's already been computed.
      if (level_ != null) {
        System.arraycopy(level_, 0, replacementLevel, 0, level_.length);
      } 

      level_ = replacementLevel;      
      computeDemandSchedule();
    }

    /* Note that there is always at least one item in
       reported_levels so dirtyDay_ will always end up being
       greater than zero */
    for (int i = dirtyDay_, n = reported_levels.length; i < n; i++) {
      double lvl = reported_levels[i];
      if (!Double.isNaN(lvl)) {
	level_[i] = reported_levels[i];
	dirtyDay_ = i + 1;
      }
    }
  }

  private void updateLevels(int day) {
    double lvl = level_[dirtyDay_ - 1]; // dirtyDay_ always > 0 (see above)
    for (int i = dirtyDay_; i <= day; i++) {
      double dueout = getDueOutTotal(i);
      double duein = getDueInTotal(i);
      lvl += duein - dueout;
      level_[i] = lvl;
    }
    dirtyDay_ = day + 1;
  }

  private boolean isDueOutValid(int day) {
    if (day < 0) return false;
    if (day >= dueOut_.size()) return false;
    return true;
  }

  private double getDueOutTotal(int day) {
    if (!isDueOutValid(day)) return 0.0;
    DueOutList dueOuts = (DueOutList) dueOut_.get(day);
    return dueOuts.getTotal();
  }

  public Scalar getProjected(int day) {
    if (!isDueOutValid(day))  {
      return null;
    }
    DueOutList dueOuts = (DueOutList) dueOut_.get(day);
    return getScalar(dueOuts.getProjectedTotal());
  }

  public Scalar getProjectedRefill(int day) {
    if (!isDueInValid(day)) return getScalar(0.0);
    DueInList dueIns = (DueInList) dueIns_.get(day);
    return getScalar(dueIns.getProjectedTotal());
  }

  private double getWeightingFactor(Task task, int imputedDay) {
    if (weight_ != null) {
      return weight_.getProjectionWeight(task, imputedDay);
    }
    else {
      GLMDebug.ERROR("InventoryBG", null, "getWeightingFactor(), Weighting Factor NOT set.");
      return 0.0;
    }
  }

  private boolean isDueInValid(int day) {
    if (day < 0) return false;
    if (day >= dueIns_.size()) return false;
    return true;
  }

  private double getDueInTotal(int day) {
    if (!isDueInValid(day)) return 0.0;
    DueInList dueIns = (DueInList) dueIns_.get(day);
    return dueIns.getTotal();
  }

  public Task refillAlreadyFailedOnDay(int day) {
    Enumeration dueins = null;
    if (day < dueInsByRequestDay_.size()) {
      Vector v = (Vector) dueInsByRequestDay_.get(day);
      dueins = v.elements();
    }
    if (dueins != null) {
      while (dueins.hasMoreElements()) {
	DueIn d = (DueIn) dueins.nextElement();
	Task task = d.getTask();
	if (task.getVerb().equals(Constants.Verb.SUPPLY)) {
	  if (!d.getFilled()) return task;
	}
      }
    }
    return null;
  }

  /**
     Get _the_ refill for a particular day. This assumes that only
     one refill exists for a particular day. In fact, we return the
     smallest refill for a particular day.
  **/
  public Task getRefillOnDay(int day) {
    Enumeration dueins = null;
    if (day < dueInsByRequestDay_.size()) {
      Vector v = (Vector) dueInsByRequestDay_.get(day);
      dueins = v.elements();
    }
    if (dueins != null) {
      Task smallestRefill = null;
      double smallestQuantity = Double.POSITIVE_INFINITY;
      while (dueins.hasMoreElements()) {
	DueIn d = (DueIn) dueins.nextElement();
	Task task = d.getTask();
	if (task.getVerb().equals(Constants.Verb.SUPPLY)) {
	  double q = TaskUtils.getRefillQuantity(task);
	  if (q < smallestQuantity) {
	    smallestQuantity = q;
	    smallestRefill = task;
	  }
	}
      }
      return smallestRefill;
    }
    return null;
  }

  public void removeRefillProjection(int day) {
    if (!isDueInValid(day)) return; // Nothing to remove
    DueInList dueIns = (DueInList) dueIns_.get(day);
    dueIns.clear();
  }

  public Date lastDemandTaskEnd(Inventory inventory) {
    long latest_time = -1;
    long end_time;
    Task task;
    Enumeration role_sched = inventory.getRoleSchedule().getRoleScheduleElements();
    while (role_sched.hasMoreElements()) {
      PlanElement pe = (PlanElement)role_sched.nextElement();
      task = pe.getTask();
      latest_time = Math.max(latest_time, TaskUtils.getRefillTime(task));
    }
    if (latest_time < 0) return null;
    latest_time = canonicalTime(latest_time);
    return new Date(latest_time);
  }

  public Integer getFirstOverflow(int i, MessageAddress cluster) {
    if (level_ == null)
      determineInventoryLevels();
    int size = level_.length;
    double capacity = getDouble(myPG_.getCapacity());
    Integer day = null;
    if ((i > size) || (i < 0))
      return null;
    for (; i < size; i++) {
      //  	    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG()", cluster, "getFirstOverflow(), checking for overflow on "+
      //  			   (TimeUtils.dateString(TimeUtils.addNDays(getStartTime(), i)))+" Level: "+level_[i]+" capacity: "+capacity);
      if (Math.floor(level_[i] - capacity) > 1.0) {
	//Put in printDebug statement because you put together the
	// strings whether you print them or not which can get
	// costly performance wise.
	if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG()", cluster, "getFirstOverflow(), OVERFLOW on "+
						 (TimeUtils.dateString(convertDayToTime(i))));
	return new Integer(i);
      }
    }
    return null;
  }

  public int updateContentSchedule(Inventory inventory) {
    ScheduledContentPG scp = inventory.getScheduledContentPG();
    QuantityScheduleElement qse;
    Vector new_elements = new Vector();
    if (level_ == null) {
      determineInventoryLevels();
    }
    if (level_ == null) {
      if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "UpdateContentSchedule(), No DueOuts or DueIns, resetting schedule.");
      clearContentSchedule(inventory);
    } else {
      int days = level_.length;
      updateLevels(days - 1); // Be sure we have all the days computed.
      //  	    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "UpdateContentSchedule(), Creating new Schedule for "+AssetUtils.assetDesc(inventory));
      for (int i = 0; i < days; i++) {
	// start time is eigher today or time 1st request was received + number of planning days
	long start = TimeUtils.addNDaysTime(getStartTime(), i);
	// end time is a millisecond before start time of next day
	long end = TimeUtils.addNDaysTime(getStartTime(), (i+1));
	try {
	  qse = ScheduleUtils.buildQuantityScheduleElement(level_[i], start, end);
	} catch (IllegalArgumentException iae) {
	  iae.printStackTrace();
	  continue;
	}
//  		if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "UpdateContentSchedule(), Start:"+TimeUtils.dateString(start)+
//  			       ", End:"+TimeUtils.dateString(end)+", Qty: "+level_[i]);
	new_elements.add(qse);
      }
    }
    Schedule new_schedule = GLMFactory.newQuantitySchedule(new_elements.elements(), 
							   PlanScheduleType.TOTAL_INVENTORY);

    if (ScheduleUtils.isOffendingSchedule(new_schedule)) {
      GLMDebug.ERROR("InventoryBG", "UpdateContentSchedule(),  CREATED BAD SCHEDULE ");
      printQuantityScheduleTimes(new_schedule);
    }
    ((NewScheduledContentPG)scp).setSchedule(new_schedule);
    return 0;
  }


  public int updateInventoryLevelsSchedule(Inventory inventory) {
    InventoryLevelsPG ilp = inventory.getInventoryLevelsPG();
    QuantityScheduleElement dse, rlse, glse;
    Vector new_av_demand_elements = new Vector();
    Vector new_reorder_elements = new Vector();
    Vector new_goal_elements = new Vector();
    if (demandSchedule_ == null) {
      if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "UpdateInventoryLevelsSchedule(), No Demand Schedule Set.");
      clearInventoryLevelsSchedule(inventory);
    }
    else {
      int days = demandSchedule_.length;
//  	    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "UpdateInventoryLevelsSchedule(), Creating new Schedule for "+AssetUtils.assetDesc(inventory));
      for (int i = 0; i < days; i++) {
	// start time is eigher today or time 1st request was received + number of planning days
	long start = TimeUtils.addNDaysTime(getStartTime(), i);
	// end time is a millisecond before start time of next day
	long end = TimeUtils.addNDaysTime(getStartTime(), (i+1));
	try {
	  dse = ScheduleUtils.buildQuantityScheduleElement(demandSchedule_[i], start, end);
	  rlse = ScheduleUtils.buildQuantityScheduleElement(getReorderLevel(i), start, end);
	  glse = ScheduleUtils.buildQuantityScheduleElement(getGoalLevel(i), start, end);
	} catch (IllegalArgumentException iae) {
	  iae.printStackTrace();
	  continue;
	}
//  	if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "UpdateInventoryLevelschedule(), Start:"+TimeUtils.dateString(start)+
//  			       ", End:"+TimeUtils.dateString(end)+", Qty: "+level_[i]);
	new_av_demand_elements.add(dse);
	new_reorder_elements.add(rlse);
	new_goal_elements.add(glse);
      }
    }
    Schedule new_av_demand_schedule = 
      GLMFactory.newQuantitySchedule(new_av_demand_elements.elements(), 
				     PlanScheduleType.TOTAL_INVENTORY);
    Schedule new_reorder_schedule = 
      GLMFactory.newQuantitySchedule(new_reorder_elements.elements(), 
				     PlanScheduleType.TOTAL_INVENTORY);
    Schedule new_goal_schedule = 
      GLMFactory.newQuantitySchedule(new_goal_elements.elements(), 
				     PlanScheduleType.TOTAL_INVENTORY);



    if (ScheduleUtils.isOffendingSchedule(new_av_demand_schedule)) {
      GLMDebug.ERROR("InventoryBG", "UpdateInventoryLevelschedule(),  CREATED BAD nDays SCHEDULE  ");
      printQuantityScheduleTimes(new_av_demand_schedule);
    }
    if (ScheduleUtils.isOffendingSchedule(new_reorder_schedule)) {
      GLMDebug.ERROR("InventoryBG", "UpdateInventoryLevelschedule(),  CREATED BAD reorder SCHEDULE  ");
      printQuantityScheduleTimes(new_reorder_schedule);
    }
    if (ScheduleUtils.isOffendingSchedule(new_goal_schedule)) {
      GLMDebug.ERROR("InventoryBG", "UpdateInventoryLevelschedule(),  CREATED BAD goal SCHEDULE  ");
      printQuantityScheduleTimes(new_goal_schedule);
    }
    ((NewInventoryLevelsPG)ilp).setAverageDemandSchedule(new_av_demand_schedule);
    ((NewInventoryLevelsPG)ilp).setReorderLevelSchedule(new_reorder_schedule);
    ((NewInventoryLevelsPG)ilp).setGoalLevelSchedule(new_goal_schedule);
    return 0;
  }

  public int updateDetailedContentSchedule(Inventory inventory) {
    if (!isStartTimeSet)
      return -1;
    if (level_ == null) {
      determineInventoryLevels();
    }
    if (level_ == null) 
      return -1;
//      System.out.println(">>>UpdateDetailedContentSchedule ");
    int days = level_.length;
    updateLevels(days - 1); // Be sure we have all the days computed.
    Vector quantity_schedule_elements = new Vector();
    for (int i = 0; i < days; i++) {
      quantity_schedule_elements = createDailyScheduleElements(quantity_schedule_elements, i);
    }
    Schedule sched = GLMFactory.newQuantitySchedule(quantity_schedule_elements.elements(), PlanScheduleType.TOTAL_INVENTORY);
    if (ScheduleUtils.isOffendingSchedule(sched)) {
      GLMDebug.ERROR("InventoryBG", "UpdateContentSchedule(),  CREATED BAD SCHEDULE ");
    }
    NewDetailedScheduledContentPG newDetailedPG =  
      org.cougaar.glm.ldm.asset.PropertyGroupFactory.newDetailedScheduledContentPG();
    newDetailedPG.setSchedule(sched);
    inventory.setPropertyGroup(newDetailedPG);
    printDetailedSchedule(sched);
    return 0;
  }

  private Vector createDailyScheduleElements(Vector quantity_schedule_elements, int day) {
    long start = TimeUtils.addNDaysTime(getStartTime(), day);
    long end = TimeUtils.addNDaysTime(getStartTime(), day+1);
    double current_level;
    if (day == 0) {
      current_level = getDouble(myPG_.getInitialLevel());
    } else {
      current_level = level_[day-1];
    }
    ArrayList activityToday = new ArrayList();
    double projectedDueIns = 0;
    DueInList dil = null;
    if (dueIns_.size() > day) {
      dil = (DueInList)dueIns_.get(day);
      if ((dil != null) && (dil.size() > 0)) {
	activityToday.addAll(dil.getSupplyTasks());
	projectedDueIns = dil.getProjectedTotal();
      }
    }
    double projectedDueOuts = 0;
    DueOutList dol = null;
    if (dueOut_.size() > day) {
      dol = (DueOutList)dueOut_.get(day);
      if ((dol != null) && (dol.size() > 0)) {
	activityToday.addAll(dol.getWithdrawTasks());
	projectedDueOuts = dol.getProjectedTotal();
      }
    }
	
    current_level += projectedDueIns;
    current_level -= projectedDueOuts;
    quantity_schedule_elements.add(ScheduleUtils.buildQuantityScheduleElement(current_level, start, end));
    
    return accountForDailyTasks(quantity_schedule_elements, activityToday);
   
  }

  private Vector accountForDailyTasks(Vector quantity_schedule_elements, ArrayList activityToday) {

    if (!activityToday.isEmpty()) {
      Task[] ordered_tasks = (Task[])activityToday.toArray(new Task[activityToday.size()]);
      java.util.Arrays.sort(ordered_tasks, new Comparator() {
	  public int compare(Object o1, Object o2) {
	    long t1_end_time = TaskUtils.getEndTime((Task)o1);
	    long t2_end_time = TaskUtils.getEndTime((Task)o2);
	    long diff = t1_end_time - t2_end_time;
	    if (diff < 0L) return -1;
	    if (diff > 0L) return  1;
	    return 0;
	  }
	});
      long time;
      int size = ordered_tasks.length;
      for (int k=0; k < size; k++) 
	System.out.println(">>>        "+TaskUtils.shortTaskDesc(ordered_tasks[k]));
      for (int i=0; i < size; i++) {
	time = TaskUtils.getEndTime(ordered_tasks[i]);
	QuantityScheduleElement qse = 
	  (QuantityScheduleElement)quantity_schedule_elements.get(quantity_schedule_elements.size()-1);
	long previous_start = qse.getStartTime();
	long previous_end = qse.getEndTime();
	double level = qse.getQuantity();
	if (time > previous_start) {
	  quantity_schedule_elements.set(quantity_schedule_elements.size()-1,
					 ScheduleUtils.buildQuantityScheduleElement(level, previous_start, time));
	  if (ordered_tasks[i].getVerb().equals(Constants.Verb.SUPPLY)) 
	    level += TaskUtils.getRefillQuantity(ordered_tasks[i]);
	  else
	    level -= TaskUtils.getWithdrawQuantity(ordered_tasks[i]);
	  quantity_schedule_elements.add(ScheduleUtils.buildQuantityScheduleElement(level, time, previous_end));
	} else { // time is equal to old start time
	  if (ordered_tasks[i].getVerb().equals(Constants.Verb.SUPPLY)) 
	    level += TaskUtils.getRefillQuantity(ordered_tasks[i]);
	  else
	    level -= TaskUtils.getWithdrawQuantity(ordered_tasks[i]);
	  ((NewQuantityScheduleElement)qse).setQuantity(level);
	}
      }
    }
    return quantity_schedule_elements;
  }

  private void printDetailedSchedule(Schedule sched) {
    Enumeration e = sched.getAllScheduleElements();
    QuantityScheduleElement qse;
    while (e.hasMoreElements()) {
      qse = (QuantityScheduleElement)e.nextElement();
      double qty = qse.getQuantity();
      long start = qse.getStartTime();
      long end = qse.getEndTime();
      System.out.println("           Start: "+TimeUtils.dateString(start)+
			 " End: "+TimeUtils.dateString(end)+
			 " Quantity: "+qty);
    }
  }

  public int clearContentSchedule(Inventory inventory) {
    if (!isStartTimeSet) return -1;
    ScheduledContentPG scp = inventory.getScheduledContentPG();
    Schedule sched = scp.getSchedule();
    QuantityScheduleElement qse;
    Vector new_elements = new Vector();
    long start = TimeUtils.addNDaysTime(getStartTime(), 0);
    long end = TimeUtils.addNDaysTime(getStartTime(), 30);
	
    qse = ScheduleUtils.buildQuantityScheduleElement(getDouble(myPG_.getInitialLevel()), start, end);
    new_elements.add(qse);
    Schedule new_schedule = GLMFactory.newQuantitySchedule(new_elements.elements(), 
							   PlanScheduleType.TOTAL_INVENTORY);

    if (ScheduleUtils.isOffendingSchedule(new_schedule)) {
      GLMDebug.ERROR("InventoryBG", "UpdateContentSchedule(),  CREATED BAD SCHEDULE ");
      printQuantityScheduleTimes(new_schedule);
    }
    ((NewScheduledContentPG)scp).setSchedule(new_schedule);
    return 0;
 
  }


  public int clearInventoryLevelsSchedule(Inventory inventory) {
    if (!isStartTimeSet) return -1;
    InventoryLevelsPG ilp = inventory.getInventoryLevelsPG();
    QuantityScheduleElement dse,rlse,glse;
    Vector new_av_demand_elements = new Vector();
    Vector new_reorder_elements = new Vector();
    Vector new_goal_elements = new Vector();
    long start = TimeUtils.addNDaysTime(getStartTime(), 0);
    long end = TimeUtils.addNDaysTime(getStartTime(), 30);
	
    dse = ScheduleUtils.buildQuantityScheduleElement(0, start, end);
    rlse = ScheduleUtils.buildQuantityScheduleElement(0, start, end);
    glse = ScheduleUtils.buildQuantityScheduleElement(0, start, end);
    new_av_demand_elements.add(dse);
    new_reorder_elements.add(rlse);
    new_goal_elements.add(glse);

    Schedule new_av_demand_schedule = 
      GLMFactory.newQuantitySchedule(new_av_demand_elements.elements(), 
				     PlanScheduleType.TOTAL_INVENTORY);
    Schedule new_reorder_schedule = 
      GLMFactory.newQuantitySchedule(new_reorder_elements.elements(), 
				     PlanScheduleType.TOTAL_INVENTORY);
    Schedule new_goal_schedule = 
      GLMFactory.newQuantitySchedule(new_goal_elements.elements(), 
				     PlanScheduleType.TOTAL_INVENTORY);

    if (ScheduleUtils.isOffendingSchedule(new_av_demand_schedule)) {
      GLMDebug.ERROR("InventoryBG", "clearInventoryLevelSchedule(),  CREATED BAD nDays SCHEDULE  ");
      printQuantityScheduleTimes(new_av_demand_schedule);
    }
    if (ScheduleUtils.isOffendingSchedule(new_reorder_schedule)) {
      GLMDebug.ERROR("InventoryBG", "clearInventoryLevelSchedule(),  CREATED BAD reorder SCHEDULE  ");
      printQuantityScheduleTimes(new_reorder_schedule);
    }
    if (ScheduleUtils.isOffendingSchedule(new_goal_schedule)) {
      GLMDebug.ERROR("InventoryBG", "clearInventoryLevelSchedule(),  CREATED BAD goal SCHEDULE  ");
      printQuantityScheduleTimes(new_goal_schedule);
    }
    ((NewInventoryLevelsPG)ilp).setAverageDemandSchedule(new_av_demand_schedule);
    ((NewInventoryLevelsPG)ilp).setReorderLevelSchedule(new_reorder_schedule);
    ((NewInventoryLevelsPG)ilp).setGoalLevelSchedule(new_goal_schedule);

    return 0;
 
  }



  public int printQuantityScheduleTimes(Schedule sched) {
    Enumeration elements = sched.getAllScheduleElements();
    QuantityScheduleElement qse;

    while (elements.hasMoreElements()) {
      qse = (QuantityScheduleElement)elements.nextElement();
      if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "printQuantityScheduleTimes()    qty: "+qse.getQuantity()+
					       " "+qse.getStartTime()+" to "+ qse.getEndTime());
    }
    return 0;
  }

  public PGDelegate copy(PropertyGroup pg) {
    return new InventoryBG((InventoryPG)pg);
  }
    
  /**
   * Add the newInventoryReport to the report_history ArrayList
   **/
  public void addInventoryReport(InventoryReport newInventoryReport) {
    synchronized (this) {
      report_history.add(newInventoryReport);
      if (latest_report == null
	  || latest_report.theReportDate < newInventoryReport.theReportDate) {
	latest_report = newInventoryReport;
      }
      if (oldest_report == null
	  || oldest_report.theReportDate > newInventoryReport.theReportDate) {
	oldest_report = newInventoryReport;
      }
    }
    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "addInventoryReport(), Report added!!!!!!! : "+this.hashCode());
  }

  /**
   * Remove inventory reports older than a specified time. Never removes the latest report.
   **/
  public void pruneOldInventoryReports(long pruneTime) {
    synchronized (this) {
      for (Iterator i = report_history.iterator(); i.hasNext(); ) {
	InventoryReport report = (InventoryReport) i.next();
	if (pruneTime > report.theReportDate && report != latest_report) {
	  i.remove();
	}
      }
      oldest_report = null; // We probably removed this, recompute when asked
    }
  }

  /**
   * Returns the report_history ArrayList
   **/
  private ArrayList getInventoryReportHistory() {
    synchronized (this) {
      return report_history;
    }
  }

  /**
   * Returns the InventoryReport with the latest date
   **/
  public InventoryReport getLatestInventoryReport() {
    synchronized (this) {
      if (latest_report == null) { // This is almost always up-to-date, but in case it isn't...
	for (Iterator i = report_history.iterator(); i.hasNext(); ) {
	  InventoryReport report = (InventoryReport) i.next();
	  if (latest_report == null
	      || latest_report.theReportDate < report.theReportDate) {
	    latest_report = report;
	  }
	}
      }
      return latest_report;
    }
  }

  /**
   * Returns the InventoryReport with the oldest date
   **/
  public InventoryReport getOldestInventoryReport() {
    synchronized (this) {
      if (oldest_report == null) { // This is almost always up-to-date, but in case it isn't...
	for (Iterator i = report_history.iterator(); i.hasNext(); ) {
	  InventoryReport report = (InventoryReport) i.next();
	  if (oldest_report == null
	      || oldest_report.theReportDate > report.theReportDate) {
	    oldest_report = report;
	  }
	}
      }
      return oldest_report;
    }
  }

  /**
   * Returns a time-ordered list of known InventoryReports
   **/
  private double[]  getTimeOrderedReportedLevels() {
    double ordered_list[];
    InventoryReport[] reports;
    synchronized (this) {   // Must be synchronized so reports cannot change while we process them
      List list = getInventoryReportHistory();
      int size = list.size();
      reports = (InventoryReport[]) list.toArray(new InventoryReport[size]);
    }
    // No Reports - use PG for initial inventory level
    if (reports.length == 0) {
      ordered_list = new double[1];
      ordered_list[0] = getDouble(myPG_.getInitialLevel());
      //  	    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), List is empty.");
    } else {
      //  	    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), Sorting Report list.");
      // Sort the Reports by time in ascending order
      // Yah, it's inefficient but it's only a few lines of code and there should never
      // be more than 5 items in this list
      java.util.Arrays.sort(reports, new Comparator() {
	  public int compare(Object o1, Object o2) {
	    InventoryReport r1 = (InventoryReport) o1;
	    InventoryReport r2 = (InventoryReport) o2;
	    long diff = r1.theReportDate - r2.theReportDate;
	    if (diff < 0L) return -1;
	    if (diff > 0L) return  1;
	    return 0;
	  }
	});
      long latest = reports[reports.length - 1].theReportDate;

      // create a double[] containing inventory levels indexed by day
      int reportDay = convertTimeToDay(latest);
      int len = reportDay + 1;
      if (len <= 0) {
	len = 1;
      }
      ordered_list = new double[len];
      java.util.Arrays.fill(ordered_list, Double.NaN);
      ordered_list[0] = getDouble(myPG_.getInitialLevel());
      for (int i = 0, n = reports.length; i < n; i++) {
	long report_date = reports[i].theReportDate;
	int day = convertTimeToDay(report_date);
	// Last Report that was received before or on day 0 becomes initial value
	if (day < 0) {
	  day = 0;
	}
	ordered_list[day] = reports[i].theQuantity;
	if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), Report day is "+day+
						 "("+TimeUtils.dateString(report_date)+"), Value is: "+ordered_list[day]);
      }
    }
    return ordered_list;
  }

  private static final Volume zeroVolume = new Volume(0, Volume.GALLONS);
  private static final Count zeroCount = new Count(0, Count.EACHES);
  private static final Mass zeroMass = new Mass(0, Mass.SHORT_TONS);

  /** 
   * Given a double, create scalar apropriate for this type of inventory 
   **/
  private Scalar getScalar(double amt) {
    Scalar measure = myPG_.getInitialLevel();
    if (measure instanceof Volume) {
      if (amt == 0.0)
	return zeroVolume;
      else
	return new Volume(amt, Volume.GALLONS);
    }
    if (measure instanceof Count) {
      if (amt == 0.0)
	return zeroCount;
      else
	return new Count(amt, Count.EACHES);
    }
    if (measure instanceof Mass) {
      if (amt == 0.0)
	return zeroMass;
      else
	return new Mass(amt, Mass.SHORT_TONS);
    }
    throw new RuntimeException("InventoryBG: Unknown type of initial level");
  }
				       
  /** 
   * Given a Scalar, return a double value representing
   * Gallons for Volume,
   * Eaches for Count and
   * Short Tons for Mass.
   **/
  private double getDouble(Scalar measure) {
    double result = Double.NaN;
    if (measure instanceof Volume) {
      result = ((Volume)measure).getGallons();
    } else if (measure instanceof Count) {
      result = ((Count)measure).getEaches();
    } else if (measure instanceof Mass) {
      result = ((Mass)measure).getShortTons();
    } else {
      GLMDebug.ERROR("InventoryBG", "getDouble(), Inventory cannot determine type of measure");
    }
    return result;
  }	

  public Enumeration getAllDueIns(){
    Hashtable dueInTaskHT = new Hashtable(500);
    dueInTasks_=new Vector(500);
    int size = dueIns_.size();
    Vector div;
    for (int i=0; i < size; i++) {
      div =  (Vector)(dueIns_.get(i));
      for (int j=0; j < div.size(); j++){
	if(dueInTaskHT.get(((DueIn)div.get(j)).getTask())==null){
	  dueInTaskHT.put(((DueIn)div.get(j)).getTask(),((DueIn)div.get(j)));
	  dueInTasks_.add(((DueIn)div.get(j)).getTask().getPlanElement());
	}
      }
    }
    return dueInTasks_.elements();
  }



  public int printInventoryLevels(Inventory inventory, MessageAddress clusterID) {
    if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG()", clusterID, "printInventoryLevels(), Day 0 is "+TimeUtils.dateString(getStartTime()));
    if (level_ != null) {
      int size = level_.length;
      ScheduledContentPG scpg = inventory.getScheduledContentPG();
      if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG", clusterID, "printInventoryLevels(), for "+AssetUtils.assetDesc(scpg.getAsset()));

      for (int i=0; i < size; i++) {
	if(GLMDebug.printDebug()) GLMDebug.DEBUG("InventoryBG()", clusterID, "printInventoryLevels(), day: "+
						 TimeUtils.dateString(TimeUtils.addNDays(getStartTime(), i))+"("+i+") "+", level: "+level_[i]);
      }
    }
    return 0;
  }
}
