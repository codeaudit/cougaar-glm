package org.cougaar.domain.mlm.ui.newtpfdd.producer;

import java.sql.DriverManager;
import java.sql.Connection;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query.Query;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query.QueryResponse;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query.LineQuery;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query.UnitQuery;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query.HierarchyQuery;

import org.cougaar.domain.mlm.ui.newtpfdd.TPFDDConstants;

public class DataGrabberProducer extends ThreadedProducer {

  DataGrabber dataGrabber;
  String host;
  boolean debug = 
	"true".equals (System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabberProducer.debug", 
									   "false"));

  public static final int DRIVER_TYPE_SQL = 1;
  public static final int DRIVER_TYPE_ORACLE = 2;
  
  public DataGrabberProducer(String clusterName, ClusterCache clusterCache, String host) {
	super(clusterName+" Producer", clusterName, clusterCache);

	if (debug)
	  System.out.println ("DataGrabberProducer - ctor, clusterName " + clusterName);
	
	this.host = host;

	try {
	  Class.forName(System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabberProducer.dbDriverName"));
	} catch (Exception e) {
	  System.err.println("WARNING: Failed to Load Driver.\n"+e);
	}

	int connectionType = 0;
	String connectionTypeName = System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabberProducer.dbDriverType");
	if (connectionTypeName.equalsIgnoreCase("sql")) connectionType = DRIVER_TYPE_SQL;
	else if (connectionTypeName.equalsIgnoreCase("oracle")) connectionType = DRIVER_TYPE_ORACLE;
	else System.err.println("WARNING: Unknown dbDriverType");

	dataGrabber = new DataGrabberClient(/* allNodes,*/ createDBConnection(), connectionType);

  }

  public Object[] produce(Object query) {
	if (!(query instanceof QueryData)) {
	  System.err.println("ALERT: Non-Query sent to DataGrabberProducer");
	  System.err.println(query+" being sent.");
	  return null;
	} else {
	  Query modifiedQuery = manipulateQuery((QueryData)query);
	  QueryResponse response = dataGrabber.queryDatabase((Query)modifiedQuery);
	    
	  // You could put code here that handles Out-of-band info in QueryResponse

	  return response.getNodes();
	}	
  }

  public void cleanup() {
	dataGrabber.cleanup();
  }

  private Query manipulateQuery(QueryData query) {
	Query newQuery = null;

	if (query.getOtherCommand ().equals(TPFDDConstants.SEND_HIERARCHY)) {
	  newQuery = new HierarchyQuery (query);
	} else if (query.getOtherCommand ().equals(TPFDDConstants.SEND_UNIT_MANIFESTS)) {
	  newQuery = new UnitQuery (query);
	} else if (query.getOtherCommand ().equals(TPFDDConstants.SEND_LINES)) {
	  newQuery = new LineQuery (query);
	} else 
	  newQuery = new LineQuery (query);
	
	return newQuery;
  }
  private Connection createDBConnection() {
	Connection connect = null;
	String database = System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabberProducer.database",
										 "gatherer");
	
	String dbURL = "jdbc:mysql://" + host + "/" + database;
	
	String user  = System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabberProducer.dbUser",
									  "tops");
	String password = 
	  System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabberProducer.dbPassword", 
						 "tops");
	if (debug) 
	  System.out.println ("DataGrabberProducer.createdDBConnection - connecting to \n\tdatabase at\t<" + 
						  dbURL + ">\n\tuser\t<" + user + ">\n\tpassword\t<" + password + ">.");
	
	try {
	  connect = DriverManager.getConnection(dbURL, user, password);
	} catch (Exception e) {
	  System.err.println("WARNING: Failed to create DB Connection.\n"+e);
	  connect = null;
	}
	return connect;
  }

}

