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
