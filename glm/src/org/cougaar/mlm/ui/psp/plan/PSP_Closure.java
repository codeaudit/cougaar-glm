/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import java.io.PrintStream;
import java.net.*;
import java.util.*;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.planning.ldm.policy.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.glm.ldm.*;import org.cougaar.glm.ldm.*;import org.cougaar.glm.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.policy.*;

public class PSP_Closure extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {

  private Enumeration org_activities = null;
  private String URLname;

  private Date cDate=null;

  /** A zero-argument constructor is required for dynamically loaded PSPs,
    required by Class.newInstance()
   **/
  public PSP_Closure() {
    super();
    try
    {
      URLname =  java.net.InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e)
    {
      System.out.println("Catch UnknownHostException at PSP_Closure");
    }

  }

  public PSP_Closure( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
    try
    {
      URLname =  java.net.InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e)
    {
      System.out.println("Catch UnknownHostException at PSP_Closure");
    }

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
    out.println("<title>Closure Plan Editor</title>");
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
    if( opTempoValue != null)
    {
      out.println("<select name=\"select\">");
      if( opTempoValue.equals("High") )
      {
        out.println("<option value=\"High\" selected>High</option>");
        out.println("<option value=\"Medium\">Medium</option>");
        out.println("<option value=\"Low\">Low</option>");
      }
      else if( opTempoValue.equals("Medium") )
      {
        out.println("<option value=\"High\">High</option>");
        out.println("<option value=\"Medium\"selected>Medium</option>");
        out.println("<option value=\"Low\">Low</option>");
      }

      else
      {
        out.println("<option value=\"High\">High</option>");
        out.println("<option value=\"Medium\">Medium</option>");
        out.println("<option value=\"Low\"selected>Low</option>");
      }
      out.println("</select>");
    }
    else
      out.println("<td>&nbsp;</td>");
  }

  private OrgActivity getOrgActivityWith(String orgID,String activityType,PlanServiceContext psc)
  {
    Collection orgActivities = PSPOplanUtilities.getOrgActivities(psc, orgID
);
    for (Iterator iterator = orgActivities.iterator();
         iterator.hasNext();) {
      OrgActivity org = (OrgActivity)iterator.next();
      if (org.getActivityType().equals(activityType))
        return org;
    }
    return null;
  }

  //
  // sorting
  //
  private static void bubbleSort(OrgActivity[] org, int size,String sortType)
  {
    if(sortType == null)
    {
      for(int i=1; i<size; i++)
        for(int k=size-1;k>=i;k--)
        {
          if(org[k-1].getOrgID().compareTo(org[k].getOrgID()) > 0)
          {
            OrgActivity temp = org[k-1];
            org[k-1] = org[k];
            org[k] = temp;
          }
        }
    }
    else
    {
      for(int i=1; i<size; i++)
        for(int k=size-1;k>=i;k--)
        {
          if( org[k-1].getTimeSpan().getEndDate().getTime() > org[k].getTimeSpan().getEndDate().getTime() )
          {
            OrgActivity temp = org[k-1];
            org[k-1] = org[k];
            org[k] = temp;
          }
          else if(org[k-1].getTimeSpan().getEndDate().getTime()==org[k].getTimeSpan().getEndDate().getTime() )
          {
            if(org[k-1].getOrgID().compareTo(org[k].getOrgID()) > 0)
            {
              OrgActivity temp = org[k-1];
              org[k-1] = org[k];
              org[k] = temp;
            }
          }

        }
    }
  }

  private boolean doPost(OrgActivity[] org, String postData)
  {
    String txtField;
    String select = " ";

    Calendar formatter = Calendar.getInstance();

    //TimeSpan[] ts= new TimeSpan[2];
    // parse the string first
    String[] strHolder = new String[3];
    int index = 0;
    StringTokenizer st = new StringTokenizer(postData,"&");
    while(st.hasMoreElements() )
    {
      strHolder[index]=new String( st.nextToken() );
      index++;
    }
    txtField = new String(strHolder[0].substring("textfield=".length() ) );

    int endDelta = Integer.parseInt(txtField);
    formatter.setTime(cDate);
    formatter.add(formatter.DATE, endDelta);
    long endTime = formatter.getTime().getTime();

    if(org.length > 1)
    {
      if(org[1].getTimeSpan() != null)
      {
        if(  (endTime + 1) > (org[1].getTimeSpan().getEndDate().getTime() ) )
          return false;

        //TimeSpan depTimeSpan = org[0].getTimeSpan();
        //TimeSpan empTimeSpan = org[1].getTimeSpan();
        // org[0].getTimeSpan().setStartDelta(startTime);
        org[0].getTimeSpan().setEndDate(new Date(endTime));
        org[1].getTimeSpan().setStartDate(new Date(endTime+1));
      }
      return true;
    }
    else
    {
      org[0].getTimeSpan().setEndDate(new Date(endTime));
      return true;
    }
    // set opTempo & timeSpan
    //if( org.getTimeSpan() != null)
    //{
    //      TimeSpan ts = org.getTimeSpan();
    //      ts.setEndDelta(Integer.parseInt(txtField) );
    //
    //      org.setTimeSpan(ts);
    //}
  }



