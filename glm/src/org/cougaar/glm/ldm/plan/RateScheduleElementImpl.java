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
