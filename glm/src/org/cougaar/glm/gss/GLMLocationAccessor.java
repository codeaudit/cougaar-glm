// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/gss/Attic/GLMLocationAccessor.java,v 1.1 2001-12-27 22:41:41 bdepass Exp $
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

import org.cougaar.glm.ldm.plan.GeolocLocation;
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