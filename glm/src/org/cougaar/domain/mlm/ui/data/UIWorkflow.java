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

package org.cougaar.domain.mlm.ui.data;

/*
  The workflow object that is created from the XML returned by
  a PlanServiceProvider.
*/

public interface UIWorkflow extends UIUniqueObject {

  /** Get the unique ID of the parent task.
      @return UUID - the UUID of the parent task.
  */

  UUID getParentTask();

  /** Return the array of tasks that comprise this workflow.
      @return UUID[] - an array of the UUIDs of the tasks in this workflow.
  */

  UUID[] getTasks();

  /** Return the constraints.
   */

  UIConstraint[] getConstraints();

  /** Returns true if a constraint has been violated.
     @return boolean
  */

  boolean isConstraintViolated();

  /** Returns the constraints that were violated.
   */

  UIConstraint[] getViolatedConstraints();

  /** Returns true if an add/rescind of the workflow whould automatically
     propagate to the subtasks of the workflow.
     @return boolean
  */

  boolean isPropagatingToSubtasks();
}
