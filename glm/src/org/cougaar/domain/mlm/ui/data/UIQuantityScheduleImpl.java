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


import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class UIQuantityScheduleImpl implements UIQuantitySchedule, XMLUIPlanObject {
  long startTime;
  long endTime;
  double quantity;

  public UIQuantityScheduleImpl(long startTime, long endTime, double quantity) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.quantity = quantity;
  }
                
  public UIQuantityScheduleImpl(long startTime, double quantity) {
    this.startTime = startTime;
    this.quantity = quantity;
    endTime = -1;
  }

  /**
   * Return the start date from a schedule element.
   @return Date - start date
   **/

  public Date getStartDate() {
      return new Date(startTime);
  }

  /**
   * Return the start time from a schedule element.
   @return long - start time
   **/

  public long getStartTime() {
    return startTime;
  }

  /**
   * Return the end date from a schedule element.
   @return Date - end date
   **/

  public Date getEndDate() {
      return new Date(endTime);
  } 

  /**
   * Return the end time from a schedule element.
   @return long - end time
   **/

  public long getEndTime() {
    return endTime;
  }

  /**
   * Return the quantity from a schedule element.
   @return double - quantity
   **/

  public double getQuantity() {
    return quantity;
  }

  /** Creates XML version of this object for user interface.
    @return Element - an element of an XML document
   */
  
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }

} 
