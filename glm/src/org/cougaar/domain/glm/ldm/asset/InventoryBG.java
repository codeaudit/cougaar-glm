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
package org.cougaar.domain.glm.ldm.asset;

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
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.execution.common.InventoryReport;
import org.cougaar.domain.glm.ldm.ALPFactory;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.domain.glm.ldm.plan.PlanScheduleType;
import org.cougaar.domain.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;

public abstract class InventoryBG implements PGDelegate {

    public static final int PRIORITY_LEVELS = 10;

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
    // Was the BG properly initialized, if not, it is unusable
    protected boolean initialized_ = false;
    // today is the alp day right now
    protected long today_;
    private long startTime_;
    private boolean startTimeNeverSet = true;
    protected long firstDayOfDemand_;
    private ProjectionWeight weight_;
    // Stuff for execution below
    protected ArrayList report_history;
    protected InventoryReport latest_report;
    protected InventoryReport oldest_report;
    protected Calendar calendar_ = Calendar.getInstance();
    
    public InventoryBG(InventoryPG  pg) {
	myPG_ = pg;
	dueIns_ = new Vector();
	dueOut_ = new Vector();
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
	level_ = null;
    }

    // GLK temp changed void to int
    public int resetInventory(Inventory inventory, long today) {
	dueOut_.clear();
	dueIns_.clear();
	level_ = null;

	initializeTime(inventory, today);
//      	GLMDebug.DEBUG("InventoryBG", null, " resetInventory():"+inventory+
//    		       " Start Time: "+TimeUtils.dateString(getStartTime())+", Today: "+
//      		       TimeUtils.dateString(today_)+"("+getToday()+")"+", 1st day of demand "+
//      		       TimeUtils.dateString(firstDayOfDemand_), 1000);
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
       Try to do a reasonable job setting the startTime_ and
       firstDayOfDemand_ variables. If there is demand, then the start
       time is set 14 days before the earliest demand. If there is no
       demand, but there are inventory reports, the start time is set
       the time of the earliest inventory report. Otherwise, the start
       time is not set, but getStartTime() will return today. If there
       is no demand the firstDayOfDemand is set to 14 days after the
       start time (which defaults to today, if it has not been set).
       Otherwise, it is set to the time of the earliest demand. In all
       cases, both the start time and the first day of demand are
       pushed to the end of the day in which they fall.
     **/
    private void initializeTime(Inventory inventory, long today) {
	try {
	    // Today is AlpTime, i.e. now
            today_ = TimeUtils.pushToEndOfDay(calendar_, today);
            Enumeration role_sched = inventory.getRoleSchedule().getRoleScheduleElements();
            long earliest_demand = Long.MAX_VALUE;
            // StartTime_ is day 0 for this inventory. It is set to the
            // earliest of 14 days before earliest demand or day of oldest
            // inventory report.
            // firstDayOfDemand is the day on which the IM receives its first request.
            while (role_sched.hasMoreElements()) {
                PlanElement pe = (PlanElement)role_sched.nextElement();
                Task dueOut = pe.getTask();
                try {
                    long time = TaskUtils.getStartTime(dueOut);
                    earliest_demand = Math.min(time, earliest_demand);
                } catch (RuntimeException re) {
                    continue;   // No start time, probably
                }
            }
            InventoryReport oldest = getOldestInventoryReport();
            long start_time = Long.MAX_VALUE;
            if (earliest_demand != Long.MAX_VALUE) {
                start_time = earliest_demand - 14 * TimeUtils.MSEC_PER_DAY;
            }
            if (oldest != null) {
                start_time = Math.min(start_time, oldest.theReportDate);
            }
            if (start_time != Long.MAX_VALUE) {
                setStartTime(TimeUtils.pushToEndOfDay(calendar_, start_time));
            }
            if (earliest_demand == Long.MAX_VALUE) {
                earliest_demand = getStartTime() + 14 * TimeUtils.MSEC_PER_DAY;
            }
            firstDayOfDemand_ = TimeUtils.pushToEndOfDay(calendar_, earliest_demand);
        } catch (RuntimeException re) {
          System.err.print(re + ": today=" + today + ", today_ = " + today_);
          throw re;
        }
    }

    private void setStartTime(long newStartTime) {
      //System.out.println("setStartTime " + TimeUtils.dateString(newStartTime));
        if (newStartTime <= 0L) {
            throw new IllegalArgumentException("Bogus newStartTime: " + newStartTime);
        }
        startTime_ = newStartTime;
        startTimeNeverSet = false;
    }

    public long getStartTime() {
        if (startTimeNeverSet){
	    //throw new RuntimeException("startTime_ never set");
	    return today_;
	} else {	
	    return startTime_;
	}
    }
    public int getToday() {
	// Be careful with this one.  Obviously, if the inventory startime is after today
	// then a negative number is returned.  This method should only be used to determine
	// if the current day is before or after inventory start time.
	return TimeUtils.getDaysBetween(getStartTime(), today_);
    }

    public int getFirstPlanningDay() {
	// The first planning day is the first day of demand unless that day falls in the past,
	// in which case the first planning day is today.
	int day;
	if (firstDayOfDemand_ > today_) {
	    // Activity starts in the future, 1st planning day is the first day of demand
	    day = TimeUtils.getDaysBetween(getStartTime(), firstDayOfDemand_);
	}
	else {
	    // Activity started in the past, 1st planning day is now
	    day = TimeUtils.getDaysBetween(getStartTime(), today_);
	}
	return day;
    }

    /**
     * Get the inventory level at a particular execution time
     **/
    public Scalar getLevel(long day) {
	int d = TimeUtils.getDaysBetween(day, getStartTime());
	return getLevel(d);
    }

    /**
     * Get inventory level on a particular day relative to the start
     * time of the inventory. If the specified day is before the start
     * time of the inventory, the initial level is returned. If after
     * the maximum day, the level on that maximum day is returned.
     **/
    public Scalar getLevel(int day) {
	Scalar initial = myPG_.getInitialLevel();
	if ((day < 0) || (level_ == null)) {
	    return initial;
	}
	if  (day >= level_.length) {
	    day = level_.length-1;
	}
	return getScalar(level_[day]);
    }

    public int withdrawFromInventory(Inventory inventory, ClusterIdentifier clusterID) {
	int requests=0;
	Enumeration role_sched = inventory.getRoleSchedule().getRoleScheduleElements();
	while (role_sched.hasMoreElements()) {
	    PlanElement pe = (PlanElement)role_sched.nextElement();
	    Task dueOut = pe.getTask();
// 	    GLMDebug.DEBUG("InventoryBG", "withdrawFromInventory() "+TaskUtils.taskDesc(dueOut));
//  	    GLMDebug.DEBUG("InventoryBG", clusterID, "withdrawFromInventory(), "+TaskUtils.taskDesc(dueOut));
	    if (dueOut.getVerb().equals(Constants.Verb.WITHDRAW)) {
		addDueOut(dueOut);
		requests++;
	    } else if (dueOut.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) {
		addDueOutProjection(dueOut);
	    } else {
		System.out.println("What the .... Task added to role schedule "+TaskUtils.taskDesc(dueOut));
	    }
	}
// 	GLMDebug.DEBUG("InventoryBG", "withdrawFromInventory(), "+requests+" requests from "+AssetUtils.assetDesc(inventory));
	return 0;
    }

    private void addDueOut(Task request) {
// 	long time = TaskUtils.getEndTime(request);
 	long time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getStartTime(request));
	int day = TimeUtils.getDaysBetween(getStartTime(), time);
	if (day <  0) {
	    day = 0;
	    GLMDebug.ERROR("InventoryBG", "addDueOut(), Request Starts in the past.(Start Time "+
			   TimeUtils.dateString(getStartTime())+")" +"Task pteod start:"+
			   TimeUtils.dateString(time)+", start"+
			   TimeUtils.dateString(TaskUtils.getStartTime(request))+
			   ", Task: "+TaskUtils.taskDesc(request));
	}
	while (day >= dueOut_.size()) {
	    dueOut_.add(new Vector());
	}
//  	if (day >= dueOut_.size()) {
//  	    // Increase size of dueOut_ vector to accommodate days
//  	    for (int i=dueOut_.size()-1; i<=day; i++) {
//  		dueOut_.add(new Vector());
//  	    }
//  	}
//     	GLMDebug.DEBUG("InventoryBG", "addDueOut(), Adding dueout on day "+day+"("+TimeUtils.dateString(time)+")"+TaskUtils.taskDesc(request));
	Vector v = (Vector)dueOut_.get(day);
	PlanElement pe = request.getPlanElement();
	// If the request has just been rescinded, the plan element will be null.
	// This task should not affect planning
	if (pe == null) 
	    return;
	AllocationResult ar = pe.getEstimatedResult();
	if(ar==null){
	    GLMDebug.ERROR("addDueOut()", TaskUtils.taskDesc(request)+
			   " allocation without estimated result: "+pe.getUID());
	} else if (!ar.isSuccess()) {
	    v.add(new DueOut(request, false));
// 	    GLMDebug.DEBUG("InventoryBG", "addDueOut, TaskUtils.taskDesc(t)+" failed prev run");
	}
	else {
	    v.add(new DueOut(request, true));
// 	    GLMDebug.DEBUG("InventoryBG", "addDueOut, TaskUtils.taskDesc(t)+" Succeeded previous run or new task");
	}
    }

