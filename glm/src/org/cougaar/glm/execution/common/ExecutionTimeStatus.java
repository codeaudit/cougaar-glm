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
