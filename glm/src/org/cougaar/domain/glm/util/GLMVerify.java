/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/util/Attic/GLMVerify.java,v 1.1 2000-12-15 20:18:03 mthome Exp $ */
/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.util;

import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.plan.GeolocLocation;

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
