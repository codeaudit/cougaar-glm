/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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

//import org.cougaar.glm.callback.GLMOrganizationCallback;
//import org.cougaar.glm.callback.GLMOrganizationListener;
import org.cougaar.glm.util.AssetUtil;

import org.cougaar.planning.servlet.ServletBase;
import org.cougaar.planning.servlet.ServletWorker;
import org.cougaar.core.servlet.SimpleServletSupport;

/**
 * <pre>
 * A servlet that injects tasks into an agent.
 *
 * Parses an xml file that defines a batch of tasks.
 *
 * The servlet takes these parameters:
 * 1) The file name of the xml file
 *    URL param is inputFileName
 * 2) The number of batches of tasks to be sent (defaults to 1)
 *    URL param is numberOfBatches
 * 3) The interval to wait between batches being sent (defaults to 1 second)
 *    URL param is interval
 * 4) Whether to wait or not for each task to complete before sending the next
 *    This can be very useful for performance measurements.
 *    URL param is wait
 * 5) Which format you'd like the results back in : XML, HTML, or serialized objects
 *    The results, when you wait for each task to complete, is a list of completion times
 *    plus the total time to complete.  See GLMStimulatorResponseData for sample output.
 *
 * A typical URL would be :
 *
 * http://localhost:8800/$3-FSB/stimulator?inputFileName=Supply.dat.xml&tasksPerBatch=1&numberOfBatches=1&interval=1000&format=html
 *
 * It requires a special servlet component in the ini file (or equivalent CSMART entry) :
 *
 * plugin = org.cougaar.glm.servlet.GLMStimulatorServletComponent(org.cougaar.glm.servlet.GLMStimulatorServlet, /stimulator)
 *
 * Also, if the xml file that defines a task that needs a prototype that is not provided by the XMLPrototypeProvider, you 
 * may need to register the prototype using an xml file, e.g.:
 *
 * plugin = org.cougaar.lib.plugin.UTILLdmXMLPlugin(ldmFile={String}Ammo_AssetList.ldm.xml)
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

  public static final String FIRST_INTERVAL  = "firstInterval";
  public static final String SECOND_INTERVAL = "secondInterval";

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
