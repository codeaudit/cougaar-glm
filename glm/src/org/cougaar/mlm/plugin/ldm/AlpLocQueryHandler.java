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

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;

/** Reads alploc info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates SQLOplanPlugin's geoloc table with alplocs.
 *
 * BOZO - alplocs are not geolocs but there isn't any other commonly understood way
 * to access them.
 */


public class AlpLocQueryHandler  extends SQLOplanQueryHandler {
  private static final String QUERY_NAME = "AlpLocQuery";

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
    if (rowData.length != 4) {
      System.err.println("AlpLocQueryHandler.processRow() -  expected 4 columns of data, " +
                         " got " + rowData.length);
    }

    NewGeolocLocation geoloc = GLMFactory.newGeolocLocation();
    try {
      if (rowData[1] instanceof String)
	geoloc.setName((String) rowData[1]);
      else
	geoloc.setName(new String ((byte[])rowData[1],"US-ASCII"));
      if (rowData[0] instanceof String) {
	geoloc.setGeolocCode((String) rowData[0]);
	geoloc.setIcaoCode((String) rowData[0]);
      } else {
	geoloc.setGeolocCode(new String ((byte[])rowData[0],"US-ASCII"));
	geoloc.setIcaoCode(new String ((byte[])rowData[0],"US-ASCII"));
      }
      //geoloc.setName((String) rowData[1]);
      //geoloc.setGeolocCode((String) rowData[0]);
      //geoloc.setIcaoCode((String) rowData[0]);
      geoloc.setLatitude(Latitude.newLatitude(((Number) rowData[2]).doubleValue()));
      geoloc.setLongitude(Longitude.newLongitude(((Number) rowData[3]).doubleValue()));
      geoloc.setInstallationTypeCode("AlpLoc");			

      myPlugin.updateLocation(geoloc);

    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
    }

  }

}




