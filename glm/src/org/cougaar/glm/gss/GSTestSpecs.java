// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/gss/Attic/GSTestSpecs.java,v 1.1 2001-12-27 22:41:41 bdepass Exp $
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

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.asset.Asset;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import org.cougaar.lib.gss.*;

/**
 * Test scheduling specs to make sure that extending works
 *
 * Copyright (C) 1999 BBN Technologies
 */

public class GSTestSpecs extends GSSchedulingSpecs {

  public Vector initialize (Vector allAssets) {
    System.out.println ("Overriding initialize, but not doing anything");
    return super.initialize (allAssets);
  }

  public String bestPOEGeoloc (Task task) {
    return bestPOE(task).getGeolocCode();
  }

  public String bestPODGeoloc (Task task) {
    return bestPOD(task).getGeolocCode();
  }

  public GeolocLocation bestPOE (Task task) {
    org.cougaar.glm.ldm.plan.GeolocLocationImpl gl =
      new org.cougaar.glm.ldm.plan.GeolocLocationImpl();
    Random rand = new Random();
    if (rand.nextBoolean()) {
      gl.setLatitude (org.cougaar.planning.ldm.measure.Latitude.newLatitude (2.0));
      gl.setLongitude (org.cougaar.planning.ldm.measure.Longitude.newLongitude (2.0));
      gl.setGeolocCode ("aaa");
      gl.setName ("aaa");
    }
    else {
      gl.setLatitude (org.cougaar.planning.ldm.measure.Latitude.newLatitude (0.0));
      gl.setLongitude (org.cougaar.planning.ldm.measure.Longitude.newLongitude (0.0));
      gl.setGeolocCode ("xxx");
      gl.setName ("xxx");
    }
    return gl;
  }

  public GeolocLocation bestPOD (Task task) {
    org.cougaar.glm.ldm.plan.GeolocLocationImpl gl =
      new org.cougaar.glm.ldm.plan.GeolocLocationImpl();
   
    Random rand = new Random();
    if (rand.nextBoolean()) {
      gl.setLatitude (org.cougaar.planning.ldm.measure.Latitude.newLatitude (8.0));
      gl.setLongitude (org.cougaar.planning.ldm.measure.Longitude.newLongitude (8.0));
      gl.setGeolocCode ("bbb");
      gl.setName ("bbb");
    }
    else {
      gl.setLatitude (org.cougaar.planning.ldm.measure.Latitude.newLatitude (10.0));
      gl.setLongitude (org.cougaar.planning.ldm.measure.Longitude.newLongitude (10.0));
      gl.setGeolocCode ("yyy");
      gl.setName ("yyy");
    }
    return gl;
  }

}
