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

public class EGInventoryReportHandler {
  private InventoryScheduleManager theInventoryScheduleManager;

  public EGInventoryReportHandler(InventoryScheduleManager anInventoryScheduleManager) {
    theInventoryScheduleManager = anInventoryScheduleManager;
  }

  public void execute(String source, InventoryReport report) {
    execute(source, new InventoryReport[] {report});
  }

  public void execute(String source, InventoryReport[] reports) {
    theInventoryScheduleManager.receiveInventoryReports(source, reports);
  }
}

  
