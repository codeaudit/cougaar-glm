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
package org.cougaar.glm.ldm;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.LoggingService;
import org.cougaar.mlm.plugin.ldm.LDMEssentialPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.Parameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Provides wrappers around the query routines. Takes care of a few LDMPlugin calls. Expects to have one Plugin argument
 * - the name of the file describing the queries ex. SpecialQueryLDMPlugin(specialQueryDescription.q)
 * @see #parseQueryFile(String queryFile) for a description of the query file.
 */
public abstract class QueryLDMPlugin extends LDMEssentialPlugin {
  protected Hashtable fileParameters_ = new Hashtable();
  protected String url_, user_, password_;
  protected MessageAddress clusterId_;
  protected Vector cannotHandle_ = new Vector();
  protected String className_ = null;
  protected Hashtable myParams_ = new Hashtable();
  protected LoggingService logger;
  //WAN the type of database we are connecting to (Oracle, MySQL...)
  protected String dbType; //WAN

  /**
   * Parse plugIn arguments, initialize driver, get cluster identifier.
   */
  // Get the logger service via reflection
  public void setLoggingService(LoggingService loggingService) {
    logger = loggingService;
  }

  protected void setupSubscriptions() {
    parseArguments();
    initializeDriver();
    clusterId_ = getMessageAddress();
    className_ = this.getClass().getName();
    int indx = className_.lastIndexOf(".");
    if (indx > -1) {
      className_ = className_.substring(indx + 1);
    }

  }

  /**
   * quick check for all things it should be able to handle
   */
  abstract public boolean canHandle(String typeid, Class class_hint);

  /**
   * Called by getPrototype() to actually create the prototype.
   */
  abstract public Asset makePrototype(java.lang.String aTypeName, java.lang.Class anAssetClassHint);

  /**
   * calls makePrototype().  calls fillProperties(). caches result
   */
  public Asset getPrototype(String aTypeName, Class anAssetClassHint) {
    if (!canHandle(aTypeName, anAssetClassHint))
      return null;
    // check if thought it could handle it, but previously failed
    if (cannotHandle_.contains(aTypeName)) {
      return null;
    }
    Asset asset = makePrototype(aTypeName, anAssetClassHint);
    if (asset == null) {
      cannotHandle_.add(aTypeName);
      return null;
    }
    // SHOULD NOT NEED TO BE CALLED!
    fillProperties(asset);
    getLDM().cachePrototype(aTypeName, asset);
    return asset;
  }

