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

package org.cougaar.mlm.ui.data;

import org.cougaar.planning.ldm.plan.Constraint;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.glm.ldm.plan.AlpineAspectType;

/*
  The constraint object that is created from the XML returned by
  a PlanServiceProvider.
*/

public class UIConstraintImpl implements UIConstraint {
  Constraint constraint;

  public UIConstraintImpl(Constraint constraint) {
    this.constraint = constraint;
  }
        
  /** Returns the ID of the task that is constraining another task.
   * @return UUID - the UUID of the constraining task
   **/
                
  public UUID getConstrainingTask() {
    return new UUID(constraint.getConstrainingTask().getUID().toString());
  }
        
  /** 
   * Returns a name for the constraining "aspect"
   * @return String - One of the aspect strings from AspectType
   **/
                
  public String getConstrainingAspect() {
    return getAspectDescription(constraint.getConstrainingAspect());
  }

  private String getAspectDescription(int constraintAspect) {
    if (constraintAspect < 0 || constraintAspect > AlpineAspectType.LAST_ALPINE_ASPECT) {
      return "Aspect " + constraintAspect;
    }
    return AlpineAspectType.aspectTypeToString(constraintAspect);
  }
        
  /** Returns the ID of the task that is being constrained.
   * @return String - the ID of the constrained Task 
   **/
                
  public UUID getConstrainedTask() {
    return new UUID(constraint.getConstrainedTask().getUID().toString());
  }
        
  /** 
   * Returns a name for the constrained "aspect"
   * @return String - One of the aspect strings from AspectType
   **/
                
  public String getConstrainedAspect() {
    return getAspectDescription(constraint.getConstrainedAspect());
  }
        
  /** 
   * Returns a string which represents the
   * order of the Constraint.  
   * @return String - The value, one of COINCIDENT, BEFORE or AFTER
   **/
                
  public String getConstraintOrder() {
    int order = constraint.getConstraintOrder();
    if (order == 0)
      return "COINCIDENT";
    else if (order == -1)
      return "BEFORE";
    else if (order == 1)
      return "AFTER";
    else
      return null; // undefined
  }
        
  /** 
   * Returns a long which represents the time offset
   * of the Constraint. 
   * @return long -  If the long is +x it represents a future offset, if it is -x, it represents a past offset.
   **/
                
  public double getOffsetOfConstraint() {
    return constraint.getOffsetOfConstraint();
  }

}
                
