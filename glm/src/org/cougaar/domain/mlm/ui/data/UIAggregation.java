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

public interface UIAggregation extends UIPlanElement {
   
  /** Get the IDs of the tasks that are being aggregated.
   */
  UUID[] getParentTasks();

  /** Get the newly created task that represents all the parent tasks.
   */
  UUID getCombinedTask();

}

