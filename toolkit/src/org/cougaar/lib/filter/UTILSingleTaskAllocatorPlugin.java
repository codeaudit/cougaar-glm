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
import org.cougaar.planning.ldm.plan.Verb;

import java.util.List;

import org.cougaar.lib.util.UTILPluginException;

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
   * @param alloc Allocation to remove from cluster's memory
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
