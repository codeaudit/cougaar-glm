/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
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
