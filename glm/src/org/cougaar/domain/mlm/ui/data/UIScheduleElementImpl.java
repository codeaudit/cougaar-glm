/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ItineraryElement;
import org.cougaar.domain.planning.ldm.plan.LocationScheduleElement;
import org.cougaar.domain.planning.ldm.plan.LocationRangeScheduleElement;
import org.cougaar.domain.glm.plan.QuantityScheduleElement;
import org.cougaar.domain.glm.plan.QuantityRangeScheduleElement;
import org.cougaar.domain.glm.plan.RateScheduleElement;

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

  /* From ItineraryElement return the role which is a verb from org.cougaar.domain.planning.ldm.plan.Constants.Verb.
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
