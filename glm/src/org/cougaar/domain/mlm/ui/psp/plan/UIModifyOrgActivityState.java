/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.plan;

import java.io.*;
import java.util.*;

import org.cougaar.domain.planning.ldm.Factory;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.util.ShortDateFormat;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.XMLObjectFactory;

import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OrgActivity;
import org.cougaar.domain.glm.ldm.oplan.TimeSpan;

import org.w3c.dom.Element;

public class UIModifyOrgActivityState implements org.cougaar.util.SelfPrinter {

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
        RootFactory rf = (RootFactory) ldmf; // must be a root factory
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
    //FIXME return org.cougaar.util.XMLPrinter.toString(this);
    java.io.ByteArrayOutputStream baout =
      new java.io.ByteArrayOutputStream();
    org.cougaar.util.XMLPrinter pr = new org.cougaar.util.XMLPrinter(baout);
    pr.printObject((org.cougaar.util.SelfPrinter)this);
    return baout.toString();
  }

  protected String pressedButton;

  protected String orgIdLabel;
  protected String typeText;
  protected String opTempoText;
  protected String cDayLabel;
  protected String startText;
  protected String thruText;
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
    thruText = "";
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

  public void setThruText(String s) {thruText = takeString(s);}
  public String getThruText() {return thruText;}

  public void setStatusLabel(String s) {statusLabel = takeString(s);}
  public String getStatusLabel() {return statusLabel;}

  public void setStatusError(boolean b) {statusError = b;}
  public boolean getStatusError() {return statusError;}

  protected static String takeString(String s) {
    return ((s != null) ? s.trim() : "");
  }

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    pr.print(pressedButton, "PressedButton");
    pr.print(orgIdLabel, "OrgIdLabel");
    pr.print(typeText, "TypeText");
    pr.print(opTempoText, "OpTempoText");
    pr.print(cDayLabel, "CDayLabel");
    pr.print(startText, "StartText");
    pr.print(thruText, "ThruText");
    pr.print(statusLabel, "StatusLabel");
    pr.print(statusError, "StatusError");
  }

  public String toString() {
    return org.cougaar.util.PrettyStringPrinter.toString(this);
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
      thruText = "";
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
    String thruDateString;
    if (timeSpan.getThruDate() == null)
      thruDateString = cDateString;
    else {
      thruDateString =
        dateFormatter.toString(
            timeSpan.getThruDate());
    }
    orgIdLabel = orgAct.getOrgID();
    opTempoText = orgAct.getOpTempo();
    cDayLabel = cDateString;
    startText = startDateString;
    thruText = thruDateString;
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
    Date thruDate = dateFormatter.toDate(thruText, false);
    if (thruDate == null) {
      throw new Exception("Invalid thru date");
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
    if (!thruDate.equals(timeSpan.getThruDate())) {
      timeSpan.setThruDate(thruDate);
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
