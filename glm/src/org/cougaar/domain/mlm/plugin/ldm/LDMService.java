/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import org.cougaar.domain.planning.ldm.asset.Asset;

public interface LDMService {

  /** get the Asset Prototype associated with the typeid.
   * Will return null if nobody can handle it.
   **/
  Asset getPrototype(String typeid, Class hint);
  Asset getPrototype(String typeid);
  /** Hook to request that other QueryHandlers fill in the
   * properties of asset.
   **/
  void provideProperties(Asset asset);
}
