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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.cougaar.core.service.LoggingService;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.DBProperties;
import org.cougaar.util.Parameters;

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
