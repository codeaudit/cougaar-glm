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

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.Disposition;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

/**
 * <pre>
 * Filters for tasks with workflows, where the tasks
 * meet the test of isInteresting.
 *
 * The reaction to a new workflow is simpler than
 * UTILSingleTaskWorkflowCallback.  This is better
 * for threaded allocators, where we can't make assumptions
 * about how the tasks will be handled, like we can with
 * a one-at-a-time model.
 * </pre>
 */

public class UTILWorkflowCallback extends UTILBufferingCallback implements UTILRehydrateReactor {
  public UTILWorkflowCallback (UTILGenericListener listener, Logger logger) {
    super (listener, logger);
  }

  /**
   * <pre>
   * Filters for tasks that have workflows.
   * They are then tested
   * against the plugin-specific interestingTask test.
   *
   * set logger.isDebugEnabled() to true if you want to see logger.info on every 
   * handled task.  That is, which tasks have been 
   * allocated and which failed to allocate.
   * (Previously handled tasks will not be given to the listener.)
   *
   * </pre>
   * @return anonymous UnaryPredicate inner class
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
	public boolean execute(Object o) {
	  if (o instanceof Task) {
	    Task subtask = (Task) o;

	    if (subtask.getWorkflow() == null) {
	      return false;
	    }

	    boolean hasBeenAllocated =
	      (subtask.getPlanElement () != null);

	    if (logger.isDebugEnabled()) 
	      debugInfo (subtask, hasBeenAllocated);

	    UTILGenericListener genericListener = 
	      (UTILGenericListener) myListener;

	    boolean interesting = 
	      (!hasBeenAllocated && 
	       genericListener.interestingTask (subtask));

	    if (logger.isDebugEnabled())
	      logger.debug ("UTILWorkflowCallback : For " + myListener + 
			    " found task " + subtask.getUID() + " interesting");
	  
		
	    return interesting;
	  }
	  return false;
	}
      };
  }

  protected void debugInfo (Task subtask, boolean hasBeenAllocated) {
    if (hasBeenAllocated) {
      PlanElement pe = subtask.getPlanElement ();
      if (pe instanceof Allocation)
	logger.debug ("UTILWorkflowCallback - Task " + subtask.getUID () + " has allocation.");
      else if (pe instanceof Disposition)
	logger.debug ("UTILWorkflowCallback - Task " + subtask + " has failed allocation.");
      else
	logger.debug ("UTILWorkflowCallback - Task " + subtask.getUID () + " has " + pe);
    }
    else {
      logger.debug ("UTILWorkflowCallback - Task " + subtask.getUID () + " has NOT been allocated.");
    }
  }

  /** place where you can react to rehydration event */
  public void reactToRehydrate () {
    Collection contents = mySub.getCollection ();
	
    if (logger.isInfoEnabled())
      logger.info ("UTILWorkflowCallback.reactToRehydrate - Notifying " + myListener + 
		   " about " + contents.size () + " previously buffered tasks.");

    // Only want to call wakeUp if some tasks still match
    boolean workToBeDone = false;
    for (Iterator iter = contents.iterator (); iter.hasNext ();) {
      Task t = (Task) iter.next();
	  
      if (isWellFormed (t)) {
	workToBeDone = true; // Will need to have plugin wake up later
	((UTILGenericListener) myListener).handleTask (t);
	if (logger.isDebugEnabled())
	  logger.debug ("UTILWorkflowCallback.reactToRehydrate - Notifying " + myListener + 
			" about " + t.getUID());
      }
    }

    if (workToBeDone) {
      if (logger.isDebugEnabled())
	logger.debug("UTILWorkflwCallback.react asking " + myListener + " to wakeUp");
      ((UTILGenericListener) myListener).wakeUp();
    }
  }  

  /**
   * Examines an incoming task to see if it is well formed.
   * Looks at timing information, and asks listener to examine
   * task as well.  If task is ill formed, asks listener to handle
   * it (probably publish as a failed plan element).
   */
  protected boolean isWellFormed (Task task) {
    UTILGenericListener genericListener = 
      (UTILGenericListener) myListener;
    if (verify.isTaskTimingCorrect(task) && 
	verify.hasRequiredFields  (task) &&
	genericListener.isTaskWellFormed(task)) 
      return true;
    else
      genericListener.handleIllFormedTask (task);

    return false;
  }
}
