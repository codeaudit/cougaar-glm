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

public interface TaskEventPlugIn extends PlugIn {
  /**
   * Apply this plugin to an TaskEventReport,
   * @return true if this plugin was applicable to the report.
   **/
  boolean apply(TimedTaskEventReport tter, long theExecutionTime);
}
