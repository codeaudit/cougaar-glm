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

import java.io.PrintWriter;

import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.planning.servlet.ServletBase;
import org.cougaar.planning.servlet.ServletWorker;

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
 * It requires a special servlet component in the ini file (or equivalent XML entry) :
 *
 * plugin = org.cougaar.planning.servlet.BlackboardServletComponent(org.cougaar.glm.servlet.GLMStimulatorServlet, /stimulator)
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
  public void setSimpleServletSupport(SimpleServletSupport support) {
    super.setSimpleServletSupport(support);
  }

  public static final String INPUT_FILE      = "inputFileName";
  public static final String FOR_PREP        = "forPrep";
  public static final String NUM_BATCHES     = "numberOfBatches";
  public static final String TASKS_PER_BATCH = "tasksPerBatch";
  public static final String INTERVAL        = "interval";
  public static final String WAIT_BEFORE     = "waitBefore";
  public static final String WAIT_AFTER      = "waitAfter";
  public static final String RESCIND_AFTER_COMPLETE = "rescindAfterComplete";
  public static final String USE_CONFIDENCE  = "useConfidence";
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
    out.print("\">\n");
    // ask what task file 
    out.print("<font size=+1>Inject tasks into agent <b>" + support.getAgentIdentifier() + "</b></font><p>\n");

    out.println ("<table>\n");

    // ask what task file 
    out.println ("<tr><td>");
    out.print("Input Task File");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + INPUT_FILE + "\" SIZE=40>");
    out.println("</td></tr>");

    // allow optional override of file's FOR prep
    out.println ("<tr><td>");
    out.print("Override \"FOR\" prepositional phrase value");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + FOR_PREP + "\" SIZE=40>");
    out.println("</td></tr>");

    // get number of batches to send 
    out.println ("<tr><td>");
    out.print("Number of batches");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + NUM_BATCHES + "\" "+
	      "VALUE=\"1\">");
    out.println("</td></tr>");

    // get number of tasks per batch
    out.print("<tr><td>");
    out.print("Tasks per batch");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + TASKS_PER_BATCH + "\" "+
	      "VALUE=\"1\">");
    out.println("</td></tr>");

    // get wait interval
    out.println ("<tr><td>");
    out.print("Wait interval between batches");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"text\" NAME=\"" + INTERVAL + "\" "+
	      "VALUE=\"1000\">&nbsp;millis");
    out.println("</td></tr>");

    // choose whether to wait for batch completion, or not
    out.println ("<tr><td>");
    out.print("Wait for each batch to complete before sending more");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"checkbox\" NAME=\"" + WAIT_BEFORE + "\" VALUE=\"true\">");
    out.println("</td></tr>");

    // choose whether to wait for being done, or not
    out.println ("<tr><td>");
    out.print("Wait for all tasks to complete before returning results");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"checkbox\" NAME=\"" + WAIT_AFTER + "\" VALUE=\"true\">");
    out.println("</td></tr>");

    // choose whether to rescind tasks after injecting them
    out.println ("<tr><td>");
    out.print("Remove injected tasks after all complete");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"checkbox\" NAME=\"" + RESCIND_AFTER_COMPLETE + "\" VALUE=\"true\">");
    out.println("</td></tr>");

    // choose whether to use 100% confidence as completion test
    out.println ("<tr><td>");
    out.print("A task is complete when it has a 100% confident reported result. " +
	      "<br>Otherwise waits only until plan element is attached.");
    out.print("</td><td>");
    out.print("<INPUT TYPE=\"checkbox\" NAME=\"" + USE_CONFIDENCE + "\" VALUE=\"true\">");
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
