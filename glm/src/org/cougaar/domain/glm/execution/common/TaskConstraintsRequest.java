package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import org.cougaar.core.society.UID;

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
