/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.ldm;

/********************************************************************************
 *
 * Class Name:	LDMQueryPlugIn
 *
 * Description:	

   The LDMQuery PlugIn is used to query contemporary (external) databases to retrieve 
   information about physical assets.  This plugIn is built on functionality found in
   the LDMQSQLPLugIn.  Once retrieved, the LDMQueryPlugIn stores asset 
   information in the given cluster’s Log Plan as assets and properties.  
   Assets and properties can then be retrieved by other cluster plug-ins using the interfaces 
   available to the Log Plan.

   The LDMQuery Plugin queries either local or remote data sources using Java Database 
   Connectivity (JDBC).  In future releases, the LDM Query PlugIn may provide a Layered 
   Initialization mechanism for querying databases using mechanisms other than JDBC. 

   When the LDMQuery PlugIn first initializes, it retrieves a directory and a UIC parameter. 
   The directory parameter specifies the directory where the database configuration and query
   files are stored.   The database configuration file(s) contain information about the 
   databases that are to be used by the given cluster.  The following figure shows an example 
   of such a configuration file.


   The database name is composed of the url and actual database name. 
   The database driver represents the type of JDBC driver to use for a given database. 
   The user id and passowrd are necessary so that connection to a given database can be successfully.  
   The minimum and maximum number represents the minimum and maximum number of connections
   for a given database pool.  The number of tries specifies the number of attempts that 
   should be made to connect to a given database.

   The LDMQuery Plug-in also uses a query file. The query file lists all of the possible 
   queries that may be used when retrieving prototype and property information for a given 
   cluster. The query file is used to associate various queries with a given prototype or 
   property.  Lines starting with “%” indicate the start of a query section and the rest 
   of the line specifies the speicy QueryHandler class to use.  Lines beginning with the 
   word “DB_NAME” represent the database that is to be used to create the given prototype, 
   property or asset within the Log Plan.  Proceeding each database line is a corresponding 
   query line.   				
   
  
   
   ********************************************************************************/
import org.cougaar.core.plugin.LDMPlugInServesLDM;
import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.SubscriberException;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.util.Parameters;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.DomainServiceProvider;
import org.cougaar.domain.planning.ldm.DomainServiceImpl;
import org.cougaar.domain.planning.ldm.DomainService;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FilenameFilter;

import java.sql.*;

class CFGFilter implements FilenameFilter 
{
  public boolean accept(File dir, String name) 
  {
    return (name.endsWith(".cfg"));
  }
}

class QFilter implements FilenameFilter 
{
  public boolean accept(File dir, String name) 
  {
    return (name.endsWith(".q"));
  }
}
 

public class LDMQueryPlugIn extends LDMEssentialPlugIn
{

  private String queryFileDirectory;
  private String queryFile;
  private String configFile;
  private Vector prototypeProviders = new Vector();
  private Vector propertyProviders = new Vector();

		private DomainService domainService = null;
		
  // Vector of LDMConnection Driver Pools
  private static Vector allDBConnections = new Vector();


  private Properties globalParameters = new Properties();

  private Vector queries = new Vector();
  private static int numDatabases = 0;


