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

import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.TypedQuantityAspectValue;

public class UITypedQuantityAspectValueImpl implements UITypedQuantityAspectValue {
  TypedQuantityAspectValue tqav;

  public UITypedQuantityAspectValueImpl(TypedQuantityAspectValue tqav) {
    this.tqav = tqav;
  }

  public UUID getAsset() {
    return new UUID(tqav.getAsset().getUID().toString());
  }

  public double getQuantity() {
    return ((AspectValue)tqav).getValue();
  }
   
}

