package org.cougaar.domain.mlm.ui.newtpfdd.producer;

import java.sql.DriverManager;
import java.sql.Connection;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.Query;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryResponse;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;

public class DataGrabberProducer extends ThreadedProducer {

    DataGrabber dataGrabber;

    public static final int DRIVER_TYPE_SQL = 1;
    public static final int DRIVER_TYPE_ORACLE = 2;

    public DataGrabberProducer(String clusterName, ClusterCache clusterCache) {
	super(clusterName+" Producer", clusterName, clusterCache);

	try {
	    Class.forName(System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabber.Producer.dbDriverName"));
	} catch (Exception e) {
	    System.err.println("WARNING: Failed to Load Driver.\n"+e);
	}

	int connectionType = 0;
	String connectionTypeName = System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabber.Producer.dbDriverType");
	if (connectionTypeName.equalsIgnoreCase("sql")) connectionType = DRIVER_TYPE_SQL;
	else if (connectionTypeName.equalsIgnoreCase("oracle")) connectionType = DRIVER_TYPE_ORACLE;
	else System.err.println("WARNING: Unknown dbDriverType");

	dataGrabber = new DataGrabberClient(allNodes, createDBConnection(), connectionType);

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
	// SHOULD DO SOMETHING
	return newQuery;
    }
    private Connection createDBConnection() {
	Connection connect = null;
	try {
	    connect = DriverManager.getConnection(System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabber.Producer.dbURL"),
					    System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabber.Producer.dbUser"),
					    System.getProperty("org.cougaar.domain.mlm.ui.newtpfdd.producer.DataGrabber.Producer.dbPassword"));
	} catch (Exception e) {
	    System.err.println("WARNING: Failed to create DB Connection.\n"+e);
	    connect = null;
	}
	return connect;
    }

}