  /********************************************************************************
   *
   * Method Name: executeQuery
   *
   * Description: This routine executes a given SQL statement and sends the 
   *				results to the given QueryHandler.
   * 
   * Input:		rawSQL - SQL from QueryHandler
   *				qh	   - A given QueryHandler
   *				dbName - The database Name
   *
   * Output:		success - returns a true if the connection was made successfully.
   *						  else returns false.
   *
   ********************************************************************************/
  public boolean executeQuery(String rawSql, QueryHandler qh, String dbName) 
  {
    boolean success = false;
    String db = null;
    LDMConnectionDriver jDriver;
    LDMConnectionDriver actualDriver = null;
    int cCount = 0;
    Connection conn;
   
    //System.out.println("executeSQL: beginning");   
    //System.out.println("The Dbname is " + dbName);
    // Check to see if the name is valid
    if (dbName == null) 
      {
        // Error
        return false;
      }

    //System.out.println("The size of the connections is " +allDBConnections.size());   
    while (cCount < allDBConnections.size())
      {

        jDriver = (LDMConnectionDriver)allDBConnections.elementAt(cCount);
	  
        //System.out.println("The jDriver db name is " + jDriver.getDBName());
        //System.out.println("The given dbame is " + dbName);

        if (jDriver.getDBName().equals(dbName))
	  {
            //System.out.println("The names Equal");	  
            db = dbName;
            actualDriver = jDriver;
            // Quit out of both loops
            cCount = allDBConnections.size();
          }// if statement
        else
          cCount++;
      }// while loop
    
    //System.out.println("The database found was  " + actualDriver.getDBName());
   
    if (actualDriver != null)
      {
        try 
          {
            //System.out.println("executeSQL:About to try and connect to " + actualDriver.getDBName());	  
            // If the particular database URL has failed int the past, then do not use it
            // any more.
            conn = null;
            if (actualDriver.isWorking())
              conn = actualDriver.connect();
            else
              return false;
	  
            // If the connection was unsucessful,
            // then tell the caller to try again with
            // another type of raw SQL query.
            if (conn == null)
              {		 
                //System.out.println("executeSQL:Connection Failed :(");		 
	        return success;
              }			
		 
            // The connection was sucessful	 
            else
              {
                //System.out.println("executeSQL:connection worked");		 
                // Produce the appropriate query      
                String sql;  
                //System.out.println("THe SQL that is going to be used in produceQuery is " + rawSql);			
                sql = produceQuery(qh, rawSql);
                //System.out.println("The resulting SQL is " + sql);	


                // A test to see what happens

                Statement statement = conn.createStatement();
                //System.out.println("executeSQL:After createStatement\n");			
                //System.out.println("executeSQL:About to query using " + sql);	   
                ResultSet rset = statement.executeQuery(sql);      
                //System.out.println("executeSQL:After executeQuery\n");	

                ResultSetMetaData md = rset.getMetaData();
                //System.out.println("executeSQL:After getMetaData\n");	
                int ncols = md.getColumnCount();
                //System.out.println("executeSQL:After getColumnCount"  + ncols);				

                Object row[] = new Object[ncols];
    
                while (rset.next()) 
                  {
                    for (int i = 0; i < ncols; i++)
                      {
			      
                        row[i] = rset.getObject(i+1);
 
                        //System.out.println("executeSQL:I see equipment characteristics for : " + row[i]);
   
                      }

                    // Give the results back to the caller.
                    qh.processRow(row);
                  }// while loop
		
   
                statement.close();
                conn.close();
                success=true;
 		

              }// else statement
		  
          } catch (Exception e) 
            {
              System.err.println("Caught exception while executing a query: "+e);
              e.printStackTrace();
              return false;			
            }
      }//else statement
    //System.out.println("executeSQL: end");   	 
    return success;
  
  }//executeQuery

  public Asset getPrototype(String typeid) {
    return getPrototype(typeid, null);
  }
  public Asset getPrototype(String typeid, Class hint)
  {
     
    for (Enumeration e = prototypeProviders.elements(); e.hasMoreElements();)
      {
        PrototypeProvider pp = (PrototypeProvider) e.nextElement();
        if (pp.canHandle(typeid)) 
          {
            Asset a = pp.getAssetPrototype(typeid);

            return a;
          }
      }// for loop
    return null;
  }

