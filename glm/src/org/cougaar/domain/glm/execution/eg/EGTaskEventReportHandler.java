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
