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

package org.cougaar.mlm.plugin.ldm;

import java.util.Date;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.agent.ClusterServesPlugin;
import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.core.domain.LDMServesPlugin;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;


import org.cougaar.util.Parameters;

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.glm.ldm.plan.GeolocLocation;

/** Reads geoloc info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates SQLOplanPlugin's geoloc table.
 */

public class GeoLocQueryHandler  extends SQLOplanQueryHandler {
  private static final String QUERY_NAME = "GeoLocQuery";

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
    if (rowData.length != 8) {
      System.err.println("GeoLocQueryHandler.processRow() - expected 8 columns of data, " +
                         " got " + rowData.length);
    }

    NewGeolocLocation geoloc = GLMFactory.newGeolocLocation();
    geoloc.setName((String) rowData[1]);
    geoloc.setGeolocCode((String) rowData[0]);
    geoloc.setIcaoCode((String) rowData[3]); 
    geoloc.setInstallationTypeCode((String) rowData[2]);
    geoloc.setLatitude(Latitude.newLatitude(((Number) rowData[4]).doubleValue()));
    geoloc.setLongitude(Longitude.newLongitude(((Number) rowData[5]).doubleValue()));
    geoloc.setCountryStateCode((String) rowData[6]);
    geoloc.setCountryStateName((String) rowData[7]);

    myPlugin.updateLocation(geoloc);
  }
}





