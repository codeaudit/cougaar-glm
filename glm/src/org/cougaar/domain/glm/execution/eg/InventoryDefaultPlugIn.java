package org.cougaar.domain.glm.execution.eg;

import  org.cougaar.domain.glm.execution.common.*;

public class InventoryDefaultPlugIn implements InventoryPlugIn {
  /**
   * @return the name of this plugin
   **/
  public String getPlugInName() {
    return "Default";
  }

  public String getDescription() {
    return "Default plugin leaves inventory reports unmodified";
  }

  public void setParameter(String parameter) {
    // No parameter needed
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
   * Apply this plugin to an InventoryReport,
   * @return true if this plugin was applicable to the report.
   **/
  public boolean apply(TimedInventoryReport tir, long theExecutionTime) {
    return true;
  }
}
