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

package org.cougaar.glm.servlet;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;

import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.Filters;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.TimeSpan;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.Date;

import java.io.PrintWriter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;

import org.cougaar.glm.util.AssetUtil;

import org.cougaar.planning.servlet.ServletBase;
import org.cougaar.planning.servlet.ServletWorker;
import org.cougaar.core.servlet.SimpleServletSupport;

/**
 * <pre>
 * A servlet that watches for when a batch of tasks have completed
 * and returns the time it took to complete.
 * 
 * See the documentation in glm/doc.
 *
 * Gordon Vidaver
 * gvidaver@bbn.com
 * (617) 873-3558
 * </pre>
 */
public class CompletionWatcherServlet extends ServletBase {
  public void setSimpleServletSupport(SimpleServletSupport support) {
    super.setSimpleServletSupport(support);
  }

  public static final String FIRST_INTERVAL   = "firstInterval";
  public static final String SECOND_INTERVAL  = "secondInterval";
  public static final String VERBS_TO_INCLUDE = "verbsToInclude";

  public static final boolean DEBUG = false;
  public static boolean VERBOSE = false;

  static {
    VERBOSE = Boolean.getBoolean("org.cougaar.glm.servlet.CompletionWatcherServlet.verbose");
  }

  protected ServletWorker createWorker () {
    return new CompletionWatcherWorker ();
  }

  /** initial page presented when the servlet is called with no arguments */
  public void getUsage (PrintWriter out, SimpleServletSupport support) {
    out.print("<HTML><HEAD><TITLE>CompletionWatcherServlet</TITLE></HEAD><BODY>\n"+
	      "<H2><CENTER>CompletionWatcherServlet</CENTER></H2><P>\n" +
	      "<FORM METHOD=\"GET\" ACTION=\"/$");
    out.print(support.getEncodedAgentName());
    out.print(support.getPath());
    out.print("\">\n");

    out.print("<font size=+1>Wait for agent <b>" + support.getAgentIdentifier() + "</b> to complete.</font><p>\n");
    String agentPath = support.getEncodedAgentName () + support.getPath();
    out.print("<img alt='Watcher Servlet Screen Shot' " + 
	      "src='/$" + agentPath + "?getImage=true'><br>");
    out.print("<center><font size=+1>Although typically used to time the period between GLSInit and when the agent is done,<br>");
    out.print("the servlet measures any busy period between two quiet periods.</font></center><p>");
    out.print("Note that the servlet may not return if it cannot go through its states.<p>");

    out.println ("<table>\n");

    // get seconds to wait for initial quiet period
    out.println ("<tr><td>");
    out.print("First quiet period duration (in seconds)");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + FIRST_INTERVAL + "\" "+
	      "VALUE=\"10\">");
    out.println("</td></tr>");

    // get seconds to wait for initial quiet period
    out.print("<tr><td>");
    out.print("Second quiet period duration (in seconds)");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + SECOND_INTERVAL + "\" "+
	      "VALUE=\"10\">");
    out.println("</td></tr>");

    // get verbs to include
    out.print("<tr><td>");
    out.print("Verbs of tasks to examine (empty means all).  Comma separated.");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + VERBS_TO_INCLUDE + "\" "+
	      "VALUE=\"\">");
    out.println("</td></tr>");

    out.println ("</table><p>");

    // choose data format - html, xml, or java objects 
    out.print("<center>Show results as "+
	      "&nbsp;&nbsp;<INPUT TYPE=\"radio\" NAME=\"format\" "+
	      "VALUE=\"html\" CHECKED>"+
	      "&nbsp;html&nbsp;");
    out.print("<INPUT TYPE=\"radio\" NAME=\"format\" "+
	      "VALUE=\"xml\">&nbsp;xml&nbsp;");
    out.print("<INPUT TYPE=\"radio\" NAME=\"format\" "+
	      "VALUE=\"data\">"+
	      "&nbsp;serialized Java objects<p>\n");
    out.print("<INPUT TYPE=\"submit\" NAME=\"Inject\"></center>\n"+
	      "</FORM></BODY></HTML>");
  }
}
