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
 * OrgActivity
 * The OrgActivity method is a LDM Object that contains organizational activity information.
 * TheOrgActivity object includes information such as activityName, activityType, 
 * opTempo, timespan, Location information, and a hashmap containing other 
 * detailed information.  The OrgActivities are initially created in the J3 cluster 
 * and then is transferred to other clusters by the Propagation Plugin. Subordinate clusters can subscribe 
 * to changes in the OrgActivity information in order to react to changes accordingly.
 * Subordinate clusters should not modify (set) OrgActivity information.
 **/
public interface OrgActivity
  extends OplanContributor,  org.cougaar.util.TimeSpan, Transferable, UniqueObject, Serializable, Cloneable
{	
  //ActivityTypes
  String DEPLOYMENT = "Deployment";
  String DEPLOYMENT_PREPO = "Deployment-Prepo";
  String EMPLOYMENT_CSS = "Employment-CSS";
  String DEFENSIVE = "Employment-Defensive";
  String OFFENSIVE = "Employment-Offensive";
  String HOME = "Home";
  String STAND_DOWN = "Stand-Down";
  String REDEPLOYMENT = "Redeployment";
  String RSOI = "RSOI";

  // Not in the official set
  String RECEPTION = "Reception";
  String RETROGRADE = "Retrograde";

  //Optempo
  String HIGH_OPTEMPO = "High";
  String MEDIUM_OPTEMPO = "Medium";
  String LOW_OPTEMPO = "Low";


  String getActivityType();
  void setActivityType(String activityType);

  String getActivityName();
  void setActivityName(String activityName);

  String getOrgID();
  void setOrgID(String orgID);

  UID getOrgActivityId();
  void setOrgActivityId(UID uid);

  UID getOplanUID();
  void setOplanUID(UID oplanUID);

  /** @deprecated Use getOplanUID */
  UID getOplanID();
  /** @deprecated Use setOplanUID */
  void setOplanID(UID oplanUID);

  String getOpTempo();
  void setOpTempo(String opTempo);

  TimeSpan getTimeSpan();
  long getStartTime();
  long getEndTime();
  void setTimeSpan(TimeSpan ts);

  GeolocLocation getGeoLoc();
  void setGeoLoc(GeolocLocation geoLoc);

  String getActivityItem(String key);
  void addActivityItem(String key, String value);
  void modifyActivityItem(String key, String value);
  HashMap getItems();


  void setAll(Transferable other);

  boolean same(Transferable other);

  /** convert OPlan-centric location and timespan to 
   * standard ALPish (logplan) schedule element 
   * @return a LocationScheduleElement or null, if a locationscheduleelement 
   * cannot be constructed (e.g. no schedule, no location).
   **/
  LocationScheduleElement getNormalizedScheduleElement();

  /** LocationScheduleElement which is labelled with an associated 
   * OrgActivity UID so that it can be found and replaced or removed
   * later.
   **/
  final class OAScheduleElement extends TaggedLocationScheduleElement {
    /** UID of the owning OrgActivity **/
    private UID uid;
    
    public OAScheduleElement(long t0, long t1, Location l, UID uid) {
      super(t0,t1,l);
      this.uid = uid;
    }
    
    /** @return the UID of the associated OrgActivity. **/
    public UID getOrgActivityUID() { return uid; }

    /** @return the UID of the associated OrgActivity **/
    public Object getOwner() { return uid; }
  }
}
