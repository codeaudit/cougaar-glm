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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.util.DBProperties;

/** Reads geoloc info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates SQLOplanPlugin's geoloc table.
 */

public class NewGeoLocQueryHandler extends NewQueryHandler {
  private static final String QUERY_NAME = "GeoLocQuery";

  public NewGeoLocQueryHandler(DBProperties dbp, NewOplanPlugin plugin) {
    super(dbp, plugin);
  }

  protected Collection executeQueries(Statement statement) throws SQLException {
    String query = dbp.getQuery(QUERY_NAME, dbp);
    ResultSet rs = statement.executeQuery(query);
    Collection result = new ArrayList();
    while (rs.next()) {
      result.add(processRow(rs));
    }
    rs.close();
    return result;
  }

  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  public GeolocLocation processRow(ResultSet rs) throws SQLException {
    NewGeolocLocation geoloc = GLMFactory.newGeolocLocation();
    geoloc.setGeolocCode(getString(rs, 1));
    geoloc.setName(getString(rs, 2));
    geoloc.setInstallationTypeCode(getString(rs, 3));
    geoloc.setIcaoCode(getString(rs, 4));
    geoloc.setLatitude(Latitude.newLatitude(rs.getDouble(5)));
    geoloc.setLongitude(Longitude.newLongitude(rs.getDouble(6)));
    geoloc.setCountryStateCode(getString(rs, 7));
    geoloc.setCountryStateName(getString(rs, 8));
    return geoloc;
  }
}





