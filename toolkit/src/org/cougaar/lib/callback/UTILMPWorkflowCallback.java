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

package org.cougaar.lib.callback;

import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

/**
 * Filters for MP tasks, where the tasks
 * meet the test of isInteresting.
 *
 */

public class UTILMPWorkflowCallback extends UTILWorkflowCallback {
  public UTILMPWorkflowCallback (UTILGenericListener listener, Logger logger) {
    super (listener, logger);
  }

  /**
   * Filters for mp tasks
   *
   * They are tested
   * against the plugin-specific interestingTask test.
   *
   * set logger.isDebugEnabled() to true if you want to see logger.info on every 
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

	  if (logger.isDebugEnabled()) 
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
