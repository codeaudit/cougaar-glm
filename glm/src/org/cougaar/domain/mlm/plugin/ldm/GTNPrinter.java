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

// Simple print sql "plugin" for the GTN query

public class GTNPrinter extends PeriodicQuery {
  public GTNPrinter() {}

  public String getQuery() {
    return (String) getParameter("query");  }

  public void processRow(Object[] data) {
    try {
      String vessel_id = (String)data[0];
      String vessel_name = (String)data[1];
      System.out.println("\n GTN query:  " + vessel_id + "   " + vessel_name);
         
    } catch (Exception e) {
      System.err.println("Caught exception in processRow(): "+e);
      e.printStackTrace();
    }
    
  }
}
