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

import java.util.List;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;

/**
 * Listeners for workflow tasks that should be handled one at a
 * time, in contrast to the bufferingAllocator.
 *
 * Abstract because does not define :
 *
 * @see #findAsset
 * @see #createAllocation
 */

public abstract class UTILSingleTaskAllocatorPlugin 
  extends UTILAllocatorPluginAdapter 
  implements UTILSingleTaskWorkflowPlugin {

  /**
   * Implemented for UTILSingleTaskWorkflowListener
   *
   * Public version of publishAdd
   * @param allocOrFailedAlloc Allocation to remove from cluster's memory
   */
  public void publishAddingOfAllocation (PlanElement allocOrFailedAlloc) { 
    if (isDebugEnabled()) {
        debug (getName () + 
			    " : Publishing alloc " + allocOrFailedAlloc.getUID () + 
			    " for task " + allocOrFailedAlloc.getTask ().getUID());
    }

    publishAdd (allocOrFailedAlloc);
  }

  /**
   * Required for UTILBufferingPlugin
   *
   * Deal with the tasks that we have accumulated, which should only ever
   * be one.  This is sort of a strange situation as I believe this is 
   * effected by parameters instead of any hard-coded policy or methods.
   *
   * @param tasks List of tasks (only one) to handle
   */
  public void processTasks (List tasks) {
    // Should only ever have one task in here
    while (!tasks.isEmpty()) {
      Task t = (Task)tasks.remove(0);
      
      Asset a = findAsset(t);
      PlanElement alloc = createAllocation(t, a);
      publishAddingOfAllocation(alloc);
    } 
  }
}
