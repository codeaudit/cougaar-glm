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

import org.cougaar.planning.ldm.measure.Capacity;
import org.cougaar.planning.ldm.plan.ScheduleElementImpl;

/**
 * A CapacityScheduleElement is an encapsulation of temporal relationships
 * and a capacity over that time interval.
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/

public class CapacityScheduleElementImpl extends ScheduleElementImpl
  implements CapacityScheduleElement, NewCapacityScheduleElement 
{
  private Capacity capacity = null;
	
  /** no-arg constructor */
  public CapacityScheduleElementImpl () { }
	
  /** constructor for factory use that takes the start and end dates and a capacity(double)*/
  public CapacityScheduleElementImpl(Date start, Date end, Capacity c) {
    super(start, end);
    capacity = c;
  }
	
  /** @return double capacity of schedule element */
  public Capacity getCapacity() {
    return capacity;
  }
		
  // NewCapacityScheduleElement interface implementations
	
  /** @param aCapacity Set capacity of the schedule element */
  public void setCapacity(Capacity aCapacity) {
    capacity = aCapacity;
  }
	
  public String toString() {
    String superstring = super.toString();
    return "<"+superstring+"-"+capacity+">"+"\n";
  }
	

} 
