package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;

public interface InventoryPlugIn extends PlugIn {
  /**
   * Apply this plugin to an InventoryReport,
   * @return true if this plugin was applicable to the report.
   **/
  boolean apply(TimedInventoryReport tir, long theExecutionTime);
}
