/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.util.*;

import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.Task;

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

