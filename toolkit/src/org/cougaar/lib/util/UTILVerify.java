/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.lib.util;

import java.util.Date;

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.log.Logger;

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
   * @param durInMillis duration to use (in millis)
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
