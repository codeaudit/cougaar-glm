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
import org.cougaar.lib.util.UTILPrepPhrase;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.log.Logger;

/** 
 * This class contains utility functions for creating
 * and accessing PrepositionalPhrases.
 */

public class GLMPrepPhrase extends UTILPrepPhrase {
  public GLMPrepPhrase (Logger logger) { super (logger); }

  public GeolocLocation getFromLocation (Task t) {
    return (GeolocLocation) getIndirectObject (t, Constants.Preposition.FROM);
  }

  public GeolocLocation getToLocation (Task t) {
    return (GeolocLocation) getIndirectObject (t, Constants.Preposition.TO);
  }

  public PrepositionalPhrase makePrepositionalPhrase(PlanningFactory ldmf,
						     String prep,
						     GeolocLocation geoloc) {
    NewPrepositionalPhrase npp = ldmf.newPrepositionalPhrase();
    npp.setPreposition(prep);
    npp.setIndirectObject(geoloc);
    return npp;
  }

}

