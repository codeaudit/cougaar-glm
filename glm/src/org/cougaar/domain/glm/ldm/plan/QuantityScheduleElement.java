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
 * A QuantityScheduleElement is a subclass of ScheduleElement which provides
 * a slot for a quantity (double)
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: QuantityScheduleElement.java,v 1.2 2001-04-05 19:27:41 mthome Exp $
 **/

public interface QuantityScheduleElement 
  extends ScheduleElement, ScheduleElementWithValue
{
  /** @return double quantity related to this schedule */
  double getQuantity();
  
  /** @return Object  a clone of the schedule element for deep copy purposes */
  Object clone();
	
  /** A Thunk which sums QuantityScheduleElements.  
   * The apply assumes that it will only be called on QuantityScheduleElements
   **/
  public static class SumQuantities implements org.cougaar.util.Thunk {
    private double a = 0.0;
    public void apply(Object o) {
      a+=((QuantityScheduleElement)o).getQuantity();
    }
    public double getSum() { return a; }
    public void reset() { a = 0.0; }
  }

} 
