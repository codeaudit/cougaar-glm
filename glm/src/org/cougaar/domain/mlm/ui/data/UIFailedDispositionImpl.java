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
import org.cougaar.domain.planning.ldm.plan.Disposition;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.core.society.UID;

public class UIFailedDispositionImpl extends UIPlanElementImpl implements UIFailedDisposition {

 public UIFailedDispositionImpl(Disposition allocation) {
    super((PlanElement)allocation);
  }
   
}

