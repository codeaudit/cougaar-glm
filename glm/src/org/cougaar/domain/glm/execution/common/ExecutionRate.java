/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.common;

import java.io.IOException;

public class ExecutionRate extends EGObjectBase implements EGObject {
  public double theExecutionRate = 1.0;

  public ExecutionRate(double anExecutionRate) {
    theExecutionRate = anExecutionRate;
  }

  public ExecutionRate() {
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeDouble(theExecutionRate);
  }

  public void read(LineReader reader) throws IOException {
    theExecutionRate = reader.readDouble();
  }
}
