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

import org.cougaar.*;
import org.cougaar.core.cluster.*;
import org.cougaar.domain.planning.ldm.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.util.ConfigFileFinder;
import org.cougaar.util.UnaryPredicate;
import java.io.*;
import java.lang.*;
import java.util.*;
import org.cougaar.domain.glm.asset.*;
import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.oplan.Oplan;
import org.cougaar.domain.glm.plan.*;
import org.cougaar.domain.glm.ALPFactory;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.asset.*;
import org.cougaar.domain.glm.asset.ProjectionWeight;

/** Allocate SUPPLY tasks to local inventory (if there is any) or to 
 *  the closest supplier.
 */
public abstract class GeneralInventoryManager extends InventoryManager {
    protected IncrementalSubscription refillAllocs_ = null;
    protected NewTask defaultSupplyTask = 
	(NewTask)buildNewTask(null,Constants.Verb.SUPPLY,null);




    /** Constructor */
    public GeneralInventoryManager(InventoryPlugIn plugin, Organization org, String type)
    {
	super(plugin, org, type);
	printLog("Constructor type:"+type);
	initialize();

    }


    // ********************************************************
    //                                                        *
    // Point Of Entry to GeneralInventoryManager              *
    //                                                        *
    // ********************************************************

    // Uses update() from superclass (Template Method pattern) (learned at JavaOne)
    // It should not be overridden.
    // This method is called everytime a subscription has changed.



    // ********************************************************
    //                                                        *
    // Need Update / Reset Section                            *
    //                                                        *
    // ********************************************************


    protected boolean needUpdate() {
    
	boolean failed_refill = updateAllocations();
	boolean refill_changed = refillAllocs_.getChangedList().hasMoreElements(); 
	boolean inventory_changed = modifiedInventorySubscription_.getChangedList().hasMoreElements();
	boolean on_hand_policy_changed = updateDaysOnHandPolicy(onHandPolicySubscription_.getAddedList()) ||
	                                 updateDaysOnHandPolicy(onHandPolicySubscription_.getChangedList());

	// Allocations of tasks with quantity > 0 to Inventory objects
	// inventoryAllocSubscription_ only used to determine when to run processor.
	// Inventory objects held in the plugin.
	boolean allocatedInventories = inventoryPlugIn_.isSubscriptionChanged(inventoryAllocSubscription_);
	//  	printDebug("allocatedInventories: "+allocatedInventories);
	if(allocatedInventories){
 	    printLog("<"+supplyType_+"> UPDATING INVENTORIES due to changed Inventory allocations.");
	}

	return(failed_refill || refill_changed  || allocatedInventories || inventory_changed || on_hand_policy_changed);
    }


    // Updates the alloc result on changed refill allocations.
    // This passes the alloc result up the chain (probably not necessary)
    // returns true if any of the new alloc results have failed.
    protected boolean updateAllocations() {
	int num_pub = 0;
	PlanElement pe;
	Task task;
	boolean failed_refill = false;

	Enumeration allocs = refillAllocs_.getChangedList();
	while (allocs.hasMoreElements()) {
	    pe = (PlanElement)allocs.nextElement();
	    task = pe.getTask();
	    // 	    printDebug("updateAllocation: "+pe+"  task:"+task);
	    AllocationResult rep_res = pe.getReportedResult();
	    
	    // Debug for finding two week intervals on alloc results
	    // if (rep_res != null) {
	    //   printLog("updateAllocations() ReportedResult start:"+new Date((long)getStartTime(rep_res)) +
	    //            " end:"+ new Date((long)getEndTime(rep_res))+ "task:"+TaskUtils.taskDesc(task));
	    //  		}
	    if ((rep_res != null) &&(!rep_res.equals(pe.getEstimatedResult()))) { 
		updateAllocationResult(pe);
		if (!rep_res.isSuccess()) {
		    failed_refill = true;
		    // reorder failure - do something....
		    printDebug("<"+supplyType_+"> Failed allocation. Requested:"+
			       TaskUtils.taskDesc(task)+", got"+arDesc(rep_res));
		}
	    }
	}
	return failed_refill;
    }



