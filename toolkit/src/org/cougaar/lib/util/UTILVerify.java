/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.lib.util;

import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.util.log.*;

import java.util.Date;

/**
 * Helper classes to see if a task is consistent and 
 * reasonable.
 */
public class UTILVerify{
  public UTILVerify (Logger log) {
    this.logger = log;
    prefHelper = new UTILPreference (logger);
  }

  /**
   * See if the time between the START_TIME preference and END_TIME
   * preference (latest date) is at least as long as the given duration.
   * @param t task to verify
   * @param d duration to use (in millis)
   * @return true if task has duration of at least as long as the 
   *         duration d, false otherwise.
   */
  public boolean exceedsTaskDuration(Task t, long durInMillis){
    Date start_time = prefHelper.getReadyAt(t);
    Date late_time  = prefHelper.getLateDate(t);
    
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
  public boolean isTaskTimingCorrect(Task t){
    Date start_time = prefHelper.getReadyAt(t);
    Date early_time = prefHelper.getEarlyDate(t);
    Date best_time  = prefHelper.getBestDate(t);
    Date late_time  = prefHelper.getLateDate(t);

    if(!late_time.before(best_time) &&
       !best_time.before(early_time) &&
       !(early_time.before(start_time) && 
		 (start_time.getTime() - early_time.getTime() < TWO_YEARS))) {
      return true;
    }
    return false;
  }

  public boolean hasRequiredFields (Task t) {
    return 
      (hasDirectObject (t) && 
       hasStartPreference (t) && 
       hasEndPreference (t));
  }
  
  public boolean hasDirectObject (Task t) {
    return (t.getDirectObject () != null);
  }

  public boolean hasStartPreference (Task t) {
    return (prefHelper.hasPrefWithAspectType (t, AspectType.START_TIME));
  }

  public boolean hasEndPreference (Task t) {
    return (prefHelper.hasPrefWithAspectType (t, AspectType.END_TIME));
  }

  /**
   * @return String explaining what's wrong with task
   */
  public String reportDurationError (Task t, long durInMillis) {
    Date start_time = prefHelper.getReadyAt(t);
    Date late_time  = prefHelper.getLateDate(t);
    
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
  public String reportTimingError (Task t) {
    Date start_time = prefHelper.getReadyAt(t);
    Date early_time = prefHelper.getEarlyDate(t);
    Date best_time  = prefHelper.getBestDate(t);
    Date late_time  = prefHelper.getLateDate(t);

    if (late_time.before(best_time))
      return "Latest arrival (" + late_time + 
	") is before best (" + best_time + ").";
    if (best_time.before(early_time))
      return "Best arrival (" + best_time + 
	") is before earliest (" + early_time + ").";

    return "Earliest arrival (" + early_time + 
      ") is before start (" + start_time + ").";
  }

  protected Logger logger;
  protected UTILPreference prefHelper;
}
