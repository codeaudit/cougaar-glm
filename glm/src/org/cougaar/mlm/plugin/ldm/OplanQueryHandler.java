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

import java.util.Date;
import java.util.Properties;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.util.Parameters;

import org.cougaar.glm.ldm.oplan.Oplan;


/** Reads oplan info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugin. Updates oplan maintained by SQLOplanPlugin.
 */

public class OplanQueryHandler  extends SQLOplanQueryHandler {
  private static final String QUERY_NAME = "OplanInfoQuery";

  private String myOperationName;
  private String myPriority;
  //private Date myCday;

  /** this method is called before a query is started,
   * before even getQuery.  The default method is empty
   * but may be overridden.
   **/
  public void startQuery() {
  }
                        
  /** Construct and return an SQL query to be used by the Database engine.
   * Subclasses are required to implement this method.
   **/
  public String getQuery() { 
    return (String) getParameter(QUERY_NAME);
  }
    
  /** Process a single row in a result set,
   * doing whatever is required.
   **/
  public void processRow(Object[] rowData) {
    if (rowData.length != 2) {
      System.err.println("OplanQueryHandler.processRow()- expected 2 columns of data, " +
                         " got " + rowData.length);
    }
    try {
      if (rowData[0] instanceof String)
	myOperationName = (String) rowData[0];
      else
	myOperationName = new String ((byte[])rowData[0],"US-ASCII");

      if (rowData[1] instanceof String)
	myPriority = (String) rowData[1];
      else
	myPriority = new String ((byte[])rowData[1],"US-ASCII");

    } catch (Exception usee) {
      System.err.println("Caught exception while executing a query: "+usee);
      usee.printStackTrace();
    }
 

  }
  


  /** this method is called when a query is complete, 
   * afer the last call to processRow.  The default method is empty
   * but may be overridden by subclasses.
   **/
  public void endQuery() {
    String oplanID = getParameter(OplanReaderPlugin.OPLAN_ID_PARAMETER);
    //should already have this oplan
    Oplan oplan = myPlugin.getOplan(oplanID);
      oplan.setOperationName(myOperationName);
      oplan.setPriority(myPriority);
      
      myPlugin.updateOplanInfo(oplan);
  }
}




