/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.util;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectScorePoint;
import org.cougaar.domain.planning.ldm.plan.AspectScoreRange;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.Disposition;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.filter.UTILPlugIn;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

/** 
 * This class contains utility functions for allocations.
 */

public class UTILAllocate {
  private static final boolean SUCCESS = true;
  private static final boolean RESCINDMEPLEASE = false;
  public static final double MEDIUM_CONFIDENCE  = 0.5d;
  public static final double HIGHEST_CONFIDENCE = 1.0d;
  private static boolean debug = false;
  private static double ONE_DAY = 1000*60*60*24; // millis
  private static double ONE_OVER_ONE_DAY = 1.0d/ONE_DAY; // millis

  /**
   * Set to true to see debug output -- tie to myExtraExtraOutput?
   * @param dbg -- debug switch
   */
  public static void setDebug (boolean dbg) { debug = dbg; }

  /**
   * Creates an Allocation or FailedAllocation 
   * depending on whether start and end dates or cost
   * exceed preference thresholds.
   *
   * @param ldmf the RootFactory
   * @param plan the log plan
   * @param t the task to allocate
   * @param asset the asset assigned to handle the task
   * @param start - start date of allocation
   * @param end   - end   date of allocation
   * @param cost  - the monetary cost of the allocation
   * @param confidence in the allocation
   * @return PlanElement = Allocation or a FailedDisposition
   */
  public static PlanElement makeAllocation (UTILPlugIn creator,
					    RootFactory ldmf,
					    Plan plan,
					    Task t,
					    Asset asset,
					    Date start,
					    Date end,
					    double cost,
					    double confidence,
					    Role assignedRole) {
    return makeAllocation (creator,
			   ldmf, plan, t, asset, 
			   getAspects (start, end, cost),
			   confidence,
			   assignedRole);
  }
					   

  /**
   * Creates an Allocation or FailedDisposition 
   * with an allocationResult w/ isSuccess=False or True, 
   * depending on whether any of the start and end dates 
   * exceed preference thresholds.
   *
   * Automates the checking of an allocation against its preferences
   *
   * PlugIns should never have to create AllocationResults directly.
   * If you want to find the score of a task-allocation result pair 
   * before making the REAL allocation, you can call scoreAgainstPreferences
   * (GSS).
   *
   * Note that returned Allocations will ALWAYS have
   * an AllocationResult with isSuccess = TRUE.
   *
   * FailedDispositions will ALWAYS have isSuccess = FALSE.
   *
   * @param ldmf the RootFactory
   * @param plan the log plan
   * @param t the task to allocate
   * @param asset the asset assigned to handle the task
   * @param start - start date of allocation
   * @param end   - end   date of allocation
   * @param confidence in the allocation
   * @return PlanElement = Allocation or a FailedDisposition
   */
  public static PlanElement makeAllocation (UTILPlugIn creator,
					    RootFactory ldmf,
					    Plan plan,
					    Task t,
					    Asset asset,
					    Date start,
					    Date end,
					    double confidence,
					    Role assignedRole) {
    return makeAllocation (creator,
			   ldmf, plan, t, asset, 
			   getAspectsFromDates (start, end),
			   confidence,
			   assignedRole);
  }
					   

