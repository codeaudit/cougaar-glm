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