  /********************************************************************************
   *
   * Method Name: setupSubscriptions
   *
   * Description: This routine is called by the cluster infrastucture to
   *				start execution of this LDMPlugin
   * 
   * Input:		None
   *
   * Output:		None
   *
   ********************************************************************************/
  protected void setupSubscriptions() 
  {

    //System.out.println("LDMQueryPlugIn!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    // first, initialize the global table with some basics

 
    // set up the subscription
    // This could be a future site for maintaining a Container of created
    // LDMObjects for future updating.
	
			// get domain service - factory
	 		domainService = (DomainService) getBindingSite().getServiceBroker().getService(
				 																					 this, DomainService.class,
																									 null);
			
			
    if (!getBlackboardService().didRehydrate()) 
      {	// Objects should already exist after rehydration
        try 
          {
            // set up initial properties
            initProperties();
	  
            // deal with the arguments.
            grokArguments();
	  
            // MML commented out and changed logic to find an individual file
            // instead of looking for files in a specified directory.
            // Parse the configuration files.
            //boolean filesFound = parseConfigFiles(queryFileDirectory);
            //if (!filesFound)
            //{
            // there is a problem
            //}
	  
            try
              {
	        // Locate the .cfg file
	        InputStreamReader config = new InputStreamReader(getConfigFinder().open(configFile));
		  
	        parseConfigFile(config);
	   
                config.close();
              } catch (Exception e)
                {         
                  String message = e.getMessage();
                  //System.out.println("Exception" + message);
                  e.printStackTrace();
                }
	  
            // MML commented out and changed logic to find an individual file
            // instead of looking for files in a specified directory.
            // parse the query files and store properties in the appropriate
            // queryhandlr.
            //filesFound = parseQueryFiles(queryFileDirectory);
            //if (!filesFound)
            //{
            //	     // there is a problem
            //}
            try
              {
	        InputStreamReader qs = new InputStreamReader(getConfigFinder().open(queryFile));	 
	        parseQueryFile(qs);
                qs.close();
              } catch (Exception e)
                {         
                  String message = e.getMessage();
                  System.err.println("Exception: " + message);
                  e.printStackTrace();
                }
	  
            // sort the queryhandlers into categories.
            grokQueries();        
          } 
        catch (SubscriberException se) 
          {
            System.err.println("Caught: "+se);
          }
      }// if statement

  }// setupSubscriptions
  /********************************************************************************
   *
   * Method Name:	grokArguments
   *
   * Description:	This method retrieves and stores the arguments passed in to the
   *			 	this plugin
   * 
   * Input:		  	None
   *
   * Output:	  	None
   *
   ********************************************************************************/
  private void grokArguments() 
  {
    // first, initialize the global table with some basics    
    Vector pv = getParameters();
  	 
      
    if (pv.isEmpty()) 
      {
        throw new RuntimeException("LDMPlugIn requires at least one parameter");
      } 
    else 
      {
        int parameter = 0;
  		
        for (Enumeration ps = pv.elements(); ps.hasMoreElements(); ) 
          {
            String p = (String) ps.nextElement();
  		   
            // The item in the vector is a directory
            if (parameter == 0) 	         
              {
    	        queryFile =  p;
                //System.out.println("First Parameter is " + queryFile);			    			  
              }
            else if (parameter == 1)
              {
                configFile = p;
                //System.out.println("The second parameter is " + configFile);			  
              }
            else
              {
                parseQueryParameter(globalParameters, p);					  
              }	  
            parameter++;
          }//for loop
        if (parameter != 3) 
          {
            // no args
            throw new RuntimeException("LDMPlugIn requires at least three parameters");
          }
      }// else
  }// grokArguments

  /********************************************************************************
   *
   * Method Name:	grokDirArguments
   *
   * Description:	This method retrieves and stores the arguments passed in to the
   *			 	this plugin
   * 
   * Input:		  	None
   *
   * Output:	  	None
   *
   ********************************************************************************/
  private void grokDirArguments() 
  {
    // first, initialize the global table with some basics    
    Vector pv = getParameters();
	 
    
    if (pv.isEmpty()) 
      {
        throw new RuntimeException("LDMPlugIn requires at least one parameter");
      } 
    else 
      {
        boolean isFirst = true;
		
        for (Enumeration ps = pv.elements(); ps.hasMoreElements(); ) 
          {
            String p = (String) ps.nextElement();
		   
            // The item in the vector is a directory
            if (isFirst) 	         
              {
                queryFileDirectory =  p;
                //System.out.println("First Parameter is " + queryFileDirectory);			  
                isFirst = false;
              }
            else 
              {
                parseQueryParameter(globalParameters, p);					  
              }	  
	  
          } // for statement
        if (isFirst) 
          {
            // no args
            throw new RuntimeException("LDMPlugIn requires at least one parameter");
          }
      }//else statement
  }// grokDirArguments
  
