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

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;

/**
 *  Defines interface for simple, non-threaded allocators.
 *  I.e. tasks are handled one at a time.
 */

public interface UTILSingleTaskWorkflowPlugin {
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
