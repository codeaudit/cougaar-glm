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