    // Looking for an priority ordered list of dueouts from today to the given day.
    public DueOut getLowestPriorityDueOutBeforeDay(int end) {
	// Also includes this day
	int today = TimeUtils.getDaysBetween(getStartTime(), today_);
	if (today < 0) {
	    today = 0;
	}
	int size = dueOut_.size();
	size--;
	if (end > size) {
	    end = size;
	}
	int priority;
	Enumeration demand;
	DueOut d;
	Vector orderedDueOuts =  new Vector();
	// Create priority buckets
	Vector bucket;
	Vector priority_buckets_ = new Vector(PRIORITY_LEVELS);
	for (int i=0; i< PRIORITY_LEVELS; i++)
	    priority_buckets_.add(new Vector());
	// Put tasks into buckets
	for (int i=end; i >= today; i--) {
	    demand = ((Vector)dueOut_.get(i)).elements();
	    while (demand.hasMoreElements()) {
		d = (DueOut)demand.nextElement();
		if (d.getFilled()) {
		    priority = TaskUtils.getNumericPriority(d.getTask());
		    bucket = (Vector)priority_buckets_.get(priority);
		    bucket.add(d);
		}
	    }
	}
	DueOut lowest_priority = null;
	for (int i=PRIORITY_LEVELS-1; i >= 0; i--) {
	    bucket = (Vector)priority_buckets_.get(i);
	    if (!bucket.isEmpty()) {
		lowest_priority = getLowestPriorityDueOut(bucket);
	    }
	}
	return lowest_priority;
    }

