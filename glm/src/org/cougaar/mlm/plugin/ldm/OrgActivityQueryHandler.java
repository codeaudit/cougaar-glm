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

package org.cougaar.mlm.plugin.ldm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OplanFactory;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.OrgActivityImpl;
import org.cougaar.glm.ldm.oplan.TimeSpan;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.util.TimeSpanSet;

/** Reads OrgActivity for specified Oplan from a database table. Assumes it's 
 * being invoked on behalf of SQLOplanPlugin. Updates orgactivities maintained 
 * by SQLOplanPlugin.
 */

public class OrgActivityQueryHandler  extends SQLOplanQueryHandler {
  public static final String ACTIVITY_PARAMETER = "activity";
  public static final String OPTEMPO_PARAMETER = "opTempo";
  public static final String LOCATION_PARAMETER = "location";

  private static final String QUERY_NAME = "OrgActivityQuery";
  
  private static final int ACTIVITY_INDEX = 0;
  private static final int LOCATION_INDEX = 1;
  private static final int OPTEMPO_INDEX = 2;

  private Calendar myCalendar = Calendar.getInstance();

  private long myLastModifiedTime = TimeSpan.MIN_VALUE;
  private long myMaxModifiedTime = TimeSpan.MIN_VALUE;

  private HashMap myOrgInfoMap = new HashMap();	
  private Oplan myOplan;
  private String myActivityKey;
  private String myOpTempoKey;
  private String myLocationKey;
  

