package org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.cougaar.domain.mlm.ui.newtpfdd.TPFDDConstants;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;

public class UnitQuery extends SqlQuery {
    public static final int DEPTH_BY = 1;
    public static final int DEPTH_TYPE = 2;
    public static final int DEPTH_ITIN = 3;
    public static final int DEPTH_LEAF = 4;
    
    private String unitID = null;
    private int treeDepth = 1;

  public UnitQuery (QueryData oldQuery) {
	if (oldQuery.getOtherCommand().equals (TPFDDConstants.SEND_UNIT_MANIFESTS)) {
	  setTreeDepth (DEPTH_TYPE);
	}
  }
  
    public String getUnitID() { return unitID; };
    public void setUnitID(String unitID) { this.unitID = unitID; };

    public int getTreeDepth() { return treeDepth; };
    public void setTreeDepth(int treeDepth) { this.treeDepth = treeDepth; };

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
}