    private DueOut getLowestPriorityDueOut(Vector bucket) {
	int size = bucket.size();
	Long[] time_keys = new Long[size];
	Hashtable map = new Hashtable();
	DueOut d;
	Long key;
	// Grab the end time as sort key
	for (int i=0; i < size; i++) {
	    d = (DueOut)bucket.get(i);
	    key = new Long(TaskUtils.getEndTime(d.getTask()));
// 	    GLMDebug.DEBUG("InventoryBG", "getLowestPriorityDueOut, Key is "+key+", DueOut :"+d);
	    map.put(key, d);
	    time_keys[i] = key;
	}
	// Sort the array
	Arrays.sort(time_keys);
	Vector previouslyFailed = new Vector();
	Vector previouslyFilled = new Vector();
	// distinguish between previously successful and unsuccessful requests 
	for (int i=time_keys.length-1; i >= 0 ; i--) {
	    key = time_keys[i];
	    d = (DueOut)map.get(key);
// 	    GLMDebug.DEBUG("InventoryBG", "getLowestPriorityDueOut, Key is "+key+", DueOut :"+d);
	    if (d.getPreviouslyFilled()) {
		previouslyFilled.add(d);
	    } else {
		previouslyFailed.add(d);
	    }
	}
	d = null;
	if (!previouslyFailed.isEmpty()) {
	    d = (DueOut)previouslyFailed.get(0);
	} else if (!previouslyFilled.isEmpty()) {
	    d = (DueOut)previouslyFilled.get(0);
	}
	return d;
    }

    /** Get the average demand for the specified range. The
        interval is shortened as necessary to yield valid results.
    **/
    public Scalar getNDaysDemand(int day, int days_of_demand, int days_forward, int days_backward) {
	int start = day - days_backward;
	if (start < 0) {
	    start = 0;
	}
	int end = day + days_forward;
	if (end > dueOut_.size()) {
	    end = dueOut_.size();
	}
	int nDays = 0;          // Counts the number of days for which valid demand is available
        int today = getToday();
	
	double demand = 0.0;
	for (int i = start; i<end; i++){
            double thisDemand = Double.NaN;
            if (isDueOutValid(i)) {
                thisDemand = getDueOutTotal(i);
            } else {
                // No valid historical demand available
            }
            if (!Double.isNaN(thisDemand)) {
                demand += thisDemand;
                nDays++;
            }
	}
// 	GLMDebug.DEBUG("InventoryBG", "getNDaysDemand(), days of demand: "+days_of_demand+", start day: "+start+
// 		       ", end day: "+end+", period: "+period+", demand: "+demand);
	if (nDays == 0) nDays = 1;
	return getScalar((days_of_demand*demand) / nDays);
    }

