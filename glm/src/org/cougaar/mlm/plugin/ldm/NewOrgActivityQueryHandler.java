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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.TimeSpan;
import org.cougaar.util.DBProperties;
import org.cougaar.util.TimeSpanSet;

/** Reads OrgActivity for specified Oplan from a database table. Assumes it's 
 * being invoked on behalf of SQLOplanPlugin. Updates orgactivities maintained 
 * by SQLOplanPlugin.
 */

public class NewOrgActivityQueryHandler extends NewQueryHandler {
  public static final String ACTIVITY_PARAMETER = "activity";
  public static final String OPTEMPO_PARAMETER = "opTempo";
  public static final String LOCATION_PARAMETER = "location";
  public static final String OPCON_PARAMETER = "opCon";

  protected static final String QUERY_NAME = "OrgActivityQuery";

  protected static final int ATTRIBUTE_NAME  = 1;
  protected static final int ORG_ID          = 2;
  protected static final int ATTRIBUTE_VALUE = 3;
  protected static final int START_HOUR      = 4;
  protected static final int END_HOUR        = 5;
  protected static final int STAGE_NUM       = 6;
  
  protected static final int ACTIVITY_INDEX = 0;
  protected static final int LOCATION_INDEX = 1;
  protected static final int OPTEMPO_INDEX = 2;
  protected static final int OPCON_INDEX = 3;

  protected String myActivityKey;
  protected String myOpTempoKey;
  protected String myLocationKey;
  protected String myOpConKey;

  public NewOrgActivityQueryHandler(DBProperties adbp, NewOplanPlugin plugin)
    throws SQLException
  {
    super(adbp, plugin);
    myActivityKey = dbp.getProperty(ACTIVITY_PARAMETER);
    myOpTempoKey = dbp.getProperty(OPTEMPO_PARAMETER);
    myLocationKey = dbp.getProperty(LOCATION_PARAMETER);
    myOpConKey = dbp.getProperty(OPCON_PARAMETER);
  }
  
