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

import org.cougaar.glm.callback.GLMOrganizationCallback;
import org.cougaar.glm.callback.GLMOrganizationListener;
import org.cougaar.glm.parser.GLMTaskParser;
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
 * http://localhost:8800/$3-FSB/stimulator?inputFileName=Supply.dat.xml&numberOfBatches=1&interval=1000&format=html
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
public class GLMStimulatorServlet extends ServletBase {
  public GLMStimulatorServlet(SimpleServletSupport support) {
    super(support);
  }

  public static final String INPUT_FILE = "inputFileName";
  public static final String NUM_BATCHES = "numberOfBatches";
  public static final String INTERVAL   = "interval";
  public static final String WAIT       = "wait";
  public static final boolean DEBUG = false;
  public static boolean VERBOSE = false;

  static {
    VERBOSE = Boolean.getBoolean("org.cougaar.glm.plugins.tools.GLMStimulatorServlet.verbose");
  }

  protected ServletWorker createWorker () {
    return new GLMStimulatorWorker ();
  }

  /** initial page presented when the servlet is called with no arguments */
  public void getUsage (PrintWriter out, SimpleServletSupport support) {
    out.print("<HTML><HEAD><TITLE>GLMStimulatorServlet</TITLE></HEAD><BODY>\n"+
	      "<H2><CENTER>GLMStimulatorServlet</CENTER></H2><P>\n" +
	      "<FORM METHOD=\"GET\" ACTION=\"/$");
    out.print(support.getEncodedAgentName());
    out.print(support.getPath());
    // ask what task file 
    out.print("\">\n"+
	      "<font size=+1>Inject tasks into agent <b>" + support.getAgentIdentifier() + "</b></font><p>\n"+
	      "&nbsp;&nbsp;Input Task File <INPUT TYPE=\"text\" NAME=\"" + INPUT_FILE + "\" "+
	      "SIZE=40>");
    // get number of tasks to send 
    out.print("<P>\n"+
	      "&nbsp;&nbsp;Number of batches <INPUT TYPE=\"text\" NAME=\"" + NUM_BATCHES + "\" "+
	      "VALUE=\"1\">");
    // get periodicity 
    out.print("<P>\n"+
	      "&nbsp;&nbsp;Wait interval between batches<INPUT TYPE=\"text\" NAME=\"" + INTERVAL + "\" "+
	      "VALUE=\"1000\">&nbsp;millis");
    // choose whether to wait for being done, or not
    out.print("<P>\n"+
	      "&nbsp;&nbsp;Wait for tasks to complete" + 
	      "<INPUT TYPE=\"checkbox\" NAME=\"" + WAIT + "\" VALUE=\"true\">&nbsp;" +
	      "<br>&nbsp;&nbsp;(A complete task has a plan element with a 100% confidence reported allocation result.)");
    // choose data format - html, xml, or java objects 
    out.print("<P>\n&nbsp;&nbsp;Show results as "+
	      "&nbsp;&nbsp;<INPUT TYPE=\"radio\" NAME=\"format\" "+
	      "VALUE=\"html\" CHECKED>"+
	      "&nbsp;html ");
    out.print("<INPUT TYPE=\"radio\" NAME=\"format\" "+
	      "VALUE=\"xml\">"+
	      "&nbsp;xml ");
    out.print("<INPUT TYPE=\"radio\" NAME=\"format\" "+
	      "VALUE=\"data\">"+
	      "&nbsp;serialized Java objects ");
    out.print("<P>\n"+
	      "<INPUT TYPE=\"submit\" NAME=\"Inject\">\n"+
	      "</FORM></BODY></HTML>");
  }
}









