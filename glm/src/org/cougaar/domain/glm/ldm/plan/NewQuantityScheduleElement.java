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
 * NewQuantityScheduleElement provides setters to build a complete object.
 **/

public interface NewQuantityScheduleElement
  extends QuantityScheduleElement, NewScheduleElement 
{
	
  /** @param aQuantity set the quantity related to this schedule */
  void setQuantity(double aQuantity);
}
