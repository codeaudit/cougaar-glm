package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.glm.execution.common.*;
import java.util.ArrayList;
import java.util.Iterator;

public class EventGroup {
  public TaskEventId theGroupId;
  private ArrayList tters = new ArrayList();

  public EventGroup(TaskEventId aGroupId) {
    this.theGroupId = aGroupId;
  }

  public void add(TimedTaskEventReport tter) {
    tters.add(tter);
  }

  public void remove(TimedTaskEventReport tter) {
    tters.remove(tter);
  }

  public boolean contains(TimedTaskEventReport tter) {
    return tters.contains(tter);
  }

  public Iterator iterator() {
    return tters.iterator();
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(theGroupId);
    buf.append(":");
    for (Iterator i = iterator(); i.hasNext(); ) {
      buf.append(' ');
      TaskEventId tid = ((TimedTaskEventReport) i.next()).theTaskEventReport.theTaskEventId;
      buf.append(tid);
    }
    return buf.toString();
  }
}
