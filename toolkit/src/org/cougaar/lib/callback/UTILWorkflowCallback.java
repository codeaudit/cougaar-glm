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

import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.Disposition;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Workflow;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.util.UTILPrepPhrase;
import org.cougaar.lib.util.UTILVerify;

import java.util.Collection;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Filters for tasks with workflows, where the tasks
 * meet the test of isInteresting.
 *
 * The reaction to a new workflow is simpler than
 * UTILSingleTaskWorkflowCallback.  This is better
 * for threaded allocators, where we can't make assumptions
 * about how the tasks will be handled, like we can with
 * a one-at-a-time model.
 */

public class UTILWorkflowCallback extends UTILFilterCallbackAdapter implements UTILRehydrateReactor {
  public UTILWorkflowCallback (UTILGenericListener listener) {
    super (listener);
  }

  /**
   * Filters for tasks that have workflows.
   * They are then tested
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
	if (o instanceof Task) {
	  Task subtask = (Task) o;

	  if (subtask.getWorkflow() == null) {
	    // System.out.println ("---> ignoring task w/ no wf = " + subtask.getUID());
	    return false;
	  }

	  boolean hasBeenAllocated =
	    (subtask.getPlanElement () != null);

	  if (xxdebug) 
	    debugInfo (subtask, hasBeenAllocated);

	  UTILGenericListener genericListener = 
	    (UTILGenericListener) myListener;

	  boolean interesting = 
		(!hasBeenAllocated && 
		 genericListener.interestingTask (subtask));

	  if (xxdebug)
		System.out.println ("UTILWorkflowCallback : For " + myListener + 
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
	System.out.println ("UTILWorkflowCallback - Task " + subtask.getUID () + " has allocation.");
      else if (pe instanceof Disposition)
	System.out.println ("UTILWorkflowCallback - Task " + subtask + " has failed allocation.");
      else
	System.out.println ("UTILWorkflowCallback - Task " + subtask.getUID () + " has " + pe);
    }
    else {
      System.out.println ("UTILWorkflowCallback - Task " + subtask.getUID () + " has NOT been allocated.");
    }
  }

  /**
   * Tells listener of new tasks that are ready to allocate.
   *
   * set xxdebug to true if you want to see what gets
   * ignored by the callback. (Removed tasks are ignored.)  
   *
   * Again, this can make you feel better, 
   * since the interestingTask test
   * is made on these ignored expansions too.  
   *
   * (Since sometimes calls to interestingTask are
   * followed by the listener doing something, but in
   * these cases, nothing happens despite the 
   * listener/plugin being "interested" in the task.)
   *
   * Will not give the listener duplicates of the same
   * task.
   */
  public void reactToChangedFilter () {
    Enumeration newTasks = mySub.getAddedList();
    UTILGenericListener genericListener = 
      (UTILGenericListener) myListener;
    boolean anythingChanged = newTasks.hasMoreElements();
    int i = 0;

	if (xxdebug)
	  System.out.println ("UTILWorkflowCallback : For " + myListener + 
						  mySub.getAddedCollection ().size() + " added tasks " +
						  mySub.getChangedCollection ().size() + " changed tasks " +
						  mySub.getRemovedCollection ().size() + " removed tasks ");

    Collection removedCollection = mySub.getRemovedCollection ();
	
    Map seenTasksMap = new HashMap ();
    while (newTasks.hasMoreElements()) {
      Task newT = (Task)newTasks.nextElement();
	  if (removedCollection.contains (newT))
		continue;
	  
      if ((seenTasksMap.get (newT) == null) &&
	  isWellFormed (newT)) {
	seenTasksMap.put (newT, newT);
	if (xxdebug)
	  System.out.println ("UTILWorkflowCallback : For " + myListener + " calling with task " + newT);
	
	genericListener.handleTask(newT);
	i++;
      }
    }

    Enumeration changedTasks = mySub.getChangedList();
    if (!anythingChanged)
      anythingChanged = changedTasks.hasMoreElements();
    
    while (changedTasks.hasMoreElements()) {
      Task changedT = (Task)changedTasks.nextElement();
	  if (removedCollection.contains (changedT))
		continue;
      if ((seenTasksMap.get (changedT) == null) &&
	  isWellFormed (changedT)) {
	seenTasksMap.put (changedT, changedT);
	genericListener.handleTask(changedT);
	i++;
      }
    }

    if (anythingChanged)
      synchronized (myListener) {
	if (xxdebug)
	  System.out.println ("UTILWorkflowCallback : Notifying " + myListener + 
			      " about " + i + 
			      " tasks");
	myListener.notify ();
      }

	if (mySub.getRemovedList().hasMoreElements ()) {
	  Enumeration removedtasks = mySub.getRemovedList();
	  while (removedtasks.hasMoreElements()) {
		Task t = (Task) removedtasks.nextElement();
		if (xxdebug)
		  System.out.println ("UTILWorkflowTaskCallback : Telling listener that task " + t.getUID() + 
							  " was removed.");
		((UTILGenericListener) myListener).handleRemovedTask(t);
	  }
	}

    if (xxdebug) {
      Enumeration removedTasks = mySub.getRemovedList();
      if (removedTasks.hasMoreElements ()) {
	Task wfTask = (Task) removedTasks.nextElement ();
	String unit = "Undefined"; //(UTILPrepPhrase.hasPrepNamed(wfTask, Constants.Preposition.FOR)) ? 
	// ("" + UTILPrepPhrase.getPrepNamed(wfTask, Constants.Preposition.FOR)) : "nonUnit";
	System.err.println(myListener.getClass() + " saw removed allocatable task " + 
			   wfTask.getUID () + " from "  + unit);
      }
    }
  }

  /** place where you can react to rehydration event */
  public void reactToRehydrate () {
    Collection contents = mySub.getCollection ();
	
	if (xdebug || xxdebug || true) 
	  System.out.println ("UTILWorkflowCallback.reactToRehydrate - Notifying " + myListener + 
						  " about " + contents.size () + " previously buffered tasks.");

    for (Iterator iter = contents.iterator (); iter.hasNext ();) {
      Task t = (Task) iter.next();
	  
      if (isWellFormed (t)) {
		((UTILGenericListener) myListener).handleTask (t);
		if (xxdebug || true) 
		  System.out.println ("UTILWorkflowCallback.reactToRehydrate - Notifying " + myListener + 
							  " about " + t.getUID());
      }
    }
	synchronized (myListener) {  myListener.notify ();	}
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
    if (UTILVerify.isTaskTimingCorrect(task) && 
	UTILVerify.hasRequiredFields  (task) &&
	genericListener.isTaskWellFormed(task)) 
      return true;
    else
      genericListener.handleIllFormedTask (task);

    return false;
  }
}
