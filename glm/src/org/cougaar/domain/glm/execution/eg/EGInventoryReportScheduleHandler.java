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

  