  /**
   * Creates an Allocation or FailedDisposition 
   * with an allocationResult w/ isSuccess=False or True, 
   * depending on whether any of the aspect values of the 
   * allocation exceed any preference thresholds.
   *
   * This automates the checking of an allocation against its preferences.
   *
   * PlugIns should never have to create AllocationResults directly.
   * If you want to find the score of a task-allocation result pair 
   * before making the REAL allocation, you can call scoreAgainstPreferences
   * (GSS).
   *
   * Note that returned Allocations will ALWAYS have
   * an AllocationResult with isSuccess = TRUE.
   *
   * FailedDispositions will ALWAYS have isSuccess = FALSE.
   *
   * @param ldmf the RootFactory
   * @param plan the log plan
   * @param t the task to allocate
   * @param asset the asset assigned to handle the task
   * @param estAR the allocationResult for the allocation
   * @param AspectValue array -- aspect represented as aspectValues
   * @param confidence in the allocation
   * @return PlanElement = Allocation or a FailedDisposition
   */
  public static PlanElement makeAllocation (UTILPlugIn creator,
					    RootFactory ldmf,
					    Plan plan,
					    Task t,
					    Asset asset,
					    AspectValue [] aspects,
					    double confidence,
					    Role assignedRole) {
    int[] aspectarray = new int[aspects.length];
    double[] resultsarray = new double[aspects.length];
    for (int i = 0; i < aspects.length; i++) {
      aspectarray [i] = aspects[i].getAspectType ();
      resultsarray[i] = aspects[i].getValue ();
    }

    return makeAllocation (creator,
			   ldmf, plan, t, asset, 
			   aspectarray, resultsarray, confidence, assignedRole);
  }					  



  /**
   * Creates an Allocation or FailedDisposition with an 
   * allocationResult w/ isSuccess=False or True, 
   * depending on whether any of the aspect values of the 
   * allocation exceed any preference thresholds.
   *
   * Also will automatically create a FailedDisposition if the asset
   * passed in is null.
   *
   * This automates the checking of an allocation against its preferences.
   *
   * PlugIns should never have to create AllocationResults directly.
   * If you want to find the score of a task-allocation result pair 
   * before making the REAL allocation, you can call scoreAgainstPreferences
   * (GSS).
   *
   * Note that returned Allocations will ALWAYS have
   * an AllocationResult with isSuccess = TRUE.
   *
   * FailedDispositions will ALWAYS have isSuccess = FALSE.
   *
   * @param ldmf the RootFactory
   * @param plan the log plan
   * @param t the task to allocate
   * @param asset the asset assigned to handle the task 
   *        (Ignored if failed allocation.)
   * @param estAR the allocationResult for the allocation
   * @param aspectarray  - array of aspect types 
   * @param resultsarray - array of aspect values
   * @param confidence in the allocation
   * @return PlanElement = Allocation or a FailedDisposition
   */
  public static PlanElement makeAllocation(UTILPlugIn creator,
					   RootFactory ldmf,
					   Plan plan,
					   Task t,
					   Asset asset,
					   int[] aspectarray,
					   double[] resultsarray,
					   double confidence,
					   Role assignedRole) {
    AllocationResult estAR = null;
    PlanElement alloc = null;

    if ((asset == null) || 
	exceedsPreferences (t, aspectarray, resultsarray)) {
      estAR = createAllocationResult (RESCINDMEPLEASE, 
				      ldmf, 
				      aspectarray, resultsarray, 
				      confidence);

      alloc = makeFailedDisposition (creator, ldmf, t, estAR);

      if (debug)
	System.out.println ("\nMaking FailedDisposition : Task " + t +
			    "\n\tAllocResult " + ARtoString (aspectarray,
							     resultsarray));
    }
    else {
      estAR = createAllocationResult (SUCCESS, 
				      ldmf, 
				      aspectarray, resultsarray, 
				      confidence);
      
      alloc = ldmf.createAllocation (plan,
				     t,
				     asset, 
				     estAR,
				     assignedRole);
      if (debug) {
	System.out.println ("\nMaking Allocation : Task " + t +
			    "\n\tAsset " + asset + 
			    "\n\tAllocResult " + ARtoString (aspectarray,
							     resultsarray));
	try {
	  System.out.println ("\tScore " + scoreAgainstPreferences (t, estAR));
	} catch (NullPointerException npe) {
	  System.out.println ("ALPINE Bug : can't take the score of an aspect immediately?");
	}
      }
    }
    return alloc;
  }

  /**
   * Adds data to an auxiliaryQuery. 
   *
   * @param pe the planelement of the task that issued the auxiliary query
   * @param aqType the AuxiliaryQueryType
   * @param data the data that answers the query
   */

  public static void addQueryResultToAR(PlanElement pe, int aqType, String data){
    AllocationResult ar = pe.getEstimatedResult();
    ar.addAuxiliaryQueryInfo(aqType, data);
  }

