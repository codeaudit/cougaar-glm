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

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBConnectionPool;
import java.util.Collection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;

public abstract class NewQueryHandler {
  protected DBProperties dbp;
  protected NewOplanPlugin plugin;
  protected String database, username, password;
  protected LoggingService logger;

  protected NewQueryHandler(DBProperties adbp, NewOplanPlugin plugin) {
    dbp = adbp;
    this.plugin = plugin;
    this.logger = plugin.getLoggingService();
    try {
      String dbtype = dbp.getDBType();
      insureDriverClass(dbtype);
      database = dbp.getProperty("database");
      username = dbp.getProperty("username");
      password = dbp.getProperty("password");
    } catch (Exception e) {
      throw new RuntimeException("SQL Driver problem", e);
    }
  }
	
  private void insureDriverClass(String dbtype) throws SQLException, ClassNotFoundException {
    String driverParam = "driver." + dbtype;
    String driverClass = Parameters.findParameter(driverParam);
    if (driverClass == null) {
      // this is likely a "cougaar.rc" problem.
      // Parameters should be modified to help generate this exception:
      throw new SQLException("Unable to find driver class for \""+
                             driverParam+"\" -- check your \"cougaar.rc\"");
    }
    Class.forName(driverClass);
  }

  protected String getString(ResultSet rs, int column) throws SQLException {
    Object o = rs.getObject(column);
    if (o instanceof String) {
      return (String) o;
    } else {
      try {
        return new String ((byte[]) o, "US-ASCII");
      } catch (java.io.UnsupportedEncodingException e) {
        SQLException sqle = new SQLException(e.getMessage());
        sqle.initCause(e);
        throw sqle;
      }
    }
  }

  protected abstract Collection executeQueries(Statement stmt) throws SQLException;
  
  public final Collection readCollection() throws SQLException {
    try {
      Connection conn =  DBConnectionPool.getConnection(database, username, password);
      try {
        Statement stmt = conn.createStatement();
        try {
          return executeQueries(stmt);
        } finally {
          stmt.close();
        }
      } finally {
        conn.close();
      }
    } catch (SQLException sqle) {
      throw sqle;
    } catch (Exception e) {
      SQLException sqle = new SQLException("Other error doing queries");
      sqle.initCause(e);
      throw sqle;
    }
  }


  public final Object readObject() throws SQLException {
    Collection c = readCollection();
    if (c.isEmpty()) return null;
    return c.iterator().next();
  }
}
