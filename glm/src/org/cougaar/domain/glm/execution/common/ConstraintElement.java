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
package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import org.cougaar.core.society.UID;

/**
 * State a constraint between two tasks,
 **/
public class ConstraintElement extends EGObjectBase implements EGObject {
  public TaskEventId thatEventId;
  public TaskEventId thisEventId;

  public ConstraintElement(UID thisTaskUID,
                           int thisAspectType,
                           UID thatTaskUID,
                           int thatAspectType)
  {
    thisEventId = new TaskEventId(thisTaskUID, thisAspectType);
    thatEventId = new TaskEventId(thatTaskUID, thatAspectType);
  }

  ConstraintElement() {}

  public void write(LineWriter writer) throws IOException {
    writer.writeUID(thisEventId.theTaskUID);
    writer.writeInt(thisEventId.theAspectType);
    writer.writeUID(thatEventId.theTaskUID);
    writer.writeInt(thatEventId.theAspectType);
  }

  public void read(LineReader reader) throws IOException {
    try {
      thisEventId = new TaskEventId(reader.readUID(), reader.readInt());
      thatEventId = new TaskEventId(reader.readUID(), reader.readInt());
    } catch (IOException ioe) {
      System.err.println("IOException reading ConstraintElement: " + ioe);
      throw ioe;
    }
  }
  public String toString() {
    return thisEventId.toString() + ":" + thatEventId.toString();
  }
}
