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

package org.cougaar.lib.filter;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.lib.callback.UTILAllocationCallback;
import org.cougaar.lib.callback.UTILAssetCallback;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.callback.UTILWorkflowCallback;
import org.cougaar.lib.util.UTILAllocate;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.Task;

/**
 * <pre>
 * By default listens for all Assets (please override
 * createAssetCallback to make more specific).  
 * Also listens for allocations.
 * 
 * Fill in empty functions as needed to add functionality
 *
 * Abstract because these are undefined:
 *
 * processTasks -- different for each plugin flavor
 *
 * </pre>
 */

public abstract class UTILAllocatorPluginAdapter 
  extends UTILBufferingPluginAdapter implements UTILAllocatorPlugin{

  /** Reads delayBeforeAllocRemoval from environment files. */
  public void getEnvData () {
    super.getEnvData ();

    alloc = new UTILAllocate(logger);

    //    try{delayBeforeAllocRemoval = myParams.getLongParam("delayBeforeAllocRemoval");}
    //    catch(Exception e){delayBeforeAllocRemoval = 0;}
  }

  /**
   * <pre>
   * The idea is to add subscriptions (via the filterCallback), and when 
   * they change, to have the callback react to the change, and tell 
   * the listener (many times the plugin) what to do.
   *
   * Override and call super to add new filters, or override 
   * createXXXCallback to change callback behaviour.
   *
   * By default adds asset callback and allocation callback after
   * creating them.
   *
   * </pre>
   * @see #createAssetCallback
   * @see #createAllocCallback
   */
  public void setupFilters () {
    super.setupFilters ();

    if (isInfoEnabled())
      info (getName () + " : Filtering for generic Assets...");

    addFilter (myAssetCallback    = createAssetCallback    ());

    if (isInfoEnabled())
      info (getName () + " : Filtering for Allocations...");

    addFilter (myAllocCallback    = createAllocCallback    ());
  }

  /**
   * Provide the callback that is paired with the buffering thread, which is a
   * listener.  The buffering thread is the listener to the callback
   *
   * @return a WorkflowCallback with the buffering thread as its listener
   */
  protected UTILFilterCallback createThreadCallback (UTILGenericListener bufferingThread) { 
    if (isInfoEnabled())
      info (getName () + " Filtering for Generic Workflows...");

    myWorkflowCallback = new UTILWorkflowCallback  (bufferingThread, logger); 
    return myWorkflowCallback;
  } 

  protected UTILFilterCallback getWorkflowCallback () {
    return myWorkflowCallback;
  }

  protected UTILAssetCallback getAssetCallback    () { return myAssetCallback; }

  /**
   * <pre>
   * Override to replace with a callback that has a different predicate
   * or different behaviour when triggered.
   *
   * PLEASE override this -- this is mainly here for backward compatibility.
   * I.e. we shouldn't get all the assets of the cluster and then iterate over
   * them, testing them with instanceof to get the assets we want.  We should
   * use a more specific callback filter/predicate.
   * 
   * </pre>
   */
  protected UTILAssetCallback createAssetCallback () { 
    return new UTILAssetCallback  (this, logger); 
  } 

  protected UTILAllocationCallback getAllocCallback    () { return myAllocCallback; }
  /**
   * Override to replace with a callback that has a different predicate
   * or different behaviour when triggered.
   */
  protected UTILAllocationCallback createAllocCallback () { 
    UTILAllocationCallback allocCallback = new UTILAllocationCallback  (this, logger); 
    return allocCallback;
  } 

  /** 
   * <pre>
   * Implemented for UTILBufferingPlugin
   *
   * Got an ill-formed task, now handle it, by
   * publishing a failed allocation for the task.
   * </pre>
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    reportIllFormedTask(t);
    blackboard.publishAdd (alloc.makeFailedDisposition (null, ldmf, t));
  }

  /**
   * <pre>
   * Implemented for UTILAllocationListener
   *
   * OVERRIDE to see which task notifications you
   * think are interesting
   * </pre>
   * @param t task to check for notification
   * @return boolean true if task is interesting
   */
  public boolean interestingNotification(Task t) { 
    return interestingTask (t);
  }


  /**
   * <pre>
   * Implemented for UTILAssetListener
   *
   * OVERRIDE to see which assets you
   * think are interesting
   * </pre>
   * @param a asset to check for notification
   * @return boolean true if asset is interesting
   */
  public boolean interestingAsset(Asset a) {
    return true;
  }

  /**
   * <pre>
   * Place to handle new assets.
   *
   * Does nothing by default.
   *
   * </pre>
   * @param newAssets new assets found in the container
   */
  public void handleNewAssets(Enumeration newAssets) {}

  /**
   * <pre>
   * Place to handle changed assets.
   *
   * Does nothing by default.
   *
   * </pre>
   * @param newAssets changed assets found in the container
   */
  public void handleChangedAssets(Enumeration changedAssets) {}

  /**
   * <pre>
   * Implemented for UTILAllocationListener
   *
   * Defines conditions for rescinding tasks.
   * 
   * When returns TRUE, handleRescindedAlloc is called. Returns 
   * TRUE when downstream, a FailedAllocation is made.
   *
   * WARNING WARNING WARNING: returning TRUE can easily lead 
   * to an infinite loop consisting of try again-fail-try again, etc.  
   * Subclasses should return FALSE even when the allocation 
   * fails if they can't do anything differently the second time. 
   * Returning FALSE makes the failure get reported to its 
   * superior.
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
   * </pre>
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
   * <pre>
   * Implemented for UTILAllocationListener
   *
   * Public version of publishRemove
   *
   * Called by UTILAllocationCallback.reactToChangedAlloc.
   *
   * FIXIT! -- if you remove the try-catch block, sometimes will see
   * reset claim exceptions.
   *
   * </pre>
   * @param alloc Allocation to remove from cluster's memory
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   */
  public void publishRemovalOfAllocation (Allocation alloc) { 
    if (isInfoEnabled())
      info (getName () + " : removing allocation for task " +
			  alloc.getTask ().getUID ());

    try {
      blackboard.publishRemove (alloc); 
    } catch (Exception e) {
      if (isInfoEnabled())
	info (getName () + " : publishRemovalOfAllocation - got reset claim exception, ignoring...");
    }
  }

  /**
   * <pre>
   * Here is where subclasses can deal with tasks that were marked
   * in a previous cycle as tasks to handle later.
   *
   * Initially only used with rescinding allocations.
   *
   * Default is to do nothing.
   * </pre>
   */
  protected void dealWithDelayedTasks (Vector nextCycleTasks) {
    for (int i = 0; i < nextCycleTasks.size (); i++) {
      Task taskToChange = (Task) nextCycleTasks.elementAt(i);

      replaceTaskInWorkflow (taskToChange);
    }
  }

  /**
   * <pre>
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
   * </pre>
   * @param alloc the allocation that should be rescinded
   * @see UTILPluginAdapter#updateAllocationResult
   * @see UTILAllocationListener#updateAllocationResult
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   * @see #needToRescind
   */
  public boolean handleRescindedAlloc (Allocation alloc) {
    /*
      if (isInfoEnabled())
      info(getName () + 
      " : handling rescinded allocation for task " +
      alloc.getTask ().getUID ());
      if (isInfoEnabled())
      info (getName () + " : waiting " +
      delayBeforeAllocRemoval/1000 + 
      " seconds after removing alloc for task " +
      alloc.getTask ().getUID () + 
      "\n\tnow " + new Date ());
      try {
      Thread.sleep (delayBeforeAllocRemoval);
      } catch (Exception e) {}

      if (isInfoEnabled())
      info ("\t resuming at " + new Date ());
    */

    return false;
  }

  /**
   * <pre>
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
   * </pre>
   * @param alloc the allocation that was successful
   * @see UTILPluginAdapter#updateAllocationResult
   * @see UTILAllocationListener#updateAllocationResult
   * @see org.cougaar.lib.callback.UTILAllocationCallback#reactToChangedAlloc
   * @see #needToRescind
   */
  public void handleSuccessfulAlloc (Allocation alloc) {
    /*
      if (isDebugEnabled())
      info(getName () + 
      " : handling successful allocation for task " +
      alloc.getTask ().getUID () + 
      " by doing nothing.");
    */
  }

  /** 
   * <pre>
   * Called when an allocation is removed from the cluster.
   * I.e. an upstream cluster removed an allocation, and this 
   * rescind has resulted in this allocation being removed.
   *
   * If the plugin maintains some local state of the availability
   * of assets, it should update them here.
   *
   * Does nothing by default.
   * </pre>
   */
  public void handleRemovedAlloc (Allocation alloc) {
    if (isDebugEnabled()) {
      String unit = "Undefined";//(UTILPrepPhrase.hasPrepNamed(alloc.getTask (), Constants.Preposition.FOR)) ? 
      //("" + UTILPrepPhrase.getPrepNamed(alloc.getTask (), Constants.Preposition.FOR)) : "nonUnit";
      debug (getName () + ".handleRemovedAlloc : alloc was removed for task " + 
	     alloc.getTask ().getUID () + " w/ d.o. " +
	     alloc.getTask ().getDirectObject () + " from " + unit);
    }
  }

  /** 
   * <pre>
   * Utility method for finding all resource assets. 
   *
   * In general, it would be better if plugins could use more
   * specific filters and so this call would return a restricted set of
   * assets. 
   *
   * At the very least, 
   * cluster assets can be divided between organizational assets
   * and physical assets. 
   *
   * @return Enumeration of ALL assets found in container
   * </pre>
   */
  protected final Iterator getAssets() {
    Collection assets = 
      getAssetCallback().getSubscription ().getCollection();

    if (assets.size() != 0) {
      return assets.iterator();
    }
    return null;
  }

  protected UTILWorkflowCallback   myWorkflowCallback;
  protected UTILAssetCallback      myAssetCallback;
  protected UTILAllocationCallback myAllocCallback;
  protected UTILAllocate alloc;

  //  protected long delayBeforeAllocRemoval = 0; //millis
}
