/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.debug.ui;

import org.cougaar.domain.planning.ldm.plan.Constraint;
import org.cougaar.domain.planning.ldm.plan.ConstraintEvent;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.glm.ldm.plan.AlpineAspectType;

/** A tree node for a Constraint.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the Constraint which has no children.
  */

public class UIConstraintNode extends UITreeNode {
  private static int msecsPerSecond = 1000;
  private static int msecsPerMinute = 1000 * 60;
  private static int msecsPerHour = 1000 * 60 * 60;
  private static int msecsPerDay = 1000 * 60 * 60 * 24;
  Constraint constraint;

  /** Create a tree node for the constraint.
    @param constraint the constraint for which to create a tree node
   */
  public UIConstraintNode(Constraint constraint) {
    super(constraint);
    this.constraint = constraint;
  }

  /** The constraint is a leaf.
    @return true
   */
  public boolean isLeaf() {
    return true;
  }

  /** This shouldn't be called because this is a leaf.
   */

  public void loadChildren() {
    System.out.println("Warning: UIConstraintNode:loadChildren called, but constraint is a leaf.");
  }

  /** Return representation of a Constraint in a tree.
  <constrained event> <constrained task> <offset of constraint> 
  <constraint order> <constraining event> <constraining task>
  @return for example: initiate Task1 5 msecs after complete Task2
  */

  public String toString() {
    String constrainingEventS =
      getConstraintEventDescription(constraint.getConstrainingEventObject());
    String constrainedEventS =
      getConstraintEventDescription(constraint.getConstrainedEventObject());
    String constraintOrderS = getConstraintOrderDescription(constraint);
    String constraintOffsetS = "";
    double constraintOffset = constraint.getOffsetOfConstraint();
    if (constraintOffset > 0.0) {
      constraintOffsetS = " + "
        + getOffsetDescription(constraintOffset, constraint.getConstrainedAspect());
    }
    if (constraintOffset < 0.0) {
      constraintOffsetS = " - "
        + getOffsetDescription(-constraintOffset, constraint.getConstrainedAspect());
    }
    return constrainedEventS + constraintOffsetS + constraintOrderS + constrainingEventS;
  }

  private String getOffsetDescription(double offset, int aspect) {
    switch (aspect) {
    case AspectType.START_TIME:
    case AspectType.END_TIME:
      return getTimeDescription((long) offset);
    default:
      return Double.toString(offset);
    }
  }

  private String getConstraintEventDescription(ConstraintEvent event) {
    Task task = event.getTask();
    int aspectType = event.getAspectType();
    if (task == null) {         // Event is absolute
      if (aspectType == AspectType.START_TIME || aspectType == AspectType.START_TIME) {
        return new java.util.Date((long) event.getValue()).toString();
      } else {
        return Double.toString(event.getValue());
      }
    } else {
      return (getConstraintAspectDescription(aspectType)
              + " of "
              + UITask.getDescription(task));
    }
  }

  /** Takes time in msecs; returns as days, hours, minutes, etc. */

  private String getTimeDescription(long time) {
    long days = time / msecsPerDay;
    time = time % msecsPerDay;
    long hours = time / msecsPerHour;
    time = time % msecsPerHour;
    long minutes = time / msecsPerMinute;
    time = time % msecsPerMinute;
    long seconds = time / msecsPerSecond;
    time = time % msecsPerSecond;
    long msecs = time % msecsPerSecond;
    String s = "";
    if (days != 0)
      s = days + " days ";
    if (hours != 0)
      s = s + hours + " hours ";
    if (minutes != 0)
      s = s + minutes + " minutes ";
    if (seconds != 0)
      s = s + seconds + " seconds ";
    if (msecs != 0)
      s = s + msecs + " msecs";
    return s.trim();
  }

  private String getConstraintOrderDescription(Constraint constraint) {
    int aspectType = constraint.getConstrainedAspect();
    if (aspectType == AspectType.START_TIME || aspectType == AspectType.START_TIME) {
      return getConstraintTimeOrderDescription(constraint.getConstraintOrder());
    } else {
      return getConstraintValueOrderDescription(constraint.getConstraintOrder());
    }
  }

  private String getConstraintTimeOrderDescription(int constraintOrder) {
    if (constraintOrder == constraint.COINCIDENT)
      return "coincident with";
    if (constraintOrder == constraint.BEFORE)
      return "before";
    if (constraintOrder == constraint.AFTER)
      return "after";
    return "unknown";
  }

  private String getConstraintValueOrderDescription(int constraintOrder) {
    if (constraintOrder == constraint.EQUALTO)
      return "equal to";
    if (constraintOrder == constraint.LESSTHAN)
      return "less than";
    if (constraintOrder == constraint.GREATERTHAN)
      return "greater than";
    return "unknown";
  }

  private String getConstraintAspectDescription(int constraintAspect) {
    if (constraintAspect < 0 || constraintAspect > AlpineAspectType.LAST_ALPINE_ASPECT) {
      return "Aspect " + constraintAspect;
    }
    return AlpineAspectType.aspectTypeToString(constraintAspect);
  }

}