  /**
   * Calls LDM factory to create prototype and set nomenclature and alternameTypeIdentification.
   * @param typeid type identification of desired asset
   * @param type the class name of the desired asset
   * @param nomenclature *
   */
  protected Asset newAsset(String typeid, String type, String nomenclature) {
    Asset proto;
    try {
      if (typeid == null)
        throw new IllegalArgumentException("newResource : " + typeid + " nomenclature: " + nomenclature);
      proto = getLDM().getFactory().createPrototype(type, typeid);
      // 	    GLMDebug.DEBUG(className_, "newResource(), typeid "+typeid+", nomenclature "+
      // 			    nomenclature);
      if (proto == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("newResource(), could not create prototype: " + typeid);
        }
        return null;
      }
      if (nomenclature != null) {
        NewTypeIdentificationPG pg = (NewTypeIdentificationPG) proto.getTypeIdentificationPG();
        pg.setNomenclature(nomenclature);
        pg.setAlternateTypeIdentification(pg.getTypeIdentification());
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("newResource:  " + typeid + " no nomenclature ");
        }
      }
    } catch (Exception ee) {
      if (logger.isErrorEnabled()) {
        logger.error("newResource() could not make " + typeid + " " + ee.toString());
      }
      ee.printStackTrace();
      return null;
    }
    return proto;
  }

  private String getDBType(String databaseVal) {
    int colonIndex1 = databaseVal.indexOf(':');
    int colonIndex2 = databaseVal.indexOf(':', colonIndex1 + 1);
    return databaseVal.substring(colonIndex1 + 1, colonIndex2);
  }

  // retrieve and parse the arguments
  // Currently expecting the query file as the only argument.
  protected void parseArguments() {
    // first, initialize the global table with some basics
    Vector pv = getParameters();
    if (pv == null) {
      throw new RuntimeException("QueryLDMPlugin requires at least one parameter");
    } else {
      String s;
      for (Enumeration ps = pv.elements(); ps.hasMoreElements();) {
        s = (String) ps.nextElement();
        if (s.indexOf('=') != -1) {
          int i = s.indexOf('=');
          String key = new String(s.substring(0, i));
          String value = new String(s.substring(i + 1, s.length()));
          myParams_.put(key.trim(), value.trim());
        } else if (s.charAt(0) == '+') {
          myParams_.put(new String(s.substring(1)), new Boolean(true));
        } else if (s.charAt(0) == '-') {
          myParams_.put(new String(s.substring(1)), new Boolean(false));
        } else {
          // If it was not a key/value pair then it is a query file
          parseQueryFile(s);
        }
      }
    }
  }

  /**
   * parses query file into local hashtable
   * @param queryFile name of query file The query file is expected to be in the format param_name=param_value The
   * necessary parameters are:  Database, Driver, Username, Password Some parameters are not currently
   * used, but may be in the future. These inclue MIN_IN_POOL, MAX_IN_POOL, TIMEOUT, and
   * NUMBER_OF_TRIES. The rest of the lines are expected to be the query statements. Here is an example
   * from icisParts.q. Database = ${org.cougaar.database} Username = ${icis.database.user} Password =
   * ${icis.database.password} MIN_IN_POOL= 1 MAX_IN_POOL= 4 TIMEOUT= 1 NUMBER_OF_TRIES= 2
   * headerQuery=select commodity, nsn, nomenclature, ui, ssc, price, icc, alt, plt, pcm, boq, diq,
   * iaq, nso, qfd, rop, owrmrp, weight, cube, aac, slq from header where NSN = :nsns
   * assetsQuery=select nsn, ric, purpose, condition, iaq from assets where NSN = :nsns nomen=select
   * nomenclature from header where NSN = :nsns cost=select price from header where NSN = :nsns
   */
  protected void parseQueryFile(String queryFile) {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(getConfigFinder().open(queryFile)));
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        line = line.trim();
        // skip empty lines
        if (line.length() == 0)
          continue;

        // handle continuation lines
        int len;
        while ((len = line.length()) > 0 && '\\' == line.charAt(len - 1)) {
          line = (line.substring(0, len - 1)) + (in.readLine().trim());
        }

        char c = line.charAt(0);

        // skip comments
        if (c == '#')
          continue;
        else if (line.indexOf("select ") != -1) { //WAN if the word "select " is found process as if query
          //String queryType = line.substring(1).trim(); // skip the <
          int equalsIndex = line.indexOf('='); //find the =
          String queryType = line.substring(0, equalsIndex);
          int dotIndex = queryType.indexOf(".");
          if (dotIndex != -1)
            queryType = queryType.substring(dotIndex + 1).trim();
          else
            queryType = "default";

          //only process query if it is a default query or it is a query for the type of database that is being used
          if (queryType.equals("default")) {
            // I don't think this needs to be done -- llg
            //line = Parameters.replaceParameters(line);
            parseQueryLine(line);
          } else if (queryType.equalsIgnoreCase(dbType)) {
            String startQuery = line.substring(0, dotIndex);
            String endQuery = line.substring(equalsIndex);
            String newLine = startQuery + endQuery;
            //newLine = Parameters.replaceParameters(line);
            // I don't think this needs to be done -- llg
            parseQueryLine(newLine, true);
          } else //skip if this query does not match this database type and is not a default query (WAN)
            continue;
        } else {
          // should be a param=value line
          line = Parameters.replaceParameters(line);
          parseQueryLine(line);
        }
      }
      in.close();

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error reading '" + queryFile + "': " + e);
      }
      e.printStackTrace();
      throw new RuntimeException("No QueryFile: " + e);
    }
  }

  protected void parseQueryLine(String s) {
    int i = s.indexOf('=');
    if (i < 1) {
      if (logger.isErrorEnabled()) {
        logger.error(" parseQueryLine cannot parse <" + s + ">");
      }
      return;
    }
    String p = s.substring(0, i).trim();
    String v = s.substring(i + 1).trim();
    if (p.equalsIgnoreCase("database")) {//WAN added
      String realVal = Parameters.replaceParameters(v); //WAN added
      dbType = getDBType(realVal);  //WAN
    }
    if (!fileParameters_.containsKey(p)) {
      fileParameters_.put(p, v);
    }
  }

  // force the database specific query
  protected void parseQueryLine(String s, boolean dbSpecific) {
    int i = s.indexOf('=');
    if (i < 1) {
      if (logger.isErrorEnabled()) {
        logger.error(" parseQueryLine cannot parse <" + s + ">");
      }
      return;
    }
    String p = s.substring(0, i).trim();
    String v = s.substring(i + 1).trim();
    if (dbSpecific)
      fileParameters_.put(p, v);
  }


  // document fileParameters_ hashtable
  protected String getParm(String name) {
    String value = (String) fileParameters_.get(name);
    if (value == null) {
      throw new RuntimeException(this.toString() + ": Couldn't initialize Driver need to specify " + name);
    }
    return value;
  }

  // initialize driver and obtain info to execute a query
  protected void initializeDriver() {
    //String driverName = getParm("Driver"); //WAN - removed this to get Driver from database name
    url_ = getParm("Database");
    user_ = getParm("Username");
    password_ = getParm("Password");
    //      int colonIndex1 = url_.indexOf(':'); //WAN
    //      int colonIndex2 = url_.indexOf(':', colonIndex1+1); //WAN

    //String dbtype = url_.substring(colonIndex1+1, colonIndex2); //delete later
    if (dbType == null) {
      throw new RuntimeException("No Connection parameter. - No dbtype");
    }

    String driverName = Parameters.findParameter("driver." + dbType);//WAN  must go directly to cougaar.rc now

    if (driverName != null) {
      try {
        DBConnectionPool.registerDriver(driverName);
      } catch (Exception e) {
        System.err.println("Could not register driver " + driverName + ":");
        e.printStackTrace();
      }
    }
    url_ = getParm("Database");
    user_ = getParm("Username");
    password_ = getParm("Password");

    // PAS MIK - COUGAAR Node scope for connection pools
    //      int minPoolSize= Integer.parseInt(getParm("MIN_IN_POOL"));
    //      int maxPoolSize= Integer.parseInt(getParm("MAX_IN_POOL"));
    //    int timeout= Integer.parseInt(getParm("TIMEOUT"));
    // String queryFile = "";
    //    int nTries= Integer.parseInt(getParm("NUMBER_OF_TRIES"));
    //try {

  }

  /**
   * @return Vector<Object[]> of results or null (on failure)
   */
  public Vector executeQuery(String query) {
    Vector result = new Vector();
    ResultSet rs = null;
    Connection conn = null;
    try {
      conn = getConnection();
      Statement statement = conn.createStatement();
      rs = statement.executeQuery(query);
      ResultSetMetaData md = rs.getMetaData();
      int ncols = md.getColumnCount();
      Object row[] = new Object[ncols];
      while (rs.next()) {
        for (int i = 0; i < ncols; i++) {
          row[i] = rs.getObject(i + 1);
        }
        result.add((Object[]) row.clone());
      }
      /*
	if ((ncols*result.size())>100) {
	System.err.println("Excessively large query ("+(ncols*result.size())+"): ");
	Thread.dumpStack();
	}
      */
      statement.close();
    } catch (java.sql.SQLException sqle) {
      if (logger.isErrorEnabled()) {
        logger.error("executeQuery failed: " + sqle);
      }
      sqle.printStackTrace();
      return null;
    } finally {
      if (conn != null)
        releaseConnection(conn);
    }
    return result;
  }

  public interface RowHandler {
    /**
     * Called by executeQuery per row.
     */
    void execute(ResultSetMetaData md, ResultSet rs) throws SQLException;
  }

  public static class QueryComplete extends RuntimeException {
  }

  /**
   * Less expensive variation on executeQuery(String) *
   */
  public void executeQuery(String query, RowHandler rh) {
    Connection conn = null;
    Statement statement = null;
    try {
      conn = getConnection();
      statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(query);
      /*
	try {
        rs.setFetchDirection(ResultSet.FETCH_FORWARD);
        rs.setFetchSize(1000);
	} catch (AbstractMethodError re) {
        // System.err.println("Driver is not JDBC 2.0 compatible");
	}
      */

      ResultSetMetaData md = rs.getMetaData();
      try {
        while (rs.next()) {
          rh.execute(md, rs);
        }
      } catch (QueryComplete qc) {
        // early exit.
      }
      //statement.close(); // close it in finally clause
    } catch (java.sql.SQLException sqle) {
      if (logger.isErrorEnabled()) {
        logger.error("executeQuery failed: " + sqle);
      }
      sqle.printStackTrace();
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException squeal) {
        }
      }
      if (conn != null)
        releaseConnection(conn);
    }
  }


  protected Connection getConnection() throws SQLException {
    return DBConnectionPool.getConnection(url_, user_, password_);
  }

  protected void releaseConnection(Connection conn) {
    try {
      conn.close();
    } catch (java.sql.SQLException sqle) {
      if (logger.isErrorEnabled()) {
        logger.error("releaseConnection " + sqle);
      }
    }
  }
}
