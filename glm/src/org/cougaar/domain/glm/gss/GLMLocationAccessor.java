// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/Attic/GLMLocationAccessor.java,v 1.2 2000-12-20 18:18:09 mthome Exp $
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

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.lib.gss.GSValueAccessor;

/**
 * Accesses the specified property value for a given asset
 *
 * Copyright (C) 1999 BBN Technologies
 */

public class GLMLocationAccessor extends GSValueAccessor {

  private final static int ICAO = 0;
  private final static int GEOLOC = 1;
  private final static int LATLONG = 2;

  /**
   * Constructor sets up which property value to access
   */
  GLMLocationAccessor (String representation) {
      super ("", "");
    if (representation.equals ("icao"))
      this.representation = ICAO;
    else if (representation.equals ("geoloc"))
      this.representation = GEOLOC;
    else if (representation.equals ("latlong"))
      this.representation = LATLONG;
    else
      System.out.println ("Bad value for location accessor - " + representation);
    //    System.out.println ("GLMLocationAccessor create with rep " + representation);
  }

  /**
   * Returns the specified representation for a given location
   */
    //  public Object value (GeolocLocation location) {
    public Object value (Object location) {
	//    System.out.println ("GLMLocationAccessor.value called.");
    GeolocLocation geoloc = (GeolocLocation) location;
    switch (representation) {
    case ICAO:
      return geoloc.getIcaoCode();
    case GEOLOC:
	String result = geoloc.getGeolocCode().substring (0,4);
	//	System.out.println ("GLMLocationAccessor.value - " + result);
      return result;
    case LATLONG:
      return geoloc;
    }
    return null;
  }

  private int representation;

}
