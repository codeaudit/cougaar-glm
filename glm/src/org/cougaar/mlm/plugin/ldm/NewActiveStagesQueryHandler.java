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
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.util.DBProperties;

/** Reads oplan info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates oplan maintained by SQLOplanPlugin.
 */

public class NewActiveStagesQueryHandler extends NewQueryHandler {
  private static final String QUERY_NAME = "ActiveStagesQuery";

  private int minRequiredStage = 0;

  public NewActiveStagesQueryHandler(DBProperties adbp, NewOplanPlugin plugin) {
    super(adbp, plugin);
  }

  public Collection executeQueries(Statement statement) throws SQLException {
    String query = dbp.getQuery(QUERY_NAME, dbp);
    ResultSet rs = statement.executeQuery(query);
    if (rs.next()) {
      Collection result = Collections.singleton(processRow(rs));
      rs.close();
      return result;
    } else {
      return Collections.EMPTY_SET;
    }
  }
    
  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  public Object processRow(ResultSet rs) {
    try {
      return (Number) rs.getObject(1);
    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
      return new Integer(0);
    }
  }
}




