/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
/*
 * Copyright 1999 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
 */

package org.cougaar.domain.mlm.ui.data;

public interface UIPlanElement extends UIUniqueObject {

  /** Get the name of the plan containing this plan element.
      @return String - the name of the Plan of this plan element.
   **/
  String  getPlanName();

  /** This returns the Task of the PlanElement. 
   * @return UUID - the UUID of the task associated with this plan elemenet
   **/
  
  UUID getTask();
  
  /** Returns the estimated allocation result that is related to performing
    * the Task.
    * @return UIAllocationResult - estimated allocation result
    **/

  UIAllocationResult getEstimatedResult();

  /** Returns the reported allocation result.
    * @return UIAllocationResult - reported allocation result
    **/
  UIAllocationResult getReportedResult();
   
}

