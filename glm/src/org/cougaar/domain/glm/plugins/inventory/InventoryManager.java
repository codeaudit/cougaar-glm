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
package org.cougaar.domain.glm.plugins.inventory;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.BulkPOL;
import org.cougaar.domain.glm.ldm.asset.ContainPG;
import org.cougaar.domain.glm.ldm.asset.VolumetricInventory;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.lang.Math;
import java.util.*;
import org.cougaar.domain.glm.ldm.asset.Inventory;
import org.cougaar.domain.glm.ldm.asset.Ammunition;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.NewScheduledContentPG;
import org.cougaar.domain.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.debug.*;

public abstract class InventoryManager extends InventoryProcessor {

    protected IncrementalSubscription inventoryAllocSubscription_ = null;
    protected IncrementalSubscription modifiedInventorySubscription_ = null;
    /** Subscription to policies */
    protected IncrementalSubscription        onHandPolicySubscription_;

    public static final int               DONE = -1; 

    /**
     *   daysOnHand_    keep enough inventory on hand to cover N days of demand
     *   daysForward_   When calculating average daily demand, look N days forward
     *                  from this day.
     *   daysBackward_  When calculating average daily demand, look N days backward
     *                  from this day.
     *   goalLevelMultiplier_     Multiplier for safety level which yields goal level
     */
    protected int daysOnHand_ = 3;
    protected int daysForward_ = 15;
    protected int daysBackward_ = 15;
    protected double goalLevelMultiplier_ = 2.0;

    static class PolicyPredicate implements UnaryPredicate {
	String type_;
	public PolicyPredicate(String type) {
	    type_ = type;
	}
	public boolean execute(Object o) {
	    if (o instanceof DaysOnHandPolicy) {
		String type = ((DaysOnHandPolicy)o).getResourceType();
		if (type.equals(type_)) {
		    return true;
		}
	    }
	    return false;
	}
    };   

    // Allocations of tasks with quantity > 0 to Inventory objects
    static class AllocToInventoryPredicate implements UnaryPredicate
    {
	String type_;
	public AllocToInventoryPredicate(String type) {
	    type_ = type;
	}
	public boolean execute(Object o) {
	    if (o instanceof Allocation ) {
		Task task = ((Allocation)o).getTask();
		if (task.getVerb().equals(Constants.Verb.WITHDRAW) ||
		    task.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) {		 
		    if (TaskUtils.isDirectObjectOfType(task, type_)) {
			// need to check if alloced to inventory
			if (((Allocation)o).getAsset() instanceof Inventory) {
			    return true;
			}
		    }
		}
	    }
	    return false;
	}
    }

    static class ModifiedInventoryPredicate implements UnaryPredicate 
    {
	String type_;
	public ModifiedInventoryPredicate(String type) {
	    type_ = type;
	}
	public boolean execute(Object o) {
	    if (o instanceof Inventory) {
		InventoryPG invpg = 
		    (InventoryPG)((Inventory)o).searchForPropertyGroup(InventoryPG.class);
		return ((invpg != null) && AssetUtils.isSupplyClassOfType(invpg.getResource(), type_));
	    }
	    return false;
	}
    }



    public InventoryManager(InventoryPlugIn plugin, Organization org, String type) {
	super(plugin, org, type);
 	inventoryAllocSubscription_ = subscribe(new AllocToInventoryPredicate(supplyType_));
	modifiedInventorySubscription_ = subscribe(new ModifiedInventoryPredicate(supplyType_));
	onHandPolicySubscription_ = subscribe(new PolicyPredicate(type));

	// FSB and higher maintain more inventory
	if(!isBattalionLevel()) {
	    printDebug(2,"Cluster: "+clusterId_.toString()+" is not Battalion level.         	    daysOnHand_=6;");
	    daysOnHand_=6;
	} else {
	    printDebug(2,"Cluster: "+clusterId_.toString()+" is Battalion level. 	    daysOnHand_=3;");
	    daysOnHand_=3;
	}

    }

    // ********************************************************
    //                                                        *
    // Point Of Entry to InventoryManager                     *
    //                                                        *
    // ********************************************************

    /** This method is called everytime a subscription has changed. */