    // ********************************************************
    //                                                        *
    // Generate/Handle Due Ins Section                        *
    //                                                        *
    // ********************************************************

    protected void generateHandleDueIns() {
    	printDebug("Step 3: generateHandleDueIns()");
	addPreviousRefills();
	boolean allocatedInventories = inventoryPlugIn_.isSubscriptionChanged(inventoryAllocSubscription_);
	// Execution requires this to be done whenever we run
	refillInventories();
    }

    // Refill Inventories

    protected void refillInventories() {
    	printDebug(1,"Refillinventories() Start");
//       	printInventoryBins();
	// notice low and high levels resulting from this allocation
	Inventory inventory;
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
//  	printDebug(1,"<"+supplyType_+"> refillInventories()");
	while (inventories.hasMoreElements()) {
	    inventory = (Inventory)inventories.nextElement();
	    InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	    invpg.determineInventoryLevels();
	    // Start on the first day of inventory activity if the activity starts in the future
	    // otherwise start planning today
	    int day = invpg.getFirstPlanningDay();
 	    int today = invpg.getToday();
	    if (day < (today+getOrderShipTime())) {
		day = today+getOrderShipTime();
	    }
	    while ((day = refillInventory(inventory,day)) != DONE) {
// 		invpg.determineInventoryLevels();
	    }

//  	    if (isBattalionLevel()){
//  		printDebug(1,"After refillInventories");
//  		printInventory(inventory,invpg.getAllDueIns());
//  	    }
	    //      	    invpg.printInventoryLevels(inventory, clusterId_);
	    
	}
    }

    protected int refillInventory(Inventory inventory, int startDay) {
	InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
//    	printDebug(1,"refillInventory on day "+startDay+" for inventory: "+AssetUtils.assetDesc(invpg.getResource()));
	int day = refillNeeded(inventory, startDay);
	if (day == DONE) {
	    return DONE;
	}
	int nRefills = 0;
	defaultSupplyTask.setDirectObject(invpg.getResource());
	Task task = (Task)defaultSupplyTask;
	Task refill;
	if((refill = invpg.refillAlreadyFailedOnDay(day)) != null) {
	    // Really, really don't like this, ask Rusty
	    day = getPolicyForNextReorderDay(refill, day, inventory);
	    printDebug(1,"refillInventory already failed, next day to look at is "+day);
	} else if ((invpg.getProjectionWeight().getProjectionWeight(task,day - invpg.getToday())) <=0.0){
	    // This test is to avoid generating refill SUPPLY tasks for times when the system
	    //  will ignore their effect on inventory
	    printDebug(1,"refillInventory asked to look beyond requisition window");
	    return DONE;
	} else {
//  	    printDebug(1,"orderRefill for "+inventoryDesc(inventory)+" on day "+day);
	    Task refillTask = orderRefill(inventory, day);
	    if(refillTask!=null){
		invpg.determineInventoryLevels();
//  		invpg.printInventoryLevels(inventory, clusterId_);
		//to see inventory printLog(date.getTime()+" refilling leaves: "+invLevel+" at: "+ inventory.getUID()+"\n");
	    }
	    // Increment the day, done
	    day++;
//  	    printDebug(1,"refillInventory ordered refill, moving to day: "+day);
	}
//  	printDebug(1, "<"+supplyType_+"> Sending "+nRefills+" refill orders");
	return day;
    }

    protected abstract double getGoalLevel(Inventory inventory, int day);


    // Refill has already failed for this day, try again?  Give up?
    protected abstract int getPolicyForNextReorderDay(Task refill, int day, Inventory inv);

