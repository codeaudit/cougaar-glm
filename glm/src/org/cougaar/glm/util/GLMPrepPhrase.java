/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/util/GLMPrepPhrase.java,v 1.5 2003-12-09 17:55:14 rtomlinson Exp $ */
/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

