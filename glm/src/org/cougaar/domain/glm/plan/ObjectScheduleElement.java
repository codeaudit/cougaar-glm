/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

import java.util.Date;
import org.cougaar.domain.planning.ldm.plan.ScheduleElementImpl;

/**
 * A ObjectScheduleElement is an encapsulation of temporal relationships
 * and a object over that interval.
 **/

public class ObjectScheduleElement extends ScheduleElementImpl 
{
	
  private Object object = null;
	
  /** no-arg constructor */
  public ObjectScheduleElement() {
    super();
  }
	
  /** constructor for factory use that takes the start and end dates 
   * and a object(Object)
   **/
  public ObjectScheduleElement(Date start, Date end, Object q) {
      this(start.getTime(), end.getTime(), q);
  }
  
  /** constructor that takes longs instead of dates */
  public ObjectScheduleElement(long start, long end, Object obj) {
    super(start, end);
    object = obj;
  }
	
  /** simple clone*/
  public Object clone() {
    return new ObjectScheduleElement(stime, etime, object);
  }
		
  // NewObjectScheduleElement interface implementations
	
  /** @param anObject Set object of the schedule element */
  public void setObject(Object obj) {
    object = obj;
  }
  
  public String toString() {
    String superstring = super.toString();
    return "<"+superstring+" = "+object+">"+"\n";
  }
	
  public Object getObject() { return object; }
} 
