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
package org.cougaar.glm.plugins.inventory;

import java.util.*;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.util.AllocationResultHelper;
import org.cougaar.glm.debug.*;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.asset.Ammunition;
import org.cougaar.glm.ldm.asset.BulkPOL;
import org.cougaar.glm.ldm.asset.ContainPG;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.NewScheduledContentPG;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.glm.ldm.asset.VolumetricInventory;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.plugins.*;
import org.cougaar.planning.plugin.DeletionPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.EmptyEnumeration;
import org.cougaar.util.MoreMath;

public abstract class InventoryManager extends InventoryProcessor {

  protected IncrementalSubscription inventoryAllocSubscription_ = null;
  protected IncrementalSubscription modifiedInventorySubscription_ = null;
  /** Subscription to policies */
  protected IncrementalSubscription inventoryPolicySubscription_;
  private Inventory selectedInventory = null;
  protected boolean forcePrintConcise = false;
  private String concisePrefix = "";

  private static final int DONE = -1; 

  /**
   *   daysOnHand_    keep enough inventory on hand to cover N days of demand
   *   daysForward_   When calculating average daily demand, look N days forward
   *                  from this day.
   *   daysBackward_  When calculating average daily demand, look N days backward
   *                  from this day.
   *   goalLevelMultiplier_     Multiplier for reorder level which yields goal level
   */
  protected int daysOnHand_ = 3;
  protected int daysForward_ = 15;
  protected int daysBackward_ = 15;
  protected double goalLevelMultiplier_ = 2.0;
  private double shortfall; // For debugging only
  private Set changedSet_;

  public final int REFILL_ALTER_TASK = 0;
  public final int REFILL_REPLACE_TASK = 1;
  public final int REFILL_ADD_TASK = 2; // This one doesn't work. Don't use it.
  public final int REFILL_CHANGE_METHOD = REFILL_REPLACE_TASK;
  private NewTask defaultSupplyTask = 
    (NewTask)buildNewTask(null,Constants.Verb.SUPPLY,null);


