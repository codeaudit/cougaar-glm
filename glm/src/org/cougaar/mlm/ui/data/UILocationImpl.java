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

package org.cougaar.mlm.ui.data;

import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;

import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.IcaoLocation;
import org.cougaar.glm.ldm.plan.Position;
import org.cougaar.glm.ldm.plan.NamedPosition;

/*
  Define a location object that is created from the XML returned by
  a PlanServiceProvider.  Note that the values returned by this class
  may be null.  For example, a non-null GeolocCode will be returned
  only if the original location (in the log plan object) was a GeolocLocation.
  The comments for each get method indicate from what interfaces
  (in org.cougaar.planning.ldm.plan) the information was originally obtained.
  Note that GeolocLocation, IcaoLocation, and DodaacLocations also
  include the information from NamedPosition and Position,
  and NamedPosition also includes the information from Position.
*/

public class UILocationImpl implements UILocation {
  Location location;
  static Class positionClass;
  static Class namedPositionClass;
  static Class geolocLocationClass;
  static Class icaoLocationClass;

  public UILocationImpl(Location location) {
    this.location = location;
    try {
      positionClass = Class.forName("org.cougaar.glm.ldm.plan.Position");
      namedPositionClass = Class.forName("org.cougaar.glm.ldm.plan.NamedPosition");
      geolocLocationClass = Class.forName("org.cougaar.glm.ldm.plan.GeolocLocation");
      icaoLocationClass = Class.forName("org.cougaar.glm.ldm.plan.IcaoLocation");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  /** @return String - the string containing the class of location, one of "Position", "NamedPosition", "GeolocLocation", "IcaoLocation", "DodaacLocation"  */

  public String getLocationType() {
    String className = null;
    try {
      className = location.getClass().toString();
    } catch (Exception e) {
      System.out.println(e);
    }
    int i = className.lastIndexOf(".");
    return (className.substring(i+1));
  }

  /*
    From Position.
  */

  /** @return double - the Latitude in degrees representing this position */
  public double getLatitude() {
    if (positionClass.isInstance(location)) {
      Latitude latitude = ((Position)location).getLatitude();
      if (latitude != null)
        return latitude.getDegrees();
    }
    return 0.0;
  }
  
  /** @return double - the Longitude in degrees representing this position */
  public double getLongitude() {
    if (positionClass.isInstance(location)) {
      Longitude longitude = ((Position)location).getLongitude();
      if (longitude != null)
        return longitude.getDegrees();
    }
    return 0.0;
  }

  /*
    From NamedPosition.
  */
        
  /** @return String - the string name representing this position */
  public String getName() {
    if (namedPositionClass.isInstance(location))
      return ((NamedPosition)location).getName();
    else
      return null;
  }

  /*
    From GeolocLocation.
  */

  /** @return String - the geoloc code representing this position */
  public String getGeolocCode() {
    if (geolocLocationClass.isInstance(location))
      return ((GeolocLocation)location).getGeolocCode();
    else
      return null;
  }
        
  /** @return String - the installation type code representing this position*/
  public String getInstallationTypeCode() {
    if (geolocLocationClass.isInstance(location))
      return ((GeolocLocation)location).getInstallationTypeCode();
    else
      return null;
  }
    
        
  /** @return String - the Country state code representing this position*/
  public String getCountryStateCode() {
    if (geolocLocationClass.isInstance(location))
      return ((GeolocLocation)location).getCountryStateCode();
    else
      return null;
  }
        
  /** @return String  - the Country state name representing this position*/
  public String getCountryStateName() {
    if (geolocLocationClass.isInstance(location))
      return ((GeolocLocation)location).getCountryStateName();
    else
      return null;
  }

  /*
    From GeolocLocation or IcaoLocation.
  */
        
  /** @return String  - the Icao code representing this position*/
  public String getIcaoCode() {
    if (geolocLocationClass.isInstance(location))
      return ((GeolocLocation)location).getIcaoCode();
    else if (icaoLocationClass.isInstance(location))
      return ((IcaoLocation)location).getIcaoCode();
    else
      return null;
  }

  //  public String getUUID() {
  //    return String.valueOf(this.hashCode());
  //  }

}
