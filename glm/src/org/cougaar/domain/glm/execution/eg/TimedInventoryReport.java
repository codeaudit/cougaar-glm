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
package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;
import java.io.IOException;

public class TimedInventoryReport extends Timed {
  public String theSource;
  protected InventoryReportManager theInventoryReportManager;
  public InventoryReport theInventoryReport;
  public InventoryReport theOriginalInventoryReport;

  public TimedInventoryReport(String aSource, InventoryReport anInventoryReport,
                              InventoryReportManager anInventoryReportManager) {
    theSource = aSource.intern();
    theInventoryReportManager = anInventoryReportManager;
    if (anInventoryReport == null) {
      throw new IllegalArgumentException("anInventoryReport is null");
    }
    theInventoryReport = anInventoryReport;
  }

  public Object getKey() {
    return null;
  }

  public String getCluster() {
    return theSource;
  }

  public long getTime() {
    return theInventoryReport.theReceivedDate;
  }

  public String getItem() {
    return theInventoryReport.theItemIdentification;
  }

  public boolean expired(long time) {
    try {
      theInventoryReportManager.sendInventoryReport(theSource, theInventoryReport);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return true;		// Expired
  }

  public InventoryReport getModifiableInventoryReport() {
    if (theOriginalInventoryReport == null) {
      theOriginalInventoryReport = new InventoryReport(theInventoryReport);
    }
    return theInventoryReport;
  }
}
