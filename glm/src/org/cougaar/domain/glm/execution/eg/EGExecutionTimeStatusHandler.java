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

/**
 * Handler for ExecutionTimeStatus messages. The execution time
 * in the message is used to set the event generator execution time.
 **/
public class EGExecutionTimeStatusHandler {
  private EventGenerator theEventGenerator;

  public EGExecutionTimeStatusHandler(EventGenerator anEventGenerator) {
    theEventGenerator = anEventGenerator;
  }

  public void execute(String source, ExecutionTimeStatus ets) {
    theEventGenerator.setExecutionTime(source, ets);
  }
}

  
