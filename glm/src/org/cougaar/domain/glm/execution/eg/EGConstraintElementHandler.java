package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

public class EGConstraintElementHandler {
  private TaskEventReportManager theScheduleManager;

  public EGConstraintElementHandler(TaskEventReportManager aScheduleManager) {
    theScheduleManager = aScheduleManager;
  }

  public void execute(String source, ConstraintElement el) {
    execute(source, new ConstraintElement[] {el});
  }

  public void execute(String source, ConstraintElement[] constraints) {
    theScheduleManager.receiveConstraintElements(source, constraints);
  }
}
