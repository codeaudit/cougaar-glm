package org.cougaar.domain.mlm.ui.newtpfdd.producer;

import java.sql.Connection;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.Query;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryResponse;

public class DataGrabberClient implements DataGrabber {
  Connection connection;
  int connectionType;
  
  public DataGrabberClient (Connection connection, int connectionType) {
	this.connection = connection;
	this.connectionType = connectionType;
  }
  
  /** Given a Query returns a QueryResponse */
  public QueryResponse queryDatabase(Query query) {
	QueryResponse response = query.getResponse (connection, connectionType);
	return response;
  }
  
  /** any necessary cleanup; may be empty */
  public void cleanup() {
  }
}
