/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.ldm;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterImpl;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.util.Parameters;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.domain.glm.debug.GLMDebug;
import org.cougaar.domain.mlm.plugin.ldm.LDMEssentialPlugIn;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Provides wrappers around the query routines.  
 * Takes care of a few LDMPlugIn calls.
 * Expects to have one PlugIn argument - the name of the file describing the queries
 * ex. SpecialQueryLDMPlugIn(specialQueryDescription.q)
 * @see #parseQueryFile(String queryFile) for a description of the query file.
 **/
public abstract class QueryLDMPlugIn extends LDMEssentialPlugIn {
    protected Hashtable fileParameters_ = new Hashtable();
    protected String url_, user_, password_;
    protected ClusterImpl cluster_;
    protected ClusterIdentifier clusterId_;
    protected Vector cannotHandle_ = new Vector();
    protected String className_ = null;
    protected Hashtable myParams_ = new Hashtable();

    /**
     * Parse plugIn arguments, initialize driver, get cluster identifier.
     **/
    protected void setupSubscriptions() { 
	parseArguments();
	initializeDriver();
	cluster_ = (ClusterImpl)getCluster();
 	clusterId_ = cluster_.getClusterIdentifier();
	className_ = this.getClass().getName();
	int indx = className_.lastIndexOf(".");
	if (indx > -1) {
	    className_ = className_.substring(indx+1);
	}

    }

    /** quick check for all things it should be able to handle **/
    abstract public boolean canHandle(String typeid, Class class_hint);

    /** Called by getPrototype() to actually create the prototype.  **/
    abstract public Asset makePrototype(java.lang.String aTypeName, java.lang.Class anAssetClassHint);

    /** calls makePrototype().  calls fillProperties(). caches result **/
    public Asset getPrototype(String aTypeName, Class anAssetClassHint) {
	if (!canHandle(aTypeName, anAssetClassHint)) return null;
	// check if thought it could handle it, but previously failed
	if (cannotHandle_.contains(aTypeName)) return null;
	Asset asset = makePrototype(aTypeName, anAssetClassHint);
	if (asset == null) {
	    cannotHandle_.add(aTypeName);
	    return null;
	}
	// SHOULD NOT NEED TO BE CALLED!
	fillProperties(asset);
	cluster_.cachePrototype(aTypeName, asset);
	//GLMDebug.DEBUG(className_,clusterId_, "Cached name "+aTypeName+" w/ asset "+asset, 3);
	return asset;
    }
	
    /** Calls LDM factory to create prototype and set nomenclature and alternameTypeIdentification. 
     * @param typeid type identification of desired asset
     * @param type the class name of the desired asset
     * @param nomenclature **/
    protected Asset newAsset(String typeid, String type, String nomenclature) {
	// 	GLMDebug.DEBUG(className_, "newResource("+typeid+", "+type+", "+ nomenclature);
	Asset proto;
	try {
	    if (typeid == null)
		throw new IllegalArgumentException("newResource : "+typeid+
						   " nomenclature: "+ nomenclature);
	    proto = getLDM().getFactory().createPrototype(type, typeid);
	    // 	    GLMDebug.DEBUG(className_, "newResource(), typeid "+typeid+", nomenclature "+
	    // 			    nomenclature);
	    if (proto== null) {
  		GLMDebug.DEBUG(className_,  "newResource(), could not create prototype: "+
				   typeid);
		return null;
	    }
	    if (nomenclature != null) {
		NewTypeIdentificationPG pg = (NewTypeIdentificationPG) proto.getTypeIdentificationPG();
		pg.setNomenclature(nomenclature);
		pg.setAlternateTypeIdentification(pg.getTypeIdentification());
	    } else {
		GLMDebug.DEBUG(className_,"newResource:  "+typeid+
				   " no nomenclature ");
	    }   
	} catch (Exception ee) {
	    GLMDebug.ERROR(className_,"newResource() could not make "+typeid+" "+ee.toString());
	    ee.printStackTrace();
	    return null;
	}
	return proto;
    }
    
    // retrieve and parse the arguments
    // Currently expecting the query file as the only argument.
    protected void parseArguments() {
	// first, initialize the global table with some basics
	Vector pv = getParameters();
	if (pv == null) {
	    throw new RuntimeException("QueryLDMPlugIn requires at least one parameter");
	} else {
	    String s;
	    for (Enumeration ps = pv.elements(); ps.hasMoreElements(); ) {
		s = (String) ps.nextElement();
		if (s.indexOf('=') != -1) {
		    int i = s.indexOf('=');
		    String key = new String(s.substring(0, i));
		    String value = new String(s.substring(i+1, s.length()));
		    myParams_.put(key.trim(), value.trim());
		}
		else if (s.charAt(0) == '+') {
		    myParams_.put(new String(s.substring(1)), new Boolean(true));
		    //GLMDebug.DEBUG("QueryLDMPlugIn", getClusterIdentifier(), "parseArguments(), adding "+s.substring(1));
		}
		else if (s.charAt(0) == '-') {
		    myParams_.put(new String(s.substring(1)), new Boolean(false));
		    //GLMDebug.DEBUG("QueryLDMPlugIn", getClusterIdentifier(), "parseArguments(), adding "+s.substring(1));
		}
		else {
		    // If it was not a key/value pair then it is a query file
		    parseQueryFile(s);
		}
	    }
	}
    }

