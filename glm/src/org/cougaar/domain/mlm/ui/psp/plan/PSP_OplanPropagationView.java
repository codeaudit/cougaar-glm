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

import java.io.PrintStream;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.policy.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.asset.*;
import org.cougaar.domain.glm.oplan.*;
import org.cougaar.domain.glm.plan.*;
import org.cougaar.domain.glm.policy.*;

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

public class PSP_OplanPropagationView extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
      
  /** A zero-argument constructor is required for dynamically loaded PSPs,
      required by Class.newInstance()
  **/
  public PSP_OplanPropagationView() {
    super();
  }

        /*************************************************************************************
         * 
         **/
  public PSP_OplanPropagationView( String pkg, String id ) throws RuntimePSPException {
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
                        default:        
                                out.println("<HTML>");
                                out.println("<HEAD>");
                                out.println("<TITLE>");
                                out.println("Cluster OPlan Propagation View");
                                out.println("</TITLE>");
                                out.println("</HEAD>");
                                out.println("<FRAMESET cols=\"20%,80%\">");
                                out.println("<FRAME src=\"/alpine/demo/OPLAN_PROP.PSP?MODE=2 \" name=\"clustersFrame\">");
                                out.println("<FRAME src=\"/alpine/demo/OPLAN_PROP.PSP?MODE=1\" name=\"thisClusterOplanFrame\">");
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
      out.println("<A HREF=\"/$"+n+"/alpine/demo/OPLAN_PROP.PSP?MODE=1\" TARGET=thisClusterOplanFrame></A></FONT>");
      out.println("</td><td>");
      out.println("<A HREF=\"/$"+n+"/alpine/demo/OPLAN_PROP.PSP?MODE=1\" TARGET=thisClusterOplanFrame>" + n + "</A></FONT>");
      out.println("</td></tr>");
    }
    
    out.println("</TABLE></DIV>");
    out.println(PSPOplanUtilities.closeDoc());
  }


  /** taskOID can be null if we are rendering this form for the first time **/
  public void displayOPlan(PrintStream out, PlanServiceContext psc) {

    try {        
      Collection en = null;
      out.println(PSPOplanUtilities.openDoc("Oplan Propagation View"));

      // Policies
      en = PSPOplanUtilities.getPolicies(psc);
      if (!en.isEmpty()) {
       out.println(PSPOplanUtilities.sectionHeader("Policies"));
       out.println(PSPOplanUtilities.writePolicies(en.iterator()));
      } 

      // OrgActivities
      en = PSPOplanUtilities.getOrgActivities(psc);
      if (!en.isEmpty()) {
       out.println(PSPOplanUtilities.sectionHeader("Org Activity"));
       out.println(PSPOplanUtilities.writeOrgActivities(en.iterator()));
      } 

      // OrgRelations
      en = PSPOplanUtilities.getOrgRelations(psc);
      if (!en.isEmpty()) {
       out.println(PSPOplanUtilities.sectionHeader("Org Relation"));
       out.println(PSPOplanUtilities.writeOrgRelations(en.iterator()));
      } 

      // TheaterInfos
//      en = PSPOplanUtilities.getTheaterInfos(psc);
//      if (!en.isEmpty()) {
//       out.println(PSPOplanUtilities.sectionHeader("Theater Info"));
//       out.println(PSPOplanUtilities.writeTheaterInfos(en.iterator()));
//      } 

      // ForcePackages
      en = PSPOplanUtilities.getForcePackages(psc);
      if (!en.isEmpty()) {
       out.println(PSPOplanUtilities.sectionHeader("Force Package"));
       out.println(PSPOplanUtilities.writeForcePackages(en.iterator()));
      } 

      out.println(PSPOplanUtilities.closeDoc());
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

}
