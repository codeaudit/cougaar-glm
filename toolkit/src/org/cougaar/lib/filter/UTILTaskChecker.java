/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.domain.planning.ldm.plan.Task;

/**
 * Interface for listeners that check task consistency.
 * 
 */

public interface UTILTaskChecker {
  /** 
   * Examines task to see if task looks like what the plugin 
   * expects it to look like.
   *
   * This is plugin-dependent.  For example, the
   * planning factor for unloading a ship is 2 days, but
   * if the task's time window is 1 day, the task is
   * not "well formed."  Duration is a common test, but others
   * could also be a good idea...
   *
   * This is an explicit contract with the plugin
   * that feeds this plugins tasks, governing which tasks
   * are possible for this plugin to handle.
   *
   * @param t Task to check for consistency
   * @return true if task is OK
   */
  boolean isTaskWellFormed    (Task t);

  /** 
   * Got an ill-formed task, now handle it, probably by
   * publishing a failed plan element for the task.
   * @param t badly-formed task to handle
   */
  void    handleIllFormedTask (Task t);
}
