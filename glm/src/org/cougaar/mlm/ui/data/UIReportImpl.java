/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import java.util.Date;
import java.util.Vector;

import org.cougaar.planning.ldm.plan.Report;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class UIReportImpl implements UIReport, XMLUIPlanObject {
  private Report myReport;

  /**
   * Constructor 
   *
   * @param report Report to use as basis
   */
  public UIReportImpl(Report report) {
    myReport = report;
  }
   
  /**
   * getInfoText - Informational text
   *
   * @return String informational message
   **/
  public String getText() {
    return myReport.getText();
  }

  /**
   * getDate - Date object was injected into the system
   *
   * @return Date creation date for message
   **/
  public Date getDate() {
    return myReport.getDate();
  }


  /** 
   * getXML - XMLPlanObject method for UI
   */
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, 
                                                     requestedFields);
  }

}
  



