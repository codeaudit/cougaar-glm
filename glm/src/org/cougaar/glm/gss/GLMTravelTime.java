// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/gss/Attic/GLMTravelTime.java,v 1.1 2001-12-27 22:41:41 bdepass Exp $
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

package org.cougaar.glm.gss;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.measure.Distance;
import org.cougaar.planning.ldm.measure.Speed;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.util.GLMMeasure;

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
