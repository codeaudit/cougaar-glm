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

import org.cougaar.domain.glm.ldm.plan.NewRateScheduleElement;
import org.cougaar.domain.glm.ldm.plan.RateScheduleElement;
import java.util.Date;
import org.cougaar.domain.planning.ldm.plan.*;

/**
 * A RateScheduleElement is an encapsulation of temporal relationships
 * and a rate over that time interval.
 **/

public class RateScheduleElementImpl 
  extends ScheduleElementImpl
  implements RateScheduleElement, NewRateScheduleElement 
{
	
  private double rate = -999.0;
	
  /** no-arg constructor */
  public RateScheduleElementImpl () {
    super();
  }
	
  /** constructor for factory use that takes the start and end dates and a rate(double)*/
  public RateScheduleElementImpl(Date start, Date end, double r) {
    super(start, end);
    rate = r;
  }
  
  /** constructor for factory use that takes the start and end longs and a rate(double)*/
  public RateScheduleElementImpl(long start, long end, double r) {
    super(start, end);
    rate = r;
  }

	
  /** @return double rate of schedule element */
  public double getRate() {
    return rate;
  }
  
  /** simple clone*/
  public Object clone() {
    return new RateScheduleElementImpl(stime, etime, rate);
  }
		
  // NewRateScheduleElement interface implementations
	
  /** @param aRate Set rate of the schedule element */
  public void setRate(double aRate) {
    rate = aRate;
  }
	
	
  public String toString() {
    String superstring = super.toString();
    return "<"+superstring+"-"+rate+">"+"\n";
  }
	
  public double getValue() { return rate; }
  public ScheduleElementWithValue newElement(long start, long end, double value) {
    return new RateScheduleElementImpl(start, end, value);
  }

} 
