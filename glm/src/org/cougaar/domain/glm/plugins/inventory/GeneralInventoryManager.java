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
import org.cougaar.core.plugin.util.AllocationResultHelper;
import org.cougaar.domain.planning.ldm.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.util.UnaryPredicate;
import java.io.*;
import java.lang.*;
import java.util.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.GLMFactory;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.asset.ProjectionWeight;

/** Allocate SUPPLY tasks to local inventory (if there is any) or to 
 *  the closest supplier.
 */
public abstract class GeneralInventoryManager extends InventoryManager {
  public final int REFILL_ALTER_TASK = 0;
  public final int REFILL_REPLACE_TASK = 1;
  public final int REFILL_ADD_TASK = 2; // This one doesn't work. Don't use it.
  public final int REFILL_CHANGE_METHOD = REFILL_REPLACE_TASK;
    
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

  // RJB changed
  protected Set needUpdate(Set invSet) {
    if (invSet == null) invSet = new HashSet();
    // Determine if this inventory processor needs to run
    // return a set of changed inventories to re-process
    boolean refill_changed = checkRefills(refillAllocs_, invSet);
    boolean inventory_changed = checkInventories(modifiedInventorySubscription_.getChangedList(), invSet);
    boolean inventory_policy_changed = checkInventoryPolicies(inventoryPolicySubscription_, 
							      modifiedInventorySubscription_.elements(), invSet);
            
    // Allocations of tasks with quantity > 0 to Inventory objects
    // inventoryAllocSubscription_ only used to determine when to run processor.
    // Inventory objects held in the plugin.
    boolean allocatedInventories = checkInventoryAllocations(inventoryAllocSubscription_, invSet);

    if (refill_changed  || allocatedInventories || inventory_changed || inventory_policy_changed) {
      String prefix = "<" + supplyType_ + "> UPDATING INVENTORIES: ";
      if (refill_changed          ) printLog(prefix + "refill changed.");
      if (allocatedInventories    ) printLog(prefix + "allocations added/removed.");
      if (inventory_changed       ) printLog(prefix + "inventory changed.");
      if (inventory_policy_changed) printLog(prefix + "inventory policy changed.");
    }
    return invSet;
  }

  private boolean checkRefills(IncrementalSubscription refillAllocs, Set invSet) {
    Enumeration refills = refillAllocs.getChangedList();
    boolean changed = false;
    while (refills.hasMoreElements()) {
      Allocation alloc = (Allocation) refills.nextElement();
      Set changes = refillAllocs.getChangeReports(alloc);
      if (TaskUtils.checkChangeReports(changes, PlanElement.EstimatedResultChangeReport.class)) {
	Task refill = alloc.getTask();
	MaintainedItem inventoryID = 
	  (MaintainedItem)refill.getPrepositionalPhrase(Constants.Preposition.MAINTAINING).getIndirectObject();
	Inventory inv = inventoryPlugIn_.findOrMakeInventory(supplyType_, inventoryID.getTypeIdentification());
	if (inv != null) {
	  invSet.add(inv);
	  changed = true;
	}
      }
    }
    return changed;
  }

  private boolean checkInventories(Enumeration changedInventories, Set invSet) {
    boolean changed = changedInventories.hasMoreElements();
    while (changedInventories.hasMoreElements()) {
      invSet.add((Inventory) changedInventories.nextElement());
    }
    return changed;
  }

  private boolean checkInventoryPolicies(IncrementalSubscription policySubscription, Enumeration inventories, Set invSet) {
    boolean changed = updateInventoryPolicy(policySubscription.getAddedList()) ||
      updateInventoryPolicy(policySubscription.getChangedList());
    if (changed) {
      while (inventories.hasMoreElements()) {
	invSet.add((Inventory) inventories.nextElement());
      }
    }
    return changed;
  }

  private boolean checkInventoryAllocations(IncrementalSubscription invAllocSubscription, Set invSet) {
    boolean changed = false;
    if (invAllocSubscription.hasChanged()) {
      Enumeration allocs = invAllocSubscription.getAddedList();
      while (allocs.hasMoreElements()) {
	Allocation alloc = (Allocation) allocs.nextElement();
	if (!inventoryPlugIn_.hasSeenAllConsumers()) {
	  inventoryPlugIn_.recordCustomerForTask(alloc.getTask());
	}
	invSet.add(alloc.getAsset());
	changed = true;
      }
      allocs = invAllocSubscription.getRemovedList();
      while (allocs.hasMoreElements()) {
	invSet.add(((Allocation) allocs.nextElement()).getAsset());
	changed = true;
      }
    }
    return changed;
  }

