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

import org.cougaar.domain.planning.ldm.plan.*;

/**
 * NewRateScheduleElement provides setters to build a complete object.
 **/

public interface NewRateScheduleElement
  extends RateScheduleElement, NewScheduleElement 
{
  /** @param aRate set the rate related to this schedule */
  void setRate(double aRate);
}
