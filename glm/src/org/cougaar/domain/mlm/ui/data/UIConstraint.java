/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
                
