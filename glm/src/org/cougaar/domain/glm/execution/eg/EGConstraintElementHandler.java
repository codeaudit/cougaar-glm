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
