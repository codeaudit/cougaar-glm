/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.PlanElement;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;

import java.util.List;

import org.cougaar.lib.util.UTILPlugInException;

/**
 * Listeners for workflow tasks that should be handled one at a
 * time, in contrast to the bufferingAllocator.
 *
 * Abstract because does not define :
 *
 * @see #findAsset
 * @see #createAllocation
 */

public abstract class UTILSingleTaskAllocatorPlugIn 
  extends UTILAllocatorPlugInAdapter 
  implements UTILSingleTaskWorkflowPlugIn {

  /**
   * Implemented for UTILSingleTaskWorkflowListener
   *
   * Public version of publishAdd
   * @param alloc Allocation to remove from cluster's memory
   */
  public void publishAddingOfAllocation (PlanElement allocOrFailedAlloc) { 
    if (myExtraExtraOutput) {
        System.out.println (getName () + 
			    " : Publishing alloc " + allocOrFailedAlloc.getUID () + 
			    " for task " + allocOrFailedAlloc.getTask ().getUID());
    }

    publishAdd (allocOrFailedAlloc); 
  }

  /**
   * Required for UTILBufferingPlugIn
   *
   * Deal with the tasks that we have accumulated, which should only ever
   * be one.  This is sort of a strange situation as I believe this is 
   * effected by parameters instead of any hard-coded policy or methods.
   *
   * @param List of tasks (only one) to handle
   */
  public void processTasks (List tasks) {
    // Should only ever have one task in here
    boolean toldMessage = false;
    while (!tasks.isEmpty()) {
      Task t = (Task)tasks.remove(0);
      
      Asset a = findAsset(t);
      PlanElement alloc = createAllocation(t, a);
      publishAddingOfAllocation(alloc);
    } 
  }
}
