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
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;

public class PSP_ClusterRelationships extends PSP_BaseAdapter
                       implements PlanServiceProvider, UISubscriber
{
  public static final boolean DEBUG = false;

  /** 
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   **/
  public PSP_ClusterRelationships() 
  {
    super();
  }

  public PSP_ClusterRelationships(
    String pkg, String id) throws RuntimePSPException
  {
    setResourceLocation(pkg, id);
  }

  public boolean test(
    HttpInput query_parameters, PlanServiceContext sc)
  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  private static UnaryPredicate getSelfPred(){
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          //System.out.println("ORG PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
          return ((Organization)o).isSelf();
        }
        return false;
      }
    };
  }

  /**
   *  Operates in 2 modes.
   *        Mode 1.   "?MODE=1"  Return relationships from all other Clusters as
   *                             well as yourself.   As HTML or XML.  A PSP
   *                             evoked in this mode doesn't actually generate
   *                             response, it will "recurse" upon all white page
   *                             clusters (redirect query) (including itself)
   *                             and evoke each PSP in MODE=2..
   *
   *        Mode 2.   "?MODE=2" Return your relationships only. As HTML or XML.
   *
   *       ?SUPPRESS  = Suppress header and footer (HTML)
   *
   *       ?HTML = return results as Expanded HTML (see ?UNEXPANDED_HTML)
   *       ?UNEXPANDED_HTML  =  Don't provide expanded table representation, just links.
   *                       Viewer can click on link.
   *                       Identical to ?HTML if Mode == 2.
   **/
  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception
  {
    MyPSPState myState = new MyPSPState(this, query_parameters, psc);
    myState.configure(query_parameters);

    //
    // implicitly htmlFlag is turned on if unexpandedHtmlFlag is turned on
    // unexpanded = special case 
    //
    if (myState.unexpandedHtmlFlag) {
      myState.htmlFlag = true;
    }

    if (myState.mode == 1) {
      // ASK CHILDREN (case MODE == 1 )
      getChildrenRelationships(out, myState);
    } else if (myState.mode == 2) {
       //
       // ASK MYSELF (ONLY ANSWER DIRECT QUERIES -- CASE OF WHEN ALL CLUSTERS
       // ASKED BY SOMEONE OF THIS PSP WILL BE HANDLED BY QUERY-REDIRECT...
       // IE. a recursive call in MODE 2.  This is done out of simplicity.
       //
       getMyRelationships(out, myState);
    } else {
      getUsage(out, myState);
    }
  }   

  /** USAGE **/
  private void getUsage(PrintStream out, MyPSPState myState)
  {
    out.println(
     "<HTML><HEAD><TITLE>Cluster Relationships Usage</TITLE></HEAD><BODY>");
    out.println(
     "<H2><CENTER>Cluster Relationships Usage</CENTER></H2><P>");
    out.print("<FORM METHOD=\"POST\" ACTION=\"");
    out.print(myState.cluster_psp_url);
    out.print("?POST");
    out.println("\">");
    out.println(
      "Show cluster relations for:<p>");
    out.print(
      "&nbsp;&nbsp;<INPUT TYPE=\"radio\" NAME=\"mode\" VALUE=\"1\" CHECKED>");
    out.println("&nbsp;All related clusters<p>");
    out.print(
      "&nbsp;&nbsp;<INPUT TYPE=\"radio\" NAME=\"mode\" VALUE=\"2\">");
    out.print("&nbsp;Just ");
    out.print(myState.clusterID);
    out.println("<P>");
    out.println("</INPUT>");
    out.println(
      "<INPUT TYPE=\"hidden\" NAME=\"html\" VALUE=\"true\">");
    out.println(
      "<INPUT TYPE=\"hidden\" NAME=\"unexpand_html\" VALUE=\"false\">");
    out.println(
      "<INPUT TYPE=\"hidden\" NAME=\"suppress\" VALUE=\"false\">");
    out.println(
      "<INPUT TYPE=\"submit\" NAME=\"Display PSP\">");
    out.println("</FORM></BODY></HTML>");
  }

  /** Mode 2 **/
  private void getMyRelationships(PrintStream out, MyPSPState myState)
  {
    if (myState.htmlFlag) {
      if (!myState.suppressFlag) {
        out.println("<HTML><BODY>");
      }

      out.println(
        "<P>Cluster relationships for <B>"+
        myState.clusterID+
        "</B></P>");
      out.println(
        "<TABLE align=center border=1 cellPadding=1 cellSpacing=1");
      out.println(
        "width=75% bordercolordark=#660000 bordercolorlight=#cc9966>");
    }

    if (myState.htmlFlag) {
      out.println("<TR>");
      out.println("<TD> <FONT color=mediumblue ><B>Organization</FONT></B> </TD>");
      out.println("<TD> <FONT color=mediumblue ><B>Assigned Organization </FONT></B></TD>");   
      out.println("<TD> <FONT color=mediumblue ><B>Role </FONT></B></TD>");
      out.println("</TR>");
    }

    // ASK MYSELF
    Collection col =
      myState.psc.getServerPlugInSupport().queryForSubscriber(
          getSelfPred());
    boolean empty = (col.size() == 0);
    if (!empty) {
      Iterator iter = col.iterator();
      do {
        Asset asst = (Asset)iter.next();
        Organization org = (Organization)asst;
        String orgName = org.getItemIdentificationPG().getNomenclature();

        if (myState.htmlFlag) {
          RelationshipSchedule schedule = org.getRelationshipSchedule();

          for (Iterator schedIter = schedule.iterator();
               schedIter.hasNext();) {
            Relationship relationship = (Relationship)schedIter.next();

            // We already know it's the SELF org
            if ((relationship.getRoleA().equals(Constants.Role.SELF)) ||
                (relationship.getRoleB().equals(Constants.Role.SELF))) {
              continue;
            }

            Object otherObject = schedule.getOther(relationship);
            String other = 
              (otherObject instanceof Asset) ?
              ((Asset)otherObject).getItemIdentificationPG().getNomenclature() :
              otherObject.toString();

            out.println("<TR><TD>"+
                        orgName+
                        "</TD><TD>"+
                        other+
                        "</TD><TD>"+
                        schedule.getOtherRole(relationship)+
                        "</TD></TR>");
          }
        }
      } while (iter.hasNext());
    }

    if (myState.htmlFlag) {
      out.println("</TABLE>"); 
    }
    if (empty && myState.htmlFlag) {
      out.println("<CENTER><FONT color=mediumblue >");
      out.println("No Organizations found...");
      out.println("...try again</FONT></CENTER>");
    }

    if (myState.htmlFlag && !myState.suppressFlag) {
      out.println("</BODY></HTML>"); 
    }
  }

  /** Mode 1 **/
  private void getChildrenRelationships(
      PrintStream out,
      MyPSPState myState)
  {
    Vector urls = new Vector();
    Vector names = new Vector();
    myState.psc.getAllURLsAndNames(urls, names);

    if (myState.htmlFlag) {
      if (!myState.suppressFlag) {
        out.println("<HTML><BODY>"); 
      }
      if (myState.unexpandedHtmlFlag) {
        out.println(
          "<P>The following are the Clusters in this society.  Select on");
        out.println(" Cluster to drill-down and inspect Cluster");
        out.println(" relationships. Note that all requests are proxied by");
        out.println(" the LPS to which you are conversing <B>(");
        out.println(myState.clusterID);
        out.println(").</B></P>");
        out.println(
          "<TABLE align=center border=1 cellPadding=1 cellSpacing=1");
        out.println(
          "width=75% bordercolordark=#660000 bordercolorlight=#cc9966>");
      }
    }

    int sz = urls.size();
    for (int i = 0; i < sz; i++) {
      String u = (String)urls.elementAt(i);
      String n = (String)names.elementAt(i);

      if (DEBUG) {
        System.out.println(
          "Attempting to OPEN PSP_ClusterRelationship Child URL("+ u +")");
      }
      try {
        // EXPANDED HTML
        if (!myState.unexpandedHtmlFlag) {

          // yuck -- this PSP calling other PSPs!
          //
          // would rather have PSPs come in two types:
          //   1) logplan access with no outside connections
          //   2) no logplan access with outside connections (i.e. PSPs)
          // i.e. this PSP split into two PSPs, a (1) and a (2).

          //
          // this.getPath() => resource path of this PSP...
          //
          URL myURL = new URL(u+myState.psp_path+"?MODE=2?SUPPRESS?HTML");

          if (DEBUG) {
            System.out.println(
              "[PSP_ClusterRelationships] child URL: " + myURL.toString());
          }
          URLConnection myConnection = myURL.openConnection();
          if (DEBUG) {
            System.out.println(
              "opened query connection = "+ myConnection.toString());
          }

          InputStream is = myConnection.getInputStream();
          BufferedInputStream bis = new BufferedInputStream(is);
          byte buf[] = new byte[512];
          int sz2;
          while ((sz2 = bis.read(buf,0,512)) != -1) {
            out.write(buf,0,sz2);
          }
        } else {
          // UNEXPANDED HTML
          out.println("<TR>");
          //
          // Request will come back thru this LPS and get redirected using "$"
          //
          out.println(
            "<TD><A HREF=\"/$"+n+myState.psp_path+"?MODE=2?HTML\""+
            ">" + n + "</A></TD>");
          out.println("</TR>");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } // for()

    if (myState.htmlFlag) {
      if (myState.unexpandedHtmlFlag) {
         out.println("</TABLE>");  
      }
      if (!myState.suppressFlag) {
        out.println("</BODY></HTML>"); 
      }
    }
  }

  // private Vector myVectorOfTasks = new Vector();
  public void subscriptionChanged(Subscription subscription) {
  /**
    synchronized(myVectorOfTasks) {
      Enumeration e = ((IncrementalSubscription)subscription).getAddedList();
      while (e.hasMoreElements()) {
        Object obj = e.nextElement();
        myVectorOfTasks.addElement(obj);
      }
    }
   **/
  }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/
  public boolean returnsXML() {
    return false;
  }

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

  /** holds PSP state (i.e. url flags for MODE, etc) **/
  protected static class MyPSPState extends PSPState {

    /** my additional fields **/
    public int mode;
    public boolean htmlFlag;
    public boolean suppressFlag;
    public boolean unexpandedHtmlFlag;

    /** constructor **/
    public MyPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
    }

    /** use a query parameter to set a field **/
    public void setParam(String name, String value) {
      //super.setParam(name, value);
      if (name.equalsIgnoreCase("html")) {
        htmlFlag =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("unexpanded_html")) {
        unexpandedHtmlFlag = 
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("suppress")) {
        suppressFlag = 
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("mode")) {
        try {
          mode = Integer.parseInt(value);
        } catch (Exception e) {
          mode = 0;
        }
      }
    }
  }

}

