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
/*
 * Copyright 1999 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
 */

package org.cougaar.mlm.ui.data;

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

