/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import java.util.Date;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.core.cluster.Subscriber;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.util.Parameters;
import org.cougaar.util.TimeSpanSet;

import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OrgActivity;
import org.cougaar.domain.glm.ldm.oplan.TimeSpan;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

/** Reads OrgActivity for specified Oplan from a database table. Assumes it's 
 * being invoked on behalf of SQLOplanPlugIn. Updates orgactivities maintained 
 * by SQLOplanPlugIn.
 */

public class OrgActivityQueryHandler  extends SQLOplanQueryHandler {
  public static final String ACTIVITY_PARAMETER = "activity";
  public static final String OPTEMPO_PARAMETER = "opTempo";
  public static final String LOCATION_PARAMETER = "location";

  private static final String QUERY_NAME = "OrgActivityQuery";

  private HashMap myOrgActivityMap;	
  private Oplan myOplan;
  private String myActivityKey;
  private String myOpTempoKey;
  private String myLocationKey;
  

  /** this method is called before a query is started,
   * before even getQuery.  The default method is empty
   * but may be overridden.
   **/
  public void startQuery() {
    String oplanID = getParameter(SQLOplanPlugIn.OPLAN_ID_PARAMETER);
    myOplan = myPlugIn.getOplan(oplanID);
    
    if (myOplan == null) {
      // error out
    }

    myActivityKey = getParameter(ACTIVITY_PARAMETER);
    myOpTempoKey = getParameter(OPTEMPO_PARAMETER);
    myLocationKey = getParameter(LOCATION_PARAMETER);
  }
                        
  /** Construct and return an SQL query to be used by the Database engine.
   * Subclasses are required to implement this method.
   **/
  public String getQuery() { 
    return (String) getParameter(QUERY_NAME);
  }
    
  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  public void processRow(Object[] rowData) {
    if (rowData.length != 7) {
      System.err.println("OrgActivityQueryHandler.processRow() - expected 7 columns of data, " +
                         " got " + rowData.length);
    }

    String relation = (String) rowData[0];

    if (relation.equals(myOpTempoKey)) {
      processOpTempo(rowData);
    }else if (relation.equals(myActivityKey)) {
      processActivity(rowData);
    } else if (relation.equals(myLocationKey)) {
      processLocation(rowData);
    } else {
      System.err.println("OrgActivityQueryHandler.processRow(): unexpected text in RELATION_NAME column 6 columns of data - " + relation);
      System.err.println("Ignoring row of data: " + rowData);
    }
  }
  


  /** this method is called when a query is complete, 
   * afer the last call to processRow. Updates OrgActivities maintained by
   * SQLOplanPlugIn
   **/
  public void endQuery() {
    // myOrgActivityMap has a TimeSpanSet for each Org
    Collection orgActivitiesByOrg = myOrgActivityMap.values();

    ArrayList allOrgActivities = new ArrayList(orgActivitiesByOrg.size());
    for (Iterator iterator = orgActivitiesByOrg.iterator();
         iterator.hasNext();) {
      allOrgActivities.addAll((Collection) iterator.next());
    }

    /* Debugging 
    for (Iterator iterator = allOrgActivities.iterator();
         iterator.hasNext();) {
      OrgActivity orgActivity = (OrgActivity)iterator.next();
      System.out.println(orgActivity.getOrgID() + " " + 
                         orgActivity.getOpTempo() + " " + 
                         orgActivity.getActivityType() + " " + 
                         orgActivity.getTimeSpan().getStartDate() + " " +
                         orgActivity.getTimeSpan().getThruDate() + " " + 
                         new Date(orgActivity.getTimeSpan().getEndTime()) + " " + 
                         orgActivity.getGeoLoc());
    }
    */

    myOplan.setOrgActivities(allOrgActivities);
  }


  protected void processOpTempo(Object[] rowData) {
    String orgName = (String) rowData[1];
    String opTempo = (String) rowData[3];

    if (!opTempo.equals(OrgActivity.HIGH_OPTEMPO) &&
        !opTempo.equals(OrgActivity.MEDIUM_OPTEMPO) &&
        !opTempo.equals(OrgActivity.LOW_OPTEMPO)) {
      System.err.println("OrgActivityQueryHandler: Unrecognized op tempo - " + 
                         opTempo + " for " + orgName + " in " + myOplan);
    }
    
    org.cougaar.domain.glm.ldm.oplan.TimeSpan timeSpan = 
      new TimeSpan(myOplan.getCday(), 
                   ((Number) rowData[5]).intValue(), 
                   ((Number) rowData[6]).intValue());

    getOrgActivity(orgName, timeSpan).setOpTempo(opTempo);
  }

  protected void processActivity(Object[] rowData) {
    String orgName = (String) rowData[1];
    String activity = (String) rowData[3];

    // validate string?
    
    org.cougaar.domain.glm.ldm.oplan.TimeSpan timeSpan = 
      new TimeSpan(myOplan.getCday(), 
                   ((Number) rowData[5]).intValue(), 
                   ((Number) rowData[6]).intValue());

    getOrgActivity(orgName, timeSpan).setActivityType(activity);
  }

  protected void processLocation(Object[] rowData) {
    String orgName = (String) rowData[1];
    String locCode = (String) rowData[3];

    // look up geoloc from location string
    GeolocLocation geoLoc = (GeolocLocation) myPlugIn.getLocation(locCode);

    org.cougaar.domain.glm.ldm.oplan.TimeSpan timeSpan = 
      new TimeSpan(myOplan.getCday(), 
                   ((Number) rowData[5]).intValue(), 
                   ((Number) rowData[6]).intValue());
    
    getOrgActivity(orgName, timeSpan).setGeoLoc(geoLoc);
  }

  private OrgActivity getOrgActivity(String orgName, 
                                     org.cougaar.domain.glm.ldm.oplan.TimeSpan timeSpan) {
    if (myOrgActivityMap == null) {
      myOrgActivityMap = new HashMap();
    }

    TimeSpanSet orgActivities = (TimeSpanSet)myOrgActivityMap.get(orgName);
    if (orgActivities == null) {
      orgActivities = new TimeSpanSet();
      myOrgActivityMap.put(orgName, orgActivities);
    } 

    Collection oaCollection = 
      orgActivities.intersectingSet(timeSpan.getStartTime(),
                                    timeSpan.getEndTime());
    OrgActivity orgActivity;
    
    if (oaCollection.size() > 0) {
      if (oaCollection.size() != 1) {
        System.err.println("OrgActivityQueryHandler.getOrgActivity: found more than match for " + 
                           orgName + " during " + 
                           timeSpan.getStartTime() + " - " + 
                           timeSpan.getEndTime());
      }
      orgActivity = (OrgActivity)oaCollection.iterator().next();
    } else {
      orgActivity = new OrgActivity(orgName, myOplan.getUID());
      myComponent.getUIDServer().registerUniqueObject(orgActivity);
      orgActivity.setOwner(myClusterIdentifier);
      orgActivity.setTimeSpan(timeSpan);
      orgActivities.add(orgActivity);
    }

    return orgActivity;
  }
}