    public void update() {
	// Skeleton algorithm (Template Method pattern)  (learned at JavaOne)
	// Used by all concrete subclasses
	super.update();
	if (needUpdate()) {
 	    printDebug(2,"\n\n\nBEGIN CYCLE___________________________________________\n");
	    resetInventories();
	    
	    if (inventoryPlugIn_.getDetermineRequirementsTask() == null) {
		// MWD - added a method to handle GLS Rescind cases,
		//       override in DLAInventoryManager that reinitializes
		//       input files as well as clearing inventory schedules
		handleGLSRescind();
	    }
	    else {
		accountForWithdraws();
		generateHandleDueIns();
 		adjustWithdraws();
		checkForOverflow();
		refreshInventorySchedule();
	    }
	}
    }


    // ********************************************************
    //                                                        *
    // Need Update / Reset Section                            *
    //                                                        *
    // ********************************************************

    protected abstract boolean needUpdate();

    // Reset Inventories

    protected void resetInventories() {
	Inventory inventory;
	Enumeration list = inventoryPlugIn_.getInventoryBins(supplyType_);
	printDebug("STEP 1: RESETINVENTORIES(), Today: "+TimeUtils.dateString(startTime_));
	while (list.hasMoreElements()) {
	    inventory = (Inventory)list.nextElement();
	    InventoryPG invpg = 
		(InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	    invpg.resetInventory(inventory, startTime_);
	}
    }


    // ********************************************************
    //                                                        *
    // Account for withdraws                                  *
    //                                                        *
    // ********************************************************

    protected void accountForWithdraws() {
// 	failedDueOuts_.clear();
	printDebug("STEP 2: ACCOUNTFORWITHDRAWS()");
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	while (inventories.hasMoreElements()) {
	    Inventory inventory = (Inventory)inventories.nextElement();
	    InventoryPG invpg = 
		(InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	    invpg.withdrawFromInventory(inventory, clusterId_);
	    invpg.determineInventoryLevels();
	    Enumeration tasks = generateProjections(inventory);
	    publishChangeProjection(inventory, tasks);
//  	    invpg.printInventoryLevels(inventory, clusterId_);
	}
    }

    protected Enumeration generateProjections(Inventory inventory) {
//  	printDebug("STEP 2:  GenerateProjections() for "+AssetUtils.getAssetIdentifier(inventory));
	Vector projections = new Vector();
	InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	int days = invpg.getPlanningDays();
	int today = invpg.getFirstPlanningDay();
	Scalar previous = invpg.getProjected(today);
	Scalar current = null;
	double safety;
	int periodBegin = today;
	boolean newCurrent = false;
	long start;
	int i=today;
	//  	printDebug(0, "!!!!!!!!!!!!!!!!! first day of demand : "+TimeUtils.dateString(TimeUtils.addNDays(invpg.getStartTime(),today))+", INV START: "+TimeUtils.dateString(invpg.getStartTime()));
	for (; i < days; i++) {
	    current = invpg.getProjected(i);
	    if (previous !=  null) {
		//    		printDebug("generateProjections(), previous: "+previous+", current: "+current+", on day "+i);
		if (!current.equals(previous)) {
		    double value = convertScalarToDouble(previous);
		    if ((value > 0) && (value != Double.NaN)) {
			start = TimeUtils.addNDays(invpg.getStartTime(), periodBegin);
			// Period spans a single day
			if (periodBegin == (i-1)) {
			    // Subtract 12 hours from the start time to ensure a non-zero time span
			    start = start - (TimeUtils.MSEC_PER_HOUR*12);
			}
			// safety accounts for the safety level which the inventory attempts to maintain.
			// add the safety 'demand' to projected deman.
			safety = determineSafetyIncrement(periodBegin, i-1, inventory);
			Task t = newProjectSupplyTask(inventory, start,
						      TimeUtils.addNDays(invpg.getStartTime(), i-1), 
						      createIncrementedDailyRate(previous,safety));
//  			if(safety>0){
//  			    printDebug(0, "  !!! projection with dailyRate=" + createDailyRate(previous)+" and safetyRate="+safety);
//  			}
			projections.add(t);
			invpg.addDueIn(t);
			//      			printDebug("generateProjections(), created Projection task. Start: "+TimeUtils.dateString(start)+", End: "+
			//  				   TimeUtils.dateString(TimeUtils.addNDays(invpg.getStartTime(), i))+", Value: "+previous);
		    }
		    periodBegin = i;
		    newCurrent = false;
		}
		else {
		    newCurrent = true;
		}
	    }
	    previous = current;
	}
	if ((newCurrent) && (previous != null)) {
	    double value = convertScalarToDouble(previous);
	    if ((value > 0) && (value != Double.NaN)) {
		start = TimeUtils.addNDays(invpg.getStartTime(), periodBegin);
		// Period spans a single day
		if (periodBegin == (days-1)) {
		    // Subtract 12 hours from the start time to ensure a non-zero time span
		    start = start - (TimeUtils.MSEC_PER_HOUR*12);
		}
		safety = determineSafetyIncrement(periodBegin, days-1, inventory);
		Task t = newProjectSupplyTask(inventory, start,
					      TimeUtils.addNDays(invpg.getStartTime(), days-1), 
					      createIncrementedDailyRate(previous,safety));
		//  		printDebug(0, "!!!!!!!!!!!!!!!!!!!! projection with dailyRate=" +
		//  			   createDailyRate(previous)+" and safetyRate="+safety+"\n        Task: "+ TaskUtils.taskDesc(t));
		projections.add(t);
		invpg.addDueIn(t);
	    }
	    //  	    printDebug("generateProjections(), previous: "+previous+", current: "+current);
	}
	return projections.elements();
    }

    protected abstract double getReorderLevel(Inventory inventory, int day);

    protected double determineSafetyIncrement(int periodStart, int periodEnd, Inventory inventory) {

	double first = getReorderLevel(inventory, periodStart);
	double highest = first;
	int highestDay = periodStart;
	for(int i = periodStart; i<=periodEnd; i++){
	    double reorder = getReorderLevel(inventory, i);
	    if(reorder>highest){
		highest = reorder;
		highestDay = i;
	    }
	}
	if(periodEnd==periodStart) {
	    return 0.0;
	} else {
	    return ((highest-first)/(periodEnd-periodStart));
	}
    }

    protected Rate createDailyRate(Measure qty) {
	Rate rate = null;
	if (qty instanceof Volume) {
	    rate = FlowRate.newGallonsPerDay(((Volume)qty).getGallons());
	} else if (qty instanceof Count) {
	    rate = CountRate.newEachesPerDay(((Count)qty).getEaches());
	} else if (qty instanceof Mass) {
	    rate = MassTransferRate.newShortTonsPerDay(((Mass)qty).getShortTons());
	}
	return rate;
    }

    protected Rate createIncrementedDailyRate(Measure qty, double increment) {
	Rate rate = null;
	if (qty instanceof Volume) {
	    rate = FlowRate.newGallonsPerDay(increment+((Volume)qty).getGallons());
	} else if (qty instanceof Count) {
	    rate = CountRate.newEachesPerDay(increment+((Count)qty).getEaches());
	} else if (qty instanceof Mass) {
	    rate = MassTransferRate.newShortTonsPerDay(increment+((Mass)qty).getShortTons());
	}
	return rate;
    }

    protected Task newProjectSupplyTask(Inventory inventory, long start, long end, Rate rate) {
	Task parentTask = inventoryPlugIn_.findOrMakeMILTask(inventory);
	// Create start and end time preferences (strictly at)
	ScoringFunction score;
	Vector prefs = new Vector();
	score = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.START_TIME, start));
	prefs.addElement(ldmFactory_.newPreference(AspectType.START_TIME, score));
	score = ScoringFunction.createStrictlyAtValue(new AspectValue(AspectType.END_TIME, end));
	prefs.addElement(ldmFactory_.newPreference(AspectType.END_TIME, score));
	Vector prep_phrases = new Vector();
	prep_phrases.add(newPrepositionalPhrase(Constants.Preposition.FOR, myOrgName_));
	prep_phrases.add(newPrepositionalPhrase(Constants.Preposition.OFTYPE, supplyType_));
	prep_phrases.add(newPrepositionalPhrase(Constants.Preposition.MAINTAINING, inventory));
	InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	NewTask t =  (NewTask)buildTask(parentTask, 
					Constants.Verb.PROJECTSUPPLY, 
					invpg.getResource(),
					prep_phrases,
					prefs.elements());
        t.setPreference(TaskUtils.createDemandRatePreference(ldmFactory_, rate));
        t.setPreference(TaskUtils.createDemandMultiplierPreference(ldmFactory_, 1.0));
 	t.setCommitmentDate(new Date(end));
//  	printDebug("newProjectSupplyTask(), created new ProjectSupply task "+TaskUtils.taskDesc(t));
  	return t;
    }

