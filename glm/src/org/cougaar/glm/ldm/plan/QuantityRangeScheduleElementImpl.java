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

package org.cougaar.glm.ldm.plan;

import org.cougaar.glm.ldm.plan.NewQuantityRangeScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityRangeScheduleElement;
import java.util.Date;
import org.cougaar.planning.ldm.plan.ScheduleElementImpl;

/**
 * A QuantityRangeScheduleElement is an encapsulation of temporal relationships
 * and a quantity over that interval.
 **/

public class QuantityRangeScheduleElementImpl 
  extends ScheduleElementImpl
  implements QuantityRangeScheduleElement, NewQuantityRangeScheduleElement 
{
	
  private double sqty = -999.0;
  private double eqty = -999.0;
	
  /** no-arg constructor */
  public QuantityRangeScheduleElementImpl () {
    super();
  }
	
  /** constructor for factory use that takes the start and end dates and a
   * start and end quantity(double)*/
  public QuantityRangeScheduleElementImpl(Date start, Date end, double sq, double eq) {
    super(start, end);
    sqty = sq;
    eqty = eq;
  }
	
  /** @return double start quantity related to this schedule */
  public double getStartQuantity() {
    return sqty;
  }
	
  /** @return double end quantity related to this schedule */
  public double getEndQuantity() {
    return eqty;
  }		
	
  // NewQuantityRangeScheduleElement interface implementations
	
  /** @param aStartQuantity set the start quantity related to this schedule */
  public void setStartQuantity(double aStartQuantity){
    sqty = aStartQuantity;
  }
			
  /** @param anEndQuantity set the end quantity related to this schedule */
  public void setEndQuantity(double anEndQuantity) {
    eqty = anEndQuantity;
  }	


} 