  /** sort queryhandlers into categories.
   * PeriodicQueries will get executed synchronously for the first
   * time here.
   **/
  /********************************************************************************
   *
   * Method Name:	grokQueries
   *
   * Description:	Sort queryhandlers into categories. PeriodicQueries will get 
   *				executed synchronously for the first time here.
   * 
   * Input:		  	None
   *
   * Output:	  	None
   *
   ********************************************************************************/   
  private void grokQueries()
  {
    for (Enumeration e = queries.elements(); e.hasMoreElements();)
      {

        QueryHandler qh = (QueryHandler) e.nextElement();
        qh.start();
        if (qh instanceof org.cougaar.domain.mlm.plugin.ldm.PrototypeProvider) 
          {

            prototypeProviders.addElement(qh);
          }  
        else if (qh instanceof org.cougaar.domain.mlm.plugin.ldm.PropertyProvider) 
          {

            propertyProviders.addElement(qh);
          } 
        // else it was just a PeriodicQuery.
      }// for loop
  }// grokQueries
  /********************************************************************************
   *
   * Method Name:	initProperties
   *
   * Description:	This method stores any generic properties.
   * 
   * Input:			None
   *
   * Output:	  	None
   *
   ********************************************************************************/
  private void initProperties() 
  {
    // default package for QueryHandler
    globalParameters.put("Package", "org.cougaar.domain.mlm.plugin.ldm");
  }  
  /********************************************************************************
   *
   * Method Name:	parseConfigFiles
   *
   * Description:	This method searches for the given database configuration [.cfg] files 
   * 				in a given directory.  It then calls parseDatabaseFile for 
   *				further processing.
   *				
   * Input:			directory - place where configuration and query files are stored.
   *
   * Output:	  	None
   *
   ********************************************************************************/
  private boolean parseConfigFiles(String directory)
  {
    // Get the File Pointer for the Directory
    // and store it in configDirectory
    //System.out.println("LDMQueryPlugIn:parseConfigFiles BEGIN " + directory);
    File configDirectory = new File(directory);
    boolean cfgFound = false;
	  
	
	  
    // Obtain a listing of all the *.cfg files in the directory
    // Using a filter 
	  
    if (configDirectory.isDirectory()) 
      {	  
      
        FilenameFilter filter = new CFGFilter();
        String files[] = configDirectory.list(filter);	  
	  
        for (int i=0; i < files.length; i++)
          {
            //System.out.println("First file and directory is " + configDirectory+files[i]);		
            try
              {
                // Get File instance for each file
                FileReader configFile = new FileReader(configDirectory+"\\"+files[i]);
		      
                cfgFound = true;
  	  
                // Parse file name before .cfg and store the result in queryFile Vector
                // Instantiate the File object.
                parseConfigFile(configFile);  	     
			  
              } catch (Exception e)
                {
		         
                  String message = e.getMessage();
                  System.err.println("Exception: " + message);
                  e.printStackTrace();
                }
  	     
          }// for loop
      }// if statement
	  
    return (cfgFound);
     
  }// parsefiles
  /********************************************************************************
   *
   * Method Name:	parseConfigFile
   *
   * Description:	This method parses the given database configuration file,
   *				instantiates a connection and stores the connection
   *				
   * Input:			configFile - The given configuration file to parse.
   *
   * Output:	  	None
   *
   ********************************************************************************/     
  private void parseConfigFile(InputStreamReader configFile) {
    String line;
    String driver = "";
    String database = "";
    String userID = "";
    String queryFile = "";
    String password = "";
    int minPoolSize = 0;
    int maxPoolSize = 0;
    int timeout = 0;
    int nTries = 0;
    int indexToUse;
  	  
	 
    try
      {		
        BufferedReader in = new BufferedReader(configFile);		
        line = (in.readLine()).trim();
        //System.out.println("Line Read in " + line);			 
        while (line != null)
          {		  
            line = Parameters.replaceParameters(line); // parameterize
      
            indexToUse = line.indexOf('=');
            //System.out.println("Index of is " + indexToUse);			
            if (line.startsWith("DB_NUM#"))
              {
                String dbNum = (line.substring(indexToUse+1, line.length())).trim();
                numDatabases++;
                //System.out.println("DB_Num is " + dbNum);
				
              }// DB_NUM
            else if (line.startsWith("DB_NAME"))
              {
                database = (line.substring(indexToUse+1, line.length())).trim();
                //System.out.println("DB_Num is " + database);			   
              }// DB_NAME
            else if (line.startsWith("DB_DRIVER"))
              {
                driver = (line.substring(indexToUse+1, line.length())).trim();
                //System.out.println("DB_DRIVER is " + driver);			   
              }// DB_DRIVER
            else if (line.startsWith("USER"))
              {
                userID = (line.substring(indexToUse+1, line.length())).trim();
                //System.out.println("USER is " + userID);			   
              }// USER
            else if (line.startsWith("MIN_IN_POOL"))
              {
                minPoolSize = (new Integer( (line.substring(indexToUse+1, line.length()).trim()))).intValue();
                //System.out.println("MIN_POOL_SIZE is " + minPoolSize);			   
              }// MIN_POOL_SIZE
            else if (line.startsWith("MAX_IN_POOL"))
              {
                maxPoolSize = (new Integer( (line.substring(indexToUse+1, line.length()).trim()))).intValue();
                //System.out.println("MAX_POOL_SIZE is " + maxPoolSize);
              }// MAXPOOLSIZE
            else if (line.startsWith("NUMBER_OF_TRIES"))
              {
                nTries = (new Integer(line.substring(indexToUse+1, line.length()).trim())).intValue();
                //System.out.println("NUMBER OF TRIES is " + nTries);				   

                LDMConnectionDriver jDriver  = new LDMConnectionDriver(driver, 
                                                                       database, 
                                                                       userID, 
                                                                       password,
                                                                       minPoolSize,
                                                                       maxPoolSize,
                                                                       timeout,
                                                                       queryFile,
                                                                       nTries);			
                allDBConnections.addElement(jDriver);					
		   

              }// NUMBER OF TRIES
            else if (line.startsWith("TIMEOUT"))
              {
           
                timeout = (new Integer( (line.substring(indexToUse+1, line.length()).trim()))).intValue();
                //System.out.println("TIMEOUT is " + timeout);	
		
		   
              }// TIMEOUT
            else if (line.startsWith("PASSWORD"))
              {
                password = (line.substring(indexToUse + 1, line.length())).trim();
                //System.out.println("PASSWORD is " + password);			   
              }// PASSWORD			
						
            String newLine = in.readLine();
				
            if (newLine != null)
              line = newLine.trim();
            else
              line = null;
				
          }// while loop
      } catch (Exception e)
        {
          //String message = e.getMessage();
          //System.out.println("Exception " + message);
          e.printStackTrace();
        }
	
    //System.out.println("END OF parseConfigFile");  
    
  }//parseConfigFile
  
