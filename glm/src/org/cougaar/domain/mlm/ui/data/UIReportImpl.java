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

import java.util.Date;
import java.util.Vector;

import org.cougaar.domain.planning.ldm.plan.Report;

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
  



