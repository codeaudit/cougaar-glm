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

package org.cougaar.lib.callback;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;

import java.util.Enumeration;
import org.cougaar.util.log.Logger;

/**
 * Filters for expansions with workflows, where the tasks
 * meet the test of isInteresting.
 *
 * The reaction to a new workflow is simpler than
 * UTILSingleTaskWorkflowCallback.  This is better
 * for threaded allocators, where we can't make assumptions
 * about how the tasks will be handled, like we can with
 * a one at a time model.
 */

public class UTILInclusiveWorkflowCallback extends UTILFilterCallbackAdapter {
  public UTILInclusiveWorkflowCallback (UTILGenericListener listener, Logger logger) {
    super (listener, logger);
  }

  /**
   * Filters for expansions and then examines the tasks
   * within the workflow for any tasks that don't have
   * associated plan elements.  These are then tested
   * against the plugin-specific interestingTask test.
   *
   * set logger.isDebugEnabled() to true if you want to see logger.info on every 
   * handled expansion.  I.e. which tasks have been 
   * allocated and which failed to allocate.
   * (Previously handled expansions are ignored by the 
   * predicate.)
   *
   * @return annonymous UnaryPredicate inner class
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Expansion) {
	  UTILGenericListener genericListener = 
	    (UTILGenericListener) myListener;
	  Workflow wf = ((Expansion)o).getWorkflow();
	  Enumeration e = wf.getTasks();
	  int num = 0;

	  while (e.hasMoreElements()){
	    Task subtask = (Task) e.nextElement();	    
	    if (genericListener.interestingTask (subtask)){
	      return true;
	    }
	  }
	}
	return false;
      }
    };
  }

  /**
   * Tells listener of new expansions
   *
   * set logger.isDebugEnabled() to true if you want to see what gets
   * ignored by the callback.  (Changed and removed
   * expansions are ignored.)  
   *
   * Again, this can make you feel better, 
   * since the interestingTask test
   * is made on these ignored expansions too.  
   *
   * (Since sometimes calls to interestingTask are
   * followed by the listener doing something, but in
   * these cases, nothing happens despite the 
   * listener/plugin being "interested" in the task.)
   */
  public void reactToChangedFilter () {
    Enumeration newExps = mySub.getAddedList();
    UTILGenericListener genericListener = 
      (UTILGenericListener) myListener;
    
    while (newExps.hasMoreElements()) {
      Expansion exp = (Expansion)newExps.nextElement();
      Workflow wf = exp.getWorkflow();
      Enumeration e = wf.getTasks();

      while (e.hasMoreElements()){
	Task t = (Task)e.nextElement();

	if (genericListener.interestingTask (t)) {
	  genericListener.handleTask(t);
	}
      }
    }

    if (logger.isDebugEnabled()) {
      if (mySub.getChangedList().hasMoreElements ())
	logger.debug ("UTILWorkflowCallback : " + 
		      "Expansions were changed. (Ignored by callback)");
      if (mySub.getRemovedList().hasMoreElements ())
	logger.debug ("UTILWorkflowCallback : " + 
			    "Expansions were removed. (Ignored by callback)");
    }
    logger.info("Prapare to clean up");
    if (genericListener instanceof UTILTemporaryListener) {
      logger.info("CLEAN UP");
      ((UTILTemporaryListener)genericListener).cleanup();
    }
  }
}