    /**
     * parses query file into local hashtable
     * @param queryFile name of query file
     * The query file is expected to be in the format
     * param_name=param_value
     * The necessary parameters are:  Database, Driver, Username, Password
     * Some parameters are not currently used, but may be in the future.
     * These inclue MIN_IN_POOL, MAX_IN_POOL, TIMEOUT, and NUMBER_OF_TRIES.
     * The rest of the lines are expected to be the query statements.
     * Here is an example from icisParts.q.
     Database=jdbc:oracle:thin:@hostname.alpine.com:(port):COUGAAR
     Driver = oracle.jdbc.driver.OracleDriver
     Username = icisUser
     Password = icisPassword
     MIN_IN_POOL= 1
     MAX_IN_POOL= 4
     TIMEOUT= 1
     NUMBER_OF_TRIES= 2
     
     headerQuery=select commodity, nsn, nomenclature, ui, ssc, price, icc, alt, plt, pcm, boq, diq, iaq, nso, qfd, rop, owrmrp, weight, cube, aac, slq from header where NSN = :nsns
     assetsQuery=select nsn, ric, purpose, condition, iaq from assets where NSN = :nsns
     nomen=select nomenclature from header where NSN = :nsns	
     cost=select price from header where NSN = :nsns
    **/
    protected void parseQueryFile(String queryFile) {
	try {
	    BufferedReader in = new BufferedReader(new InputStreamReader(getCluster().getConfigFinder().open(queryFile)));
	    for (String line = in.readLine(); line != null; line=in.readLine()) {
		line = line.trim();
		// skip empty lines
		if (line.length() == 0)
		    continue;

		// handle continuation lines
		int len;
		while ((len = line.length()) > 0 && '\\' == line.charAt(len-1)) {
		    line = (line.substring(0,len-1))+(in.readLine().trim());
		}

		char c = line.charAt(0);

		// skip comments
		if (c == '#') 
		    continue;

		// should be a param=value line
		line = Parameters.replaceParameters(line);
		parseQueryLine(line);
	    }
	    in.close();
      
	} catch (Exception e) {
	    GLMDebug.ERROR(className_,"Error reading '"+queryFile+"': "+e);
	    e.printStackTrace();
	    throw new RuntimeException("No QueryFile: "+e);
	}
    }

    protected void parseQueryLine(String s) {
	int i = s.indexOf('=');
	if (i < 1) {
	    GLMDebug.ERROR(className_, " parseQueryLine cannot parse <"+s+">");
	    return;
	}
	String p = s.substring(0,i).trim();
	String v = s.substring(i+1).trim();
	fileParameters_.put(p,v);
    }

    // document fileParameters_ hashtable
    protected String getParm(String name) {
	String value = (String)fileParameters_.get(name);
	if (value == null) {
	    throw new RuntimeException(this.toString()+": Couldn't initializeDriver need to specify "+name);
	}
	return value;
    }

    // initialize driver and obtain info to execute a query
    protected void initializeDriver() {
	String driverName = getParm("Driver");
        if (driverName != null) {
          try {
            DBConnectionPool.registerDriver(driverName);
          } catch (Exception e) {
            System.err.println("Could not register driver "+driverName+":");
            e.printStackTrace();
          }
        }
  	url_ = getParm("Database");
	user_ = getParm("Username"); 
	password_= getParm("Password");

	// PAS MIK - COUGAAR Node scope for connection pools
	int minPoolSize= Integer.parseInt(getParm("MIN_IN_POOL"));
	int maxPoolSize= Integer.parseInt(getParm("MAX_IN_POOL"));
	int timeout= Integer.parseInt(getParm("TIMEOUT"));
	// String queryFile = "";
	int nTries= Integer.parseInt(getParm("NUMBER_OF_TRIES"));
	//try {
	//GLMDebug.DEBUG(className_,"initialized driver");
    }

  /** @return Vector<Object[]> of results or null (on failure) **/
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
              row[i] = rs.getObject(i+1);
            }
            result.add((Object[])row.clone());
          }
          statement.close();
        } catch (java.sql.SQLException sqle) {
          GLMDebug.ERROR(className_,"executeQuery failed: "+sqle);
          sqle.printStackTrace();
          return null;
        } finally {
          if (conn != null)
            releaseConnection(conn);
        }
	return result;
    }

    protected Connection getConnection() throws SQLException {
      return DBConnectionPool.getConnection(url_, user_, password_);
    }

    protected void releaseConnection(Connection conn) {
	try {
          conn.close();
	} catch (java.sql.SQLException sqle) {
	    GLMDebug.ERROR(className_,"releaseConnection "+sqle);
	}
    }


}
