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

public interface UIAllocation extends UIPlanElement {
   
  /** @return UUID - the uuid of the asset allocated */

  UUID getAsset();

  /** Return the UUID of the allocation task.  If a task is allocated
    to another cluster, then the allocation task is the task that is
    sent to the other cluster.  Note, that this is only valid if the
    allocated asset is another cluster.
    @return UUID - the uuid of the allocation task
    */

  UUID getAllocationTask();
}

