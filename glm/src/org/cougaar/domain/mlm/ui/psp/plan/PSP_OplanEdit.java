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
import java.net.*;
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

public class PSP_OplanEdit 
    extends PSP_BaseAdapter 
    implements PlanServiceProvider, UISubscriber {

  /** A zero-argument constructor is required for dynamically loaded PSPs,
      required by Class.newInstance()
  **/
  public PSP_OplanEdit() {
    super();
  }

  public PSP_OplanEdit(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

 /* The UISubscriber interface.
     This PSP doesn't care if subscriptions change
     because it treats each request as a new request.
  */
  public void subscriptionChanged(Subscription subscription) {
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }


  private void printHtmlBegin(PrintStream out)
  {
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Oplan Editor</title>");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    out.println("</head>");
    out.println("<body bgcolor=\"#FFFFFF\">");
  }

  private void printHtmlEnd(PrintStream out)
  {
    out.println("</body>");
    out.println("</html>");
  }

  private void printHtmlSelectOption( PrintStream out, String opTempoValue)
  {
    if( opTempoValue != null) {
      out.println("<select name=\"select\">");
      if (opTempoValue.equals("High")) {
        out.println("<option value=\"High\" selected>High</option>");
        out.println("<option value=\"Medium\">Medium</option>");
        out.println("<option value=\"Low\">Low</option>");
      } else if (opTempoValue.equals("Medium")) {
        out.println("<option value=\"High\">High</option>");
        out.println("<option value=\"Medium\" selected>Medium</option>");
        out.println("<option value=\"Low\">Low</option>");
      } else {
        out.println("<option value=\"High\">High</option>");
        out.println("<option value=\"Medium\">Medium</option>");
        out.println("<option value=\"Low\" selected>Low</option>");
      }
      out.println("</select>");
    } else {
      out.println("<td>&nbsp;</td>");
    }
  }

  /**
   * Convert day offsets from dates to Date and back to offsets
   */
  private static Calendar formatter;
  static {
    formatter = Calendar.getInstance();
  }
  private static Date getRelativeDate(Date baseDate, int offsetDays) {
    formatter.setTime(baseDate);
    formatter.add(formatter.DATE, offsetDays);
    return formatter.getTime();
  }
  private static Date getRelativeDate(Date baseDate, String sOffsetDays) {
    return getRelativeDate(baseDate, Integer.parseInt(sOffsetDays));
  }
  private static int getRelativeOffsetDays(Date baseDate, Date offsetDate) {
    try {
      return (int)((offsetDate.getTime() - baseDate.getTime())/
                   (1000*60*60*24));
    } catch (Exception e) {
      return 0;
    }
  }
  /**
   * deprecated...
   */
  private static String getGMTString(Date d) {
    return d.toGMTString();
  }

  /**
   * FIXME!
   * <pre>
   * Only able to modify first orgActivity.
   * Does lots of "new String" allocs for no good reason.
   * Odd postData format -- should allow both postdata and
   *   URL parameter line (ala PSP_PlugInLoader).
   * Calls getTimeSpan(), array index too often.
   * </pre>
   */
  private boolean doPost(
     OrgActivity[] org, Date cDate, String activityType, String postData)
  {
    String txtField;
    String txtField2;
    String select = " ";
    int actIndex = 0;
    // parse the string first
    String[] strHolder = new String[4];
    int index = 0;
    StringTokenizer st = new StringTokenizer(postData,"&");
    System.out.println("Post Data!!! " + postData);
    while (st.hasMoreElements()) {
      strHolder[index] = new String(st.nextToken());
      //System.out.println(st.nextToken() );
      //System.out.println("index="+index+strHolder[index]);
      index++;
    }

    for (int i = 0; i < org.length; i++) {
      if (org[i].getActivityType().equals(activityType))
        actIndex = i;
      break;
    }
    // set Tempo
    if (org[actIndex].getOpTempo() != null) {
      select = strHolder[0].substring("select=".length());
      txtField = new String(strHolder[1].substring("textfield=".length()));
      txtField2 = new String(strHolder[2].substring("textfield2=".length()));
      org[actIndex].setOpTempo(select);
    } else {
      txtField = new String(strHolder[0].substring("textfield=".length()));
      txtField2 = new String(strHolder[1].substring("textfield2=".length()));
    }
    //System.out.println(select+";"+txtField+";"+txtField2);
    Date startDate = getRelativeDate(cDate, txtField);
    Date thruDate = getRelativeDate(cDate, txtField2);
    Date nextStartDate;
/**/
    // leave one day between activities
    nextStartDate = getRelativeDate(thruDate, 1);
/**/
/*
    // leave same spacing between activities as before
    long nextSpacingTime = 
      org[1].getTimeSpan().getStartDate().getTime() -
      org[0].getTimeSpan().getThruDate().getTime();
    if (nextSpacingTime == 0) {
      nextStartDate = thruDate;
    } else {
      nextStartDate = new Date(thruDate.getTime() + nextSpacingTime);
    }
*/
    //if( activityType.equals("Deployment") )
    //{
      if (org.length > 1)
      {
        //if( org[1].getTimeSpan() != null )
        //{
          if (nextStartDate.after(org[1].getTimeSpan().getThruDate()) ||
              startDate.after(org[0].getTimeSpan().getThruDate())) {
            return false;
          }
          org[0].getTimeSpan().setStartDate(startDate);
          org[0].getTimeSpan().setThruDate(thruDate);
          org[1].getTimeSpan().setStartDate(nextStartDate);
        //}
      //else
      //  return false;
      }
      else
      {
        org[0].getTimeSpan().setStartDate(startDate);
        org[0].getTimeSpan().setThruDate(thruDate);
      }
    //}
    //      return false;
    return true;
  }

  /***************************************************************************
   *
   **/

  public void execute(
      PrintStream out,
      HttpInput query_parameters,
      PlanServiceContext psc,
      PlanServiceUtilities psu) throws Exception 
  {
System.out.println("begin oplan edit");
    String URLname = null;
    try {
      int port = psc.getLocalPort();
      String loc; 
      try {
        loc = psc.getLocalAddress().getLocalHost().getHostAddress();
      } catch (Exception e) {
        System.err.println("UNABLE TO FIND HOST!");
        loc = "UNKNOWN";
      }
      String base_url = "http://"+loc+":"+port+"/";
      String clusterID = 
        psc.getServerPlugInSupport().getClusterIDAsString();
      String cluster_url = base_url+"$"+clusterID;
      String path = query_parameters.getGETUrlString();
      if (path.endsWith("/")) {
        path = path.substring(0, path.length()-1);
      }
      URLname = cluster_url+path;
    } catch (Exception nourl) {
      System.err.println("Unable to get PSP's URL: "+nourl);
    }
System.out.println("url: "+URLname);

    // get the oplan and cdate
    Oplan plan = PSPOplanUtilities.getOplan(psc);
    if (plan == null) {
      displayOPlanNotFound(out);
      return;
    }
    Date cDate = plan.getCday();

    try {
      Vector orgID_Info = 
        query_parameters.getParameterTokens("ORGID",'=');
      if ((orgID_Info != null) &&
          (orgID_Info.size() != 0)) {
System.out.println("is orgid");
        // display orgActivity info
        // take parameters
        String orgID = null;
        String activityType = null;
        String activityName = null;
        int n = orgID_Info.size();
        if (n > 0) {
          orgID = (String)orgID_Info.get(0);
          if (n > 1) {
            activityType = (String)orgID_Info.get(1);
            if (n > 2) {
              activityName = (String)orgID_Info.get(2);
            }
          }
        }
        // find matching orgAct in oplan
        OrgActivity showOrg = null;
        Enumeration org_activities = plan.getOrgActivities();
        while (org_activities.hasMoreElements()) {
          OrgActivity org = (OrgActivity)org_activities.nextElement();
          if (((orgID == null) ||
               orgID.equals(org.getOrgID())) && 
              ((activityType == null) || 
               activityType.equals(org.getActivityType())) &&
              ((activityName == null) ||
               (activityName.equals(org.getActivityName())))) {
            showOrg = org;
            break;
          }
        }
        // display it
        if (showOrg != null) {
          displayOrgActivity(out, showOrg, cDate);
        } else {
          displayOrgNotFound(out, orgID, activityType, activityName);
        }
      } else {
        Vector postOrgID_Info = query_parameters.getParameterTokens("POST",'=');
        if ((postOrgID_Info != null) &&
            (postOrgID_Info.size() != 0)) {
System.out.println("is post");
          // take post data
          String postData = query_parameters.getBodyAsString();
          String postOrgID = (String) postOrgID_Info.get(0);
          String postActivityType = (String) postOrgID_Info.get(1);
          // get list of orgActs with matching orgID
          Vector orgActivityVector = new Vector();
          Enumeration org_activities = plan.getOrgActivities();
          while (org_activities.hasMoreElements()) {
            OrgActivity org = (OrgActivity)org_activities.nextElement();
            if (org.getOrgID().equals(postOrgID))
              orgActivityVector.addElement(org);
          }
          // make array -- dumb?
          OrgActivity[] orgActivityArray =
            new OrgActivity[orgActivityVector.size()];
          orgActivityVector.copyInto(orgActivityArray);
          // post 
          if (doPost(orgActivityArray, cDate,
                     postActivityType, postData)) {
            psc.getServerPlugInSupport().publishChangeForSubscriber(plan);
            displayPostSuccess(out, URLname);
          } else {
            displayPostFailure(out, URLname);
          }
        } else {
System.out.println("is usage");
          displayOPlanEdit(out, psc, URLname, plan, cDate);
        }
      }
    } catch (Exception topLevelException) {
      displayExceptionFailure(out, topLevelException);
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

  public void displayOPlanNotFound(PrintStream out)
  {
    printHtmlBegin(out);
    out.println("<font size=+1>OPlan not found</font>");
    printHtmlEnd(out);
  }

  public void displayExceptionFailure(PrintStream out, Exception e)
  {
    printHtmlBegin(out);
    out.println("<font size=+1>Failed due to Exception</font><p>");
    out.println(e);
    out.println("<p><pre>");
    e.printStackTrace(out);
    out.println("</pre>");
    printHtmlEnd(out);
  }

  public void displayPostSuccess(PrintStream out, String URLname) {
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Untitled Document</title>");
    out.print("<meta http-equiv=\"refresh\" content=\" 1;URL=");
    out.print(URLname);
    out.print("\">");
    out.println("</head>");
    out.println("<body bgcolor=\"#FFFFFF\">");
    out.println("<H3><CENTER>Submit Successful !!</CENTER></H3>");
    printHtmlEnd(out);
  }

  public void displayPostFailure(PrintStream out, String URLname) {
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Untitled Document</title>");
    out.print("<meta http-equiv=\"refresh\" content=\" 2;URL=");
    out.print(URLname);
    out.println("\">");
    out.println("</head>");
    out.println("<body bgcolor=\"#FFFFFF\">");
    out.println("<H3><CENTER> !!ERROR !! Start Date is bigger than End Date</CENTER></H3>");
    printHtmlEnd(out);
  }

  public void displayOPlanEdit(
      PrintStream out, PlanServiceContext psc, String URLname,
      Oplan plan, Date cDate) {
    String str1 = new String("Deployment");
    String str2 = new String("Employment-Offensive");
    String str3 = new String("Employment-Defensive");

    try {
      //Enumeration en = null
      //Enumeration org_activities = 
      //  PSPOplanUtilities.getOrgActivities(this, psc);

      Enumeration org_activities = plan.getOrgActivities();
      String previousOrgID = " ";
      OrgActivity org = (OrgActivity)org_activities.nextElement();
      int index = 0;
      printHtmlBegin(out);

      out.println("<p align=\"center\"><b><font size=\"5\">OPLAN EDITOR</font></b></p>");
      out.println("<table width=\"100%\" border=\"1\">");
      out.println("<tr>");
      out.println("<td><b>Organization</b></td>");
      out.println("<td><b>Deployment</b></td>");
      out.println("<td><b>Employment-Defensive</b></td>");
      out.println("<td><b>Employment-Offensive</b></td>");
      out.println("<td><b>Employment Standdown</b></td>");
      out.println("</tr>");

      while (org_activities.hasMoreElements()) {
        //System.out.println("Debug: "+ org.getOrgID() );
        //System.out.println("Debug: "+ org.getActivityType() );

        out.println("<tr>");
        out.println("<td>" + org.getOrgID() +"</td>");
        previousOrgID = org.getOrgID();
        while (org.getOrgID().equals(previousOrgID)) {
          //out.println("<td><a href=" +
          //  "\""+URLname+"?ORGID="+
          //  org.getOrgID() + "\">" + org.getOrgID() + "</a></td>");
          if (org.getActivityType().startsWith("Cinc")) {
            out.println("<td> &nbsp;</td>");
          } else if (org.getActivityType().equals("Deployment") &&
                     (org.getActivityName() != null)) {
            out.println("<td><a href="+
              "\""+URLname+"?ORGID="+ 
              org.getOrgID() +"="+
              org.getActivityType()+"="+
              org.getActivityName()+ "\">" +"C+" +
              getRelativeOffsetDays(cDate, org.getTimeSpan().getStartDate()) + 
              " To C+" + 
              getRelativeOffsetDays(cDate, org.getTimeSpan().getThruDate()) +
              "</a></td>");
          } else if (org.getActivityType().equals("Deployment")) {
            out.println("<td><a href=" +
              "\""+URLname+"?ORGID="+ 
              org.getOrgID() +"="+
              org.getActivityType()+ "\">" +"C+" +
              getRelativeOffsetDays(cDate, org.getTimeSpan().getStartDate()) +
              " To C+" + 
              getRelativeOffsetDays(cDate, org.getTimeSpan().getThruDate()) + 
              "</a></td>");
          } else {
            out.println("<td>" +"C+" +
              getRelativeOffsetDays(cDate, org.getTimeSpan().getStartDate()) +
              " To C+" + 
              getRelativeOffsetDays(cDate, org.getTimeSpan().getThruDate()) +
              "</td>");
          }
          org = (OrgActivity)org_activities.nextElement();
          index++;
        }
        // fill up remaining spaces
        for (int i = index; i <4; i++) {
          out.println("<td> &nbsp;</td>");
        }
        index=0;
        out.println("</tr>");
      }

      out.println("</table>");
      printHtmlEnd(out);
      out.flush();
    } catch (Exception e) {
      out.println("</table>");
      printHtmlEnd(out);
      System.out.println(e.getMessage());
    }
  }

  public void displayOrgNotFound(
      PrintStream out, String orgID, String activityType, String activityName)
  {
    printHtmlBegin(out);
    out.println("<font size=+1>Organization not found:<p><ul>");
    if (orgID != null) {
      out.print("<li><b>OrgID</b>: ");
      out.print(orgID);
      out.println("</li>");
    }
    if (activityType != null) {
      out.print("<li><b>ActivityType</b>: ");
      out.print(activityType);
      out.println("</li>");
    }
    if (activityName != null) {
      out.print("<li><b>ActivityName</b>: ");
      out.print(activityName);
      out.println("</li>");
    }
    out.println("</ul>");
    printHtmlEnd(out);
  }

  public void displayOrgActivity(
      PrintStream out, OrgActivity org, Date cDate)
  {
    printHtmlBegin(out);
    out.println("<p align=\"center\"><b><font size=\"5\">"+
      org.getOrgID() +"</font></b></p>");
    out.println("<form method=\"post\" action=\"OPLAN_EDIT.PSP?POST="+
      org.getOrgID()+"="+org.getActivityType()+"\">");
    out.println("<table width=\"90%\" border=\"1\">");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\"><b>Activity Type</b></td>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;" +
      org.getActivityType() + "</td>");
    if (org.getActivityName() == null) {
      out.println("<td colspan=\"2\">&nbsp;" + 
        "&nbsp" + "</td>");
    } else {
      out.println("<td colspan=\"2\">&nbsp;" +
        org.getActivityName()+ "</td>");
    }
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\"><b>OpTempo</b></td>");
    out.println("<td colspan=\"2\">");
 
    printHtmlSelectOption(out, org.getOpTempo() );
 
    out.println("</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"4\">&nbsp;</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"4\"><b>TimeSpan</b></td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    out.println("<td><b>Start Time</b></td>");
    out.println("<td><b>Thru Time</b></td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td>&nbsp; </td>");
    out.println("<td>");
    out.println("<div align=\"right\"><b>C-Time</b></div>");
    out.println("</td>");
    out.println("<td>C+ <input type=\"text\" name=\"textfield\" value=\"" + 
      getRelativeOffsetDays(cDate, org.getTimeSpan().getStartDate()) +
      "\" size=\"6\"> </td>");
    out.println("<td>C+ <input type=\"text\" name=\"textfield2\" value=\""+ 
      getRelativeOffsetDays(cDate, org.getTimeSpan().getThruDate()) +
      "\" size=\"6\"></td>");
 
    out.println("<tr>");
    out.println("<td>&nbsp;</td>");
    out.println("<td>");
    out.println("<div align=\"right\"><b>Absolute Time</b></div>");
    out.println("</td>");
    out.println("<td>" +
      getGMTString(org.getTimeSpan().getStartDate()) + "</td>");
    out.println("<td>" +
      getGMTString(org.getTimeSpan().getThruDate()) + "</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    out.println("<td>&nbsp;</td>");
    out.println("<td>&nbsp;</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\"><b>Geographic Location</b></td>");
    out.println("<td>&nbsp;</td>");
    out.println("<td>&nbsp;</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    out.println("<td><b>Name</b></td>");
    out.println("<td>" + org.getGeoLoc().getName() +"</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    out.println("<td><b>Code</b></td>");
    out.println("<td>"+ org.getGeoLoc().getGeolocCode() +"</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    out.println("<td><b>Latitude</b></td>");
    out.println("<td>"+org.getGeoLoc().getLatitude().getDegrees()+"</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    out.println("<td><b>Longitude</b></td>");
    out.println("<td>"+org.getGeoLoc().getLongitude().getDegrees()+"</td>");
    out.println("</tr>");
 
    out.println("<tr>");
    out.println("<td colspan=\"4\">");
    out.println("<div align=\"center\">");
    out.println("<input type=\"submit\" name=\"Submit\" value=\"Submit Changes\">");
    out.println("</div>");
    out.println("</td>");
    out.println("</tr>");
    out.println("</table>");
    out.println("</form>");
    out.println("<p>&nbsp;</p>");
 
    printHtmlEnd(out);
  }
 
} // end of class