  public Collection executeQueries(Statement statement) throws SQLException {
    String oplanID = dbp.getProperty(OplanReaderPlugin.OPLAN_ID_PARAMETER);
    String query = dbp.getQuery(QUERY_NAME, dbp);
    String orgId = null;
    ResultSet rs = statement.executeQuery(query);
    TimeSpanSet opTempoSchedule = new TimeSpanSet();
    TimeSpanSet activitySchedule = new TimeSpanSet();
    TimeSpanSet locationSchedule = new TimeSpanSet();
    TimeSpanSet opConSchedule = new TimeSpanSet();
    TimeSpanSet schedule;
    boolean haveOpCon = false;
    while (rs.next()) {
      String attribute_name = getString(rs, ATTRIBUTE_NAME);
      String attribute_value = getString(rs, ATTRIBUTE_VALUE);
      if (orgId == null) orgId = getString(rs, ORG_ID);
      if (attribute_name.equals(myOpTempoKey)) {
        schedule = opTempoSchedule;
        if (!attribute_value.equals(OrgActivity.HIGH_OPTEMPO) &&
            !attribute_value.equals(OrgActivity.MEDIUM_OPTEMPO) &&
            !attribute_value.equals(OrgActivity.LOW_OPTEMPO)) {
          throw new SQLException("Unrecognized op tempo " + attribute_value);
        }
      } else if (attribute_name.equals(myActivityKey)) {
        schedule = activitySchedule;
      } else if (attribute_name.equals(myLocationKey)) {
        schedule = locationSchedule;
      } else if (attribute_name.equals(myOpConKey)) {
        haveOpCon = true;
        schedule = opConSchedule;
      } else {
        if (logger.isErrorEnabled()) {
          logger.error("NewOrgActivityQueryHandler.processRow(): unexpected text in ATTRIBUTE_NAME column - " + attribute_name);
        }
        continue;
      }
      OrgInfoElement element =
        new OrgInfoElement(attribute_value, 
                           plugin.getCDay().getTime(), 
                           rs.getDouble(START_HOUR),
                           rs.getDouble(END_HOUR));
      if (schedule.intersectingSet(element).isEmpty()) {
        if (logger.isDebugEnabled()) {
          logger.debug("NewOrgActivityQueryHandler: Adding " + element);
        }
        schedule.add(element);
      } else {
        if (logger.isErrorEnabled()) {
          logger.error("NewOrgActivityQueryHandler: found more than one "
                       + attribute_value + " during "
                       + formatDate(element.getStartTime())
                       + " - "
                       + formatDate(element.getEndTime()));
        }
      }
    }
    //now each schedule has the set of values for each timespan for a given part of the OA
    
    TimeSpanSet timeSpanSet = new TimeSpanSet();    

    //opConSchedule might be empty
      OrgInfoElements[] oiElements = {
        new OrgInfoElements(activitySchedule, "activity"),
        new OrgInfoElements(locationSchedule, "location"),
        new OrgInfoElements(opTempoSchedule, "opTempo"),
        new OrgInfoElements(opConSchedule, "opCon")  
      };

    long minStart = TimeSpan.MIN_VALUE;
    while (true) {
      long start = minStart;
      long end = TimeSpan.MAX_VALUE;
      int missing = 0;
      int present = 0;
      // Step all iterators/elements
      // Need to pull out the values from each type of OA element for each necessary timespan
      for (int i = 0; i < oiElements.length; i++) {
        OrgInfoElement element = oiElements[i].endsAfter(minStart);
        if (element == null) {
          missing++;
          if (logger.isDebugEnabled()) {
            logger.debug("NOAQH: missing = " +missing +" and haveOpCon value is: " +haveOpCon);
          }
        } else {
          present++;
          if (logger.isDebugEnabled()) {
            logger.debug("NOAQH: Element is: " + element);
            logger.debug("NOAQH: present = " +present );
          }
          end = Math.min(end, element.getEndTime()); 
          start = Math.max(start, element.getStartTime());
          if (logger.isDebugEnabled()) {
            logger.debug("end value is " + new Date(end));
            logger.debug("start value is " + new Date(start));
          }
         }
      }

      if (missing > 1) {
        //if the missing element is the OPCON its OK
        return timeSpanSet;
      }

//       if (missing > 0 ) {
//         if (logger.isWarnEnabled() && present > 0) {
//           StringBuffer msg = new StringBuffer();
//           msg.append("NewOrgActivityQueryHandler: missing ");
//           boolean first = true;
//           for (int i = 0; i < oiElements.length; i++) {
//             if (oiElements[i].isFinished()) {
//               if (first) {
//                 first = false;
//               } else {
//                 msg.append(", ");
//               }
//               msg.append(oiElements[i].getType());
//             }
//           }
//           msg.append(" between ")
//             .append(formatDate(start))
//             .append(" - ")
//             .append(formatDate(end));
//           logger.warn(msg.toString());
//         }
//         return timeSpanSet;
//       }

      if (start > minStart && minStart > TimeSpan.MIN_VALUE) {
        if (logger.isInfoEnabled()) {
          logger.info("NewOrgActivityQueryHandler: schedule gap between "
                      + formatDate(minStart)
                      + " - "
                      + formatDate(start));
        }
      }

      String myOpCon = null;
      if (haveOpCon) {
        myOpCon = oiElements[OPCON_INDEX].getStringObject();
      }
      OrgActivity orgActivity =
        plugin.makeOrgActivity(new TimeSpan(start, end),
                               oiElements[ACTIVITY_INDEX].getStringObject(), 
                               oiElements[LOCATION_INDEX].getStringObject(),
                               oiElements[OPTEMPO_INDEX].getStringObject(),
                               myOpCon);
      timeSpanSet.add(orgActivity);
      minStart = end;
    }
  }

  protected static class OrgInfoElements {
    Iterator iterator;
    OrgInfoElement current = null;
    String type;
    OrgInfoElements(TimeSpanSet tss, String type) {
      iterator = tss.iterator();
      this.type = type;
    }

    public boolean isFinished() {
      return current == null && !iterator.hasNext();
    }

    public OrgInfoElement endsAfter(long start) {
      while (current == null || current.getEndTime() <= start) { 
        if (iterator.hasNext()) {
          current = (OrgInfoElement) iterator.next();
        } else {
          return null;          // Exhausted
        }
      }
      return current;
    }

    public String getStringObject() {
      return current.getStringObject();
    }

    public String getType() {
      return type;
    }
  }

  protected static class OrgInfoElement implements org.cougaar.util.TimeSpan {
    private Object myObject;
    private TimeSpan timeSpan;

    public OrgInfoElement(Object object, long baseTime, double startDelta, double endDelta) {
      long startTime = baseTime + (long) (startDelta * 3600000.0);    //milliseconds per hour
      long endTime = baseTime + (long) (endDelta * 3600000.0);
      timeSpan = new TimeSpan(startTime, endTime);
      myObject = object;
    }

    public TimeSpan getTimeSpan() {
      return timeSpan;
    }

    public long getStartTime() {
      return timeSpan.getStartTime();
    }

    public long getEndTime() {
      return timeSpan.getEndTime();
    }

    public String getStringObject() {
      return (String) myObject;
    }

    public String toString() {
      return myObject + "(" + timeSpan + ")";
    }
  }
  protected static SimpleDateFormat dateFormat =
    new SimpleDateFormat("MM/dd/yy HH:mm");

  protected static String formatDate(long time) {
    return formatDate(new Date(time));
  }
  protected static String formatDate(Date date) {
    return dateFormat.format(date);
  }
}
