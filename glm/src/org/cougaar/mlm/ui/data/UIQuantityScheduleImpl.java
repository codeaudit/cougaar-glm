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
