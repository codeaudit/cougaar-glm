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

public class LDMConnectionPool
{

   private Vector connectionPool;
   private String url;
   private String user;
   private String password;
   private int maxPoolSize;
   private int minPoolSize;
   private int timeout;
   private int nTries;
   private LDMConnectionReaper theReaper;

   /************************************************************************************
    *
	* Method: LDMConnection Pool
	*
	* Description: This method serves as a constructor for the LDMConnectionPool class.
	*              
	*
	* Inputs:		url		- url for the database
	*				user	- user to login to the database as
	*			password	- password to login to the database
	*		    minSize		- The minimum size of the pool?
	*			maxSize		- The maximum size of the database pool
	*			timeout		- Specifies the time to reap unused or dead database 
	*						  connections.
	*			nTries		- The number of times to retry a database connection before
	*						  deeming that the connection is not accessible.
	* Outputs:	None
	***********************************************************************************/
   public LDMConnectionPool (String url, String user, String password, int minSize, int maxSize, int timeout, int nTries) 
   {

      this.url = url;
      this.user = user;
      this.password = password;
      this.timeout = timeout;
      this.nTries = nTries;   
      minPoolSize = minSize;
      maxPoolSize = maxSize;
      theReaper = new LDMConnectionReaper(this);
	  connectionPool = new Vector(maxPoolSize);
      theReaper.start();	  

   }// LDMConnectionPool

   /*********************************************************************************/
   /*
    * Method: removeStaleConnections
   	*
   	* Description: This method is called to remove any stale database connections
   	*
   	* Inputs:	   None
   	* Outputs:	   The JConnection class or null if a database connection can not
   	*			   be retrieved.
	*********************************************************************************/
   public synchronized void removeStaleConnections() 
   {

      long stale = System.currentTimeMillis() - timeout;
      Enumeration conns = connectionPool.elements();
	  
      // Loop Through the Pool to see if there are any stale connections
      while((conns != null) && (conns.hasMoreElements())) 
	  {	  
          LDMConnection conn = (LDMConnection)conns.nextElement();
		  
          if ((conn.isAvailable()) && (stale > conn.getStartTime()))
		     removeConnection(conn);
         
      }// while loop
   }// removeStaleConnections
   
   /*********************************************************************************/
   /*
    * Method: getConnection
	*
	* Description: This method is used to retrieve a connection for a given data
	*			   source.
	*
	* Inputs:	   None
	* Outputs:	   The JConnection class or null if a database connection can not
	*			   be retrieved.
	*********************************************************************************/
    public synchronized Connection getConnection() 
    {
       Connection c = null;
   	   Connection con = null;
	   LDMConnection jcon = null;
	
	   try 
	   {
	      // Check all of the connections to see if one is available
	      for (int i = 0; i < connectionPool.size(); i++) 
	      {
	         jcon = (LDMConnection)connectionPool.elementAt(i);          
			 
			 // Check to see if the connection can be leased.
	         if (jcon.lease())
			 {
	            return jcon;
			 }// if the connection can be leased.

	      }//for loop
	
	      // Since no connection was found, a new one has to be created.

	      if ((maxPoolSize <= 0) ||
              (connectionPool.size() < maxPoolSize))
          {
		     				   	  
	       	    // Try to access the database
			    // Keep on Trying until the nTries is reached.
                int tries = 1;
	            while ((con == null) && (tries <= nTries))
	            {		     		        
	 	          try 
			      {
	                 con = DriverManager.getConnection(url, user, password);
	              } catch (SQLException e)
		          {
					 System.out.println("The connection failed on attempt " + tries);
	                 tries++;
		          }//	
		  			 
                }//while loop	     

		  
		        // If a connection was successful then
		        // add the connection to the pool.
                if (con != null)
		        {
		    		     
	               jcon = new LDMConnection(con, this);
			       connectionPool.addElement(jcon);
	               jcon.lease();
		        }				  		 		  
             }// if maxPoolSize
       
       } catch(Exception e)
       {
          c = null;
       }// try exception block
       return (jcon);
   }// getConnection
   
   /***********************************************************************************
    *
	* Method: closeAll
	*
	* Description:	This method is used to close all the database connections in the 
	*				vector
	*
	* Inputs:		None
	*
	* Outputs:		None
	***********************************************************************************/
   public synchronized void closeAll()
   {
      Enumeration conns = connectionPool.elements();
   
      while((conns != null) && (conns.hasMoreElements())) 
	  {
         LDMConnection conn = (LDMConnection)conns.nextElement();
         removeConnection(conn);
      }// while loop

   }// closeAll
   
   /***********************************************************************************
    *
   	* Method: removeConnection
   	*
   	* Description:	This method is used to remove a specific connection.
   	*
   	* Inputs:		None
   	*
   	* Outputs:		None
	***********************************************************************************/
   private synchronized void removeConnection(LDMConnection conn) 
   {
      connectionPool.removeElement(conn);
      try {
	  conn.getConnection().close();
      } catch (SQLException sqle) {
	  System.out.println("Exception closing connection!!!");
      }
   }
   
   /***********************************************************************************
    *
   	* Method: expireLease
   	*
   	* Description:	This method is used to expire a given database connection.  When
	*				this method is called, the database connection is deemed expired,
	*				but continues to stay open.
   	*
   	* Inputs:		None
   	*
   	* Outputs:		None
	***********************************************************************************/
   protected synchronized void expireLease(LDMConnection conn)
   {
      conn.expireLease();
   }// expireLease


}// LDMConnectionPool

/***********************************************************************************
 *
 * Method: LDMConnectionReaper
 *
 * Description:	This class, when instantiated, serves as a thread to remove
 *				unused database connections.
 *
 * Raytheon Systems Company MML
 **********************************************************************************/
class LDMConnectionReaper extends Thread 
{
    // References the actual database pool
    private LDMConnectionPool connectionPool;

    /*******************************************************************************
	 *
	 * Method: LDMConnectionReaper
	 *
	 * Description:		This method serves as the constructor for this class.
	 *
	 * Inputs:			The database pool it is associated with
	 *
	 * Outputs:			None
	 *
	 ********************************************************************************/
    LDMConnectionReaper(LDMConnectionPool pool) 
	{
        this.connectionPool=pool;
    }// LDMConnectionReaper
    
	/*******************************************************************************
	 *
	 * Method: run
	 *
	 * Description:		When the thread is started, this run method is invoked.
	 *				    After every 10 seconds, the stale connections are removed.
	 *
	 * Inputs:			The database pool it is associated with
	 *
	 * Outputs:			None
	 *
	 ********************************************************************************/
    public void run() 
	{
	    // Loop and remove stale connections 
		// after 10 seconds
        while(true) 
		{
           try 
		   {
              sleep(10000);
           } catch( InterruptedException e) 
		   { }
		   
		   // Remove all the stale connections
           connectionPool.removeStaleConnections();
        }// while loop
    }// run
	
}// LDMConnectionReaper
