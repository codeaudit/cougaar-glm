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
import java.util.Date;

public class ExecutionTimeStatus extends ExecutionRate implements EGObject {
  public long theExecutionTime = -1L;

  public ExecutionTimeStatus(long anExecutionTime, double anExecutionRate) {
    super(anExecutionRate);
    theExecutionTime = anExecutionTime;
  }

  public ExecutionTimeStatus() {
  }

  public String toString() {
    return new Date(theExecutionTime).toString() + "*" + theExecutionRate;
  }

  public void write(LineWriter writer) throws IOException {
    super.write(writer);
    writer.writeLong(theExecutionTime);
  }

  public void read(LineReader reader) throws IOException {
    super.read(reader);
    theExecutionTime = reader.readLong();
  }
}
