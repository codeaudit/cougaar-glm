package org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.Node;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.RollupNode;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;

public class HierarchyQuery extends SqlQuery {
  String root;
  
  boolean debug = 
	"true".equals (System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.gui.view.HierarchyQuery.debug", 
									   "false"));

  /** 
   * by default returns demand hierarchy, 
   * but setting setRoot to TRANSCOM will return TOPS hierarchy 
   */
  public HierarchyQuery (QueryData oldHierarchyQuery) {
	setLeavesOnly(false); // when should this be true?
	root = "HigherAuthority";
  }
  
  public void setRoot (String root) { this.root = root;  }

  /** 
   * does a join between org descend table and org table to get only those orgs
   * that descend from root 
   *
   * e.g.
   * mysql> select org_1.org_id, org_1.related_id from org_1, orgdescend_1 where orgd
   * escend_1.org_id = 'HigherAuthority' and orgdescend_1.descendent_id = org_1.org_i
   * d;
   * +-----------------+------------+
   * | org_id          | related_id |
   * +-----------------+------------+
   * | HigherAuthority | XVIIICorps |
   * | HigherAuthority | VIICorps   |
   * | XVIIICorps      | 3BDE       |
   * | 3BDE            | 1BDE       |
   * | 1BDE            | 3-69-ARBN  |
   * | 1BDE            | 3-72-ARBN  |
   * | VIICorps        | 9-FRE      |
   * | 9-FRE           | 10--KJR    |
   * +-----------------+------------+
   *
   * Uses maps to keep track of which nodes have already been created.
   */
  public QueryResponse getResponse (Connection connection, int connectionType) {
	QueryResponse response = new QueryResponse ();
	
	// first figure out which run to use
	int recentRun = getRecentRun (connection);
	
	ResultSet rs = null;
	try{
	  String orgTable = "org_" + recentRun;
	  String orgDescendTable = "orgdescend_" + recentRun;
	  
	  String sqlQuery = 
		"select " + orgTable + ".org_id, " + orgTable + ".related_id" + 
		" from "  + orgTable + ", " + orgDescendTable + 
		" where " + orgDescendTable + ".org_id = '" + root + "' and " + 
		orgDescendTable + ".descendent_id = " + orgTable + ".org_id";

	  Map nameToNode = new HashMap ();
	  
	  rs = getResultSet(connection, sqlQuery);
	  while(rs.next()){
		String cluster  = rs.getString ("related_id");
		String superior = rs.getString ("org_id");
		
		if (debug) 
		  System.out.println ("HierarchyQuery.getRecentRun - " + 
							  cluster + "\tsuperior is " + superior);

		// why is the first map necessary?  Can the map live in the tpfdd shell instead?
		Node clusterNode  = null;
		Node superiorNode = null;
		if ((clusterNode = (Node) nameToNode.get (cluster)) == null) {
		  clusterNode = new RollupNode (new HashMap(), cluster);
		  response.addNode (clusterNode);
		  nameToNode.put (cluster, clusterNode);
		}
		if ((superiorNode = (Node) nameToNode.get (cluster)) == null) {
		  superiorNode = new RollupNode (new HashMap(), cluster);
		  response.addNode (superiorNode);
		  nameToNode.put (cluster, superiorNode);
		}
		clusterNode.setParent (superiorNode);
		superiorNode.addChild (clusterNode);
	  }

	  // go through a second time to set relationships
	  /*
	  rs = getResultSet(connection, sqlQuery);
	  while(rs.next()){
		String cluster  = rs.getString ("related_id");
		String superior = rs.getString ("org_id");
		
		if (debug) 
		  System.out.println ("HierarchyQuery.getRecentRun - (2) " + 
							  cluster + "\tsuperior is " + superior);
		Node clusterNode  = (Node) nameToNode.get (cluster);
		Node superiorNode = (Node) nameToNode.get (superior);
		clusterNode.setParent (superiorNode);
		superiorNode.addChild (clusterNode);
	  }

	  */
	} catch (SQLException e) {
		System.out.println ("HierarchyQuery.getResponse - SQLError : " + e);
	}finally{
	  if(rs!=null) {
		try { rs.close(); } catch (SQLException e){
		  System.out.println ("SqlQuery.getResponse - closing result set, got sql error : " + e); 
		}
	  }
	}

	return response;
  }

  /** 
   * for now just the latest run with condition =1, 
   * later, something more sophisticated.
   */
  protected int getRecentRun (Connection connection) {
	ResultSet rs = null;
	int runToUse = 1;
	try{
	  // first figure out which run to use
	  String sqlQuery = "select * from runtable where condition = '1'";
	  rs = getResultSet(connection, sqlQuery);
	  while(rs.next()){
		runToUse = rs.getInt ("runid");
		if (debug) System.out.println ("HierarchyQuery.getRecentRun - Run id " + rs.getInt ("runid"));
		//        process a row as you see fit.  use rs.getXXXX()
	  }
	} catch (SQLException e) {
		System.out.println ("HierarchyQuery.getResponse - SQLError : " + e);
	}finally{
	  if(rs!=null) {
		try { rs.close(); } catch (SQLException e){
		  System.out.println ("SqlQuery.getResponse - closing result set, got sql error : " + e); 
		}
	  }
	}

	return runToUse;
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
