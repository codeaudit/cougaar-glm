/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

public interface UIExpansion extends UIPlanElement {
   
  /** Returns the workflow created by expansion.
      @return UUID - the UUID of the workflow created by the expansion 
  */

  UUID getWorkflow();
}

