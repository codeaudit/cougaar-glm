/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
