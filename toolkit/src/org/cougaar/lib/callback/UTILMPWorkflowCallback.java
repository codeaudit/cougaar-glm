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

import org.cougaar.domain.planning.ldm.plan.MPTask;

import org.cougaar.util.UnaryPredicate;

/**
 * Filters for MP tasks, where the tasks
 * meet the test of isInteresting.
 *
 */

public class UTILMPWorkflowCallback extends UTILWorkflowCallback {
  public UTILMPWorkflowCallback (UTILGenericListener listener) {
    super (listener);
  }

  /**
   * Filters for mp tasks
   *
   * They are tested
   * against the plugin-specific interestingTask test.
   *
   * set xxdebug to true if you want to see info on every 
   * handled task.  That is, which tasks have been 
   * allocated and which failed to allocate.
   * (Previously handled tasks will not be given to the listener.)
   *
   * @return anonymous UnaryPredicate inner class
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof MPTask) {
	  MPTask subtask = (MPTask) o;

	  boolean hasBeenAllocated =
	    (subtask.getPlanElement () != null);

	  if (xxdebug) 
	    debugInfo (subtask, hasBeenAllocated);

	  if (!hasBeenAllocated &&
	      ((UTILGenericListener) myListener).interestingTask (subtask))
	    return true;
	}
	return false;
      }
    };
  }
}
