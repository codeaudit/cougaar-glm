/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import javax.swing.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.SubscriptionWatcher;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.servlet.data.Failure;
import org.cougaar.planning.servlet.data.xml.*;
import org.cougaar.planning.servlet.ServletBase;
import org.cougaar.planning.servlet.ServletWorker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.servlet.ServletUtil;

import org.cougaar.mlm.ui.psp.transit.DataComputer;
import org.cougaar.mlm.ui.psp.transit.Registry;
import org.cougaar.mlm.ui.psp.transit.RegistryCache;
import org.cougaar.mlm.ui.psp.transit.RegistryComputer;

import org.cougaar.util.Trigger;
import org.cougaar.util.TriggerModel;
import org.cougaar.util.SyncTriggerModelImpl;

import org.cougaar.lib.util.UTILAllocate;
import org.cougaar.glm.parser.GLMTaskParser;

import org.cougaar.core.service.SchedulerService;

/**
 *
 */
public class GLMStimulatorWorker
  extends ServletWorker {

  private static long MIN_INTERVAL = 20l;

  //  public static boolean VERBOSE = false;
  //  static {
  //    VERBOSE = Boolean.getBoolean("org.cougaar.glm.servlet.GLMStimulatorWorker.verbose");
  //  }

  public GLMStimulatorWorker (ServletBase servlet) {
    this.servlet = servlet;
  }

  /**
   * Here is our inner class that will handle all HTTP and
   * HTTPS service requests for our <tt>myPath</tt>.
   */
  public void execute(HttpServletRequest request, 
		      HttpServletResponse response,
		      SimpleServletSupport support) throws IOException, ServletException {
    Enumeration params = request.getParameterNames ();
    for (;params.hasMoreElements();) {
      String name  = (String) params.nextElement (); 
      String value = request.getParameter (name);
      getSettings (name, value);
    }

    // createGUI(inputFile);

    if (debug)
      System.out.println("GLMStimulatorWorker Invoked...");

    this.support = (GLMStimulatorSupport) support;

    if (wait) {
      // create a blackboard watcher
      watcher = 
	new SubscriptionWatcher() {
	    public void signalNotify(int event) {
	      // gets called frequently as the blackboard objects change
	      super.signalNotify(event);
	      tm.trigger ();
	    }
	    public String toString() {
	      return "ThinWatcher("+GLMStimulatorWorker.this.toString()+")";
	    }
	  };

      // create a callback for running this component
      Trigger myTrigger = 
	new Trigger() {
	    // no need to "sync" when using "SyncTriggerModel"
	    public void trigger() {
	      watcher.clearSignal(); 
	      if (wait)
		blackboardChanged ();
	    }
	    public String toString() {
	      return "Trigger("+GLMStimulatorWorker.this.toString()+")";
	    }
	  };

      this.tm = new SyncTriggerModelImpl(this.support.getSchedulerService(), myTrigger);
      this.support.getBlackboardService().registerInterest(watcher);
      // activate the trigger model
      tm.initialize();
      tm.load();
      tm.start();
    }

    // returns an "XMLable" result.
    // never returns
    XMLable result = getResult();

    if (result != null)
      writeResponse (result, response.getOutputStream(), request, support, format);
  }

  protected String getPrefix () { return "GLMStimulator at "; }

  /** 
   * use a query parameter to set a field 
   */
  public void getSettings(String name, String value) {
    super.getSettings (name, value);

    if (debug) 
      System.out.println ("GLMStimulatorWorker.getSettings - name " + name + " value " + value);

    if (eq (name, GLMStimulatorServlet.INPUT_FILE))
      inputFile = value;
    else if (eq (name, "debug"))
      debug = eq (value, "true");
    else if (eq (name, GLMStimulatorServlet.NUM_TASKS))
      totalBatches = Integer.parseInt(value);
    else if (eq (name, GLMStimulatorServlet.INTERVAL)) {
      try { 
	interval = Long.parseLong(value); 
	if (interval < MIN_INTERVAL)
	  interval = MIN_INTERVAL;
      } catch (Exception e) { interval = 1000l; }
    } else if (eq (name, GLMStimulatorServlet.WAIT)) {
      wait = eq (value, "true");
    }
  }

  /**
   * When the rescind task button is pressed, rescind the task.
   *
   * @param label provides way to give feedback
   */
  /*
  protected void rescindTasks (JLabel label) {
    if (tasksSent.size() == 0){
      label.setText("No tasks to Rescind.");
    } else {
      try {
	support.getBlackboardService().openTransaction();
	Iterator iter = tasksSent.iterator ();
	Object removed = iter.next ();
	iter.remove ();
		
	if (debug)
	  System.out.println ("GLMStimulatorWorker - Removing " + removed);
	publishRemove(removed);
	tasksSent.remove(removed);
	label.setText("Rescinded last task. " + tasksSent.size () + " left.");
      }catch (Exception exc) {
	System.err.println(exc.getMessage());
	exc.printStackTrace();
      } finally{
	support.getBlackboardService().closeTransaction(false);
      }
    }
  }
  */

  /**
   * @return an "XMLable" result.
   */
  protected XMLable getResult() {
    // Get name of XML data file
    if (support.getConfigFinder().locateFile (inputFile) == null) {
      if (debug)
	System.out.println("GLMStimulatorWorker Could not find the file " + inputFile);
      return new Message (inputFile);
    }

    testStart = new Date();

    sendTasks(true);

    while (batchesSent < totalBatches || 
	   (wait && !tasksSent.isEmpty ())) {
      if (debug)
	System.out.println ("GLMStimulatorWorker.getResult - batches so far " + batchesSent +
			    " < total " + totalBatches + " tasksSent " + tasksSent.size());
      if (wait) {
	try {
	  if (debug)
	    System.out.println ("GLMStimulatorWorker.getResult - waiting for blackboard to notify.");
	  synchronized (this) {
	    this.wait ();
	  }
	} catch (Exception e) {}
      } 
      else {
	if (System.currentTimeMillis () - sentTime > interval) {
	  // recordTime ();
	  if (debug)
	    System.out.println ("GLMStimulatorWorker.getResult - current-sentTime " + 
				(System.currentTimeMillis () - sentTime) +
				" > interval " + interval + " so sending next task.");
	  sendNextTask (true);
	} else {
	  long waitTime = interval - (System.currentTimeMillis () - sentTime);
	  if (debug)
	    System.out.println ("GLMStimulatorWorker.getResult - waiting wait time " + waitTime);
	  try { Thread.sleep (waitTime); } catch (Exception e) {}
	}
      }
    }

    return responseData;
  }

  private static class Message implements XMLable, Serializable {
    String file;
    public Message (String file) { this.file = file; }

    public void toXML(XMLWriter w) throws IOException{
      w.tagln("Error", "Couldn't find file " + file + ". Check path, try again.");
    }
  }

  protected void sendTasks (boolean withinTransaction) {
    batchStart = new Date();

    if (debug) System.out.println ("GLMStimulatorWorker.sendTasks - batch start " + batchStart);

    Collection tasks = null;
    //    tasksSent.clear();
    batchesSent++;

    try {
      if (withinTransaction)
	support.getBlackboardService().openTransaction();
      // Get the tasks out of the XML file
      tasks = readXmlTasks(inputFile);
      tasksSent.addAll (tasks);
      sentTime = System.currentTimeMillis ();
      for (Iterator iter = tasks.iterator(); iter.hasNext();) {
	Object task = iter.next ();
	support.getBlackboardService().publishAdd (task);
	if (wait) {
	  if (debug)
	    System.out.println ("GLMStimulatorWorker.sendTasks - subscribing for task " + task);
	  subscriptions.add(support.getBlackboardService().subscribe (new PlanElementPredicate ((Task)task))); 
	}
      }
    } catch (Exception exc) {
      System.err.println("Could not publish tasks.");
      System.err.println(exc.getMessage());
      exc.printStackTrace();
    }
    finally{
      if (withinTransaction)
	support.getBlackboardService().closeTransaction(false);
    }
  }

  protected void blackboardChanged () {
    try {
      //      if (debug)
      // System.out.println ("GLMStimulatorWorker.blackboard changed called.");

      support.getBlackboardService().openTransaction();
      Set toRemove = new HashSet (); // to avoid concurrent mod error
      for (Iterator iter = subscriptions.iterator (); iter.hasNext(); ) {
	IncrementalSubscription sub = (IncrementalSubscription) iter.next ();
	//	if (debug)
	//	  System.out.println ("GLMStimulatorWorker.blackboard checking subscription " + sub);
	if (sub.hasChanged ()) {
	  boolean wasEmpty = true;
	  Collection changedItems = sub.getChangedCollection();
	  for (Iterator iter2 = changedItems.iterator (); iter2.hasNext();) {
	    if (debug)
	      System.out.println ("GLMStimulatorWorker.blackboard changed - found changed plan elements.");
	    wasEmpty = false;
	    PlanElement pe = (PlanElement) iter2.next();
	    handleSuccessfulPlanElement (pe);
	    Task task = pe.getTask ();
	    tasksSent.remove (task);
	  }

	  if (!wasEmpty) {
	    support.getBlackboardService().unsubscribe (sub);
	    toRemove.add (sub);
	  }
	}
	//	else {
	  //	  if (debug)
	//	    System.out.println ("GLMStimulatorWorker.blackboard subscription " + sub + " has not changed.");
	//	}
      }
      subscriptions.removeAll (toRemove); // we've been notified about these, no reason to keep them around
      if (debug)
	System.out.println ("GLMStimulatorWorker.blackboard changed - notifying.");
      synchronized (this) {
	this.notify ();
      }
    } catch (Exception exc) {
      System.err.println("Could not publish tasks.");
      System.err.println(exc.getMessage());
      exc.printStackTrace();
    }
    finally{
      support.getBlackboardService().closeTransaction(false);
    }
  }

  /**
   * Everytime a successful allocation returns, we want to send a new batch of
   * the same tasks until the desired total has been sent.  
   *
   * Also keep track of the time spent on each batch.
   *
   * Cache the already handled allocations by their UID's
   */
  public void handleSuccessfulPlanElement(PlanElement planElement) {
    // we were rehydrated -- no guarantees across rehydration
    if (batchStart == null || testStart == null)
      return;

    if (!handledPlanElements.contains(planElement.getUID())) {
      // Cache the allocation UID
      handledPlanElements.add(planElement.getUID());
	
      recordTime ();
      sendNextTask (false);
    }
  }

  protected void recordTime () {
    // Print timing information for the completed batch
    String t = getElapsedTime(batchStart);
    String total = getElapsedTime(testStart);
    if (debug)
      System.out.println("\n*** Testing batch #" + (responseData.batchTimes.size() + 1) + 
			 " completed in " + t + " total " + total);

    // Cache the timing information
    responseData.batchTimes.add(t);
  }

  protected void sendNextTask (boolean withinTransaction) {
    // Send another batch if needed
    if (batchesSent < totalBatches) {
      sendTasks(withinTransaction);
    } else {
      printTestingSummary();
    }
  }

  /*
  protected void sendTasksAgain () {//String xmlTaskFile) {
    batchStart = new Date();

    Collection tasks = readXmlTasks(xmlTaskFile);
    try {
      support.getBlackboardService().openTransaction();
      for (Iterator iter = task.iterator(); iter.hasNext();)
	publishAdd (iter.next ());
    } catch (Exception exc) {
      System.err.println("Could not send tasks.");
      System.err.println(exc.getMessage());
      exc.printStackTrace();
    } finally{
      support.getBlackboardService().closeTransaction(false);
    }
  }
  */

  private static final class PlanElementPredicate implements UnaryPredicate {
    Task forTask;
    public PlanElementPredicate (Task forTask) { this.forTask = forTask; }
    public boolean execute(Object o) {
      if (!(o instanceof PlanElement))
	return false;

      PlanElement pe = (PlanElement) o;

      boolean taskWereLookingFor = (pe.getTask () == forTask);
      if (!taskWereLookingFor)
	return false;
      boolean hasReported = (pe.getReportedResult () != null);
      if (!hasReported)
	return false;
      boolean highConfidence = (pe.getReportedResult ().getConfidenceRating() >= UTILAllocate.HIGHEST_CONFIDENCE);
      if (!highConfidence)
	return false;
      return true;
    }
  };

  /**
   * get the allocation
   */
  /*
  protected PlanElement getPlanElement(SimpleServletSupport support, Task forTask) {
    // get self org
    Collection col = support.queryBlackboard (new PlanElementPredicate (forTask));
    if ((col != null) && 
        (col.size() == 1)) {
      Iterator iter = col.iterator();
      PlanElement planElement = (PlanElement) iter.next();
      return planElement;
    } else {
      return null;
    }
  }
  */

  protected String getElapsedTime (Date start) {
    Date end = new Date ();
    long diff = end.getTime () - start.getTime ();

    long min  = diff/60000l;
    long sec  = (diff - (min*60000l))/1000l;
    long millis = diff - (min*60000l) - (sec*1000l);
    return min + ":" + ((sec < 10) ? "0":"") + sec + ":" +
      ((millis < 10) ? "00": ((millis < 100) ? "0":"")) + millis;
  }

  protected void printTestingSummary() {
    String totalTime = getElapsedTime (testStart);
    responseData.totalTime = totalTime;

    if (debug) {
      System.out.println("\n************************* Testing Summary *************************\n");
      System.out.println("         Batch Number             Batch Time ");
      for (int i=0;i<responseData.batchTimes.size();i++)
	System.out.println("            " + (i+1) + "                       " + responseData.batchTimes.elementAt(i)); 
      System.out.println("\n         Total Time: " + totalTime);
      System.out.println("\n*******************************************************************\n");
    }
  }

  /**
   * Parse the xml file and return the COUGAAR tasks.
   *
   * @param  xmlTaskFile file defining tasks to stimulate cluster with
   * @return Collection of tasks defined in xml file
   */
  protected Collection readXmlTasks(String xmlTaskFile) {
    Collection tasks = null;
    try {
      GLMTaskParser tp = new GLMTaskParser(xmlTaskFile, 
					   support.getLDMF(), 
					   support.getAgentIdentifier(),
					   support.getConfigFinder(),
					   support.getLDM());
      tasks = UTILAllocate.enumToList (tp.getTasks());
    } 
    catch( Exception ex ) {
      System.err.println(ex.getMessage());
      ex.printStackTrace();
    }
    return tasks;
  }

  private class ResponseData implements XMLable, Serializable {
    //XMLable members:
    //----------------
    //    public List messages = new ArrayList ();
    public String totalTime;
    public Vector batchTimes = new Vector();

    //    public void addMessage (String message) { messages.add (message); }

    /**
     * Write this class out to the Writer in XML format
     * @param w output Writer
     **/
    public void toXML(XMLWriter w) throws IOException{
      w.optagln("results", "totalTime", totalTime);
      for (int i=0;i<batchTimes.size();i++)
	w.sitagln("batch", 
		  "id", ""+(i+1), 
		  "time", (String)batchTimes.elementAt(i)); 
      w.cltagln("results");
    }
  }

  // rely upon load-time introspection to set these services - 
  //   don't worry about revokation.
  public final void setSchedulerService(SchedulerService ss) {
    scheduler = ss;
  }

  /** frame for 2-button UI */
  //  JFrame frame;

  /** Collection of tasks that have been sent.  Needed for later rescinds */
  protected Collection tasksSent = new HashSet();

  protected SubscriptionWatcher watcher;
  protected TriggerModel tm;
  private SchedulerService scheduler;

  protected Date testStart = null;
  protected Date batchStart = null;
  protected Collection handledPlanElements = new HashSet();
  protected ResponseData responseData=new ResponseData();

  //  protected JLabel myLabel = null;
  //  protected String myXmlTaskFile = "";
  protected int batchesSent = 0;
  protected int totalBatches = 0;
  protected long interval;
  protected long sentTime;
  protected String inputFile = "                     ";
  protected ServletBase servlet;
  protected GLMStimulatorSupport support;
  protected boolean debug = true;
  protected boolean wait = false;
  protected Set subscriptions = new HashSet();
}
