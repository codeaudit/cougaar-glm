package org.cougaar.domain.glm.execution.eg;

import java.util.Properties;

public interface ScheduleManager {
  void advanceTime(long newExecutionTime);
  ReportManagerGUI getGUI();
  String getGUITitle();
  void save(Properties props, String prefix);
  void restore(Properties props, String prefix);
}
