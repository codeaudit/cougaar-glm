/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
   * @param allocOrFailedAlloc the allocation or FailedAllocation to add...
   */
  void publishAddingOfAllocation(PlanElement allocOrFailedAlloc);
}