  // ********************************************************
  //                                                        *
  // Generate/Handle Due Ins Section                        *
  //                                                        *
  // ********************************************************

  // Refill Inventories

  protected void refillInventories() {
    // Done as part of adjustForInadequateInventory
  }


  /**
   * Generate a refill order to replenish the item in storage,
   * assuming we already have determined we have at least an
   * Economic Reorder Quantity. Allocate task to provider Org.
   * @return true if an order was placed or changed
   **/
  protected boolean orderRefill(Inventory inventory, int day) {
    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
    double currentInventory = convertScalarToDouble(invpg.getLevel(day));
    double goal_level = invpg.getGoalLevel(day);
    double refill_qty = goal_level - currentInventory;
    double reorder_level = invpg.getReorderLevel(day);

    defaultSupplyTask.setDirectObject(invpg.getResource());
    if (invpg.getProjectionWeight().getProjectionWeight(defaultSupplyTask, invpg.getImputedDay(day)) <= 0.0) {
      // This test is to avoid generating refill SUPPLY tasks for times when the system
      //  will ignore their effect on inventory
      return false;       // Can't do a refill
    }

    if (!invpg.getFillToCapacity()) {
      // ** THIS IS A KLUDGE FOR SUBSISTENCE -- WITHOUT IT WE FAIL THE FIRST ORDER.
      //    SHOULDN'T HAPPEN LIKE THAT -- FIX ASAP -- RUSTY AND AMY!!
      //    	    refill_qty = refill_qty*1.3;
      // *** KLUDGE ********
    }

    boolean isCount = invpg.getCapacity() instanceof Count;
    if (refill_qty > 0.0) {
      if (isPrintConcise()) {
	printConcise("orderRefill goal=" + goal_level
		     + " reorder level=" + reorder_level
		     + " current level=" + currentInventory);
      }
      Task task = null;
      Task prev_refill = invpg.refillAlreadyFailedOnDay(day);
      if (prev_refill != null) {
	double min_qty = reorder_level - currentInventory;
	if (min_qty <= 0.0) { // Refill unneeded
	  min_qty = 1e-10;
	  //                      invpg.removeDueIn(prev_refill);
	  //                      plugin_.publishRemoveFromExpansion(prev_refill);
	  //                      return true;
	}
	double prev_qty = TaskUtils.getQuantity(prev_refill);
	if (isCount) min_qty = Math.ceil(min_qty);
	if (min_qty < prev_qty) {
	  refill_qty = min_qty; // Retry the refill with the min needed
	} else {
	  return false; // Can't refill on this day.
	}
      } else {
	prev_refill = invpg.getRefillOnDay(day);
      }
      if (isCount) {
	refill_qty=java.lang.Math.ceil(refill_qty);
      }
      if(prev_refill != null) {
	return orderRefillWithPrevious(inventory, day, invpg, refill_qty);
      } else {
	return orderNewRefill(inventory, day, invpg, refill_qty);
      }
    } else {
      printLog("OrderRefill qty < 0: "+refill_qty+" = "+goal_level+" - "+currentInventory);
    }
    return true;
  }

  protected boolean orderNewRefill(Inventory inventory,
				   int day,
				   InventoryPG invpg,
				   double refill_qty)
  {
    long time = invpg.convertDayToTime(day);
    Task task = createRefillTask(inventory, refill_qty,  time);
    // FIX ME - sets to today??? check dates.
    // 	task.setCommitmentDate(date);
    //      		printDebug(1,"orderRefill task:"+TaskUtils.taskDesc(task));
    Task parentTask = inventoryPlugIn_.findOrMakeMILTask(inventory);
    plugin_.publishAddToExpansion(parentTask, task);
    if (isPrintConcise()) {
      printConcise("orderNewRefill() "
		   + task.getUID()
		   + " "
		   + refill_qty
		   + " on "
		   + TimeUtils.dateString(invpg.convertDayToTime(day))
		   );
    }
    invpg.addDueIn(task);
    return true;
  }

