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
