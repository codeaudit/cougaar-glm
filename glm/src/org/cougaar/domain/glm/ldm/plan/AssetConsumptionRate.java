/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */


/* original contributed by ALPIcis group, BBN Technologies/GTE */

package org.cougaar.domain.glm.ldm.plan;
import java.util.*;
import java.io.Serializable;
import org.cougaar.domain.planning.ldm.measure.Rate;
import org.cougaar.domain.planning.ldm.asset.Asset;

/** Representation of Consumption Rate structure for a particular Asset.
 * May also be used as Failure rate for 
 */
public interface AssetConsumptionRate extends Serializable {
  /** 
   * @return Enumeration of Assets which have failure/consumption rates.
   **/
  public Enumeration getAssets();

  /** The actual Failure/Consumption rate for a given Asset
   *
   * @param asset to be consumed or repaired
   * @param optempo current rate of use
   * @return failure/consumption rate of the part at the given optempo, 
   * in terms of Unit of Issue (usually Each).
   **/
  public Rate getRate(Asset asset, String optempo);
}
