/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.plan;

import java.io.PrintStream;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.policy.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;

/**
  * This is a demonstration/proof-of-concept PSP which illustrates a number
  * of techniques based on PSP (server-side) layout and HTML to construct
  * a "multi-pane" Task View.
  *
  *
  * Important to note how cross-references between Cluster LPSs are specified in HTML.  No longer will
  * browser client directly speak to any LPS other than its host.  Task drill-down and
  * Task views to other clusters are handled via redirection at server rather than
  * URL link from client.
  *
  * This is required by Netscape browsers to work with multiple-form/multipe-URL data
  * model.  Netscape browsers insist on opening new window when Host changes.
  *
  **/

public class PSP_OplanView extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
      
  /** A zero-argument constructor is required for dynamically loaded PSPs,
      required by Class.newInstance()
  **/
  public PSP_OplanView() {
    super();
  }

        /*************************************************************************************
         * 
         **/
  public PSP_OplanView( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

        /*************************************************************************************
         * 
         **/
  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

        /*************************************************************************************
         * 
         **/
  public void execute( PrintStream out,
                        HttpInput query_parameters,
                        PlanServiceContext psc,
                        PlanServiceUtilities psu ) throws Exception    {
    //
    // MODE:
    // 1 = OPLAN AT IDENTIFIED CLUSTER
    // 2 = DISPLAY ALL CLUSTERS
    //
    String modeID =  (String)query_parameters.getFirstParameterToken("MODE", '=');
    int mode=0;
    try{ mode = Integer.parseInt(modeID); } catch( Exception e) { mode=0; };

    //
    // clusterID required for ALL MODES (1,2)
    //
    String clusterID = (String)query_parameters.getFirstParameterToken("CLUSTER", '=');
                
                switch( mode ) {
                        case 1:
                                displayOPlan(out, psc);
        break;
      case 2:
                                displayAllClusters(out,psc);
                                break;
/*
      case 3:
        displayOPlanTOC(out, psc);
        break;
*/
                        default:        
                                out.println("<HTML>");
                                out.println("<HEAD>");
                                out.println("<TITLE>");
                                out.println("Cluster OPlan Propagation View");
                                out.println("</TITLE>");
                                out.println("</HEAD>");
                                out.println("<FRAMESET cols=\"20%,80%\">");
                                out.println("<FRAME src=\"/alpine/demo/OPLAN.PSP?MODE=2 \" name=\"clustersFrame\">");
                                out.println("<FRAME src=\"/alpine/demo/OPLAN.PSP?MODE=1\" name=\"thisClusterOplanFrame\">");
                                out.println("</FRAMESET>");
                                out.println("<NOFRAMES>");
                                out.println("<H2>Frame Propagation</H2>");
                                out.println("<P>This document is designed to be viewed using the frames feature.  If you see this message, you are using a non-frame-capable web client.");
                                out.println("</HTML>");
                                break;
    }
  }


  /**
    * A PSP can output either HTML or XML (for now).  The server
    * should be able to ask and find out what type it is.
    **/
  public boolean returnsXML() {
    return false;
  }

  /**
    * A PSP can output either HTML or XML (for now).  The server
    * should be able to ask and find out what type it is.
    **/
  public boolean returnsHTML() {
    return true;
  }

  /**  Any PlanServiceProvider must be able to provide DTD of its
    *  output IFF it is an XML PSP... ie.  returnsXML() == true;
    *  or return null
    **/
  public String getDTD()  {
    return null;
  }


  /*************************************************************************************
   * 
   **/
  public void subscriptionChanged(Subscription subscription) {

  }

  /*************************************************************************************
   * 
   **/
  public void displayAllClusters(PrintStream out, PlanServiceContext psc) {     
    Vector urls = new Vector();
    Vector names = new Vector();

    psc.getAllURLsAndNames(urls, names);

    out.println(PSPOplanUtilities.openDoc("Oplan Propagation View"));
    out.println("<DIV ALIGN=\"left\">");
    out.println("<TABLE>");

    int sz = urls.size();
    int i;
    for(i=0;i<sz;i++) {
      String u = (String)urls.elementAt(i);
      String n = (String)names.elementAt(i);
      out.println("<tr><td><FONT SIZE=small COLOR=mediumblue>");
      out.println("<A HREF=\"/$"+n+"/alpine/demo/OPLAN.PSP?MODE=1\" TARGET=thisClusterOplanFrame></A></FONT>");
      out.println("</td><td>");
      out.println("<A HREF=\"/$"+n+"/alpine/demo/OPLAN.PSP?MODE=1\" TARGET=thisClusterOplanFrame>" + n + "</A></FONT>");
      out.println("</td></tr>");
    }
    
    out.println("</TABLE></DIV>");
    out.println(PSPOplanUtilities.closeDoc());
  }

  /*************************************************************************************
   * 
   **/
  public void displayOPlan(PrintStream out, PlanServiceContext psc) {

    try {        
      Oplan plan = PSPOplanUtilities.getOplan(psc);

      Iterator org_activities = PSPOplanUtilities.getOrgActivities(psc).iterator();

      out.println(PSPOplanUtilities.openDoc("Oplan View"));

      if (plan == null) 
        out.println("<H1>No Oplan Found</H1>");

                        else {
        out.println("<H1>Oplan</H1>");

        // OPlan Internal
        out.println(PSPOplanUtilities.sectionHeader("Filename, ID and Version"));
        out.println(PSPOplanUtilities.openTable("20"));
        out.println(PSPOplanUtilities.writeProperty("XMLFileName", plan.getXMLFileName()));
        out.println(PSPOplanUtilities.writeProperty("OplanId", plan.getOplanId()));
        out.println(PSPOplanUtilities.writeProperty("UID", plan.getUID()));
        Double version = new Double(plan.getVersion());
        out.println(PSPOplanUtilities.writeProperty("Version", version));
        out.println(PSPOplanUtilities.closeTable());

        // Oplan Operation Information
        out.println(PSPOplanUtilities.sectionHeader("Operation Info"));
        out.println(PSPOplanUtilities.openTable("20"));
        out.println(PSPOplanUtilities.writeProperty("OperationName", plan.getOperationName()));
        out.println(PSPOplanUtilities.writeProperty("Priority", plan.getPriority()));
        out.println(PSPOplanUtilities.closeTable());

        // Cincs
        out.println(PSPOplanUtilities.sectionHeader("CINCs"));
        out.println(PSPOplanUtilities.openTable("20"));
//        out.println(PSPOplanUtilities.writeProperty("CINC", PSPOplanUtilities.writeCINCs(plan.getCincs())));
        out.println(PSPOplanUtilities.closeTable());

        // Oplan Timespan
        out.println(PSPOplanUtilities.sectionHeader("Timespan"));
        out.println(PSPOplanUtilities.openTable("20"));
        out.println(PSPOplanUtilities.writeProperty("Cday", plan.getCday()));

//        TimeSpan ts = new TimeSpan(plan);
//        ts.setStartDelta(plan.getStartDelta());
//        ts.setEndDelta(plan.getEndDelta());
//        out.println(PSPOplanUtilities.writeProperty("Timespan", PSPOplanUtilities.writeTimeSpan(ts)));
        //out.println(writeProperty("Timespan", writeTimeSpan(plan.getTimeSpan())));
        out.println(PSPOplanUtilities.closeTable());

        // TheaterInfo
//        out.println(PSPOplanUtilities.sectionHeader("TheaterInfo"));
//        out.println(PSPOplanUtilities.writeTheaterInfos(plan.getTheaterInfo()));

        // Force Packages
//        out.println(PSPOplanUtilities.sectionHeader("Force Packages"));
//        out.println(PSPOplanUtilities.writeForcePackages(plan.getForcePackages()));


        // Organizations
        Date c_date = plan.getCday();
        out.println(PSPOplanUtilities.sectionHeader("Organizations"));
        while(org_activities.hasNext()) {
            OrgActivity org = (OrgActivity)org_activities.next();
            if (org.getOrgID().equals(psc.getServerPlugInSupport().getClusterIDAsString())) {
                out.println("<p>" + org.getOrgID() + " " + org.getActivityType() + " " + org.getOpTempo() + 
                            " " + org.getTimeSpan().getStartDate() + 
                            "(C+" + computeDeltaDay(org.getTimeSpan().getStartDate(), c_date) + ") " + 
                            " " + org.getTimeSpan().getEndDate() + 
                            "(C+" + computeDeltaDay(org.getTimeSpan().getEndDate(), c_date) + ") " + 
                            "</p>");
                out.println("<p>[" + org.getGeoLoc().getName() + " " + 
                            org.getGeoLoc().getGeolocCode() + " " + 
                            org.getGeoLoc().getLatitude() + " , " + 
                            org.getGeoLoc().getLongitude() + "]" +
                            "</p>");
            }
            
        }


        // Policies
//        en = PSPOplanUtilities.getPolicies(psc);
//          if (en != null) {
//           out.println(PSPOplanUtilities.sectionHeader("Policies"));
//           out.println(PSPOplanUtilities.writePolicies(en));
//          }
      }

      out.println(PSPOplanUtilities.closeDoc());
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

    private long computeDeltaDay(Date day, Date c_day) 
    {
        return (day.getTime()  - c_day.getTime()) / (1000*60*60*24);
    }

  /*************************************************************************************
   * 
  public void displayOPlanTOC(PrintStream out, PlanServiceContext psc) {

    try {        
      Oplan plan = PSPOplanUtilities.getOplan(psc);
      Enumeration en = null;

      out.println(PSPOplanUtilities.openDoc("Oplan Table Of Contents View"));
      out.println(PSPOplanUtilities.openTOC("Table Of Contents"));

      if (plan != null) {
        out.println(PSPOplanUtilities.writeTOCSection("Filename, ID and Version", "Filename, ID and Version", "thisClusterOplanFrame"));
        out.println(PSPOplanUtilities.writeTOCSection("Operation Info", "Operation Info", "thisClusterOplanFrame"));
        out.println(PSPOplanUtilities.writeTOCSection("CINCs", "CINCs", "thisClusterOplanFrame"));
//        out.println(PSPOplanUtilities.writeProperty("CINC", PSPOplanUtilities.writeCINCs(plan.getCincs())));
        out.println(PSPOplanUtilities.writeTOCSection("Timespan", "Timespan", "thisClusterOplanFrame"));
        out.println(PSPOplanUtilities.writeTOCSection("TheaterInfo", "TheaterInfo", "thisClusterOplanFrame"));
//        out.println(PSPOplanUtilities.writeTheaterInfos(plan.getTheaterInfo()));
        out.println(PSPOplanUtilities.writeTOCSection("Force Packages", "Force Packages", "thisClusterOplanFrame"));
//        out.println(PSPOplanUtilities.writeForcePackages(plan.getForcePackages()));

        out.println(PSPOplanUtilities.writeTOCSection("Organizations", "Organizations", "thisClusterOplanFrame"));
//        out.println(PSPOplanUtilities.writeOrganizations(plan.getOrganizations()));
        out.println(PSPOplanUtilities.writeTOCSection("Policies", "Policies", "thisClusterOplanFrame"));
//       out.println(PSPOplanUtilities.writePolicies(en));
      }

      out.println(PSPOplanUtilities.closeTOC());
      out.println(PSPOplanUtilities.closeDoc());
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
   **/

}