  /**
   * Modify an existing refill task if possible to increase the
   * refill as indicated. If the previous refill has failed we don't
   * expect an additional refill to succeed, but if the new amount
   * is smaller than the previous one, then it might succeed so we go
   * ahead with the change. Otherwise, we increase the amount of the
   * existing refill (or replace with a larger refill, or add an
   * additional refill depending on REFILL_CHANGE_METHOD).
   * Note -- This routine is actually never called with a failed
   * refill on day (See refillInventory).
   * @param inventory the Inventory -- not used
   * @param day the day of the refill
   * @param invpg the InventoryPG of the inventory.
   * @param date -- not used
   * @param currentInventory -- not used
   * @param goal_level -- not used
   * @param refill_qty the amount needed to be added to the inventory
   **/
  protected boolean orderRefillWithPrevious(Inventory inventory,
					    int day,
					    InventoryPG invpg,
					    double refill_qty)
  {
    Task refill_task = invpg.getRefillOnDay(day);
    double prev_qty = TaskUtils.getQuantity(refill_task);
    boolean failed = false;
    Allocation alloc = (Allocation) refill_task.getPlanElement();
    if (alloc != null) {
      AllocationResult report = alloc.getReportedResult();
      if (report != null) {
	failed = !report.isSuccess();
      }
    }
    if (failed) {
      if (prev_qty <= refill_qty) {
	System.out.println("Known failed refill: "+TaskUtils.taskDesc(refill_task));
	//  		printLog("Known failed refill: "+TaskUtils.taskDesc(refill_task));
	return false;
      } // If quantity reduced make the change
    }
    // Send orders for whole items, i.e. do not order 0.5 O-rings
    if (invpg.getCapacity() instanceof Count) {
      refill_qty = java.lang.Math.ceil(refill_qty);
    }

    if (isPrintConcise()) {
      printConcise("orderRefillWithPrevious() "
		   + refill_task.getUID()
		   + " "
		   + prev_qty
		   + "-->"
		   + refill_qty
		   + " on "
		   + TimeUtils.dateString(invpg.convertDayToTime(day))
		   );
    }
    invpg.removeDueIn(refill_task);
    switch (REFILL_CHANGE_METHOD) {
    case REFILL_ALTER_TASK:
      /* Change the quantity preference on the existing task.
	 refill_qty is the additional amount needed, prev_qty is
	 the effective quantity of the current task. */
      if (!failed) refill_qty += prev_qty;
      Preference qpref = createRefillQuantityPreference(refill_qty);
      ((NewTask) refill_task).setPreference(qpref);
      PlanElement pe = refill_task.getPlanElement();
      if (pe != null) {
	pe.setEstimatedResult(createEstimatedAllocationResult(refill_task));
      }
      delegate_.publishChange(refill_task);
      invpg.addDueIn(refill_task);
      return true;
    case REFILL_REPLACE_TASK:
      if (!failed) refill_qty += prev_qty;
      plugin_.publishRemoveFromExpansion(refill_task);
      return orderNewRefill(inventory, day, invpg, refill_qty);
    case REFILL_ADD_TASK:
      invpg.addDueIn(refill_task);
      return orderNewRefill(inventory, day, invpg, refill_qty);
    }
    return false;
  }
   
  // ********************************************************
  //                                                        *
  // Adjust Withdraws Section                               *
  //                                                        *
  // ********************************************************

  protected void adjustWithdraws() {
    adjustForInadequateInventory();
    passPreviouslyFailedDueOuts();
  }

  // Pass Previously Failed Dueouts

  protected void passPreviouslyFailedDueOuts() { 
    // The work formerly done by this method is now subsumed by
    // updateDueOutAllocations()
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

  /** Figure out how much of an item we can and should draw from local storage. */
  protected double calcAmountStockToDraw(Inventory inventory, double requestedAmount, Date date)
  {
    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
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

  public boolean goBelowSafety(Inventory bin) {
    ScheduledContentPG scp = bin.getScheduledContentPG();
    InventoryPG invpg = (InventoryPG)bin.getInventoryPG();

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
      // inventory less than reorder
      int day = TimeUtils.getDaysBetween(startTime_, qse.getStartDate().getTime());
      if (qse.getQuantity() < invpg.getReorderLevel(day) ) {
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
	Verb verb = task.getVerb();
	if (verb.equals(Constants.Verb.SUPPLY)
	    || verb.equals(Constants.Verb.PROJECTSUPPLY)) {
	  if (TaskUtils.isDirectObjectOfType(task, type_)) {
	    // need to check if externally allocated
	    if(((Allocation)o).getAsset() instanceof Organization) {
	      if (TaskUtils.isMyRefillTask(task, orgName_)){
		return true;
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