  /**
   * Handy utility function for checking a task's preferences against start and
   * end dates and cost.
   *
   * @param t task to check
   * @param start date to check against preferences
   * @param end date to check against preferences
   * @param cost - the monetary cost of a potential allocation
   * @return true if either the start or end dates or cost lie 
   *         outside the task's preferences.
   */
  public static boolean exceedsPreferences (Task t, Date start, Date end, 
					    double cost) {
    if (debug)
      System.out.println ("Checking for task " + t.getUID () + 
			  " against start " + start + 
			  " end " + end + 
			  " and cost " + cost);
    return exceedsPreferences (t, getAspects (start, end, cost));
  }

  /**
   * Handy utility function for checking a task's preferences against start and
   * end dates.
   *
   * @param t task to check
   * @param start date to check against preferences
   * @param end date to check against preferences
   * @return true if either the start or end dates lie 
   *         outside the task's preferences.
   */
  public static boolean exceedsPreferences (Task t, Date start, Date end) {
    if (debug)
      System.out.println ("Checking for task " + t.getUID () + 
			  " against start " + start + 
			  " end " + end);
    return exceedsPreferences (t, getAspectsFromDates (start, end));
  }

  /**
   * Check a task's preferences against given aspect values.
   *
   * @param t task to check
   * @param aspectValues array of AspectValues
   * @return boolean -- true if exceeds any preference
   * @see org.cougaar.domain.planning.ldm.plan.AspectType
   * @see org.cougaar.domain.planning.ldm.plan.AspectValue
   */
  public static boolean exceedsPreferences (Task t, AspectValue [] aspectValues) {
    int[] aspectarray = new int[aspectValues.length];
    double[] resultsarray = new double[aspectValues.length];
    for (int i = 0; i < aspectValues.length; i++) {
      aspectarray  [i] = aspectValues[i].getAspectType ();
      resultsarray [i] = aspectValues[i].getValue ();
    }
    return exceedsPreferences (t, aspectarray, resultsarray);
  }

  /**
   * Check a task's preferences against given aspect values.
   *
   * Throws an informative exception if somehow the 
   * the aspect types being checked are not the same set as the task's 
   * preferences.  E.g. if the task has only a START_TIME preference
   * and one of the allocation aspects is COST.  
   * This should never happen -- one
   * should only assign aspects against a task's preferences.
   *
   * If debug is set, warns to stdout if set of preferences types doesn't match set
   * of aspect values.
   *
   * @param t task to check
   * @param aspectTypes array of aspect types
   * @param aspectValues array of aspect values
   * @return boolean -- true if any preference is exceeded
   * @see org.cougaar.domain.planning.ldm.plan.AspectType
   */
  public static boolean exceedsPreferences (Task t,
					    int [] aspectTypes,
					    double [] aspectValues) {
    boolean prefExceeded = false;
    Enumeration prefs = t.getPreferences ();
    Map map = new HashMap ();
    int aspectType = -1;

    for (; prefs.hasMoreElements (); ) {
      Preference pref = (Preference) prefs.nextElement ();
      map.put (new Integer (pref.getAspectType ()), pref);
    }

    try {
      // check each aspect value against its preference threshold
      for (int i = 0; i < aspectTypes.length && !prefExceeded; i++) {
	aspectType = aspectTypes[i];
	Integer aspectTypeInt = new Integer (aspectType);
	Preference pref = (Preference) map.remove (aspectTypeInt);

	AspectValue av = new AspectValue (aspectType, aspectValues[i]);
	double score = pref.getScoringFunction().getScore (av);

	if (score > (ScoringFunction.HIGH_THRESHOLD - 0.000001d)) {
	  prefExceeded = true;
	  if (debug) {
	    System.out.println ("UTILAllocate.exceedsPreferences - score " + score + 
							" exceeds threshold " + ScoringFunction.HIGH_THRESHOLD + 
							" for " + t.getUID ());
		AspectValue lower = new AspectValue (aspectType, 0.0d);
		AspectValue upper = new AspectValue (aspectType, 1000000000.0d);
	    print (av, pref, score, pref.getScoringFunction().getDefinedRange (),
			   pref.getScoringFunction().getValidRanges (lower, upper));
	  }
	}
      }
    } catch (NullPointerException npe) {
      // thrown when the hash table returns a null preference
      // for an aspect type
      throw new UTILRuntimeException ("exceedsPreferences : For task " + t.getUID () + 
				      " making alloc w/ aspect <" + aspectType +
				      "> - but task doesn't have this preference!");
    }
    
    // !map.isEmpty() isn't a problem if we've exceeded preferences already,
    // since by design the rest of the map doesn't get processed after one
    // preference has been exceeded.

    if (!prefExceeded && !map.isEmpty ()) {
      for (Iterator i = map.keySet ().iterator (); i.hasNext (); ) {
	  // Report when there are preferences without matching aspect values.
	  // It's a responsibility of the allocator to fill in aspect values for all prefs.

	  System.out.println ("UTILAllocate.exceedsPreferences - Task " + t + 
			      " from " + t.getSource () + 
			      " has preference of aspect type " + (Integer) i.next () + 
			      " but no value of this type found in list of reported values." + 
			      "\nPerhaps missing an aspect value when calling makeAllocation?");
	  UTILExpand.showPlanElement(t);
      }
    }
    return prefExceeded;
  }