  static class PolicyPredicate implements UnaryPredicate {
    String type_;
    public PolicyPredicate(String type) {
      type_ = type;
    }
    public boolean execute(Object o) {
      if (o instanceof InventoryPolicy) {
	String type = ((InventoryPolicy)o).getResourceType();
	if (type.equals(type_)) {
	  return true;
	}
      }
      return false;
    }
  }   

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
	  (InventoryPG)((Inventory)o).getInventoryPG();
	return ((invpg != null) && AssetUtils.isSupplyClassOfType(invpg.getResource(), type_));
      }
      return false;
    }
  }



  public InventoryManager(InventoryPlugin plugin, Organization org, String type) {
    super(plugin, org, type);
    inventoryAllocSubscription_ = subscribe(new AllocToInventoryPredicate(supplyType_));
    modifiedInventorySubscription_ = subscribe(new ModifiedInventoryPredicate(supplyType_));
    inventoryPolicySubscription_ = subscribe(new PolicyPredicate(type));

    checkDeletionPolicy();
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
    // RJB keep track of inventories that need to be run, until you actually run them
    changedSet_ = needUpdate(changedSet_);
    Iterator changedInventories = changedSet_.iterator();

    if (!changedSet_.isEmpty()) {
      printDebug(2,"\n\n\nBEGIN CYCLE___________________________________________\n");
	    
      if (inventoryPlugin_.getDetermineRequirementsTask() == null) {
	Enumeration inventories = inventoryPlugin_.getInventoryBins(supplyType_);
	changedSet_ = new HashSet();
	while (inventories.hasMoreElements()) {
	  changedSet_.add(inventories.nextElement());
	}
	System.out.println("#####"+clusterId_+" is running because (inventoryPlugin_.getDetermineRequirementsTask() == null)"); 

	handleRescinds();

	// RJB now we have handled all inventories
	changedSet_=null;

      } else if (inventoryPlugin_.hasSeenAllConsumers()) {
	handleChangedInventories();
	// RJB now we have handled all inventories
	changedSet_=null;
      }
      printDebug(2,"\n\nEND CYCLE___________________________________________\n\n");
    }
  }

  private void handleRescinds() {
    //nominal handling of rescinds
    resetInventories();
    accountForWithdraws();
    addPreviousRefills();
    refreshInventorySchedule();
    
    // MWD hook for any additional handling of rescinds by subclasses
    handleGLSRescind();
  }

  private void handleChangedInventories() {
    resetInventories();
    accountForWithdraws();
    generateHandleDueIns();
    adjustForInadequateInventory();
    updateWithdrawAllocations();
    refreshInventorySchedule();
  }

  public void printConcise(String s) {
    if (isPrintConcise()) GLMDebug.DEBUG(className_, clusterId_, concisePrefix + s, GLMDebug.ERROR_LEVEL);
  }

  public boolean isPrintConcise() {
    return forcePrintConcise || super.isPrintConcise();
  }

  public void forcePrintConcise(boolean f) {
    forcePrintConcise = f;
  }

  public static class IMDeletionPolicy extends DeletionPlugin.DeletionPolicy {
    public String supplyType_;
  }

  private IMDeletionPolicy createDeletionPolicy(long deletionDelay) {
    IMDeletionPolicy policy =
      (IMDeletionPolicy) ldmFactory_.newPolicy(IMDeletionPolicy.class.getName());
    policy.supplyType_ = supplyType_;
    policy.init(supplyType_ + " Due Out Deletion",
		inventoryPlugin_.getDueOutPredicate(supplyType_),
		deletionDelay);
    return policy;
  }

  /**
     This predicate finds our deletion policies. They must be
     instances of IMDeletionPolicy and have a supplyType_ matching
     our supplyType_;
  **/
  private UnaryPredicate deletionPolicyPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof IMDeletionPolicy) {
	  IMDeletionPolicy policy = (IMDeletionPolicy) o;
	  return policy.supplyType_.equals(supplyType_);
	}
	return false;
      }
    };

  /**
     Checks the current deletion policy and insures that it is
     consistent with the current on hand policy. Generally, due out
     tasks must not be deleted until the daysBackward_ days have
     passed. The DeletionPolicy is created or updated to reflect
     this.
  **/
  protected void checkDeletionPolicy() {
    long deletionDelay = daysBackward_ * TimeUtils.MSEC_PER_DAY;
    Collection policies = delegate_.query(deletionPolicyPredicate);
    if (policies.isEmpty()) {
      IMDeletionPolicy policy = createDeletionPolicy(deletionDelay);
      delegate_.publishAdd(policy);
    } else {
      IMDeletionPolicy policy = (IMDeletionPolicy) policies.iterator().next();
      policy.setDeletionDelay(deletionDelay);
      delegate_.publishChange(policy);
    }
  }

  // ********************************************************
  //                                                        *
  // Need Update / Reset Section                            *
  //                                                        *
  // ********************************************************

  protected abstract Set needUpdate(Set inv);

  // Reset Inventories

  private void resetInventories() {
    Inventory inventory;
    Iterator list = changedSet_.iterator();
    printDebug("STEP 1: RESETINVENTORIES(), Today: "+TimeUtils.dateString(startTime_));
    while (list.hasNext()) {
      inventory = (Inventory)list.next();
      InventoryPG invpg = 
	(InventoryPG)inventory.getInventoryPG();
      //              if (selectedInventory == null) {
      //                  if (invpg.getResource().getTypeIdentificationPG().getTypeIdentification().equals("NSN/9150001806383")) {
      //                      selectedInventory = inventory;
      //                  }
      //              }
      invpg.resetInventory(inventory, startTime_);
    }
  }


  // ********************************************************
  //                                                        *
  // Account for withdraws                                  *
  //                                                        *
  // ********************************************************

  private void accountForWithdraws() {
    printDebug("STEP 2: ACCOUNTFORWITHDRAWS()");
    Iterator inventories = changedSet_.iterator();
    while (inventories.hasNext()) {
      Inventory inventory = (Inventory)inventories.next();
      InventoryPG invpg = 
	(InventoryPG)inventory.getInventoryPG();
      invpg.withdrawFromInventory(inventory, clusterId_);
      invpg.determineInventoryLevels();
      computeThresholdSchedule(inventory);
      //  	    invpg.printInventoryLevels(inventory, clusterId_);
    }
  }

  private void computeThresholdSchedule(Inventory inventory) {
    InventoryPG invpg = 
      (InventoryPG)inventory.getInventoryPG();
    invpg.computeThresholdSchedule(daysOnHand_,
				   daysForward_,
				   daysBackward_,
				   getMinReorderLevel(inventory),
				   getMaxReorderLevel(inventory),
				   goalLevelMultiplier_);
  }

  private Vector generateInactiveProjections(Inventory inventory, int switchoverDay) {
    printDebug("STEP 2:  GenerateInactiveProjections() for "+AssetUtils.getAssetIdentifier(inventory));
    Vector projections = new Vector();
    InventoryPG invpg = inventory.getInventoryPG();
    int today = invpg.getFirstPlanningDay();
    int periodBegin = today;
    Scalar previous = invpg.getProjected(periodBegin);
    /* Loop from tomorrow to the switchover day. The extra step at
     * the end insures that the final segment is processed. */
    for (int day = today + 1; day <= switchoverDay; day++) {
      Scalar current = (day < switchoverDay) ? invpg.getProjected(day) : null;

      if (previous == null) {
        System.out.println("#####" + clusterId_ + 
                           " current projection is null on day " + day +
                           " for " + inventory.getItemIdentificationPG().getItemIdentification());
        break;
      } 

      if (!previous.equals(current)) {
	double value = convertScalarToDouble(previous);
	if (!Double.isNaN(value) && value > 0.0) {
	  long start = invpg.getStartOfDay(periodBegin);
	  int nDays = day - periodBegin;
	  long end = start + nDays * TimeUtils.MSEC_PER_DAY;
	  Rate dailyRate = createDailyRate(previous);
	  Task t = newProjectSupplyTask(inventory, start, end, dailyRate);
	  projections.add(t);
	}

        previous = current;
	periodBegin = day;
      }
    }
    return projections;
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

  protected double ignoreSmallIncrement(double increment, double x) {
    double t = x + increment;
    double diff = (x - increment) / t;
    if (diff < 0.0001 && diff > -0.0001) return x;
    return x + increment;
  }

  private Rate createIncrementedDailyRate(Measure qty, double increment) {
    Rate rate = null;
    if (qty instanceof Volume) {
      double d = ignoreSmallIncrement(increment, ((Volume) qty).getGallons());
      if (d <= 0.0) return null;
      rate = FlowRate.newGallonsPerDay(d);
    } else if (qty instanceof Count) {
      double d = ignoreSmallIncrement(increment, ((Count) qty).getEaches());
      if (d <= 0.0) return null;
      rate = CountRate.newEachesPerDay(d);
    } else if (qty instanceof Mass) {
      double d = ignoreSmallIncrement(increment, ((Mass) qty).getShortTons());
      if (d <= 0.0) return null;
      rate = MassTransferRate.newShortTonsPerDay(d);
    }
    return rate;
  }

  private Task newProjectSupplyTask(Inventory inventory, long start, long end, Rate rate) {
    Task parentTask = inventoryPlugin_.findOrMakeMILTask(inventory);
    // Create start and end time preferences (strictly at)
    ScoringFunction score;
    Vector prefs = new Vector();
    score = ScoringFunction.createStrictlyAtValue(new TimeAspectValue(AspectType.START_TIME, start));
    prefs.addElement(ldmFactory_.newPreference(AspectType.START_TIME, score));
    score = ScoringFunction.createStrictlyAtValue(new TimeAspectValue(AspectType.END_TIME, end));
    prefs.addElement(ldmFactory_.newPreference(AspectType.END_TIME, score));
    Vector prep_phrases = new Vector();
    prep_phrases.add(newPrepositionalPhrase(Constants.Preposition.FOR, myOrgName_));
    prep_phrases.add(newPrepositionalPhrase(Constants.Preposition.OFTYPE, supplyType_));
    Asset resource = inventory.getInventoryPG().getResource();
    TypeIdentificationPG tip = ((Asset)resource).getTypeIdentificationPG();
    MaintainedItem itemID = MaintainedItem.findOrMakeMaintainedItem("Inventory", tip.getTypeIdentification(), 
								    null, tip.getNomenclature());
    prep_phrases.add(newPrepositionalPhrase(Constants.Preposition.MAINTAINING, itemID));
    prep_phrases.add(newPrepositionalPhrase(Constants.Preposition.REFILL));

    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
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

  private Inventory getInventoryForTask (Task task) {
    return inventoryPlugin_.findOrMakeInventory(supplyType_,(Asset)task.getDirectObject());
  }

  // ********************************************************
  //                                                        *
  // Generate/Handle Due Ins Section                        *
  //                                                        *
  // ********************************************************

  private void generateHandleDueIns() {
    printDebug("Step 3: generateHandleDueIns()");
    addPreviousRefills();
  }

  private void addPreviousRefills() {

    printDebug("      : addPreviousRefills()");
    int total=0;
    Iterator inventories = changedSet_.iterator();
    Inventory inv;
    InventoryPG invpg;
    while (inventories.hasNext()) {
      inv = (Inventory)inventories.next();
      invpg = (InventoryPG)inv.getInventoryPG();
      // maintainInventory Task is the parent of all the refills for this inventory
      Task maintainInventory = inventoryPlugin_.findOrMakeMILTask(inv);
      // should never be null but may want to add a check AHF
      total += invpg.addPreviousRefillsToInventory(maintainInventory);
      invpg.determineInventoryLevels();
      //  	    invpg.printInventoryLevels(inv, clusterId_);
    }
    printDebug(2,"end addDueIns(), number of refillTasks is "+total);
  }

  public long defaultRefillEndTime(long time, Inventory inv) {
    if (time == startTime_) {
      time = TimeUtils.addNDays(startTime_, 1);
    }
    return time;
  }

  private Task createRefillTask(Inventory inv, double refill_qty, long time) {
    Asset item = getInventoryAsset(inv);
    // create request task
    Vector prefs = new Vector();
    Preference p_start, p_end,p_qty;

    long end_time = defaultRefillEndTime(time,inv);
    //  	long start_time = defaultRefillStartTime(time,inv);

    //  	p_start = createDateAfterPreference(AspectType.START_TIME, start_time);
    p_end = createDateBeforePreference(AspectType.END_TIME,end_time);

    p_qty = createRefillQuantityPreference(refill_qty);
    //  	prefs.addElement(p_start);
    prefs.addElement(p_end);
    prefs.addElement(p_qty);

    Vector pp_vector = new Vector();
    pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.FOR, myOrgName_));
    pp_vector.add(newPrepositionalPhrase(Constants.Preposition.OFTYPE, supplyType_));

    Object io;
    Enumeration geolocs = AssetUtils.getGeolocLocationAtTime(myOrganization_, end_time);
    if (geolocs.hasMoreElements()) {
      io = (GeolocLocation)geolocs.nextElement();
    } else {
      io = thisGeoloc_;
    }
    pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.TO, io));
    Asset resource = inv.getInventoryPG().getResource();
    TypeIdentificationPG tip = ((Asset)resource).getTypeIdentificationPG();
    MaintainedItem itemID = MaintainedItem.findOrMakeMaintainedItem("Inventory", tip.getTypeIdentification(), 
								    null, tip.getNomenclature());
    pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.MAINTAINING, itemID));
    pp_vector.addElement(newPrepositionalPhrase(Constants.Preposition.REFILL));

    NewTask task =  (NewTask)buildTask(null, Constants.Verb.SUPPLY, 
				       item, pp_vector, prefs.elements());
    return task;

  }

  protected Preference createRefillQuantityPreference(double refill_qty) {
    AspectValue lowAV = new AspectValue(AspectType.QUANTITY, 0.01);
    AspectValue bestAV = new AspectValue(AspectType.QUANTITY, refill_qty);
    AspectValue highAV = new AspectValue(AspectType.QUANTITY, refill_qty+1.0);
    ScoringFunction qtySF = ScoringFunction.createVScoringFunction(lowAV, bestAV, highAV);
    return  ldmFactory_.newPreference(AspectType.QUANTITY, qtySF);
    //  	return createQuantityPreference(AspectType.QUANTITY, refill_qty);
  }

  // Refill Inventories

  /**
   * Generate a refill order to replenish the item in storage,
   * assuming we already have determined we have at least an
   * Economic Reorder Quantity. Allocate task to provider Org.
   * @return true if an order was placed or changed
   **/
  private boolean orderRefill(Inventory inventory, int day) {
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

  private boolean orderNewRefill(Inventory inventory,
				   int day,
				   InventoryPG invpg,
				   double refill_qty)
  {
    long time = invpg.convertDayToTime(day);
    Task task = createRefillTask(inventory, refill_qty,  time);
    // FIX ME - sets to today??? check dates.
    // 	task.setCommitmentDate(date);
    //      		printDebug(1,"orderRefill task:"+TaskUtils.taskDesc(task));
    Task parentTask = inventoryPlugin_.findOrMakeMILTask(inventory);
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
  private boolean orderRefillWithPrevious(Inventory inventory,
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
      if (!failed)  {
        refill_qty += prev_qty;
      }
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


  private void adjustForInadequateInventory() {
    printLog("adjustForInadequateInventory()");
    // For each inventory
    Iterator inventories = changedSet_.iterator();
    while (inventories.hasNext()) {
      Inventory inventory = (Inventory) inventories.next();
      //              forcePrintConcise(inventory == selectedInventory);
      concisePrefix = inventory.getInventoryPG().getResource().getTypeIdentificationPG().getTypeIdentification() + " ";
      adjustForInadequateInventory(inventory);
      concisePrefix = "";
      //              forcePrintConcise(false);
    }
  }

  private void adjustForInadequateInventory(Inventory inventory) {
    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
    int firstDay = invpg.getFirstPlanningDay();
    int days = invpg.getPlanningDays();
    int switchoverDay = days;
    {
      Task testTask = buildNewTask(null, Constants.Verb.PROJECTSUPPLY, null);
      int imputedDay0 = invpg.getImputedDay(0);
      for (switchoverDay = 0; switchoverDay < days; switchoverDay++) {
	double weight =
	  invpg.getProjectionWeight()
	  .getProjectionWeight(testTask, imputedDay0 + switchoverDay);
	if (weight > 0.0) break; // found switchoverDay
      }
    }
    Vector projections = generateInactiveProjections(inventory, switchoverDay);
    for (int day = firstDay; day < switchoverDay; day++) {
      if (checkFailedRefill(inventory, invpg, day)) { // Check and update any failed refills
	invpg.determineInventoryLevels();
      }
    }
    int refillDay = refillNeeded(inventory, firstDay);
    int periodBegin = switchoverDay;
    Rate currentRate = null;
    double pendingDelta = 0.0;
    for (int day = firstDay; day <= days; day++) {
      Scalar projected = invpg.getProjected(day);
      double projectedDemand = convertScalarToDouble(projected);
      if (day < switchoverDay) {
	if (day == refillDay) {
	  // Try to do a refill
	  if (orderRefill(inventory, day)) {
	    invpg.determineInventoryLevels();
	    // If the refill succeeded, then we are above
	    // the reorder level so we advance to the next
	    // day and loop.
	    refillDay = refillNeeded(inventory, day + 1);
	  } else {
	    while (day == refillDay) {
	      if (failDueOut(inventory, invpg, day)) {
		invpg.determineInventoryLevels();
		refillDay = refillNeeded(inventory, day);
	      } else {
                                // Can this happen? If it does, just go to the next day
		refillDay = refillNeeded(inventory, day + 1);
	      }
	    }
	  }
	}
      } else {
	Scalar nextProjection = invpg.getProjectedRefill(day);
	Rate newRate = null;
	double delta = 0.0;
	if (day < days) {
	  double target = 0.5 * (invpg.getReorderLevel(day) + invpg.getGoalLevel(day));
	  if (false && isPrintConcise())
	    printConcise("target(" + TimeUtils.dateString(invpg.convertDayToTime(day))
			 + ")=" + target);
	  double qty = convertScalarToDouble(invpg.getLevel(day)) + pendingDelta;
	  double nextRefill = convertScalarToDouble(nextProjection);
	  if (!MoreMath.nearlyEquals(qty, target, 0.0001)) {
	    delta = Math.max(target - qty, -nextRefill); // Can only reduce by projection amount
	  }
	  newRate = createIncrementedDailyRate(nextProjection, delta);
	  if (newRate == null) delta = 0.0; // Can't do negative, cancel the delta (I think this does nothing)
	}
	if (isSameRate(currentRate, newRate)) {
	  /* If we use the currentRate instead of newRate,
	     then the delta we achieve is not the delta we
	     computed above. Compute a new delta that
	     reflects what the currentRate achieves. */
	  //                      delta += (TaskUtils.getDailyQuantity(currentRate)
	  //                                - TaskUtils.getDailyQuantity(newRate));
	  pendingDelta += delta;
	  continue;
	}
	for (int i = periodBegin; i < day; i++) {
	  invpg.removeRefillProjection(i);
	}
	if (currentRate != null) { // Terminate the current rate
	  long start = invpg.getStartOfDay(periodBegin);
	  long end = invpg.getStartOfDay(day);
	  Task t = newProjectSupplyTask(inventory, start, end, currentRate);
	  projections.add(t);
	  invpg.addDueIn(t);
	}
	currentRate = newRate;
	periodBegin = day;
	pendingDelta = delta;
      }
    }
    publishChangeProjection(inventory, projections.elements());
  }

  private boolean isSameRate(Rate rate1, Rate rate2) {
    if (rate1 == rate2) return true;
    if (rate1 == null || rate2 == null) return false;
    double val1 = rate1.getValue(rate1.getCommonUnit());
    double val2 = rate2.getValue(rate2.getCommonUnit());
    return MoreMath.nearlyEquals(val1, val2, 0.0001);
  }

  private boolean checkFailedRefill(Inventory inventory, InventoryPG invpg, int day) {
    Task prev_refill = invpg.refillAlreadyFailedOnDay(day);
    if (prev_refill != null) {
      return orderRefill(inventory, day);
    }
    return false;
  }

//    protected abstract boolean orderRefill(Inventory inventory, int day);

  private boolean failDueOut(Inventory inventory, InventoryPG invpg, int day) {
    DueOut lowestPriorityDueOut = invpg.getLowestPriorityDueOutBeforeDay(day);
    if (lowestPriorityDueOut == null) return false;
    //      printDebug("Inventory level before failing allocation is "+
    //                 TimeUtils.dateString(invpg.convertDayToTime(day)) + " : "+invpg.getLevel(day)+
    //                 ", Reorder level :"+invpg.getReorderLevel(day));
    invpg.setDueOutFilled(lowestPriorityDueOut, false);
    lowestPriorityDueOut.setFilled(false);
    if (isPrintConcise()) {
      if (lowestPriorityDueOut.getPreviouslyFilled() != false) {
	Task task = lowestPriorityDueOut.getTask();
	printConcise("adjustForInadequateInventory() shortfall="
		     + shortfall
		     + " on "
		     + day
		     + "="
		     + TimeUtils.dateString(invpg.getStartOfDay(day))
		     + " failed "
		     + task.getUID()
		     + " having "
		     + TaskUtils.getDailyQuantity(task)
		     + " on "
		     + TimeUtils.dateString(invpg.convertDayToTime(lowestPriorityDueOut.getDay()))
		     );
      }
    }
    //      printDebug("Inventory level after failing allocation is "
    //                 + TimeUtils.dateString(invpg.convertDayToTime(day))
    //                 + " : "+invpg.getLevel(day));
    return true;
  }

  private void updateWithdrawAllocations() {
    printLog("updateWithdrawAllocations()");
    Iterator inventories = changedSet_.iterator();
    while (inventories.hasNext()) {
      Inventory inventory = (Inventory)inventories.next();
      InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
      List changes = invpg.updateDueOutAllocations();
      for (int i = 0, n = changes.size(); i < n; i++) {
	PlanElement pe = (PlanElement) changes.get(i);
	delegate_.publishChange(pe);
      }
    }
  }
    
  /**
   * Computes the next day on which a refill is needed. This simply
   * looks for the next day that the inventory drops below the
   * reorder level. It is likely that this is overridden in a
   * subclass. Indeed, GeneralInventoryManager _does_ override.
   **/
  private int refillNeeded(Inventory inventory, int startDay) {
    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
    int days = invpg.getPlanningDays();
    for (int day = startDay; day < days; day++) {
      if (needRefill(inventory, day)) return day;
    }
    return DONE;
  }

  private boolean needRefill(Inventory inventory, int day) {
    InventoryPG invpg = inventory.getInventoryPG();
    double qty = convertScalarToDouble(invpg.getLevel(day));
    double level = invpg.getReorderLevel(day);
    shortfall = level - qty;
    return (shortfall > 0.0);
  }

  // ********************************************************
  //                                                        *
  // CheckForOverflow Section - only relevent for limited   *
  //                            capacity inventories        *
  //                                                        *
  // ********************************************************

  // ********************************************************
  //                                                        *
  // Refresh ScheduledContentPG on Inventory  Section       *
  //                                                        *
  // ********************************************************

  private void refreshInventorySchedule() {
    Inventory inventory;
    InventoryPG invpg;
    Iterator inventories = changedSet_.iterator();

    printDebug("LAST STEP: REFRESHINVENTORYSCHEDULE()");
    while (inventories.hasNext()) {
      inventory = (Inventory)inventories.next();
      invpg = (InventoryPG)inventory.getInventoryPG();
      // invpg.printInventoryLevels(inventory, clusterId_);
      invpg.updateContentSchedule(inventory);

      // detailed Inventory Schedule for demo purposes only
      Boolean detailed = (Boolean)inventoryPlugin_.getParam("Detailed");
      if ((detailed != null) && detailed.booleanValue()) {
	invpg.updateDetailedContentSchedule(inventory);
      }
      invpg.updateInventoryLevelsSchedule(inventory);
    }
  }

  /**
     method called from update when a GLS Rescind is detected. This
     is an entry point for any additional handling by subclasses.
  **/
  protected void handleGLSRescind() {}

  // ********************************************************
  //                                                        *
  // Utilities Section                                      *
  //                                                        *
  // ********************************************************


  /** @return a double indicating the amount requests by the task (in terms of the standard unit of measure for the item) */
  protected double getAmountRequested(Task task) {
    return TaskUtils.getPreference(task, AspectType.QUANTITY);
  }

  /** 
      Given a Scalar, return a double value representing
      Gallons for Volume,
      Eaches for Count and
      Short Tons for Mass.
  **/
  static protected double convertScalarToDouble(Scalar measure) {
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

  static protected Scalar newScalarFromOldToDouble(Scalar old, double newVal){
    if (old instanceof Volume) {
      return Volume.newGallons(newVal);
    } else if (old instanceof Count) {
      return Count.newEaches(newVal);
    } else if (old instanceof Mass) {
      return Mass.newShortTons(newVal);
    }

    String oldUnitName = old.getUnitName(old.getCommonUnit());
    return (Scalar) AbstractMeasure.newMeasure(oldUnitName,(int) newVal);
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

    Iterator bins = changedSet_.iterator();
    if(bins.hasNext()){
      printDebug(priority,"\n\n***Status of inventory  after accounting for due-ins and due-outs: "+TimeUtils.dateString(getAlpTime()));
    } else {
      printDebug(priority,"\n\n***FUNNY -- no inventory assets for "+myOrgName_);
    }
    while (bins.hasNext()){
      Inventory bin = (Inventory)bins.next();
      InventoryPG invpg = (InventoryPG)bin.getInventoryPG();
      Enumeration dueIns = invpg.getAllDueIns();
      printInventory(bin,dueIns,priority);
      printDebug(priority,"\n_____________________________________________________________________________\n");
    }
  }

  protected int printInventoryStatus() {

    Iterator inventory = changedSet_.iterator();
    if(inventory.hasNext()){
      printDebug("\n\n\n**********Inventory status at the end of sourcing********");
    }
    int nItems = 0;
    while (inventory.hasNext()) {
      nItems = nItems+1;
      Inventory bin = (Inventory)inventory.next();
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

  //Max at the capacity
  protected double getMaxReorderLevel(Inventory inventory) { 
    InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
    return convertScalarToDouble(invpg.getCapacity());
  }

  //Min at zero unless maintainAtCapacity inventory, then min is capacity.
  protected double getMinReorderLevel(Inventory inventory) { 
    double mrl = 0.0;
    InventoryPG invpg = (InventoryPG)inventory.searchForPropertyGroup(InventoryPG.class);
    if(invpg.getMaintainAtCapacity()) {
      mrl = convertScalarToDouble(invpg.getCapacity());
    }
    return mrl;
  }

  /** Update the current InventoryPolicy */
  protected boolean updateInventoryPolicy(Enumeration policies) {
    InventoryPolicy pol;
    boolean changed = false;
    //  	printDebug("updateInventoryPolicy(), Days On Hand Policy for "+supplyType_+". DaysOnHand: "+daysOnHand_+
    //  		   ", Days Forward: "+daysForward_+", Days Backward: "+daysBackward_+", Window size: "+
    //  		   (daysForward_+daysBackward_));
    while (policies.hasMoreElements()) {
      pol = (InventoryPolicy)policies.nextElement();
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
	checkDeletionPolicy(); // Changed daysBackward, need to change deletion policy
	changed = true;
      }
      double multiplier = pol.getGoalLevelMultiplier();
      if ((multiplier > 1.0) && (multiplier != goalLevelMultiplier_)) {
	goalLevelMultiplier_ = multiplier;
	changed = true;
      }
      if (pol.hasFillToCapacityRule()) {
	inventoryPlugin_.setFillToCapacity(supplyType_, pol.getFillToCapacity());
	changed = true;
      }
      if (pol.hasMaintainAtCapacityRule()) {
	inventoryPlugin_.setMaintainAtCapacity(supplyType_, pol.getMaintainAtCapacity());
	changed = true;
      }
      if (pol.hasSwitchoverRule()) {
	ProjectionWeight newWeight =
	  new ProjectionWeightImpl(pol.getWithdrawSwitchoverDay(),
				   pol.getRefillSwitchoverDay(),
				   pol.getTurnOffProjections());
	inventoryPlugin_.setProjectionWeight(supplyType_, newWeight);
	changed = true;
      }
    }
    if (changed) {
      printDebug("updateInventoryPolicy(), Days On Hand Policy CHANGED for "+supplyType_+". DaysOnHand: "+daysOnHand_+
		 ", Days Forward: "+daysForward_+", Days Backward: "+daysBackward_+", Window size: "+
		 (daysForward_+daysBackward_)+", goal level multiplier: "+goalLevelMultiplier_);
    }		
    return changed;
  }

}



