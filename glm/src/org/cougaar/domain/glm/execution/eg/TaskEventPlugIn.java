package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

public interface TaskEventPlugIn extends PlugIn {
  /**
   * Apply this plugin to an TaskEventReport,
   * @return true if this plugin was applicable to the report.
   **/
  boolean apply(TimedTaskEventReport tter, long theExecutionTime);
}
