package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

public class EGInventoryReportScheduleHandler {
  private InventoryScheduleManager theScheduleManager;

  public EGInventoryReportScheduleHandler(InventoryScheduleManager aScheduleManager) {
    theScheduleManager = aScheduleManager;
  }

  public void execute(String source, InventoryReportSchedule schedule) {
    execute(source, new InventoryReportSchedule[] {schedule});
  }

  public void execute(String source, InventoryReportSchedule[] schedules) {
    theScheduleManager.receiveInventoryReportSchedules(source, schedules);
  }

  public void execute(String source, InventoryReportSchedule.Rescind schedule) {
    execute(source, new InventoryReportSchedule.Rescind[] {schedule});
  }

  public void execute(String source, InventoryReportSchedule.Rescind[] schedules) {
    theScheduleManager.receiveInventoryReportSchedules(source, schedules);
  }
}

  
