/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