  /********************************************************************************
   *
   * Method Name:	parseQueryFiles
   *
   * Description:	This method searches for all of the query files [.q] in a given
   *				directory.  It then calls parseQueryFile for further processing.
   *				
   * Input:			directory - The given directory
   *
   * Output:	  	None
   *
   ********************************************************************************/  
  private boolean parseQueryFiles(String directory)
  {
    File queryFileDirectory = new File(directory);
    boolean qFound = false;
  	  
    // Get the File Pointer for the Directory
    // and store it in queryFileDirectory
  	  
    // Obtain a listing of all the *.q files in the directory
    // Using a filter 
    FilenameFilter filter = new QFilter();
    String[] files = queryFileDirectory.list(filter);
  	  
  	  
    for (int i=0; i < files.length; i++)
      {
        try
          {
            // Get File instance for each file
            FileReader queryFile = new FileReader(directory+"\\"+files[i]);
			
            // A query file has been found
            qFound = true;
    	  
    	    // Parse file name before .cfg and store the result in queryFile Vector
       	    // Instantiate the File object.
    	    parseQueryFile(queryFile);  	     
			
          } catch (FileNotFoundException e) 
            {
              e.printStackTrace();
            }// catch
    	     
      }	
    return (qFound);
	  
  }// parseQueryFiles  
	