  /**
   * This should be used when the preference thresholds are exceeded, or 
   * the cumulative
   * score for all preferences exceeds some threshold.
   *
   * This form lets the tasked cluster say how the preferences were exceeded.
   *
   * @param ldmf RootFactory for making the plan elements
   * @param t Task that failed to be allocated
   * @param failedAR AllocationResult stating how the preferences would have been
   *        exceeded.
   * @return FailedDisposition 
   * @see #makeFailedDisposition (RootFactory, Task)
   */
  public static Disposition makeFailedDisposition(UTILPlugIn creator,
							 RootFactory ldmf, Task t,
							 AllocationResult failedAR) {
    Disposition falloc    = 
      ldmf.createFailedDisposition(ldmf.getRealityPlan(), t, failedAR);

    if (creator != null)
      creator.showDebugIfFailure ();

    return falloc;
  }

  /**
   * When you just can't allocate!
   *
   * This should be used when the preference thresholds are exceeded, or 
   * the cumulative
   * score for all preferences exceeds some threshold.
   *
   * Generally the other makeFailedDisposition should be used -- 
   * the tasking/superior cluster should be told not just that the allocation failed,
   * but what aspect values made it fail.  This function does not let you specify
   * the aspect results.
   *
   * @param ldmf RootFactory for making the plan elements
   * @param t Task that failed to be allocated
   * @return FailedDisposition 
   * @see #makeFailedDisposition (RootFactory, Task, AllocationResult)
   */
  public static Disposition makeFailedDisposition(UTILPlugIn creator,
						      RootFactory ldmf, Task t) {
    AllocationResult failedAR  = 
      ldmf.newAllocationResult(HIGHEST_CONFIDENCE, RESCINDMEPLEASE, 
			       new int[1], new double[1]);
    Disposition falloc    = 
      ldmf.createFailedDisposition(ldmf.getRealityPlan(), t, failedAR);

    if (creator != null)
      creator.showDebugIfFailure ();

    return falloc;
  }

  /**
   * Freeform allocation result creation.
   *
   * Protected because plugin should not need to call this.
   *
   * Workaround for ALP bug (MB5.2) where asking for aspect values
   * will throw null pointer exception.
   *
   * @param isSuccess sets whether allocation obeyed preferences or not
   * @param ldmf RootFactory for making the plan elements
   * @param aspectarray  - array of aspect type IDs (ints from AspectType)
   * @param resultsarray - results for those aspects, in same order as aspect types
   * @param confidence of allocation
   * @return allocation result with aspect results
   */
  protected static AllocationResult createAllocationResult(boolean isSuccess,
							   RootFactory ldmf,
							   int [] aspectarray,
							   double [] resultsarray,
							   double confidence) {
    AllocationResult myestimate = null;

    //    myestimate = ldmf.newAllocationResult(confidence, 
    //					  isSuccess, aspectarray, resultsarray);
    myestimate = ldmf.newAVAllocationResult(confidence, 
					    isSuccess, 
					    getAspectsFromArrays (aspectarray, resultsarray));
    return myestimate;
  }