    /** Generate a refill order to replenish the item in storage,
     *  assuming we already have determined we have at least an
     *  Economic Reorder Quantity. Allocate task to provider Org. */
    protected Task orderRefill(Inventory inventory, int day) {
	InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	Date date = new Date(TimeUtils.addNDays(invpg.getStartTime(), day));
	double currentInventory = convertScalarToDouble(invpg.getLevel(day));
	double goal_level = getGoalLevel(inventory,day);

	double refill_qty = goal_level - currentInventory;


	if(!invpg.getFillToCapacity()) {
	    // ** THIS IS A KLUDGE FOR SUBSISTENCE -- WITHOUT IT WE FAIL THE FIRST ORDER.
	    //    SHOULDN'T HAPPEN LIKE THAT -- FIX ASAP -- RUSTY AND AMY!!
	    refill_qty = refill_qty*1.3;
	    // *** KLUDGE ********
	}


	//  	if (isBattalionLevel()){
	//  	    printLog("orderRefill day "+day+"("+TimeUtils.dateString(date)+") level: current= "+currentInventory+ " goal = "+ goal_level);
	//  	}
	if (refill_qty > 0 ) {
	    Task task = null;	    
	    Task prev_refill = invpg.getRefillOnDay(day);
	    if(prev_refill!=null) {
		return orderRefillWithPrevious(inventory, day, invpg,date,currentInventory,goal_level,refill_qty);
	    } else {
		if (invpg.getCapacity() instanceof Count) {
		    refill_qty=java.lang.Math.ceil(refill_qty);
		}
		task = createRefillTask(inventory, refill_qty,  TimeUtils.addNDays(invpg.getStartTime(), day));
		//  		printDebug(1,"GeneralInventoryManager, orderRefill(), day is "+day+"for "+TaskUtils.taskDesc(task));		

		// FIX ME - sets to today??? check dates.
		// 	task.setCommitmentDate(date);
		//      		printDebug(1,"orderRefill task:"+TaskUtils.taskDesc(task));
		Task parentTask = inventoryPlugIn_.findOrMakeMILTask(inventory);
		plugin_.publishAddToExpansion(parentTask, task);
		invpg.addDueIn(task);
	    }
	    //to see inventory 
	    //  	if (isBattalionLevel()){
	    //    	    printLog("Refill " //+ inventory.getUID() + " with= "
	    //  		     + TaskUtils.taskDesc(task));
	    //  	}
	    //   	    printLog("Refill: " + TaskUtils.taskDesc(task));
	    return task;
	} else {
	    printLog("OrderRefill qty < 0: "+refill_qty+" = "+goal_level+" - "+currentInventory);
	    return null;
	}
    }

    protected Task orderRefillWithPrevious(Inventory inventory, int day, InventoryPG invpg, Date date,
					   double currentInventory, double goal_level,
					   double refill_qty){

	//   		printDebug(1,"orderRefill with previous on day: "+day+
	//  			   " current= "+currentInventory+ " goal_level = "+ goal_level);
	Task task = null;	    
	Task prev_refill = invpg.getRefillOnDay(day);
	Allocation alloc =(Allocation)prev_refill.getPlanElement();
	AllocationResult report = null;

	if(alloc!=null) {
	    report = alloc.getReportedResult();
	}
	if (report!=null) {
	    if (!report.isSuccess()){
		printLog("Known failed refill: "+TaskUtils.taskDesc(prev_refill));
		return null;
	    } else {
		// check if the refill came too late....
		if (TaskUtils.getRefillTime(prev_refill) > TaskUtils.getEndTime(prev_refill)) {
		    printDebug(0,"previous refill succeeded too late: "+
			       TaskUtils.taskDesc(prev_refill)+
			       " to fill: " +refill_qty);
		    if (TaskUtils.getQuantity(prev_refill) >= refill_qty) {
				// the refill would have been enough 
			printDebug(0,"NO REFILL!");
			return null;
		    } 
		} else {
		    // previous refill already added to inventory, need increase 
		    // requested amount by the old plus the new 
		    printDebug(0,"Previous refill already added to the inventory on the right date, still need more");
		    refill_qty += TaskUtils.getQuantity(prev_refill);
		}
	    }
	} else if (TaskUtils.getQuantity(prev_refill) >= refill_qty) {
	    printDebug(0,"NEW POLICY- DON'T LOWER PREVIOUS ORDER -- NO REFILL!");
	    return null;
	}

	// Send orders for whole items, i.e. do not order 0.5 O-rings
	if (invpg.getCapacity() instanceof Count) {
	    if(refill_qty<1.0){
		return null;
	    } else {
		refill_qty=java.lang.Math.ceil(refill_qty);
		if (refill_qty == 0)
		    return null;
	    }
	}
	printDebug(1, "Refill Quantity is: "+refill_qty);
	task = createRefillTask(inventory, refill_qty, TimeUtils.addNDays(invpg.getStartTime(), day));
	// if prev_refill!= null, and we have no reported failure, then we
	//  should modify this refill task

	printLog("Replacing: "+TaskUtils.taskDesc(prev_refill)+" by: "+
		 TaskUtils.taskDesc(task));
	publishRemoveTask(prev_refill);
	invpg.removeDueIn(prev_refill);
	Task parentTask = inventoryPlugIn_.findOrMakeMILTask(inventory);
	plugin_.publishAddToExpansion(parentTask, task);
	invpg.addDueIn(task);
	return task;
    }



