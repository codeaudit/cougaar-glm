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

import org.cougaar.util.TimeSpan;
import org.cougaar.domain.glm.plugins.TimeUtils;
import org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement;
import java.io.Serializable;

public class UIQuantityScheduleElement  implements TimeSpan, 
						   Cloneable,
						   Serializable {
  long startTime;
  long endTime;
  double quantity;

  public UIQuantityScheduleElement(long startTime, long endTime, double quantity) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.quantity = quantity;
  }

  public UIQuantityScheduleElement(QuantityScheduleElement qse) {
    this.startTime = qse.getStartTime();
    this.endTime = qse.getEndTime();
    this.quantity = qse.getQuantity();
  }

  public double getQuantity() {
    return quantity;
  }

  public void setQuantity(double quantity) {
    this.quantity = quantity;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long EndTime) {
    this.endTime = endTime;
  }

  public Object clone() { 
      return (Object) new UIQuantityScheduleElement(startTime,endTime,quantity);
  }

  public String toString() {
    return quantity + " from " + TimeUtils.dateString(startTime)
      + " to " + TimeUtils.dateString(endTime);
  }
}
