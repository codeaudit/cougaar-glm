/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

public interface UIScheduleElement extends org.cougaar.util.TimeSpan
{

  /* From ItineraryElement which extends LocationRangeScheduleElement,
     return the role which is a verb from org.cougaar.domain.planning.ldm.plan.Constants.Verb.
     */
  String getRole();

  /* From LocationRangeScheduleElement, return the start location.
   */
  UILocation getStartLocation();

  /* From LocationRangeScheduleElement, return the end location.
   */
  UILocation getEndLocation();

  /* From LocationScheduleElement, return the location.
   */
  UILocation getLocation();

  /* From QuantityScheduleElement, return the quantity.
   */
  double getQuantity();

  /* From QuantityRangeScheduleElement, return the start quantity.
   */
  double getStartQuantity();

  /* From QuantityRangeScheduleElement, return the end quantity.
   */
  double getEndQuantity();

  /* From RateScheduleElement, return the rate.
   */
  double getRate();

}




