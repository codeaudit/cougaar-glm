/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
