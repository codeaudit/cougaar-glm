/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/util/Attic/GLMMeasure.java,v 1.5 2001-08-22 20:27:30 mthome Exp $ */
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

package org.cougaar.domain.glm.util;


import org.cougaar.domain.planning.ldm.Factory;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Distance;

import org.cougaar.domain.glm.ldm.GLMFactory;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.NewGeolocLocation;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.cougaar.lib.util.UTILPlugInException;

/**
 * This class contains utility functions for measurements.
 */

public class GLMMeasure {
  private static String myName = "GLMMeasure";

  // This section is for calculating great-circle distances for timing estimates
  // useful constants
  private static double EARTH_RADIUS = 3437.75d; // nmi
  private static double DEGREES_TO_RADIANS = (3.1415927d/180.0d);

  private static boolean debug = false;
  public static void setDebug (boolean dbg) { debug = dbg; }

  /**
   * Utility function to calculate the distance between two locations
   * @param start GeolocLocation starting point
   * @param end GeolocLocation ending point
   * @return Distance between the two points
   */
  public static Distance distanceBetween(GeolocLocation start, GeolocLocation end) {
    return GLMMeasure.distanceBetween(start, end, 1.0);
  }  

  /**
   * Utility function to calculate the distance between two locations
   * @param start GeolocLocation starting point
   * @param end GeolocLocation ending point
   * @param multiplier Multiplier for the final dist result
   * @return Distance between the two points
   */
  public static Distance distanceBetween(GeolocLocation start, GeolocLocation end, double multiplier) {

    if (start == null)
      throw new UTILPlugInException("start geoloc is null");
    if (end == null)
      throw new UTILPlugInException("end geoloc is null");

    // get Long/Lat
    Longitude startlong = start.getLongitude();
    Latitude  startlat  = start.getLatitude();
    Longitude endlong   = end.getLongitude();
    Latitude  endlat    = end.getLatitude();

    if (startlong == null)
      throw new UTILPlugInException("startlong is null in start GeolocLocation");
    if (startlat == null)
      throw new UTILPlugInException("startlat is null in start  GeolocLocation");
    if (endlong == null)
      throw new UTILPlugInException("endlong is null in end GeolocLocation");
    if (endlat == null)
      throw new UTILPlugInException("endlat is null in end GeolocLocation");
    if ((startlong.getDegrees () == 0.0d) && (startlat.getDegrees () == 0.0d))
      System.out.println ("distanceBetween - Geoloc " + start + " has lat = lon = 0.0?");
    if ((endlong.getDegrees () == 0.0d) && (endlat.getDegrees () == 0.0d))
      System.out.println ("distanceBetween - Geoloc " + end + " has lat = lon = 0.0?");

    // get radian measures
    double startlongrad = (startlong.getDegrees()*DEGREES_TO_RADIANS);
    double startlatrad = (startlat.getDegrees()*DEGREES_TO_RADIANS);
    double endlongrad = (endlong.getDegrees()*DEGREES_TO_RADIANS);
    double endlatrad  = (endlat.getDegrees()*DEGREES_TO_RADIANS);

    // calculate distance
    double deltalong = startlongrad - endlongrad;
    double distinradians = Math.acos(Math.sin(startlatrad)*Math.sin(endlatrad) +
				     Math.cos(startlatrad)*Math.cos(endlatrad)*
				     Math.cos(deltalong));
    double retval = EARTH_RADIUS * distinradians;

    return Distance.newNauticalMiles(retval * multiplier);
  }

  /**
   * This is a helper function to generate geo loc codes.
   * It is used in TOPSGlobalGroundAllocatorPlugIn.getOrgLocation() 
   * to initialize the static locations
   * of the known organizations.  In the long term, it is hoped that
   * this can be phased out in favor of a native ALPINE mechanism.
   *
   * @param code String representation of Geoloc code
   * @param name String name of organization
   * @param longd double degrees longitude
   * @param latd double degrees latitude
   * @return GeolocLocation initialized to input values
   * @deprecated
   */
  public static GeolocLocation makeGeoloc(GLMFactory fac,
					  String code, 
					  String name, 
					  double longd, 
					  double latd) {


    NewGeolocLocation retval = fac.newGeolocLocation();
    retval.setGeolocCode(code);
    retval.setName(name);
    retval.setLatitude(Latitude.newDegrees(latd));
    retval.setLongitude(Longitude.newDegrees(longd));
    return retval;
  }