  /** this method is called before a query is started,
   * before even getQuery.  The default method is empty
   * but may be overridden.
   **/
  public void startQuery() {
    String oplanID = getParameter(OplanReaderPlugin.OPLAN_ID_PARAMETER);
    myOplan = myPlugin.getOplan(oplanID);
    
    if (myOplan == null) {
      throw new java.lang.IllegalArgumentException("OrgActivityQueryHandler.startQuery() - " +
                         " unable to get oplan info for " + oplanID);
    }

    myActivityKey = getParameter(ACTIVITY_PARAMETER);
    myOpTempoKey = getParameter(OPTEMPO_PARAMETER);
    myLocationKey = getParameter(LOCATION_PARAMETER);

    myOrgInfoMap.clear();
    myMaxModifiedTime = myLastModifiedTime;
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
    try {
      String relation = null;
      if (rowData[0] instanceof String)
	relation = (String) rowData[0];
      else
	relation = new String ((byte[])rowData[0],"US-ASCII");
    
      myMaxModifiedTime = Math.max(((Date) myOplan.getCday()).getTime(), myMaxModifiedTime);

      if (relation.equals(myOpTempoKey)) {
        processOpTempo(rowData);
      }else if (relation.equals(myActivityKey)) {
        processActivity(rowData);
      } else if (relation.equals(myLocationKey)) {
        processLocation(rowData);
      } else {
        System.err.println("OrgActivityQueryHandler.processRow(): unexpected text in RELATION_NAME column - " + relation);
        System.err.println("Ignoring row of data: " + rowData);
      }
    }catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
    }

  }
  


  /** this method is called when a query is complete, 
   * afer the last call to processRow. Updates OrgActivities maintained by
   * SQLOplanPlugin
   **/
  public void endQuery() {
    // myOrgInfoMap has a TimeSpanSet for each Org
    Collection orgInfosByOrg = myOrgInfoMap.values();
    ArrayList allOrgActivities = new ArrayList();
    OrgInfo myOrgInfo = null;
    for (Iterator iterator = orgInfosByOrg.iterator();
         iterator.hasNext();) {
      OrgInfo orgInfo = (OrgInfo) iterator.next();
      myOrgInfo = orgInfo;

      // Need to compute all org activities so I can update the 
      // oplan end day
      TimeSpanSet orgActivities = getMergedOrgActivities(orgInfo);
      allOrgActivities.addAll(orgActivities);
      if (needToUpdate(orgInfo)) {
        
        /* Debugging  */
        
        for (Iterator oaIterator = orgActivities.iterator();
             oaIterator.hasNext();) {
          OrgActivity orgActivity = (OrgActivity)oaIterator.next();
          System.out.println(orgActivity.getOrgID() + " " + 
                             orgActivity.getOpTempo() + " " + 
                             orgActivity.getActivityType() + " " + 
                             orgActivity.getTimeSpan().getStartDate() + " " +
                             orgActivity.getTimeSpan().getEndDate() + " " + 
                             orgActivity.getGeoLoc());
        }
        
        myPlugin.updateOrgActivities(myOplan, orgInfo.getOrgName(), 
                                     orgActivities);
      }
    }

    Date currentEndDay = myOplan.getEndDay();
    myOplan.inferEndDay(allOrgActivities);
    if ((currentEndDay == null) ||
        (!currentEndDay.equals(myOplan.getEndDay()))) {
      myPlugin.updateOplanInfo(myOplan);
    }


    myLastModifiedTime = myMaxModifiedTime;
  }


  protected void processOpTempo(Object[] rowData) {
    try {
      String orgName = null;
      String opTempo = null;
      if (rowData[1] instanceof String)
	orgName = (String)rowData[1];
      else
	orgName = new String ((byte[])rowData[1],"US-ASCII");

      if (rowData[3] instanceof String)
	opTempo = (String)rowData[3];
      else
	opTempo = new String ((byte[])rowData[3],"US-ASCII");

      if (!opTempo.equals(OrgActivity.HIGH_OPTEMPO) &&
          !opTempo.equals(OrgActivity.MEDIUM_OPTEMPO) &&
          !opTempo.equals(OrgActivity.LOW_OPTEMPO)) {
        System.err.println("OrgActivityQueryHandler: Unrecognized op tempo - " + 
                           opTempo + " - for " + orgName + " in " + myOplan);
      }
    
    
      OrgInfoElement element = 
        new OrgInfoElement(opTempo, 
                           System.currentTimeMillis(), 
                           myOplan.getCday(), 
                           ((Number) rowData[5]).intValue(), 
                           ((Number) rowData[6]).intValue());

      OrgInfo orgInfo = (OrgInfo) myOrgInfoMap.get(orgName);
      if (orgInfo == null) {
        orgInfo = new OrgInfo(orgName);
        myOrgInfoMap.put(orgName, orgInfo);
      } else {
        Collection opTempoCollection = 
          orgInfo.getOpTempoSchedule().intersectingSet(element.getStartTime(),
                                                       element.getEndTime());

        if (opTempoCollection.size() > 0) {
          System.err.println("OrgActivityQueryHandler: found more than one opTempo for " + 
                             orgName + " during " + 
                             new Date(element.getStartTime()) + " - " + 
                             new Date(element.getEndTime()));
        }
      }
      orgInfo.getOpTempoSchedule().add(element);

    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
    }
    
  }

  protected void processActivity(Object[] rowData) {
    try {
      String orgName = null;
      String activity = null;
      if (rowData[1] instanceof String)
	orgName = (String)rowData[1];
      else
	orgName = new String ((byte[])rowData[1],"US-ASCII");

      if (rowData[3] instanceof String)
	activity = (String) rowData[3];
      else
	activity = new String ((byte[])rowData[3],"US-ASCII");

      OrgInfoElement element = 
        new OrgInfoElement(activity, 
                           System.currentTimeMillis(),
                           myOplan.getCday(), 
                           ((Number) rowData[5]).intValue(), 
                           ((Number) rowData[6]).intValue());

      OrgInfo orgInfo = (OrgInfo) myOrgInfoMap.get(orgName);
      if (orgInfo == null) {
        orgInfo = new OrgInfo(orgName);
        myOrgInfoMap.put(orgName, orgInfo);
      } else {
        Collection activityCollection = 
          orgInfo.getActivitySchedule().intersectingSet(element.getStartTime(),
                                                        element.getEndTime());

        if (activityCollection.size() > 0) {
          System.err.println("OrgActivityQueryHandler: found more than one activity for " + 
                             orgName + " during " + 
                             new Date(element.getStartTime()) + " - " + 
                             new Date(element.getEndTime()));
        }
      }
      orgInfo.getActivitySchedule().add(element);
    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
    }
    
  }

  protected void processLocation(Object[] rowData) {
    try {
      String orgName = null;
      String locCode = null;
      if (rowData[1] instanceof String)
	orgName = (String)rowData[1];
      else
	orgName = new String ((byte[])rowData[1],"US-ASCII");

      if (rowData[3] instanceof String)
	locCode = (String)rowData[3];
      else
	locCode = new String ((byte[])rowData[3],"US-ASCII");

      // look up geoloc from location string
      GeolocLocation geoLoc = (GeolocLocation) myPlugin.getLocation(locCode);

      OrgInfoElement element = 
        new OrgInfoElement(geoLoc, 
                           System.currentTimeMillis(),
                           myOplan.getCday(), 
                           ((Number) rowData[5]).intValue(), 
                           ((Number) rowData[6]).intValue());

      OrgInfo orgInfo = (OrgInfo) myOrgInfoMap.get(orgName);
      if (orgInfo == null) {
        orgInfo = new OrgInfo(orgName);
        myOrgInfoMap.put(orgName, orgInfo);
      } else {
        Collection locationCollection = 
          orgInfo.getLocationSchedule().intersectingSet(element.getStartTime(),
                                                        element.getEndTime());

        if (locationCollection.size() > 0) {
          System.err.println("OrgActivityQueryHandler: found more than one location for " + 
                             orgName + " during " + 
                             new Date(element.getStartTime()) + " - " + 
                             new Date(element.getEndTime()));
        }
      }
      orgInfo.getLocationSchedule().add(element);
    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
    }

  }

  protected boolean needToUpdate(OrgInfo orgInfo) {
    return true;
//     for (Iterator iterator = orgInfo.getActivitySchedule().iterator();
//          iterator.hasNext();) {
//       OrgInfoElement element = (OrgInfoElement) iterator.next();
//       if (element.getLastModified() > myLastModifiedTime) {
//         return true;
//       }
//     }

//     for (Iterator iterator = orgInfo.getLocationSchedule().iterator();
//          iterator.hasNext();) {
//       OrgInfoElement element = (OrgInfoElement) iterator.next();
//       if (element.getLastModified() > myLastModifiedTime) {
//         return true;
//       }
//     }

//     for (Iterator iterator = orgInfo.getOpTempoSchedule().iterator();
//          iterator.hasNext();) {
//       OrgInfoElement element = (OrgInfoElement) iterator.next();
//       if (element.getLastModified() > myLastModifiedTime) {
//         return true;
//       }
//     }

//     return false;
  }

  /**
   * getMergedOrgActivities - merges the activity, location, and optempo 
   * TimeSpanSets for the Org into a set of OrgActivities.
   **/
  protected TimeSpanSet getMergedOrgActivities(OrgInfo orgInfo) {
    TimeSpanSet timeSpanSet = new TimeSpanSet();
    if (orgInfo != null) {
      Iterator activityIterator = orgInfo.getActivitySchedule().iterator();
      Iterator locationIterator = orgInfo.getLocationSchedule().iterator();
      Iterator opTempoIterator = orgInfo.getOpTempoSchedule().iterator();

      OrgInfoElement activity;
      if (activityIterator.hasNext()) {
        activity = (OrgInfoElement) activityIterator.next();
      } else {
        System.err.println("OrgActivityQueryHandler:getMergedOrgActivities()" +
                           " - no activities for " + orgInfo.getOrgName());
        return timeSpanSet;
      }

      OrgInfoElement location;
      if (locationIterator.hasNext()) {
        location = (OrgInfoElement) locationIterator.next();
      } else {
        System.err.println("OrgActivityQueryHandler:getMergedOrgActivities()" +
                           " - no location for " + orgInfo.getOrgName());
        return timeSpanSet;
      }

      OrgInfoElement opTempo;
      if (opTempoIterator.hasNext()) {
        opTempo = (OrgInfoElement) opTempoIterator.next();
      } else {
        System.err.println("OrgActivityQueryHandler:getMergedOrgActivities()" +
                           " - no optempo for " + orgInfo.getOrgName());
        return timeSpanSet;
      }


      ArrayList orgInfoElements = new ArrayList(3);

      orgInfoElements.add(ACTIVITY_INDEX, activity);
      orgInfoElements.add(LOCATION_INDEX, location);
      orgInfoElements.add(OPTEMPO_INDEX, opTempo);

      long start = getStart(TimeSpan.MIN_VALUE, orgInfoElements);
      long end = TimeSpan.MAX_VALUE;
      
      while (start != TimeSpan.MAX_VALUE) {
        end = getEnd(start, orgInfoElements);

        OrgActivity orgActivity = makeOrgActivity(orgInfo.getOrgName(),
                                                  start, end, activity, 
                                                  location, opTempo);
        /** Debugging**/
        if (orgActivity == null) {
          System.out.println(orgInfo.getOrgName() + ": activity row count = " + orgInfo.getActivitySchedule().size() +
                             " location row count = " + orgInfo.getLocationSchedule().size() +
                             " opTempo row count = " + orgInfo.getOpTempoSchedule().size());

          RuntimeException re = new RuntimeException();
          re.printStackTrace();
          throw re;
        }
        /****/
        timeSpanSet.add(orgActivity);

        activity = endsAfter(end, activity, activityIterator);
        orgInfoElements.set(ACTIVITY_INDEX, activity);

        location = endsAfter(end, location, locationIterator);
        orgInfoElements.set(LOCATION_INDEX, location);

        opTempo = endsAfter(end, opTempo, opTempoIterator);
        orgInfoElements.set(OPTEMPO_INDEX, opTempo);

        // Find start of next interval
        start = getStart(end, orgInfoElements);
 
        if ((start != TimeSpan.MAX_VALUE) &&
            (start > end)) {
          System.out.println("OrgActivityQueryHandler: found a schedule gap for " +
                             orgInfo.getOrgName() + 
                             " - gap between " + new Date(end) + " - " + new Date(start));
        }
      }
    }      
    return timeSpanSet;
  }

  private OrgActivity makeOrgActivity(String orgName,
                                      long startTime,
                                      long endTime,
                                      OrgInfoElement activity, 
                                      OrgInfoElement location, 
                                      OrgInfoElement opTempo) {

    OrgActivityImpl orgActivity = OplanFactory.newOrgActivity(orgName, myOplan.getUID());
    myComponent.getUIDServer().registerUniqueObject(orgActivity);
    orgActivity.setOwner(myMessageAddress);
    orgActivity.setTimeSpan(makeOplanTimeSpan(startTime, endTime));

    if ((activity != null) &&
        (intersect(orgActivity.getTimeSpan(), activity))) {
      orgActivity.setActivityType((String) activity.getObject());
    } else {
      System.err.println("OrgActivityQueryHandler.makeOrgActivity() - " +
                         " no activity for " + orgName + 
                         " from " + new Date(startTime) + 
                         " to " + new Date(endTime));
      return null;
    }

    if ((location != null) &&
        (intersect(orgActivity.getTimeSpan(), location))) {
      orgActivity.setGeoLoc((GeolocLocation) location.getObject());
    } else {
      System.err.println("OrgActivityQueryHandler.makeOrgActivity() - " +
                         " no location for " + orgName + 
                         " from " + new Date(startTime) + 
                         " to " + new Date(endTime));
      return null;
    }
    
    if ((opTempo != null) &&
        (intersect(orgActivity.getTimeSpan(), opTempo))) {
      orgActivity.setOpTempo((String) opTempo.getObject());
    } else {
      System.err.println("OrgActivityQueryHandler.makeOrgActivity() - " +
                         " no optempo for " + orgName + 
                         " from " + new Date(startTime) + 
                         " to " + new Date(endTime));
      return null;
    }
      
    return orgActivity; 
  }

  private static long getEnd(long start, Collection timeSpans) {
    long end = TimeSpan.MAX_VALUE;

    for (Iterator iterator = timeSpans.iterator(); 
         iterator.hasNext();) {
      Object o = iterator.next();
      if (o != null) {
        TimeSpan timeSpan = (TimeSpan) o;
        if (timeSpan.getStartTime() <= start) {
          end = Math.min(end, timeSpan.getEndTime());
        } else {
          end = Math.min(end, timeSpan.getStartTime());
        }
      }
    }

    return end;
  }

  // returns TimeSpan.MAX_VALUE if there isn't any more data
  private static long getStart(long end, Collection timeSpans) {
    long start = TimeSpan.MAX_VALUE;

    for (Iterator iterator = timeSpans.iterator(); 
         iterator.hasNext();) {
      Object o = iterator.next();
      if (o != null) {
        TimeSpan timeSpan = (TimeSpan) o;
        if (timeSpan.getStartTime() <  end) {
          start = Math.min(start, end);
        } else {
          start = Math.min(start, timeSpan.getStartTime());
        }
      }
    }

    return start;
  }


  private static boolean intersect(TimeSpan ts1, TimeSpan ts2) {
    return ((ts1 != null) &&
            (ts2 != null) &&
            (ts1.getStartTime() < ts2.getEndTime()) &&
            (ts1.getEndTime() > ts2.getStartTime()));
  }

  private TimeSpan makeOplanTimeSpan(long startTime, long endTime) {
    Date startDate = normalize(new Date(startTime));
    Date endDate = normalize(new Date(endTime));
    Date cDate = normalize(myOplan.getCday());
    
    // Find cDate -> startDate
    int startDelta = 0;
    for (myCalendar.setTime(cDate); 
         myCalendar.getTime().getTime() < startDate.getTime(); 
         startDelta++) {
      myCalendar.add(Calendar.DATE, 1);
    }

    int endDelta = startDelta;
    for (myCalendar.setTime(startDate); 
         myCalendar.getTime().getTime() < endDate.getTime(); 
         endDelta++) {
      myCalendar.add(Calendar.DATE, 1);
    }

    return new org.cougaar.glm.ldm.oplan.TimeSpan(myOplan.getCday(), 
                                                         startDelta, 
                                                         endDelta);
  }
  
  private Date normalize(Date date) {
    myCalendar.setTime(date);
    int year = myCalendar.get(Calendar.YEAR);
    int month = myCalendar.get(Calendar.MONTH);
    int day = myCalendar.get(Calendar.DATE);

    // set to midnight
    myCalendar.set(year, month, day, 0, 0);
    return myCalendar.getTime();
  }


  // Presumes that that OrgInfoElements are sorted by TimeSpan and non 
  // overlapping
  private OrgInfoElement endsAfter(long start, OrgInfoElement current, 
                                   Iterator iterator) {

    if (current == null) {
      // We've already walked the entire set.
      return current;
    }

    if (current.getEndTime() <= start) {
      while (iterator.hasNext()) {
        OrgInfoElement next = (OrgInfoElement) iterator.next();
        if (next.getEndTime() > start) {
          return next;
        }
      }
      return null;
    } else {
      return current;
    }
  }

  private class OrgInfo {
    
    private String myOrgName;
    private TimeSpanSet myOpTempoSchedule;
    private TimeSpanSet myActivitySchedule;
    private TimeSpanSet myLocationSchedule;

    public OrgInfo(String orgName) {
      myOrgName = orgName;

      myOpTempoSchedule = new TimeSpanSet();
      myActivitySchedule = new TimeSpanSet();
      myLocationSchedule = new TimeSpanSet();
    }

    public String getOrgName() {
      return myOrgName;
    }

    public TimeSpanSet getOpTempoSchedule() {
      return myOpTempoSchedule;
    }

    public TimeSpanSet getActivitySchedule() {
      return myActivitySchedule;
    }

    public TimeSpanSet getLocationSchedule() {
      return myLocationSchedule;
    }
  }
  

  private class OrgInfoElement extends org.cougaar.glm.ldm.oplan.TimeSpan {
    private Object myObject;
    private long myLastModified;

    public OrgInfoElement(Object object, long lastModified, Date baseDate, int startDelta, int endDelta) {
      super(baseDate, startDelta, endDelta);

      myObject = object;
      myLastModified = lastModified;
    }

    public Object getObject() {
      return myObject;
    }

    public long getLastModified() {
      return myLastModified;
    }
  }
}
