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

import org.cougaar.core.util.UID;

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
