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

package org.cougaar.domain.mlm.plugin.ldm;


import org.cougaar.core.plugin.LDMPlugInServesLDM;
import org.cougaar.util.StateModelException;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.core.blackboard.BlackboardService;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.SubscriberException;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.FactoryException;
import org.cougaar.domain.planning.ldm.DomainService;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.domain.mlm.plugin.ldm.LDMEssentialPlugIn;

import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;


import org.cougaar.util.Parameters;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.sql.*;

/**
 * Provide a JDBC binding to an COUGAAR cluster using MB5.0 interfaces.
 * This class replaces the JDBCPlugIn.  Current use involves runtime
 * creation of LDMObjects at Cluster startup.  Future use will involve
 * dynamic, runtime creation of LDMObjects indirectly, via LDMFactory
 * methods.
 *
 * On one side, we present a normal plugin API.  On the other side,
 * we parse a "Query file" and talk to JDBC and a set of QueryHandler
 * objects.
 *
 * The plugin expects at least one PlugIn parameter
 * (via PlugIn.getParameters). The first parameter is
 * interpreted as the name of a "query file".  We look for this file
 * first in the current directory and then in $alphome/demo/queries/.
 * Any other parameters are interpreted as query parameter settings,
 * as though they were parsed from a global section of the query file.
 * plugin=org.cougaar.domain.mlm.plugin.sql.LDMSQLPlugIn(foo.q, NSN=12345669)
 *
 * The query file is a description of one or more queries to execute
 * on behalf of the plugin.  Comment lines (lines starting with '#') and
 * empty lines are ignored.  Lines starting with '%' indicate the start
 * of a "query section" and the rest of the line names the QueryHandler
 * class to use.  All other lines are of the form "parameter=value" where
 * the parameter values are made available to the current query.  Initial
 * parameter settings (before a '%' line) are considered global and are
 * inherited by all following queries.  In addition, a special case
 * pseudo-query of "%Global" allows additional entries into the global table.
 *
 * A QueryHandler (for now) names a java class to instantiate for the
 * constructing an SQL expression and then parsing the rows of the results.
 * If the QueryHandler name does not include a '.' character, the parser
 * will prepend the value of the "Package" parameter (which defaults to
 * "org.cougaar.domain.mlm.plugin.sql") in order to resolve the class.
 *
 * See QueryHandler for more information.
 *
 * Reserved QueryFile Parameters:
 *  Package	Default package for QueryHandler classnames
 *  QueryFile	the QueryFile of the current Query
 *  Driver	JDBC driver to use (oracle is already loaded)
 *  Database	JDBC database descriptor (used in getConnection)
 *  Username	JDBC Username (also "user")
 *  Password	JDBC Password (also "password")
 *
 * Also support parameter substitutions in the sql query string.
 * If the QueryHandler returns
 *   "select nsn, quantity from nsns where nsn = ':NSN'"
 * the JDBC plugin will substitute the value of parameter NSN
 * for the string :NSN.  A colon character may be escaped with
 * a backslash.
 */

public class LDMSQLPlugIn extends LDMEssentialPlugIn //implements SQLService
{

  private static final String DEFAULT_DB_DRIVER = "oracle.jdbc.driver.OracleDriver";

  DomainService domainService = null;
  private RootFactory theFactory = null;

  public LDMSQLPlugIn() {}

  // the global parameters table (String -> object/String
  protected Properties globalParameters = new Properties();
  // list of query objects
  protected Vector queries = new Vector();
  // the name of the file to look for.
  protected String queryFile;
  //WAN the type of database we are connecting to (Oracle, MySQL...)
  protected String dbType; //WAN

  protected void setupSubscriptions() {
    // let the super run to deal with the uninteresting stuff
    //super.setupSubscriptions();


				// get the domain service
    if (theFactory == null) {
      domainService = (DomainService) getBindingSite()
	.getServiceBroker().getService(this, DomainService.class,
				       new ServiceRevokedListener() {
					   public void serviceRevoked(ServiceRevokedEvent re) {
					     theFactory = null;
					   }
					 });
    }
    theFactory = domainService.getFactory();
    
    // set up the subscription
    // This could be a future site for maintaining a Container of created
    // LDMObjects for future updating.
    //if (!didRehydrate()) {	// Objects should already exist after rehydration
    if (!getBlackboardService().didRehydrate()) {
      try {
	// set up initial properties
	initProperties();
	// deal with the arguments.
	grokArguments();
	// parse the query file into our query vectors and parameters
	parseQueryFile();
	// sort the queryhandlers into categories.
	grokQueries();
      } catch (SubscriberException se) {
	System.err.println(this.toString()+": Initialization failed: "+se);
      }
    }
  }

  // empty execute
  public void execute() {}

    protected void initProperties() {
    // default package for QueryHandler
    globalParameters.put("Package", "org.cougaar.domain.mlm.plugin.ldm");
    globalParameters.put("agent","'"+getClusterIdentifier()+"'");
    //System.out.println("LDMSQLPlugIn, globalParameters is: "+globalParameters);
  }

