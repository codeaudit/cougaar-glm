/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;

import org.cougaar.domain.planning.ldm.plan.Location;
import org.cougaar.domain.glm.plan.GeolocLocation;
import org.cougaar.domain.glm.plan.IcaoLocation;
import org.cougaar.domain.glm.plan.Position;
import org.cougaar.domain.glm.plan.NamedPosition;

/*
  Define a location object that is created from the XML returned by
  a PlanServiceProvider.  Note that the values returned by this class
  may be null.  For example, a non-null GeolocCode will be returned
  only if the original location (in the log plan object) was a GeolocLocation.
  The comments for each get method indicate from what interfaces
  (in org.cougaar.domain.planning.ldm.plan) the information was originally obtained.
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
      positionClass = Class.forName("org.cougaar.domain.glm.plan.Position");
      namedPositionClass = Class.forName("org.cougaar.domain.glm.plan.NamedPosition");
      geolocLocationClass = Class.forName("org.cougaar.domain.glm.plan.GeolocLocation");
      icaoLocationClass = Class.forName("org.cougaar.domain.glm.plan.IcaoLocation");
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
