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

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.glm.ldm.plan.NewCapacityScheduleElement;
import org.cougaar.domain.glm.ldm.plan.CapacityScheduleElement;

import org.cougaar.domain.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.domain.planning.ldm.measure.Capacity;
import java.util.Date;

/**
 * A CapacityScheduleElement is an encapsulation of temporal relationships
 * and a capacity over that time interval.
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: CapacityScheduleElementImpl.java,v 1.3 2001-08-22 20:27:24 mthome Exp $
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
