/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.io.Serializable;
import java.util.Date;

public class UISimpleSchedule implements Serializable {
  Date startDate;
  Date endDate;
  double quantity;

  public UISimpleSchedule(Date startDate, Date endDate, double quantity) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.quantity = quantity;
  }

  public double getQuantity() {
    return quantity;
  }

  public void setQuantity(double quantity) {
    this.quantity = quantity;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date EndDate) {
    this.endDate = endDate;
  }
}
