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

public class SetExecutionTime extends ExecutionRate implements EGObject {
  public long theTime = -1L;
  public boolean timeIsAbsolute = false;
  public long theTransitionInterval;

  public static SetExecutionTime
    createAbsoluteInstance(long anAbsoluteTime,
                           double anExecutionRate,
                           long aTransitionInterval)
  {
    return new SetExecutionTime(anAbsoluteTime, true, anExecutionRate, aTransitionInterval);
  }

  public static SetExecutionTime
    createRelativeInstance(long aDeltaTime,
                           double anExecutionRate,
                           long aTransitionInterval)
  {
    return new SetExecutionTime(aDeltaTime, false, anExecutionRate, aTransitionInterval);
  }


  public static SetExecutionTime createRateChangeInstance(double anExecutionRate) {
    return new SetExecutionTime(0L, false, anExecutionRate, 0L);
  }

  /**
   * Construct an absolute or relative advancement with a given rate.
   * Private constructor. Use static create methods.
   **/
  private SetExecutionTime(long aTime,
                          boolean isAbsolute,
                          double anExecutionRate,
                          long aTransitionInterval)
  {
    super(anExecutionRate);
    theTime = aTime;
    timeIsAbsolute = isAbsolute;
    theTransitionInterval = aTransitionInterval;
  }

  public SetExecutionTime() {
  }

  public void write(LineWriter writer) throws IOException {
    super.write(writer);
    writer.writeLong(theTime);
    writer.writeBoolean(timeIsAbsolute);
    writer.writeLong(theTransitionInterval);
  }

  public void read(LineReader reader) throws IOException {
    super.read(reader);
    theTime = reader.readLong();
    timeIsAbsolute = reader.readBoolean();
    theTransitionInterval = reader.readLong();
  }

  public String toString() {
    return "*" + theExecutionRate + (timeIsAbsolute ? "@" : "+") + theTime;
  }
}
