/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.SubTaskResult;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Workflow;

import org.cougaar.lib.callback.UTILExpandableTaskCallback;
import org.cougaar.lib.callback.UTILExpansionCallback;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;

import org.cougaar.lib.util.UTILExpand;
import org.cougaar.lib.util.UTILPlugInException;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * The UTILExpanderPlugInAdapter is a handy base class for TOPS 
 * Expanders.  getSubtasks should be redefined in subclasses.
 *
 * Has hooks for handling various replanning conditions.
 *
 * defines handleFailedExpansion with the default behavior of reporting a
 * changed expansion.
 *
 * @see org.cougaar.lib.plugin.plugins.UTILSimpleExpanderPlugIn
 */
public class UTILExpanderPlugInAdapter extends UTILBufferingPlugInAdapter 
  implements UTILExpanderPlugIn {

  /****************************************************************
   ** Setup Filters...
   **/

  /**
   * Provide the callback that is paired with the buffering thread, which is a
   * listener.  The buffering thread is the listener to the callback
   *
   * @return an ExpandableTaskCallback with the buffering thread as its listener
   * @see org.cougaar.lib.callback.UTILWorkflowCallback
   */
  protected UTILFilterCallback createThreadCallback (UTILGenericListener bufferingThread) { 
    if (myExtraOutput)
      System.out.println (getName () + " : Filtering for Expandable Tasks...");

    myInputTaskCallback = new UTILExpandableTaskCallback (bufferingThread);  
    myInputTaskCallback.setExtraDebug (myExtraExtraOutput);
    return myInputTaskCallback;
  } 

  /**
   * Filter out/listen for expandable tasks
   */
  protected UTILFilterCallback getInputTaskCallback    () { 
    return myInputTaskCallback; 
  }

  /** 
   * Implemented for UTILBufferingPlugIn interface
   *
   * filter for tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   * @see UTILBufferingPlugIn
   */
  public boolean interestingTask(Task t) { return true; }

  /** 
   * Implemented for UTILBufferingPlugin
   *
   * Got an ill-formed task, now handle it, by
   * publishing a failed expansion for the task.
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    reportIllFormedTask(t);
    publishAdd (UTILExpand.makeFailedExpansion (null, ldmf, t));
  }

  /**
   * create the expansion callback
   */
  protected UTILFilterCallback createExpansionCallback () { 
    if (myExtraOutput)
      System.out.println (getName () + " : Filtering for Expansions...");
        
    UTILFilterCallback cb = new UTILExpansionCallback (this); 
    cb.setExtraDebug      (myExtraOutput);
    cb.setExtraExtraDebug (myExtraExtraOutput);

    return cb;
  }

  /**
   * Implemented for UTILExpansionListener
   *
   * Gives plugin a way to filter out which expanded tasks it's
   * interested in.
   *
   * @param t Task that has been expanded (getTask of Expansion)
   * @return true if task is interesting to this plugin
   */
  public boolean interestingExpandedTask (Task t) { return interestingTask(t); }

  /**
   * Implemented for UTILExpansionListener
   *
   * An expansion has failed.  It's up to the plugin how to deal with the
   * failure.
   *
   * Just report it to superior, where hopefully it will be dealt with.
   *
   * @param expansion that failed
   */
  public void handleFailedExpansion(Expansion exp, List failedSubTaskResults) {
    reportChangedExpansion (exp);

    if (failedSubTaskResults.size () == 0)
      System.err.println(getName () + " - empty list of failed subtasks?"); 
      
    // Go through the list of failed subtasks
    Iterator failed_it = failedSubTaskResults.iterator();
    while (failed_it.hasNext()) {
      // Get the next failed task
      SubTaskResult str = (SubTaskResult)failed_it.next();
      Task failed_e_task = str.getTask();

      System.err.println("\tFailed task : " + failed_e_task + 
			     "\n\twith pe " + failed_e_task.getPlanElement ());
      System.err.println("\nPref-Aspect comparison : ");
      UTILExpand.showPlanElement (failed_e_task);
    }
  }


  /**
   * At least one constraint has been violated.  It's up to the plugin how to deal 
   * with the violation(s).
   *
   * Ideally, this will not happen very often, and when it does, we should hear about it.
   *
   * @param expansion that failed
   * @param list of Constraints that have been violated
   */
  public void handleConstraintViolation(Expansion exp, List violatedConstraints) {
    throw new UTILPlugInException (getName (), 
				   "handleConstraintViolation : expansion " + exp +
				   " has violated constraints that were ignored.");
  }

  /**
   * Implemented for UTILExpansionListener
   *
   * Does the plugin want to change the expansion?
   *
   * For instance, although no individual preference may have been exceeded,
   * the total score for the expansion may exceed some threshold, and so the
   * plugin may want to alter the expansion.
   *
   * Defaults to FALSE.
   *
   * @see org.cougaar.lib.util.UTILAllocate#scoreAgainstPreferences
   * @param expansion to check
   * @return true if plugin wants to change expansion
   */
  public boolean wantToChangeExpansion(Expansion exp) {
    return false;
  }


  /**
   * The plugin changes the expansion.
   *
   * Default does nothing.
   *
   * @see wantToChangeExpansion
   * @param expansion to change
   */
  public void changeExpansion(Expansion exp) {}

  /**
   * publish the change
   *
   * @see wantToChangeExpansion
   * @param expansion to change
   */
  public void publishChangedExpansion(Expansion exp) {
    publishChange (exp);
  }

  /**
   * Report to superior that the expansion has changed. Usually just a pass
   * through to the UTILPlugInAdapter's updateAllocationResult.
   *
   * @param exp Expansion that has changed.
   * @see UTILPlugInAdapter#updateAllocationResult
   */
  public void reportChangedExpansion(Expansion exp) { 
    if (myExtraExtraOutput)
      System.out.println (getName () + 
			  " : reporting changed expansion to superior.");
      
    updateAllocationResult (exp); 
  }

  /**
   * Handle a successful expansion
   *
   * @param exp Expansion that has succeeded.
   */
  public void handleSuccessfulExpansion(Expansion exp, List successfulSubtasks) { 
    if (myExtraExtraOutput) {
      AllocationResult estAR = exp.getEstimatedResult();
      AllocationResult repAR = exp.getReportedResult();
      String est = "e null/";
      String rep = " r null";
      if (estAR != null)
	est = " e " + (estAR.isSuccess () ? "S" : "F") +  " - " +
	  (int) (estAR.getConfidenceRating ()*100.0) + "% /";
      if (repAR != null)
	rep = " r " + (repAR.isSuccess () ? "S" : "F") + " - " +
	  (int) (repAR.getConfidenceRating ()*100.0) + "%";

      System.out.println (getName () + 
			  " : got successful expansion for task " + exp.getTask ().getUID() + 
			  est + rep);
    }
  }

  /**
   * The idea is to add subscriptions (via the filterCallback), and when 
   * they change, to have the callback react to the change, and tell 
   * the listener (many times the plugin) what to do.
   *
   * Override and call super to add new filters, or override 
   * createXXXCallback to change callback behaviour.
   */
  public void setupFilters () {
    super.setupFilters ();
              
    addFilter (createExpansionCallback());
  }

  /** 
   * Implemented for UTILBufferingPlugIn interface
   *
   * @param tasks that have been buffered up to this point
   * @see UTILBufferingPlugIn
   */
  public void processTasks (List tasks) {
    for (int i = 0; i < tasks.size (); i++)
      handleTask ((Task) tasks.get (i));
  }

  /** 
   * Implemented for UTILGenericListener interface
   *
   * This method Expands the given Task and publishes the PlanElement.
   * The method expandTask should be implemented by child classes.
   * @param t the task to be expanded.
   */
  public void handleTask(Task t) {
    boolean wantConfidence = false;
    
    try { wantConfidence = getMyParams().getBooleanParam ("SimpleExpanderWantConfidence"); }
    catch (Exception e) {}

    UTILExpand.handleTask(ldmf, 
			 getDelegate(), 
			 getName(),
			 wantConfidence, 
			 myExtraOutput,
			 t, 
			 getSubtasks(t));
  }

  /**
   * Implemented for UTILExpanderPlugIn interface
   *
   * The guts of the expansion.
   *
   * Default does nothing!  Subclass should override.
   */
  public Vector getSubtasks(Task t) { 
    System.out.println (getName () + 
			" : WARNING - getSubtasks should be overriden." +
			" Default does nothing.");
    return new Vector (); 
  }

  protected UTILExpandableTaskCallback myInputTaskCallback;
}
