/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/util/Attic/GLMPrepPhrase.java,v 1.2 2000-12-20 18:18:39 mthome Exp $ */
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

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;


import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

import org.cougaar.domain.planning.ldm.plan.NewItineraryElement;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.cougaar.lib.util.UTILPrepPhrase;

/** 
 * This class contains utility functions for creating
 * and accessing PrepositionalPhrases.
 */

public class GLMPrepPhrase extends UTILPrepPhrase {
  private static String myName = "GLMPrepPhrase";

  public static GeolocLocation getFromLocation (Task t) {
    return (GeolocLocation) getIndirectObject (t, Constants.Preposition.FROM);
  }

  public static GeolocLocation getToLocation (Task t) {
    return (GeolocLocation) getIndirectObject (t, Constants.Preposition.TO);
  }

  public static PrepositionalPhrase makePrepositionalPhrase(RootFactory ldmf,
							    String prep,
							    GeolocLocation geoloc) {
    NewPrepositionalPhrase npp = ldmf.newPrepositionalPhrase();
    npp.setPreposition(prep);
    npp.setIndirectObject(geoloc);
    return npp;
  }

}

