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

import org.cougaar.domain.planning.ldm.plan.ScheduleElementType;

/** 
 * Planning ScheduleElement types
 */
public interface PlanScheduleElementType extends ScheduleElementType {

  static final Class CAPACITY = CapacityScheduleElement.class;
  static final Class OBJECT = ObjectScheduleElement.class;
  static final Class QUANTITY = QuantityScheduleElement.class;
  static final Class QUANTITYRANGE = QuantityRangeScheduleElement.class;
  static final Class RATE = RateScheduleElement.class;
}

