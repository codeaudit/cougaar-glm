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
package org.cougaar.glm.execution.cluster;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationforCollections;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Constraint;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.util.UnaryPredicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.execution.common.*;

public class PSP_TaskConstraints
  extends PSP_Base
  implements PlanServiceProvider
{
  public PSP_TaskConstraints() throws RuntimePSPException {
    super();
  }

  public PSP_TaskConstraints(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  public PlanServiceProvider pspClone() throws RuntimePSPException {
    return new PSP_TaskConstraints();
  }

  protected Context createContext() {
    return new MyContext();
  }

  protected static class MyContext extends Context {
  protected void execute() throws IOException {
    TaskConstraintsRequest request = (TaskConstraintsRequest) reader.readEGObject();
    final Set wantedTasks = new HashSet();
    for (int i = 0, n = request.theTaskUIDs.length; i < n; i++) {
      wantedTasks.add(request.theTaskUIDs[i]);
    }
    Collection tasks = sps.queryForSubscriber(new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          return (wantedTasks.contains(((Task) o).getUID()));
        }
        return false;
      }
    });
    Chaser chaser = new Chaser();
    for (Iterator i = tasks.iterator(); i.hasNext(); ) {
      chaser.chaseTask((Task) i.next());
    }
    writer.writeEGObject(new EGObjectArray(chaser.constraints));
  }

  /**
   * Build a collection of tasks that are related to other tasks by
   * constraints. The relations are all constrained equality of end or
   * start times. The exact relationship is expressed with an int
   * having four values. the names of the values express the aspect of
   * the given task that equals the aspect of the related task:
   * These values are bit coded to facilitate their construction.
   *
   * Related tasks are of three kinds: thisTask may be the parent task
   * of the subtasks of an expansion. Those subtasks may have
   * constraints with their parent (thisTask). thisTask may be a
   * subtask in a workflow and have constraints with other subtasks or
   * the parent task. thisTask may have been allocated to another
   * organization and be synonymous with the resulting remote task.
   **/
  private static class Chaser {
    /** Collect all task relationships **/
    List constraints = new ArrayList();

    /** Avoid repeated processing of tasks **/
    Set processedTasks = new HashSet();

    /**
     * Get the relationships relative to a particular task. Does
     * nothing if the Task has already been processed.
     **/
    public void chaseTask(Task thisTask) {
      if (processedTasks.contains(thisTask)) return;
      processedTasks.add(thisTask);

      Workflow wf = thisTask.getWorkflow();
      if (wf != null) {
        chaseWorkflow(thisTask, wf);
      }
      PlanElement pe = thisTask.getPlanElement();
      if (pe instanceof Expansion) {
        Expansion exp = (Expansion) pe;
        wf = exp.getWorkflow();
        if (wf != null) {
          chaseWorkflow(thisTask, wf);
        }
      } else if (pe instanceof Allocation) {
        Allocation alloc = (Allocation) pe;
        if (alloc.getAsset() instanceof Organization) {
          Task thatTask = ((AllocationforCollections) alloc).getAllocationTask();
          if (thatTask != null) {
            constraints.add(new ConstraintElement(thisTask.getUID(), AspectType.START_TIME,
                                                  thatTask.getUID(), AspectType.START_TIME));
            constraints.add(new ConstraintElement(thisTask.getUID(), AspectType.END_TIME,
                                                  thatTask.getUID(), AspectType.END_TIME));
            // There is no point in chasing the remote task in this cluster
          }
        }
      }
    }

    /**
     * Process a task and its related workflow. The workflow is either
     * the workflow of which thisTask is a subtask or one for which
     * thisTask is the parent task.
     **/
    private void chaseWorkflow(Task thisTask, Workflow wf) {
      for (Enumeration constraintTasks = wf.getTaskConstraints(thisTask);
           constraintTasks.hasMoreElements(); ) {
        Constraint constraint = (Constraint) constraintTasks.nextElement();
        if (true || constraint.getConstraintOrder() == 0) {
          int thisTaskAspect;
          int thatTaskAspect;
          Task thatTask;
          if (constraint.getConstrainingTask() == thisTask) {
            thatTask = constraint.getConstrainedTask();
            thisTaskAspect = constraint.getConstrainingAspect();
            thatTaskAspect = constraint.getConstrainedAspect();
          } else {
            thatTask = constraint.getConstrainingTask();
            if (thatTask == null) continue; // Not inter-task constraint
            thisTaskAspect = constraint.getConstrainedAspect();
            thatTaskAspect = constraint.getConstrainingAspect();
          }
          constraints.add(new ConstraintElement(thisTask.getUID(), thisTaskAspect,
                                                thatTask.getUID(), thatTaskAspect));
          chaseTask(thatTask);
        }
      }
    }
  }
}
}
