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
import java.util.*;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.DynamicUnaryPredicate;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.SubscriptionWatcher;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.servlet.data.xml.*;
import org.cougaar.planning.servlet.ServletBase;
import org.cougaar.planning.servlet.ServletWorker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.cougaar.core.servlet.SimpleServletSupport;

import org.cougaar.util.Trigger;
import org.cougaar.util.TriggerModel;
import org.cougaar.util.SyncTriggerModelImpl;
import org.cougaar.util.log.NullLogger;

import org.cougaar.lib.util.UTILAllocate;
import org.cougaar.glm.parser.GLMTaskParser;

import org.cougaar.core.service.SchedulerService;
import org.cougaar.core.service.BlackboardService;

/**
 * <pre>
 * One created for every URL access.
 *
 * If either waitBefore or waitAfter is true, uses a blackboard
 * service, a watcher, and a trigger to monitor published tasks to see
 * when they are complete.
 * </pre>
 **/
public class GLMStimulatorWorker
  extends ServletWorker {

  /** no batch interval can be less than this number */
  private static long MIN_INTERVAL = 20l; // millis

  /**
   * Here is our inner class that will handle all HTTP and
   * HTTPS service requests.
   *
   * If we should wait until the batch is complete, we make a watcher to watch
   * the blackboard, a trigger that will call blackboardChanged (), and a trigger model
   * to connect them.
   *
   * @see #blackboardChanged
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

    if (debug)
      System.out.println("GLMStimulatorWorker Invoked...");

    this.support = (GLMStimulatorSupport) support;

    if (waitBefore || waitAfter) {
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
            blackboardChanged();
          }
          public String toString() {
            return "Trigger("+GLMStimulatorWorker.this.toString()+")";
          }
        };

      this.tm = new SyncTriggerModelImpl(this.support.getSchedulerService(), myTrigger);
      this.support.getBlackboardService().registerInterest(watcher);
      this.support.getBlackboardService().openTransaction();
      planElementSubscription = (IncrementalSubscription)
        this.support.getBlackboardService().subscribe(planElementPredicate);
      this.support.getBlackboardService().closeTransaction();
      // activate the trigger model
      tm.initialize();
      tm.load();
      tm.start();
    }

    // returns an "XMLable" result.
    XMLable result = getResult();

    if (result != null)
      writeResponse (result, response.getOutputStream(), request, support, format);
    if (waitBefore || waitAfter) {
      BlackboardService bb = this.support.getBlackboardService();
      bb.openTransaction();

      if (rescindAfterComplete) {
	for (Iterator i = rescindTasks.iterator(); i.hasNext(); ) {
	  bb.publishRemove(i.next());
	  i.remove();
	}
      }

      rescindTasks.clear();

      bb.unsubscribe(planElementSubscription);
      bb.closeTransaction();
    }
  }

  // unused
  protected String getPrefix () { return "GLMStimulator at "; }

  /**
   * <pre>
   * Use a query parameter to set a field
   *
   * Sets the recognized parameters : inputFile, debug, totalBatches, tasksPerBatch, interval, and wait
   * </pre>
   */
  public void getSettings(String name, String value) {
    super.getSettings (name, value);

    if (debug)
      System.out.println ("GLMStimulatorWorker.getSettings - name " + name + " value " + value);

    if (eq (name, GLMStimulatorServlet.INPUT_FILE))
      inputFile = value;
    else if (eq (name, "debug"))
      debug = eq (value, "true");
    else if (eq (name, GLMStimulatorServlet.NUM_BATCHES))
      totalBatches = Integer.parseInt(value);
    else if (eq (name, GLMStimulatorServlet.TASKS_PER_BATCH))
      tasksPerBatch = Integer.parseInt(value);
    else if (eq (name, GLMStimulatorServlet.INTERVAL)) {
      try {
        interval = Long.parseLong(value);
        if (interval < MIN_INTERVAL)
          interval = MIN_INTERVAL;
      } catch (Exception e) { interval = 1000l; }
    } else if (eq (name, GLMStimulatorServlet.WAIT_BEFORE)) {
      waitBefore = eq (value, "true");
    } else if (eq (name, GLMStimulatorServlet.WAIT_AFTER)) {
      waitAfter = eq (value, "true");
    } else if (eq (name, GLMStimulatorServlet.RESCIND_AFTER_COMPLETE)) {
      rescindAfterComplete = eq (value, "true");
    } else if (eq (name, GLMStimulatorServlet.USE_CONFIDENCE)) {
      useConfidence = eq (value, "true");
    }
  }

  /**
   * When the rescind task button is pressed, rescind the task.
   *
   * @param label provides way to give feedback
   */
  /*
  protected void rescindTasks (JLabel label) {
    if (sentTasks.size() == 0){
      label.setText("No tasks to Rescind.");
    } else {
      try {
        support.getBlackboardService().openTransaction();
        Iterator iter = sentTasks.iterator ();
        Object removed = iter.next ();
        iter.remove ();

        if (debug)
          System.out.println ("GLMStimulatorWorker - Removing " + removed);
        publishRemove(removed);
        sentTasks.remove(removed);
        label.setText("Rescinded last task. " + sentTasks.size () + " left.");
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
   * Main work done here. <p>
   *
   * Sends the first batch of tasks, and keeps sending until totalBatches have been
   * sent.  If should wait for completion, waits until notified by blackboardChanged (). <p>
   *
   * Will wait <b>interval</b> milliseconds between batches if there are
   * more than one batches to send.
   *
   * @see #getSettings
   * @see #blackboardChanged
   * @return an "XMLable" result.
   */
  protected XMLable getResult() {
    // Get name of XML data file
    if (inputFile == null || inputFile.equals("") || support.getConfigFinder().locateFile (inputFile) == null) {
      if (debug)
        System.out.println("GLMStimulatorWorker Could not find the file [" + inputFile + "]");
      return new Message (inputFile);
    }

    testStart = System.currentTimeMillis(); // First send is immediate
    nextSendTime = testStart;

    while (batchesSent < totalBatches) {
      if (debug)
        System.out.println ("GLMStimulatorWorker.getResult - batches so far " + batchesSent +
                            " < total " + totalBatches + " sentTasks " + sentTasks.size());
      if (waitBefore) {
        synchronized (sentTasks) {
          while (!sentTasks.isEmpty()) {
            // Wait for previously sent tasks to complete
            try {
              if (debug)
                System.out.println ("GLMStimulatorWorker.getResult - waiting for blackboard to notify.");
              sentTasks.wait();
            } catch (Exception e) {}
          }
        }
      }
      long waitTime = nextSendTime - System.currentTimeMillis();
      if (waitTime > 0L) {
        // Need to wait a while
        if (debug)
          System.out.println ("GLMStimulatorWorker.getResult - waiting wait time " + waitTime);
        try {
          Thread.sleep(waitTime);
        } catch (Exception e) {
        }
      }
      sendNextBatch(true);
      nextSendTime += interval;
    }

    if (waitAfter || waitBefore) {
      synchronized (sentTasks) {
        while (!sentTasks.isEmpty()) {
          try {
            if (debug)
              System.out.println ("GLMStimulatorWorker.getResult - waiting for tasks to complete.");
            sentTasks.wait();
          } catch (Exception e) {}
        }
      }
    }

    return responseData;
  }

  /** tiny little class for sending back a message when it can't find the file */
  private static class Message implements XMLable, Serializable {
    String file;
    public Message (String file) { this.file = file; }

    public void toXML(XMLWriter w) throws IOException{
      w.tagln("Error", "Couldn't find file " + file + ". Check path, try again.");
    }
  }

  /**
   * Publishes the tasks created by readXmlTasks. <p>
   *
   * For each task, adds to sentTasks map of task to its send time. 
   * sentTasks is used later by blackboardChanged to determine how long the
   * task took to complete.
   *
   * @see #readXmlTasks
   * @see #blackboardChanged
   * @param withinTransaction - true when called from handleSuccessfulPlanElement
   *                            this avoids having nested transactions
   */
  protected void sendNextBatch(boolean withinTransaction) {
    Date batchStart = new Date();

    if (debug) System.out.println ("GLMStimulatorWorker.sendTasks - batch start " + batchStart);

    batchesSent++;

    try {
      if (withinTransaction)
        support.getBlackboardService().openTransaction();
      for (int i = 0; i < tasksPerBatch; i++) {
        // Get the tasks out of the XML file
        Collection theseTasks = readXmlTasks(inputFile);
        for (Iterator it = theseTasks.iterator(); it.hasNext(); ) {
          Task task = (Task) it.next();
          sentTasks.put(task, batchStart);
          support.getBlackboardService().publishAdd(task);
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
                                           support.getLDM(),
					   NullLogger.getNullLogger()); // THIS SHOULD BE CHANGED ONCE THERE IS LOGGING SUPPORT
      tasks = UTILAllocate.enumToList (tp.getTasks());
    }
    catch( Exception ex ) {
      System.err.println(ex.getMessage());
      ex.printStackTrace();
    }
    return tasks;
  }

  /**
   * Called when one of the tasks that was added to the blackboard has
   * it's plan element change. If the task has been successfully
   * disposed, it is removed from the sentTasks Map.
   **/
  protected void blackboardChanged() {
    try {
      support.getBlackboardService().openTransaction();
      Set toRemove = new HashSet (); // to avoid concurrent mod error
      if (planElementSubscription.hasChanged()) {
        boolean wasEmpty = true;
        Collection changedItems = planElementSubscription.getChangedCollection();
        synchronized (sentTasks) {
          for (Iterator iter2 = changedItems.iterator (); iter2.hasNext();) {
            if (debug)
              System.out.println ("GLMStimulatorWorker.blackboard changed - found changed plan elements.");
            wasEmpty = false;
            PlanElement pe = (PlanElement) iter2.next();
            Task task = pe.getTask();
            Date timeSent = (Date) sentTasks.get(task);
            if (timeSent != null) {
              recordTime(task.getUID().toString(), timeSent);
            }
            sentTasks.remove(task);
            rescindTasks.add(task);
          }
          if (debug)
            System.out.println ("GLMStimulatorWorker.blackboard changed - notifying.");
          synchronized (this) {
            sentTasks.notify();
          }
        }
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

  /** records the time taken for the task */
  protected void recordTime (String taskUID, Date sentTime) {
    // Print timing information for the completed batch
    long now = System.currentTimeMillis();
    long elapsed = now - sentTime.getTime();
    String t = getElapsedTime(elapsed);
    String total = getElapsedTime(now - testStart);
    if (debug)
      System.out.println("\n*** Testing batch #" + (responseData.taskTimes.size() + 1) +
                         " completed in " + t + " total " + total);

    // Cache the timing information
    responseData.addTaskAndTime(taskUID, t, elapsed);
  }

  /** encodes a time interval in a min:sec:millis format */
  protected String getElapsedTime(long diff) {
    long min  = diff/60000l;
    long sec  = (diff - (min*60000l))/1000l;
    long millis = diff - (min*60000l) - (sec*1000l);
    return min + ":" + ((sec < 10) ? "0":"") + sec + ":" +
      ((millis < 10) ? "00": ((millis < 100) ? "0":"")) + millis;
  }

  /** record the elapsed time */
  protected void printTestingSummary() {
    String totalTime = getElapsedTime(System.currentTimeMillis() - testStart);
    responseData.totalTime = totalTime;

    if (debug)
      System.out.println(responseData);
  }

  // rely upon load-time introspection to set these services -
  //   don't worry about revokation.
  public final void setSchedulerService(SchedulerService ss) {
    scheduler = ss;
  }

  /**
   * Matches PlanElements for tasks that we have sent and which are
   * complete, i.e. have plan elements and 100% confident reported
   * allocation results. Both Failure and Success are accepted.
   **/
  private DynamicUnaryPredicate planElementPredicate = new DynamicUnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof PlanElement) {
        PlanElement pe = (PlanElement) o;
        Task task = pe.getTask();
        if (sentTasks.containsKey(task)) {
	  if (useConfidence) {
	    boolean hasReported = (pe.getReportedResult () != null);
	    if (!hasReported)
	      return false;
	    boolean highConfidence = (pe.getReportedResult ().getConfidenceRating() >= UTILAllocate.HIGHEST_CONFIDENCE);
	    if (!highConfidence)
	      return false;
	  }
          return true;          // Ignore confidence for now.
        }
      }
      return false;
    }
  };

  /**
   * Subscription to PlanElements disposing of our tasks
   **/
  private IncrementalSubscription planElementSubscription;

  /** Collection of tasks that have been sent.  Needed for later rescinds */
  protected Map sentTasks = new HashMap();
  protected Set rescindTasks = new HashSet();

  /** for waiting for a subscription on the blackboard */
  protected SubscriptionWatcher watcher;
  /** for waiting for a subscription on the blackboard */
  protected TriggerModel tm;
  /** for waiting for a subscription on the blackboard */
  private SchedulerService scheduler;

  /** start of the whole test */
  protected long testStart;

  /** returned response */
  protected GLMStimulatorResponseData responseData=new GLMStimulatorResponseData();

  /** batches sent so far */
  protected int batchesSent = 0;
  /** tasks per batch */
  protected int tasksPerBatch = 1;
  /** total batches requested */
  protected int totalBatches = 0;
  /** millis between batches */
  protected long interval;
  /** dump debug output if true */
  protected boolean debug = false;

  /** wait for completion before publishing next batch */
  protected boolean waitBefore = false;

  /** wait for completion after publishing everything */
  protected boolean waitAfter = false;

  /** when was the last batch sent */
  protected long nextSendTime;

  /** use confidence to determine when task is complete */
  protected boolean useConfidence;

  /** remove the injected tasks after they have been completed */
  protected boolean rescindAfterComplete;

  protected String inputFile = "                     ";

  protected GLMStimulatorSupport support;
}