  /*************************************************************************************
   *
   **/

  public void execute( PrintStream out,
      HttpInput query_parameters,
      PlanServiceContext psc,
      PlanServiceUtilities psu ) throws Exception    {

    Vector orgID_Info =  query_parameters.getParameterTokens("ORGID",'=');
    Vector postOrgID_Info = query_parameters.getParameterTokens("POST",'=');

    String orgID = new String("");
    if(orgID_Info == null && postOrgID_Info == null)
    {
      String sortType = (String)query_parameters.getFirstParameterToken("SORT",'=');
      displayClosurePlan(out,psc,sortType);
    }
    else if( orgID_Info != null )
    {
      if(orgID_Info.size() > 2)
      {
        //do search for OrgActivity Object
        orgID = (String) orgID_Info.get(0);
        String activityType = (String) orgID_Info.get(1);
        String activityName = (String) orgID_Info.get(2);
        OrgActivity org = getOrgActivityWith(orgID, activityType ,psc);
        displayOrgActivityWithName(out,org);
      }
      else
      {
        orgID = (String) orgID_Info.get(0);
        String activityType = (String) orgID_Info.get(1);
        OrgActivity org = getOrgActivityWith(orgID, activityType, psc);
        displayOrgActivity(out,org);
      }


    }
    else  // do Post
    {
      String postData = query_parameters.getBodyAsString();
      String postOrgID = (String) postOrgID_Info.get(0);
      String postActivityType = (String) postOrgID_Info.get(1);
      Oplan plan = PSPOplanUtilities.getOplan(psc);

      cDate = plan.getCday();

      Collection orgActivities = 
        PSPOplanUtilities.getOrgActivities(psc, postOrgID);


      OrgActivity[] orgActivityArray = 
        (OrgActivity []) orgActivities.toArray();
      System.out.println("DEBUG:!!!!"+orgActivities.size());

      PluginDelegate delegate = psc.getServerPluginSupport().getDirectDelegate();
      delegate.openTransaction();

      if (doPost(orgActivityArray,postData) == true)
      {
        delegate.publishChange(plan);
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Untitled Document</title>");
        out.println("<meta http-equiv=\"refresh\" content=\" 1;URL=http://"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP\">");
        out.println("</head>");
        out.println("<body bgcolor=\"#FFFFFF\">");
        out.println("<H3><CENTER>Submit Successful !!</CENTER></H3>");
        printHtmlEnd(out);
      }
      else
      {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Untitled Document</title>");
        out.println("<meta http-equiv=\"refresh\" content=\" 2;URL=http://"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP\">");
        out.println("</head>");
        out.println("<body bgcolor=\"#FFFFFF\">");

        out.println("<h3><center>You have made an invalid change in which the start time is later than the ending time.</center></h3> \n");
        out.println("<h3><center>Please redo it</center></h3>");
        printHtmlEnd(out);
      }
      delegate.closeTransaction();
    }
  }



