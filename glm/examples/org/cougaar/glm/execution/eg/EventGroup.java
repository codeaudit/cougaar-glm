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
