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
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.cluster.*;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.AbstractPrinter;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.XMLObjectFactory;

import com.ibm.xml.parsers.*;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;

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
        psc.getServerPlugInSupport().getFactoryForPSP());
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
          Date origThruDate = timeSpan.getThruDate();
          // modify
          moa.changeOrgActivity(orgAct, oplan);
          // fix other orgActs
          fixAdjacentOrgActivities(psc, orgAct,
             origStartDate, origThruDate);
          // changed orgAct
          publishChange(psc, orgAct);
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
   * Find an orgAct with a thruDate <= the origStartDate.
   */
  protected OrgActivity findPrevOrgActivity(
      PlanServiceContext psc, 
      OrgActivity orgAct, 
      long maxThruTime) {
    String orgId = orgAct.getOrgID();
    long bestMaxThruTime = 0;
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
            ((td = ts.getThruDate()) != null) &&
            ((tt = td.getTime()) <= maxThruTime) &&
            (tt > bestMaxThruTime)) {
          prevOrgAct = oa;
          bestMaxThruTime = tt;
        }
      }
    }
    return prevOrgAct;
  }

  /**
   * find an orgAct with a startDate >= the origThruDate
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
            (ts.getThruDate() != null) &&
            ((st = sd.getTime()) >= minStartTime) &&
            (st < bestMinStartTime)) {
          nextOrgAct = oa;
          bestMinStartTime = st;
        }
      }
    }
    return nextOrgAct;
  }

  protected Date getNewPrevThruDate(
      OrgActivity prevOrgAct, long deltaStartTime) throws Exception {
    TimeSpan timeSpan = prevOrgAct.getTimeSpan();
    Date startDate = timeSpan.getStartDate();
    Date thruDate = timeSpan.getThruDate();
    Date newPrevThruDate = new Date(thruDate.getTime() + deltaStartTime);
    if (DEBUG) {
      System.out.println("Orig Prev StartDate: "+startDate);
      System.out.println("Orig Prev ThruDate: "+thruDate);
      System.out.println("New Prev ThruDate: "+newPrevThruDate);
    }
    if (newPrevThruDate.before(startDate)) {
      if (DEBUG) {
        System.out.println("BAD: NewThruDate < OrigStartDate");
      }
      throw new Exception(
         "Can't adjust thruDate to "+newPrevThruDate+
         " which is before startDate "+startDate);
    }
    return newPrevThruDate;
  }

  protected Date getNewNextStartDate(
      OrgActivity nextOrgAct, long deltaThruTime) throws Exception {
    TimeSpan timeSpan = nextOrgAct.getTimeSpan();
    Date startDate = timeSpan.getStartDate();
    Date thruDate = timeSpan.getThruDate();
    Date newNextStartDate = new Date(startDate.getTime() + deltaThruTime);
    if (DEBUG) {
      System.out.println("Orig Next StartDate: "+startDate);
      System.out.println("Orig Next ThruDate: "+thruDate);
      System.out.println("New Next StartDate: "+newNextStartDate);
    }
    if (newNextStartDate.after(thruDate)) {
      if (DEBUG) {
        System.out.println("BAD: NewStartDate < OrigThruDate");
      }
      throw new Exception(
          "Can't adjust startDate to "+newNextStartDate+
          " which is after thruDate "+thruDate);
    }
    return newNextStartDate;
  }

  /**
   * The OrgActivity <code>orgAct</code> has changed, maybe moving
   * it's start and thru date.  Adjust the cronologically adjacent
   * orgActivities for this timespan change, pushing them earlier 
   * or later to prevent overlap.
   */
  protected void fixAdjacentOrgActivities(
      PlanServiceContext psc, OrgActivity orgAct,
      Date origStartDate, Date origThruDate) throws Exception {
    TimeSpan newTimeSpan = orgAct.getTimeSpan();
    Date newStartDate = newTimeSpan.getStartDate();
    Date newThruDate = newTimeSpan.getThruDate();
    long deltaStartTime = (newStartDate.getTime() - origStartDate.getTime());
    long deltaThruTime = (newThruDate.getTime() - origThruDate.getTime());
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
    if (deltaThruTime != 0) {
      if (DEBUG) {
        System.out.println("Adjust next orgAct thruTime");
        System.out.println("  this orgAct original thruDate: "+origThruDate);
        System.out.println("  this orgAct new thruDate: "+newThruDate);
        System.out.println("  this orgAct thru delta: "+deltaThruTime);
      }
      nextOrgAct = findNextOrgActivity(psc, orgAct, origThruDate.getTime());
      if (DEBUG) {
        System.out.println("Found nextOrgAct: "+nextOrgAct);
      }
      // ok if null --> no next
    }
    Object obj;
    // get new thru date for prev orgAct
    Date newPrevThruDate = null;
    if (prevOrgAct != null) {
      if (DEBUG) {
        System.out.println("Get the prevOrgAct new ThruDate");
      }
      newPrevThruDate = getNewPrevThruDate(prevOrgAct, deltaStartTime);
      if (DEBUG) {
        System.out.println("  prevOrgAct new ThruDate: "+newPrevThruDate);
      }
    }
    // get new start date for next orgAct
    Date newNextStartDate = null;
    if (nextOrgAct != null) {
      if (DEBUG) {
        System.out.println("Get the nextOrgAct new StartDate");
      }
      newNextStartDate = getNewNextStartDate(nextOrgAct, deltaThruTime);
      if (DEBUG) {
        System.out.println("  nextOrgAct new StartDate: "+newNextStartDate);
      }
    }
    // change the prev orgAct's thru date
    if (prevOrgAct != null) {
      prevOrgAct.getTimeSpan().setThruDate(newPrevThruDate);
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
    psc.getServerPlugInSupport().publishChangeForSubscriber(obj);
  }

  protected Enumeration searchUsingUnaryPredicate(
      PlanServiceContext psc, UnaryPredicate pred) {
    Subscription subscription = 
      psc.getServerPlugInSupport().subscribe(this, pred);
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
      s = org.getClusterPG().getClusterIdentifier().toString();
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
