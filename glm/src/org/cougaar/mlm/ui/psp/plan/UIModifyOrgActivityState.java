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
 
package org.cougaar.mlm.ui.psp.plan;

import java.io.*;
import java.util.*;

import org.cougaar.core.domain.Factory;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.util.ShortDateFormat;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.mlm.ui.util.XMLObjectFactory;

import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.TimeSpan;

import org.w3c.dom.Element;

public class UIModifyOrgActivityState implements org.cougaar.mlm.ui.util.SelfPrinter {

  public static void main(String[] args) {
    UIModifyOrgActivityState moa = new UIModifyOrgActivityState();
    moa.setPressedButton("my_button");
    System.out.println("######MOA######");
    System.out.println(moa);
    System.out.println("######MOA TO XML######");
    String xml = moa.toXMLString();
    System.out.println("######MOA XML######");
    System.out.println(xml);
    System.out.println("######XML TO MOAXML######");
    UIModifyOrgActivityState moaXml = 
      readFromXML(xml, XMLObjectFactory.emptyLFactory);
    System.out.println("######MOAXML######");
    System.out.println(moaXml);
  }

  /**
   * Read instance from XML String
   */
  public static UIModifyOrgActivityState readFromXML(
      String xml, Factory ldmf) {
    try {
      ReaderInputStream ris = 
        new ReaderInputStream(
          new StringReader(xml));
      return readFromXML(ris, ldmf);
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static UIModifyOrgActivityState readFromXML(
      InputStream in, Factory ldmf) {
    try {
      Element root = XMLObjectFactory.readXMLRoot(in);
      if (root != null) {
        PlanningFactory rf = (PlanningFactory) ldmf; // must be a root factory
        Object obj = XMLObjectFactory.parseObject(rf, root);
        if (obj instanceof UIModifyOrgActivityState)
          return (UIModifyOrgActivityState)obj;
      }
    } catch (Exception eBadXML) {
    }
    return null;
  }

  /**
   * Write instance to XML String
   */
  public String toXMLString() {
    //FIXME return org.cougaar.mlm.ui.util.XMLPrinter.toString(this);
    java.io.ByteArrayOutputStream baout =
      new java.io.ByteArrayOutputStream();
    org.cougaar.mlm.ui.util.XMLPrinter pr = new org.cougaar.mlm.ui.util.XMLPrinter(baout);
    pr.printObject((org.cougaar.mlm.ui.util.SelfPrinter)this);
    return baout.toString();
  }

  protected String pressedButton;

  protected String orgIdLabel;
  protected String typeText;
  protected String opTempoText;
  protected String cDayLabel;
  protected String startText;
  protected String endText;
  protected String statusLabel;
  protected boolean statusError;

  public UIModifyOrgActivityState() {
    makeClean();
  }

  public void makeClean() {
    pressedButton = "";
    orgIdLabel = "";
    typeText = "";
    opTempoText = "";
    cDayLabel = "";
    startText = "";
    endText = "";
    statusLabel = "";
    statusError = false;
  }

  public void setPressedButton(String s) {pressedButton = takeString(s);}
  public String getPressedButton() {return pressedButton;}

  public void setOrgIdLabel(String s) {orgIdLabel = takeString(s);}
  public String getOrgIdLabel() {return orgIdLabel;}

  public void setTypeText(String s) {typeText = takeString(s);}
  public String getTypeText() {return typeText;}

  public void setOpTempoText(String s) {opTempoText = takeString(s);}
  public String getOpTempoText() {return opTempoText;}

  public void setCDayLabel(String s) {cDayLabel = takeString(s);}
  public String getCDayLabel() {return cDayLabel;}

  public void setStartText(String s) {startText = takeString(s);}
  public String getStartText() {return startText;}

  public void setEndText(String s) {endText = takeString(s);}
  public String getEndText() {return endText;}

  public void setStatusLabel(String s) {statusLabel = takeString(s);}
  public String getStatusLabel() {return statusLabel;}

  public void setStatusError(boolean b) {statusError = b;}
  public boolean getStatusError() {return statusError;}

  protected static String takeString(String s) {
    return ((s != null) ? s.trim() : "");
  }

  public void printContent(org.cougaar.mlm.ui.util.AsciiPrinter pr) {
    pr.print(pressedButton, "PressedButton");
    pr.print(orgIdLabel, "OrgIdLabel");
    pr.print(typeText, "TypeText");
    pr.print(opTempoText, "OpTempoText");
    pr.print(cDayLabel, "CDayLabel");
    pr.print(startText, "StartText");
    pr.print(endText, "EndText");
    pr.print(statusLabel, "StatusLabel");
    pr.print(statusError, "StatusError");
  }

  public String toString() {
    return org.cougaar.mlm.ui.util.PrettyStringPrinter.toString(this);
  }

  public void drawOrgActivity(OrgActivity orgAct, Oplan oplan) 
      throws Exception {
    Date cDay;
    if ((oplan == null) ||
        ((cDay = oplan.getCday()) == null)) {
      throw new Exception("Oplan lacks cDay");
    }
    ShortDateFormat dateFormatter = new ShortDateFormat();
    String cDateString =
       dateFormatter.toString(
         cDay);
    if (orgAct == null) {
      orgIdLabel = "";
      opTempoText = "";
      cDayLabel = cDateString;
      startText = "";
      endText = "";
      throw new Exception("Missing Org Activity");
    }
    if (orgAct.getOplanUID() != oplan.getUID()) {
      throw new Exception(
         "Wrong Oplan ("+oplan.getUID()+
         ") for this Org Activity ("+orgAct.getOplanUID()+")!");
    }
    TimeSpan timeSpan = orgAct.getTimeSpan();
    if (timeSpan == null) {
      throw new Exception("Missing Org Activity TimeSpan");
    }
    String startDateString;
    if (timeSpan.getStartDate() == null)
      startDateString = cDateString;
    else {
      startDateString =
        dateFormatter.toString(
            timeSpan.getStartDate());
    }
    String endDateString;
    if (timeSpan.getEndDate() == null)
      endDateString = cDateString;
    else {
      endDateString =
        dateFormatter.toString(
            timeSpan.getEndDate());
    }
    orgIdLabel = orgAct.getOrgID();
    opTempoText = orgAct.getOpTempo();
    cDayLabel = cDateString;
    startText = startDateString;
    endText = endDateString;
  }

  /**
   * @return null if orgAct needs to be publishChanged, otherwise error String
   */
  public String changeOrgActivity(OrgActivity orgAct, Oplan oplan) 
      throws Exception {
    Date cDay;
    if ((oplan == null) ||
        ((cDay = oplan.getCday()) == null)) {
      throw new Exception("Oplan lacks cDay");
    }
    String opTempo = opTempoText;
    if (opTempo.length() < 1) {
      throw new Exception("Invalid OpTempo");
    }
    TimeSpan timeSpan = orgAct.getTimeSpan();
    if (timeSpan == null) {
      throw new Exception("Missing Org Activity TimeSpan");
    }
    ShortDateFormat dateFormatter = new ShortDateFormat();
    Date startDate = dateFormatter.toDate(startText, false);
    if (startDate == null) {
      throw new Exception("Invalid start date");
    }
    Date endDate = dateFormatter.toDate(endText, false);
    if (endDate == null) {
      throw new Exception("Invalid end date");
    }
    boolean changed = false;
    if (!opTempo.equals(orgAct.getOpTempo())) {
      orgAct.setOpTempo(opTempo);
      changed = true;
    }
    if (!startDate.equals(timeSpan.getStartDate())) {
      timeSpan.setStartDate(startDate);
      changed = true;
    }
    if (!endDate.equals(timeSpan.getEndDate())) {
      timeSpan.setEndDate(endDate);
      changed = true;
    }
    if (!changed) {
      throw new Exception("No change to Org Activity");
    }
    return null;
  }

  public void setStatus(String s) {
    setStatus(true, s);
  }
  public void setStatus(boolean isSuccess, String s) {
    statusError = !isSuccess;
    statusLabel = s;
  }

  /** Simple InputStream from String **/
  protected static class ReaderInputStream extends InputStream {
    StringReader strR;
    public ReaderInputStream(StringReader sr) {
      strR = sr;
    }
    public int read() throws IOException {
      return strR.read();
    }
  }

}
