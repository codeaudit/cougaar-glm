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
package org.cougaar.glm.execution.common;

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
