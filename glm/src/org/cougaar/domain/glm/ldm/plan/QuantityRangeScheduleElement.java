/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;
import org.cougaar.domain.planning.ldm.plan.*;

/**
 * A QuantityRangeScheduleElement is a subclass of ScheduleElement which provides
 * a two slots for a start and end quantity (double)
 **/

public interface QuantityRangeScheduleElement 
  extends ScheduleElement 
{
  /** @return double start quantity related to this schedule */
  double getStartQuantity();
	
  /** @return double end quantity related to this schedule */
  double getEndQuantity();
}
