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


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.cougaar.core.blackboard.SubscriberException;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.service.DomainService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.Parameters;

/**
 * Provide a JDBC binding to an COUGAAR cluster using MB5.0 interfaces.
 * This class replaces the JDBCPlugin.  Current use involves runtime
 * creation of LDMObjects at Cluster startup.  Future use will involve
 * dynamic, runtime creation of LDMObjects indirectly, via LDMFactory
 * methods.
 *
 * On one side, we present a normal plugin API.  On the other side,
 * we parse a "Query file" and talk to JDBC and a set of QueryHandler
 * objects.
 *
 * The plugin expects at least one Plugin parameter
 * (via Plugin.getParameters). The first parameter is
 * interpreted as the name of a "query file".  We look for this file
 * first in the current directory and then in $alphome/demo/queries/.
 * Any other parameters are interpreted as query parameter settings,
 * as though they were parsed from a global section of the query file.
 * plugin=org.cougaar.mlm.plugin.sql.LDMSQLPlugin(foo.q, NSN=12345669)
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
 * "org.cougaar.mlm.plugin.sql") in order to resolve the class.
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

public class LDMSQLPlugin extends LDMEssentialPlugin //implements SQLService
{

  private static final String DEFAULT_DB_DRIVER = "oracle.jdbc.driver.OracleDriver";

  DomainService domainService = null;
  protected PlanningFactory theFactory = null;

  protected LoggingService log = LoggingService.NULL;

  public LDMSQLPlugin() {}

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
    theFactory = ((PlanningFactory) domainService.getFactory("planning"));

    LoggingService thelog = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, 
											    new ServiceRevokedListener() {
											      public void serviceRevoked(ServiceRevokedEvent re) {
												log = LoggingService.NULL;
											      }
											    });
    if (thelog != null)
      log = thelog;
    
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
	log.error("Initialization Failed in " + getMessageAddress(), se);
      }
    }
  }

  // empty execute
  public void execute() {}

  protected void initProperties() {
    // default package for QueryHandler
    globalParameters.put("Package", "org.cougaar.mlm.plugin.ldm");
    globalParameters.put("agent","'"+getMessageAddress()+"'");
    //globalParameters.put("PublishOnSelfOrg","true");
//     if (log.isDebugEnabled()) 
//       log.debug("In " + getMessageAddress() + " globalParameters are: "+globalParameters);
  }

  // retrieve and parse the arguments
  protected void grokArguments() {
    // first, initialize the global table with some basics
    Vector pv = getParameters();
    if (pv == null) {
      throw new RuntimeException("LDMPlugin requires at least one parameter");
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
	throw new RuntimeException("LDMPlugin requires at least one parameter");
      }
    }
  }

  // parse the query file
  protected void parseQueryFile() {
//     if (log.isDebugEnabled())
//       log.debug("In " + getMessageAddress() + " query file is: "+queryFile);
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
// 	      if (log.isDebugEnabled())
// 		log.debug("LDMSQLPlugin, pt is: "+pt);
	      cqh.initialize(this, // LDMEssentialPlugin
			     // this, // ldmservice
			     getMessageAddress(),
			     getCluster(),
			     ((PlanningFactory) domainService.getFactory("planning")),
			     pt,
			     getBlackboardService());
	      queries.addElement(cqh);
	    } catch (Exception bogon) {
	      log.error("In " + getMessageAddress() + ", Exception creating "+s, bogon);
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
      log.error("In " + getMessageAddress() + ", Error reading '"+queryFile+"'",e);
      throw new RuntimeException("No QueryFile: "+e);
    }
  }

    /**
     * This method exposes the protected LDM in the plugin Adapter
     * to the QueryHandler.  Had to name it differently than
     * getLDM() because the plugin adapter version of getLDM() is final.
     */
  public LDMServesPlugin getLDMPlugin() { return getLDM(); }

  private String getDBType(String databaseVal) {
    int colonIndex1 = databaseVal.indexOf(':');
    int colonIndex2 = databaseVal.indexOf(':', colonIndex1+1);
    return databaseVal.substring(colonIndex1+1, colonIndex2);
  }

  private void parseQueryParameter(Properties table, String s) {
    int i = s.indexOf('=');
//     if (log.isDebugEnabled())
//       log.debug( "\ns = " + s );
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
  protected void grokQueries() {
    for (Enumeration e = queries.elements(); e.hasMoreElements();) {
      QueryHandler qh = (QueryHandler) e.nextElement();
      qh.start();
      if (qh instanceof org.cougaar.mlm.plugin.ldm.PrototypeProvider) {
	prototypeProviders.addElement(qh);
      } else if (qh instanceof org.cougaar.mlm.plugin.ldm.PropertyProvider) {
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
	  log.error("Subsituting params in agent " + getMessageAddress() + " in '"+s+"' at "+(i-1)+ " got null value for param: " + param);
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
	if (log.isWarnEnabled())
	  log.warn("In agent " + getMessageAddress() + ": No Driver parameter specified for " + dbType +
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
	log.error("In agent " + getMessageAddress() + ": executeQuery failed for " + sql, sqe);
      } finally {
        conn.close();
      }

    } catch (Exception e) {
      log.error("In agent " + getMessageAddress() + ": Caught exception while executing a query",e);
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
