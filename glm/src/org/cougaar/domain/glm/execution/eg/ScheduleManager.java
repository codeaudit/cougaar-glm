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

import java.util.Properties;
import java.io.IOException;

public interface ScheduleManager {
    void advanceTime(long newExecutionTime);
    ReportManagerGUI getGUI();
    String getGUITitle();
    void save(Properties props, String prefix);
    void restore(Properties props, String prefix);
    void enableLog(String prefix) throws IOException;
}
