/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.core.society.UID;

import org.cougaar.domain.planning.ldm.plan.AllocationforCollections;

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

