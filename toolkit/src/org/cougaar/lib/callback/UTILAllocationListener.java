/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;

/**
 * Interface defining what to do when an allocation changes.
 *
 * Paired with UTILAllocationCallback.
 *
 * @see org.cougaar.lib.callback.UTILAllocationCallback
 * @see org.cougaar.lib.callback.UTILExpansionListener
 * @see org.cougaar.lib.callback.UTILAggregationListener
 */

public interface UTILAllocationListener extends UTILFilterCallbackListener {
  /**
   * Filter for task notifications on allocations that are 
   * interesting.
   *
   * @param t task to check for notification
   * @return boolean true if task is interesting
   */
  boolean interestingNotification(Task t);

  /**
   * Defines conditions for rescinding tasks.
   *
   * When returns TRUE, handleRescindedAlloc is called.
   *
   * See comment on UTILAllocatorPlugInAdapter.needToRescind.
   *
   * @param alloc allocation to check for
   * @return boolean true if task needs to be rescinded
   * @see #handleRescindedAlloc
   * @see org.cougaar.lib.filter.UTILAllocatorPlugInAdapter#needToRescind
   */
  boolean needToRescind (Allocation alloc);

  /**
   * What to do with a rescinded allocation. 
   *
   * See comment on UTILAllocatorPlugInAdapter.needToRescind.
   *
   * Implementers need to take into consideration that 
   * the asset chosen last time is not available this time.
   *
   * @param alloc the allocation that has been rescinded
   * @return true if handled
   * @see #needToRescind
   * @see org.cougaar.lib.filter.UTILAllocatorPlugInAdapter#needToRescind
   */
  boolean handleRescindedAlloc (Allocation alloc);

  /** 
   * Called when an allocation is removed from the cluster.
   * I.e. an upstream cluster removed an allocation, and this 
   * has resulted in this allocation being removed.
   *
   * If the plugin maintains some local state of the availability
   * of assets, it should update them here.
   */
  void handleRemovedAlloc (Allocation alloc);

  /**
   * Default implementation automatically moves the 
   * reported allocation result (AR) to the estimated AR on a
   * plan element that was previously allocated.
   * 
   * Called when needToRescind returns FALSE.
   *
   * (This is the ALP notification mechanism.)
   *
   * @param alloc the allocation that needs to be updated.
   * @see #needToRescind
   */
  void updateAllocationResult (PlanElement cpe);

  /**
   * What to do with a successful allocation. 
   * For implementers who DON'T extend UTILPlugInAdapter,
   * this is equivalent to updateAllocationResult and therefore
   * should be implemented with an empty body.
   * 
   * Called after updateAllocationResult when needToRescind returns FALSE.
   * Implementers need to take into consideration that 
   * the asset chosen last time is not available this time.
   *
   * @param alloc the returned successful allocation
   * @see #needToRescind
   * @see org.cougaar.lib.filter.UTILAllocatorPlugInAdapter#needToRescind
   */
  void handleSuccessfulAlloc(Allocation alloc);

  /**
   * Public version of publishRemove
   *
   * Called when needToRescind returns TRUE.
   *
   * @param alloc the allocation to remove...
   */
  void publishRemovalOfAllocation(Allocation alloc);
}