 /**
  * Utility function to shift dates around.  This is helpful
  * if you need to make a Date object that is "x hours before/after 
  * some reference date".
  * For example, READYAT dates could be 1 day before mission date.
  * @param d Date object representing the reference date
  * @param factor float representing number of hours to shift
  * @param direction int, should be either 1 or -1.  
  *        1 indicates shifting forward, -1 indicates shifting backward.
  * @return Date the shifted date.
  */
  public static Date dateShift(Date d, float factor, int direction){
    long x = (long)(factor * 60 * 60 * 1000 * direction);
    Date newDate = new Date();
    if ((direction == 1)||(direction == -1)){
      newDate.setTime(d.getTime() + x);
      return (newDate);
    }
    else{
       throw new UTILPlugInException("illegal direction received.");
    }
  }

 /**
  * Utility functions to decode Date objects.  
  * Given a Date object representing anytime of the day,
  * returns a Date object representing 12:00AM of that
  * same day.
  * @param d Date object representing the reference date
  * @return Date, midnight of d.
  */
  public static Date decodeDate(Date d){
    //    long millis = d.getTime();
    //    Date midnight = new Date(millis - (millis % 86400000));
    Calendar cal = Calendar.getInstance ();
    cal.setTime (d);
    int year = cal.get (Calendar.YEAR);
    int month = cal.get (Calendar.MONTH);
    int day = cal.get (Calendar.DAY_OF_MONTH);
    cal.clear ();
    cal.set (year, month, day);
      
      //      Date midnight = new Date(millis - secondsToday);
    Date midnight = cal.getTime ();
    return midnight;
  }

  /** 
   * test if location is US or foreign
   * What we really want to know is if the port is a POE or POD; is there a 
   * better way?  Should we just create a list of all ports and find the
   * closest each time we need one?
   * @param p Organization to be tested
   * @return true if is an ammo port
   */
  public static boolean isForeignLoc(GeolocLocation org_loc) {
    boolean foreign_org = true;
    
    // For the sake of this demo, the US is bounded by the following cities:
    // Morgan, MT 48:59:36 N    (above) 48.9933
    // Quoddy, ME 67:01:51 W    (east)  -67.0308
    // Key West, FL 24:33:00 N  (below) 24.5500
    // Eureka, CA 124:09:45 W   (west) -124.1625
    // (data from www.mit.edu:8001/geo)

    double lat = org_loc.getLatitude().getDegrees();
    double lon = org_loc.getLongitude().getDegrees();

    if (lon < -67.0308d && 
	lon > -124.1625d && 
	lat > 24.5500d && 
	lat < 48.9933d)
      foreign_org = false;

    return foreign_org;
  }

  /**
   * given a geoloclocation and set of possible orgs, suggest
   * the best (closest) org.
   *
   * @param t Task representing cargo to be moved
   * @param ports Vector of possible orgs
   * @param String cluster ident. for debugging purposes
   * @return Organization object indicating best org
   */
  public static Organization bestOrg(GeolocLocation loc, Set orgs, String clusterName) {

    if (orgs == null || orgs.size() == 0) {
      throw new UTILPlugInException(clusterName+ "GLMMeasure: no orgs available");
    }

    if (loc == null) {
      // This is a lie- it doesn't actually return anything, does it?  it
      // just dies...
      throw new UTILPlugInException(clusterName+ "GLMMeasure: null loc while checking for orgs, returning default org");
    }

    Iterator orgse = orgs.iterator();
    Organization best = null;
    double distance = 0.0d;

    while (orgse.hasNext()) {
      Organization nextorg = (Organization) orgse.next();

      double nextdistance = 9999.0d;
      GeolocLocation orgloc = AssetUtil.getOrgLocation(nextorg);     

      // if string codes match, we don't have to look at distances...
      if (orgloc.getGeolocCode ().equals (loc.getGeolocCode ()))
	return nextorg;
      
      if (orgloc != null && 
	  orgloc.getLongitude() != null && 
	  orgloc.getLatitude() != null && 
	  loc.getLongitude() != null && 
	  loc.getLatitude() != null) {
	nextdistance = GLMMeasure.distanceBetween(loc, orgloc).getMiles();
	if (debug) System.out.println ("Dist between loc " + loc + " and orglog " + orgloc + " is " + nextdistance);
      }

      if (best == null) {
	best = nextorg;
	distance = nextdistance;
      } 

      else if (nextdistance < distance) {
	best = nextorg;
	distance = nextdistance;
	if (debug) System.out.println ("Best is " + best);
      }
    }

    if (best == null) {
      throw new UTILPlugInException(clusterName+ " GLMMeasure saw no appropriate orgs");
    }
    return best;
  }
}