  /**1
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



  public void displayClosurePlan(PrintStream out, PlanServiceContext psc,String sortType) {

    try {
      Vector orgVector = new Vector();
      for (Iterator iterator = 
             PSPOplanUtilities.getOrgActivities(psc).iterator();
           iterator.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) iterator.next();
        if (orgActivity.getActivityType().equals("Deployment")) {
          orgVector.addElement(orgActivity);
        }
      }

      OrgActivity[] orgArray = new OrgActivity[orgVector.size()];
      orgVector.copyInto(orgArray);

      bubbleSort(orgArray,orgArray.length,sortType);

      printHtmlBegin(out);

      out.println("<p align=\"center\"><b><font size=\"5\">CLOSURE PLAN</font></b></p>");
      out.println("<center>");
      out.println("<table cols=\"2\" width=\"60%\" border=\"1\">");
      out.println("<tr>");
      out.println("<td><b>Organization&nbsp;&nbsp;&nbsp;</b><a href=" +"\"http://"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP\">"+"Click to Sort</a></td>");
      out.println("<td><b>RDD&nbsp;&nbsp;&nbsp;</b><a href=" +"\"http://"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP?SORT=RDD\">"+"Click to Sort</a></td>");
      out.println("</tr>");

      for (int i  = 0;i <orgArray.length; i++ )
      {
        //if(orgArray[i].getActivityType().equals("Deployment") )
        //{
        out.println("<tr>");
        out.println("<td>" + orgArray[i].getOrgID() +"</td>");
        if( orgArray[i].getActivityName() != null  )
          out.println("<td><a href=" +"\"http://"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP?ORGID="+orgArray[i].getOrgID()+"="+orgArray[i].getActivityType()+"="+orgArray[i].getActivityName()+ "\">"+" C+"+orgArray[i].getTimeSpan().getEndDate().getTime()+"</a></td>");
        else
          out.println("<td><a href=" +"\"http://"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP?ORGID="+orgArray[i].getOrgID()+"="+orgArray[i].getActivityType()+"\">" +" C+"+orgArray[i].getTimeSpan().getEndDate().getTime()+"</a></td>");
        out.println("</tr>");
        //}
      }
      out.println("</table>");
      out.println("</center>");
      printHtmlEnd(out);
      out.flush();

    }
    catch (Exception e) {
      out.println("</table>");
      printHtmlEnd(out);
      System.out.println(e.getMessage());
    }

  }

  public void displayOrgActivity(PrintStream out, OrgActivity org)
  {
    printHtmlBegin(out);
    out.println("<p align=\"center\"><b><font size=\"5\">"+ org.getOrgID() +"</font></b></p>");
    //out.println("<form method=\"post\" action=\"http:\\"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP?POST="+org.getOrgID()+"\">");
    out.println("<form method=\"post\" action=\"CLOSURE_PLAN.PSP?POST="+org.getOrgID()+"="+org.getActivityType()+"\">");
    out.println("<table width=\"90%\" border=\"1\">");

    out.println("<tr>");
    out.println("<td>&nbsp;</td>");
    out.println("<td><center><b>RDD</b></center></td>");
    //out.println("<td>&nbsp;</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>C-Time</b></td>");
    //out.println("<div align=\"right\"><b>C-Time</b></div>");
    out.println("<td>C+ <input type=\"text\" name=\"textfield\" value=\""+ org.getTimeSpan().getEndDate().getTime() +"\" size=\"6\"></td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>Date</b></td>");


    out.println("<td>" +org.getTimeSpan().getEndDate().toGMTString() + "</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    //out.println("<td>&nbsp;</td>");
    //out.println("<td>&nbsp;</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td colspan=\"2\"><b>Geographic Location</b></td>");
    //out.println("<td>&nbsp;</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>Name</b></td>");
    out.println("<td>" +org.getGeoLoc().getName()+"</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>Code</b></td>");
    out.println("<td>"+ org.getGeoLoc().getGeolocCode() +"</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>Latitude</b></td>");
    out.println("<td>"+org.getGeoLoc().getLatitude().getDegrees()+"</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>Longitude</b></td>");
    out.println("<td>"+org.getGeoLoc().getLongitude().getDegrees()+"</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td colspan=\"2\">");
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

  public void displayOrgActivityWithName(PrintStream out, OrgActivity org)
  {
    printHtmlBegin(out);
    out.println("<p align=\"center\"><b><font size=\"5\">"+ org.getOrgID() +"</font></b></p>");
    //out.println("<form method=\"post\" action=\"http:\\"+URLname+":5555/alpine/demo/CLOSURE_PLAN.PSP?POST="+org.getOrgID()+"\">");
    out.println("<form method=\"post\" action=\"CLOSURE_PLAN.PSP?POST="+org.getOrgID()+"="+org.getActivityType()+"\">");
    out.println("<table width=\"90%\" border=\"1\">");

    out.println("<tr>");
    out.println("<td>&nbsp;</td>");
    out.println("<td><center><b>RDD</b></center></td>");
    //out.println("<td>&nbsp;</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>C-Time</b></td>");
    //out.println("<div align=\"right\"><b>C-Time</b></div>");
    out.println("<td>C+ <input type=\"text\" name=\"textfield\" value=\""+ org.getTimeSpan().getEndDate().getTime() +"\" size=\"6\"></td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td><b>Date</b></td>");


    out.println("<td>" +org.getTimeSpan().getEndDate().toGMTString() + "</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td colspan=\"2\">&nbsp;</td>");
    //out.println("<td>&nbsp;</td>");
    //out.println("<td>&nbsp;</td>");
    out.println("</tr>");

    out.println("<tr>");
    out.println("<td colspan=\"2\">");
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