  public static AllocationResult cloneAllocResultAsSuccess(RootFactory ldmf,
							   AllocationResult toClone) {
    return ldmf.newAVAllocationResult(toClone.getConfidenceRating (), 
				      true,
				      toClone.getAspectValueResults ());

  }
  /**
   * Utility function for creating AspectValues from start and end date.
   *
   * @param start date
   * @param end date
   * @return AspectValue array representing dates.
   */
  public static AspectValue [] getAspectsFromDates (Date start, Date end) {
    AspectValue [] aspects = new AspectValue [2];
    aspects[0] = new AspectValue (AspectType.START_TIME, (double) start.getTime ());
    aspects[1] = new AspectValue (AspectType.END_TIME,   (double) end.getTime ());
    return aspects;
  }

  /**
   * Utility function for creating AspectValues from start, end date, and cost.
   *
   * @param start date
   * @param end date
   * @param cost
   * @return AspectValue array representing dates and cost.
   */
  public static AspectValue [] getAspects (Date start, Date end, double cost) {
    AspectValue [] aspects = new AspectValue [3];
    aspects[0] = new AspectValue (AspectType.START_TIME, (double) start.getTime ());
    aspects[1] = new AspectValue (AspectType.END_TIME,   (double) end.getTime ());
    aspects[2] = new AspectValue (AspectType.COST,       cost);
    return aspects;
  }

  /**
   * Utility function for creating AspectValues from start, end date, cost and quantity.
   *
   * @param start date
   * @param end date
   * @param cost
   * @param quantity 
   * @return AspectValue array representing dates, cost, and quantity.
   */
  public static AspectValue [] getAspects (Date start, Date end, double cost, long quantity) {
    AspectValue[] aspects = new AspectValue[4];
    aspects[0] = new AspectValue (AspectType.START_TIME, (double) start.getTime ());
    aspects[1] = new AspectValue (AspectType.END_TIME,   (double) end.getTime ());
    aspects[2] = new AspectValue (AspectType.COST,       cost);
    aspects[3] = new AspectValue (AspectType.QUANTITY,   quantity);
    return aspects;
  }

  /**
   * Utility function for creating AspectValues from type and value arrays
   *
   * @param aspect type array
   * @param aspect value array
   * @return AspectValue array
   */
  public static AspectValue [] getAspectsFromArrays (int [] aspectarray,
						     double [] resultsarray) {
    AspectValue [] aspects = new AspectValue [aspectarray.length];

    for (int i = 0; i < aspectarray.length; i++)
      aspects [i] = new AspectValue (aspectarray[i], resultsarray[i]);

    return aspects;
  }

  // Scoring -------------------------------------------------------------

  /**
   * Score of a task's preferences against an allocation result
   *
   * @return the total score of the allocation result 
   *         against the task's preferences
   */
  protected static double scoreAgainstPreferences (Task t,
						   AllocationResult allocResult) {
    return scoreAgainstPreferences (t, allocResult.getAspectValueResults ());
  }

  /**
   * Score of a task's preferences against start and
   * end dates, and cost
   * 
   * @param t task to check
   * @param start date to score against preferences
   * @param end date to score against preferences
   * @param cost - the monetary cost of a potential allocation
   * @return double score 
   */
  public static double scoreAgainstPreferences (Task t, Date start, Date end,
						double cost) {
    if (debug)
      System.out.println ("Scoring task " + t.getUID () + 
			  " against start " + start + 
			  " end " + end + 
			  " and cost " + cost);
    return scoreAgainstPreferences (t, getAspects (start, end, cost));
  }

