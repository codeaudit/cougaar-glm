package org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public abstract class SqlQuery implements Query {
  public QueryResponse getResponse (Connection connection, int connectionType) {
	QueryResponse response = new QueryResponse ();
	Statement s=null;
	try{
	  s=connection.createStatement();
	}catch(SQLException e){
	  System.out.println ("SqlQuery.getResponse - got sql error : " + e); 
	}
	if(s==null) {
	  System.out.println ("SqlQuery.getResponse - ERROR, statement null."); 
	  return response;
	}

	/*
	 * some sql here, probably built using a string buffer. ... 
	 * don't forget to handle dates and doubles in a DB safe way and to use ' quotes.  
	 * See /tops/src/org/cougaar/domain/mlm/ui/grabber/config/DBConfig 
	 * for examples of functions for doing oracle/my sql syntax)
	 */	

	String sqlQuery = getSqlQuery ();
	
	ResultSet rs = null;
	try{
	  rs = s.executeQuery(sqlQuery);
	  handleResult (rs, response);
	}catch(SQLException e){
	  System.out.println ("SqlQuery.getResponse - got sql error : " + e); 
	}finally{
	  if(rs!=null) {
		try { rs.close(); } catch (SQLException e){
		  System.out.println ("SqlQuery.getResponse - closing result set, got sql error : " + e); 
		}
	  }
	}

	try{
	  if(s!=null)
		s.close();
	}catch(SQLException e){
	  System.out.println ("SqlQuery.getResponse - got sql error : " + e); 
	}
	
	return response;
  }

  protected abstract String getSqlQuery ();
  
  protected abstract void handleResult (ResultSet rs, QueryResponse response);
}
