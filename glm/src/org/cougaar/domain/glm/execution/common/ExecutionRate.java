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
