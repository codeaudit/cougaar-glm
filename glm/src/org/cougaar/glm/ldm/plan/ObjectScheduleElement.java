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
	
  /** @param obj Set object of the schedule element */
  public void setObject(Object obj) {
    object = obj;
  }
  
  public String toString() {
    String superstring = super.toString();
    return "<"+superstring+" = "+object+">"+"\n";
  }
	
  public Object getObject() { return object; }
} 
