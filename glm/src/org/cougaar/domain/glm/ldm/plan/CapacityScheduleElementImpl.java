/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
 * @version $Id: CapacityScheduleElementImpl.java,v 1.1 2000-12-20 18:18:16 mthome Exp $
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
