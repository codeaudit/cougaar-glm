/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation;

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
import org.cougaar.util.*;

import com.ibm.xml.parsers.*;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OrgActivity;
import org.cougaar.domain.glm.ldm.oplan.TimeSpan;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

import org.w3c.dom.*;

/**
 * Computes the transportation legs after TAA.
 * This information comes from a combination of a "faked" XML
 * file (ScheduleElements) and Organization-specific information 
 * found in OrgActivities (ScheduleElement OpTempo and ActivityType).
 * <p>
 * The name "clusterID" is used in an awkward way for now.  The
 * PSP wants UIOrgItineraries to have a "clusterID", but then
 * wants to look up UIOrgItineraries in OrgActivities by the
 * "OrgID".  <b>For now these are assumed to be identical!</b>
 */

public class PSP_TransportTAA 
    extends PSP_BaseAdapter 
    implements PlanServiceProvider, UISubscriber
{

  /**
   * Initial default name of file containing OrgItinerary XML.
   * <p>
   * <b>Note:</b>The "xmlFilename=" parameter is "sticky" -- initially
   * it uses this default, and once set by parameter it uses to 
   * the parameter's file from then on (until specified once again).
   */
  public static String DEFAULT_XML_FILE_NAME = "OrgItinerary.xml";

  /**
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   */
  public PSP_TransportTAA() {
    super();
  }

  public PSP_TransportTAA(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
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

  public void execute(AbstractPrinter pr,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    // get parameters
    String xmlFilename = DEFAULT_XML_FILE_NAME;
    String clusterID = null;
    Enumeration params = query_parameters.getURLParameters().elements();
    while (params.hasMoreElements()) {
      String p = (String)params.nextElement();
      if (p.startsWith("orgUID=")) {
        clusterID = p.substring("orgUID=".length()).trim();
        if (clusterID.length() <= 0)
          clusterID = null;
      } else if (p.startsWith("xmlFilename=")) {
        xmlFilename = p.substring("xmlFilename=".length()).trim();
        if (xmlFilename.length() <= 0)
          xmlFilename = null;
      }
    }
    if (clusterID == null)
      throw new RuntimePSPException("Missing \"orgUID\" parameter");

    // get matching orgItinerary

    // syncronize map access?

    UIOrgItinerary orgItin = getOrgItinerary(clusterID, psc, xmlFilename);
    if (orgItin != null) {
      if (!orgItin.getHasOrgActivityInfo()) {
        Vector vOrgActs = searchForOrgActivities(psc, clusterID);
        setOrgActInfo(vOrgActs, orgItin);
      }
    } else {
      System.err.println("Request for unknown orgUID: "+clusterID);
    }

    // return result
    pr.printObject(orgItin);
  }

  protected String mapXMLFilename = null;
  protected Map mapIDtoItin = null;

  protected UIOrgItinerary getOrgItinerary(
      String clusterID, 
      PlanServiceContext psc, 
      String xmlFilename) {
    if ((mapIDtoItin == null) ||
        (xmlFilename != null)) {
      mapIDtoItin = readOrgItineraryMap(psc, xmlFilename);
      mapXMLFilename = xmlFilename;
    }
    return (UIOrgItinerary)mapIDtoItin.get(clusterID);
  }

  /**
   * readOrgItineraryMap builds a <code>Map</code> with "clusterID" as keys
   * and UIOrgItinerary as value.
   * @param xmlFilename XML filename containing Collection of UIOrgItineraries
   * @return Map of (clusterID -&gt; UIOrgItinerary)
   */
  protected Map readOrgItineraryMap(
      PlanServiceContext psc, String xmlFilename) {
    // read collection from XML
    Element root = null;
    try {
      Document doc = ConfigFileFinder.parseXMLConfigFile(xmlFilename);
      if (doc != null)
        root = doc.getDocumentElement();
    } catch (Exception e) {
    }
    if (root == null) {
      System.err.println("PSP_TransportTAA BAD XML File: "+xmlFilename);
      return new HashMap();
    } else {
      //System.out.println("PSP_TransportTAA Read XML File: " + xmlFilename);
    }

    org.cougaar.domain.planning.ldm.RootFactory ldmf = 
      psc.getServerPlugInSupport().getFactoryForPSP();
    Object obj = XMLObjectFactory.parseObject(ldmf, root);
    if (!(obj instanceof Collection)) {
      System.err.println("PSP_TransportTAA INVALID XML File: "+xmlFilename);
      return new HashMap();
    }
    Collection col = (Collection)obj;

    // create Map
    Map m = new HashMap();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      UIOrgItinerary itin = (UIOrgItinerary)iter.next();
      //System.out.println("Itinerary=" + itin.toString() );
      
      if (itin.getClusterID() == null) {
        System.err.println("Itinerary missing clusterID: "+itin);
      } else {
        UIOrgItinerary oldItin = 
          (UIOrgItinerary)m.put(itin.getClusterID(), itin);
        if (oldItin != null) {
          System.err.println("Itinerary "+
              itin+" REPLACES Itinerary: "+oldItin);
        }
      }
    }

    return m;
  }

  /**
   * OrgActivity predicate.
   **/
  protected static UnaryPredicate newOrgActPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof OrgActivity);
      }
    };
  }

  protected Vector searchForOrgActivities(
      PlanServiceContext psc, String clusterID) {
    Iterator iter = 
      psc.getServerPlugInSupport().queryForSubscriber(
        newOrgActPred()).iterator();
    Vector v = new Vector();
    while (iter.hasNext()) {
      OrgActivity orgA = (OrgActivity)iter.next();
      if (clusterID.equals(orgA.getOrgID())) 
        v.addElement(orgA);
    }
    return v;
  }

  protected void setOrgActInfo(Vector vOrgActs, UIOrgItinerary itin) {
    if (vOrgActs.size() <= 0) {
      System.err.println("Itinerary "+itin.getClusterID()+
          " with no matching OrgActivities");
      return;
    }
    Vector vSchedElems = itin.getScheduleElements();
    if (vSchedElems == null) {
      System.err.println("Itinerary lacks schedule elements");
      return;
    }
    // set info from org activities
    itin.setHasOrgActivityInfo(true);
    Enumeration eSchedElems = vSchedElems.elements();
    while (eSchedElems.hasMoreElements()) {
      UIItineraryElement ie = (UIItineraryElement)eSchedElems.nextElement();
      Date startDate = ie.getStartDate();
      // look in OrgActs for matching timespan
      Enumeration eOrgActs = vOrgActs.elements();
      while (true) {
        if (!eOrgActs.hasMoreElements()) {
          System.err.println("ItineraryElement startDate "+startDate+
              " not within TimeSpan of any "+itin.getClusterID()+
              " OrgActivity");
          break;
        }
        OrgActivity oa = (OrgActivity)eOrgActs.nextElement();
        TimeSpan ts;
        if (((ts = oa.getTimeSpan()) != null) &&
            !(startDate.before(ts.getStartDate())) &&
            !(startDate.after(ts.getThruDate()))) {
          ie.setActivityType(oa.getActivityType());
          ie.setOpTempo(oa.getOpTempo());
          break;
        }
      }
    }
  }

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
