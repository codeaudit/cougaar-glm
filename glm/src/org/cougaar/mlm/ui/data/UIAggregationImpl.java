/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.Task;

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
