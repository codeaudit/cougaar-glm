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

package org.cougaar.mlm.ui.data;

import java.util.*;

import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.Task;

/*
  Define a MPTask object that is created from the XML returned by
  a PlanServiceProvider.
*/

public class UIMPTaskImpl extends UITaskImpl implements UIMPTask {

  public UIMPTaskImpl(MPTask task) {
    super((Task)task);
  }

  /** @return UUID[] - the UUIDs of the parent tasks
   */
  public UUID[] getParentTasks() {
    Enumeration parentTasks = ((MPTask)task).getParentTasks();
    ArrayList tmp = new ArrayList();
    while (parentTasks.hasMoreElements()) {
      Task parentTask = (Task)parentTasks.nextElement();
      tmp.add(new UUID(parentTask.getUID().toString()));
    }
    UUID UUIDs[] = new UUID[tmp.size()];
    return (UUID[])tmp.toArray(UUIDs);
  }

  public UUID getParentTask(int i) {
    UUID UUIDs[] = getParentTasks();
    if (i < UUIDs.length)
      return UUIDs[i];
    else
      return null;
  }

}

