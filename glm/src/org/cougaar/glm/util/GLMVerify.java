/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/util/GLMVerify.java,v 1.1 2001-12-27 22:42:14 bdepass Exp $ */
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

package org.cougaar.glm.util;

import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.GeolocLocation;

import java.util.Date;

import org.cougaar.lib.util.UTILVerify;

/**
 * Helper classes to see if a task is consistent and 
 * reasonable.
 */
public class GLMVerify extends UTILVerify {

  public static boolean hasFromPrep (Task t) {
    boolean retval = GLMPrepPhrase.hasPrepNamed (t, Constants.Preposition.FROM);
    if (!retval) return retval;
    
    return checkGeoloc (GLMPrepPhrase.getFromLocation(t));
  }

  protected static boolean checkGeoloc (GeolocLocation geoloc) {
    Longitude startlong = geoloc.getLongitude();
    Latitude  startlat  = geoloc.getLatitude();

    if (startlong == null)
      return false;
    if (startlat == null)
      return false;
    return true;
  }

  public static boolean hasToPrep (Task t) {
    boolean retval = GLMPrepPhrase.hasPrepNamed (t, Constants.Preposition.TO);
    if (!retval) return retval;

    return checkGeoloc (GLMPrepPhrase.getToLocation(t));
  }

}
