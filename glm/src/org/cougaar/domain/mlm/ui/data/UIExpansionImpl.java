/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
/*
 * Copyright 1999 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
 */

package org.cougaar.domain.mlm.ui.data;

import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.PlanElement;

public class UIExpansionImpl extends UIPlanElementImpl implements UIExpansion {
   
  public UIExpansionImpl(Expansion expansion) {
    super((PlanElement)expansion);
  }

  /** Returns the workflow created by expansion.
      @return UUID - the UUID of the workflow created by the expansion 
  */

  public UUID getWorkflow() {
    Expansion expansion = (Expansion)planElement;
    return new UUID(expansion.getWorkflow().getUID().toString());
  }
}

