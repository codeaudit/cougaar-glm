/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.plugin.ldm;

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
    Oplan oplan = (Oplan) myPlugin.getOplan(oplanID).clone();
    oplan.setOperationName(myOperationName);
    oplan.setPriority(myPriority);
      
    myPlugin.updateOplanInfo(oplan);
  }
}




