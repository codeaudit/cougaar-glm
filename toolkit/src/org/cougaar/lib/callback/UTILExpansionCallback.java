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

import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewExpansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.SubTaskResult;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;

import org.cougaar.util.UnaryPredicate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cougaar.util.log.Logger;

/**
 * <pre>
 * Filters for expansions, testing if the parent task
 * is interesting.  Calls the listener's handleExpansion 
 * method (which gives the expansion to the listener).
 *
 * Intended use is for expanders that should listen to
 * their products : expansions.
 *
 * Allocators should use one of the WorkflowCallbacks.
 *
 * </pre>
 */

public class UTILExpansionCallback extends UTILFilterCallbackAdapter {
  public UTILExpansionCallback (UTILExpansionListener listener, Logger logger) {
    super (listener, logger);
  }

  /** 
   * Looks for expansions with interesting parent tasks
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
	public boolean execute(Object o) {
	  if (o instanceof Expansion) {
	    Expansion exp = (Expansion) o;
	    if (((UTILExpansionListener) 
		 myListener).interestingExpandedTask (exp.getTask ())){
	      return true;
	    }
	  }
	  return false;
	}
      };
  }

  /**
   * Expanders are generally only interested in expansions
   * that have changed as a result of their reported alloc.
   * results changing.
   *
   * New and removed expansions can be safely ignored.
   */
  public void reactToChangedFilter () {
    //    Map seenExpsMap = new HashMap ();

    Enumeration changedExps = mySub.getChangedList();

    while (changedExps.hasMoreElements()) {
      Expansion exp = (Expansion)changedExps.nextElement();

      //      if (seenExpsMap.get (exp) == null) {
      reactToChangedExpansion (exp);
      //  	seenExpsMap.put (exp, exp);
      //      } else if (logger.isDebugEnabled()) 
      //  	logger.info ("UTILExpansionCallback : " + 
      //  			    "Duplicate changed exp for task " + 
      //  			    exp.getTask ().getUID () + " ignored.");
    }

    if (logger.isDebugEnabled()) {
      Enumeration removedExps = mySub.getRemovedList();
      if (removedExps.hasMoreElements ()) {
	Expansion exp = (Expansion)removedExps.nextElement ();
	logger.debug (myListener.getClass() + " saw removed expansion for task " + 
		      exp.getTask ().getUID ());
      }
    }
  }

