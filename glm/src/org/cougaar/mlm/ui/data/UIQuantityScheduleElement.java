/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.mlm.ui.data;

import java.io.Serializable;

import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.util.TimeSpan;

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

  public void setEndTime(long endTime) {
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
