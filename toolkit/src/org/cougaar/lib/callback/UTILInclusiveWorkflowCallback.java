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

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Workflow;

import java.util.Enumeration;

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
  public UTILInclusiveWorkflowCallback (UTILGenericListener listener) {
    super (listener);
  }

  /**
   * Filters for expansions and then examines the tasks
   * within the workflow for any tasks that don't have
   * associated plan elements.  These are then tested
   * against the plugin-specific interestingTask test.
   *
   * set xxdebug to true if you want to see info on every 
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
   * set xxdebug to true if you want to see what gets
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

    if (xxdebug) {
      if (mySub.getChangedList().hasMoreElements ())
	System.out.println ("UTILWorkflowCallback : " + 
			    "Expansions were changed. (Ignored by callback)");
      if (mySub.getRemovedList().hasMoreElements ())
	System.out.println ("UTILWorkflowCallback : " + 
			    "Expansions were removed. (Ignored by callback)");
    }
    System.out.println("Prapare to clean up");
    if (genericListener instanceof UTILTemporaryListener) {
      System.out.println("CLEAN UP");
      ((UTILTemporaryListener)genericListener).cleanup();
    }
  }
}
