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

/*
  Define a MPTask object that is created from the XML returned by
  a PlanServiceProvider.
*/

public interface UIMPTask extends UITask {

  /** @return UUID[] - the UUIDs of the parent tasks
   */

  UUID[] getParentTasks();


}

