/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.alert;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.plan.Alert;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

/**
  * This is a PSP which generates random Alert messages
  * for a demo/illustration java/java-script browser alert application.
  *
  **/

public class PSP_HTMLAlerts extends PSP_BaseAdapter
                       implements PlanServiceProvider, UISubscriber
{
    /** A zero-argument constructor is required for dynamically loaded PSPs,
        required by Class.newInstance()
        **/
    public PSP_HTMLAlerts()
    {
       super();
    }

    public PSP_HTMLAlerts( String pkg, String id ) throws RuntimePSPException
    {
        setResourceLocation(pkg, id);
    }


    public boolean test(HttpInput query_parameters, PlanServiceContext sc)
    {
        super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
        return false;  // This PSP is only accessed by direct reference.
    }

    private static UnaryPredicate getAlertPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Alert) {
              //System.out.println("ORG PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                          return true; }
                 return false;
          }
      };
    }


    /**
      **/
    public void execute( PrintStream out,
                          HttpInput query_parameters,
                          PlanServiceContext psc,
                          PlanServiceUtilities psu ) throws Exception
    {
         String value = (String)query_parameters.getFirstParameterToken("NUM", '=');
         int num = Integer.parseInt(value);

         out.println("<html><head>");
         out.println("<script language=\"JavaScript\">");
         out.println("var game = 0;");
         out.println("function scrollPage() {");
         out.println("game++;");
         out.println("if (game > 900)");
         out.println("    game = 0;");
         out.println("  self.scroll(0, game);");
         out.println("  setTimeout(\"scrollPage()\", 50);");
         out.println("}");
         out.println("</script>");
         out.println("<title>Active Alerts</title>");
         out.println("</head>");
         out.println("<body bgcolor=\"#ffffff\" onLoad=\"scrollPage()\">");
         out.println("<center>");
         out.println("<table border=0 cellpadding=0 cellspacing=0>");

         int i;
         for(i=0; i< num; i++)
         {
              out.println("<tr valign=top><td><img src=\"darkred.gif\" width=20 height=20></td><td width=10>&nbsp;</td><th align=left width=100><font face=\"Arial,Helvetica\" size=2>"
                    + generateRandomAlertType()
                    + "</th><td width=25>&nbsp;</td></tr>");
              out.println("<tr valign=top><td><img src=\"darkred.gif\" width=20 height=20></td><td width=10>&nbsp;</td><td align=left width=100><font face=\"Arial,Helvetica\" size=2>"
                    + generateRandomAlertText()
                    + "</td><td width=25>&nbsp;</td></tr>");

         }

         out.println("</table>");
         out.println("</center>");


         out.println("</body>");
         out.println("</html>");
    }


    private String generateRandomAlertType() {
        String type = null;
        int i = (int)(Math.random() * (double)4);
        switch(i){
           case 0:
             type = "Supply";
             break;
           case 1:
             type = "Shortage";
             break;
           case 2:
             type = "Transport";
             break;
           case 3:
             type = "Weather";
             break;
           default:
             type = "Medical";
      }

        return type;
    }

    private String generateRandomAlertText() {
        String text = null;
       int i = (int)(Math.random() * (double)8);
        switch(i){
           case 0:
             text = "Inventory Shortage at MCCGLOBALMODE";
             break;
           case 1:
             text = "Inventory Shortage at 3ID";
             break;
           case 2:
             text = "Inventory Shortage at 1BDE";
             break;
           case 3:
             text = "Inventory Shortage at 3ID";
             break;
           case 4:
             text = "Transportation Shortage at MCCGLOBALMODE";
             break;
           case 5:
             text = "Transportation Shortage at 3ID";
             break;
           case 6:
             text = "Transportation Shortage at 1BDE";
             break;
           case 7:
             text = "Transportation Shortage at 3ID";
             break;
           default:
             text = "Godzilla encountered";
        }
        return text;
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
}