    public int addPreviousRefillsToInventory(Task maintainInv) {

	int totalRefills=0;
// 	Enumeration refills = myPG_.getRefillSchedule().getAllScheduleElements();
// 	ObjectScheduleElement ose;
	if (maintainInv == null) {
	    return 0;
	}
	PlanElement pe = maintainInv.getPlanElement();
	if ((pe != null) && (pe instanceof Expansion)){
	    Expansion expansion;
	    Task refill;
	    expansion = (Expansion)pe;
	    Workflow wf = expansion.getWorkflow();
	    Enumeration tasks = wf.getTasks();
	    while (tasks.hasMoreElements()) {
		boolean filled = true;
		refill = (Task)tasks.nextElement();
		if (TaskUtils.isSupply(refill)) {
		    double qty = TaskUtils.getRefillQuantity(refill);
//  		    GLMDebug.DEBUG("InventoryBG", "addPreviousRefillsToInventory(), adding Refill to inventory, amt: "+qty);
		    if (qty == 0) {
			filled = false;
//			AllocationResult ar = ((Allocation)refill.getPlanElement()).getReportedResult();
//  			GLMDebug.DEBUG("InventoryBG", "addPreviousRefillsToInventory(), adding failed allocation to dueIns table : "+ 
//  				       TaskUtils.shortTaskDesc(refill)+" Allocation: "+ TaskUtils.arDesc(ar));
		    }
		    else {
			filled = true;
			totalRefills++;
		    }
		    addDueIn(refill, filled);
		}
	    }
	}
 	return totalRefills;
    }

    public int addDueIn(Task refillTask) {
	if (TaskUtils.isProjection(refillTask)) {
	    addProjection(refillTask, true);
	}
	else {
	    addDueIn(refillTask, true);
	}
	return 0;
    }

