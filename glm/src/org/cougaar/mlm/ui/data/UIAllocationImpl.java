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

package org.cougaar.mlm.ui.data;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.core.util.UID;

import org.cougaar.planning.ldm.plan.AllocationforCollections;

public class UIAllocationImpl extends UIPlanElementImpl implements UIAllocation {

  public UIAllocationImpl(Allocation allocation) {
    super((PlanElement)allocation);
  }
   
  /** @return UUID - the UID of the asset allocated */

  public UUID getAsset() {
    Asset asset = ((Allocation)planElement).getAsset();
    if (asset != null) {
      UID uid = asset.getUID();
      if (uid != null)
        return new UUID(uid.toString());
    }
    return null;
  }

  /** Return the UUID of the allocation task.  If a task is allocated
    to another cluster, then the allocation task is the task that is
    sent to the other cluster.  Note, that this is only valid if the
    allocated asset is another cluster.
    @return UUID - the uuid of the allocation task
    */

  public UUID getAllocationTask() {
    if (planElement instanceof AllocationforCollections) {
      Task allocationTask = 
        ((AllocationforCollections)planElement).getAllocationTask();
      if (allocationTask != null)
        return new UUID(allocationTask.getUID().toString());
    }
    return null;
  }
}

