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
