/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.measure.Distance;
import org.cougaar.domain.planning.ldm.measure.Speed;

/**
 * Finds the travel time of a task using great circle distance
 *
 */

public class GSTravelTime implements GSParent {

  private GSAssetAccessor speedAccessor;
  private GSTaskAccessor toAccessor;
  private GSTaskAccessor fromAccessor;
  private double extraFactor;
  private boolean toAccessorReset = false;

  public GSTravelTime (String factor) {
    extraFactor = (new Double (factor)).doubleValue();
    toAccessor = new GSTaskAccessorImpl ("To");
    fromAccessor = new GSTaskAccessorImpl ("From");
//     GSLocationAccessor la = new GSLocationAccessor ("latlong");
//     ((GSTaskAccessorImpl) toAccessor).addChild (la);
//     ((GSTaskAccessorImpl) fromAccessor).addChild (la);
  }

  public void addChild (Object obj) {
    if (obj instanceof GSAssetAccessor)
      speedAccessor = (GSAssetAccessor) obj;
    else if (toAccessorReset)
      fromAccessor = (GSTaskAccessor) obj;
    else {
      toAccessor = (GSTaskAccessor) obj;
      toAccessorReset = true;
    }
  }

  public double value (Task task, Asset asset) {
//     double d = UTILMeasure.distanceBetween
//       ((GeolocLocation) toAccessor.value (task),
//        (GeolocLocation) fromAccessor.value (task)).getMeters();
//     double s = ((Speed) speedAccessor.value (asset)).getMetersPerSecond();
//     return extraFactor * d / s;
    return 10000.0; // Faked to make GSS seprable from logistics and geolocs
  }

}
