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

package org.cougaar.lib.gss.plugins;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.callback.UTILAllocationCallback;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.callback.UTILWorkflowCallback;

import org.cougaar.lib.gss.GSTaskGroup;

import org.cougaar.lib.util.UTILAllocate;

import java.util.Iterator;
import java.util.List;

import org.cougaar.lib.filter.UTILAllocatorPlugIn;

public class UTILGSSAllocatorPlugIn 
  extends UTILGSSBufferingPlugInAdapter implements UTILAllocatorPlugIn {

  /** Reads delayBeforeAllocRemoval from environment files. */
  public void getEnvData () {
    super.getEnvData ();

    try{delayBeforeAllocRemoval = myParams.getLongParam("delayBeforeAllocRemoval");}
    catch(Exception e){delayBeforeAllocRemoval = 20000;}
  }

  /**
   * The idea is to add subscriptions (via the filterCallback), and when 
   * they change, to have the callback react to the change, and tell 
   * the listener (many times the plugin) what to do.
   *
   * Override and call super to add new filters, or override 
   * createXXXCallback to change callback behaviour.
   *
   * By default adds allocation callback after creating it.
   *
   * @see #createAllocCallback
   */
  public void setupFilters () {
    super.setupFilters ();

    if (myExtraOutput)
      System.out.println (getName () + " : Filtering for Allocations...");

    addFilter (myAllocCallback    = createAllocCallback    ());
  }

  /**
   * Provide the callback that is paired with the buffering thread, which is a
   * listener.  The buffering thread is the listener to the callback
   *
   * @return a WorkflowCallback with the buffering thread as its listener
   */
  protected UTILFilterCallback createThreadCallback (UTILGenericListener bufferingThread) { 
    if (myExtraOutput)
      System.out.println (getName () + " Filtering for tasks with Workflows...");

    myWorkflowCallback = new UTILWorkflowCallback  (bufferingThread); 
    return myWorkflowCallback;
  } 

  protected UTILFilterCallback getWorkflowCallback () {
    return myWorkflowCallback;
  }


  protected UTILAllocationCallback getAllocCallback    () { return myAllocCallback; }
  /**
   * Override to replace with a callback that has a different predicate
   * or different behaviour when triggered.
   */
  protected UTILAllocationCallback createAllocCallback () { 
    return new UTILAllocationCallback  (this); 
  } 

  /** 
   * Implemented for UTILBufferingPlugin
   *
   * Got an ill-formed task, now handle it, by
   * publishing a failed plan allocation for the task.
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    reportIllFormedTask(t);
    publishAdd (UTILAllocate.makeFailedDisposition (null, ldmf, t));
  }

  /**
   * Implemented for UTILAllocationListener
   *
   * WARNING: The filters from the XML file will
   * not be considered here unless you make a 
   * call to the GSScheduler somehow. (i.e.
   * via interestingTask or similar method)
   * 
   * OVERRIDE to see which task notifications you
   * think are interesting
   * @param t task to check for notification
   * @return boolean true if task is interesting
   */
  public boolean interestingNotification(Task t) { 
    return interestingTask(t);
  }

  /**
   * Implemented for UTILAllocationListener
   *
   * Defines conditions for rescinding tasks.
   *
   * Only return true if the plugin can do something different 
   * with the task that failed.  See UTILAllocate.isFailedPE.
   *
   * When returns TRUE, handleRescindedAlloc is called. 
   *
   * Returns TRUE when downstream, a FailedAllocation is made.
   *
   *
   * If in making an allocation, a preference
   * threshold is exceeded, the returned plan element will be
   * a FailedAllocation (see UTILAllocate.makeAllocation ()).
   *
   * TOPS does not create any allocations with 
   * AllocationResults w/ isSuccess = false, but COUGAAR will roll
   * up the results of a workflow, and create an AllocResult
   * w/ isSuccess = False if it contains a FailedAllocation.
   *
   * Called by UTILAllocationCallback.reactToChangedAlloc.
   *
   * @param alloc the allocation to check
   * @return boolean true if the allocation need to be rescinded
   *         Also returns false if there is no report alloc result
   *         attached to allocation
   * @see #handleRescindedAlloc
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   * @see org.cougaar.lib.util.UTILAllocate#makeAllocation
   * @see org.cougaar.lib.util.UTILAllocate#isFailedPE
   */
  public boolean needToRescind(Allocation alloc){
    return false;
  }

  /**
   * Implemented for UTILAllocationListener
   *
   * Public version of publishRemove
   *
   * Called by UTILAllocationCallback.reactToChangedAlloc.
   *
   * @param alloc Allocation to remove from cluster's memory
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   */
  public void publishRemovalOfAllocation (Allocation alloc) { 
    publishRemove (alloc); 
  }

  /**
   * Implemented for UTILAllocationListener
   *
   * Defines re-allocation of a rescinded task.  
   * Overriders need to take into consideration that 
   * the asset chosen last time is not available this time.
   *
   * Note that updateAllocationResult is called automatically by
   * the UTILAllocationCallback if the allocation has changed 
   * (typically if its allocation result has changed) 
   * but it does NOT need to be rescinded.
   *
   * Called by UTILAllocationCallback.reactToChangedAlloc.
   *
   * Only called when needToRescind returns TRUE.
   * See comment on needToRescind.
   *
   * Does nothing by default.
   *
   * @param alloc the allocation that should be rescinded
   * @see UTILPlugInAdapter#updateAllocationResult
   * @see UTILAllocationListener#updateAllocationResult
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   * @see #needToRescind
   */
  public boolean handleRescindedAlloc (Allocation alloc) {
    /*
    if (myExtraOutput)
      System.out.println(getName () + 
			 " : handling rescinded allocation for task " +
			 alloc.getTask ().getUID ());
    try {
      if (myExtraOutput)
	System.out.println (getName () + " : waiting " +
			    delayBeforeAllocRemoval/1000 + 
			    " seconds after removing allocation for task " +
			    alloc.getTask ().getUID ());

      Thread.sleep (delayBeforeAllocRemoval);
    } catch (Exception e) {}

    if (myExtraOutput)
      System.out.println (getName () + " : removing allocation for task " +
			  alloc.getTask ().getUID ());
    */
    return false;
  }

  /**
   * Implemented for UTILAllocationListener
   *
   * Called automatically by the UTILAllocationCallback 
   * if the allocation has changed but it does NOT need 
   * to be rescinded. 
   * updateAllocationResult is called first and then this method 
   * gets called.
   *
   * Called by UTILAllocationCallback.reactToChangedAlloc.
   *
   * Only called when needToRescind returns FALSE.
   * See comment on needToRescind.
   *
   * Does nothing by default.
   *
   * @param alloc the allocation that was successful
   * @see UTILPlugInAdapter#updateAllocationResult
   * @see UTILAllocationListener#updateAllocationResult
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   * @see #needToRescind
   */
  public void handleSuccessfulAlloc (Allocation alloc) {
    /*
      if (myExtraExtraOutput)
      System.out.println(getName () + 
			 " : handling successful allocation for task " +
			 alloc.getTask ().getUID () + 
			 " by doing nothing.");
    */
  }

  /** 
   * Called when an allocation is removed from the cluster.
   * I.e. an upstream cluster removed an allocation, and this 
   * rescind has resulted in this allocation being removed.
   *
   * If the plugin maintains some local state of the availability
   * of assets, it should update them here.
   *
   * Does nothing by default.
   */
  public void handleRemovedAlloc (Allocation alloc) {}

  /** if the asset is ready to be disposed, dispose of it */
  public void makePlanElement (Asset anAsset, GSTaskGroup group) {
    for (Iterator iter = group.getTasks ().iterator (); iter.hasNext (); ) {
      Task t = (Task) iter.next ();

      if (myExtraOutput)
	UTILAllocate.setDebug (true);

      PlanElement allocation = UTILAllocate.makeAllocation(this,
							   ldmf, realityPlan, t, anAsset,
							   group.getCurrentStart(), 
							   group.getCurrentEnd(), 
							   UTILAllocate.HIGHEST_CONFIDENCE,
							   Role.getRole("Transporter"));
      if (t.getAuxiliaryQueryTypes()[0] != -1) {
    	int[] aqr = t.getAuxiliaryQueryTypes();
	for (int q= 0; q < aqr.length; q++) {
	  int checktype = aqr[q];
	  if (checktype == AuxiliaryQueryType.UNIT_SOURCED ) {
	    String data = anAsset.getItemIdentificationPG().getItemIdentification();
	    UTILAllocate.addQueryResultToAR(allocation, AuxiliaryQueryType.UNIT_SOURCED, data);
	  }
	}
      }

      if (myExtraOutput)
	UTILAllocate.setDebug (false);

      publishAdd(allocation);

      /*
      if (myExtraOutput) {
	if (allocation instanceof FailedAllocation)
	  System.out.println(getName () + " : Making failed allocation for task " +t.getUID());
	else
	  System.out.println(getName () + " : Making allocation for task " +t.getUID());
      }
      if (myExtraExtraOutput)
	System.out.println("\tfrom " + group.getCurrentStart() + 
			   " to " + group.getCurrentEnd());
      */
    }
  }

  /**
   * if no asset could be found to handle the task, handle them in some way -
   * Tasks that did not get allocated become failed allocations.
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
	//	if (myExtraOutput) 
	  System.out.println (getName () + ".handleImpossibleTask : Making failed allocation for task " + 
			      unallocatedTask + "\n despite having " + 
			      getAssetCallback().getSubscription ().getCollection().size () + 
			      " assets.");
	publishAdd (UTILAllocate.makeFailedDisposition (this, ldmf, unallocatedTask)); 
      }
    }
  }

  /** 
   * Allocator returns false, since the task group will be reflected in the 
   * RoleSchedule after allocation.
   * (!!FIXIT!! is there a point between the creation of the allocation and the
   * update of the roleschedule during which the scheduler could get in and schedule
   * another task?  assumed this could only happen during aggregation...)
   * 
   * if we need to wait before removing the task group from the scheduler for
   * some publish results, return true.  NOTE that we must then manually remove
   * the group later or it will just take up unnecessary space forever
   */
  public boolean freezeTaskGroupForPublish(Asset asset, GSTaskGroup group) {
    return false;
  }

  protected UTILWorkflowCallback   myWorkflowCallback;
  protected UTILAllocationCallback myAllocCallback;

  protected long delayBeforeAllocRemoval = 20000; //millis
}
