// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/Attic/GLMTravelTime.java,v 1.1 2000-12-15 20:18:00 mthome Exp $
/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.measure.Distance;
import org.cougaar.domain.planning.ldm.measure.Speed;
import org.cougaar.domain.glm.plan.GeolocLocation;
import org.cougaar.domain.glm.util.GLMMeasure;

import org.cougaar.lib.gss.GSAssetAccessor;
import org.cougaar.lib.gss.GSTaskAccessor;
import org.cougaar.lib.gss.GSParent;
import org.cougaar.lib.gss.GSTravelTime;

/**
 * Finds the travel time of a task using great circle distance
 *
 * Copyright (C) 1999 BBN Technologies
 */

public class GLMTravelTime extends GSTravelTime implements GSParent {

  private GSAssetAccessor speedAccessor;
  private GSTaskAccessor toAccessor;
  private GSTaskAccessor fromAccessor;
  private double extraFactor;
  private boolean toAccessorReset = false;

  public GLMTravelTime (String factor) {
    super(factor);
    extraFactor = (new Double (factor)).doubleValue();
    toAccessor = new GLMTaskAccessorImpl ("To");
    fromAccessor = new GLMTaskAccessorImpl ("From");
    GLMLocationAccessor la = new GLMLocationAccessor ("latlong");
    ((GLMTaskAccessorImpl) toAccessor).addChild (la);
    ((GLMTaskAccessorImpl) fromAccessor).addChild (la);
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
    double d = GLMMeasure.distanceBetween
      ((GeolocLocation) toAccessor.value (task),
       (GeolocLocation) fromAccessor.value (task)).getMeters();
    double s = ((Speed) speedAccessor.value (asset)).getMetersPerSecond();
    return extraFactor * d / s;
  }

}
