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

import org.cougaar.domain.planning.ldm.plan.*;

/**
 * A RateScheduleElement is a subclass of ScheduleElement which provides
 * a slot for a rate  (double for now - maybe a rate measure later)
 **/

public interface RateScheduleElement 
  extends ScheduleElement, ScheduleElementWithValue
{
  
  /** @return double rate related to this schedule */
  double getRate();
  
  /** @return Object  a clone of the schedule element for deep copy purposes */
  Object clone();

}
