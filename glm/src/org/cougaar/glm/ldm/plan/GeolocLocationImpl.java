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

package org.cougaar.glm.ldm.plan;
 
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.mlm.ui.util.AsciiPrinter; 
import org.cougaar.mlm.ui.util.SelfPrinter; 
 
public class GeolocLocationImpl extends NamedPositionImpl
  implements GeolocLocation, NewGeolocLocation, SelfPrinter {
        
  String GeolocCode, InstallTypeCode, CSCode, CSName, IcaoCode;
        
  public GeolocLocationImpl() {
    super();
  }
  
  public GeolocLocationImpl(Latitude la, Longitude lon, String name)
  {
    super(la, lon, name);
  }
         
        
  /** @return String - the geoloc code representing this position */
  public String getGeolocCode() {
    return GeolocCode;
  }
                
  /** @return String - the installation type code representing this position*/
  public String getInstallationTypeCode() {
    return InstallTypeCode;
  }
        
  /** @return String - the Country state code representing this position*/
  public String getCountryStateCode() {
    return CSCode;
  }
        
  /** @return String  - the Country state name representing this position*/
  public String getCountryStateName() {
    return CSName;
  }
        
  /** @return String  - the Icao code representing this position*/
  public String getIcaoCode() {
    return IcaoCode;
  }
        
  /** @param aGeolocCode - set the geoloc code representing this position */
  public void setGeolocCode(String aGeolocCode) {
    if (aGeolocCode != null) aGeolocCode = aGeolocCode.intern();
    GeolocCode = aGeolocCode;
  }
        
  /** @param aInstCode - set the installation type code representing this position*/
  public void setInstallationTypeCode(String aInstCode) {
    if (aInstCode != null) aInstCode = aInstCode.intern();
    InstallTypeCode = aInstCode;
  }
        
  /** @param aCSCode - set the Country state code representing this position*/
  public void setCountryStateCode(String aCSCode) {
    if (aCSCode != null) aCSCode = aCSCode.intern();
    CSCode = aCSCode;
  }
        
  /** @param aCSName  - set the Country state name representing this position*/
  public void setCountryStateName(String aCSName) {
    if (aCSName != null) aCSName = aCSName.intern();
    CSName = aCSName;
  }
        
  /** @param anIcaoCode  - set the Icao code representing this position*/
  public void setIcaoCode(String anIcaoCode) {
    if (anIcaoCode != null) anIcaoCode = anIcaoCode.intern();
    IcaoCode = anIcaoCode;
  }

  public String toString() {
    String n = getName();
    return
      ((n != null) ?
       (GeolocCode+"("+n+")") :
       GeolocCode);
  }

  public void printContent(AsciiPrinter pr) {
    pr.print(GeolocCode, "GeolocCode");
    pr.print(InstallTypeCode, "InstallationTypeCode");
    pr.print(CSCode, "CountryStateCode");
    pr.print(CSName, "CountryStateName");
    pr.print(IcaoCode, "IcaoCode");
    pr.print(getName(), "Name");
    pr.print(lat, "Latitude");
    pr.print(lon, "Longitude");
  }

  public Object clone() {
    GeolocLocationImpl gli = new GeolocLocationImpl();
    gli.setGeolocCode(GeolocCode);
    gli.setInstallationTypeCode(InstallTypeCode);
    gli.setCountryStateCode(CSCode);
    gli.setCountryStateName(CSName);
    gli.setIcaoCode(IcaoCode);
    gli.setName(getName());
    gli.setLatitude(lat);
    gli.setLongitude(lon);
    return gli;
  }

  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }

    if (object == this) {
      return true;
    }

    if (!(object instanceof GeolocLocation)) {
      return false;
    }

    GeolocLocation other = (GeolocLocation)object;

    return (matches(getGeolocCode(), other.getGeolocCode()) &&
            matches(getInstallationTypeCode(), other.getInstallationTypeCode()) &&
            matches(getCountryStateCode(), other.getCountryStateCode()) &&
            matches(getCountryStateName(), other.getCountryStateName()) &&
            matches(getIcaoCode(), other.getIcaoCode()) &&
            matches(getName(), other.getName()) &&
            matches(getLatitude(), other.getLatitude()) &&
            matches(getLongitude(), other.getLongitude()));
  }

  private transient int _hc = 0;
  public int hashCode()
  {
    if (_hc == 0) _hc = getName().hashCode();
    return _hc;
  }

  private boolean matches(Object a, Object b) {
    return (a==null)?(b==null):(a.equals(b));
  }

}






