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

import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleElementWithValue;

/**
 * A QuantityScheduleElement is a subclass of ScheduleElement which provides
 * a slot for a quantity (double)
 *
 **/

public interface QuantityScheduleElement 
  extends ScheduleElement, ScheduleElementWithValue
{
  /** @return double quantity related to this schedule */
  double getQuantity();
  
  /** @return Object  a clone of the schedule element for deep copy purposes */
  Object clone();
	
  /** A Thunk which sums QuantityScheduleElements.  
   * The apply assumes that it will only be called on QuantityScheduleElements
   **/
  class SumQuantities implements org.cougaar.util.Thunk {
    private double a = 0.0;
    public void apply(Object o) {
      a+=((QuantityScheduleElement)o).getQuantity();
    }
    public double getSum() { return a; }
    public void reset() { a = 0.0; }
  }

} 
