/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
