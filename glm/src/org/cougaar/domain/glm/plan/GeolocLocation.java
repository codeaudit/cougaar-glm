/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.plan;
 
public interface GeolocLocation extends NamedPosition, Cloneable {
	
  /** @return String - the geoloc code representing this position */
  String getGeolocCode();
	
  /** @return String - the installation type code representing this position*/
  String getInstallationTypeCode();
	
  /** @return String - the Country state code representing this position*/
  String getCountryStateCode();
	
  /** @return String  - the Country state name representing this position*/
  String getCountryStateName();
	
  /** @return String  - the Icao code representing this position*/
  String getIcaoCode();

  public Object clone();	
	
}
