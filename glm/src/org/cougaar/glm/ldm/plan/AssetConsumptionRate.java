/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */


/* original contributed by ALPIcis group, BBN Technologies/GTE */

package org.cougaar.glm.ldm.plan;
import java.util.*;
import java.io.Serializable;
import org.cougaar.planning.ldm.measure.Rate;
import org.cougaar.planning.ldm.asset.Asset;

/** Representation of Consumption Rate structure for a particular Asset.
 * May also be used as Failure rate for 
 */
public interface AssetConsumptionRate extends Serializable {
  /** 
   * @return Enumeration of Assets which have failure/consumption rates.
   **/
  Enumeration getAssets();

  /** The actual Failure/Consumption rate for a given Asset
   *
   * @param asset to be consumed or repaired
   * @param optempo current rate of use
   * @return failure/consumption rate of the part at the given optempo, 
   * in terms of Unit of Issue (usually Each).
   **/
  Rate getRate(Asset asset, String optempo);
}