    protected Inventory getInventoryForTask (Task task) {
	return inventoryPlugIn_.findOrMakeInventory(supplyType_,(Asset)task.getDirectObject());
    }

    // ********************************************************
    //                                                        *
    // Generate/Handle Due Ins Section                        *
    //                                                        *
    // ********************************************************


    protected abstract void generateHandleDueIns();


    protected void addPreviousRefills() {

    	printDebug("      : addPreviousRefills()");
	int total=0;
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	Inventory inv;
	InventoryPG invpg;
	while (inventories.hasMoreElements()) {
	    inv = (Inventory)inventories.nextElement();
	    invpg = (InventoryPG)inv.searchForPropertyGroup(InventoryPG.class);
	    // maintainInventory Task is the parent of all the refills for this inventory
	    Task maintainInventory = inventoryPlugIn_.findOrMakeMILTask(inv);
	    // should never be null but may want to add a check AHF
	    total += invpg.addPreviousRefillsToInventory(maintainInventory);
	    invpg.determineInventoryLevels();
//  	    invpg.printInventoryLevels(inv, clusterId_);
	}
    	printDebug(2,"end addDueIns(), number of refillTasks is "+total);
    }

    // Integer number of days needed to get anything
    // Change to policy!!!
    public abstract int getOrderShipTime(); 
    public long defaultRefillStartTime(long time, Inventory inv) {
	long default_start;
	int day = TimeUtils.getDaysBetween(startTime_, time);
	int start = day - getOrderShipTime();
	if (start > 0) {
	    time = TimeUtils.addNDays(startTime_, start);
	}
	else {
	    time = startTime_;
	}
	return time;
    }

