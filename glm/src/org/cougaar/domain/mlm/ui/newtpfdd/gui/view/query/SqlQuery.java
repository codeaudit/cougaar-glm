package org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public abstract class SqlQuery implements Query {
  public QueryResponse getResponse (Connection connection, int connectionType) {
	QueryResponse response = new QueryResponse ();
	String sqlQuery = getSqlQuery ();
	
	ResultSet rs = null;
	try{
	  rs = getResultSet(connection, sqlQuery);
	  handleResult (rs, response);
	}finally{
	  if(rs!=null) {
		try { rs.close(); } catch (SQLException e){
		  System.out.println ("SqlQuery.getResponse - closing result set, got sql error : " + e); 
		}
	  }
	}

	return response;
  }

  public ResultSet getResultSet (Connection connection, String sqlQuery) {
	Statement s=null;
	try{
	  s=connection.createStatement();
	}catch(SQLException e){
	  System.out.println ("SqlQuery.getResponse - got sql error : " + e); 
	}
	if(s==null) {
	  System.out.println ("SqlQuery.getResponse - ERROR, statement null."); 
	  return null;
	}

	/*
	 * some sql here, probably built using a string buffer. ... 
	 * don't forget to handle dates and doubles in a DB safe way and to use ' quotes.  
	 * See /tops/src/org/cougaar/domain/mlm/ui/grabber/config/DBConfig 
	 * for examples of functions for doing oracle/my sql syntax)
	 */	

	ResultSet rs = null;
	try{
	  rs = s.executeQuery(sqlQuery);
	}catch(SQLException e){
	  System.out.println ("SqlQuery.getResponse - got sql error : " + e); 
	}

	try{
	  if(s!=null)
		s.close();
	}catch(SQLException e){
	  System.out.println ("SqlQuery.getResponse - got sql error : " + e); 
	}
	
	return rs;
  }

  protected abstract String getSqlQuery ();
  
  protected abstract void handleResult (ResultSet rs, QueryResponse response);
}
