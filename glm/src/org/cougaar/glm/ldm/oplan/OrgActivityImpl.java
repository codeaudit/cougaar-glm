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
package org.cougaar.glm.ldm.oplan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.plan.LocationScheduleElement;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

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
public class OrgActivityImpl extends OwnedUniqueObject
  implements OrgActivity
{	
  private static final Logger logger = Logging.getLogger(OrgActivity.class);

  private String activityName;
  private String activityType;
  private String opTempo;
  private TimeSpan theTimeSpan;	
  private GeolocLocation geoLoc;
  private HashMap oaHashMap = new HashMap(5);
  private String orgID;
  private UID oplanUID;
  private String adCon;
  private String opCon;
  
  OrgActivityImpl(String orgID, UID oplanUID) 
  {	
    this.orgID = unique(orgID);
    this.oplanUID = oplanUID;	
  }
	
  OrgActivityImpl(String activityType, String activityName, String orgID, UID oplanUID) 
  {
    this.activityType = unique(activityType);
    this.activityName = unique(activityName);
    this.orgID = unique(orgID);   
    this.oplanUID = oplanUID;
  }

  public void setActivityType(String activityType) 
  {
    this.activityType = unique(activityType);	   
  }
	
  public void setActivityName(String activityName) 
  {
    this.activityName = unique(activityName);
  }
	
  public void setOrgID(String orgID)
  {
    this.orgID = unique(orgID);
  }

  public void setOrgActivityId(UID uid)
  {

  }

  public UID getOrgActivityId()
  {
    return getUID();
  }

  public String getOpCon() {
    return opCon;
  }
  public void setOpCon(String opCon) {
    this.opCon = opCon;
  }

  public String getAdCon() {
    return adCon;
  }
  public void setAdCon(String adCon) {
    this.adCon = adCon;
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
  }
	
  public void setTimeSpan(TimeSpan ts)
  {
    theTimeSpan = ts;
  }
	
  public void setGeoLoc(GeolocLocation geoLoc) 
  {	
    this.geoLoc = geoLoc;	   
  }
	
  public GeolocLocation getGeoLoc()
  {
    return geoLoc;
  }	

  public String getActivityType() 
  {
    return activityType;
  }

  public String getActivityName() 
  {
    return activityName;
  }
	
  public void addActivityItem(String key, String value)
  { 
    // Does the key already exist
    if (oaHashMap.containsKey(key)) {
      logger.warn("OrgActivity:Key already in use: "+key, new Throwable());
    } else {
      oaHashMap.put(unique(key), unique(value));
    }
  }
	 
  public void modifyActivityItem(String key, String value)
  {
    oaHashMap.put(unique(key), unique(value));
  }
  
  public String getActivityItem(String key)
  {
    return (String)oaHashMap.get(key);
  }
  
  public HashMap getItems()
  {
    return oaHashMap;
  }

  public String getOpTempo() 
  {
    return opTempo;
  }

  public TimeSpan getTimeSpan() 
  {
    return theTimeSpan;
  }
	
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
    } else {		    	    		    
      OrgActivity oa = (OrgActivity)other;
      activityName = oa.getActivityName();
      activityType = oa.getActivityType();
      opTempo = oa.getOpTempo();
      theTimeSpan = oa.getTimeSpan();
      orgID = oa.getOrgID();
      oplanUID = oa.getOplanUID();
      adCon = oa.getAdCon();
      opCon = oa.getOpCon();
      setUID(oa.getOrgActivityId());
      if (oa instanceof OwnedUniqueObject) {
        setOwner(((OwnedUniqueObject)oa).getOwner());
      }
      geoLoc = oa.getGeoLoc();	
    }
  }

  public boolean same(Transferable other) 
  {
    if (other instanceof OrgActivity) {
      OrgActivity oa = (OrgActivity) other;
      return getUID().equals(oa.getUID());
    }
    return false;
  }

  public boolean equals(Object o) {
    if (o instanceof OrgActivity) {
      OrgActivity oa = (OrgActivity) o;
      boolean status = 
        matches(getActivityName(), oa.getActivityName()) &&
        matches(getActivityType(), oa.getActivityType()) &&
        matches(getOpTempo(), oa.getOpTempo()) &&
        matches(getTimeSpan(), oa.getTimeSpan()) &&
        matches(getGeoLoc(), oa.getGeoLoc()) &&
        matches(getOrgID(), oa.getOrgID()) &&
        matches(getOplanUID(), oa.getOplanUID()) &&
        matches(getAdCon(), oa.getAdCon()) &&
        matches(getOpCon(), oa.getOpCon()) &&
        matches(getOrgActivityId(), oa.getOrgActivityId());

      return status;
    } else {
      return false;
    }
  }

  public Object clone() {
    OrgActivityImpl oa = new OrgActivityImpl(orgID, oplanUID);
    
    oa.setOpTempo(opTempo);
    oa.setActivityType(activityType);
    oa.setActivityName(activityName);
    oa.setUID(getUID());
    oa.setOwner(getOwner());
    oa.setAdCon(getAdCon());
    oa.setOpCon(getOpCon());
	
    if (oaHashMap != null) oa.oaHashMap = new HashMap((HashMap)oaHashMap.clone()); 	

    if (theTimeSpan != null) oa.setTimeSpan((TimeSpan)theTimeSpan.clone());
       
    if (geoLoc != null) oa.setGeoLoc((GeolocLocation)geoLoc.clone());	
    
    return oa;
  }
  
  //dummy PropertyChangeSupport for the Jess Interpreter.
  protected transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }
  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    pcs.removePropertyChangeListener(pcl);
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


  private static boolean matches(Object a, Object b) {
    return (a==null)?(b==null):(a.equals(b));
  }
  public static final String unique(String s) {
    return (s==null)?null:(s.intern());
  }
}