    private int addDueIn(Task refillTask, boolean filled) {
	// Add Refill to daily schedule
	// All times set to Midnight of that day
	long time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getRefillTime(refillTask));
	int day = TimeUtils.getDaysBetween(getStartTime(), time);
//    	GLMDebug.DEBUG("InventoryBG", null, "addDueIn(), Start: "+TimeUtils.dateString(getStartTime())+", task time: "+TimeUtils.dateString(time)+", day:"+day, 0);
	if (day >= dueIns_.size()) {
	    // Increase size of dueOut_ vector to accommodate days
	    for (int i=dueIns_.size()-1; i<=day; i++) {
		dueIns_.add(new Vector());
	    }
	}
	if (day >= 0) {
	    Vector v = (Vector) dueIns_.get(day);
	    v.add(new DueIn(refillTask, filled));
// 	    GLMDebug.DEBUG("InventoryBG", "addDueIn(), on day "+day+"for "+TaskUtils.shortTaskDesc(refillTask));
//  	    GLMDebug.DEBUG("InventoryBG", "addDueIn(), on day "+day+".");
	}
	else {
	    GLMDebug.ERROR("InventoryBG", "addDueIn(), Refill Starts in the past (start time "+
			   TimeUtils.dateString(getStartTime())+"). Task = "+TaskUtils.taskDesc(refillTask));
	}
	return 0;
    }

    // By the way, this is really addDueInProjection
    // AHF -- REFACTOR addDueInProjection and addDueOutProjection to use same piece of code
    private void addProjection(Task task, boolean filled) {
	long start_time = TaskUtils.getStartTime(task);
	long end_time = TaskUtils.getEndTime(task);
        int day0 = (int) (getStartTime() / TimeUtils.MSEC_PER_DAY);
 	int start = ((int) (start_time / TimeUtils.MSEC_PER_DAY)) - day0;
 	int end = ((int) (end_time / TimeUtils.MSEC_PER_DAY)) - day0;
	if (end <= 0) { return; }
	if (start < 0) { start = 0; }
	while (end > dueIns_.size()) {
            dueIns_.add(new Vector());
	}
	DueIn d = new DueIn(task, filled);
//   	GLMDebug.DEBUG("addProjection()", "start="+start+"("+TimeUtils.dateString(start_time)+")"+
//   	", end="+end+" ("+TimeUtils.dateString(end_time)+") Task:"+TaskUtils.taskDesc(task));	
	
        for (int day = start; day < end; day++) {
// 	    GLMDebug.DEBUG("InventoryBG", "addProjection(), Adding dueout on day "+day);
	    Vector v = (Vector) dueIns_.get(day);
	    v.add(d);
	}
    }

    // DRAT!!! Golden opportunity for refactoring.  Emergency demo solution
    // AHF -- Need to merge common code from addDueOut() and addDueOutProjeciton()
    // demos SUCK!
    private void addDueOutProjection(Task task) {
	PlanElement pe = task.getPlanElement();
	// If the task has just been rescinded, the plan element will be null.
	// This task should not affect planning
	if (pe == null) 
	    return;
	boolean filled = false;
	AllocationResult ar = pe.getEstimatedResult();
	if(ar==null){
	    GLMDebug.ERROR("addDueOut()", TaskUtils.taskDesc(task)+
			   " allocation without estimated result: "+pe.getUID());
	} else if (ar.isSuccess()) {
	    filled = true;
	}
	long start_time = TaskUtils.getStartTime(task);
	long end_time = TaskUtils.getEndTime(task);
        int day0 = (int) (getStartTime() / TimeUtils.MSEC_PER_DAY);
 	int start = ((int) (start_time / TimeUtils.MSEC_PER_DAY)) - day0;
 	int end = ((int) (end_time / TimeUtils.MSEC_PER_DAY)) - day0;
	if (end <= 0) { return; }
	if (start < 0) { start = 0; }
	while (end > dueOut_.size()) {
            dueOut_.add(new Vector());
	}
	
	DueOut d = new DueOut(task, filled);
//     	GLMDebug.DEBUG("addDueOutProjection()", "start="+start+"("+TimeUtils.dateString(start_time)+")"+
//     	", end="+end+" ("+TimeUtils.dateString(end_time)+") Task:"+TaskUtils.taskDesc(task));	
        for (int day = start; day < end; day++) {
//   	    GLMDebug.DEBUG("InventoryBG", "addProjection(), Adding dueout on day "+day);
	    Vector v = (Vector) dueOut_.get(day);
	    v.add(d);
	}
    }

    public int removeDueIn(Task refillTask) {
	// Remove Task from local structure
	long time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getEndTime(refillTask));
	int day = TimeUtils.getDaysBetween(getStartTime(), time);
	try {
	    Vector v = (Vector)dueIns_.get(day);
	    Enumeration e = v.elements();
	    DueIn d;
	    while (e.hasMoreElements()) {
		d = (DueIn)e.nextElement();
		if (d.getTask() == refillTask) {
		    v.remove(d);
		    GLMDebug.DEBUG("InventoryBG", "removeDueIn(), removing dueIn on day "+day);
		}
	    }
	} catch (ArrayIndexOutOfBoundsException exception) {
	    GLMDebug.ERROR("InventoryBG", "removeDueIn, day "+day+": Refill not found. "+TaskUtils.taskDesc(refillTask));
	}
	return 0;
    }

    public int getPlanningDays() {
	if (level_ == null) {
	    determineInventoryLevels();
	}
	return level_.length;
    }

    public int determineInventoryLevels() {
	double reported_levels[] = getTimeOrderedReportedLevels();
	double dueout, duein;
        int today = getToday();
	int size = today + 1;  // Always include tomorrow in the levels.
	if (dueIns_.size() > size) size = dueIns_.size();
	if (dueOut_.size() > size) size = dueOut_.size();
	if (reported_levels.length > size) size = reported_levels.length;
	double previous_level = reported_levels[0];
//          GLMDebug.DEBUG("InventoryBG", null,
//                         "determineInventoryLevels(), Number of planning days is "
//                         + size
//                         + ", DueOuts="
//                         + dueOut_.size()
//                         + ", DueIns="
//                         + dueIns_.size()
//                         + ", reported levels="
//                         + reported_levels.length,
//                         1000);
	level_ = new double[size];
	for (int i=0; i < size; i++) {
            dueout = getDueOutTotal(i);
	    if ((reported_levels.length > i) && !Double.isNaN(reported_levels[i])) {
		level_[i] = reported_levels[i];
	    } else {
                duein = getDueInTotal(i);
		level_[i] = previous_level + duein - dueout;
	    }
	    previous_level = level_[i];

//   	    GLMDebug.DEBUG("InventoryBG", "determineInventoryLevels(), day "+i+": level "+level_[i]+"= previous "+previous_level+
//   			   " + duein "+duein+" - dueout "+dueout);
	}
	return 0;
    }

    private boolean isDueOutValid(int day) {
        if (day < 0) return false;
        if (day >= dueOut_.size()) return false;
        return true;
    }

    private double getDueOutTotal(int day) {
        if (!isDueOutValid(day)) return 0.0;
	Vector dueOuts = (Vector) dueOut_.get(day);
	Enumeration e = dueOuts.elements();
	double actualTotal = 0.0;
	double projectedTotal = 0.0;
	InventoryTask t;
	Task task;
	int imputedDay = day + java.lang.Math.round((startTime_ -today_)/TimeUtils.MSEC_PER_DAY);
	while (e.hasMoreElements()) {
	    t = (InventoryTask)e.nextElement();
	    task = t.getTask();
	    if (t.getFilled()) {
                if (task.getVerb().equals(Constants.Verb.WITHDRAW)){
		    actualTotal += TaskUtils.getWithdrawQuantity(task) * getWeightingFactor(task,imputedDay);
		} else if (task.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) {
//  		    projectedTotal += TaskUtils.getWithdrawQuantity(task) * getWeightingFactor(task,imputedDay);
		    Rate r = TaskUtils.getRate(task);
		    if (r instanceof FlowRate) {
			projectedTotal += ((FlowRate)r).getGallonsPerDay() * getWeightingFactor(task, imputedDay);
		    } else if (r instanceof CountRate) {
			projectedTotal += ((CountRate)r).getEachesPerDay() * getWeightingFactor(task, imputedDay);
//   			GLMDebug.DEBUG("InventoryBG", null, "getDueInTotal() with factor :"+d+
//    				       " is "+(((CountRate)r).getEachesPerDay() * d));
		    }		
		}		
	    }
	}
//  	GLMDebug.DEBUG("InventoryBG", null, "Demand on day: "+day+"; actual= "+actualTotal+
//  		       ", projected= "+projectedTotal);
	return actualTotal + projectedTotal;
    }

    private double getDueInTotal(int day) {
        if (day < 0) return 0.0;
        if (day >= dueIns_.size()) return 0.0;
	Vector dueIns = (Vector) dueIns_.get(day);
	Enumeration e = dueIns.elements();
	double actualTotal = 0.0;
	double projectedTotal = 0.0;
	InventoryTask t;
	Task task;
	int imputedDay = day + java.lang.Math.round((startTime_ -today_)/TimeUtils.MSEC_PER_DAY);
	while (e.hasMoreElements()) {
	    t = (InventoryTask)e.nextElement();
	    task = t.getTask();
	    double d = getWeightingFactor(task, imputedDay);
	    if (t.getFilled()) {
		if (TaskUtils.isProjection(task)) {
		    Rate r = TaskUtils.getRate(task);
		    if (r instanceof FlowRate) {
			projectedTotal += ((FlowRate)r).getGallonsPerDay() * d;
		    } else if (r instanceof CountRate) {
			projectedTotal += ((CountRate)r).getEachesPerDay() * d;
//   			GLMDebug.DEBUG("InventoryBG", null, "getDueInTotal() with factor :"+d+
//    				       " is "+(((CountRate)r).getEachesPerDay() * d));
		    }
		}
		else {
		    if(d<1.0){
			GLMDebug.ERROR("InventoryBG", null, "Inv Day: "+day+" imputedDay: "+imputedDay+
				       "- Zero weighted supply task: "+TaskUtils.taskDesc(task));
		    }
		    actualTotal += TaskUtils.getRefillQuantity(task) * d;
		}
	    }
	}
//  	if(((AntsProjectionWeight)weight_).isFSB()){
//  	GLMDebug.DEBUG("InventoryBG", null, "Refill on day: "+day+"; actual= "+actualTotal+
//  		       ", projected= "+projectedTotal);
//  	}
	return actualTotal+projectedTotal;
    }

    public Scalar getProjected(int day) {
	// ignore requests for days beyond the scope of inventory
	if ((day < 0) ||  (day >= dueOut_.size())) {
	    return null;
	}
	// dueouts for the day contain Supply and ProjectSupply tasks
	Enumeration e = ((Vector)dueOut_.get(day)).elements();
	double total = 0;
	Task t = null;
	while (e.hasMoreElements()) {
	    t = ((InventoryTask)e.nextElement()).getTask();
	    if (t.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) {
		Rate r = TaskUtils.getRate(t);
		if (r instanceof FlowRate) {
		    total += ((FlowRate)r).getGallonsPerDay();
		} else if (r instanceof CountRate) {
		    total += ((CountRate)r).getEachesPerDay();
		}
//  		total += TaskUtils.getWithdrawQuantity(t);
	    }
	} 
	return getScalar(total);
    }