    public long defaultRefillEndTime(long time, Inventory inv) {
	if (time == startTime_) {
	    time = TimeUtils.addNDays(startTime_, 1);
	}
	return time;
    }

    public Task createRefillTask(Inventory inv, double refill_qty, long time) {
	Asset item = getInventoryAsset(inv);
	// create request task
	Preference p_start, p_end,p_qty;

	long end_time = defaultRefillEndTime(time,inv);
	long start_time = defaultRefillStartTime(time,inv);

	p_start = createDateAfterPreference(AspectType.START_TIME, start_time);
	p_end = createDateBeforePreference(AspectType.END_TIME,end_time);

	// AMY - SF need early (OPlan Start date), best (defaultRefillEndDate) and Late (Plan End date)
//  	    double early = (double)oplan.getStartTime();
//  	    double best = (double)end_time;
//  	    double late = (double)oplan.getEndTime();
//  	    AspectValue earlyAV = new AspectValue(AspectType.END_TIME, early);
//  	    AspectValue bestAV = new AspectValue(AspectType.END_TIME, best);
//  	    AspectValue lateAV = new AspectValue(AspectType.END_TIME, late);
//  	    ScoringFunction endTimeSF = ScoringFunction.createVScoringFunction(earlyAV, bestAV, lateAV);
//  	    p_end = ldmFactory_.newPreference(AspectType.END_TIME, endTimeSF);

	AspectValue lowAV = new AspectValue(AspectType.QUANTITY, 0.01);
	AspectValue bestAV = new AspectValue(AspectType.QUANTITY, refill_qty);
	AspectValue highAV = new AspectValue(AspectType.QUANTITY, refill_qty+1.0);
	ScoringFunction qtySF = ScoringFunction.createVScoringFunction(lowAV, bestAV, highAV);
	p_qty = ldmFactory_.newPreference(AspectType.QUANTITY, qtySF);
//  	p_qty = createQuantityPreference(AspectType.QUANTITY, refill_qty);

	
	Vector prefs = new Vector();
	prefs.addElement(p_start);
	prefs.addElement(p_end);
	prefs.addElement(p_qty);

	Vector pp_vector = new Vector();
	pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.FOR, myOrgName_));
	pp_vector.add(newPrepositionalPhrase(Constants.Preposition.OFTYPE, supplyType_));

	Object io;
	Enumeration geolocs = AssetUtils.getGeolocLocationAtTime(myOrganization_, start_time);
	if (geolocs.hasMoreElements()) {
	    io = (GeolocLocation)geolocs.nextElement();
	} else {
	    io = thisGeoloc_;
	}
	pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.TO, io));

	pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.REFILL));

	NewTask task =  (NewTask)buildTask(null, Constants.Verb.SUPPLY, 
					   item, pp_vector, prefs.elements());
	return task;

    }


    // ********************************************************
    //                                                        *
    // Adjust Withdraws Section                               *
    //                                                        *
    // ********************************************************


    protected abstract void adjustWithdraws();

    protected void adjustForInadequateInventory() {
	// GLK NEED TO FILL IN, POSSIBLY FROM GIM
  	printLog("adjustForInadequateInventory()-----------------------------Inventory before failUnfillableDueOuts()");
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	Inventory inventory;
	InventoryPG invpg;
	// For each inventory
	while (inventories.hasMoreElements()) {
	    inventory = (Inventory)inventories.nextElement();
	    invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	    int day = invpg.getFirstPlanningDay();
	    DueOut lowestPriorityDueOut = null;
	    // Find lowest priority task to fail
	    while ((day =refillNeeded(inventory,day)) != DONE) {
		printDebug("Inventory level before failing allocation is "+
			   TimeUtils.dateString(TimeUtils.addNDays(invpg.getStartTime(),day))+" : "+invpg.getLevel(day)+
			   ", Reorder level :"+getReorderLevel(inventory, day));
		lowestPriorityDueOut = invpg.getLowestPriorityDueOutBeforeDay(day);
		// Is there another request to fail?
		if (lowestPriorityDueOut != null) {
		    // Fail Request
		    Task task = lowestPriorityDueOut.getTask();
		    printDebug("    ---->Failing due-out: "+TaskUtils.taskDesc(task)+"\n");
		    
		    // DEBUG PRINTOUT
//  		    Enumeration dueIns = invpg.getAllDueIns();
//  		    printInventory(inventory,dueIns,0);
//  		    invpg.printInventoryLevels(inventory, clusterId_);

		    PlanElement pe = task.getPlanElement();
		    if (pe instanceof Allocation) {
			failAlloc(inventory, (Allocation)pe);
			lowestPriorityDueOut.setFilled(false);
			lowestPriorityDueOut = null;
			// recalculate inventory levels
			invpg.determineInventoryLevels();
			day++;
			printDebug("Inventory level after  failing allocation is "+
				   TimeUtils.dateString(TimeUtils.addNDays(invpg.getStartTime(),day))+" : "+invpg.getLevel(day));
		    }
		    else {
			printError("adjustForInadequateInventory(),  PlanElement is not an Allocation to inventory!!!");
		    }
		}
		else {
		    printError("adjustForInadequateInventory(), Need to fail back but no requests found on day "+
			       TimeUtils.dateString(TimeUtils.addNDays(invpg.getStartTime(),day)));
		    day++;
		}
		printDebug(" ");
	    }
	}
    }

    protected abstract int  refillNeeded(Inventory inventory, int startDay);


    public void failAlloc(Inventory inv, Allocation alloc) {
	AllocationResult ar = alloc.getEstimatedResult();
	Task task = alloc.getTask();
 	Task parent = task.getWorkflow().getParentTask();
// 	printLog("Failing allocation: "+alloc.getUID()+" for task:"+TaskUtils.taskDesc(task)+" that supports dueOut: "+TaskUtils.taskDesc(parent));
//  	    publishRemoveAllocation(alloc);
	ar = buildQuantityFailedAllocationResult(task);
//  	    publishFailedAllocation(task, ar);	
	publishAllocation(task, alloc.getAsset(), alloc.getRole(), ar);
    }
    
    protected AllocationResult buildQuantityFailedAllocationResult(Task task) {
	Enumeration prefs = task.getPreferences();
	Vector aspects = new Vector();

	// do something really simple for now.
	while (prefs.hasMoreElements()) {
	    Preference pref = (Preference) prefs.nextElement();
	    int type = pref.getAspectType();
	    ScoringFunction sf = pref.getScoringFunction();
	    AspectValue av;
		// allocate as if you can do it at the "Best" point
		double result_pref = pref.getScoringFunction().getBest().getValue();
		if(type == AspectType.QUANTITY) {
		    // this indicates we cannot supply anything!!
		    result_pref= 0.0;
		}
 		av = new AspectValue(type,result_pref);
		double myresult = sf.getScore(av);
		aspects.addElement(av);
	}

	AspectValue[] aspectarray = new AspectValue[aspects.size()];
	for (int i = 0; i < aspectarray.length; i++)
	  aspectarray[i] =  (AspectValue)aspects.elementAt(i);
        AllocationResult ar = ldmFactory_.newAVAllocationResult(1.0, false, aspectarray);
	return ar;
    }

    // ********************************************************
    //                                                        *
    // CheckForOverflow Section - only relevent for limited   *
    //                            capacity inventories        *
    //                                                        *
    // ********************************************************

    // Check for overflow
    protected void checkForOverflow() {
	Inventory inventory;
	printDebug(1, "      :checkForOverflow(), START");
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	while (inventories.hasMoreElements()) {
	  inventory = (Inventory)inventories.nextElement();
	  checkInventoryForOverFlow(inventory);
	 }
	printDebug(1, "checkForOverflow(), END");
    }

    protected void checkInventoryForOverFlow(Inventory inventory) {
	InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	if (!invpg.getFillToCapacity()) {
	    // If no capacity restriction, done.
	    return;
	}
	double capacity = convertScalarToDouble(invpg.getCapacity());
	Task refill_task = null;
	// looking at the inventory level for each planning day
	int i = invpg.getFirstPlanningDay();
	Integer day  = invpg.getFirstOverflow(i, clusterId_);
	while (day != null) {
	    i = day.intValue();
	    // There had better be a refill on this day, else why would we overflow
	    printDebug(1,inventoryDesc(inventory)+" fillToCapacity()="+invpg.getFillToCapacity()+
		       " ABOUT TO overflow "+convertScalarToDouble(invpg.getLevel(i))+
		       " max on hand:"+convertScalarToDouble(invpg.getCapacity())+" on day "+i);
	    refill_task = invpg.getRefillOnDay(i);
	    if (refill_task != null) {
		invpg.removeDueIn(refill_task);
		publishRemoveTask(refill_task);
		invpg.determineInventoryLevels();	  
		printDebug(1,"checkForOverflow remove refill "+TaskUtils.taskDesc(refill_task));
	    }
	    day = invpg.getFirstOverflow(++i, clusterId_);
	}
    }


    // ********************************************************
    //                                                        *
    // Refresh ScheduledContentPG on Inventory  Section       *
    //                                                        *
    // ********************************************************

    protected void refreshInventorySchedule() {
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	Inventory inventory;
	InventoryPG invpg;

	printDebug("LAST STEP: REFRESHINVENTORYSCHEDULE()");
	while (inventories.hasMoreElements()) {
	    inventory = (Inventory)inventories.nextElement();
	    invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
// 	    invpg.printInventoryLevels(inventory, clusterId_);
	    invpg.updateContentSchedule(inventory);
	}
    }

    protected void clearInventorySchedule() {
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	Inventory inventory;
	InventoryPG invpg;

	printDebug("LAST STEP: CLEARINVENTORYSCHEDULE()");
	while (inventories.hasMoreElements()) {
	    inventory = (Inventory)inventories.nextElement();
	    invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	    invpg.clearContentSchedule(inventory);
	}
    }

    /** method called from update when a GLS Rescind is detected.   Right now
     here it just calls clearInventorySchedule.   It may be overloaded 
     by subclasses*/
    protected void handleGLSRescind() {
	clearInventorySchedule();
    }

    // ********************************************************
    //                                                        *
    // Utilities Section                                      *
    //                                                        *
    // ********************************************************


    /** @return a double indicating the amount requests by the task (in terms of the standard unit of measure for the item) */
    protected double getAmountRequested(Task task) {
	return TaskUtils.getPreference(task, AspectType.QUANTITY);
    }

    public boolean isBattalionLevel(){
	return (clusterId_.toString().indexOf("BN")>0);
    }

    /** 
	Given a Scalar, return a double value representing
	Gallons for Volume,
	Eaches for Count and
	Short Tons for Mass.
    **/
    protected double convertScalarToDouble(Scalar measure) {
	double d = Double.NaN;
	if (measure instanceof Volume) {
	    d = ((Volume)measure).getGallons();
	} else if (measure instanceof Count) {
	    d = ((Count)measure).getEaches();
	} else if (measure instanceof Mass) {
	    d = ((Mass)measure).getShortTons();
	}
	return d;
    }

    // ********************************************************
    // PRINT/DEBUG Section                                    * 
    // ********************************************************

    protected void debugInventory() {
	// Print Inventory status for debug
	int numItems = printInventoryStatus();
	printLog("COMPLETED SOURCING <"+supplyType_+">");
	printLog("SOURCING COMPLETE for tasks involving "+numItems+" inventory items on:"+ new GregorianCalendar().getTime());
	printInventoryBins(0);
    }

    void printInventoryBins(int priority) {
	printDebug(priority,"printInventoryBins");

	Enumeration bins = inventoryPlugIn_.getInventoryBins(supplyType_);
	if(bins.hasMoreElements()){
	    printDebug(priority,"\n\n***Status of inventory  after accounting for due-ins and due-outs: "+TimeUtils.dateString(getAlpTime()));
	} else {
	    printDebug(priority,"\n\n***FUNNY -- no inventory assets for "+myOrgName_);
	}
	while (bins.hasMoreElements()){
	    Inventory bin = (Inventory)bins.nextElement();
	    InventoryPG invpg = (InventoryPG)bin.searchForPropertyGroup(InventoryPG.class);
	    Enumeration dueIns = invpg.getAllDueIns();
	    printInventory(bin,dueIns,priority);
	    printDebug(priority,"\n_____________________________________________________________________________\n");
	}
    }

    private int printInventoryStatus() {

	Enumeration inventory = inventoryPlugIn_.getInventoryBins(supplyType_);
	if(inventory.hasMoreElements()){
	    printDebug("\n\n\n**********Inventory status at the end of sourcing********");
	}
	int nItems = 0;
	while (inventory.hasMoreElements()) {
	    nItems = nItems+1;
	    Inventory bin = (Inventory)inventory.nextElement();
	    ScheduledContentPG scp = bin.getScheduledContentPG();
	    Schedule sched = scp.getSchedule();
	    if (sched == null) {
		printError("buildAssetsFile()  null sched for bin:"+bin);
		continue;
	    }
	    String nsn = null;
	    Asset asset = scp.getAsset();
	    if (asset != null) {
		TypeIdentificationPG tip = asset.getTypeIdentificationPG();
		if (tip!= null) {
		     nsn = tip.getTypeIdentification();
		} else {
		    printDebug("asset "+asset+" has null getTypeIdentificationPG()");
		}
	    } else {
		printDebug("Inventory: "+bin+" has no asset");
	    }
	}
	return nItems;
    }

    /** Update the current DaysOnHandPolicy */
    protected boolean updateDaysOnHandPolicy(Enumeration policies) {
	DaysOnHandPolicy pol;
	boolean changed = false;
//  	printDebug("updateDaysOnHandPolicy(), Days On Hand Policy for "+supplyType_+". DaysOnHand: "+daysOnHand_+
//  		   ", Days Forward: "+daysForward_+", Days Backward: "+daysBackward_+", Window size: "+
//  		   (daysForward_+daysBackward_));
	while (policies.hasMoreElements()) {
	    pol = (DaysOnHandPolicy)policies.nextElement();
	    int days = pol.getDaysOnHand();
            if ((days >= 0) && (days != daysOnHand_)) {
		daysOnHand_ = days;
		changed = true;
	    }
	    int forward = pol.getDaysForward();
	    if ((forward >= 0) && (forward != daysForward_)) {
		daysForward_ = forward;
		changed = true;
	    }
	    int backward = pol.getDaysBackward();
	    if ((backward >= 0) && (backward != daysBackward_)) {
		daysBackward_ = backward;
		changed = true;
	    }
	    double multiplier = pol.getGoalLevelMultiplier();
	    if ((multiplier > 1.0) && (multiplier != goalLevelMultiplier_)) {
		goalLevelMultiplier_ = multiplier;
		changed = true;
	    }
	}
	if (changed) {
	    printDebug("updateDaysOnHandPolicy(), Days On Hand Policy CHANGED for "+supplyType_+". DaysOnHand: "+daysOnHand_+
		       ", Days Forward: "+daysForward_+", Days Backward: "+daysBackward_+", Window size: "+
		       (daysForward_+daysBackward_)+", goal level multiplier: "+goalLevelMultiplier_);
	}		
	return changed;
    }

}


