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

import org.cougaar.domain.glm.ldm.plan.NewQuantityRangeScheduleElement;
import org.cougaar.domain.glm.ldm.plan.QuantityRangeScheduleElement;
import java.util.Date;
import org.cougaar.domain.planning.ldm.plan.ScheduleElementImpl;

/**
 * A QuantityRangeScheduleElement is an encapsulation of temporal relationships
 * and a quantity over that interval.
 **/

public class QuantityRangeScheduleElementImpl 
  extends ScheduleElementImpl
  implements QuantityRangeScheduleElement, NewQuantityRangeScheduleElement 
{
	
  private double sqty = -999.0;
  private double eqty = -999.0;
	
  /** no-arg constructor */
  public QuantityRangeScheduleElementImpl () {
    super();
  }
	
  /** constructor for factory use that takes the start and end dates and a
   * start and end quantity(double)*/
  public QuantityRangeScheduleElementImpl(Date start, Date end, double sq, double eq) {
    super(start, end);
    sqty = sq;
    eqty = eq;
  }
	
  /** @return double start quantity related to this schedule */
  public double getStartQuantity() {
    return sqty;
  }
	
  /** @return double end quantity related to this schedule */
  public double getEndQuantity() {
    return eqty;
  }		
	
  // NewQuantityRangeScheduleElement interface implementations
	
  /** @param aStartQuantity set the start quantity related to this schedule */
  public void setStartQuantity(double aStartQuantity){
    sqty = aStartQuantity;
  }
			
  /** @param anEndQuantity set the end quantity related to this schedule */
  public void setEndQuantity(double anEndQuantity) {
    eqty = anEndQuantity;
  }	


} 
