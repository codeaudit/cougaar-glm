/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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

/** Reads alploc info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates SQLOplanPlugin's geoloc table with alplocs.
 *
 * BOZO - alplocs are not geolocs but there isn't any other commonly understood way
 * to access them.
 */


public class NewAlpLocQueryHandler extends NewQueryHandler {
  private static final String QUERY_NAME = "AlpLocQuery";

  public NewAlpLocQueryHandler(DBProperties dbp, NewOplanPlugin plugin) {
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
    String code = getString(rs, 1);
    geoloc.setName(getString(rs, 2));
    geoloc.setLatitude(Latitude.newLatitude(rs.getDouble(3)));
    geoloc.setLongitude(Longitude.newLongitude(rs.getDouble(4)));
    geoloc.setInstallationTypeCode("AlpLoc");			
    geoloc.setGeolocCode(code);
    geoloc.setIcaoCode(code);
    return geoloc;
  }
}