  /**
   * Score of a task's preferences against start and
   * end dates.
   * 
   * Hoefully this form would be called most often...
   *
   * @param t task to check
   * @param start date to score against preferences
   * @param end date to score against preferences
   * @return double score 
   */
  public static double scoreAgainstPreferences (Task t, Date start, Date end) {
    if (debug)
      System.out.println ("Scoring task " + t.getUID () + 
			  " against start " + start + 
			  " end " + end);
    return scoreAgainstPreferences (t, getAspectsFromDates (start, end));
  }

  /**
   * Score a task's preferences against given aspect values.
   *
   * @param t task to check
   * @param aspectValues array of AspectValues
   * @return double -- the score
   * @see org.cougaar.domain.planning.ldm.plan.AspectType
   * @see org.cougaar.domain.planning.ldm.plan.AspectValue
   */
  public static double scoreAgainstPreferences (Task t, AspectValue [] aspectValues) {
    int[] aspectarray = new int[aspectValues.length];
    double[] resultsarray = new double[aspectValues.length];
    for (int i = 0; i < aspectValues.length; i++) {
      aspectarray  [i] = aspectValues[i].getAspectType ();
      resultsarray [i] = aspectValues[i].getValue ();
    }
    return scoreAgainstPreferences (t, aspectarray, resultsarray);
  }

  /**
   * Score a task's preferences against given aspect values.
   *
   * Throws an informative exception if somehow the 
   * the aspect types being checked are not the same set as the task's 
   * preferences.  E.g. if the task has only a START_TIME preference
   * and one of the allocation aspects is COST, then this method
   * will throw an exception.  This should never happen -- one
   * should only assign aspects against a task's preferences.
   *
   * Protects against ALP bug : sometimes preference weight
   * gets lost... (ends up = 0).
   *
   * @param t task to check
   * @param aspectTypes array of aspect types
   * @param aspectValues array of aspect values
   * @return double -- score of these aspect values against the task's preferences
   * @see org.cougaar.domain.planning.ldm.plan.AspectType
   * @see org.cougaar.domain.planning.ldm.plan.AspectValue
   * @see org.cougaar.domain.planning.ldm.plan.Preference
   */
  public static double scoreAgainstPreferences (Task t,
						int [] aspectTypes,
						double [] aspectValues) {
    
    int aspectType = -1;
    Map hash = new HashMap ();
    double total = 0.0d;

    for (Enumeration prefs = t.getPreferences (); prefs.hasMoreElements (); ) {
      Preference pref = (Preference) prefs.nextElement ();
      hash.put (new Integer (pref.getAspectType ()), pref);
    }

    try {
      for (int i = 0; i < aspectTypes.length; i++) {
	aspectType = aspectTypes[i];
	Preference pref = (Preference) hash.get (new Integer (aspectType));
	AspectValue av = new AspectValue (aspectType, aspectValues[i]);
	double score = pref.getScoringFunction().getScore (av);
	double weight = (pref.getWeight () == 0) ? 1 : pref.getWeight ();
	total += score*weight;
      }
    } catch (NullPointerException npe) {
      // thrown when the hash table returns a null preference
      // for an aspect type
      throw new UTILRuntimeException ("scoreAgainstPreferences : For task " + t.getUID () + 
				      " scoring against aspect <" + aspectType +
				      "> - but task doesn't have this aspect preference!");
    }
    return total;
  }

  /**
   * Checks plan element's allocation result to see if it's a failed
   * plan element.
   *
   * @param alloc the allocation to check
   * @return boolean true if the allocation need to be rescinded
   *         Also returns false if there is no report alloc result
   *         attached to allocation
   * @see #handleRescindedAlloc
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   * @see org.cougaar.lib.util.UTILAllocate#makeAllocation
   */
  public boolean isFailedPE (PlanElement pe){
    AllocationResult reportedResult = pe.getReportedResult ();
    if (reportedResult == null) {
      // It's ok for an allocation to be in the changed list multiple times, 
      // so this is a legal occurrence.
      return false;
    }

    boolean need = !reportedResult.isSuccess ();
    if (debug && need) {
      System.out.println ("UTILAllocate.isFailedPE - found failed task " +
			  pe.getTask ().getUID () + "-" +  pe.getTask().getVerb());
    }
    return need;
  }

