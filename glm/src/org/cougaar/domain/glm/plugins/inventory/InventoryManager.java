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

import java.util.*;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.util.AllocationResultHelper;
import org.cougaar.domain.glm.debug.*;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.asset.Ammunition;
import org.cougaar.domain.glm.ldm.asset.BulkPOL;
import org.cougaar.domain.glm.ldm.asset.ContainPG;
import org.cougaar.domain.glm.ldm.asset.Inventory;
import org.cougaar.domain.glm.ldm.asset.NewScheduledContentPG;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.domain.glm.ldm.asset.VolumetricInventory;
import org.cougaar.domain.glm.ldm.plan.AlpineAspectType;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.plugins.*;
import org.cougaar.domain.planning.ldm.DeletionPlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
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
  private boolean forcePrintConcise = false;
  private String concisePrefix = "";

  public static final int DONE = -1; 
  public static final double GOAL_LEVEL_BOOST_CAPACITY_FACTOR= 1.1;



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
  protected double shortfall; // For debugging only
  protected Set changedSet_;

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
	  (InventoryPG)((Inventory)o).getInventoryPG();
	return ((invpg != null) && AssetUtils.isSupplyClassOfType(invpg.getResource(), type_));
      }
      return false;
    }
  }



  public InventoryManager(InventoryPlugIn plugin, Organization org, String type) {
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
	    
      if (inventoryPlugIn_.getDetermineRequirementsTask() == null) {
	// MWD - added a method to handle GLS Rescind cases,
	//       override in DLAInventoryManager that reinitializes
	//       input files as well as clearing inventory schedules
	Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
	changedSet_ = new HashSet();
	while (inventories.hasMoreElements()) {
	  changedSet_.add(inventories.nextElement());
	}
	System.out.println("#####"+clusterId_+" is running because (inventoryPlugIn_.getDetermineRequirementsTask() == null)"); 
	resetInventories();
	handleGLSRescind();
	// RJB now we have handled all inventories
	changedSet_=null;
      } else if (inventoryPlugIn_.hasSeenAllConsumers()) {
	resetInventories();
	accountForWithdraws();
	generateHandleDueIns();
	adjustForInadequateInventory();
	updateWithdrawAllocations();
	refreshInventorySchedule();
	// RJB now we have handled all inventories
	changedSet_=null;
      }
      printDebug(2,"\n\nEND CYCLE___________________________________________\n\n");
    }
  }

  public void printConcise(String s) {
    if (isPrintConcise()) GLMDebug.DEBUG(className_, clusterId_, concisePrefix + s, GLMDebug.ERROR_LEVEL);
  }

  public boolean isPrintConcise() {
    return forcePrintConcise || super.isPrintConcise();
  }

  protected void forcePrintConcise(boolean f) {
    forcePrintConcise = f;
  }

  public static class IMDeletionPolicy extends DeletionPlugIn.DeletionPolicy {
    public String supplyType_;
  }

  private IMDeletionPolicy createDeletionPolicy(long deletionDelay) {
    IMDeletionPolicy policy =
      (IMDeletionPolicy) ldmFactory_.newPolicy(IMDeletionPolicy.class.getName());
    policy.supplyType_ = supplyType_;
    policy.init(supplyType_ + " Due Out Deletion",
		inventoryPlugIn_.getDueOutPredicate(supplyType_),
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

  protected void resetInventories() {
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

  protected void accountForWithdraws() {
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

  protected void computeThresholdSchedule(Inventory inventory) {
    InventoryPG invpg = 
      (InventoryPG)inventory.getInventoryPG();
    invpg.computeThresholdSchedule(daysOnHand_,
				   daysForward_,
				   daysBackward_,
				   getMinReorderLevel(inventory),
				   getMaxReorderLevel(inventory),
				   goalLevelMultiplier_);
  }

  protected Vector generateInactiveProjections(Inventory inventory, int switchoverDay) {
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

  /**
   * Reorder increment specifies an adjustment to duein preferences
   * to keep the inventory level above the target level. Since each
   * projection produces a piece of a piecewise-linear approximation
   * to the needed level we need to find a series of straight lines
   * that overbound the target level. Starting at the startDay, we
   * find the next horizon day to delimit a segment. The horizon day
   * is the day for which the slope of the line from the value on
   * one day to the value on the other is maximum. Think of ranges
   * of mountains in the distance. some peaks in the foreground will
   * have others behind them that reach higher and higher until,
   * finally, there is a peak that obscures all the peaks behind it.
   * The peaks behind may be higher, but because of the point of
   * view, they can't be seen. From the peak on the horizon day,
   * find the next horizon day, and so on until the last day is
   * reached.
   **/
  protected static class SegmentInfo {
    int nDays;
    int endDay;
    double startLevel;
    double endLevel;
    SegmentInfo(int nDays, double startLevel, double endLevel) {
      this.nDays = nDays;
      this.startLevel = startLevel;
      this.endLevel = endLevel;
    }
    public double getSlope() {
      return (endLevel - startLevel) / nDays;
    }
    public int getDays() {
      return nDays;
    }
  }

  protected SegmentInfo[] determineReorderProfile(int startDay, int endDay, Inventory inventory) {
    int e = endDay - startDay;
    List segments = new ArrayList();
    if (e == 0) return new SegmentInfo[0];

    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
    double[] level = new double[e + 1];
    for (int i = 0; i <= e; i++) {
      //              level[i] = 0.5 * (invpg.getReorderLevel(i + startDay) + invpg.getGoalLevel(i + startDay));
      level[i] = invpg.getReorderLevel(i + startDay - 1);
    }
    if (e == 1) return new SegmentInfo[] {new SegmentInfo(1, level[0], level[1])};
    int i = 0;
    double li = level[i];
    while (true) {
      int ip = i + 1;
      double peakSlope = (level[ip] - li); // The bounding slope
      for (int j = ip + 1; j <= e; j++) {
	double lj = level[j];
	double thisSlope = (lj - li) / (j - i);
	double minLevel = (li + peakSlope * (j - i)) * 0.99;
	if (lj > minLevel) {
	  ip = j;     // Include in this segment
	  if (thisSlope > peakSlope) {
	    peakSlope = thisSlope;
	  }
	}
      }
      double lip = li + peakSlope * (ip - i);
      segments.add(new SegmentInfo(ip - i, li, lip));
      i = ip;
      li = lip;
      if (i == e) break;
    }
    return (SegmentInfo[]) segments.toArray(new SegmentInfo[segments.size()]);
  }

  protected static class ReorderIncrement {
    public double first;
    public double highest;
    public double last;
    public int highestDay;
  }

  protected ReorderIncrement determineReorderIncrement(int startDay, int endDay, Inventory inventory) {
    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
    double first = invpg.getGoalLevel(startDay);

    ReorderIncrement si = new ReorderIncrement();
    double highest = first;
    double last = invpg.getGoalLevel(endDay);
    int highestDay = startDay;
    for (int i = startDay + 1; i < endDay; i++) {
      double reorder = invpg.getGoalLevel(i);
	    
      if (reorder > highest){
	highest = reorder;
	highestDay = i;
      }
    }
    si.first = first * 1.1;
    si.highest = highest * 1.1;
    si.highestDay = highestDay;
    si.last = last * 1.1;
    return si;
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

  private double ignoreSmallIncrement(double increment, double x) {
    double t = x + increment;
    double diff = (x - increment) / t;
    if (diff < 0.0001 && diff > -0.0001) return x;
    return x + increment;
  }

  protected Rate createIncrementedDailyRate(Measure qty, double increment) {
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

  protected Task newProjectSupplyTask(Inventory inventory, long start, long end, Rate rate) {
    Task parentTask = inventoryPlugIn_.findOrMakeMILTask(inventory);
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

  protected Inventory getInventoryForTask (Task task) {
    return inventoryPlugIn_.findOrMakeInventory(supplyType_,(Asset)task.getDirectObject());
  }

  // ********************************************************
  //                                                        *
  // Generate/Handle Due Ins Section                        *
  //                                                        *
  // ********************************************************

  protected void generateHandleDueIns() {
    printDebug("Step 3: generateHandleDueIns()");
    addPreviousRefills();
  }

  protected void addPreviousRefills() {

    printDebug("      : addPreviousRefills()");
    int total=0;
    Iterator inventories = changedSet_.iterator();
    Inventory inv;
    InventoryPG invpg;
    while (inventories.hasNext()) {
      inv = (Inventory)inventories.next();
      invpg = (InventoryPG)inv.getInventoryPG();
      // maintainInventory Task is the parent of all the refills for this inventory
      Task maintainInventory = inventoryPlugIn_.findOrMakeMILTask(inv);
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

  public Task createRefillTask(Inventory inv, double refill_qty, long time) {
    Asset item = getInventoryAsset(inv);
    // create request task
    Vector prefs = new Vector();
    Preference p_start, p_end,p_qty;

    long end_time = defaultRefillEndTime(time,inv);
    //  	long start_time = defaultRefillStartTime(time,inv);

    //  	p_start = createDateAfterPreference(AspectType.START_TIME, start_time);
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

  // ********************************************************
  //                                                        *
  // Adjust Withdraws Section                               *
  //                                                        *
  // ********************************************************


  protected void adjustForInadequateInventory() {
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

  protected void adjustForInadequateInventory(Inventory inventory) {
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

  protected boolean checkFailedRefill(Inventory inventory, InventoryPG invpg, int day) {
    Task prev_refill = invpg.refillAlreadyFailedOnDay(day);
    if (prev_refill != null) {
      return orderRefill(inventory, day);
    }
    return false;
  }

  protected abstract boolean orderRefill(Inventory inventory, int day);

  protected boolean failDueOut(Inventory inventory, InventoryPG invpg, int day) {
    DueOut lowestPriorityDueOut = invpg.getLowestPriorityDueOutBeforeDay(day);
    if (lowestPriorityDueOut == null) return false;
    //      printDebug("Inventory level before failing allocation is "+
    //                 TimeUtils.dateString(invpg.convertDayToTime(day)) + " : "+invpg.getLevel(day)+
    //                 ", Reorder level :"+invpg.getReorderLevel(day));
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

  protected void updateWithdrawAllocations() {
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
  protected int refillNeeded(Inventory inventory, int startDay) {
    InventoryPG invpg = (InventoryPG)inventory.getInventoryPG();
    int days = invpg.getPlanningDays();
    for (int day = startDay; day < days; day++) {
      if (needRefill(inventory, day)) return day;
    }
    return DONE;
  }

  protected boolean needRefill(Inventory inventory, int day) {
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

  protected void refreshInventorySchedule() {
    Inventory inventory;
    InventoryPG invpg;
    Iterator inventories = changedSet_.iterator();

    printDebug("LAST STEP: REFRESHINVENTORYSCHEDULE()");
    while (inventories.hasNext()) {
      inventory = (Inventory)inventories.next();
      invpg = (InventoryPG)inventory.getInventoryPG();
      // 	    invpg.printInventoryLevels(inventory, clusterId_);
      invpg.updateContentSchedule(inventory);
      // detailed Inventory Schedule for demo purposes only
      Boolean detailed = (Boolean)inventoryPlugIn_.getParam("Detailed");
      if ((detailed != null) && detailed.booleanValue()) {
	invpg.updateDetailedContentSchedule(inventory);
      }
      invpg.updateInventoryLevelsSchedule(inventory);
    }
  }

  protected void clearInventorySchedule() {
    Enumeration inventories = inventoryPlugIn_.getInventoryBins(supplyType_);
    Inventory inventory;
    InventoryPG invpg;
    printError("clearInventorySchedule() !!!!!!!! Should not be called.");
    printDebug("LAST STEP: CLEARINVENTORYSCHEDULE()");
    while (inventories.hasMoreElements()) {
      inventory = (Inventory)inventories.nextElement();
      invpg = (InventoryPG)inventory.getInventoryPG();
      invpg.clearContentSchedule(inventory);
    }
  }

  /**
     method called from update when a GLS Rescind is detected. We
     simply make sure the inventory levels have been recomputed to
     reflect the removed dueins and dueouts.
  **/
  protected void handleGLSRescind() {
    accountForWithdraws();
    addPreviousRefills();
    Iterator inventories = changedSet_.iterator();
    while (inventories.hasNext()) {
      Inventory inventory = (Inventory)inventories.next();
      InventoryPG invpg = 
	(InventoryPG)inventory.getInventoryPG();
      invpg.determineInventoryLevels();
      invpg.updateContentSchedule(inventory);
    }
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

  private int printInventoryStatus() {

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
	inventoryPlugIn_.setFillToCapacity(supplyType_, pol.getFillToCapacity());
	changed = true;
      }
      if (pol.hasMaintainAtCapacityRule()) {
	inventoryPlugIn_.setMaintainAtCapacity(supplyType_, pol.getMaintainAtCapacity());
	changed = true;
      }
      if (pol.hasSwitchoverRule()) {
	ProjectionWeight newWeight =
	  new ProjectionWeightImpl(pol.getWithdrawSwitchoverDay(),
				   pol.getRefillSwitchoverDay(),
				   pol.getTurnOffProjections());
	inventoryPlugIn_.setProjectionWeight(supplyType_, newWeight);
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
