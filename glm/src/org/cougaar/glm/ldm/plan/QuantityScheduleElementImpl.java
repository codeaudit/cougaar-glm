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

package org.cougaar.glm.ldm.plan;

import java.util.Date;

import org.cougaar.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.planning.ldm.plan.ScheduleElementWithValue;


/**
 * A QuantityScheduleElement is an encapsulation of temporal relationships
 * and a quantity over that interval.
 **/

public class QuantityScheduleElementImpl extends ScheduleElementImpl
  implements QuantityScheduleElement, NewQuantityScheduleElement
{
	
  private double quantity = -999.0;
	
  /** no-arg constructor */
  public QuantityScheduleElementImpl () {
    super();
  }
	
  /** constructor for factory use that takes the start and end dates 
   * and a quantity(double)
   **/
  public QuantityScheduleElementImpl(Date start, Date end, double q) {
    super(start, end);
    quantity = q;
  }
  
  /** constructor that takes longs instead of dates */
  public QuantityScheduleElementImpl(long start, long end, double q) {
    super(start, end);
    quantity = q;
  }
	
  /** @return double quantity of schedule element */
  public double getQuantity() {
    return quantity;
  }
  
  /** simple clone*/
  public Object clone() {
    return new QuantityScheduleElementImpl(stime, etime, quantity);
  }
		
  // NewQuantityScheduleElement interface implementations
	
  /** @param aQuantity Set quantity of the schedule element */
  public void setQuantity(double aQuantity) {
    quantity = aQuantity;
  }
  
  public String toString() {
    String superstring = super.toString();
    return "<"+superstring+" = "+quantity+">"+"\n";
  }
	
  public double getValue() { return quantity; }
  public ScheduleElementWithValue newElement(long start, long end, double value) {
    return new QuantityScheduleElementImpl(start, end, value);
  }
} 
