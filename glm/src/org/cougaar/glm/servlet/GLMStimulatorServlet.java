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
 * An extension of the GLMStimulator PlugIn that automatically
 * sends batches of a given task and keep track of the time it
 * takes to finish the batch.  
 * </pre>
 */
public class GLMStimulatorServlet extends ServletBase {
  public GLMStimulatorServlet(SimpleServletSupport support) {
    super(support);
  }

  public static final String INPUT_FILE = "inputFileName";
  public static final String NUM_TASKS  = "numberOfTasks";
  public static final String INTERVAL   = "interval";
  public static final String WAIT       = "wait";
  public static final boolean DEBUG = false;
  public static boolean VERBOSE = false;

  static {
    VERBOSE = Boolean.getBoolean("org.cougaar.glm.plugins.tools.GLMStimulatorServlet.verbose");
  }

  /*
  public void doGet(HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException {
    ServletWorker worker = createWorker ();

    if (VERBOSE) {
      Enumeration paramNames = request.getParameterNames();
      for (int i = 0; paramNames.hasMoreElements (); )
	System.out.println ("ServletBase got param #" + i++ + " - " + paramNames.nextElement ());
    }
    worker.execute (request, response, support);
  }
  */

  protected ServletWorker createWorker () {
    return new GLMStimulatorWorker (this);
  }

  public void getUsage (PrintWriter out, SimpleServletSupport support) {
    out.print("<HTML><HEAD><TITLE>GLMStimulatorServlet Usage</TITLE></HEAD><BODY>\n"+
	      "<H2><CENTER>GLMStimulatorServlet Usage</CENTER></H2><P>\n" +
	      "<FORM METHOD=\"GET\" ACTION=\"/$");
    out.print(support.getEncodedAgentName());
    out.print(support.getPath());
    // ask what task file 
    out.print("\">\n"+
	      "Inject tasks into agent<p>\n"+
	      "&nbsp;&nbsp;Input Task File <INPUT TYPE=\"text\" NAME=\"" + INPUT_FILE + "\" "+
	      "SIZE=40>");
    // get number of tasks to send 
    out.print("<P>\n"+
	      "&nbsp;&nbsp;Number of tasks <INPUT TYPE=\"text\" NAME=\"" + NUM_TASKS + "\" "+
	      "VALUE=\"1\">");
    // get periodicity 
    out.print("<P>\n"+
	      "&nbsp;&nbsp;Wait interval in millis<INPUT TYPE=\"text\" NAME=\"" + INTERVAL + "\" "+
	      "VALUE=\"1000\">");
    // choose whether to wait for being done, or not
    out.print("<P>\n"+
	      "&nbsp;&nbsp;Wait for task to complete" + 
	      "<INPUT TYPE=\"checkbox\" NAME=\"" + WAIT + "\" VALUE=\"true\">");
    // choose data format - html, xml, or java objects 
    out.print("<P>\nShow results as "+
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









