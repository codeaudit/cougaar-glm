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

import org.cougaar.domain.planning.ldm.measure.Capacity;
import org.cougaar.domain.planning.ldm.plan.*;

/**
 * NewCapacityScheduleElement provides setters to build a complete object.
 **/

public interface NewCapacityScheduleElement 
  extends CapacityScheduleElement, NewScheduleElement 
{
  /** @param aCapacity set the capacity related to this schedule */
  void setCapacity(Capacity aCapacity);
}
