/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.ldm;
import java.sql.*;
import java.util.*;

/** @deprecated Use org.cougaar.util.DBConnection instead. **/
public class LDMConnection implements Connection 
{
  private boolean available = true;
  private Connection con;

  public LDMConnection (Connection con, LDMConnectionPool pool)
  {
    this.con = con;

  }

  public void close() throws SQLException
  {
    con.close();
    con = null;
    available = false;
  }

  public Connection getConnection() 
  {
    return(con);
  }

  public synchronized boolean isAvailable()
  {
    return available && (con != null);
  }

  public synchronized boolean lease ()
  {
    if (available) {
      if (con != null) {
        available=false;
        return true;
      } else {
        available = false;
      }
    }
    return false;
  }

  public synchronized void expireLease()
  {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {}
      con = null;
    }
    available = false;
  }

  public long getStartTime()
  {
    return 0L;
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException 
  {
    return con.prepareStatement(sql);
  }

  public CallableStatement prepareCall(String sql) throws SQLException 
  {
    return con.prepareCall(sql);
  }

  public Statement createStatement() throws SQLException 
  {
    return con.createStatement();
  }

  public String nativeSQL(String sql) throws SQLException 
  {
    return con.nativeSQL(sql);
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException 
  {
    con.setAutoCommit(autoCommit);
  }

  public boolean getAutoCommit() throws SQLException 
  {
    return con.getAutoCommit();
  }

  public void commit() throws SQLException 
  {
    con.commit();
  }

  public void rollback() throws SQLException 
  {
    con.rollback();
  }

  public boolean isClosed() throws SQLException 
  {
    return con.isClosed();
  }

  public DatabaseMetaData getMetaData() throws SQLException 
  {
    return con.getMetaData();
  }

  public void setReadOnly(boolean readOnly) throws SQLException 
  {
    con.setReadOnly(readOnly);
  }
  
  public boolean isReadOnly() throws SQLException 
  {
    return con.isReadOnly();
  }

  public void setCatalog(String catalog) throws SQLException 
  {
    con.setCatalog(catalog);
  }

  public String getCatalog() throws SQLException 
  {
    return con.getCatalog();
  }

  public void setTransactionIsolation(int level) throws SQLException 
  {
    con.setTransactionIsolation(level);
  }

  public int getTransactionIsolation() throws SQLException 
  {
    return con.getTransactionIsolation();
  }

  public SQLWarning getWarnings() throws SQLException 
  {
    return con.getWarnings();
  }

  public void clearWarnings() throws SQLException 
  {
    con.clearWarnings();
  }
	
  // JDBC 2.0 stuff
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
  {
    return (con.createStatement(resultSetType, resultSetConcurrency));
  }
	
  public Map getTypeMap() throws SQLException
  {
    return(con.getTypeMap());
  }
	
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
  {
    return(con.prepareCall(sql, resultSetType, resultSetConcurrency));
  }
	
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
  {
    return(con.prepareStatement(sql, resultSetType, resultSetConcurrency));
  }
	
  public void setTypeMap(Map map) throws SQLException
  {
    con.setTypeMap(map);
  }

}// LDMConnection
