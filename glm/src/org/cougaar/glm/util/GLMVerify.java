/*  */
/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.util;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.lib.util.UTILVerify;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.log.Logger;

/**
 * Helper classes to see if a task is consistent and 
 * reasonable.
 */
public class GLMVerify extends UTILVerify {
  public GLMVerify (Logger logger) { super (logger); glmPrepHelper = new GLMPrepPhrase(logger);}

  public boolean hasFromPrep (Task t) {
    boolean retval = glmPrepHelper.hasPrepNamed (t, Constants.Preposition.FROM);
    if (!retval) return retval;
    
    return checkGeoloc (glmPrepHelper.getFromLocation(t));
  }

  protected boolean checkGeoloc (GeolocLocation geoloc) {
    Longitude startlong = geoloc.getLongitude();
    Latitude  startlat  = geoloc.getLatitude();

    if (startlong == null)
      return false;
    if (startlat == null)
      return false;
    return true;
  }

  public boolean hasToPrep (Task t) {
    boolean retval = glmPrepHelper.hasPrepNamed (t, Constants.Preposition.TO);
    if (!retval) return retval;

    return checkGeoloc (glmPrepHelper.getToLocation(t));
  }

  protected GLMPrepPhrase glmPrepHelper;
}
