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

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.domain.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.domain.glm.ldm.ALPFactory;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.execution.common.InventoryReport;
//  import com.bbn.supply.assets.AntsProjectionWeight;

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
    protected int demandAveragePeriod_ = 30;
    private ProjectionWeight weight_;
    // Stuff for execution below
    protected ArrayList report_history;
    protected InventoryReport latest_report;
    protected Calendar calendar_ = Calendar.getInstance();
    
    public InventoryBG(InventoryPG  pg) {
	myPG_ = pg;
	dueIns_ = new Vector();
	dueOut_ = new Vector();
	level_ = null;
	report_history = new ArrayList();
	latest_report = null;
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

	latest_report = null;

	initializeTime(inventory, today);
//    	GLMDebug.DEBUG("InventoryBG", null, " resetInventory():"+inventory+
//  		       " Start Time: "+TimeUtils.dateString(getStartTime())+", Today: "+
//    		       TimeUtils.dateString(today_)+"("+getToday()+")"+", 1st day of demand "+
//    		       TimeUtils.dateString(firstDayOfDemand_), 0);
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

    private void initializeTime(Inventory inventory, long today) {
	try {
	    // Today is AlpTime, i.e. now
	today_ = TimeUtils.pushToEndOfDay(calendar_, today);
	Enumeration role_sched = inventory.getRoleSchedule().getRoleScheduleElements();
	long start_time = -1L;
        // StartTime_ is 14 days before earliest demand, Day 0 for this inventory.
        // firstDayOfDemand is the day on which the IM receives its first request.
        while (role_sched.hasMoreElements()) {
            PlanElement pe = (PlanElement)role_sched.nextElement();
            Task dueOut = pe.getTask();
            long time = TaskUtils.getStartTime(dueOut);
            if (time <= 0L) continue;
            if (start_time == -1L || time < start_time) {
                start_time = time;
            }
        }
        if (start_time != -1L) {
	    firstDayOfDemand_ = TimeUtils.pushToEndOfDay(calendar_, start_time);
	    setStartTime(TimeUtils.pushToEndOfDay(calendar_, start_time - (14*TimeUtils.MSEC_PER_DAY)));
        } else {
	    // No Demand, Start is today and firstDayOfDemand is sometime in the future
	    // if InventoryManager runs before any requests are recieved 14 days allows
	    // plenty of order and ship time to bring inventories up to safety levels.
	    //  don't actually set start time -- use default
//  	    setStartTime(TimeUtils.pushToEndOfDay(calendar_, today_));
	    firstDayOfDemand_ = TimeUtils.addNDays(today_, 14);;
	}
        } catch (RuntimeException re) {
          System.err.print(re + ": today=" + today + ", today_ = " + today_);
          throw re;
        }
    }

  private void setStartTime(long newStartTime) {
      if (newStartTime <= 0L) {
        throw new IllegalArgumentException("Bogus newStartTime: " + newStartTime);
      }
      if(startTimeNeverSet || (startTime_>newStartTime)) {
      startTime_ = newStartTime;
      }
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

    public Scalar getLevel(long day) {
	int d = TimeUtils.getDaysBetween(day, getStartTime());
	return getLevel(d);
    }

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
	    addDueOut(dueOut);
	    requests++;
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
	if (day >= dueOut_.size()) {
	    // Increase size of dueOut_ vector to accommodate days
	    for (int i=dueOut_.size()-1; i<=day; i++) {
		dueOut_.add(new Vector());
	    }
	}
//     	GLMDebug.DEBUG("InventoryBG", "addDueOut(), Adding dueout on day "+day+"("+TimeUtils.dateString(time)+")"+TaskUtils.taskDesc(request));
	Vector v = (Vector)dueOut_.get(day);
	PlanElement pe = request.getPlanElement();
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

    public Scalar getNDaysDemand(int day, int days_of_demand) {
	// Needs work still (lost the 30 day buffer at either end)
	int start = day;
	start = start-(demandAveragePeriod_/2);
	if (start < 0) {
	    start = 0;
	}
	int end = start + demandAveragePeriod_;
	if (end > dueOut_.size()) {
	    end = dueOut_.size();
	}
	int period = end - start;
	
	double demand = 0.0;
	for (int i = start; i<end; i++){
	    demand+=getDueOutTotal((Vector)dueOut_,i);
	}
// 	GLMDebug.DEBUG("InventoryBG", "getNDaysDemand(), days of demand: "+days_of_demand+", start day: "+start+
// 		       ", end day: "+end+", period: "+period+", demand: "+demand);
	return getScalar((days_of_demand*demand)/period);
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
	    boolean filled = true;
	    while (tasks.hasMoreElements()) {
		refill = (Task)tasks.nextElement();
		if (TaskUtils.isSupply(refill)) {
		    double qty = TaskUtils.getRefillQuantity(refill);
//  		    GLMDebug.DEBUG("InventoryBG", "addPreviousRefillsToInventory(), adding Refill to inventory, amt: "+qty);
		    if (qty == 0) {
			filled = false;
			AllocationResult ar = ((Allocation)refill.getPlanElement()).getReportedResult();
//  			GLMDebug.DEBUG("InventoryBG", "addPreviousRefillsToInventory(), adding failed allocation to dueIns table : "+ 
//  				       TaskUtils.shortTaskDesc(refill)+" Allocation: "+ TaskUtils.arDesc(ar));
		    }
		    else {
			filled = true;
			totalRefills++;
		    }
		    if (TaskUtils.isProjection(refill)) {
			addProjection(refill, filled);
		    }
		    else {
			addDueIn(refill, filled);
		    }
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
	long time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getEndTime(refillTask));
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

    private void addProjection(Task task, boolean filled) {
	long start_time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getStartTime(task));
	long end_time = TimeUtils.pushToEndOfDay(calendar_, TaskUtils.getEndTime(task));
 	int start = TimeUtils.getDaysBetween(getStartTime(), start_time);
	int end = TimeUtils.getDaysBetween(getStartTime(), end_time);
	if (start < 0) { start = 0; }
	if (end < 0) { return; }
	if (end >= dueIns_.size()) {
	    for (int i=dueIns_.size()-1; i<=end; i++) {
		dueIns_.add(new Vector());
	    }
	}
	DueIn d = new DueIn(task, filled);
//   	GLMDebug.DEBUG("addProjection()", "start="+start+"("+TimeUtils.dateString(start_time)+")"+
//   	", end="+end+" ("+TimeUtils.dateString(end_time)+") Task:"+TaskUtils.taskDesc(task));	
	for (int i=start; i<= end; i++) {
// 	    GLMDebug.DEBUG("InventoryBG", "addProjection(), Adding dueout on day "+day);
	    Vector v = (Vector)dueIns_.get(i);
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
	int size = dueIns_.size();
	if (dueOut_.size() > size) {
	    size = dueOut_.size();
	}
	if (reported_levels.length > size) {
	    size = reported_levels.length;
	}
	double previous_level = reported_levels[0];
	// 	GLMDebug.DEBUG("InventoryBG", "determineInventoryLevels(), Number of planning days is "+size+
	// 		       ", DueOuts="+dueOut_.size()+", DueIns="+dueIns_.size()+", reported levels="+reported_levels.length);
	level_ = new double[size];
	for (int i=0; i < size; i++) {
	    try {
		dueout = getDueOutTotal((Vector)dueOut_,i);
	    } catch (ArrayIndexOutOfBoundsException exception) {
		dueout = 0;
	    }
	    try {
		duein = getDueInTotal((Vector)dueIns_,i);
	    }
	    catch (ArrayIndexOutOfBoundsException exception) {
		duein = 0;
	    }
	    if ((reported_levels.length > i) && (reported_levels[i] >= 0)) {
		level_[i] = reported_levels[i];
	    } else {
		level_[i] = previous_level + duein - dueout;
	    }
	    previous_level = level_[i];

	    //   	    GLMDebug.DEBUG("InventoryBG", "determineInventoryLevels(), day "+i+": level "+level_[i]+"= previous "+previous_level+
	    //   			   " + duein "+duein+" - dueout "+dueout);
	}
	return 0;
    }

    private double getDueOutTotal(Vector dueOutsVector, int day) {
	Vector dueOuts = (Vector)dueOutsVector.get(day);
	Enumeration e = dueOuts.elements();
	double actualTotal = 0;
	double projectedTotal = 0;
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
		    projectedTotal += TaskUtils.getWithdrawQuantity(task) * getWeightingFactor(task,imputedDay);
		}		
	    }
	}
//  	GLMDebug.DEBUG("InventoryBG", null, "Demand on day: "+day+"; actual= "+actualTotal+
//  		       ", projected= "+projectedTotal);
	return actualTotal+projectedTotal;
    }

    private double getDueInTotal(Vector dueInsVector, int day) {
	Vector dueIns = (Vector)dueInsVector.get(day);
	Enumeration e = dueIns.elements();
	double actualTotal = 0;
	double projectedTotal = 0;
	InventoryTask t;
	Task task;
	int imputedDay = day + java.lang.Math.round((startTime_ -today_)/TimeUtils.MSEC_PER_DAY);
	while (e.hasMoreElements()) {
	    t = (InventoryTask)e.nextElement();
	    task = t.getTask();
	    double d = getWeightingFactor(task,imputedDay);
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
		total += TaskUtils.getWithdrawQuantity(t);
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

    public Task getRefillOnDay(int day) {
	if (day < dueIns_.size()) {
	    Vector refills = (Vector)dueIns_.get(day);
	    Enumeration e = refills.elements();
	    DueIn d;
	    Task task;
	    while (e.hasMoreElements()) {
		d = (DueIn) e.nextElement();
		task = d.getTask();
		if (task.getVerb().equals(Constants.Verb.WITHDRAW))
		    return task;
	    }
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
                  System.err.println("start=" + start);
                  System.err.println("end=" + end);
                  continue;
                }
//  		GLMDebug.DEBUG("InventoryBG", "UpdateContentSchedule(), Start:"+TimeUtils.dateString(start)+
//  			       ", End:"+TimeUtils.dateString(end)+", Qty: "+level_[i]);
		new_elements.add(qse);
	    }
	}
	Schedule new_schedule = ALPFactory.newQuantitySchedule(new_elements.elements(), 
							       ScheduleType.TOTAL_INVENTORY);

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
							       ScheduleType.TOTAL_INVENTORY);

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
	}
	GLMDebug.DEBUG("InventoryBG", "addInventoryReport(), Report added!!!!!!! : "+this.hashCode());
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
    private InventoryReport getLatestInventoryReport() {
	synchronized (this) {
	    InventoryReport curr_report = null;
	    latest_report = null;

	    if (!report_history.isEmpty()) {
		// size is set to the # of elements in the report_history
		int size = report_history.size();
		
		// initialize the latest_report to be the first one
		latest_report = (InventoryReport) report_history.get(0);
		
		// Now, we loop through the rest of the reports to find the latest one
		for(int i = 1; i < size; i++) {
		    curr_report = (InventoryReport) report_history.get(i);
		    // Erika: not sure if I should be looking at the ReportDate or ReceivedDate!!!
		    if (latest_report.theReportDate < curr_report.theReportDate) {
			latest_report = curr_report;
		    }
		}
	    }
	    return latest_report;
	}
    }

    /**
     * Returns a time-ordered list of known InventoryReports
     **/
    private double[]  getTimeOrderedReportedLevels() {
	double ordered_list[];
	ArrayList list = getInventoryReportHistory();
	// No Reports - use PG for initial inventory level
	if (list.isEmpty()) {
	    ordered_list = new double[1];
	    ordered_list[0] = getDouble(myPG_.getInitialLevel());
//  	    GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), List is empty.");
	}
	else {
//  	    GLMDebug.DEBUG("InventoryBG", "getTimeOrderedReportedLevels(), Sorting Report list.");
	    // Sort the Reports by time in ascending order
	    // Yah, it's inefficient but it's only a few lines of code and there should never
	    // be more than 5 items in this list
	    int size = list.size();
	    Object obj;
	    for (int i = 0; i < size - 1; i++) {
		for (int j = i + 1; j < size ; j++) {
		    InventoryReport repi = (InventoryReport)list.get(i);
		    InventoryReport repj = (InventoryReport)list.get(j);
  		    if (repi.theReportDate > repj.theReportDate) {
  			list.set(i, repj);
  			list.set(j, repi);
  		    }
		}
	    }
//  	    for (int i=0; i< size; i++) {
//  		while (i < size-1) {
//  		    if (((InventoryReport)list.get(i)).theReportDate >
//  			((InventoryReport)list.get(i+1)).theReportDate) {
//  			obj = list.get(i);
//  			list.set(i, list.get(i+1));
//  			list.set(i+1, obj);
//  		    }
//  		}

//  	    }
	    long latest = ((InventoryReport)list.get(size-1)).theReportDate;

	    // create a double[] containing inventory levels indexed by day
	    int len = TimeUtils.getDaysBetween(getStartTime(), TimeUtils.pushToEndOfDay(calendar_, latest))+1;
	    if (len < 0) {
		len = 1;
	    }
	    ordered_list = new double[len];
	    java.util.Arrays.fill(ordered_list, -1.0);
	    ordered_list[0] = getDouble(myPG_.getInitialLevel());
	    Double d;
	    for (int i=0; i<size; i++) {
		long report_date = ((InventoryReport)list.get(i)).theReportDate;
		int day = TimeUtils.getDaysBetween(getStartTime(), TimeUtils.pushToEndOfDay(calendar_, report_date));
		// Last Report that was received before or on day 0 becomes initial value
		if (day < 0) {
		    day = 0;
		}
		ordered_list[day] = ((InventoryReport)list.get(i)).theQuantity;
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
