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
package org.cougaar.glm.ldm.oplan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;

import org.cougaar.core.util.XMLizable;
import org.cougaar.core.util.XMLize;

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
public class OrgActivity extends OwnedUniqueObject
  implements OplanContributor,  org.cougaar.util.TimeSpan, Transferable, XMLizable, UniqueObject, Serializable, Cloneable
{	
  private String activityName;
  private String activityType;
  private String opTempo;
  private TimeSpan theTimeSpan;	
  private GeolocLocation geoLoc;
  private HashMap oaHashMap = new HashMap(5);
  private String orgID;
  private UID oplanUID;
  
  //ActivityTypes
  public static final String DEPLOYMENT = "Deployment";
  public static final String DEPLOYMENT_PREPO = "Deployment-Prepo";
  public static final String EMPLOYMENT_CSS = "Employment-CSS";
  public static final String DEFENSIVE = "Employment-Defensive";
  public static final String OFFENSIVE = "Employment-Offensive";
  public static final String HOME = "Home";
  public static final String STAND_DOWN = "Stand-Down";
  public static final String REDEPLOYMENT = "Redeployment";
  public static final String RSOI = "RSOI";

  // Not in the official set
  public static final String RECEPTION = "Reception";
  public static final String RETROGRADE = "Retrograde";

  //Optempo
  public static final String HIGH_OPTEMPO = "High";
  public static final String MEDIUM_OPTEMPO = "Medium";
  public static final String LOW_OPTEMPO = "Low";

  public OrgActivity(String orgID, UID oplanUID) 
  {	
    this.orgID = unique(orgID);
    this.oplanUID = oplanUID;	
  }// OrgActivity
	
  public OrgActivity(String activityType, String activityName, String orgID, UID oplanUID) 
  {
    this.activityType = unique(activityType);
    this.activityName = unique(activityName);
    this.orgID = unique(orgID);   
    this.oplanUID = oplanUID;
  }// OrgActivity

  public void setActivityType(String activityType) 
  {
    this.activityType = unique(activityType);	   
  }//setActivityType
	
  public void setActivityName(String activityName) 
  {
    this.activityName = unique(activityName);
  }//setActivityName */
	
  public void setOrgID(String orgID)
  {
    this.orgID = unique(orgID);
  }

//    public void setUID(UID uid) {
//      orgActivityId = uid;
//    }
//    public UID getUID() {
//      return orgActivityId;
//    }
  public void setOrgActivityId(UID uid)
  {

  }

  public UID getOrgActivityId()
  {
    return getUID();
  }
	
  public void setOplanUID(UID oplanUID)
  {
    this.oplanUID = oplanUID;
  }

  /** @deprecated Use setOplanUID */
  public void setOplanID(UID oplanUID)
  {
    this.oplanUID = oplanUID;
  }
  
  /** @deprecated Use getOplanUID */
  public UID getOplanID()
  {
    return oplanUID;
  }

  public UID getOplanUID()
  {
    return oplanUID;
  }
	
  public String getOrgID()
  {
    return orgID;
  }

  public void setOpTempo(String opTempo) 
  {
    // BOZO - check against valid range
    this.opTempo = unique(opTempo);
  }//setOpTempo
	
  public void setTimeSpan(TimeSpan ts)
  {
    theTimeSpan = ts;
  }
	
  public void setGeoLoc(GeolocLocation geoLoc) 
  {	
    this.geoLoc = geoLoc;	   
  }//setGeoLoc
	
  public GeolocLocation getGeoLoc()
  {
    return geoLoc;
  }	

  public String getActivityType() 
  {
    return (activityType);
  }//getActivityType

  public String getActivityName() 
  {
    return (activityName);
  }//getActivityName
	
  public void addActivityItem(String key, String value)
  { 
    // Does the key already exist
    if (oaHashMap.containsKey(key))
      System.err.println("OrgActivity:Key already in use.");
    else
      oaHashMap.put(unique(key), unique(value));    
  }//addItem
	 
  public void modifyActivityItem(String key, String value)
  {
    oaHashMap.put(unique(key), unique(value));
  }//modifyItem
  
  HashMap getOaHashMap() { return oaHashMap; }

  public String getActivityItem(String key)
  {
    return (String)oaHashMap.get(key);
  }//getItem
  
  public HashMap getItems()
  {
    return oaHashMap;
  }//getItems	 

  public String getOpTempo() 
  {
    return (opTempo);
  }//getOpTempo

  public TimeSpan getTimeSpan() 
  {
    return(theTimeSpan);
  }//getTimeSpan
	
  public long getStartTime() {
    return theTimeSpan.getStartTime();
  }

  public long getEndTime() {
    return theTimeSpan.getEndTime();
  }

  public void setAll(Transferable other) 
  {
	    
    if (!(other instanceof OrgActivity)) {
      throw new IllegalArgumentException("Parameter is not OrgActivity.");
    }
    else 
      {		    	    		    
        OrgActivity oa = (OrgActivity)other;
        activityName = oa.getActivityName();
        activityType = oa.getActivityType();
        opTempo = oa.getOpTempo();
        theTimeSpan = oa.getTimeSpan();
        orgID = oa.getOrgID();
        oplanUID = oa.getOplanUID();
        setUID(oa.getOrgActivityId());
        setOwner(oa.getOwner());
			
        geoLoc = oa.getGeoLoc();	
      }//  set all the values
	
  }// setAll

  public boolean same(Transferable other) 
  {
    if (other instanceof OrgActivity) {
      OrgActivity oa = (OrgActivity) other;
      return getUID().equals(oa.getUID());
    }
    return false;
  }//same

  private boolean matches(Object a, Object b) {
    return (a==null)?(b==null):(a.equals(b));
  }

  public boolean equals(Object o) {
    if (o instanceof OrgActivity) {
      OrgActivity oa = (OrgActivity) o;

      return
        matches(getActivityName(), oa.getActivityName()) &&
        matches(getActivityType(), oa.getActivityType()) &&
        matches(getOpTempo(), oa.getOpTempo()) &&
        matches(getTimeSpan(), oa.getTimeSpan()) &&
        matches(getGeoLoc(), oa.getGeoLoc()) &&
        matches(getOaHashMap(), oa.getOaHashMap()) &&
        matches(getOrgID(), oa.getOrgID()) &&
        matches(getOplanUID(), oa.getOplanUID()) &&
        matches(getOrgActivityId(), oa.getOrgActivityId());
    } else
      return false;
  }

  public Object clone() {
    //OrgActivity oa = new OrgActivity(activityType, activityName);
    OrgActivity oa = new OrgActivity(orgID, oplanUID);
    
    oa.setOpTempo(opTempo);
    oa.setActivityType(activityType);
    oa.setActivityName(activityName);
    oa.setUID(getUID());
    oa.setOwner(getOwner());

	
    if (oaHashMap != null)
      oa.oaHashMap = new HashMap((HashMap)oaHashMap.clone()); 	

    if (theTimeSpan != null)
      oa.setTimeSpan((TimeSpan)theTimeSpan.clone());
       
    if (geoLoc != null)
      oa.setGeoLoc((GeolocLocation)geoLoc.clone());	
    
    return oa;
  }//clone
  
  // 
  // XMLizable method for UI, other clients
  //
  public Element getXML(Document doc) {
    return XMLize.getPlanObjectXML(this,doc);
  }


  //dummy PropertyChangeSupport for the Jess Interpreter.
  protected transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl)   {
    pcs.removePropertyChangeListener(pcl);
  }

  public static final String unique(String s) {
    return (s==null)?null:(s.intern());
  }
  
  /** convert OPlan-centric location and timespan to 
   * standard ALPish (logplan) schedule element 
   * @return a LocationScheduleElement or null, if a locationscheduleelement 
   * cannot be constructed (e.g. no schedule, no location).
   **/
  public LocationScheduleElement getNormalizedScheduleElement() {
    // don't NPE on bogus OAs
    if (theTimeSpan == null || geoLoc == null) return null;
    
    return new OAScheduleElement(theTimeSpan.getStartTime(),
                                 theTimeSpan.getEndTime(),
                                 geoLoc,
                                 getUID());
  }

  /** LocationScheduleElement which is labelled with an associated 
   * OrgActivity UID so that it can be found and replaced or removed
   * later.
   **/
  public static final class OAScheduleElement extends TaggedLocationScheduleElement {
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

}// OrgActivity
