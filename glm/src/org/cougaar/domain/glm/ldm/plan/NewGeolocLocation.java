/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.ldm.plan;
 
public interface NewGeolocLocation extends GeolocLocation, NewNamedPosition {
	
	/** @param aGeolocCode - set the geoloc code representing this position */
	void setGeolocCode(String aGeolocCode);
	
	/** @param aInstCode - set the installation type code representing this position*/
	void setInstallationTypeCode(String aInstCode);
	
	/** @param aCSCode - set the Country state code representing this position*/
	void setCountryStateCode(String aCSCode);
	
	/** @param aCSName  - set the Country state name representing this position*/
	void setCountryStateName(String aCSName);
	
	/** @param anIcaoCode  - set the Icao code representing this position*/
	void setIcaoCode(String anIcaoCode);
	
	
}