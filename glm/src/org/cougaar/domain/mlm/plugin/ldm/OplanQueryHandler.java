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

import java.util.Date;
import java.util.Properties;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.core.cluster.Subscriber;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.util.Parameters;

import org.cougaar.domain.glm.ldm.oplan.Oplan;


/** Reads oplan info from a database table. Assumes it's being invoked on
 * behalf of SQLOplanPlugIn. Updates oplan maintained by SQLOplanPlugIn.
 */

public class OplanQueryHandler  extends SQLOplanQueryHandler {
  private static final String QUERY_NAME = "OplanInfoQuery";

  private String myOperationName;
  private String myPriority;
  private Date myCday;

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
    if (rowData.length != 3) {
      System.err.println("OplanQueryHandler.processRow()- expected 3 columns of data, " +
                         " got " + rowData.length);
    }

    myOperationName = (String) rowData[0];
    myPriority = (String) rowData[1];
    myCday = (Date) rowData[2];
  }
  


  /** this method is called when a query is complete, 
   * afer the last call to processRow.  The default method is empty
   * but may be overridden by subclasses.
   **/
  public void endQuery() {
    String oplanID = getParameter(SQLOplanPlugIn.OPLAN_ID_PARAMETER);
    Oplan oplan = myPlugIn.getOplan(oplanID);
    boolean updateNeeded = false;

    if (oplan != null) {
      // No time stamp on the oplan so we fudge with a field by field 
      // comparison
      if ((!oplan.getOperationName().equals(myOperationName)) ||
          (!oplan.getPriority().equals(myPriority)) ||
          (!oplan.getCday().equals(myCday))) {
        updateNeeded = true;
      } 
    } else {
      oplan = new Oplan();
      oplan.setOplanId(oplanID);
      updateNeeded = true;
    }

    if (updateNeeded) {
      oplan.setOperationName(myOperationName);
      oplan.setPriority(myPriority);
      oplan.setCday(myCday);
      
      myPlugIn.updateOplanInfo(oplan);
    }
  }
}




