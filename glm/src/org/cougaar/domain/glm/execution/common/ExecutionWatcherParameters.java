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

public class ExecutionWatcherParameters extends EGObjectBase implements EGObject {
  public long theTimeStep;

  public boolean timeStatusOnly;  

  public ExecutionWatcherParameters(long aTimeStep) {
      this(aTimeStep,false);
  }

  public ExecutionWatcherParameters() {
      timeStatusOnly=false;     
  }

  public ExecutionWatcherParameters(long aTimeStep,boolean justTimeStatus) {
      theTimeStep = aTimeStep;
      timeStatusOnly=justTimeStatus;     
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeLong(theTimeStep);
    writer.writeBoolean(timeStatusOnly);
  }

  public void read(LineReader reader) throws IOException {
    theTimeStep = reader.readLong();
    timeStatusOnly = reader.readBoolean();
  }
}
