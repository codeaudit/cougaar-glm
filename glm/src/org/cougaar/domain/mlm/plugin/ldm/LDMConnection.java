/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.ldm;
import java.sql.*;
import java.util.*;

public class LDMConnection implements Connection 
{

   private boolean available = true;
   private Connection con;
   private LDMConnectionPool pool;
   private long startTime;


   public LDMConnection (Connection con, LDMConnectionPool pool)
   {
      this.con = con;
      this.pool = pool;

   }// LDMConnection


   public void close() throws SQLException
   {
      pool.expireLease(this);
   }

   public Connection getConnection() 
   {
      return(con);
   }

   public synchronized boolean isAvailable()
   {
      return (available);
   }

   public synchronized boolean lease ()
   {
      boolean canLease = false;
    
      try {
      
         if (con != null) 
         {
            if (available && !con.isClosed())
            {
               available = false;
               canLease = true;
 			   startTime=System.currentTimeMillis();
            }		  
         }	  
      }// Try Block
      catch (Exception ex) 
      {
	     System.out.println("Exception raised");
	     ex.printStackTrace();
	  }// catch
  
      return (canLease);    

   }// lease

   public void expireLease()
   {
      available = true;
   }// expireLease

   public long getStartTime()
   {
      return (startTime);
   }// get startTime

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