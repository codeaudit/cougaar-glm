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

import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;

/**
 *  Defines interface for simple, non-threaded allocators.
 *  I.e. tasks are handled one at a time.
 */

public interface UTILSingleTaskWorkflowPlugIn {
  /** 
   * Defines tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  boolean interestingTask(Task t);

 /**
   * This method should find the appropriate resource to
   * handle the given task, usually based on the direct
   * object of the task.
   * @param t task to find asset for
   * @return Asset to handle the task
   */

  Asset findAsset(Task t);

  /**
   * This method should create the actual allocation.
   * Child classes are responsible for calculating the
   * aspect values for the allocation result.
   *
   * When a task is assigned to an asset, if any preferences
   * are violated, then the PlanElement that is returned is a
   * FailedAllocation.  
   *
   * It has an AllocationResult w/ isSuccess=False.
   *
   * @param t task to allocate
   * @param a asset to handle the task
   * @return PlanElement which = Allocation or FailedAllocation
   */
  PlanElement createAllocation(Task t, Asset a);

  /**
   * Public version of publishAdd
   * @param alloc the allocation or FailedAllocation to add...
   */
  void publishAddingOfAllocation(PlanElement allocOrFailedAlloc);
}
