/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.ldm;

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
	
    String q = getQuery();		
    myLDMPlugin.executeSQL(q, this);
	  
    endQuery();                   // tell the query it is done.
  }
  
  public String getQueryString(String query) { 
    return getParameter(query);
  }
}
