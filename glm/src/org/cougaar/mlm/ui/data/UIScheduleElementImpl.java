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

package org.cougaar.mlm.ui.data;

import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ItineraryElement;
import org.cougaar.planning.ldm.plan.LocationScheduleElement;
import org.cougaar.planning.ldm.plan.LocationRangeScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityRangeScheduleElement;
import org.cougaar.glm.ldm.plan.RateScheduleElement;

public class UIScheduleElementImpl implements UIScheduleElement {
  ScheduleElement scheduleElement;

  public UIScheduleElementImpl(ScheduleElement scheduleElement) {
    this.scheduleElement = scheduleElement;
  }
    
  /* From ScheduleElement; return the start time.
   */
  public long getStartTime() {
    return scheduleElement.getStartTime();
  }

  /* From ScheduleElement; return the end time.
   */
  public long getEndTime() {
    return scheduleElement.getEndTime();
  }

  /* From ItineraryElement return the role which is a verb from org.cougaar.planning.ldm.plan.Constants.Verb.
     */
  public String getRole() {
    if (ItineraryElement.class.isInstance(scheduleElement))
      return ((ItineraryElement)scheduleElement).getRole().toString();
    else
      return null;
  }

  /* From LocationRangeScheduleElement, return the start location.
   */
  public UILocation getStartLocation() {
    if (LocationRangeScheduleElement.class.isInstance(scheduleElement))
      return new UILocationImpl(((LocationRangeScheduleElement)scheduleElement).getStartLocation());
    else
      return null;
  }

  /* From LocationRangeScheduleElement, return the end location.
   */
  public UILocation getEndLocation() {
    if (LocationRangeScheduleElement.class.isInstance(scheduleElement))
      return new UILocationImpl(((LocationRangeScheduleElement)scheduleElement).getEndLocation());
    else
      return null;
  }

  /* From LocationScheduleElement, return the location.
   */
  public UILocation getLocation() {
    if (LocationScheduleElement.class.isInstance(scheduleElement))
      return new UILocationImpl(((LocationScheduleElement)scheduleElement).getLocation());
    else
      return null;
  }

  /* From QuantityScheduleElement, return the quantity.
     Returns -1 if not defined.
   */
  public double getQuantity() {
    if (QuantityScheduleElement.class.isInstance(scheduleElement))
      return ((QuantityScheduleElement)scheduleElement).getQuantity();
    else
      return -1;
  }

  /* From QuantityRangeScheduleElement, return the start quantity.
     Returns -1 if not defined.
   */
    
  public double getStartQuantity() {
    if (QuantityRangeScheduleElement.class.isInstance(scheduleElement))
      return ((QuantityRangeScheduleElement)scheduleElement).getStartQuantity();
    else
      return -1;
  }

  /* From QuantityRangeScheduleElement, return the end quantity.
     Returns -1 if not defined.
   */
  public double getEndQuantity() {
    if (QuantityRangeScheduleElement.class.isInstance(scheduleElement))
      return ((QuantityRangeScheduleElement)scheduleElement).getEndQuantity();
    else
      return -1;
  }

  /* From RateScheduleElement, return the rate.
     Returns -1 if not defined.
   */
  public double getRate() {
    if (RateScheduleElement.class.isInstance(scheduleElement))
      return ((RateScheduleElement)scheduleElement).getRate();
    else
      return -1;
  }

}
