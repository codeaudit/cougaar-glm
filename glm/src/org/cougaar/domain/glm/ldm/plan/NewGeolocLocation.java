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