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

import java.util.*;

import org.cougaar.planning.ldm.plan.Constraint;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/*
  The workflow object that is created from the XML returned by
  a PlanServiceProvider.
*/

public class UIWorkflowImpl implements UIWorkflow, XMLUIPlanObject {
  Workflow workflow;
  UUID workflowUUID;

  public UIWorkflowImpl(Workflow workflow) {
    this.workflow = workflow;
    workflowUUID = new UUID(workflow.getUID().toString());
  }

  /** Get the unique ID of the parent task.
      @return UUID - the UUID of the parent task.
  */

  public UUID getParentTask() {
    return new UUID(workflow.getParentTask().getUID().toString());
  }

  /** Return the array of tasks that comprise this workflow.
      @return UUID[] - an array of the UUIDs of the tasks in this workflow.
  */

  public UUID[] getTasks() {
    Enumeration tasks = workflow.getTasks();
    ArrayList tmp = new ArrayList();
    while (tasks.hasMoreElements())
      tmp.add(new UUID(((Task)tasks.nextElement()).getUID().toString()));
    UUID[] UITasks = new UUID[tmp.size()];
    return (UUID[])tmp.toArray(UITasks);
  }

  public UUID getTask(int i) {
    UUID[] tasks = getTasks();
    if (i < tasks.length)
      return tasks[i];
    else
      return null;
  }

  /** Return the constraints.
   */

  public UIConstraint[] getConstraints() {
    Enumeration constraints = workflow.getConstraints();
    ArrayList tmp = new ArrayList();
    while (constraints.hasMoreElements())
      tmp.add(new UIConstraintImpl((Constraint)constraints.nextElement()));
    UIConstraint[] UIConstraints = new UIConstraint[tmp.size()];
    return (UIConstraint[])tmp.toArray(UIConstraints);
  }

  public UIConstraint getConstraint(int i) {
    UIConstraint[] constraints = getConstraints();
    if (i < constraints.length)
      return constraints[i];
    else
      return null;
  }

  /** Returns true if a constraint has been violated.
     @return boolean
  */

  public boolean isConstraintViolated() {
    return workflow.constraintViolation();
  }

  /** Returns the constraints that were violated.
   */

  public UIConstraint[] getViolatedConstraints() {
    Enumeration violatedConstraints = workflow.getViolatedConstraints();
    ArrayList tmp = new ArrayList();
    while (violatedConstraints.hasMoreElements())
      tmp.add(new UIConstraintImpl((Constraint)violatedConstraints.nextElement()));
    UIConstraint[] UIConstraints = new UIConstraint[tmp.size()];
    return (UIConstraint[])tmp.toArray(UIConstraints);
  }

  public UIConstraint getViolatedConstraint(int i) {
    UIConstraint[] violatedConstraints = getViolatedConstraints();
    if (i < violatedConstraints.length)
      return violatedConstraints[i];
    else
      return null;
  }

  /** Returns true if an add/rescind of the workflow whould automatically
     propagate to the subtasks of the workflow.
     @return boolean
  */

  public boolean isPropagatingToSubtasks() {
    return workflow.isPropagatingToSubtasks();
  }

  public UUID getUUID() {
    return workflowUUID;
  }

  //  XMLPlanObject method for UI
  
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }

}
