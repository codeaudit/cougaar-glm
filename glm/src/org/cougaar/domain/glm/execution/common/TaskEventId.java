package org.cougaar.domain.glm.execution.common;

import org.cougaar.core.society.UID;

public class TaskEventId implements Comparable {
  public UID theTaskUID;
  public int theAspectType;
  private int hc;

  public TaskEventId(UID aTaskUID, int anAspectType) {
    theTaskUID = aTaskUID;
    theAspectType = anAspectType;
    hc = theTaskUID.hashCode() + theAspectType;
  }

  public int hashCode() {
    return hc;
  }

  public boolean equals(Object o) {
    if (o instanceof TaskEventId) {
      TaskEventId other = (TaskEventId) o;
      return (theTaskUID.equals(other.theTaskUID) && theAspectType == other.theAspectType);
    }
    return false;
  }

  public int compareTo(Object o) {
    TaskEventId key = (TaskEventId) o;
    int diff = theTaskUID.compareTo(key.theTaskUID);
    if (diff == 0) {
      diff = theAspectType - key.theAspectType;
    }
    return diff;
  }

  public String toString() {
    return theTaskUID.toString() + ":" + theAspectType;
  }
}
