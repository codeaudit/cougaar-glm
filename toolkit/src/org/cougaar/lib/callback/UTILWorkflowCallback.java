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
