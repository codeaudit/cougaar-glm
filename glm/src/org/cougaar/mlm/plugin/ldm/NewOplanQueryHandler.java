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

import java.util.Date;
import java.util.Properties;
import java.util.Collection;
import java.util.ArrayList;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.util.DBProperties;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;


/** Reads oplan info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates oplan maintained by SQLOplanPlugin.
 */
public class NewOplanQueryHandler extends NewQueryHandler {
  private static final String QUERY_NAME = "OplanInfoQuery";

  public NewOplanQueryHandler(DBProperties adbp, NewOplanPlugin plugin) {
    super(adbp, plugin);
  }

  public Collection executeQueries(Statement statement) throws SQLException {
    String query = dbp.getQuery(QUERY_NAME, dbp);
    ResultSet rs = statement.executeQuery(query);
    Collection result = new ArrayList(1);
    while (rs.next()) {
      result.add(processRow(rs));
    }
    rs.close();
    if (result.isEmpty())
	System.err.println("Returning empty collection of oplans");
    return result;
  }

  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  private Oplan processRow(ResultSet rs) throws SQLException {
    try {
      String opName = getString(rs, 1);
      String priority = getString(rs, 2);
      String oplanId = dbp.getProperty(OplanReaderPlugin.OPLAN_ID_PARAMETER);
      Date cDay = plugin.getCDay();
      return new Oplan(null, // Filled in by caller
                       oplanId,
                       opName,
                       priority,
                       cDay);
    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
      return new Oplan();
    }
  }
}




