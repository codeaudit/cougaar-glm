/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import org.cougaar.core.util.UID;

public class TaskConstraintsRequest extends EGObjectBase implements EGObject {
  public UID[] theTaskUIDs;

  public TaskConstraintsRequest(UID[] taskUIDs) {
    theTaskUIDs = taskUIDs;
  }

  TaskConstraintsRequest() {}

  public void write(LineWriter writer) throws IOException {
    writer.writeInt(theTaskUIDs.length);
    for (int i = 0; i < theTaskUIDs.length; i++) {
      writer.writeUID(theTaskUIDs[i]);
    }
  }

  public void read(LineReader reader) throws IOException {
    theTaskUIDs = new UID[reader.readInt()];
    for (int i = 0; i < theTaskUIDs.length; i++) {
      theTaskUIDs[i] = reader.readUID();
    }
  }
}
