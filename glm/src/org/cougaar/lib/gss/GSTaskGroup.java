/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.util.UTILPreference;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.io.Serializable; 

/**
 * Hypothetical group of tasks along with currently assigned times;
 * Used for building up schedules using a greedy scheduler
 *
 */

public class GSTaskGroup implements Serializable {
  private Vector tasks;
  private long taskDuration;
  private Date earliestStart;
  private Date earliestEnd;
  private Date bestEnd;
  private Date latestEnd;
  private double[] requiredCapacities;
  private GSSchedulingSpecs specs;
  private Date currentEnd;
  private Date currentStart;
  private double currentScore = 0.0f;
  private boolean full = false;
  private boolean frozen = false; // immutable flag
    private static boolean debug = false;

  private static final long DAY_IN_MILLIS = 24l * 60l * 60l * 1000l;

  /** Constructor for single task groupings */
  public GSTaskGroup (Task task, long taskDuration, GSSchedulingSpecs specs){
    this.taskDuration = taskDuration;
    this.specs = specs;
    tasks = new Vector();
    tasks.addElement (task);
    earliestStart = specs.getTaskEarliestStart (task);
    //bestEnd = task.getPenaltyFunction().getDesiredScheduleBestDate();
    //earliestEnd = task.getPenaltyFunction().getDesiredScheduleEarliestDate();
    //latestEnd = task.getPenaltyFunction().getDesiredScheduleLatestDate();
    bestEnd = UTILPreference.getBestDate(task);
    earliestEnd = UTILPreference.getEarlyDate(task);
    latestEnd = UTILPreference.getLateDate(task);
    currentStart = new Date();
    currentEnd = new Date();
    requiredCapacities = specs.getRequiredCapacities (task);
  }

  private GSTaskGroup () {
  }

  /** to allow copies made of original object for hypothetical modifications
   *  without modifying original */
  public GSTaskGroup duplicate() {
    GSTaskGroup newG = new GSTaskGroup ();
    newG.tasks = (Vector) tasks.clone();
    newG.taskDuration = taskDuration;
    newG.earliestStart = new Date (earliestStart.getTime());
    newG.earliestEnd = new Date (earliestEnd.getTime());
    newG.bestEnd = new Date (bestEnd.getTime());
    newG.latestEnd = new Date (latestEnd.getTime());
    newG.requiredCapacities = new double [requiredCapacities.length];
    System.arraycopy (requiredCapacities, 0, newG.requiredCapacities,
                      0, requiredCapacities.length);
    newG.specs = specs;
    newG.currentScore = currentScore;
    return newG;
  }

  public List getCapacityConstraints () {
    return specs.getCapacityConstraints ();
  } 

  public double [] getCapacityLevels () {
    return requiredCapacities;
  }

  /** add a new task to group */
  public void addTask (Task task) {
    tasks.addElement (task);
    Date es = specs.getTaskEarliestStart (task);
    //Date ee = task.getPenaltyFunction().getDesiredScheduleEarliestDate();
    //Date be = task.getPenaltyFunction().getDesiredScheduleBestDate();
    //Date le = task.getPenaltyFunction().getDesiredScheduleLatestDate();
    Date ee = UTILPreference.getEarlyDate(task);
    Date be = UTILPreference.getBestDate(task);
    Date le = UTILPreference.getLateDate(task);
    if (es.after (earliestStart))
      earliestStart = es;
    if (ee.after (earliestEnd))
      earliestEnd = ee;
    if (le.before (latestEnd))
      latestEnd = le;
    bestEnd = new Date (bestEnd.getTime() +
                        (be.getTime() - bestEnd.getTime()) / tasks.size());
    requiredCapacities = specs.incrementCapacities (task, requiredCapacities,
						    this);
  }

  /** 
   * check if task grouping is internally consistent 
   *
   * 1   2   3   4   5   6
   * <------ task 1 ----->
   *     <-- task 2 ->
   *     |           |
   *     latest      earliest
   *     ready at    latest
   */
  public boolean isConsistent (Asset asset) {
    if (latestEnd.before (earliestEnd) ||
        (earliestStart.getTime() > (latestEnd.getTime() - taskDuration))) {
	if (true || debug) {
	    if (latestEnd.before (earliestEnd))
		System.out.println ("GSTaskGroup.isConsistent not consistent with asset " +
				    " latest End " + latestEnd + 
				    " before earliest End " + earliestEnd);
	    else 
		System.out.println ("GSTaskGroup.isConsistent not consistent with asset " +
				    " latest ReadyAT " + earliestStart + 
				    " after earliest Arrival - dur " + new Date(latestEnd.getTime() - taskDuration) +
				    " earliest Arrival = " + new Date(latestEnd.getTime()) +
				    " dur = " + taskDuration/3600000 + " hrs");
	}
	return false;
    }
    
    if (specs.withinCapacities (asset, requiredCapacities)) {
      return true;
    }

    if (debug)
	System.out.println ("GSTaskGroup.isConsistent not within capacities of asset " + asset);

    full = true;
    return false;
  }

