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

public class EGTaskEventReportHandler {
  private TaskEventReportManager theScheduleManager;

  public EGTaskEventReportHandler(TaskEventReportManager aScheduleManager) {
    theScheduleManager = aScheduleManager;
  }

  public void execute(String source, TaskEventReport ter) {
    execute(source, new TaskEventReport[] {ter});
  }

  public void execute(String source, TaskEventReport[] ters) {
    theScheduleManager.receiveTaskEventReports(source, ters);
  }

  public void execute(String source, TaskEventReport.Rescind terr) {
    execute(source, new TaskEventReport.Rescind[] {terr});
  }

  public void execute(String source, TaskEventReport.Rescind[] tersr) {
    theScheduleManager.receiveTaskEventReports(source, tersr);
  }
}
