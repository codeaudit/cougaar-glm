/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;

/**
 * Interface that all asset accessors must satisfy
 *
 */

public interface GSAssetAccessor {

  /**
   * Accesses the specified property value for asset
   */
  Object value (Asset asset);

  /** value returned null - give some indication why */
  void reportError (Asset asset);
}