  public boolean isFull() {
    return full;
  }

  public void setFrozen(boolean newfrozen) {
      this.frozen = newfrozen;
  }
  public boolean isFrozen() {
    return frozen;
  }

  /** get a representative task for doing grouping check */
  public Task representativeTask() {
    return (Task) tasks.elementAt (0);
  }

  public Vector getTasks() {
    return tasks;
  }
  
  private Date getEarliestStart() {
    long tm = earliestEnd.getTime() - taskDuration;
    return (earliestStart.getTime() >= tm) ? earliestStart : new Date (tm);
  }
  
  private Date getEarliestEnd() {
    long tm = earliestStart.getTime() + taskDuration;
    return (earliestEnd.getTime() >= tm) ? earliestEnd : new Date (tm);
  }
  
  private Date getBestEnd() {
    long tm = earliestStart.getTime() + taskDuration;
    if (tm > bestEnd.getTime())
      return (tm > earliestEnd.getTime()) ? new Date (tm) : earliestEnd;
    else
      return (earliestEnd.after (bestEnd)) ? earliestEnd : bestEnd;
  }

  /** currently assigned end time */
  public Date getCurrentEnd() {
    return currentEnd;
  }

  /** currently assigned start time */
  public Date getCurrentStart() {
    return currentStart;
  }

  
  private static Date[] getOverlappingExtent (Asset asset,
                                              Vector otherGroupings,
                                              Date start, Date end) {
    Date early = null;
    Date late = null;
    if(otherGroupings != null){
	for (int i = 0; i < otherGroupings.size(); i++) {
	    GSTaskGroup tg = (GSTaskGroup) otherGroupings.elementAt(i);
	    if (tg == null)
	      System.err.println("Task Group is null!  No element at " +
				 i + " in list of possible schedules");
	    if (tg.getCurrentEnd() == null)
	      System.err.println("Task Group current end is null!  End time "+
				 "not being correctly set for task group " +
				 i);
	    if (tg.getCurrentStart() == null)
	      System.err.println("Task Group current start is null! Start time"
				 +"not being correctly set for task group "+i);
	    if (! (tg.getCurrentEnd().after (start) &&
		   tg.getCurrentStart().before (end)))
		continue;
	    if (early == null) {
		early = tg.getCurrentStart();
		late = tg.getCurrentEnd();
	    } else {
		if (tg.getCurrentStart().before (early))
		    early = tg.getCurrentStart();
		if (tg.getCurrentEnd().after (late))
		    late = tg.getCurrentEnd();
	    }
	}
    }
    Date[] dates = getOverlapInterval (asset, start, end);
    if (early == null)
      return dates;
    if (dates != null) {
      if (dates[0] == null || dates[1] == null) 
	System.err.println("UTIL GSS ERROR: getOverlapInterval returned dates "
			   + dates[0] + " and " + dates[1]);
      if (dates[0].before (early))
        early = dates[0];
      if (dates[1].after (late))
        late = dates[1];
    }
    Date[] dates2 = {early, late};
    return dates2;
  }

  /**
   * Takes into account that estimated result on plan elements returned from asset role
   * schedule can be NULL.  If all are null, returns null Date [].
   *
   * @return Date [] - earliest and latest of all time blocks that overlap start and end
   *         can be null if 1) start = latest   of role time blocks 
   *                        2) end   = earliest of role time blocks 
   *                        3) there are no plan elements, or ones with valid estimated results
   */
  private static Date[] getOverlapInterval (Asset asset, Date startDate, Date endDate) {
    /*
    Enumeration pes = asset.getRoleSchedule().
      getOverlappingRoleSchedule(start, end).elements();    
    if (! pes.hasMoreElements())
      return null;
    ScheduleElement se = ((PlanElement) pes.nextElement()).getEstimatedSchedule();
    Date early = se.getStartDate();
    Date late = se.getEndDate();
    while (pes.hasMoreElements()) {
      se = ((PlanElement) pes.nextElement()).getEstimatedSchedule();
      if (early.after (se.getStartDate()))
        early = se.getStartDate();
      if (late.before (se.getEndDate()))
        late = se.getEndDate();
    }
    if (late.equals (start))
      return null;
    if (early.equals (end))
      return null;
    Date[] dates = {early, late};
    return dates;
    */

    long start = startDate.getTime();
    long end = endDate.getTime();
    Iterator pes = asset.getRoleSchedule().getOverlappingRoleSchedule(start, end).iterator();    
    AllocationResult ar;
    long early = 0;
    long late  = 0;

    if (debug && !pes.hasNext ())
	System.out.println ("NO overlap for " + asset + 
			    " from " + start + " to " + end);

    while (pes.hasNext()) {
	if (debug) System.out.println ("Found overlap for " + asset);

      ar = ((PlanElement) pes.next()).getEstimatedResult();
      if (ar != null) {
	long next_early = (new Double(ar.getValue(AspectType.START_TIME))).longValue();
	long next_late  = (new Double(ar.getValue(AspectType.END_TIME  ))).longValue();

	if (early == 0 || (early > next_early))
	  early = next_early;
	if (late  == 0 || (late < next_late))
	  late = next_late;
      }
      if (debug) System.out.println ("\tOverlap is " + early + " -> " + late);
    }

    if ((late == 0)  || (late == start))
      return null;
    if ((early == 0) || (early == end))
      return null;

    Date[] dates = {new Date(early), new Date(late)};
    return dates;
  }

private static long YEARINMILLIS = 1000l*60l*60l*24l*365l;

