package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import org.cougaar.core.society.UID;

/**
 * Report the Observable Aspects of a Task.
 * In particular: START_TIME, END_TIME, & QUANTITY
 **/
public class TaskEventReport extends Report implements java.io.Serializable {
  public static class Rescind extends TaskEventReport {
    public Rescind() {}
    public Rescind(TaskEventId aTaskEventId) {
      theTaskEventId = aTaskEventId;
    }
    public boolean isRescind() { return true; }
  }
  public boolean isRescind() { return false; }

  public TaskEventId theTaskEventId;
  public String theVerb = "";
  public double theAspectValue;
  public String theFullDescription = "";
  public String theShortDescription = "";

  public TaskEventReport(TaskEventId aTaskEventId,
                         String aVerb,
                         double anAspectValue,
                         long aReportDate,
                         long aReceivedDate,
                         String aFullDescription,
                         String aShortDescription)
  {
    super(aReportDate, aReceivedDate);
    theTaskEventId = aTaskEventId;
    theVerb = aVerb;
    theAspectValue = anAspectValue;
    theFullDescription = aFullDescription;
    theShortDescription = aShortDescription;
  }

  TaskEventReport() {}

  public TaskEventReport(TaskEventReport original) {
    this(original.theTaskEventId,
         original.theVerb,
         original.theAspectValue,
         original.theReportDate,
         original.theReceivedDate,
         original.theFullDescription,
         original.theShortDescription);
  }

  public void write(LineWriter writer) throws IOException {
    super.write(writer);
    writer.writeUID(theTaskEventId.theTaskUID);
    writer.writeInt(theTaskEventId.theAspectType);
    writer.writeUTF(theVerb);
    writer.writeDouble(theAspectValue);
    writer.writeUTF(theFullDescription);
    writer.writeUTF(theShortDescription);
  }

  public void read(LineReader reader) throws IOException {
    super.read(reader);
    theTaskEventId = new TaskEventId(reader.readUID(), reader.readInt());
    theVerb = reader.readUTF();
    theAspectValue = reader.readDouble();
    theFullDescription = reader.readUTF();
    theShortDescription = reader.readUTF();
  }
}
