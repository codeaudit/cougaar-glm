/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.servlet;

import java.io.PrintWriter;

import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.planning.servlet.ServletBase;
import org.cougaar.planning.servlet.ServletWorker;

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
