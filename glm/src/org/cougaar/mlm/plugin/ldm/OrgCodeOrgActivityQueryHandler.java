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

package org.cougaar.mlm.plugin.ldm;
//package org.cougaar.logistics.plugin.asset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.TimeSpan;
import org.cougaar.util.DBProperties;
import org.cougaar.util.TimeSpanSet;

/** Reads OrgActivity for specified Oplan from a database table. Assumes it's 
 * being invoked on behalf of SQLOplanPlugin. Updates orgactivities maintained 
 * by SQLOplanPlugin.
 */

public class OrgCodeOrgActivityQueryHandler extends NewOrgActivityQueryHandler {

  private static final int ORG_CODE_COLUMN      = 7;
  public static final String ORG_CODE = ":org_code:";

  public OrgCodeOrgActivityQueryHandler(DBProperties adbp, NewOplanPlugin plugin)
    throws SQLException
  {
    super(adbp, plugin);
  }

  public Collection executeQueries(Statement statement) throws SQLException {

    String myOrgCode = plugin.getOrgCode();  

    String myRollups = computeAllRollupSuperiors(myOrgCode);
    dbp.put(ORG_CODE, myRollups);

    String oplanID = dbp.getProperty(OplanReaderPlugin.OPLAN_ID_PARAMETER);
    String query = dbp.getQuery(QUERY_NAME, dbp);
    String orgId = null;
    ResultSet rs = statement.executeQuery(query);
    TimeSpanSet opTempoSchedule = new TimeSpanSet();
    TimeSpanSet activitySchedule = new TimeSpanSet();
    TimeSpanSet locationSchedule = new TimeSpanSet();
    TimeSpanSet schedule;
    boolean firstTimeThru = true;
    String org_code = "";
    while (rs.next()) {
      if (firstTimeThru) {
        org_code = getString(rs, ORG_CODE_COLUMN); //get first org_code
        firstTimeThru = false;
      }
      if (logger.isDebugEnabled()) {
	  logger.debug("Querying for OrgActivities for " + myOrgCode + ", got result: OrgID=" + getString(rs, ORG_ID) + ": " + getString(rs, ATTRIBUTE_NAME) + ", " + getString(rs, ATTRIBUTE_VALUE) + ", Start: " + rs.getDouble(START_HOUR) + ", End: " + rs.getDouble(END_HOUR) + ", UIC: " + getString(rs, ORG_CODE_COLUMN));
      }
      if (! org_code.equals(getString(rs, ORG_CODE_COLUMN))) { //changed to next org_code 
        break;
      }
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
      } else {
        if (logger.isErrorEnabled()) {
          logger.error("OrgActivityQueryHandler.processRow(): unexpected text in ATTRIBUTE_NAME column - " + attribute_name);
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
          logger.debug("OrgActivityQueryHandler: Adding " + element);
        }
        schedule.add(element);
      } else {
        if (logger.isErrorEnabled()) {
          logger.error("OrgActivityQueryHandler: found more than one "
                       + attribute_value + " during "
                       + formatDate(element.getStartTime())
                       + " - "
                       + formatDate(element.getEndTime()));
        }
      }
    }
    TimeSpanSet timeSpanSet = new TimeSpanSet();
    OrgInfoElements[] oiElements = {
      new OrgInfoElements(activitySchedule, "activity"),
      new OrgInfoElements(locationSchedule, "location"),
      new OrgInfoElements(opTempoSchedule, "opTempo")
    };

    long minStart = TimeSpan.MIN_VALUE;
    while (true) {
      long start = minStart;
      long end = TimeSpan.MAX_VALUE;
      int missing = 0;
      int present = 0;
      // Step all iterators/elements
      for (int i = 0; i < oiElements.length; i++) {
        OrgInfoElement element = oiElements[i].endsAfter(minStart);
        if (element == null) {
          missing++;
        } else {
          present++;
          end = Math.min(end, element.getEndTime());
          start = Math.max(start, element.getStartTime());
        }
      }
      if (missing > 0) {
        if (logger.isWarnEnabled() && present > 0) {
          StringBuffer msg = new StringBuffer();
          msg.append("OrgActivityQueryHandler: missing ");
          boolean first = true;
          for (int i = 0; i < oiElements.length; i++) {
            if (oiElements[i].isFinished()) {
              if (first) {
                first = false;
              } else {
                msg.append(", ");
              }
              msg.append(oiElements[i].getType());
            }
          }
          msg.append(" between ")
            .append(formatDate(start))
            .append(" - ")
            .append(formatDate(end));
          logger.warn(msg.toString());
        }
        return timeSpanSet;
      }
      if (start > minStart && minStart > TimeSpan.MIN_VALUE) {
        if (logger.isInfoEnabled()) {
          logger.info("OrgActivityQueryHandler: schedule gap between "
                      + formatDate(minStart)
                      + " - "
                      + formatDate(start));
        }
      }
      OrgActivity orgActivity =
        plugin.makeOrgActivity(new TimeSpan(start, end),
                               oiElements[ACTIVITY_INDEX].getStringObject(), 
                               oiElements[LOCATION_INDEX].getStringObject(),
                               oiElements[OPTEMPO_INDEX].getStringObject());
      timeSpanSet.add(orgActivity);
      minStart = end;
    }
  }

  // OrgCodes reflect hierarchy of agents in length of the code.
  // So my superiors code + 1 char at end is my code.
  // Hence I can get all of my superiors by iteratively chopping
  // off the last char on my orgCode
  // Use this to find OrgActivities of my superiors, in case I dont have any
  private String computeAllRollupSuperiors(String myOrgCode) {
    StringBuffer sb = new StringBuffer("'" + myOrgCode + "'");
    
    // Spot to chop of String
    int chopLen = myOrgCode.length() - 1;
    // So we never enter this loop if orgCode was only 1 char
    while (chopLen > 0) {
      //shorten orgCode by 1 each time to get next rollup
      String nextRollup = myOrgCode.substring(0, chopLen);
      sb.append(", '" + nextRollup + "'");
      chopLen--;
    }
    return sb.toString();
  }

}
