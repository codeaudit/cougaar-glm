/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.debug.ui;

import java.util.Enumeration;

import org.cougaar.domain.planning.ldm.plan.Constraint;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Workflow;

/** A tree node for a Workflow.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the Workflow and its children,
  which are Tasks and Constraints.
  */

public class UIWorkflowNode extends UITreeNode {
  Workflow workflow;

  /** Create a tree node for the Workflow.
    @param workflow for which to create tree node
   */
  public UIWorkflowNode(Workflow workflow) {
    super(workflow);
    this.workflow = workflow;
  }

  /** The Workflow is always a parent (of tasks and constraints).
    @param return false
   */
  public boolean isLeaf() {
    return false;
  }

  /** Children are tasks and constraints.
   */

  public synchronized void loadChildren() {
    Enumeration tasks = workflow.getTasks();
    int i = 0;
    while (tasks.hasMoreElements())
      insert(new UITaskNode((Task)tasks.nextElement()), i++);
    Enumeration constraints = workflow.getConstraints();
    while (constraints.hasMoreElements())
      insert(new UIConstraintNode((Constraint)constraints.nextElement()), i++);
  }

  /** Return representation of a Workflow in a tree.
    @return workflow.toString
   */

  public String toString() {
    //    return workflow.toString();
    return "Workflow";
  }
}