  /** get the best time for the group and return the score */
  public double pickBestTime (Asset asset, Vector otherGroupings) {
    double taskDurationDays = taskDuration / (double) DAY_IN_MILLIS;

    Date be = getBestEnd();
    Date bs = new Date (be.getTime() - taskDuration -
			specs.computePreTaskDuration(asset, 
						     be.getTime()-taskDuration,
						     representativeTask()));
    Date[] interval = null;
    boolean sunk = false;
    if (otherGroupings != null) { // i.e. not UNALIGNED!
      interval = getOverlappingExtent (asset, otherGroupings, bs, be);
      if ((interval != null) &&
	  (interval[1].getTime () - interval[0].getTime () > YEARINMILLIS)) {
	  sunk = true;
	  if (debug) 
	      System.out.println ("Found sunk ship " + asset + 
				  " interval start " + interval[0] + 
				  " end " + interval[1]);
      }
    }

    // can put at best time
    if (interval == null) {
      currentEnd = be;
      currentStart = bs;
      if (debug)
	  System.out.println ("GSTaskGroup - Can put at best time.");
      return taskDurationDays; // return 0.0f;
    }
    
    // set up for forwards and backward search
    Date[] oldInterval = interval;
    Date ee = getEarliestEnd();
    double bestScore = Double.POSITIVE_INFINITY;
    
    // do backwards search
    while (! interval[0].before (ee)) {
	if (debug && sunk)
	    System.out.println ("Backwards earliest end " + ee + 
				" interval start " + interval[0] + 
				" end " + interval[1]);

      Date newStart = new Date (interval[0].getTime() - taskDuration);
      Date[] newInt = getOverlappingExtent (asset, otherGroupings,
                                            newStart, interval[0]);
      if (newInt == null) {
        bestScore = (((double) (be.getTime() - interval[0].getTime())) /
                     ((double) (be.getTime() - ee.getTime())));
        currentEnd = interval[0];
        currentStart = newStart;
	if (debug && sunk)
	    System.out.println ("score = " + bestScore);
        break;
      }
      interval = newInt;
    }
    
    // do forward search
    interval = oldInterval;
    Date ls = new Date (latestEnd.getTime() - taskDuration);
    while (! interval[1].after (ls)) {
	if (debug && sunk)
	    System.out.println ("Forwards latest start " + ls + 
				" interval start " + interval[0] + 
				" end " + interval[1]);

      Date newEnd = new Date (interval[1].getTime() + taskDuration);
      Date[] newInt = getOverlappingExtent (asset, otherGroupings,
                                            interval[1], newEnd);
      if (newInt == null) {
        double score = Math.abs(((double) (newEnd.getTime() - be.getTime())) /
				((double) (latestEnd.getTime() - be.getTime())));
        if (score < bestScore) {
          bestScore = score;
          currentEnd = newEnd;
          currentStart = interval[1];
	  if (debug && sunk)
	      System.out.println ("score = " + bestScore);
        }
        break;
      }
      interval = newInt;
    }

    // The score is proportional to the closeness of the end time to the earliest
    // (even though it says the best, it returns negative if the newEnd is earlier 
    // than the best and so prefers that) which will always be < 1.0
    // AND
    // takes into account the number of tasks in this group
    // AND
    // prefers larger jumps in score
    
    double sc = currentScore;
    currentScore = bestScore * tasks.size();
    return currentScore - sc + taskDurationDays;
  }

  /**
   * Debugging
   */
  public String toString() {
   String s = "Group has " + getTasks().size() + " tasks\n";
   s += "Representative task :" + representativeTask() + ":\n";

   return s;
  }
    
}
