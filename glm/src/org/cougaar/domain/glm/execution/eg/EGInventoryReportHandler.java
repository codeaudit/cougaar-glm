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

  
