/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.filter.UTILTaskChecker;

/**
 * Listener for use with Workflow and ExpandableTask Callbacks.
 */

public interface UTILGenericListener extends UTILFilterCallbackListener, UTILTaskChecker {
  /** 
   * Defines tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  boolean interestingTask(Task t);

  /** 
   * Got an interesting task, now handle it in some way
   * @param t Task to handle
   */
  void    handleTask     (Task t);
}

