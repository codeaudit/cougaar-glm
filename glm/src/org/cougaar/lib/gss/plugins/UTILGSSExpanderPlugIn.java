/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss.plugins;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;

import org.cougaar.lib.callback.UTILExpandableTaskCallback;
import org.cougaar.lib.callback.UTILExpansionCallback;
import org.cougaar.lib.callback.UTILExpansionListener;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;

import org.cougaar.lib.gss.GSTaskGroup;

import org.cougaar.lib.util.UTILExpand;
import org.cougaar.lib.util.UTILPlugInException;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.lib.filter.UTILExpanderPlugIn;

/**
 * The UTILGSSExpanderPlugInAdapter is a handy base class for TOPS 
 * Expanders.  getSubtasks should be redefined in subclasses.
 *
 * Has hooks for handling various replanning conditions.
 * 
 * Subclasses must define
 * <UL>
 * <LI>getSubtasks()
 * </UL>
 * @see org.cougaar.lib.plugin.plugins.UTILSimpleExpanderPlugIn
 */
public abstract class UTILGSSExpanderPlugIn
  extends UTILGSSBufferingPlugInAdapter implements UTILExpanderPlugIn {

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
    //  public boolean interestingTask(Task t) { return true; }

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
        
    return new UTILExpansionCallback (this); 
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
   * Report to superior that the expansion has changed.  Includes a pass
   * through to the UTILPlugInAdapter's updateAllocationResult.
   * Updates and publishes allocation result of expansion.
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
   * Also must remove the GSTaskGroup from the GSS SchedulerResult
   * storage
   *
   * @param exp Expansion that has succeeded.
   */
  public void handleSuccessfulExpansion(Expansion exp, List successfulSubtasks) { 
      if (myExtraOutput)
	  System.out.println (getName () + 
			      " : got successful expansion for task " + exp.getTask ().getUID());
      Enumeration enum = exp.getWorkflow().getTasks();
      Vector tasks = new Vector();
      while(enum.hasMoreElements()) tasks.add((Task)enum.nextElement());
      removeFrozenTasks(exp.getReportedResult(), tasks);
  }

  /**
   * Handle a failed expansion
   * Also must remove the GSTaskGroup from the GSS SchedulerResult
   * storage
   *
   * @param exp Expansion that has succeeded.
   */
  public void handleFailedExpansion(Expansion exp, List failedSubtasks) { 
      if (myExtraOutput)
	  System.out.println (getName () + 
			      " : got failed expansion for task " + exp.getTask ().getUID());
      Enumeration enum = exp.getWorkflow().getTasks();
      Vector tasks = new Vector();
      while(enum.hasMoreElements()) tasks.add((Task)enum.nextElement());
      removeFrozenTasks(exp.getReportedResult(), tasks);
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
   * if no asset could be found to handle the task, handle them in some way -
   * Tasks that did not get expanded become failed expansions.
   *
   * debugging may come on automatically
   */
  public void handleImpossibleTasks (List unallocatedTasks) {
    super.handleImpossibleTasks (unallocatedTasks);

    for (Iterator iter = unallocatedTasks.iterator ();
	 iter.hasNext (); ) {
      GSTaskGroup tg = (GSTaskGroup) iter.next ();
      for (Iterator tgIter = tg.getTasks ().iterator (); tgIter.hasNext ();) {
	Task unallocatedTask = (Task) tgIter.next ();
	if (myExtraOutput) 
	  System.out.println (getName () + " : Making failed expansion for task " + 
			      unallocatedTask);
	publishAdd (UTILExpand.makeFailedExpansion (this, ldmf, unallocatedTask)); 
      }
    }
  }

  /** 
   * Aggregator returns true since there is a gap in between aggregation and 
   * allocation when the task group isn't taken into account anywhere by the
   * schedule unless we leave it (frozen) in the scheduler lists.
   * 
   * if we need to wait before removing the task group from the scheduler for
   * some publish results, return true.  NOTE that we must then manually remove
   * the group later or it will just take up unnecessary space forever.  i.e. NOTE
   * that "freeze" implies "don't remove".
   */
  public boolean freezeTaskGroupForPublish(Asset asset, GSTaskGroup group) {
    return true;
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


    org.cougaar.lib.util.UTILExpand.handleTask(ldmf, 
						    getDelegate(), 
						    getName(),
						    wantConfidence, 
						    myExtraOutput,
						    t, 
						    getSubtasks(t));
  }

  /** if the asset is ready to be disposed, dispose of it */
  public void makePlanElement (Asset anAsset, GSTaskGroup group) {
    for (Iterator iter = group.getTasks ().iterator (); iter.hasNext (); ) {
      Task t = (Task) iter.next ();

      handleTask(t);

    }
  }


  protected UTILExpandableTaskCallback myInputTaskCallback;
}
