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
