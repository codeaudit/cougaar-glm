/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions LLC (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.asset.Capacity;
import org.cougaar.domain.glm.ldm.asset.Person;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.plugins.TaskUtils;
import org.cougaar.domain.glm.plugins.TimeUtils;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.measure.FlowRate;
import org.cougaar.domain.planning.ldm.measure.CountRate;


import java.text.DateFormat;
import java.util.*;

import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectScorePoint;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.RoleSchedule;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ScheduleType;
import org.cougaar.domain.planning.ldm.plan.ScheduleUtilities;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.TimeAspectValue;
import org.cougaar.domain.planning.ldm.plan.ScheduleUtilities;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.domain.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.domain.glm.ldm.plan.LaborSchedule;
import org.cougaar.domain.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.domain.glm.ldm.Constants;

/** Composes inventory and capacity schedules from ALP log plan objects.
  Note that the getters return null if the schedule requested is null
  or empty.
  */
// PAS does anyone use TotalSchedule - no
// PSP REQUESTED_DUE_OUT_SHORTFALL not use ie getDueOutShortfallSchedule not used
public class UIInventoryImpl {
  static final String[][] capacityInfo = {
      { "AmmunitionTransportation", "Tons" },
      { "AmmunitionHandling", "Tons/Day" },
      { "AmmunitionStorage", "Tons" },
      { "FuelTransportation", "Gallons" },
      { "FuelHandling", "Gallons/Day" },
      { "FuelStorage", "Gallons" },
      { "WaterTransportation", "Gallons" },
      { "WaterHandling", "Gallons/Day" },
      { "WaterStorage", "Gallons" },
      { "ContainerTransportation", "Count" },
      { "NonContainerTransportation", "Tons" },
      { "MaterielHandling", "Tons/Day" },
      { "MaterielStorage", "Tons" },
      { "PassengerTransportation", "Count" },
      { "HETTransportation", "Tons" },
  };
  static final String[] fuelTypes = { "DF2", "DFM", "JP5", "JP8", "MUG" };

  private final static boolean ignoreStartTime=true;

  public final static String NO_INVENTORY_SCHEDULE_JUST_CONSUME="NO INVENTORY SCHEDULE(DEMAND)";

  Asset asset;
  TimeSpanSet dueInSchedule = new TimeSpanSet();
  TimeSpanSet unconfirmedDueInSchedule = new TimeSpanSet();
  TimeSpanSet requestedDueInSchedule = new TimeSpanSet();
  TimeSpanSet projectedDueInSchedule = new TimeSpanSet();
  TimeSpanSet projectedRequestedDueInSchedule = new TimeSpanSet();
  TimeSpanSet inactiveDueInSchedule = new TimeSpanSet();
  TimeSpanSet inactiveUnconfirmedDueInSchedule = new TimeSpanSet();
  TimeSpanSet inactiveRequestedDueInSchedule = new TimeSpanSet();
  TimeSpanSet inactiveProjectedDueInSchedule = new TimeSpanSet();
  TimeSpanSet inactiveProjectedRequestedDueInSchedule = new TimeSpanSet();

  Vector dueOutLaborSchedule = null;
  Vector laborSchedule = null;

  Vector onHandSchedule = null;

  Vector dueOutSchedule = null;
  Vector projectedDueOutSchedule = null;
  Vector projectedDueOutLaborSchedule = null;
  Vector requestedDueOutSchedule = null;
  Vector projectedRequestedDueOutSchedule = null;

  Vector inactiveDueOutSchedule = null;
  Vector inactiveProjectedDueOutSchedule = null;
  Vector inactiveRequestedDueOutSchedule = null;
  Vector inactiveProjectedRequestedDueOutSchedule = null;
//    Vector dueOutShortfallSchedule = null;
//  Vector totalSchedule = null;
  final static long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
  static boolean debug;    

  public UIInventoryImpl() {
      // print debug messages if inventory_debug set to true
    debug = false;
    String val = System.getProperty("inventory_debug");
    if (val != null) {
	if (val.equals("true")) {
	    debug = true;
	}
    }
  }

    
  public Asset getAsset() {return this.asset;}

  protected Asset getInsideAsset() {
      if((asset instanceof ALPAsset) &&
	 (((ALPAsset) asset).getScheduledContentPG() != null))
	  return ((ALPAsset)asset).getScheduledContentPG().getAsset();
      else
	  return this.asset;
  }

  /** Set asset for this inventory object.
   * Handle Labor assets differently from non-labor.
   */

  public void setAsset(Asset asset) {
    this.asset = asset;
    
    if((asset instanceof ALPAsset) &&
       (((ALPAsset) asset).getScheduledContentPG() != null)){

	Schedule s = ((ALPAsset)asset).getScheduledContentPG().getSchedule();
	if (s != null) {
	    if (s.getScheduleType().equals(PlanScheduleType.TOTAL_CAPACITY)) {
		if (s instanceof LaborSchedule)
		    s = ((LaborSchedule)s).getQuantitySchedule();
		else 
		    System.out.println("UIInventoryImpl WARNING: Expected labor schedule");	  
		laborSchedule = scheduleToNonOverlapVector(s);
	    } else
		onHandSchedule = scheduleToNonOverlapVector(s);
	}
	if (isLaborAsset(asset)){
	    dueOutLaborSchedule = computeDueOutVector(false); // All non-inventory assets are active
	    projectedDueOutLaborSchedule = computeProjectedDueOutVector(false);
	    //        setDueOutLaborSchedule();
	}
	else {
	    dueOutSchedule = computeDueOutVector(false);
	    inactiveDueOutSchedule = computeDueOutVector(true);
	    projectedDueOutSchedule = computeProjectedDueOutVector(false);
	    inactiveProjectedDueOutSchedule = computeProjectedDueOutVector(true);
            requestedDueOutSchedule = computeRequestedDueOutVector(false);
            inactiveRequestedDueOutSchedule = computeRequestedDueOutVector(true);
            projectedRequestedDueOutSchedule = computeProjectedRequestedDueOutVector(false);
            inactiveProjectedRequestedDueOutSchedule = computeProjectedRequestedDueOutVector(true);
//  	    setRequestedDueOutSchedule(); 
//  	    setProjectedRequestedDueOutSchedule();
//          setDueOutShortfallSchedule();
	}
    }
  }

    /** It's a labor asset if the class of the asset is Capacity or
        if the class of the inner asset is Person.
    */

    private boolean isLaborAsset(Asset asset) {
        if (asset instanceof Capacity)
            return true;
        if (getInsideAsset() instanceof Person)
            return true;
        return false;
    }

//      /** total = onHand + dueOut
//    private void setTotalSchedule() {
//      Schedule onHandALPSchedule = getALPSchedule(onHandSchedule);
//      Schedule dueOutALPSchedule = getALPSchedule(dueOutSchedule);
//      Schedule total = ScheduleUtilities.addSchedules(onHandALPSchedule,
//                                                      dueOutALPSchedule);
//      if (debug)	{
//  	System.out.println("Available Schedule");
//  	printSchedule(onHandALPSchedule);
//  	System.out.println("Allocated ALP Schedule");
//  	printSchedule(dueOutALPSchedule);
//  	System.out.println("Total Schedule");
//  	printSchedule(total);
//      }
//      totalSchedule = scheduleToVector(total);
//    }

 //   public Vector getTotalSchedule() {
//      setTotalSchedule(); // only compute this if needed
//      return getSchedule(totalSchedule);
//    }

    // checks if empty schedule, return null
    protected Vector getSchedule(Vector sched) {
	if (sched == null) return null;
	if (sched.size() == 0) return null;
	return sched;
    }

    // checks if empty schedule, return null
    protected TimeSpanSet checkTimeSpanSet(TimeSpanSet set) {
	if (set == null) {
	    System.out.println("Null timespanset");
	    return null;
	}
	if (set.size() == 0) {
	    System.out.println("Empty timespanset");
	    return null;
	}
	return set;
    }
			       

  /** Get human readable form of the TypeIdentificationPG.
    @return String - the nomenclature from the TypeIdentificationPG
   */
  public String getAssetName() {
    TypeIdentificationPG typeIdPG = this.getInsideAsset().getTypeIdentificationPG();
    return typeIdPG.getNomenclature() + ":" + typeIdPG.getTypeIdentification();
  }

  /**
    Returns a schedule as defined in ScheduleType
    */

  public String getScheduleType() {
      if((asset instanceof ALPAsset) &&
	 (((ALPAsset) asset).getScheduledContentPG() != null)) {
	  Schedule s = ((ALPAsset)asset).getScheduledContentPG().getSchedule();
	  if (s != null)
	      return s.getScheduleType();
      }
      else {
	  return NO_INVENTORY_SCHEDULE_JUST_CONSUME;
      }

      return "";
  }

  /**
    Returns the unit type -- i.e. gallons, man hours, tons, etc.
    */

  public String getUnitType() {
    Asset insideAsset=this.getInsideAsset();
    String typeId =
      insideAsset.getTypeIdentificationPG().getTypeIdentification();
    String nomenclature =
      insideAsset.getTypeIdentificationPG().getNomenclature();

    String scheduleType = getScheduleType();

    // capacity
    if (scheduleType.equals(PlanScheduleType.TOTAL_CAPACITY) ||
        scheduleType.equals(PlanScheduleType.AVAILABLE_CAPACITY) ||
        scheduleType.equals(PlanScheduleType.ACTUAL_CAPACITY) ||
        scheduleType.equals(PlanScheduleType.LABOR)) {
      if (typeId.startsWith("MOS"))
        return "Hours/Day";
      else {
        for (int i = 0; i < capacityInfo.length; i++)
          if (capacityInfo[i][0].equals(typeId))
            return capacityInfo[i][1];
      }
      return "";
    }
    
    // inventory
    if (typeId.startsWith("NSN")) {
      for (int i = 0; i < fuelTypes.length; i++)
        if (fuelTypes[i].equals(nomenclature))
          return "Gallons"; // fuel
      return "Items"; // consumables
    } else 
      return "STons"; // ammunition
  }

  /**
     Get the schedule that indicates the on hand inventory for this asset.
     @return Vector - vector of UIQuantityScheduleElement
  */
  public Vector getOnHandSchedule() {
      return getSchedule(onHandSchedule);
  }

  /** Take an ALP schedule and make it into a vector of UIQuantityScheduleElement
    for serialization.
    */
  static private Vector scheduleToVector(Schedule ALPSchedule) {
    Vector s = new Vector();
    Enumeration elements = ALPSchedule.getAllScheduleElements();
    while (elements.hasMoreElements()) {
      QuantityScheduleElement scheduleElement = 
	(QuantityScheduleElement)elements.nextElement();
      s.addElement(new UIQuantityScheduleElement(scheduleElement));
    }
    return s;
  }

  private UIQuantityScheduleElement getScheduleElementFromTask(Task task,
							      boolean useStartTime) {
    if (task == null) {
      System.out.println("UIInventoryImpl WARNING: Allocation result is null");
      return null;
    }

    double quantity;
    
    if(TaskUtils.isProjection(task)) {
	Rate r = getProjectionRate(task);
	if(r == null) 
	    quantity = -1;
	else
	    quantity = getDailyQty(r);

    }
    else {
	quantity  = getPreferenceValue(task, AspectType.QUANTITY);
    }
    if (quantity < 0) {
      System.out.println("UIInventoryImpl WARNING: EXPECTED QUANTITY IN ALLOCATION RESULT WHEN DERIVING SCHEDULE ELEMENT FROM TASK");
      return null; // ignore if no quantity
    }

    long start_time = getPreferredTime(task, AspectType.START_TIME);
    if (start_time < 0) {
      return null; // ignore if no start time
    }

    long end_time = getPreferredTime(task, AspectType.END_TIME);
    if (end_time < 0) {
      end_time = start_time; // if no end time, make end = start
    }

    //    if (debug)  System.out.println("Creating schedule: " + quantity +
    //		       " start time: " + start_time +
    //		       " end time: " + end_time);

    // use end time only
    if (useStartTime)
	return new UIQuantityScheduleElement(start_time, end_time, quantity);
    else
	return new UIQuantityScheduleElement(end_time-1,end_time, quantity);
  }


  private Vector getScheduleFromProjectionTask(Task projectTask, boolean useStartTime, boolean isInactive) {
    Vector schedule = null;
    if (projectTask == null) {
      System.out.println("UIInventoryImpl WARNING: Project Task is null");
      return null;
    }
    if(!TaskUtils.isProjection(projectTask)) {
	System.out.println("UIInventoryImpl WARNING: Expected Project Task.");
	return null;
    }

    double quantity;    
    Rate r = getProjectionRate(projectTask);
    if(r == null) 
	quantity = -1;
    else
	quantity = getDailyQty(r);

    //    if((projectTask.getVerb().equals(Constants.Verb.PROJECTWITHDRAW)) &&
    //   (r!=null)) {
    //	System.out.println("getScheduleFromProjectionTask:: Project Withdraw task with rate of: " + r + " heres the task " + TaskUtils.taskDesc(projectTask));
    //}

    if (quantity < 0) {
      System.out.println("UIInventoryImpl WARNING: EXPECTED DAILY QTY QUANTITY IN PROJECTION TASK");
      return null; // ignore if no quantity
    }

    long start_time = getPreferredTime(projectTask, AspectType.START_TIME);
    if (start_time < 0) {
      System.out.println("UIInventoryImpl  WARNING: NO START TIME IN PROJECT TASK");
      return null; // ignore if no start time
    }

    long end_time = getPreferredTime(projectTask, AspectType.END_TIME);
    if (end_time < 0) {
      System.out.println("UIInventoryImpl  WARNING: NO END TIME IN PROJECT TASK");
      return null;
      // end_time = start_time; // if no end time, make end = start
    }

    long elementStartTime=start_time;
    long elementEndTime;

    while(elementStartTime < end_time) {
	elementEndTime = elementStartTime + (TimeUtils.MSEC_PER_DAY-1);
	if (elementEndTime > end_time)
	    elementEndTime = end_time;
	if(schedule == null)
	    schedule = new Vector();
	// use end time only
        UIQuantityScheduleElement el;
	if (useStartTime)
	    el = new UIQuantityScheduleElement(elementStartTime, elementEndTime, quantity);
	else
	    el = new UIQuantityScheduleElement(elementEndTime-1,elementEndTime, quantity);
        if (isInactive(projectTask, el) == isInactive) {
          schedule.add(el);
        }
	elementStartTime = elementEndTime + 1;
    }
    return schedule;
  }



    
    public static Rate getProjectionRate(Task task) {	
	Rate r = TaskUtils.getRate(task);
	return r;
    }

    public static double getDailyQty(Rate aRate) {
	if (aRate instanceof FlowRate) {
	    return ((FlowRate) aRate).getGallonsPerDay();
	}
	else if (aRate instanceof CountRate) {
	    return ((CountRate) aRate).getEachesPerDay();
	}
	else {
	    System.out.println("UIInventoryImpl::getDailyQty - unknown type of rate - " + aRate.getClass().getName());
	    return -1;
	}
    }


    public static double getPreferenceValue(Task task, int aspect_type) {
	Preference task_pref = task.getPreference(aspect_type);
	if (task_pref == null) {
	    return -1;
	} else if (task_pref.getScoringFunction() == null) {
	    return -1;
	} else if (task_pref.getScoringFunction().getBest() == null) {
	    return -1;
	} else {
	    return task_pref.getScoringFunction().getBest().getValue();
	}
    }

  /**
     useStartTime is used because refill tasks are credited to the
     inventory at the end time of the task and withdraw tasks are
     debited from the inventory at their start time. This routine is
     used for both those cases.
   **/
  private UIQuantityScheduleElement getScheduleFromAllocation(AllocationResult allocationResult, 
                                                              boolean useStartTime)
  {
    long startTime = -1;
    long endTime = -1;
    double quantity = 0;

    if (allocationResult == null) {
      System.out.println("UIInventoryImpl WARNING: Allocation result is null");
      return null;
    }

    if (allocationResult.isDefined(AspectType.QUANTITY))
      quantity = allocationResult.getValue(AspectType.QUANTITY);
    else {
      System.out.println("UIInventoryImpl WARNING: EXPECTED QUANTITY IN ALLOCATION RESULT FOR DUE OUT");
      return null; // ignore if no quantity
    }

    if (allocationResult.isDefined(AspectType.START_TIME)) {
      startTime = (long)allocationResult.getValue(AspectType.START_TIME);
    } else {
      return null; // ignore if no start time
    }

    if (allocationResult.isDefined(AspectType.END_TIME)) {
      endTime = (long)allocationResult.getValue(AspectType.END_TIME);
    } else {
      endTime = startTime; // if no end time, make end = start
    }

    //    if (debug)  System.out.println("Creating schedule: " + quantity +
    //		       " start time: " + startTime +
    //		       " end time: " + endTime);

    // use end time only
    if (useStartTime)
	return new UIQuantityScheduleElement(startTime, startTime+1, quantity);
    else
	return new UIQuantityScheduleElement(endTime-1, endTime, quantity);
  }

  /**
    Add schedule elements from an allocation to the due-ins. Get the
    start and end times and quantity from the allocation reported
    results. Adds the elements to the inactive or active schedules
    depending on the time of the elements. IGNORE ALLOCATION RESULTS
    IF isSuccess IS FALSE. */

  public void addDueInSchedule(Allocation allocation) {
    Task task = allocation.getTask();
    AllocationResult ar = allocation.getReportedResult();
    if (TaskUtils.isProjection(task)) {
      /* Use requested value for now. Due to fill-or-kill request and
         actual should match */
      if (ar == null || ar.isSuccess()) {
        // if unconfirmed or successful add to dueInSchedule
        for (int i = 0; i < 2; i++) {
          boolean isInactive = (i == 0);
          Vector schedule = getScheduleFromProjectionTask(task, !ignoreStartTime, isInactive);
	  if (schedule != null) {
//          if (debug) {
//            System.out.println("Adding projected due in schedule from allocation: " + 
//                               allocation.getUID());
//            for (int j = 0; j < schedule.size(); j++) 
//              printQuantityScheduleElement((UIQuantityScheduleElement) schedule.elementAt(j));
//          }
            if (isInactive) {
              inactiveProjectedDueInSchedule.addAll(schedule);
            } else {
              projectedDueInSchedule.addAll(schedule);
            }
          }
        }
      }
    } else if (ar == null) {
      // Use request if no allocation result
      UIQuantityScheduleElement se = 
        getScheduleElementFromTask(task, !ignoreStartTime); // Use end time for due-ins
      if (se != null) {
//      if (debug) {
//  	  System.out.println("Adding unconfirmed due in schedule from allocation: " + 
//			     allocation.getUID());
//  	  printQuantityScheduleElement(se);
//  	}
        if (isInactive(task, se)) {
          inactiveUnconfirmedDueInSchedule.add(se);
        } else {
          unconfirmedDueInSchedule.add(se);
        }
      }
    } else if (ar.isSuccess()) {
      UIQuantityScheduleElement se = 
        getScheduleFromAllocation(allocation.getReportedResult(), !ignoreStartTime);
      if (se != null) {
//      if (debug) {
//        System.out.println("Adding due in schedule from allocation: " + 
//                           allocation.getUID());
//        printQuantityScheduleElement(se);
//      }
        if (isInactive(allocation, se)) {
          inactiveDueInSchedule.add(se);
        } else {
          dueInSchedule.add(se);
        }
      }
    } else {
      // Ignore if not successful
    }
  }

  /** Add schedule elements from the preferences in the task in the due-in schedule.
   */

  public void addRequestedDueInSchedule(Allocation allocation) {
    Task task = allocation.getTask();
    if (task == null) {
      System.out.println("UIInventoryImpl WARNING: no task in due-in allocation");
      return;
    }

    double quantity=-1;
    long startTime = getPreferredTime(task, AspectType.START_TIME);
    long endTime = getPreferredTime(task, AspectType.END_TIME);
    
    if (TaskUtils.isProjection(task)) {
      for (int i = 0; i < 2; i++) {
        boolean isInactive = (i == 0);
	Vector projectSchedule = getScheduleFromProjectionTask(task, !ignoreStartTime, isInactive);
	if (projectSchedule != null) {
          if (isInactive)
	    inactiveProjectedRequestedDueInSchedule.addAll(projectSchedule);
          else
	    projectedRequestedDueInSchedule.addAll(projectSchedule);
        }
      }
      return;
    }
    quantity  = getPreferenceValue(task, AspectType.QUANTITY);

    // must have start time and quantity
    if ((quantity != -1) && (startTime != -1)) { 
      if (endTime == -1)
	endTime = startTime;
      // use end time only
      UIQuantityScheduleElement se = 
 	new UIQuantityScheduleElement(endTime-1, endTime, quantity);
//        if (debug) {
//  	  System.out.println("Adding requested due in schedule from task: " + 
//  			     task.getUID());
//  	  printQuantityScheduleElement(se);
//        }

      if (isInactive(task, se))
        inactiveRequestedDueInSchedule.add(se);
      else
        requestedDueInSchedule.add(se);
    }
  }

  private Vector convertTimeSpanSet(TimeSpanSet schedule, String debugLabel) {
     if (checkTimeSpanSet(schedule) == null) return null;
     
     if (debug) {
	 // 	// print original schedule for debugging
	 System.out.println("ORIGINAL " + debugLabel + " SCHEDULE");
	 Vector s = new Vector(schedule);
	 printSchedule(s);
     }
     
     // make an alp schedule so we can use the alp utilities
     // to make it non-overlapping
     Schedule tmpSchedule = makeNonOverlapping(schedule);
     Vector results = scheduleToVector(tmpSchedule);
     
     if (debug)  {
	 System.out.println("FINAL " + debugLabel + " SCHEDULE");
	 printSchedule(results);
     }	
     return results;
  }

  /**
     Get the schedule that indicates the due-in inventory for this asset.
     @return Vector - the schedule for this asset in this cluster
  */
  public Vector getDueInSchedule() {
    return convertTimeSpanSet(dueInSchedule, "DUE IN");
  }

  /**
     Get the schedule that indicates the due-in inventory for this asset.
     @return Vector - the schedule for this asset in this cluster
  */
  public Vector getUnconfirmedDueInSchedule() {
    return convertTimeSpanSet(unconfirmedDueInSchedule, "UNCONFIRMED DUE IN");
  }

  /**
     Get the schedule that indicates the projected due-in inventory for this asset.
     @return Vector - the schedule for this asset in this cluster
  */
  public Vector getProjectedDueInSchedule() {
    return convertTimeSpanSet(projectedDueInSchedule, "PROJECTED DUE IN");
  }

  /**
     Get the schedule that indicates the inactive due-in inventory for this asset.
     @return Vector - the schedule for this asset in this cluster
  */
  public Vector getInactiveDueInSchedule() {
    return convertTimeSpanSet(inactiveDueInSchedule, "INACTIVE DUE IN");
  }

  /**
     Get the schedule that indicates the due-in inventory for this asset.
     @return Vector - the schedule for this asset in this cluster
  */
  public Vector getInactiveUnconfirmedDueInSchedule() {
    return convertTimeSpanSet(inactiveUnconfirmedDueInSchedule, "INACTIVE UNCONFIRMED DUE IN");
  }

  /**
     Get the schedule that indicates the projected due-in inventory for this asset.
     @return Vector - the schedule for this asset in this cluster
  */
  public Vector getInactiveProjectedDueInSchedule() {
    return convertTimeSpanSet(inactiveProjectedDueInSchedule, "INACTIVE PROJECTED DUE IN");
  }

    public static Schedule makeNonOverlapping(TimeSpanSet inSchedule) {
	// make an alp schedule so we can use the alp utilities
	// to make it non-overlapping
	Schedule tmpALPSchedule = getALPSchedule(inSchedule);
	if (isOverlappingSchedule(tmpALPSchedule)) {
	    if (debug)  System.out.println("UIInventoryImpl::makeNonOverlapping:IS OVERLAPPING");
	    tmpALPSchedule =
		ScheduleUtilities.computeNonOverlappingSchedule(tmpALPSchedule);
	} else if (debug) {
	    System.out.println("UIInventoryImpl::makeNonOverlapping:is NOT Overlapping");
	}
	return tmpALPSchedule;
    }

    /** Get the requested due in schedule.
     */
    public Vector getRequestedDueInSchedule() {
	if (checkTimeSpanSet(requestedDueInSchedule) == null) return null;
	return new Vector(requestedDueInSchedule); 
    }

    /** Get the projected requested due in schedule.
     */
    public Vector getProjectedRequestedDueInSchedule() {
	if (checkTimeSpanSet(projectedRequestedDueInSchedule) == null) return null;
	return new Vector(projectedRequestedDueInSchedule); 
    }


    /** Get the requested due in schedule.
     */
    public Vector getInactiveRequestedDueInSchedule() {
	if (checkTimeSpanSet(inactiveRequestedDueInSchedule) == null) return null;
	return new Vector(inactiveRequestedDueInSchedule); 
    }

    /** Get the projected requested due in schedule.
     */
    public Vector getInactiveProjectedRequestedDueInSchedule() {
	if (checkTimeSpanSet(inactiveProjectedRequestedDueInSchedule) == null) return null;
	return new Vector(inactiveProjectedRequestedDueInSchedule); 
    }


    /** Get the schedule that indicates the due-out (allocated)
	schedule for a labor asset.  This is similar to the due-out
	inventory schedule, but uses both the start and end times.
    */

  public Vector getDueOutLaborSchedule() {
      return getSchedule(dueOutLaborSchedule);
  }

  /**
     Get the schedule that indicates the due-out inventory for this asset.
     The schedule is from the allocations reported results from
     the allocations in the role schedules attached to the assets.
     @return Vector - the schedule for this asset in this cluster
  */
  
  public Vector getDueOutSchedule() {
      return getSchedule(dueOutSchedule);
  }

  public Vector getInactiveDueOutSchedule() {
      return getSchedule(inactiveDueOutSchedule);
  }

  public Vector getProjectedDueOutSchedule() {
      return getSchedule(projectedDueOutSchedule);
  }

  public Vector getInactiveProjectedDueOutSchedule() {
      return getSchedule(inactiveProjectedDueOutSchedule);
  }

    /*  Get allocations to this.asset from the RoleSchedule.
     *  Create schedule where each element is based on an allocation result. */
  private Vector computeDueOutVector(boolean inactive) {
       return computeDueOutVectorWVerb(Constants.Verb.WITHDRAW, inactive);
  }


    /*  Get allocations to this.asset from the RoleSchedule.
     *  Create schedule where each element is based on an allocation result. */
  private Vector computeProjectedDueOutVector(boolean inactive) {
      return computeDueOutVectorWVerb(Constants.Verb.PROJECTWITHDRAW, inactive);
  }
    

    /*  Get allocations to this.asset from the RoleSchedule.
     *  Create schedule where each element is based on an allocation result. */
    private Vector computeDueOutVectorWVerb(String compareVerb, boolean inactive){
    RoleSchedule roleSchedule = asset.getRoleSchedule();
    if(debug) {System.out.println("UIInventoryImpl-Projected Due Outs:");}
    if (roleSchedule == null) {
      System.out.println("UIInventoryImpl WARNING: no role schedule in asset");
      return null;
    }
    Enumeration e = roleSchedule.getRoleScheduleElements();
    if (e == null) {
      System.out.println("UIInventoryImpl WARNING: no role schedule in role schedule");
      return null;
    }

    Vector due_outs = new Vector();
    while (e.hasMoreElements()) {
      Allocation allocation = (Allocation) e.nextElement();
      Task task = allocation.getTask();
      AllocationResult er = allocation.getEstimatedResult();
      Verb dueOutTaskVerb= task.getVerb();

      if ((dueOutTaskVerb.equals(compareVerb))) {
	  if(TaskUtils.isProjection(task)) {
	      if((er==null) || (er.isSuccess())) {
		  Vector projectionScheduleExpansion = 
		      getScheduleFromProjectionTask(task,true,inactive);
		  due_outs.addAll(projectionScheduleExpansion);
	      }
	  }
	  else {
	      UIQuantityScheduleElement el = getScheduleFromAllocation(er, true);
	      if ((el != null) &&
		  (isInactive(allocation, el) == inactive)) {
		  due_outs.addElement(el); 
	      }
	  }
      }
    }
    return scheduleToNonOverlapVector(due_outs);
  }
    
  private boolean isInactive(Task task, UIQuantityScheduleElement el) {
    return isInactive(task, el.getStartTime());
  }

  private boolean isInactive(Allocation alloc, UIQuantityScheduleElement el) {
    return isInactive(alloc.getTask(), el.getStartTime());
  }

  private boolean isInactive(Allocation alloc, long time) {
    return isInactive(alloc.getTask(), time);
  }

  private boolean isInactive(Task task, long time) {
    if (asset instanceof Inventory) {
      Inventory inv = (Inventory) asset;
      InventoryPG invpg = inv.getInventoryPG();
      long invStart = invpg.getStartTime();
      int imputedDay = (int) ((time - invStart) / TimeUtils.MSEC_PER_DAY);
      return invpg.getProjectionWeight().getProjectionWeight(task, imputedDay) < 0.5;
    }
    return false;
  }

    static private Vector scheduleToNonOverlapVector(Vector schedule) {
	Vector   nonOverlapVector;
	// make an alp schedule so we can use the alp utilities
	// to make it non-overlapping
	if (schedule == null || schedule.size() == 0)
	    return null;
	Schedule tmpALPSchedule = getALPSchedule(schedule);
	return scheduleToNonOverlapVector(tmpALPSchedule);
    }

    static private Vector scheduleToNonOverlapVector(Schedule schedule) {
	Vector   nonOverlapVector;
	if (debug)  {
	    System.out.println("Original schedule");
	    printSchedule(schedule);
	}
	if (isOverlappingSchedule(schedule)) {
	    Schedule nonoverlapping =
		ScheduleUtilities.computeNonOverlappingSchedule(schedule);
	    nonOverlapVector = scheduleToVector(nonoverlapping);
	    if (debug)  {
		System.out.println("Is Overlapping::Computing non-overlapping schedule");
		printSchedule(nonOverlapVector);
	    }
	} else
	    nonOverlapVector = scheduleToVector(schedule);
	
	return nonOverlapVector;
    }

  private long getPreferredTime(Task task, int aspectType) {
    Preference preference = task.getPreference(aspectType);
    ScoringFunction scoringFunction = preference.getScoringFunction();
    AspectScorePoint pt = scoringFunction.getBest();
    AspectValue aspectValue = pt.getAspectValue();
    if (aspectValue instanceof TimeAspectValue)
      return ((TimeAspectValue)aspectValue).longValue();
    else 
      return aspectValue.longValue();
  }

  private Vector computeRequestedDueOutVector(boolean isInactive) {
      return computeRequestedDueOutVectorWVerb(Constants.Verb.WITHDRAW, isInactive);
  }

  private Vector computeProjectedRequestedDueOutVector(boolean isInactive) {
      return computeRequestedDueOutVectorWVerb(Constants.Verb.PROJECTWITHDRAW, isInactive);
  }

  private Vector computeRequestedDueOutVectorWVerb(String compareVerb, boolean isInactive) {
    RoleSchedule roleSchedule = asset.getRoleSchedule();
    Vector scheduleElements = new Vector();
    if (roleSchedule == null) {
      System.out.println("UIInventoryImpl WARNING: no role schedule in asset");
      return null;
    }
    Enumeration e = roleSchedule.getRoleScheduleElements();
    if (e == null) {
      System.out.println("UIInventoryImpl WARNING: no role schedule in role schedule");
      return null;
    }
    while (e.hasMoreElements()) {
      Allocation allocation = (Allocation) e.nextElement();
      Task task = allocation.getTask();
      if (task == null) { 
        System.out.println("UIInventoryImpl WARNING: no allocation task in allocation");
        continue;
      }

      //// If not the right kind of task, then don't compute and add to schedule
      if (!(task.getVerb().equals(compareVerb))) { 
	  //System.out.println("UIInventoryImpl::computeRequestedDueOutScheduleWVerb Unexpected verb: " + task.getVerb());
	  continue;
      }
      if(TaskUtils.isProjection(task)) {
	  Vector projectionScheduleExpansion = 
	      getScheduleFromProjectionTask(task,true,isInactive);
	  scheduleElements.addAll(projectionScheduleExpansion);
      }
      else { 
	  long startTime = getPreferredTime(task, AspectType.START_TIME);
	  long endTime = startTime + 1;
	  double quantity = task.getPreferredValue(AspectType.QUANTITY);
	  if (debug) System.out.println("Adding " + compareVerb + " requested due out task: " + task.getUID()+" "+quantity+" at "+new Date(startTime)+" to "+new Date(endTime));
	  // must have start time and quantity, but use specified start/end time
	  if ((quantity != -1) && (startTime != -1)) { 
	      if (isInactive(task, startTime) == isInactive) {
		  UIQuantityScheduleElement element;
		  element = new UIQuantityScheduleElement(startTime, endTime, quantity);
		  scheduleElements.addElement(element);
	      }
	  }
      }
    }
    return scheduleToNonOverlapVector(scheduleElements);
  }

   static private boolean isOverlappingSchedule(Schedule aSchedule) {
       Enumeration enum = aSchedule.getAllScheduleElements();
       long last_time = aSchedule.getStartTime()-1;
       while (enum.hasMoreElements()) {
	   ScheduleElement element = (ScheduleElement)enum.nextElement();
	   if (element.getStartTime() <= last_time) return true;
	   last_time = element.getEndTime();
       }
       return false;
   }

//   private boolean isOverlappingSchedule(Schedule aSchedule) {
//     long earliestTime = aSchedule.getStartTime();
//     long latestTime = aSchedule.getEndTime();
//     Calendar calendar = new GregorianCalendar();
//     calendar.setTime(new Date(earliestTime));
//     Vector scheduleElements = new Vector();
//     long time = earliestTime;
//     while (time <= latestTime) {
//       CountElementsAtTime counter = new CountElementsAtTime(time);
//       aSchedule.applyThunkToScheduleElements(counter);
//       if (counter.count > 1)
//         return true;
//       calendar.add(calendar.DAY_OF_YEAR, 1);
//       time = calendar.getTime().getTime();
//     }
//     return false;
//   }

  public Vector getRequestedDueOutSchedule() {
    return getSchedule(requestedDueOutSchedule);
  }

  public Vector getInactiveRequestedDueOutSchedule() {
    return getSchedule(inactiveRequestedDueOutSchedule);
  }

  public Vector getProjectedRequestedDueOutSchedule() {
    return getSchedule(projectedRequestedDueOutSchedule);
  }

  public Vector getInactiveProjectedRequestedDueOutSchedule() {
    return getSchedule(inactiveProjectedRequestedDueOutSchedule);
  }


  /** Take a schedule of UIQuantityScheduleElements and
    make it into an ALP schedule
    */

  private static Schedule getALPSchedule(Collection mySchedule) {
    Vector scheduleElements = new Vector();
    for (Iterator it = mySchedule.iterator(); it.hasNext();) {
      UIQuantityScheduleElement s = (UIQuantityScheduleElement)it.next();
      NewQuantityScheduleElement qse = ALPFactory.newQuantityScheduleElement();
      qse.setStartTime(s.getStartTime());
      qse.setEndTime(s.getEndTime());
      qse.setQuantity(s.getQuantity());
      scheduleElements.addElement(qse);
    }
    return ALPFactory.newQuantitySchedule(scheduleElements.elements(),
                                       PlanScheduleType.TOTAL_INVENTORY);
  }

 //   /** Shortfall = requestedDueOut - dueOut (shows as a positive shortage)
//     */

//    private void setDueOutShortfallSchedule() {
//      if (ALPRequestedDueOutSchedule == null || dueOutSchedule == null)
//        return;
//      if (dueOutSchedule.size() == 0) {
//        if (debug) 
//  	  System.out.println("Due out schedule is empty; using requested due out as due out shortfall");
//        dueOutShortfallSchedule = dueOutSchedule;
//        return;
//      }

//      Schedule dueOut = getALPSchedule(dueOutSchedule);
//      Enumeration scheduleElements =
//        ALPRequestedDueOutSchedule.getAllScheduleElements();
//      if (!scheduleElements.hasMoreElements()) {
//        if (debug) System.out.println("Requested due out schedule is empty; ignoring due out shortfall");
//        return;
//      }
//      Schedule results = ScheduleUtilities.subtractSchedules(dueOut,ALPRequestedDueOutSchedule);
//      dueOutShortfallSchedule = scheduleToVector(results);
//    }

//    public Vector getDueOutShortfallSchedule() {
//        return getSchedule(dueOutShortfallSchedule);
//    }

  public Vector getLaborSchedule() {
      return getSchedule(laborSchedule);
  }

  // for debugging
  static private void printSchedule(Schedule s) {
      if (s == null) return;
    Enumeration e = s.getAllScheduleElements();
    while (e.hasMoreElements()) {
      QuantityScheduleElement se = (QuantityScheduleElement)e.nextElement();
      System.out.println("Start date: " + shortDate(se.getStartTime()) +
			 " end date: " + shortDate(se.getEndTime()) +
			 " quantity: " + se.getQuantity());
    }
  }

  static private void printSchedule(Vector s) {
      if (s == null || s.isEmpty()) {
	  System.out.println("printSchedule() Empty Schedule");
	  return;
      }
      Enumeration e = s.elements();
      while (e.hasMoreElements()) {
	  printQuantityScheduleElement((UIQuantityScheduleElement)e.nextElement());
      }
  }

    static private void printQuantityScheduleElement(UIQuantityScheduleElement qse) {
      System.out.println("Start date: " + shortDate(qse.getStartTime()) +
			 " end date: " + shortDate(qse.getEndTime()) +
			 " quantity: " + qse.getQuantity());
    }

    static private String shortDate(long time) {
	String sdate = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date(time));
	// map '9/8/00 12:00 AM' to ' 9/8/00 12:00 AM'
	while(sdate.length()<17){
	    sdate = " "+sdate;
	}
	return sdate;
    }
}

class CountElementsAtTime implements org.cougaar.util.Thunk {
  private long time;
  public int count = 0;
  public CountElementsAtTime(long t) {
    time = t;
  }
  public void apply(Object o) {
    ScheduleElement se = (ScheduleElement) o;
    if (time >= se.getStartTime()  && time < se.getEndTime()) {
      count++;
    }
  }
}