  /**
    * Is there a better place for this?
    */
  public static Vector enumToVector (Enumeration e) {
    Vector retval = new Vector ();
 
    if (e == null)
      return retval;

    for (; e.hasMoreElements (); )
      retval.addElement (e.nextElement ());
 
    return retval;
  }

  /**
    * Is there a better place for this?
    */
  public static Vector iterToVector (Iterator i) {
    Vector retval = new Vector ();
 
    if (i == null)
      return retval;

    for (; i.hasNext (); )
      retval.addElement (i.next ());
 
    return retval;
  }

  public static List enumToList (Enumeration e) {
    List retval = new ArrayList ();
 
    if (e == null)
      return retval;

    for (; e.hasMoreElements (); )
      retval.add (e.nextElement ());
 
    return retval;
  }

  protected static String ARtoString (int    [] aspectTypes,
				      double [] aspectValues) {
    String retval = "";
    for (int i = 0; i < aspectTypes.length; i++) {
      retval = retval + "\n\t" + 
	printTypeValue (aspectTypes[i], aspectValues[i]);
    }

    return retval;
  }

  protected static String printTypeValue (int type, double value) {
    String retval = "";
    switch (type) {
    case AspectType.START_TIME: 
      retval = "START_TIME";
      retval = retval + "/" + new Date ((long) value);
      break;
    case AspectType.END_TIME: 
      retval = "END_TIME";
      retval = retval + "/" + new Date ((long) value);
      break;
    case AspectType.COST:
      retval = "COST";
      retval = retval + "/$" + (long) value;
      break;
    default:
      retval = "" + type;
      retval = retval + "/" + (long) value;
      break;
    }

    return retval;
  }

  protected static void print (AspectValue av, Preference pref, double score, 
							   AspectScoreRange definedRange, Enumeration validRanges) {
    double prefval = pref.getScoringFunction().getBest ().getValue ();
    String prefstr = "" + prefval;
    String type = "" + av.getAspectType ();
    String value = "" + prefval;
	boolean isDate = false;
    switch (av.getAspectType ()) {
    case AspectType.START_TIME: 
      type = "START_TIME";
      value   = "" + new Date ((long) av.getValue ());
      prefstr = "" + new Date ((long) prefval);
	  isDate = true;
	  break;
    case AspectType.END_TIME: 
      type = "END_TIME";
      value   = "" + new Date ((long) av.getValue ());
      prefstr = "" + new Date ((long) prefval);
	  isDate = true;
	  break;
    case AspectType.COST: 
      type = "COST";
      value   = "$" + (long) av.getValue ();
      prefstr = "$" + (long) prefval;
	  break;
    }

    if (score == ScoringFunction.HIGH_THRESHOLD) {
      System.out.println ("Aspect " + type +
			  "/" + value + " exceeds preference (best = " + 
			  prefstr + ")");
	  if ((av.getAspectType () == AspectType.START_TIME) || 
		  (av.getAspectType () == AspectType.END_TIME)) {
		System.out.println ("\tDifference (pref-aspect) " + (prefval - av.getValue())/60000 +
							" minutes, valid ranges : ");
	  }
	  else
		System.out.println ("\tDifference (pref-aspect) " + (prefval - av.getValue()) +
							", valid ranges : ");

	  for (; validRanges.hasMoreElements (); ) {
		AspectScoreRange range = (AspectScoreRange) validRanges.nextElement();
		AspectScorePoint start = range.getRangeStartPoint ();
		AspectScorePoint end   = range.getRangeEndPoint ();
		double startValue = start.getValue ();
		double endValue   = end.getValue ();

		if (isDate)
		  System.out.print ("<" + new Date ((long) (startValue)) + "-" + new Date ((long) (endValue)) + "> "); 
		else
		  System.out.print ("<" + startValue + "-" + endValue + "> "); 
	  }
	  System.out.println ("");
    }
  }
}
