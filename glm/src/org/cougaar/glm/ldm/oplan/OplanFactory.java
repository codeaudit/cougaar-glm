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
package org.cougaar.glm.ldm.oplan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;

import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;

import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.LocationScheduleElement;
import org.cougaar.planning.ldm.plan.LocationScheduleElementImpl;
import org.cougaar.planning.ldm.plan.TaggedLocationScheduleElement;
import org.cougaar.planning.ldm.plan.Transferable;


import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;

/**
 * Factory for the various oplan component implementations
 **/
public final class OplanFactory
{
  // nobody else can instantiate me, and I aint gonna do it.
  private OplanFactory() {}

  public static OrgActivityImpl newOrgActivity(String orgID, UID oplanUID) {
    return new OrgActivityImpl(orgID, oplanUID);
  }

  public static OrgActivityImpl newOrgActivity(String activityType, String activityName, String orgID, UID oplanUID) {
    return new OrgActivityImpl(activityType, activityName, orgID, oplanUID);
  }

}
