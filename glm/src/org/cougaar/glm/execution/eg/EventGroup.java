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
package org.cougaar.glm.execution.eg;

import org.cougaar.glm.execution.common.*;
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
