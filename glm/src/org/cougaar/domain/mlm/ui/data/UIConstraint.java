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
  The constraint object that is created from the XML returned by
  a PlanServiceProvider.
*/

public interface UIConstraint {
        
  /** Returns the ID of the task that is constraining another task.
   * @return UUID - the UUID of the constraining task
   **/
                
  UUID getConstrainingTask();
        
  /** 
   * Returns a name for the constraining "aspect"
   * @return String - One of the aspect strings from AspectType
   **/
                
  String getConstrainingAspect();
        
  /** Returns the ID of the task that is being constrained.
   * @return UUID - the UUID of the constrained Task 
   **/
                
  UUID getConstrainedTask();
        
  /** 
   * Returns a name for the constrained "aspect"
   * @return String - One of the aspect strings from AspectType
   **/
                
  String getConstrainedAspect();
        
  /** 
   * Returns a string which represents the order of the Constraint.  
   * @return String - One of COINCIDENT, BEFORE or AFTER.
   **/
                
  String getConstraintOrder();
        
  /** 
   * Returns a double which represents the offset
   * of the Constraint. 
   * @return The offset that is added to the constraining value before
   * comparing to the constrained value.
   **/
                
  double getOffsetOfConstraint();

}
                
