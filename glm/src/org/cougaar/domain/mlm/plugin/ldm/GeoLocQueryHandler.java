/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import java.util.Date;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.core.cluster.Subscriber;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;


import org.cougaar.util.Parameters;

import org.cougaar.domain.glm.ldm.GLMFactory;
import org.cougaar.domain.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

/** Reads geoloc info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugIn. Updates SQLOplanPlugIn's geoloc table.
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

    myPlugIn.updateLocation(geoloc);
  }
}





