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

import org.cougaar.lib.callback.UTILAggregationCallback;
import org.cougaar.lib.callback.UTILAssetCallback;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.callback.UTILWorkflowCallback;
import org.cougaar.lib.util.UTILAggregate;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Task;


/**
 * <pre>
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
 * </pre>
 */
public abstract class UTILAggregatorPluginAdapter 
extends UTILBufferingPluginAdapter implements UTILAggregatorPlugin {
  protected UTILAggregationCallback getAggCallback    () { return myAggCallback; }
  /**
   * Override to replace with a callback that has a different predicate
   * or different behaviour when triggered.
   */
  protected UTILAggregationCallback createAggCallback () { 
    return new UTILAggregationCallback  (this, logger); 
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
   * @see #createAggCallback
   */
  public void setupFilters () {
    super.setupFilters ();

    aggregate = new UTILAggregate(logger);

    if (isInfoEnabled())
      info (getName () + " : Filtering for generic Assets...");

    addFilter (myAssetCallback    = createAssetCallback    ());

    if (isInfoEnabled())
      info (getName () + " : Filtering for Aggregations...");

    addFilter (myAggCallback    = createAggCallback    ());
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


  /** 
   * <pre>
   * Implemented for UTILBufferingPlugin
   *
   * Got an ill-formed task, now handle it, by
   * publishing a failed aggregation for the task.
   * </pre>
   * @param t badly-formed task to handle
   */
  public void handleIllFormedTask (Task t) {
    reportIllFormedTask(t);
    publishAdd (aggregate.makeFailedAggregation (null, ldmf, t));
  }

  /**
   * <pre>
   * Implemented for UTILAggregationListener
   *
   * OVERRIDE to see which task notifications you
   * think are interesting
   *
   * By default, interested in all tasks.
   * <pre>
   * @param t task to check for notification
   * @return boolean true if task is interesting
   * @see org.cougaar.planning.ldm.plan.Verb
   */
  public boolean interestingParentTask (Task t) { 
    return true;
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
   * Place to handle new assets.
   *
   * Does nothing by default.
   *
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
   * @param changedAssets changed assets found in the container
   */
  public void handleChangedAssets(Enumeration changedAssets) {}

  /**
   * <pre>
   * Implemented for UTILAggregationListener
   *
   * define conditions for rescinding tasks.
   * 
   * Only return true if the plugin can do something different 
   * with the task that failed.  See UTILAllocate.isFailedPE.
   *
   * </pre>
   * @param agg aggregation to check
   * @return boolean true if task needs to be rescinded
   * @see org.cougaar.lib.filter.UTILAggregatorPluginAdapter
   * @see org.cougaar.lib.util.UTILAllocate#isFailedPE
   */
  public boolean needToRescind (Aggregation agg) { 
    return false;
  }
    
  /**
   * <pre>
   * Implemented for UTILAggregationListener
   *
   * Updates and publishes allocation result of aggregation.
   *
   * </pre>
   * @param agg aggregation to report
   */
  public void reportChangedAggregation(Aggregation agg) {
    updateAllocationResult (agg);
  }

  /**
   * <pre>
   * What to do with a successful aggregation. 
   * 
   * Called after updateAllocationResult when needToRescind returns FALSE.
   *
   * </pre>
   * @param agg the returned successful aggregation
   * @see #needToRescind
   * @see org.cougaar.lib.filter.UTILAggregatorPluginAdapter#needToRescind
   */
  public void handleSuccessfulAggregation(Aggregation agg) {
  }

  /** 
   * <pre>
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
   * </pre>
   */
  public void handleRemovedAggregation (Aggregation agg) {}

  /** 
   * <pre>
   * Remove aggregation from cluster's log plan
   *
   * Done by 
   *  1) publishRemove of aggregation
   *  2) publishChange of task
   * </pre>
   * @param aggToRemove aggregation to remove
   */
  public void publishRemovalOfAggregation (Aggregation aggToRemove) {
    Task changedTask = aggToRemove.getTask ();
    publishRemove (aggToRemove);
    publishChange (changedTask);
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
   * </pre>
   * @return ALL assets found in container
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
  protected UTILAggregate aggregate;
}
