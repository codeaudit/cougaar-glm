// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/Attic/GSTestSpecs.java,v 1.2 2000-12-20 18:18:09 mthome Exp $
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

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.planning.ldm.asset.Asset;

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
    org.cougaar.domain.glm.ldm.plan.GeolocLocationImpl gl =
      new org.cougaar.domain.glm.ldm.plan.GeolocLocationImpl();
    Random rand = new Random();
    if (rand.nextBoolean()) {
      gl.setLatitude (org.cougaar.domain.planning.ldm.measure.Latitude.newLatitude (2.0));
      gl.setLongitude (org.cougaar.domain.planning.ldm.measure.Longitude.newLongitude (2.0));
      gl.setGeolocCode ("aaa");
      gl.setName ("aaa");
    }
    else {
      gl.setLatitude (org.cougaar.domain.planning.ldm.measure.Latitude.newLatitude (0.0));
      gl.setLongitude (org.cougaar.domain.planning.ldm.measure.Longitude.newLongitude (0.0));
      gl.setGeolocCode ("xxx");
      gl.setName ("xxx");
    }
    return gl;
  }

  public GeolocLocation bestPOD (Task task) {
    org.cougaar.domain.glm.ldm.plan.GeolocLocationImpl gl =
      new org.cougaar.domain.glm.ldm.plan.GeolocLocationImpl();
   
    Random rand = new Random();
    if (rand.nextBoolean()) {
      gl.setLatitude (org.cougaar.domain.planning.ldm.measure.Latitude.newLatitude (8.0));
      gl.setLongitude (org.cougaar.domain.planning.ldm.measure.Longitude.newLongitude (8.0));
      gl.setGeolocCode ("bbb");
      gl.setName ("bbb");
    }
    else {
      gl.setLatitude (org.cougaar.domain.planning.ldm.measure.Latitude.newLatitude (10.0));
      gl.setLongitude (org.cougaar.domain.planning.ldm.measure.Longitude.newLongitude (10.0));
      gl.setGeolocCode ("yyy");
      gl.setName ("yyy");
    }
    return gl;
  }

}
