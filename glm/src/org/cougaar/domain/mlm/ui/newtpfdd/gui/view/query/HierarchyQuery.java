package org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;

public class HierarchyQuery extends SqlQuery {
  public HierarchyQuery (QueryData oldHierarchyQuery) {
	setLeavesOnly(false); // when should this be true?
  }

  /**
   * some sql here, probably built using a string buffer. ... 
   * don't forget to handle dates and doubles in a DB safe way and to use ' quotes.  
   * See /tops/src/org/cougaar/domain/mlm/ui/grabber/config/DBConfig 
   * for examples of functions for doing oracle/my sql syntax)
   */	
  protected String getSqlQuery () {
	StringBuffer sb = new StringBuffer ();
	
	sb.append ("");
	return sb.toString ();
  }
  
  protected void handleResult (ResultSet rs, QueryResponse response) {
	try {
	while(rs.next()){
	  //        process a row as you see fit.  use rs.getXXXX()
	}
	} catch (SQLException e) {
	}
  }
  
  private boolean leavesOnly = false;
  public boolean getLeavesOnly() { return leavesOnly; };
  public void setLeavesOnly(boolean leavesOnly) { this.leavesOnly = leavesOnly; };
}
