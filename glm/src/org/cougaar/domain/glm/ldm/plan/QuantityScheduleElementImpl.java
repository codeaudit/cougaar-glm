/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement;
import java.util.Date;
import org.cougaar.domain.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ScheduleElementWithValue;


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