//     private double getWeightingFactor(int days) {
    private double getWeightingFactor(Task task, int imputedDay) {
	if (weight_ != null) {
	    return weight_.getProjectionWeight(task, imputedDay);
	}
	else {
	    GLMDebug.ERROR("InventoryBG", null, "getWeightingFactor(), Weighting Factor NOT set.");
	    return 0.0;
	}
    }

    public Task refillAlreadyFailedOnDay(int day) {
	if (day < dueIns_.size()) {
	    Enumeration dueins = ((Vector)dueIns_.get(day)).elements();
	    DueIn d;
	    while (dueins.hasMoreElements()) {
		d = (DueIn)dueins.nextElement();
		if (!d.getFilled()) {
		    GLMDebug.DEBUG("InventoryBG()", "failedRefill(), alreadyFailedOnDay "+day);
		    return d.getTask();
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
	if (day < dueIns_.size()) {
	    Vector refills = (Vector)dueIns_.get(day);
	    Enumeration e = refills.elements();
	    DueIn d;
	    Task smallestRefill = null;
            double smallestQuantity = Double.POSITIVE_INFINITY;
	    while (e.hasMoreElements()) {
		d = (DueIn) e.nextElement();
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

    public Date lastDemandTaskEnd(Inventory inventory) {
	long latest_time = -1;
	long end_time;
	Task task;
	Enumeration role_sched = inventory.getRoleSchedule().getRoleScheduleElements();
	while (role_sched.hasMoreElements()) {
	    PlanElement pe = (PlanElement)role_sched.nextElement();
	    task = pe.getTask();
	    end_time = TaskUtils.getRefillTime(task);
	    if (end_time > latest_time) {
		latest_time = end_time;
	    }
	}
	if (latest_time < 0) 
	    return null;
	return new Date(TimeUtils.pushToEndOfDay(calendar_, latest_time));
    }

    // Looking for successful dueouts that failed last run
    public Vector getPreviouslyFailedDueOuts() {
	int size = dueOut_.size();
	Vector failed_dueout = new Vector();
	Enumeration dueouts;
	DueOut d;
	for (int i=0; i < size; i++) {
	    dueouts = ((Vector)dueOut_.get(i)).elements();
	    while (dueouts.hasMoreElements()) {
		d = (DueOut)dueouts.nextElement();
		if (d.getFilled() && !d.getPreviouslyFilled()) {
		    failed_dueout.add(d);
		}
	    }
	}
	return failed_dueout;
    }

    public Integer getFirstOverflow(int i, ClusterIdentifier cluster) {
	if (level_ == null)
	    determineInventoryLevels();
	int size = level_.length;
	double capacity = getDouble(myPG_.getCapacity());
	Integer day = null;
	if ((i > size) || (i < 0))
	    return null;
	for (; i < size; i++) {
//  	    GLMDebug.DEBUG("InventoryBG()", cluster, "getFirstOverflow(), checking for overflow on "+
//  			   (TimeUtils.dateString(TimeUtils.addNDays(getStartTime(), i)))+" Level: "+level_[i]+" capacity: "+capacity);
	    if (java.lang.Math.floor(level_[i] - capacity)>1.0) {
		day = new Integer(i);
		GLMDebug.DEBUG("InventoryBG()", cluster, "getFirstOverflow(), OVERFLOW on "+
			       (TimeUtils.dateString(TimeUtils.addNDays(getStartTime(), i))));
		break;
	    }
	}
	return day;
    }

    public int updateContentSchedule(Inventory inventory) {
	ScheduledContentPG scp = inventory.getScheduledContentPG();
	Schedule sched = scp.getSchedule();
	QuantityScheduleElement qse;
	Vector new_elements = new Vector();
	if (level_ == null) {
            determineInventoryLevels();
        }
	if (level_ == null) {
	    GLMDebug.DEBUG("InventoryBG", "UpdateContentSchedule(), No DueOuts or DueIns, resetting schedule.");
	    clearContentSchedule(inventory);
	}
	else {
	    int days = level_.length;
//  	    GLMDebug.DEBUG("InventoryBG", "UpdateContentSchedule(), Creating new Schedule for "+AssetUtils.assetDesc(inventory));
	    for (int i=0; i < days; i++) {
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
//  		GLMDebug.DEBUG("InventoryBG", "UpdateContentSchedule(), Start:"+TimeUtils.dateString(start)+
//  			       ", End:"+TimeUtils.dateString(end)+", Qty: "+level_[i]);
		new_elements.add(qse);
	    }
	}
	Schedule new_schedule = ALPFactory.newQuantitySchedule(new_elements.elements(), 
							       PlanScheduleType.TOTAL_INVENTORY);

	if (ScheduleUtils.isOffendingSchedule(new_schedule)) {
	    GLMDebug.ERROR("InventoryBG", "UpdateContentSchedule(),  CREATED BAD SCHEDULE ");
 	    printQuantityScheduleTimes(new_schedule);
	}
	((NewScheduledContentPG)scp).setSchedule(new_schedule);
	return 0;
    }

    public int clearContentSchedule(Inventory inventory) {
	ScheduledContentPG scp = inventory.getScheduledContentPG();
	Schedule sched = scp.getSchedule();
	QuantityScheduleElement qse;
	Vector new_elements = new Vector();
	long start = TimeUtils.addNDaysTime(getStartTime(), 0);
	long end = TimeUtils.addNDaysTime(getStartTime(), 30);
	
	qse = ScheduleUtils.buildQuantityScheduleElement(getDouble(myPG_.getInitialLevel()), start, end);
	new_elements.add(qse);

	Schedule new_schedule = ALPFactory.newQuantitySchedule(new_elements.elements(), 
							       PlanScheduleType.TOTAL_INVENTORY);

	if (ScheduleUtils.isOffendingSchedule(new_schedule)) {
	    GLMDebug.ERROR("InventoryBG", "UpdateContentSchedule(),  CREATED BAD SCHEDULE ");
 	    printQuantityScheduleTimes(new_schedule);
	}
	((NewScheduledContentPG)scp).setSchedule(new_schedule);
	return 0;
 
    }


    public int printQuantityScheduleTimes(Schedule sched) {
	Enumeration elements = sched.getAllScheduleElements();
	QuantityScheduleElement qse;

	while (elements.hasMoreElements()) {
	    qse = (QuantityScheduleElement)elements.nextElement();
	    GLMDebug.DEBUG("InventoryBG", "printQuantityScheduleTimes()    qty: "+qse.getQuantity()+
			   " "+qse.getStartTime()+" to "+ qse.getEndTime());
	}
	return 0;
    }

    public abstract PGDelegate copy(PropertyGroup pg);
    
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
	GLMDebug.DEBUG("InventoryBG", "addInventoryReport(), Report added!!!!!!! : "+this.hashCode());
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
//  	    GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), List is empty.");
	} else {
//  	    GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), Sorting Report list.");
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
	    int len = TimeUtils.getDaysBetween(getStartTime(), TimeUtils.pushToEndOfDay(calendar_, latest))+1;
	    if (len < 0) {
		len = 1;
	    }
	    ordered_list = new double[len];
	    java.util.Arrays.fill(ordered_list, Double.NaN);
	    ordered_list[0] = getDouble(myPG_.getInitialLevel());
            long start = getStartTime();
	    for (int i = 0, n = reports.length; i < n; i++) {
		long report_date = reports[i].theReportDate;
		int day = TimeUtils.getDaysBetween(start, TimeUtils.pushToEndOfDay(calendar_, report_date));
		// Last Report that was received before or on day 0 becomes initial value
		if (day < 0) {
		    day = 0;
		}
		ordered_list[day] = reports[i].theQuantity;
		GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), Report day is "+day+
			       "("+TimeUtils.dateString(report_date)+"), Value is: "+ordered_list[day]);
	    }
	}
	return ordered_list;
    }

    /** 
	Given a double, create scalar apropriate for this type of inventory 
    **/
    private Scalar getScalar(double amt) {
	Scalar result = null,
	       measure = myPG_.getInitialLevel();
	if (measure instanceof Volume) {
	    result = new Volume(amt, Volume.GALLONS);
	} else if (measure instanceof Count) {
	    result = new Count(amt, Count.EACHES);
	} else if (measure instanceof Mass) {
	    result = new Mass(amt, Mass.SHORT_TONS);
	} else {
	    GLMDebug.ERROR("InventoryBG", "getScalar(), Inventory cannot determine type of measure");
	}
	return result;
    }
				       
    /** 
	Given a Scalar, return a double value representing
	Gallons for Volume,
	Eaches for Count and
	Short Tons for Mass.
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

    public int printInventoryLevels(Inventory inventory, ClusterIdentifier clusterID) {
	GLMDebug.DEBUG("InventoryBG()", clusterID, "printInventoryLevels(), Day 0 is "+TimeUtils.dateString(getStartTime()));
	if (level_ != null) {
	    int size = level_.length;
	    ScheduledContentPG scpg = inventory.getScheduledContentPG();
	    GLMDebug.DEBUG("InventoryBG", clusterID, "printInventoryLevels(), for "+AssetUtils.assetDesc(scpg.getAsset()));
	    for (int i=0; i < size; i++) {
		GLMDebug.DEBUG("InventoryBG()", clusterID, "printInventoryLevels(), day: "+
			       TimeUtils.dateString(TimeUtils.addNDays(getStartTime(), i))+"("+i+") "+", level: "+level_[i]);
	    }
	}
	return 0;
    }


}
