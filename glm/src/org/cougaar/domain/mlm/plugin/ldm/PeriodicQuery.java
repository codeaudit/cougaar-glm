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

/** A QueryHandler which wants to be execute()ed periodically.
 * The requested frequency is specified as the parameter Frequency.
 * if Frequency is not supplied or is <= 0, the execute method
 * will be called exactly once.
 * Frequency is x calls per second!
 **/

public abstract class PeriodicQuery extends QueryHandler {
  public PeriodicQuery() {}

  private int myFrequency = 0;

  protected int getFrequency() { return myFrequency; }

  /** main interface to PeriodicQuery. Called each time 
   * to run the query.
   **/
  public void start() {
    String fs = getParameter("Frequency");
    if (fs != null) {
      myFrequency = 1000 * Integer.parseInt(fs);
    }
    
    execute();

    if (myFrequency > 0) {
      Thread myThread = new Thread(
                                   new Runnable() {
                                       public void run() {
                                         while (true) {
                                           try {
                                             Thread.sleep(getFrequency());
                                           } catch (Exception e) {}

                                           try {
                                             execute();
                                           } catch (Exception e) {
                                             System.err.println("PeriodicQuery loop "+this+" Caught "+e);
                                             return;
                                           }
                                         }
                                       }}, this.toString()+"/PeriodicQuery("+(getFrequency()/1000)+")");
      myThread.start();
    }
  }
  
  

  protected void execute() 
  {
    startQuery();                 // let the query have some state
	

    if (LDMQueryType)
      {	
        int count = 1;
        String dbName = getParameter("DB_NAME" + count);
        String queryString = getQueryString("query" + count);
	  	  
	  	  
        System.out.println("I am about to execute the Query " +queryString);
        System.out.println("For Database " + dbName);	  
	        
        boolean success = myQueryLDMPlugIn.executeQuery(queryString, this, dbName);
	  	  
        if (!success)
          {	 
            System.out.println("THE CONNECTION DID NOT WORK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");		  
            count++;
            queryString = getQueryString("query" + count);
            dbName = getParameter("DB_NAME" + count);	
            while ((queryString != null) && (!success))
              {
                success = myQueryLDMPlugIn.executeQuery(queryString, this, dbName);
                count++;
                dbName = getParameter("DB_NAME" + count);	
                queryString = getQueryString("query" + count);
			
                System.out.println("The query String for query" +count+ " is "+ queryString);	

              }// while loop
          } // if statement
	  
        if (!success)
          { 
            // Send an alert 
          }
      }// LDMType = Query	 
    // It is the LDMSQLPlugIn       
    else
      {
        String q = getQuery();		
        myLDMPlugIn.executeSQL(q, this);
      }// LDMSQLPlugIn		 
	  
    endQuery();                   // tell the query it is done.
  }
  
  public String getQueryString(String query) { 
    return getParameter(query);
  }
}
