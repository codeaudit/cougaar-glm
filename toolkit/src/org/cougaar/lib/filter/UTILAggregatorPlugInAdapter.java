/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.Aggregation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.lib.callback.UTILAggregationCallback;
import org.cougaar.lib.callback.UTILAggregationListener;
import org.cougaar.lib.callback.UTILAssetCallback;
import org.cougaar.lib.callback.UTILAssetListener;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.callback.UTILWorkflowCallback;

import org.cougaar.lib.util.UTILAggregate;


import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;


/**
 * By default listens for all Assets (please override
 * createAssetCallback to make more specific).  
 * Also listens for allocations.
 * 
 * Fill in empty functions as needed to add functionality
 *
 * Abstract because does not define :
 *
 * - processTasks
 * - handleFailedAggregation
 */

public abstract class UTILAggregatorPlugInAdapter 
extends UTILBufferingPlugInAdapter implements UTILAggregatorPlugIn {
  protected UTILAggregationCallback getAggCallback    () { return myAggCallback; }
  /**
   * Override to replace with a callback that has a different predicate
   * or different behaviour when triggered.
   */
  protected UTILAggregationCallback createAggCallback () { 
    return new UTILAggregationCallback  (this); 
  } 

  protected UTILAssetCallback getAssetCallback    () { return myAssetCallback; }

  /**
   * Override to replace with a callback that has a different predicate
   * or different behaviour when triggered.
   *
   * PLEASE override this -- this is mainly here for backward compatibility.
   * I.e. we shouldn't get all the assets of the cluster and then iterate over
   * them, testing them with instanceof to get the assets we want.  We should
   * use a more specific callback filter/predicate.
   * 
   * @see org.cougaar.lib.callback.UTILPhysicalAssetCallback
   * @see org.cougaar.lib.callback.UTILNotOrganizationCallback
   */
  protected UTILAssetCallback createAssetCallback () { 
    return new UTILAssetCallback  (this); 
  } 

  /**
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
   * @see #createAssetCallback
   * @see #createAggCallback
   */
  public void setupFilters () {
    super.setupFilters ();

    if (myExtraOutput)
      System.out.println (getName () + " : Filtering for generic Assets...");

    addFilter (myAssetCallback    = createAssetCallback    ());

    if (myExtraOutput)
      System.out.println (getName () + " : Filtering for Aggregations...");

    addFilter (myAggCallback    = createAggCallback    ());
  }

  /**
   * Provide the callback that is paired with the buffering thread, which is a
   * listener.  The buffering thread is the listener to the callback
   *
   * @return a WorkflowCallback with the buffering thread as its listener
   */
  protected UTILFilterCallback createThreadCallback (UTILGenericListener bufferingThread) { 
    if (myExtraOutput)
      System.out.println (getName () + " Filtering for Generic Workflows...");

    myWorkflowCallback = new UTILWorkflowCallback  (bufferingThread); 
    return myWorkflowCallback;
  } 

  protected UTILFilterCallback getWorkflowCallback () {
    return myWorkflowCallback;
  }


  /** 
   * Implemented for UTILBufferingPlugin
   *
   * Got an ill-formed task, now handle it, by
   * publishing a failed aggregation for the task.
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    reportIllFormedTask(t);
    publishAdd (UTILAggregate.makeFailedAggregation (null, ldmf, t));
  }

  /**
   * Implemented for UTILAggregationListener
   *
   * OVERRIDE to see which task notifications you
   * think are interesting
   *
   * By default, interested in tasks with TRANSPORT verbs
   *
   * @param t task to check for notification
   * @return boolean true if task is interesting
   * @see org.cougaar.domain.planning.ldm.plan.Verb
   */
  public boolean interestingParentTask (Task t) { 
    return true; //(t.getVerb().equals(Constants.Verb.TRANSPORT));
  }


  /**
   * Implemented for UTILAssetListener
   *
   * OVERRIDE to see which assets you
   * think are interesting
   * @param a asset to check for notification
   * @return boolean true if asset is interesting
   */
  public boolean interestingAsset(Asset a) {
    return true;
  }

  /**
   * Place to handle new assets.
   *
   * Does nothing by default.
   *
   * @param newAssets new assets found in the container
   */
  public void handleNewAssets(Enumeration newAssets) {}

  /**
   * Place to handle changed assets.
   *
   * Does nothing by default.
   *
   * @param changedAssets changed assets found in the container
   */
  public void handleChangedAssets(Enumeration changedAssets) {}

  /**
   * Implemented for UTILAggregationListener
   *
   * define conditions for rescinding tasks.
   * 
   * Only return true if the plugin can do something different 
   * with the task that failed.  See UTILAllocate.isFailedPE.
   *
   * @param alloc allocation to check for
   * @return boolean true if task needs to be rescinded
   * @see org.cougaar.lib.filter.UTILAggregatorPlugInAdapter
   * @see org.cougaar.lib.util.UTILAllocate#isFailedPE
   */
  public boolean needToRescind (Aggregation agg) { 
    return false;
  }
    
  /**
   * Implemented for UTILAggregationListener
   *
   * Updates and publishes allocation result of aggregation.
   *
   * @param aggregation to report
   */
  public void reportChangedAggregation(Aggregation agg) {
    updateAllocationResult (agg);
  }

  /**
   * What to do with a successful aggregation. 
   * 
   * Called after updateAllocationResult when needToRescind returns FALSE.
   *
   * @param agg the returned successful aggregation
   * @see #needToRescind
   * @see org.cougaar.lib.filter.UTILAggregatorPlugInAdapter#needToRescind
   */
  public void handleSuccessfulAggregation(Aggregation agg) {
  }

  /** 
   * Implemented for UTILAggregationListener
   *
   * Called when an aggregation is removed from the cluster.
   * I.e. an upstream cluster removed an allocation, and this 
   * has resulted in this aggregation being removed.
   *
   * If the plugin maintains some local state of the availability
   * of assets, it should update them here.
   *
   * Does nothing by default.
   */
  public void handleRemovedAggregation (Aggregation agg) {}

  /** 
   * Remove aggregation from cluster's log plan
   *
   * Done by 
   *  1) publishRemove of aggregation
   *  2) publishChange of task
   * @param aggregation to remove
   */
  public void publishRemovalOfAggregation (Aggregation aggToRemove) {
    Task changedTask = aggToRemove.getTask ();
    publishRemove (aggToRemove);
    publishChange (changedTask);
  }

  /** 
   * Utility method for finding all resource assets. 
   *
   * In general, it would be better if plugins could use more
   * specific filters and so this call would return a restricted set of
   * assets. 
   *
   * At the very least, 
   * cluster assets can be divided between organizational assets
   * and physical assets.  And getOrganizationalAssets () in UTILPlugIn
   * returns only org assets.
   *
   * @see UTILPlugIn#getOrganizationAssets ()
   * @return Enumeration of ALL assets found in container
   */
  protected final Iterator getAssets() {
    Collection assets = 
      getAssetCallback().getSubscription ().getCollection();

    if (assets.size() != 0) {
      return assets.iterator();
    }
    return null;
  }

  protected UTILWorkflowCallback    myWorkflowCallback;
  protected UTILAssetCallback       myAssetCallback;
  protected UTILAggregationCallback myAggCallback;
}