    protected int refillNeeded(Inventory inventory, int startDay) {
	// 	printInventory(inventory,null);
	boolean needed = false;
	InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	int days = invpg.getPlanningDays();
// 	printDebug(1, "InventoryManager, refillNeeded(), current day: "+startDay+", planning days :"+ days);
	int day = startDay;
	for (; day<days-1; day++) {


//  	    double qty = java.lang.Math.ceil(convertScalarToDouble(invpg.getLevel(day)));

	    // WHY DO WE TAKE A CEIL??
	    double qty = convertScalarToDouble(invpg.getLevel(day));

	    // reorder level depends on day, now, since it is often N days of supply
	    double reorder_level = getReorderLevel(inventory,day);
	    if (qty < reorder_level) {
//   		printDebug(1,inventoryDesc(inventory)+" needs refill on day: "+day+
//   			   " level= "+qty+" reorder_level="+reorder_level);
		needed = true;
		break; // order refill on this day
	    } else {
// 		printDebug(1,inventoryDesc(inventory)+" doesn't need refill on day"+i+
// 			   ", level= "+qty+" reorder_level="+reorder_level);
	    }
	}
	if (needed) {
// 	    printDebug(1, "InventoryManager, refillNeeded(), next day is "+day);
//  	    ProjectionWeight pw = invpg.getProjectionWeight();
	    return day;
	}
	else {
	    return DONE;
	}
    }

   
    // ********************************************************
    //                                                        *
    // Adjust Withdraws Section                               *
    //                                                        *
    // ********************************************************

    protected void adjustWithdraws() {
	boolean refill_changed = refillAllocs_.getChangedList().hasMoreElements(); 

	if (refill_changed) {
	    adjustForInadequateInventory();
	    passPreviouslyFailedDueOuts();
	}
    }

    // Pass Previously Failed Dueouts

    protected void passPreviouslyFailedDueOuts() { 
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	printDebug("PassPreviouslyFailedDueOuts()");
	Inventory inventory;
	InventoryPG invpg;
	while (inventories.hasMoreElements()) {
	    inventory = (Inventory)inventories.nextElement();
	    invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	    Vector dueouts = invpg.getPreviouslyFailedDueOuts();
	    Enumeration e = dueouts.elements();
	    DueOut dueout;
	    Task task;
	    while (e.hasMoreElements()) {
		dueout = (DueOut)e.nextElement();
		// If it was filled this time then change allocation result
		if (dueout.getFilled()) {
		    task = dueout.getTask();
		    printDebug(1, "passPreviouslyFailedDueOuts "+TaskUtils.taskDesc(task)+" didn't fail this run");
		    // triggers SupplyInventoryAllocator to re allocate this task
		    publishChangeTask(task);
		}
	    }
	}
    }

    // ********************************************************
    //                                                        *
    // Utilities Section                                      *
    //                                                        *
    // ********************************************************

    // Public

    public Enumeration getAllScheduleElements(Inventory inventory) {
	ScheduledContentPG scp = inventory.getScheduledContentPG();
	return scp.getSchedule().getAllScheduleElements();
    }
        
    // this should be over-ridden for specialized inventory managers
    public double getMinRefillQuantity(Inventory inventory) {
	return inventory.getVolumetricStockagePG().getMinReorderVolume().getGallons();
    }

    void printInventoryBins() {
	printInventoryBins(0);
    }