  // retrieve and parse the arguments
  private void grokArguments() {
    // first, initialize the global table with some basics
    Vector pv = getParameters();
    if (pv == null) {
      throw new RuntimeException("LDMPlugIn requires at least one parameter");
    } else {
      boolean isFirst = true;
      for (Enumeration ps = pv.elements(); ps.hasMoreElements(); ) {
	String p = (String) ps.nextElement();
	if (isFirst) {
	  isFirst = false;
	  globalParameters.put("QueryFile", p);
	  queryFile = p;
	} else {
	  parseQueryParameter(globalParameters, p);
	}
      }
      if (isFirst) {
				// no args
	throw new RuntimeException("LDMPlugIn requires at least one parameter");
      }
    }
  }

  // parse the query file
  private void parseQueryFile() {
      //System.out.println("LDMSQLPlugIn, query file is: "+queryFile);
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(getConfigFinder().open(queryFile)));
				//BufferedReader in = new BufferedReader(new InputStreamReader(getCluster().getConfigFinder().open(queryFile)));
      Properties pt = null;
      for (String line = in.readLine(); line != null; line=in.readLine()) {

	line = line.trim();     // ugly

				// skip empty lines
	if (line.length() == 0)
	  continue;

	int len;
				// handle continuation lines
	while ((len = line.length()) > 0 && '\\' == line.charAt(len-1)) {
	  line = (line.substring(0,len-1))+(in.readLine().trim());
	}

	char c = line.charAt(0);

				// skip comments
	if (c == '#')
	  continue;

				// queryhandler section
	if (c == '%') {
	  String s = line.substring(1).trim(); // skip the %
	  if (s == null || s.length() == 0 || s.equals("Global")) {
	    // global handler parameters
	    pt = null;
	  } else {
	    // s names a queryhandler class (for now)
	    try {
	      if (s.indexOf('.') < 0) { // if the class has no package..
		// try the default package (which is the same as ours)
		String pkg = globalParameters.getProperty("Package");
		if (pkg != null) {
		  s = pkg+"."+s;
		}
	      }
	      QueryHandler cqh= (QueryHandler)(Class.forName(s).newInstance());
	      
	      pt = (Properties)globalParameters.clone();
	      //System.out.println("LDMSQLPlugIn, pt is: "+pt);
	      cqh.initialize(this, // LDMEssentialPlugIn
			     // this, // ldmservice
			     getClusterIdentifier(),
			     getCluster(),
			     domainService.getFactory(),
			     pt,
			     getBlackboardService());
	      queries.addElement(cqh);
	    } catch (Exception bogon) {
	      System.err.println("Exception creating "+s+": "+bogon);
	      bogon.printStackTrace();
	    }
	  }
	} else if (line.indexOf("select ") != -1) { //WAN if the word "select " is found process as if query
          int equalsIndex = line.indexOf('='); //find the =
          String queryType = line.substring(0, equalsIndex);
          int dotIndex = queryType.indexOf(".");
          if (dotIndex != -1)
            queryType = queryType.substring(dotIndex+1).trim();
          else
            queryType = "default";

          //only process query if it is a default query or it is a query for the type of database that is being used
          if (queryType.equals("default")) {
            parseQueryParameter(((pt==null)?globalParameters:pt),
				line);
	  }
          else if (queryType.equalsIgnoreCase(dbType)){
	    StringBuffer newLine = new StringBuffer(line.substring(0, dotIndex));
	    newLine.append(line.substring(equalsIndex));
	    parseQueryParameter(((pt==null)?globalParameters:pt),
				newLine.toString(), true);
          }
          else //skip if this query does not match this database type and is not a default query (WAN)
            continue;
        }
        else {
	  // should be a param=value line
	  parseQueryParameter(((pt==null)?globalParameters:pt),
			      line);
	}
      }
      in.close();

    } catch (Exception e) {
      System.err.println("Error reading '"+queryFile+"': "+e);
      e.printStackTrace();
      throw new RuntimeException("No QueryFile: "+e);
    }
  }

  private String getDBType(String databaseVal) {
    int colonIndex1 = databaseVal.indexOf(':');
    int colonIndex2 = databaseVal.indexOf(':', colonIndex1+1);
    return databaseVal.substring(colonIndex1+1, colonIndex2);
  }

  private void parseQueryParameter(Properties table, String s) {
    int i = s.indexOf('=');
    //System.err.println( "\ns = " + s );
    String p = s.substring(0,i).trim();
    String v = s.substring(i+1).trim();

    if (p.equalsIgnoreCase("database")) {//WAN added
      String realVal = Parameters.replaceParameters(v); //WAN added
      dbType = getDBType(realVal); //WAN added
    }
    if  (!table.containsKey(p))
      table.put(p,v);
  }
  // force the database specific query in 
  private void parseQueryParameter(Properties table, String s, boolean dbSpecific) {
    int i = s.indexOf('=');
    String p = s.substring(0,i).trim();
    String v = s.substring(i+1).trim();
    if (dbSpecific) // if database specific, overwrite the default
      table.put(p,v);
  }

  /** sort queryhandlers into categories.
   * PeriodicQueries will get executed synchronously for the first
   * time here.
   **/
  private void grokQueries() {
    for (Enumeration e = queries.elements(); e.hasMoreElements();) {
      QueryHandler qh = (QueryHandler) e.nextElement();
      qh.start();
      if (qh instanceof org.cougaar.domain.mlm.plugin.ldm.PrototypeProvider) {
	prototypeProviders.addElement(qh);
      } else if (qh instanceof org.cougaar.domain.mlm.plugin.ldm.PropertyProvider) {
	propertyProviders.addElement(qh);
      }
      // else it was just a PeriodicQuery.
    }
  }

  private String produceQuery(QueryHandler qh, String s) {
    if (s.indexOf(':') < 0) {   // no params to handle
      return s;
    }

    char[] scs = s.toCharArray();
    int l = s.length();
    StringBuffer sb = new StringBuffer();

    int i = 0;
    while (i < l) {
      char c = scs[i];

      if (c == '\\') {          // quoted?
	i++;
	sb.append(scs[i]);
	i++;
      } else if (c == ':') {    // param subst?
	i++;
				// find the end of the var
	int j;
	for (j = i; j<l && Character.isLetterOrDigit(scs[j]); j++)
	  ;
				// j is now the index of the first non symbol char past the colon
	String param = s.substring(i,j);
	String value = qh.getParameter(param);
	if (value == null) {
	    //System.out.println("qh.getParameter(param="+param);
	    throw new RuntimeException("Parameter Substitution problem in '"+s+
				       "' at "+(i-1));
	}
	sb.append(value);
	i = j;
      } else {
	sb.append(c);
	i++;
      }
    }

    // sb contains the substituted string
    return sb.toString();
  }

  //
  // SQLService interface
  //

  public void executeSQL(String rawSql, QueryHandler qh) {
    try {
      String dbname = qh.getParameter("Database");
      if (dbname == null) {
	throw new RuntimeException("No Connection parameter.");
      }
      //WAN find the dbtype by using the value of the dbname delete later
      //String dbtype = getDBType(dbname); delete later
      if (dbType == null) {
	throw new RuntimeException("No Connection parameter. - No dbtype");
      }

      String driver = Parameters.findParameter("driver."+dbType);//WAN  must go directly to cougaar.rc now
      if (driver != null) {
	DBConnectionPool.registerDriver(driver);
      } else {
        System.err.println("No Driver parameter specified for " + dbType +
                           " - using default driver - " + DEFAULT_DB_DRIVER);
        DBConnectionPool.registerDriver(DEFAULT_DB_DRIVER);
      }

      // do Param substitution
      String sql;
      sql = produceQuery(qh, rawSql);
      
      Properties props = new Properties();
      String user = qh.getParameter("Username");
      if (user == null) user = qh.getParameter("user");
      String pass = qh.getParameter("Password");
      if (pass == null) pass = qh.getParameter("password");
      if (user == null || pass == null)
	throw new RuntimeException("Incomplete user/password parameters");
      props.put("user", user);
      props.put("password", pass);

      Connection conn = DBConnectionPool.getConnection(dbname, user, pass);
      try {
        Statement statement = conn.createStatement();
        ResultSet rset = statement.executeQuery(sql);
        ResultSetMetaData md = rset.getMetaData();
        int ncols = md.getColumnCount();

        Object row[] = new Object[ncols];

        while (rset.next()) {
	  for (int i = 0; i < ncols; i++)
            row[i] = rset.getObject(i+1);
	  qh.processRow(row);
        }

        statement.close();
      } catch (SQLException sqe) {
	  System.err.println("executeQuery failed: "+sqe+"\n" + sql);
      } finally {
        conn.close();
      }

    } catch (Exception e) {
      System.err.println("Caught exception while executing a query: "+e);
      e.printStackTrace();
    }
  }

  private Vector prototypeProviders = new Vector();
  private Vector propertyProviders = new Vector();

  //
  // LDMService
  //

  public Asset getPrototype(String typeid, Class hint) {
    for (Enumeration e = prototypeProviders.elements(); e.hasMoreElements();){
      PrototypeProvider pp = (PrototypeProvider) e.nextElement();
      if (pp.canHandle(typeid)) {
	Asset a = pp.getAssetPrototype(typeid);
	return a;
      }
    }
    return null;
  }

  public Asset getPrototype(String typeid) {
    return getPrototype(typeid, null);
  }

  public void fillProperties( Asset anAsset ) {
    for (Enumeration e = propertyProviders.elements(); e.hasMoreElements();){
      PropertyProvider pp = (PropertyProvider) e.nextElement();
      pp.provideProperties( anAsset );
    }
  }

}