  /********************************************************************************
   *
   * Method Name:	parseQueryFile
   *
   * Description:	This method parses the given query file,
   *				instantiates the appropriate queryhandler (which is based on the
   *				queryhandler specified in the queryfile), and stores the appropriate
   *				sql query information along with the database name.
   *				
   * Input:			queryFile - The given query file to parse.
   *
   * Output:	  	None
   *
   ********************************************************************************/   
  private void parseQueryFile(InputStreamReader queryFile) 
  {
     	 	 			
    try 
      {
        BufferedReader in = new BufferedReader(queryFile);
        Properties pt = null;
			  
        for (String line = in.readLine(); line != null; line=in.readLine())
          {
			  
            line = line.trim();
			  				
            // skip empty lines
            if (line.length() == 0)
              continue;
			  
            int len;
            // handle continuation lines
            while ((len = line.length()) > 0 && '\\' == line.charAt(len-1)) 
              {
                line = (line.substring(0,len-1))+(in.readLine().trim());
              }
            line = Parameters.replaceParameters(line); // parameterize
			  
            char c = line.charAt(0);
			  
            // skip comments
            if (c == '#')
              continue;
			  
            // queryhandler section
            if (c == '%') 
              {
                String s = line.substring(1).trim(); // skip the %
                if (s == null || s.length() == 0 || s.equals("Global"))
                  {
			     
                    // global handler parameters
                    pt = null;
                  }
                else
                  {
                    // s names a queryhandler class (for now)
                    try
                      {
                        if (s.indexOf('.') < 0)
                          { 
                            // if the class has no package..
                            // try the default package (which is the same as ours)
                            String pkg = globalParameters.getProperty("Package");
                            if (pkg != null)
                              {
                                s = pkg+"."+s;
			 
                              }
                          }// if statement
			  
                        // This code needs to be commented out before delivery MML		  
                        QueryHandler cqh= (QueryHandler)(Class.forName(s).newInstance());
                        cqh.initializeQH(this, // LDMEssentialPlugIn			    		
                                         getClusterIdentifier(),
                                         getCluster(),
                                         domainService.getFactory(),
                                         pt = (Properties)globalParameters.clone(),
                                         getBlackboardService());
			
                        queries.addElement(cqh);
                      } // try statement
                    catch (Exception bogon) 
                      {
                        System.err.println("Exception creating "+s+": "+bogon);
                        bogon.printStackTrace();
                      }
                  }// else statement
              }// if line begins with a %
            else
              {
                // should be a param=value line
                parseQueryParameter(((pt==null)?globalParameters:pt),
                                    line);
              }// else 

            //if (pt != null)			
            //   pt.list(System.out);
            //else
            //   globalParameters.list(System.out);

          }// for loop
      } 
    catch (Exception e) 
      {
	     
        e.printStackTrace();
        throw new RuntimeException("No QueryFile: "+e);
      }
         
      
  }//parseQueryFile
  
  /********************************************************************************
   *
   * Method Name:	parseQueryParmeters
   *
   * Description:	This method stores the given parameter line in the
   *				Properties table
   *				
   * Input:			table - the Properties table to store information
   *				s     - the query parameter to store
   *
   * Output:	  	None
   *
   ********************************************************************************/     
  private void parseQueryParameter(Properties table, String s) 
  {
    //System.out.println("ParseQueryParameter String is " + s);  
    int i = s.indexOf('=');

    String p = s.substring(0,i).trim();
    String v = s.substring(i+1).trim();
    table.put(p,v);
	  
  }// parseQueryParameter
  
  public void registerAsset( Asset anAsset ) 
  {
    
    for (Enumeration e = propertyProviders.elements(); e.hasMoreElements();)
      {
        PropertyProvider pp = (PropertyProvider) e.nextElement();
        pp.provideProperties( anAsset );
      }
  }//registerAsset
  
  private String produceQuery(QueryHandler qh, String s) {
    if (s.indexOf(':') < 0) {   // no params to handle
      return s;
    }
  
    char[] scs = s.toCharArray();
    int l = s.length();
    StringBuffer sb = new StringBuffer();
  
    int i = 0;
    while (i < l) {
      char c = scs[i];
  
      if (c == '\\') {          // quoted?
  	i++;
  	sb.append(scs[i]);
  	i++;
      } else if (c == ':') {    // param subst?
  	i++;
  				// find the end of the var
  	int j;
  	for (j = i; j<l && Character.isLetterOrDigit(scs[j]); j++)
          ;
  				// j is now the index of the first non symbol char past the colon
  	String param = s.substring(i,j);
  	String value = qh.getParameter(param);
  	if (value == null) {
  	  throw new RuntimeException("Parameter Substitution problem in '"+s+
  				     "' at "+(i-1));
  	}
  	sb.append(value);
  	i = j;
      } else {
  	sb.append(c);
  	i++;
      }
    }
  
    // sb contains the substituted string
    return sb.toString();
  }
  
  public void fillProperties( Asset anAsset ) 
  {
    //System.err.println("Providing properties to "+asset);
    for (Enumeration e = propertyProviders.elements(); e.hasMoreElements();)
      {
        //System.out.println("Providing properties for " + anAsset);	
        PropertyProvider pp = (PropertyProvider) e.nextElement();
        pp.provideProperties( anAsset );
      } 
  }
	

}//LDMQueryPlugIn