  /**
   * <pre>
   * Defines protocol for dealing with Expansions as they change over time.
   *
   * This follows the expansion lifecycle.  When an expansion fails, we get an initial
   * shot at fixing it.  If we can replace all the failed subtasks, then the expansion can
   * be called "fixed."  If it has been fixed, don't report it as failed (we can't change
   * the reported alloc result, even if we replace all the tasks in the workflow).
   * If there are still some subtasks in the workflow that we could
   * not replace, then the expansion is a failure and should be reported as such.
   *
   * Uses getSubTaskResults, which erases it's logger.info after being called. (Another call
   * will give an empty list.)  It reports on subtask alloc results that have changed.
   *
   * This breaks down into 6 steps (= each if statement below):
   *
   * 1) Looks for failed subtasks on the expansion, and if finds any, passes them
   * to listener.  handleFailedExpansion gets a chance at removing the failed subtasks
   * from the expansion's workflow and replacing them with tasks with different prefs.
   * By default, if the expander can't fix this expansion, it should just report the 
   * failure.  If the expander can only fix some of the tasks and not all, it should
   * report as failure.
   *
   * 2) Then if no subtask failed, but there was a constraint violation, the 
   * listener is told of which constraints were violated.
   *
   * 3) If no subtasks failed, and no constraint was violated, the listener still
   * has the option of changing the expansion, and if it wants to, will get passed
   * the expansion.
   *
   * 4) If some subtasks could not be fixed, we may want to report the failed expansion 
   * The problem is that we may then report the failed expansion twice, since we report
   * it once if it fails and we can't do anything about it in handleFailedExpansion, and 
   * then again after the report in handleFailedExpansion.  (The Expansion has changed
   * with the report->estimated flip (I think)).  The second time, no subtask results
   * are marked as changed.
   * 
   * 5) If it's a success, report it.
   *
   * 6) If it's reported as a failure, but has no subtasks that are failures, the 
   * expansion has been handled previously, and we can ignore it.
   *
   * So for now (01/11/00), we don't automaticaly report this.  We may want to revisit 
   * later.
   *
   * BOZO - 01/11/00 GWFV
   *
   * </pre>
   * @param Expansion to examine
   */
  public void reactToChangedExpansion (Expansion exp) {
    UTILExpansionListener listener = (UTILExpansionListener) myListener;

    if (exp.getReportedResult () == null)
      return;

    if (exp.getReportedResult().isSuccess ()) {
      if (exp.getWorkflow ().constraintViolation ()) {
	if (logger.isInfoEnabled())
	  logger.info(listener.getClass() +
		      " Expansion " + exp.getUID() + " violated constraints.");
	List violatedList = 
	  enumToList (exp.getWorkflow ().getViolatedConstraints ());
	listener.handleConstraintViolation (exp, violatedList);
      } else if (listener.wantToChangeExpansion (exp)) {
	if (logger.isInfoEnabled())
	  logger.info(listener.getClass() +
		      " Expansion " + exp.getUID() + 
		      " successful, but wants to be changed.");
	// NOTE that if the listener wants to change the expansion,
	// it is responsible for calling or not calling 
	// handleSuccessfulExpansion
	listener.changeExpansion (exp);
	listener.publishChangedExpansion (exp);
      }
      else {
	if (logger.isDebugEnabled())
	  logger.debug(listener.getClass() +
		       " Expansion " + exp.getUID() + " was successful.");
	listener.reportChangedExpansion (exp);

	List subtaskResults = ((NewExpansion) exp).getSubTaskResults();

	/** only use the following if getSubTaskResults is expensive */
	/*
	  List subtaskResults;
	  if (listener.wantsSuccessfulExpResults ())
	  subtaskResults = ((NewExpansion) exp).getSubTaskResults();
	  else
	  subtaskResults = new ArrayList ();
	*/

	listener.handleSuccessfulExpansion(exp, subtaskResults);
      }
    }
    else {
      // Why cache the results?  Because getSubTaskResults is evil -- calling
      // it clears its contents.  Something one would not normally expect
      // from something called getXXX.  If it said getAndClearXXX you might
      // have a chance to catch it before it becomes a nasty bug.

      List subtaskResults = ((NewExpansion) exp).getSubTaskResults();
      if (logger.isDebugEnabled())
	logger.debug (listener.getClass() + " - reportChangedExpansion " +
		      exp.getUID ());

      List failedSubTaskResults = getFailedSubTaskResults (subtaskResults);

      if (!failedSubTaskResults.isEmpty ()) {
	if (logger.isInfoEnabled())
	  logger.info(listener.getClass() +
		      " Expansion " + exp.getUID() + 
		      " task " + exp.getTask ().getUID() + 
		      " failed.");
	listener.handleFailedExpansion(exp, failedSubTaskResults);
      } else if (exp.getWorkflow ().constraintViolation ()) {
	if (logger.isInfoEnabled())
	  logger.info(listener.getClass() +
		      " Expansion " + exp.getUID() + " violated constraints.");
	List violatedList = 
	  enumToList (exp.getWorkflow ().getViolatedConstraints ());
	listener.handleConstraintViolation (exp, violatedList);
      } else if (getNumFailedSubTasks(subtaskResults) > 0 ) {
	if (logger.isInfoEnabled())
	  logger.info(listener.getClass() +
		      " Expansion " + exp.getUID() + 
		      " task " + exp.getTask ().getUID() + 
		      " had failed subtasks, " + 
		      "but all have been seen before (NO REPORT!)");
	// BOZO - GWFV 01/11/00 - We may have to revisit this.
	// Some cases where this may not be what we want.
      
	// GWFV 01/23/00 The story continues -- without the line below, we would
	// ignore failed expansions, as they sometimes had no changed subTaskResults,
	// despite the fact that we had never seen them before.
	// So for the moment, the report is back in.
      
	listener.reportChangedExpansion (exp);
      } else {
	if (logger.isDebugEnabled())
	  logger.debug(listener.getClass() +
		       " Expansion " + exp.getUID() + 
		       " task " + exp.getTask ().getUID() + 
		       " - failed expansion, but has been handled already.");
      }
    }
  }

  /**
   * @param exp Expansion to examine for failed sub tasks
   * @return List of SubTaskResult objects, all of which have newly failed.
   */
  protected List getFailedSubTaskResults (List subtaskResults) {
    List result = new ArrayList ();
    int n = 0;
    for (Iterator iter = subtaskResults.iterator(); iter.hasNext (); ) {
      SubTaskResult stres = (SubTaskResult) iter.next ();
      boolean didFail = false;
      if (stres.getAllocationResult () != null)
	didFail = !stres.getAllocationResult().isSuccess ();
      else if (logger.isInfoEnabled ())
	logger.info ("getFailedSubtasks - null AR for subtask " + 
		     stres.getTask ());
	
      if (didFail)
	n++;
      if (didFail && stres.hasChanged ())
	result.add (stres);
    }

    if (logger.isInfoEnabled() && (n > 0))
      logger.info (this + " : getFailedSubtasks - " + n + 
		   " failed subtasks, " + result.size () + 
		   " changed.");

    return result;
  }

  /**
   * If an expansion is altered after it is reported as failed,
   * the reported alloc result will remain marked as failed, even
   * after the failed subtasks have been removed.
   *
   * @param exp Expansion to examine for failed sub tasks
   * @return number of failed sub tasks
   */
  protected int getNumFailedSubTasks (List subtaskResults) {
    int n = 0;
    for (Iterator iter = subtaskResults.iterator();
	 iter.hasNext (); ) {
      SubTaskResult stres = (SubTaskResult) iter.next ();
      if (!stres.getAllocationResult().isSuccess ())
	n++;
    }

    return n;
  }

  protected List enumToList (Enumeration enum) {
    List result = new ArrayList ();
    while (enum.hasMoreElements ())
      result.add (enum.nextElement());
    return result;
  }
}
