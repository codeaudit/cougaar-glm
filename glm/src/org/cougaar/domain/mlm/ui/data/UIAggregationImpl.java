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

import org.cougaar.domain.planning.ldm.plan.Aggregation;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.Task;

import java.util.*;

public class UIAggregationImpl extends UIPlanElementImpl implements UIAggregation {

  public UIAggregationImpl(Aggregation aggregation) {
    super((PlanElement)aggregation);
  }
      
  /** Get the IDs of the tasks that are being aggregated.
   */
  public UUID[] getParentTasks() {
    List parentTasks = ((Aggregation)planElement).getComposition().getParentTasks();
    Vector tmp = new Vector();
    ListIterator lit = parentTasks.listIterator();
    while (lit.hasNext()) {
      Task parentTask = (Task)lit.next();
      tmp.add(new UUID(parentTask.getUID().toString()));
    }
    UUID[] parentTaskUUIDs = new UUID[tmp.size()];
    return (UUID[])tmp.toArray(parentTaskUUIDs);
  }

  public UUID getParentTask(int i) {
    UUID[] ids = getParentTasks();
    if (i < ids.length)
      return ids[i];
    else
      return null;
  }

  /** Get the newly created task that represents all the parent tasks.
   */
  public UUID getCombinedTask() {
    Aggregation aggregation = (Aggregation)planElement;
    return new UUID(aggregation.getComposition().getCombinedTask().getUID().toString());
  }

}
