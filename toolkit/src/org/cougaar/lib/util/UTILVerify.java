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

import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Task;

import java.util.Date;

/**
 * Helper classes to see if a task is consistent and 
 * reasonable.
 */
public class UTILVerify{

  /**
   * See if the time between the START_TIME preference and END_TIME
   * preference (latest date) is at least as long as the given duration.
   * @param t task to verify
   * @param d duration to use (in millis)
   * @return true if task has duration of at least as long as the 
   *         duration d, false otherwise.
   */
  public static boolean exceedsTaskDuration(Task t, long durInMillis){
    Date start_time = UTILPreference.getReadyAt(t);
    Date late_time  = UTILPreference.getLateDate(t);
    
    if(late_time.before(start_time)){
      return false;
    }
    long diff = late_time.getTime () - start_time.getTime ();
    return (diff >= durInMillis);
  }

  private static final long TWO_YEARS = 2*365*24*60*60l;

  /**
   * Verify a few things in the task.  The things verified are:
   * END_TIME, verify that the earliest comes before the best and latest,
   * and that the best comes before the latest.
   * START_TIME, verify that the start time comes before the END_TIME
   * earliest time.
   */
  public static boolean isTaskTimingCorrect(Task t){
    Date start_time = UTILPreference.getReadyAt(t);
    Date early_time = UTILPreference.getEarlyDate(t);
    Date best_time  = UTILPreference.getBestDate(t);
    Date late_time  = UTILPreference.getLateDate(t);

    if(!late_time.before(best_time) &&
       !best_time.before(early_time) &&
       !(early_time.before(start_time) && 
		 (start_time.getTime() - early_time.getTime() < TWO_YEARS))) {
      return true;
    }
    return false;
  }

  public static boolean hasRequiredFields (Task t) {
    return 
      (hasDirectObject (t) && 
       hasStartPreference (t) && 
       hasEndPreference (t));
  }
  
  public static boolean hasDirectObject (Task t) {
    return (t.getDirectObject () != null);
  }

  public static boolean hasStartPreference (Task t) {
    return (UTILPreference.hasPrefWithAspectType (t, AspectType.START_TIME));
  }

  public static boolean hasEndPreference (Task t) {
    return (UTILPreference.hasPrefWithAspectType (t, AspectType.END_TIME));
  }

  /**
   * @return String explaining what's wrong with task
   */
  public static String reportDurationError (Task t, long durInMillis) {
    Date start_time = UTILPreference.getReadyAt(t);
    Date late_time  = UTILPreference.getLateDate(t);
    
    if(late_time.before(start_time)){
      return "Late arrival (" + late_time + 
	") is before start (" + start_time + ").";
    }

    long td = late_time.getTime () - start_time.getTime ();
    if (durInMillis == -1)
      return "Task duration, ready at->late arrival (" + 
	td/3600000 + " hours) is too short.";

    return "Task duration, ready at->late arrival (" + td/3600000 + 
      " hours) is shorter than (" + durInMillis/3600000 + 
      " hours).";
  }

  /**
   * @return String explaining what's wrong with task
   */
  public static String reportTimingError (Task t) {
    Date start_time = UTILPreference.getReadyAt(t);
    Date early_time = UTILPreference.getEarlyDate(t);
    Date best_time  = UTILPreference.getBestDate(t);
    Date late_time  = UTILPreference.getLateDate(t);

    if (late_time.before(best_time))
      return "Latest arrival (" + late_time + 
	") is before best (" + best_time + ").";
    if (best_time.before(early_time))
      return "Best arrival (" + best_time + 
	") is before earliest (" + early_time + ").";

    return "Earliest arrival (" + early_time + 
      ") is before start (" + start_time + ").";
  }
}
