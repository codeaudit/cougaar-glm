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

package org.cougaar.mlm.debug.ui;

import java.util.Enumeration;

import org.cougaar.planning.ldm.plan.Constraint;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;

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


