/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss.plugins;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;

import org.cougaar.lib.callback.UTILAssetListener;
import org.cougaar.lib.gss.GSTaskGroup;

import java.util.List;
import java.util.Vector;

import org.cougaar.lib.filter.UTILTimeoutBufferingPlugIn;

/**
 * Defines what it means to be a gss buffering plugin
 *
 */

public interface UTILGSSBufferingPlugIn extends UTILTimeoutBufferingPlugIn, UTILAssetListener {
  /** do any preprocessing necessary before passing tasks to scheduler */
  void preProcessBufferedTasks(List bufferedTasks);
    
  /** 
   * ask plugin if asset is ready to be disposed 
   * of (allocated, expanded, or aggregated).
   *
   */
  boolean isReadyToMakePE (Asset anAsset, GSTaskGroup group);

  /** if the asset is ready to be disposed, dispose of it */
  void    makePlanElement (Asset anAsset, GSTaskGroup group);

  /** if no asset could be found to handle the task, handle them in some way */
  void    handleImpossibleTasks (List unallocatedTasks);

  /** 
   * if we need to wait before removing the task group from the scheduler for
   * some publish results, return true.  NOTE that we must then manually remove
   * the group later or it will just take up unnecessary space forever i.e. NOTE
   * that "freeze" implies "don't remove".
   */
  boolean freezeTaskGroupForPublish(Asset asset, GSTaskGroup group);

  /**
   * Call this function with all task groups that are being frozen.
   * @param gstg the task group that is being frozen.
   * @return A vector of PrepPhrases that will uniquely define this
   * GSTaskGroup, and can be used later to remove the Frozen tasks and
   * unfreeze the group.
   **/
  List registerFrozenTaskGroup(GSTaskGroup gstg);

  /**
   * This function should be called to actually remove the frozen tasks from
   * The scheduler when what ever processing is necessary has been done.
   * @param allocResult The allocation result that generated the
   * possiblyFrozenTasks.  They should/or should not be removed based on
   * the contents of the allocationResult.
   * @param possiblyFrozenTasks a list of Tasks that have been 
   * processed and dependent on the AllocationResults, should now be removed 
   * from the scheduler.
   **/
  void removeFrozenTasks(AllocationResult allocResult, List possiblyFrozenTasks);
}
