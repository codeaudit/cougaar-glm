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

public class TaskEventDefaultPlugIn implements TaskEventPlugIn {
  /**
   * @return the name of this plugin
   **/
  public String getPlugInName() {
    return "Default";
  }

  public String getDescription() {
    return "Default plugin leaves task events unmodified";
  }

  public void setParameter(String parameter) {
  }

  public boolean isConfigurable() {
    return false;
  }

  public void configure(java.awt.Component c) {}

  public void save(java.util.Properties props, String prefix) {
  }

  public void restore(java.util.Properties props, String prefix) {
  }

  public void setEventGenerator(EventGenerator eg) {
  }

  /**
   * Apply this plugin to an TaskEventReport,
   * @return true if this plugin was applicable to the report.
   **/
  public boolean apply(TimedTaskEventReport tter, long theExecutionTime) {
    if (tter.getAspectTypeString().equals("QUANTITY")) {
      TaskEventReport theTaskEventReport = tter.getModifiableTaskEventReport();
      theTaskEventReport.theAspectValue += 3.0;
    } else {
      // Leave it alone
    }
    return true;
  }
}