    // ********************************************************
    //                                                        *
    // ????????? Section                                      *
    //                                                        *
    // ********************************************************



    Date negInventoryDate(Inventory inventory, Date afterDate) {
	if(afterDate==null){
	    afterDate= new Date(TimeUtils.addNDays(startTime_,0));
	}
	Schedule sched = inventory.getScheduledContentPG().getSchedule();
	Enumeration sched_el = sched.getAllScheduleElements();
	Date date;
	while (sched_el.hasMoreElements()) {
	    // reorder_level = sum of previous daysOnHand_ number of days of supply
	    QuantityScheduleElement qse = (QuantityScheduleElement)sched_el.nextElement();
	    date = qse.getStartDate();
	    if (date.after(afterDate)) {
		if (qse.getQuantity() < 0.0) {
		    return date;
		}
	    }
	}
	return null;
    }

    protected double safetyLevelDays(){
	return daysOnHand_;
    }




    protected void addStartEndTimePref(NewTask task, Task parent_task) {
	
	long parent_end = TaskUtils.getEndTime(parent_task);
	 // allow for transportation time
	long end = parent_end;
	Preference p_end = createDateBeforePreference(AspectType.END_TIME, end);
	task.addPreference(p_end);

	long start =  parent_end - MSEC_PER_DAY;
	Preference p_start = createDateAfterPreference(AspectType.START_TIME, start);
	task.addPreference(p_start);

    }

    /** Figure out how much of an item we can and should draw from local storage. */
    protected double calcAmountStockToDraw(Inventory inventory, double requestedAmount, Date date)
    {
	InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
	double level = convertScalarToDouble(invpg.getLevel(date.getTime()));
	return Math.min(level, requestedAmount);
    }

    public Inventory getInvAllocTaskInventory(Task task){
	PrepositionalPhrase pp =task.getPrepositionalPhrase(Constants.Preposition.USINGSUPPLYSOURCE);
	Object io = pp.getIndirectObject();
	if (io instanceof Inventory) {
	    return (Inventory)io; 
	} else {
	    return null;
	}
    }

    public boolean goBelowSafety (Inventory bin) {
	ScheduledContentPG scp = bin.getScheduledContentPG();
	Schedule sched = scp.getSchedule();
	String nsn;
	if (sched == null) {
	    printError("printInventory()  null sched for bin:"+bin);
	    return false;
	}
	Enumeration elements = sched.getAllScheduleElements();
	QuantityScheduleElement qse;

	boolean pos_inventory = true;
	while (elements.hasMoreElements()) {
	    qse = (QuantityScheduleElement)elements.nextElement();
	    // inventory less than safety
	    int day = TimeUtils.getDaysBetween(startTime_, qse.getStartDate().getTime());
	    if (qse.getQuantity() < getReorderLevel(bin, day) ) {
		pos_inventory = false;
		break;
	    }
	}
	return pos_inventory;
    }

    // ********************************************************
    //                                                        *
    // Initialization  Section                                *
    //                                                        *
    // ********************************************************

    //Allocation of refill tasks
    static class RefillAllocPredicate implements UnaryPredicate
    {
	String type_;
	String orgName_;

	public RefillAllocPredicate(String type, String orgName) {
	    type_ = type;
	    orgName_ = orgName;
	}

	public boolean execute(Object o) {
	    if (o instanceof Allocation ) {
		Task task = ((Allocation)o).getTask();
		if (task.getVerb().equals(Constants.Verb.SUPPLY)) {
		    if (TaskUtils.isDirectObjectOfType(task, type_)) {
			// need to check if externally allocated
			if(((Allocation)o).getAsset() instanceof Organization) {
			    if (TaskUtils.getQuantity(task) > 0.0){
				if (TaskUtils.isMyRefillTask(task, orgName_)){
				    return true;
				}
			    }
			}
		    }
		}
	    }
	    return false;
	}
    }

    /** Initialize this instance. */
    protected void initialize() {
	setupSubscriptions();
    }

    /** Initialize my subscriptions. */
    protected void setupSubscriptions() {
	
	refillAllocs_ = subscribe(new RefillAllocPredicate(supplyType_, myOrgName_));
    }



}
