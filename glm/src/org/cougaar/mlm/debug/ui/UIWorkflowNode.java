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
    @return false
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


