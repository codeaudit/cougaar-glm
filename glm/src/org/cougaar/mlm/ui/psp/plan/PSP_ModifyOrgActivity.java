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
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.mlm.ui.util.AbstractPrinter;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.mlm.ui.util.XMLObjectFactory;

import org.cougaar.glm.ldm.*;import org.cougaar.glm.ldm.*;import org.cougaar.glm.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.policy.*;

import org.w3c.dom.*;

public class PSP_ModifyOrgActivity 
    extends PSP_BaseAdapter 
    implements PlanServiceProvider, UISubscriber
{

  /**
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   */
  public PSP_ModifyOrgActivity() {
    super();
    setDebug();
  }

  public PSP_ModifyOrgActivity(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /** Utilities for passing XML on HTTP parameter line **/
  public static String fixXmlForParamPassing(String moaXml) {
    String mangledMoaXml = moaXml.replace(' ', '^');
    return mangledMoaXml;
  }

  public static String unfixXmlFromParamPassing(String mangledMoaXml) {
    String moaXml = mangledMoaXml.replace('^', ' ');
    return moaXml;
  }

  protected AbstractPrinter getAbstractPrinter(
      PrintStream out,
      HttpInput query_parameters,
      String defaultFormat) throws Exception {
    String format = defaultFormat;
    Enumeration params = query_parameters.getURLParameters().elements();
    while (params.hasMoreElements()) {
      String p = ((String)params.nextElement()).toLowerCase();
      if (p.startsWith("format=")) {
        format = p.substring("format=".length()).trim();
        if ((format.length() <= 0) ||
            !AbstractPrinter.isValidFormat(format)) {
          throw new RuntimePSPException("Invalid format!: "+format);
        }
      }
    }
    return AbstractPrinter.createPrinter(format, out);
  }

  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    execute(getAbstractPrinter(out, query_parameters, "html"), 
            query_parameters, psc, psu);
  }

  public static final String GET_ORG_ACT_BUTTON = "Get Org Activity";
  public static final String MODIFY_ORG_ACT_BUTTON = "Modify Org Activity";

  public void execute(AbstractPrinter pr,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    // get parameters
    String XMLData = null;
    Enumeration params = query_parameters.getURLParameters().elements();
    while (params.hasMoreElements()) {
      String p = (String)params.nextElement();
      if (p.startsWith("XMLDATA=")) {
        XMLData = p.substring("XMLDATA=".length()).trim();
        if (XMLData.length() <= 0)
          XMLData = null;
      }
    }
    if (XMLData == null)
      throw new RuntimePSPException("Missing \"XMLData\" parameter");

    // read the object from XML
    XMLData = unfixXmlFromParamPassing(XMLData);
    UIModifyOrgActivityState moa = 
      UIModifyOrgActivityState.readFromXML(
        XMLData,
        psc.getServerPluginSupport().getFactoryForPSP());
    if (moa == null) {
      throw new RuntimePSPException("Invalid \"XMLData\" parameter: "+XMLData);
    }

    try {
      String activityType = moa.getTypeText();
      Organization selfOrg;
      Oplan oplan;
      OrgActivity orgAct;
      if ((selfOrg = getSelfOrg(psc)) == null) {
        moa.setStatus("Missing self organization.");
      } else if ((oplan = getOplan(psc)) == null) {
        moa.setStatus("No Oplan yet.");
      } else if ((orgAct = 
                  getOrgActivity(psc, selfOrg, activityType)) == null) {
        moa.drawOrgActivity(null, oplan);
        moa.setStatus("No \""+activityType+"\" Org activities.");
      } else {
        // Have to do this as within transaction boundary
        boolean isGet = GET_ORG_ACT_BUTTON.equals(moa.getPressedButton());

        if (isGet) {
          moa.drawOrgActivity(orgAct, oplan);
          moa.setStatus(true, "Read Org Activity");
        } else {
          // remember date differences
          TimeSpan timeSpan = orgAct.getTimeSpan();
          if (timeSpan == null) {
            throw new Exception("OrgActivity lacks TimeSpan");
          } 
          Date origStartDate = timeSpan.getStartDate();
          Date origEndDate = timeSpan.getEndDate();
          
          PluginDelegate delegate = psc.getServerPluginSupport().getDirectDelegate();
          delegate.openTransaction();
          // modify
          moa.changeOrgActivity(orgAct, oplan);
          // fix other orgActs
          fixAdjacentOrgActivities(psc, orgAct,
                                   origStartDate, origEndDate);
          // changed orgAct
          publishChange(psc, orgAct);
          delegate.closeTransaction();

          moa.setStatus(true, "Modified Org Activity");
        }
      }
    } catch (Exception e) {
      if (DEBUG) {
        System.out.println("EXCEPTION: "+e);
        e.printStackTrace();
      }
      moa.setStatus("FAILED: "+e.getMessage());
    }

    // return result
    pr.printObject(moa);
  }

  /**
   * Find an orgAct with a endDate <= the origStartDate.
   */
  protected OrgActivity findPrevOrgActivity(
      PlanServiceContext psc, 
      OrgActivity orgAct, 
      long maxEndTime) {
    String orgId = orgAct.getOrgID();
    long bestMaxEndTime = 0;
    OrgActivity prevOrgAct = null;
    Enumeration eOrgActs = searchForOrgActivities(psc);
    while (eOrgActs.hasMoreElements()) {
      OrgActivity oa = (OrgActivity)eOrgActs.nextElement();
      if (orgId.equals(oa.getOrgID()) &&
          (oa != orgAct)) {
        TimeSpan ts;
        Date td;
        long tt;
        if (((ts = oa.getTimeSpan()) != null) &&
            (ts.getStartDate() != null) &&
            ((td = ts.getEndDate()) != null) &&
            ((tt = td.getTime()) <= maxEndTime) &&
            (tt > bestMaxEndTime)) {
          prevOrgAct = oa;
          bestMaxEndTime = tt;
        }
      }
    }
    return prevOrgAct;
  }

  /**
   * find an orgAct with a startDate >= the origEndDate
   */
  protected OrgActivity findNextOrgActivity(
      PlanServiceContext psc, 
      OrgActivity orgAct,
      long minStartTime) {
    long bestMinStartTime = TimeSpan.MAX_VALUE;
    String orgId = orgAct.getOrgID();
    OrgActivity nextOrgAct = null;
    Enumeration eOrgActs = searchForOrgActivities(psc);
    while (eOrgActs.hasMoreElements()) {
      OrgActivity oa = (OrgActivity)eOrgActs.nextElement();
      if (orgId.equals(oa.getOrgID()) &&
          (oa != orgAct)) {
        TimeSpan ts;
        Date sd;
        long st;
        if (((ts = oa.getTimeSpan()) != null) &&
            ((sd = ts.getStartDate()) != null) &&
            (ts.getEndDate() != null) &&
            ((st = sd.getTime()) >= minStartTime) &&
            (st < bestMinStartTime)) {
          nextOrgAct = oa;
          bestMinStartTime = st;
        }
      }
    }
    return nextOrgAct;
  }

  protected Date getNewPrevEndDate(
      OrgActivity prevOrgAct, long deltaStartTime) throws Exception {
    TimeSpan timeSpan = prevOrgAct.getTimeSpan();
    Date startDate = timeSpan.getStartDate();
    Date endDate = timeSpan.getEndDate();
    Date newPrevEndDate = new Date(endDate.getTime() + deltaStartTime);
    if (DEBUG) {
      System.out.println("Orig Prev StartDate: "+startDate);
      System.out.println("Orig Prev EndDate: "+endDate);
      System.out.println("New Prev EndDate: "+newPrevEndDate);
    }
    if (newPrevEndDate.before(startDate)) {
      if (DEBUG) {
        System.out.println("BAD: NewEndDate < OrigStartDate");
      }
      throw new Exception(
         "Can't adjust endDate to "+newPrevEndDate+
         " which is before startDate "+startDate);
    }
    return newPrevEndDate;
  }

  protected Date getNewNextStartDate(
      OrgActivity nextOrgAct, long deltaEndTime) throws Exception {
    TimeSpan timeSpan = nextOrgAct.getTimeSpan();
    Date startDate = timeSpan.getStartDate();
    Date endDate = timeSpan.getEndDate();
    Date newNextStartDate = new Date(startDate.getTime() + deltaEndTime);
    if (DEBUG) {
      System.out.println("Orig Next StartDate: "+startDate);
      System.out.println("Orig Next EndDate: "+endDate);
      System.out.println("New Next StartDate: "+newNextStartDate);
    }
    if (newNextStartDate.after(endDate)) {
      if (DEBUG) {
        System.out.println("BAD: NewStartDate < OrigEndDate");
      }
      throw new Exception(
          "Can't adjust startDate to "+newNextStartDate+
          " which is after endDate "+endDate);
    }
    return newNextStartDate;
  }

  /**
   * The OrgActivity <code>orgAct</code> has changed, maybe moving
   * it's start and end date.  Adjust the cronologically adjacent
   * orgActivities for this timespan change, pushing them earlier 
   * or later to prevent overlap.
   */
  protected void fixAdjacentOrgActivities(
      PlanServiceContext psc, OrgActivity orgAct,
      Date origStartDate, Date origEndDate) throws Exception {
    TimeSpan newTimeSpan = orgAct.getTimeSpan();
    Date newStartDate = newTimeSpan.getStartDate();
    Date newEndDate = newTimeSpan.getEndDate();
    long deltaStartTime = (newStartDate.getTime() - origStartDate.getTime());
    long deltaEndTime = (newEndDate.getTime() - origEndDate.getTime());
    OrgActivity prevOrgAct = null;
    if (deltaStartTime != 0) {
      if (DEBUG) {
        System.out.println("Adjust prev orgAct startTime");
        System.out.println("  this orgAct original startDate: "+origStartDate);
        System.out.println("  this orgAct new startDate: "+newStartDate);
        System.out.println("  this orgAct start delta: "+deltaStartTime);
      }
      prevOrgAct = findPrevOrgActivity(psc, orgAct, origStartDate.getTime());
      if (DEBUG) {
        System.out.println("Found prevOrgAct: "+prevOrgAct);
      }
      // ok if null --> no prev
    }
    OrgActivity nextOrgAct = null;
    if (deltaEndTime != 0) {
      if (DEBUG) {
        System.out.println("Adjust next orgAct endTime");
        System.out.println("  this orgAct original endDate: "+origEndDate);
        System.out.println("  this orgAct new endDate: "+newEndDate);
        System.out.println("  this orgAct end delta: "+deltaEndTime);
      }
      nextOrgAct = findNextOrgActivity(psc, orgAct, origEndDate.getTime());
      if (DEBUG) {
        System.out.println("Found nextOrgAct: "+nextOrgAct);
      }
      // ok if null --> no next
    }
    Object obj;
    // get new end date for prev orgAct
    Date newPrevEndDate = null;
    if (prevOrgAct != null) {
      if (DEBUG) {
        System.out.println("Get the prevOrgAct new EndDate");
      }
      newPrevEndDate = getNewPrevEndDate(prevOrgAct, deltaStartTime);
      if (DEBUG) {
        System.out.println("  prevOrgAct new EndDate: "+newPrevEndDate);
      }
    }
    // get new start date for next orgAct
    Date newNextStartDate = null;
    if (nextOrgAct != null) {
      if (DEBUG) {
        System.out.println("Get the nextOrgAct new StartDate");
      }
      newNextStartDate = getNewNextStartDate(nextOrgAct, deltaEndTime);
      if (DEBUG) {
        System.out.println("  nextOrgAct new StartDate: "+newNextStartDate);
      }
    }
    // change the prev orgAct's end date
    if (prevOrgAct != null) {
      prevOrgAct.getTimeSpan().setEndDate(newPrevEndDate);
      publishChange(psc, prevOrgAct);
    } 
    // change the next orgAct's start date
    if (nextOrgAct != null) {
      nextOrgAct.getTimeSpan().setStartDate(newNextStartDate);
      publishChange(psc, nextOrgAct);
    }
  }

  protected void publishChange(
      PlanServiceContext psc, Object obj) {
    psc.getServerPluginSupport().getDirectDelegate().publishChange(obj);
  }

  protected Enumeration searchUsingUnaryPredicate(
      PlanServiceContext psc, UnaryPredicate pred) {
    Subscription subscription = 
      psc.getServerPluginSupport().getDirectDelegate().subscribe(pred);
    return ((CollectionSubscription)subscription).elements();
  }

  protected Enumeration searchForOrgs(PlanServiceContext psc) {
    UnaryPredicate pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Organization);
      }
    };
    return searchUsingUnaryPredicate(psc, pred);
  }

  protected Enumeration searchForOrgActivities(PlanServiceContext psc) {
    UnaryPredicate pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof OrgActivity);
      }
    };
    return searchUsingUnaryPredicate(psc, pred);
  }

  protected Enumeration searchForOplan(PlanServiceContext psc) {
    UnaryPredicate pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Oplan);
      }
    };
    return searchUsingUnaryPredicate(psc, pred);
  }

  protected static String getOrgId(Organization org) {
    String s = null;
    try {
      // FOR NOW:
      s = org.getClusterPG().getMessageAddress().toString();
      // FOR LATER:
      //s = org.getItemIdentificationPG().getItemIdentification();
    } catch (Exception e) {}
    return s;
  }

  protected Organization getSelfOrg(
      PlanServiceContext psc) {
    Enumeration eOrgs = searchForOrgs(psc);
    while (eOrgs.hasMoreElements()) {
      Organization org = (Organization)eOrgs.nextElement();
      if (org.isSelf()) {
        return org;
      }
    }
    return null;
  }

  protected OrgActivity getOrgActivity(
      PlanServiceContext psc,
      Organization org, String activityType) {
    return getOrgActivity(psc, getOrgId(org), activityType);
  }

  protected OrgActivity getOrgActivity(
       PlanServiceContext psc,
       String orgId, String activityType) {
    OrgActivity orgAct = null;
    if ((orgId != null) && (activityType != null)) {
      Enumeration eOrgActs = searchForOrgActivities(psc);
      while (eOrgActs.hasMoreElements()) {
        OrgActivity oa = (OrgActivity)eOrgActs.nextElement();
        if (orgId.equals(oa.getOrgID()) &&
              activityType.equals(oa.getActivityType())) {
            orgAct = oa;
          break;
        }
      }
    }
    return orgAct;
  }

  protected Oplan getOplan(PlanServiceContext psc) {
    Oplan oplan = null;
    // which oplan?  take the first one for now.
    Enumeration eOplans = searchForOplan(psc);
    if (eOplans.hasMoreElements()) {
      oplan = (Oplan)eOplans.nextElement();
    }
    return oplan;
  }

  /*
  protected boolean DEBUG;
  protected boolean setDebug() {
    // try system property
    String sysProp = System.getProperty(this.getClass().getName()+".debug");
    if (sysProp != null) {
      DEBUG = "true".equalsIgnoreCase(sysProp);
      return DEBUG;
    }
    // default
    DEBUG = false;
    return DEBUG;
  }
  */
  /** DEBUG forced off! **/
  protected static final boolean DEBUG = false;
  protected boolean setDebug() {return false;}

  protected String getClusterNameFromUID(String uuid) {
    int i = uuid.indexOf("/");
    String nme = uuid.substring(0,i);
    return nme;
  }

  /** Get rid of "<..>" prefix/suffix **/
  public String getClusterIDAsString( String rawCID) {
    String id  = rawCID;
    if( id.startsWith("<") ) id = id.substring(1);
    if( id.endsWith(">") ) id = id.substring(0,id.length()-1);
    return id;
  }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   */
  public boolean returnsXML() {
    return false;
  }

  public boolean returnsHTML() {
    return true;
  }

  /**  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   */
  public String getDTD()  {
    return null;
  }

  public void subscriptionChanged(Subscription subscription) {
  }

}
