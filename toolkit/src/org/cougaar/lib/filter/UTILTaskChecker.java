/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.lib.filter;

import org.cougaar.planning.ldm.plan.Task;

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
