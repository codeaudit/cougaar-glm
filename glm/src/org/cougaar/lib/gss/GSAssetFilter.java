/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;

/**
 * Selects which assets to use to schedule
 *
 */

public class GSAssetFilter implements GSParent {

  private GSAssetClassMatch match = null;

  public void addChild (Object obj) {
    match = (GSAssetClassMatch) obj;
  }

  public boolean passesFilter (Asset a) {
    return ((match == null) || (match.matchesClass (a)));
  }

}
